package io.zink.boson.bson.codec.impl

import io.zink.boson.bson.codec._
import io.zink.boson.bson.bsonImpl.Dictionary._

import scala.collection.{JavaConverters, mutable}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.parallel.immutable.ParSeq
import scala.util.{Failure, Success, Try}
import collection.JavaConverters._



class CodecJson(str: String) extends Codec {
  //Structure to deal with the input
  val input: Array[Char] = str.toCharArray
  val inputRead: mutable.Buffer[Char] = mutable.Buffer.empty
  var readerIndex: Int = 0
  var writerIndex: Int = str.length - 1
  val keysList: ListBuffer[String] = new ListBuffer[String]
  val valuesList: ListBuffer[String] = new ListBuffer[String]

  override def getReaderIndex: Int = readerIndex
  override def setReaderIndex(value: Int): Unit = if(value>=0){readerIndex = value
  ////println(new String(input.slice(readerIndex, input.length)))
  }else{
    readerIndex+=value
  }
  override def getWriterIndex: Int = writerIndex
  override def setWriterIndex(value: Int): Unit =
    if(value>=0)
      writerIndex = value
    else {
      //Nao faz nada
    }

  override def getToken(tkn: SonNamedType): SonNamedType = tkn match{
    case SonObject(request,_)=>
      request match {
        case C_DOT =>
          SonObject(request, new String(input))
        case "object" =>
          //println(new String(input.slice(readerIndex, input.length)))
          val size = findObjectSize(input.slice(readerIndex, input.length),'{','}')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          //println(new String(subStr1))
          SonObject(request, new String(subStr1))

      }
    case SonArray(request,_)=>
      request match {
        case C_DOT =>
          SonArray(request,  new String(input))
        case "array" =>
          //println(new String(input.slice(readerIndex, input.length)))
          val size = findObjectSize(input.slice(readerIndex, input.length),'[',']')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          //println(new String(subStr1))
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
              //readerIndex+=name.length
              SonString(request, new String(name))
          }
        case "string" =>
          val index =  input.slice(readerIndex, input.length).indexOf('\"')
          //println(index)
          val rI = readerIndex+index
          val endIndex = input.slice(rI+1, input.length).indexOf('\"')
          val subStr1 = input.slice(rI, rI+endIndex+2)
          ////println( new String(subStr1))
          ////println( new String(subStr1.slice(1, subStr1.length-1)))

          SonString(request, new String(subStr1.slice(1, subStr1.length-1)))

      }
    case SonNumber(request,_)=>
      request match {
        case "int"=>
          //          val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
          //          val subStr = input.slice(readerIndex, indexMin)
          //          readerIndex+=indexMin
          val subStr1 = getNextNumber
          ////println("Int="+ new String(subStr1).toInt)
          SonNumber(request, new String(subStr1).toInt)

        case "double"=>
          //          val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
          //          val subStr = input.slice(readerIndex, indexMin)
          //          readerIndex+=indexMin
          val subStr1 = getNextNumber
          ////println("Int="+ new String(subStr1).toInt)
          SonNumber(request, new String(subStr1).toDouble)

        case "long"=>
          //          val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
          //          val subStr = input.slice(readerIndex, indexMin)
          //          readerIndex+=indexMin
          val subStr1 = getNextNumber
          ////println("Int="+ new String(subStr1).toInt)
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
          //println("object")
          //println(new String(input.slice(readerIndex, input.length)))
          val size = findObjectSize(input.slice(readerIndex, input.length),'{','}')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          readerIndex+=size
          //println(new String(subStr1))
          SonObject(request, new String(subStr1))
      }

    case SonArray(request,_)=>
      request match {
        case C_DOT =>
          SonArray(request,  new String(input))
        case "array" =>
          //println(new String(input.slice(readerIndex, input.length)))
          val size = findObjectSize(input.slice(readerIndex, input.length),'[',']')
          val subStr1 = input.slice(readerIndex, readerIndex+size)
          readerIndex+=size
          //println(new String(subStr1))
          SonArray(request, new String(subStr1))
      }
    case SonString(request,_)=>
      request match {
        case "name"=>
          //println("name")
          //println(new String(input.slice(readerIndex, input.length)))
          if(input.apply(readerIndex).equals(',')|input.apply(readerIndex).equals('{')) readerIndex+=1
          //println(input.apply(readerIndex))
          input.apply(readerIndex) match {
            case '\"' =>
              val subStr = input.slice(readerIndex+1, input.length).indexOf('\"')
              val name = input.slice(readerIndex, readerIndex+subStr+2)
              readerIndex+=name.length
              SonString(request, new String(name.slice(1, name.length-1)))

          }
        case "string" =>
          val index =  input.slice(readerIndex, input.length).indexOf('\"')
          //println(index)
          readerIndex+=index
          val endIndex = input.slice(readerIndex+1, input.length).indexOf('\"')
          val subStr1 = input.slice(readerIndex, readerIndex+endIndex+2)
          ////println( new String(subStr1))
          ////println( new String(subStr1.slice(1, subStr1.length-1)))
          readerIndex+=subStr1.length
          SonString(request, new String(subStr1.slice(1, subStr1.length-1)))


      }
    case SonNumber(request,_)=>
      request match {
        case "int"=>
//          val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
//          val subStr = input.slice(readerIndex, indexMin)
//          readerIndex+=indexMin
          val subStr1 = readNextNumber
          ////println("Int="+ new String(subStr1).toInt)
          SonNumber(request, new String(subStr1).toInt)

        case "double"=>
//          val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
//          val subStr = input.slice(readerIndex, indexMin)
//          readerIndex+=indexMin
          val subStr1 = readNextNumber
          ////println("Int="+ new String(subStr1).toInt)
          SonNumber(request, new String(subStr1).toDouble)

        case "long"=>
//          val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
//          val subStr = input.slice(readerIndex, indexMin)
//          readerIndex+=indexMin
          val subStr1 = readNextNumber
          ////println("Int="+ new String(subStr1).toInt)
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
    ////println("Int="+ new String(subStr1).toInt)
    subStr1
  }
  def readNextNull : Array[Char] = {

    val indexMin = List(input.slice(readerIndex, input.length).indexOf(','),input.slice(readerIndex, input.length).indexOf('}'),input.slice(readerIndex, input.length).indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, readerIndex+indexMin)
    readerIndex+=indexMin
    val subStr1 = subStr.dropWhile(p => !p.equals('n'))
    ////println("Int="+ new String(subStr1).toInt)
    subStr1
  }

  def readNextNumber : Array[Char] = {
    //println("readNextNumber")
    //val input0 = input.slice(readerIndex, input.length)
      //var i =0
      while(!input.apply(readerIndex).isDigit){
        println("passa aqui")
        readerIndex+=1
      }

    val indexMin = List(input.slice(readerIndex, input.length).indexOf(','),input.slice(readerIndex, input.length).indexOf('}'),input.slice(readerIndex, input.length).indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, readerIndex+indexMin)
    println(new String(subStr))
    readerIndex+=indexMin
    val subStr1 = subStr.dropWhile(p => !p.isDigit)
    ////println("Int="+ new String(subStr1).toInt)
    subStr1
  }
  def getNextBoolean : Array[Char] = {
    val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.equals('t') && !p.equals('f'))
    ////println("Int="+ new String(subStr1).toInt)
    subStr1
  }
  def getNextNull : Array[Char] = {
    val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.equals('n'))
    ////println("Int="+ new String(subStr1).toInt)
    subStr1
  }
  def getNextNumber : Array[Char] = {
    val indexMin = List(input.indexOf(','),input.indexOf('}'),input.indexOf(']')).filter(n => n>=0).min
    val subStr = input.slice(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.isDigit)
    ////println("Int="+ new String(subStr1).toInt)
    subStr1
  }
  override def getSize: Int = this.readSize

  override def readSize: Int = {
    //val firstChar: Char =
      input.apply(readerIndex) match {
        case '{' | '[' if readerIndex==0 => input.length
        case '{' =>
          //TODO Find the closing bracket and return size
          val inputAux: Array[Char] = input.slice(readerIndex, input.length)
          val size = findObjectSize(inputAux, '{', '}')
          println(new String(input.slice(readerIndex, readerIndex+size)), size)
          size

        case '[' =>
          //TODO Find the closing bracket and return size
          val inputAux: Array[Char] = input.slice(readerIndex, input.length)
          val size = findObjectSize(inputAux, '[', ']')
          println(new String(input.slice(readerIndex, readerIndex+size)), size)
          size
        case '\"' =>
          //TODO Find the closing Quotes and return size
          val inputAux: Array[Char] = input.slice(readerIndex, input.length)
          val size = findStringSize(inputAux, '\"')
          println(new String(input.slice(readerIndex, readerIndex+size)), size)
          size
        case _ =>
          readerIndex+=1
          val s = readSize
          //readerIndex-=1
          s+1

      }
  }

  def findObjectSize(input: Array[Char], chO: Char, chC: Char): Int = {
    var counter: Int = 1
    var i = 1
    while(counter!=0){
      print(input.apply(i) + " ")
      val aux = input.apply(i) match {
        case x if x.equals(chO) => 1
        case x if x.equals(chC) => -1
        case _ => 0
      }
      counter+=aux
      i+=1
    }
    //println()
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
    if(input.apply(readerIndex).equals(',')/*|input.apply(readerIndex).equals('{')*/) readerIndex+=1
    input.apply(readerIndex) match{
      case '}' | ']' =>
        readerIndex+=1
        D_ZERO_BYTE
      case '\"' =>
        val rIndexAux = readerIndex+1
        val finalIndex: Int = input.slice(rIndexAux, input.length).indexOf('\"')
        val value0 = input.slice(readerIndex, finalIndex)
        ////println(input.apply(rIndexAux+finalIndex+1))
        input.apply(rIndexAux+finalIndex+1) match {
          case ':'=>
            //readerIndex+=1
            val a = input.slice(rIndexAux+finalIndex+2, input.length)
            ////println(a.apply(0))
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
                ////println(bindex)
                val inputAux = input.slice(index, index+bindex)
                ////println(new String(inputAux))
                if(!inputAux.contains('.')){
                  Try(new String(inputAux).toInt) match {
                    case Success(v)=> D_INT//int
                    case Failure(_) => D_LONG//long
                  }
                }else D_FLOAT_DOUBLE//DOUBLE
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
        ////println(bindex)
        val inputAux = input.slice(readerIndex, readerIndex+bindex)
        ////println(new String(inputAux))
        if(!inputAux.contains('.')){
          Try(new String(inputAux).toInt) match {
            case Success(v)=> D_INT//int
            case Failure(_) => D_LONG//long
          }
        }else D_FLOAT_DOUBLE//DOUBLE
    }
  }







  override def duplicate: Codec = {
    val newCodec = new CodecJson(str)
    newCodec.setReaderIndex(readerIndex)
    newCodec.setWriterIndex(writerIndex)
    newCodec
  }

  override def printCodec(): Unit = println(str.substring(readerIndex, str.length))

  override def release(): Unit = {}

  override def getArrayPosition: Int = {
    val substr = input.reverse.slice(input.length-readerIndex, input.length)
    ////println(new String(substr))
    val index = substr.indexOf('[')
    substr.slice(0, index).count(p => p.equals(','))
  }

  override def readArrayPosition: Int ={
    val substr = input.reverse.slice(input.length-readerIndex, input.length)
    ////println(new String(substr))
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


      ///.count(p => p.equals(','))
    //???
    //println(res)
    res
  }

  override def downOneLevel: Unit = {
    if(input.apply(readerIndex).equals(':'))readerIndex+=1

    readerIndex+=1
  }
}



//case class MyParser() extends RegexParsers{
//  val P_NUMBER: String = """\d+(\.\d*)?"""
//  val P_WORD: String =  """[/^[?a-zA-Z\u00C0-\u017F]+\d_-]+"""
//  val P_CLOSE_ARRAY: String = "\\]"
//  val P_OPEN_ARRAY: String = "\\["
//  val P_CLOSE_BRACKET: String = "\\}"
//  val P_OPEN_BRACKET: String = "\\{"
//  val P_QM: String = "\""
//  val DOUBLEDOT: String = ":"
//  val ARROW: String = "->"
//  val COMMA: String = ","
//  val digits: Regex = P_NUMBER.r
//  val word: Regex = P_WORD.r
//  def comma: Parser[COMMA_S] = COMMA.r ^^ {_ => COMMA_S()}
//  def doubledot: Parser[DOUBLE_DOT_S] = DOUBLEDOT.r ^^ {_ => DOUBLE_DOT_S()}
//  def arrow: Parser[ARROW_S] = ARROW.r ^^{_ => ARROW_S()}
//  def open_brackets_array: Parser[OPEN_BA] = P_OPEN_ARRAY.r ^^ {_ => OPEN_BA()}
//  def close_brackets_array: Parser[CLOSE_BA] = P_CLOSE_ARRAY.r ^^ {_ => CLOSE_BA()}
//  def open_brackets: Parser[OPEN_B] = P_OPEN_BRACKET.r ^^ {_ => OPEN_B()}
//  def close_brackets: Parser[CLOSE_B] = P_CLOSE_BRACKET.r ^^ {_ => CLOSE_B()}
//  def key: Parser[KEY] = P_QM ~> word <~ P_QM  ^^ {k => KEY(k)}
//  def numbers: Parser[NUMBER] = digits  ^^ {k => NUMBER(k)}
//  def entry: Parser[ENTRY] = ((key ~ (DOUBLEDOT|ARROW) ~ key)|(key ~ (DOUBLEDOT|ARROW) ~ jsonA)|(key ~ (DOUBLEDOT|ARROW) ~ jsonO)|(key ~ (DOUBLEDOT|ARROW) ~ numbers)) ^^ {
//    case k0 ~ _ ~ k1  if k1.isInstanceOf[KEY] | k1.isInstanceOf[JSONO] | k1.isInstanceOf[JSONA] | k1.isInstanceOf[NUMBER] => ENTRY(k0, k1)}
//  def jsonO: Parser[JSONO] = P_OPEN_BRACKET ~> rep(entry ~ opt(COMMA)) <~ P_CLOSE_BRACKET ^^ {list =>      JSONO(list.map(e => e._1))}
//  def jsonA: Parser[JSONA] = P_OPEN_ARRAY ~> rep((key|jsonA|jsonO|numbers ) ~ opt(COMMA)) <~ P_CLOSE_ARRAY ^^ {    list => JSONA(list.map(e => e._1))}
//  def root: Parser[ROOT] = (jsonO | jsonA) ^^ {root => ROOT(root)}
//
//
//  def program: Parser[Program] =
//    (open_brackets
//      | open_brackets_array
//      | close_brackets
//      | close_brackets_array
//      | key
//      | doubledot
//      | arrow
//      | comma
//      | numbers
//      //| entry
//      //| key
//      //| numbers
//      )^^ { s => {
//      new Program(List(s)) }
//    }
//
//}