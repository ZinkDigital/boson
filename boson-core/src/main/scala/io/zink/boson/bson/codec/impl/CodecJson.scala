package io.zink.boson.bson.codec.impl

import io.zink.boson.bson.codec._
import io.zink.boson.bson.bsonImpl.Dictionary._

import scala.collection.{JavaConverters, mutable}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.parallel.immutable.ParSeq
import scala.util.{Failure, Success, Try}
import collection.JavaConverters._



class CodecJson(str: String) extends Codec {
  //val input: Array[Char] = str.toCharArray
  val input: StringBuilder = StringBuilder.newBuilder
  input.append(str)
  val inputSize: Int = input.length
  
  var readerIndex: Int = 0
  var writerIndex: Int = str.length - 1


  override def getReaderIndex: Int = readerIndex

  override def setReaderIndex(value: Int): Unit = if(value>=0){readerIndex = value
  }else{
    readerIndex+=value
  }
  
  override def getWriterIndex: Int = writerIndex

  override def setWriterIndex(value: Int): Unit = if(value>=0)writerIndex = value

  override def getToken(tkn: SonNamedType): SonNamedType = tkn match{
    case SonObject(request,_)=>
      request match {
        case C_DOT =>
          SonObject(request, input.mkString)
        case CS_OBJECT =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view,CS_OPEN_BRACKET, CS_CLOSE_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex+size)
          SonObject(request, subStr1)
      }
    case SonArray(request,_)=>
      request match {
        case C_DOT =>
          SonArray(request,  input.mkString)
        case CS_ARRAY =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view,CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex+size)
          SonArray(request, subStr1)
      }
    case SonString(request,_)=>
      request match {
        case CS_NAME=>
          val charSliced: Char = input(readerIndex)
         val ri = if(charSliced == CS_COMMA || charSliced == CS_OPEN_BRACKET) readerIndex+1 else readerIndex
          input(ri) match {
            case CS_QUOTES =>
              val subStr = input.substring(ri+1, inputSize).view.indexOf(CS_QUOTES)
              val name = input.substring(ri, subStr+2)
              SonString(request, name)
          }
        case CS_STRING =>
          val index =  input.substring(readerIndex, inputSize).view.indexOf(CS_QUOTES)
          val rI = readerIndex+index
          val endIndex = input.substring(rI+1, inputSize).view.indexOf(CS_QUOTES)
          val subSize = endIndex+2
          val subStr1 = input.substring(rI, rI+subSize)
          SonString(request, subStr1.substring(1, subSize-1))
      }
    case SonNumber(request,_)=>
      request match {
        case CS_INTEGER=>
          val subStr1 = getNextNumber
          SonNumber(request, subStr1.toInt)
        case CS_DOUBLE=>
          val subStr1 = getNextNumber
          SonNumber(request, subStr1.toDouble)
        case CS_LONG =>
          val subStr1 = getNextNumber
          SonNumber(request, subStr1.toLong)
      }
    case SonNull(request,_ )=>
      request match {
        case CS_NULL =>
          val subStr1 = getNextNull
          SonNull(request, V_NULL)
      }
    case SonBoolean(request,_) =>
      val subStr1 = if(getNextBoolean.equals(CS_TRUE)) 1 else 0
      SonBoolean(request, subStr1)
  }

  override def readToken(tkn: SonNamedType): SonNamedType = tkn match{
    case SonObject(request,_)=>
      request match {
        case C_DOT =>
          SonObject(request, input.mkString)
        case CS_OBJECT =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view,CS_OPEN_BRACKET, CS_CLOSE_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex+size)
          readerIndex+=size
          SonObject(request, subStr1)
      }
    case SonArray(request,_)=>
      request match {
        case C_DOT =>
          SonArray(request,  input.mkString)
        case CS_ARRAY =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view,CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex+size)
          readerIndex+=size
          SonArray(request, subStr1)
      }
    case SonString(request,_)=>
      request match {
        case CS_NAME=>
          val charSliced: Char = input(readerIndex)
          if(charSliced == CS_COMMA||charSliced == CS_OPEN_BRACKET) readerIndex+=1
          input(readerIndex) match {
            case CS_QUOTES =>
              val subStr = input.substring(readerIndex+1, inputSize).indexOf(CS_QUOTES)
              val name = input.substring(readerIndex, readerIndex+subStr+2)
              readerIndex+=name.length
              SonString(request, name.substring(1, name.length-1))
          }
        case CS_STRING =>
          val index =  input.substring(readerIndex, inputSize).indexOf(CS_QUOTES)
          readerIndex+=index
          val endIndex = input.substring(readerIndex+1, inputSize).indexOf(CS_QUOTES)
          val subStr1 = input.substring(readerIndex, readerIndex+endIndex+2)
          readerIndex+=subStr1.length
          SonString(request, subStr1.substring(1, subStr1.length-1))
      }
    case SonNumber(request,_)=>
      request match {
        case CS_INTEGER=>
          val subStr1 = readNextNumber
          SonNumber(request, subStr1.toInt)
        case CS_DOUBLE=>
          val subStr1 = readNextNumber
          SonNumber(request, subStr1.toDouble)
        case CS_LONG=>
          val subStr1 = readNextNumber
          SonNumber(request, subStr1.toLong)
      }
    case SonNull(request,_ )=>
      request match {
        case CS_NULL =>
          val subStr1 = readNextNull
          SonNull(request, V_NULL)
      }
    case SonBoolean(request,_) =>
      val subStr1 = if(readNextBoolean.equals(CS_TRUE)) 1 else 0
      SonBoolean(request, subStr1)
  }

  def readNextBoolean : String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n>=0).min
    val subStr = input.substring(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr dropWhile (p => !p.equals(CS_T) && !p.equals(CS_F))
    subStr1
  }

  def readNextNull : String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n>=0).min
    val subStr = input.substring(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr dropWhile (p => !p.isDigit)
    subStr1
  }

  def readNextNumber : String = {
    while(!input(readerIndex).isDigit){readerIndex+=1}
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n>=0).min
    val subStr = input.substring(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr.dropWhile(p => !p.isDigit)
    subStr1
  }

  def getNextBoolean : String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n>=0).min
    val subStr = input.substring(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.equals(CS_T) && !p.equals(CS_F))
    subStr1
  }

  def getNextNull : String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n>=0).min
    val subStr = input.substring(readerIndex, indexMin)
    val subStr1 = subStr dropWhile (p => !p.isDigit)
    subStr1
  }

  def getNextNumber : String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n>=0).min
    val subStr = input.substring(readerIndex, indexMin)
    val subStr1 = subStr dropWhile (p => !p.isDigit)
    subStr1
  }

  override def getSize: Int = this.readSize

  override def readSize: Int = {
      input(readerIndex) match {
        case CS_OPEN_BRACKET | CS_OPEN_RECT_BRACKET if readerIndex==0 => inputSize
        case CS_OPEN_BRACKET =>
          val inputAux: Seq[Char] = input.substring(readerIndex, inputSize).view
          val size = findObjectSize(inputAux, CS_OPEN_BRACKET, CS_CLOSE_BRACKET)
          size
        case CS_OPEN_RECT_BRACKET =>
          val inputAux: Seq[Char] = input.substring(readerIndex, inputSize)
          val size = findObjectSize(inputAux, CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
          size
        case CS_QUOTES =>
          val inputAux: Seq[Char] = input.substring(readerIndex, inputSize)
          val size = findStringSize(inputAux, CS_QUOTES)
          size
        case _ =>
          readerIndex+=1
          val s = readSize
          s+1
      }
  }

  def findObjectSize(input: Seq[Char], chO: Char, chC: Char): Int = {
    var counter: Int = 1
    var i = 1
    while(counter!=0){
      val aux = input(i) match {
        case x if x.equals(chO) => 1
        case x if x.equals(chC) => -1
        case _ => 0
      }
      counter+=aux
      i+=1
    }
    i
  }

  def findStringSize(input: Seq[Char], ch: Char): Int = {
    var counter: Int = 1
    var i = 1
    while(counter!=0){
      val aux = input(i) match {
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
      case CS_OPEN_BRACKET=> SonObject(C_DOT)
      case CS_OPEN_RECT_BRACKET=> SonArray(C_DOT)
      case _ => SonZero
    }
  }

  override def getValueAt(i: Int): Int = {
    val value = input(i)
    if(value.equals(CS_CLOSE_BRACKET)||value.equals(CS_CLOSE_RECT_BRACKET)){
      0
    }else{
      value
    }
  }

  override def getDataType: Int = this.readDataType

  override  def readDataType: Int = {
    if(readerIndex==0)readerIndex+=1
    if(input(readerIndex).equals(CS_COMMA)) readerIndex+=1
    input(readerIndex) match{
      case CS_CLOSE_BRACKET | CS_CLOSE_RECT_BRACKET =>
        readerIndex+=1
        D_ZERO_BYTE
      case CS_QUOTES =>
        val rIndexAux = readerIndex+1
        val finalIndex: Int = input.substring(rIndexAux, inputSize).indexOf(CS_QUOTES)
        //val value0 = input.substring(readerIndex, finalIndex)
        input(rIndexAux+finalIndex+1) match {
          case CS_2DOT=>
            val a = input.substring(rIndexAux+finalIndex+2, inputSize)
            a(0) match{
              case CS_QUOTES => D_ARRAYB_INST_STR_ENUM_CHRSEQ
              case CS_OPEN_BRACKET => D_BSONOBJECT
              case CS_OPEN_RECT_BRACKET =>D_BSONARRAY
              case CS_T=>D_BOOLEAN
              case CS_F=>D_BOOLEAN
              case CS_N=> D_NULL
              case x if x.isDigit =>
                val index = rIndexAux+finalIndex+2
                lazy val strSliced = input.substring(index, inputSize)
                val bindex = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(v => v>0).min
                val inputAux = input.substring(index, index+bindex)
                if(!inputAux.contains(CS_DOT)){
                  Try(inputAux.toInt) match {
                    case Success(v)=> D_INT
                    case Failure(_) => D_LONG
                  }
                }else D_FLOAT_DOUBLE
            }
          case _ => D_ARRAYB_INST_STR_ENUM_CHRSEQ
        }
      case CS_OPEN_BRACKET => D_BSONOBJECT
      case CS_OPEN_RECT_BRACKET =>D_BSONARRAY
      case CS_T=>D_BOOLEAN
      case CS_F=>D_BOOLEAN
      case CS_N=> D_NULL
      case x if x.isDigit =>
        lazy val strSliced = input.substring(readerIndex, inputSize)
        val bindex = List(strSliced.view.indexOf(CS_COMMA),strSliced.view.indexOf(CS_CLOSE_BRACKET),strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(v => v>0).min
        val inputAux = input.substring(readerIndex, readerIndex+bindex)
        if(!inputAux.contains(CS_DOT)){
          Try(inputAux.toInt) match {
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

  override def downOneLevel: Unit = {
    if(input(readerIndex).equals(CS_2DOT))readerIndex+=1
    readerIndex+=1
  }

//  override def getArrayPosition: Int = {
//    lazy val substr = input.reverse.substring(inputSize-readerIndex, inputSize)
//    val index = substr.view.indexOf(CS_OPEN_RECT_BRACKET)
//    substr.substring(0, index).count(p => p.equals(CS_COMMA))
//  }
//
//  override def readArrayPosition: Int ={
//    val substr = input.reverse.substring(inputSize-readerIndex, inputSize)
//    val index = substr.view.indexOf(CS_OPEN_RECT_BRACKET)
//    val str = substr.substring(0, index).view
//    val list = StringBuilder.newBuilder
//    var a = 0
//    var i = 0
//    while(i!=str.length){
//      str(i)match{
//        case x if x.equals(CS_OPEN_BRACKET)||x.equals(CS_OPEN_RECT_BRACKET) =>
//          list.append(x)
//          a+=1
//        case  x if x.equals(CS_CLOSE_BRACKET)||x.equals(CS_CLOSE_RECT_BRACKET) =>
//          list.append(x)
//          a-=1
//        case x =>
//          if(a==0) list.append(x) else list.append(CS_ZERO)
//      }
//      i+=1
//    }
//    val res = list.count(p => p.equals(CS_COMMA))
//    res
//  }

  override def getArrayPosition: Unit = {}

  override def readArrayPosition: Unit ={}

  override def consumeValue(seqType: Int): Unit = seqType match{
    case D_FLOAT_DOUBLE=>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value>=0).min
      readerIndex+=size

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ=>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value>=0).min
      readerIndex+=size
//    case D_BSONOBJECT=> ???
//    case D_BSONARRAY=> ???
    case D_BOOLEAN=>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value>=0).min
      readerIndex+=size
    case D_NULL=>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value>=0).min
      readerIndex+=size
    case D_INT=>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value>=0).min
      readerIndex+=size
    case D_LONG=>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value>=0).min
      readerIndex+=size
  }
}

