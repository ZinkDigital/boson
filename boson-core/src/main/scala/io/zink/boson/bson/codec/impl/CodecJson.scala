package io.zink.boson.bson.codec.impl

import io.zink.boson.bson.codec._
import io.zink.boson.bson.bsonImpl.Dictionary._

import scala.collection.{JavaConverters, mutable}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.parallel.immutable.ParSeq
import scala.util.{Failure, Success, Try}
import collection.JavaConverters._



class CodecJson(str: String) extends Codec {
  val input: Array[Char] = str.toCharArray
  var readerIndex: Int = 0
  var writerIndex: Int = str.length - 1


  override def getReaderIndex: Int = readerIndex

  override def setReaderIndex(value: Int): Unit = if(value>=0){readerIndex = value
  }else{
    readerIndex+=value
  }
  override def getWriterIndex: Int = writerIndex

  override def setWriterIndex(value: Int): Unit =
    if(value>=0)
      writerIndex = value

  override def getToken(tkn: SonNamedType): SonNamedType = tkn match{
    case SonObject(request,_)=>
      request match {
        case C_DOT =>
          SonObject(request, new String(input))
        case "object" =>
          val size = findObjectSize(input.slice(readerIndex, input.length),'{','}')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          SonObject(request, new String(subStr1))
      }
    case SonArray(request,_)=>
      request match {
        case C_DOT =>
          SonArray(request,  new String(input))
        case "array" =>
          val size = findObjectSize(input.slice(readerIndex, input.length),'[',']')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          SonArray(request, new String(subStr1))
      }
    case SonString(request,_)=>
      request match {
        case "name"=>
         val ri = if(input.apply(readerIndex).equals(',')|input.apply(readerIndex).equals('{')) readerIndex+1 else readerIndex
          input.apply(ri) match {
            case '\"' =>
              val subStr = input.slice(ri+1, input.length).indexOf('\"')
              val name = input.slice(ri, subStr+2)
              SonString(request, new String(name))
          }
        case "string" =>
          val index =  input.slice(readerIndex, input.length).indexOf('\"')
          val rI = readerIndex+index
          val endIndex = input.slice(rI+1, input.length).indexOf('\"')
          val subStr1 = input.slice(rI, rI+endIndex+2)
          SonString(request, new String(subStr1.slice(1, subStr1.length-1)))
      }
    case SonNumber(request,_)=>
      request match {
        case "int"=>
          val subStr1 = getNextNumber
          SonNumber(request, new String(subStr1).toInt)
        case "double"=>
          val subStr1 = getNextNumber
          SonNumber(request, new String(subStr1).toDouble)
        case "long"=>
          val subStr1 = getNextNumber
          SonNumber(request, new String(subStr1).toLong)
      }
    case SonNull(request,_ )=>
      request match {
        case "null" =>
          val subStr1 = getNextNull
          SonNull(request, V_NULL)
      }
    case SonBoolean(request,_) =>
      val subStr1 = if(new String(getNextBoolean).equals("true")) 1 else 0
      SonBoolean(request, subStr1)
  }

  override def readToken(tkn: SonNamedType): SonNamedType = tkn match{
    case SonObject(request,_)=>
      request match {
        case C_DOT =>
          SonObject(request, new String(input))
        case "object" =>
          val size = findObjectSize(input.slice(readerIndex, input.length),'{','}')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          readerIndex+=size
          SonObject(request, new String(subStr1))
      }
    case SonArray(request,_)=>
      request match {
        case C_DOT =>
          SonArray(request,  new String(input))
        case "array" =>
          val size = findObjectSize(input.slice(readerIndex, input.length),'[',']')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          readerIndex+=size
          SonArray(request, new String(subStr1))
      }
    case SonString(request,_)=>
      request match {
        case "name"=>
          if(input.apply(readerIndex).equals(',')|input.apply(readerIndex).equals('{')) readerIndex+=1
          input.apply(readerIndex) match {
            case '\"' =>
              val subStr = input.slice(readerIndex+1, input.length).indexOf('\"')
              val name = input.slice(readerIndex, readerIndex+subStr+2)
              readerIndex+=name.length
              SonString(request, new String(name.slice(1, name.length-1)))
          }
        case "string" =>
          val index =  input.slice(readerIndex, input.length).indexOf('\"')
          readerIndex+=index
          val endIndex = input.slice(readerIndex+1, input.length).indexOf('\"')
          val subStr1 = input.slice(readerIndex, readerIndex+endIndex+2)
          readerIndex+=subStr1.length
          SonString(request, new String(subStr1.slice(1, subStr1.length-1)))
      }
    case SonNumber(request,_)=>
      request match {
        case "int"=>
          val subStr1 = readNextNumber
          SonNumber(request, new String(subStr1).toInt)
        case "double"=>
          val subStr1 = readNextNumber
          SonNumber(request, new String(subStr1).toDouble)
        case "long"=>
          val subStr1 = readNextNumber
          SonNumber(request, new String(subStr1).toLong)
      }
    case SonNull(request,_ )=>
      request match {
        case "null" =>
          val subStr1 = readNextNull
          SonNull(request, V_NULL)
      }
    case SonBoolean(request,_) =>
      val subStr1 = if(new String(readNextBoolean).equals("true")) 1 else 0
      SonBoolean(request, subStr1)
  }

  def readNextBoolean : Array[Char] = {
    val indexMin = List(input.slice(readerIndex, input.length).indexOf(','),input.slice(readerIndex, input.length).indexOf('}'),input.slice(readerIndex, input.length).indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr.dropWhile(p => !p.equals('t') && !p.equals('f'))
    subStr1
  }

  def readNextNull : Array[Char] = {
    val indexMin = List(input.slice(readerIndex, input.length).indexOf(','),input.slice(readerIndex, input.length).indexOf('}'),input.slice(readerIndex, input.length).indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr.dropWhile(p => !p.equals('n'))
    subStr1
  }

  def readNextNumber : Array[Char] = {
    while(!input.apply(readerIndex).isDigit){readerIndex+=1}
    val indexMin = List(input.slice(readerIndex, input.length).indexOf(','),input.slice(readerIndex, input.length).indexOf('}'),input.slice(readerIndex, input.length).indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr.dropWhile(p => !p.isDigit)
    subStr1
  }

  def getNextBoolean : Array[Char] = {
    val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.equals('t') && !p.equals('f'))
    subStr1
  }

  def getNextNull : Array[Char] = {
    val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.equals('n'))
    subStr1
  }

  def getNextNumber : Array[Char] = {
    val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.isDigit)
    subStr1
  }

  override def getSize: Int = this.readSize

  override def readSize: Int = {
      input.apply(readerIndex) match {
        case '{' | '[' if readerIndex==0 => input.length
        case '{' =>
          val inputAux: Array[Char] = input.slice(readerIndex, input.length)
          val size = findObjectSize(inputAux, '{', '}')
          size
        case '[' =>
          val inputAux: Array[Char] = input.slice(readerIndex, input.length)
          val size = findObjectSize(inputAux, '[', ']')
          size
        case '\"' =>
          val inputAux: Array[Char] = input.slice(readerIndex, input.length)
          val size = findStringSize(inputAux, '\"')
          size
        case _ =>
          readerIndex+=1
          val s = readSize
          s+1
      }
  }

  def findObjectSize(input: Array[Char], chO: Char, chC: Char): Int = {
    var counter: Int = 1
    var i = 1
    while(counter!=0){
      val aux = input.apply(i) match {
        case x if x.equals(chO) => 1
        case x if x.equals(chC) => -1
        case _ => 0
      }
      counter+=aux
      i+=1
    }
    i
  }

  def findStringSize(input: Array[Char], ch: Char): Int = {
    var counter: Int = 1
    var i = 1
    while(counter!=0){
      val aux = input.apply(i) match {
        case x if x.equals(ch) => -1
        case _ => 0
      }
      counter+=aux
      i+=1
    }
    i
  }

  override def rootType: SonNamedType = {
    input.head match {
      case '{'=> SonObject(C_DOT)
      case '['=> SonArray(C_DOT)
      case _ => SonZero
    }
  }

  override def getValueAt(i: Int): Int = {
    val value = input.apply(i)
    if(value.equals('}')|value.equals(']')){
      0
    }else{
      value
    }
  }

  override def getDataType: Int = this.readDataType

  override  def readDataType: Int = {
    if(readerIndex==0)readerIndex+=1
    if(input.apply(readerIndex).equals(',')) readerIndex+=1
    input.apply(readerIndex) match{
      case '}' | ']' =>
        readerIndex+=1
        D_ZERO_BYTE
      case '\"' =>
        val rIndexAux = readerIndex+1
        val finalIndex: Int = input.slice(rIndexAux, input.length).indexOf('\"')
        val value0 = input.slice(readerIndex, finalIndex)
        input.apply(rIndexAux+finalIndex+1) match {
          case ':'=>
            val a = input.slice(rIndexAux+finalIndex+2, input.length)
            a.apply(0) match{
              case '\"' => D_ARRAYB_INST_STR_ENUM_CHRSEQ
              case '{' => D_BSONOBJECT
              case '[' =>D_BSONARRAY
              case 't'=>D_BOOLEAN
              case 'f'=>D_BOOLEAN
              case 'n'=> D_NULL
              case x if x.isDigit =>
                val index = rIndexAux+finalIndex+2
                val bindex = List(input.slice(index, input.length).indexOf(','),input.slice(index, input.length).indexOf('}'),input.slice(index, input.length).indexOf(']')).filter(v => v>0).min
                val inputAux = input.slice(index, index+bindex)
                if(!inputAux.contains('.')){
                  Try(new String(inputAux).toInt) match {
                    case Success(v)=> D_INT
                    case Failure(_) => D_LONG
                  }
                }else D_FLOAT_DOUBLE
            }
          case _ => D_ARRAYB_INST_STR_ENUM_CHRSEQ
        }
      case '{' => D_BSONOBJECT
      case '[' =>D_BSONARRAY
      case 't'=>D_BOOLEAN
      case 'f'=>D_BOOLEAN
      case 'n'=> D_NULL
      case x if x.isDigit =>
        val bindex = List(input.slice(readerIndex, input.length).indexOf(','),input.slice(readerIndex, input.length).indexOf('}'),input.slice(readerIndex, input.length).indexOf(']')).filter(v => v>0).min
        val inputAux = input.slice(readerIndex, readerIndex+bindex)
        if(!inputAux.contains('.')){
          Try(new String(inputAux).toInt) match {
            case Success(v)=> D_INT
            case Failure(_) => D_LONG
          }
        }else D_FLOAT_DOUBLE
    }
  }

  override def duplicate: Codec = {
    val newCodec = new CodecJson(str)
    newCodec.setReaderIndex(readerIndex)
    newCodec.setWriterIndex(writerIndex)
    newCodec
  }

  //override def printCodec(): Unit = println(str.substring(readerIndex, str.length))

  override def release(): Unit = {}

  override def getArrayPosition: Int = {
    val substr = input.reverse.slice(input.length-readerIndex, input.length)
    val index = substr.indexOf('[')
    substr.slice(0, index).count(p => p.equals(','))
  }

  override def readArrayPosition: Int ={
    val substr = input.reverse.slice(input.length-readerIndex, input.length)
    val index = substr.indexOf('[')
    val str = substr.slice(0, index)
    val list: ListBuffer[Char] = new ListBuffer[Char]
    var a = 0
    var i = 0
    while(i!=str.length){
      str.apply(i)match{
        case x if x.equals('{')|x.equals('[') =>
          list.append(x)
          a+=1
        case  x if x.equals('}')|x.equals(']') =>
          list.append(x)
          a-=1
        case x =>
          if(a==0) list.append(x) else list.append('0')
      }
      i+=1
    }
    val res = list.toArray.count(p => p.equals(','))
    res
  }

  override def downOneLevel: Unit = {
    if(input.apply(readerIndex).equals(':'))readerIndex+=1
    readerIndex+=1
  }
}

