package io.zink.boson.bson.codec.impl

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.codec._
import io.zink.boson.bson.bsonImpl.Dictionary.{CS_INTEGER, _}

import scala.collection.{JavaConverters, mutable}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.parallel.immutable.ParSeq
import scala.util.{Failure, Success, Try}
import collection.JavaConverters._


/**
  * Class that represents the Codec to deal with Json Values
  *
  * @param str is the Json received by the user
  */
class CodecJson(str: String) extends Codec {
  /**
    * input is a value of type StringBuilder to process the value received by the user
    */
  val input: StringBuilder = StringBuilder.newBuilder
  input.append(str)
  /**
    * inputSize is a constant with the size of the input
    * this constant is used several times along the code
    */
  val inputSize: Int = input.length

  /**
    * readerIndex and writerIndex are two var's used to maintain the actual reading and writing position, trying to mimic the functionality
    * of the ByteBuf's from Netty
    */
  var readerIndex: Int = 0
  var writerIndex: Int = str.length - 1

  /**
    * getReaderIndex is used to get the actual reader index position in the stream
    *
    * @return an Int representing the position on the stream
    */
  override def getReaderIndex: Int = readerIndex

  /**
    * setReaderIndex is used to set the reader index position in the stream
    *
    * @param value is the new value of the reader index
    */
  override def setReaderIndex(value: Int): Unit = if (value >= 0) {
    readerIndex = value
  } else {
    readerIndex += value
  }

  /**
    * getWriterIndex is used to get the actual writer index position in the stream
    *
    * @return An Int representing the position on the stream
    */
  override def getWriterIndex: Int = writerIndex

  /**
    * setWriterIndex is used to set the writer index position in the stream
    *
    * @param value is the new value of the writer index
    */
  override def setWriterIndex(value: Int): Unit = if (value >= 0) writerIndex = value

  /**
    * getToken is used to obtain a value correponding to the SonNamedType request, without consuming the value from the stream
    *
    * @param tkn is a value from out DSL trait representing the requested type
    * @return returns the same SonNamedType request with the value obtained.
    */
  override def getToken(tkn: SonNamedType): SonNamedType = tkn match {
    case SonObject(request, _) =>
      request match {
        case C_DOT =>
          SonObject(request, input.mkString)
        case CS_OBJECT =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view, CS_OPEN_BRACKET, CS_CLOSE_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex + size)
          SonObject(request, subStr1)
      }
    case SonArray(request, _) =>
      request match {
        case C_DOT =>
          SonArray(request, input.mkString)
        case CS_ARRAY | CS_ARRAY_WITH_SIZE =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view, CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex + size)
          SonArray(request, subStr1)
      }
    case SonString(request, _) =>
      request match {
        case CS_NAME =>
          val charSliced: Char = input(readerIndex)
          val ri = if (charSliced == CS_COMMA || charSliced == CS_OPEN_BRACKET) readerIndex + 1 else readerIndex
          input(ri) match {
            case CS_QUOTES =>
              val subStr = input.substring(ri + 1, inputSize).view.indexOf(CS_QUOTES)
              val name = input.substring(ri, subStr + 2)
              SonString(request, name)
          }
        case CS_STRING | CS_ARRAY_WITH_SIZE => //TODO not sure about CS_ARRAY_WITH_SIZE here
          val index = input.substring(readerIndex, inputSize).view.indexOf(CS_QUOTES)
          val rI = readerIndex + index
          val endIndex = input.substring(rI + 1, inputSize).view.indexOf(CS_QUOTES)
          val subSize = endIndex + 2
          val subStr1 = input.substring(rI, rI + subSize)
          SonString(request, subStr1.substring(1, subSize - 1))

        case _ => ??? //TODO Implement cases where it is not a String
      }
    case SonNumber(request, _) =>
      request match {
        case CS_INTEGER =>
          val subStr1 = getNextNumber
          SonNumber(request, subStr1.toInt)
        case CS_DOUBLE =>
          val subStr1 = getNextNumber
          SonNumber(request, subStr1.toDouble)
        case CS_LONG =>
          val subStr1 = getNextNumber
          SonNumber(request, subStr1.toLong)
      }
    case SonNull(request, _) =>
      request match {
        case CS_NULL =>
          val subStr1 = getNextNull
          SonNull(request, V_NULL)
      }
    case SonBoolean(request, _) =>
      val subStr1: Byte = if (getNextBoolean.equals(CS_TRUE)) 1 else 0
      SonBoolean(request, subStr1)
  }

  /**
    * getArrayPosition is used to get the actual array position, without consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  override def readToken(tkn: SonNamedType, ignore: Boolean = false): SonNamedType = tkn match {
    case SonObject(request, _) =>
      request match {
        case C_DOT =>
          SonObject(request, input.mkString)
        case CS_OBJECT | CS_OBJECT_WITH_SIZE | CS_OBJECT_INJ =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view, CS_OPEN_BRACKET, CS_CLOSE_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex + size)
          readerIndex += size
          SonObject(request, subStr1)
      }
    case SonArray(request, _) =>
      request match {
        case C_DOT =>
          SonArray(request, input.mkString)
        case CS_ARRAY | CS_ARRAY_WITH_SIZE =>
          val size = findObjectSize(input.substring(readerIndex, inputSize).view, CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
          val subStr1 = input.substring(readerIndex, readerIndex + size)
          readerIndex += size
          SonArray(request, subStr1)
        case CS_ARRAY_INJ =>
          if (input(readerIndex).equals('[')) {
            val size = findObjectSize(input.substring(readerIndex, inputSize).view, CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
            val subStr1 = input.substring(readerIndex, readerIndex + size)
            SonArray(request, subStr1)
          } else {
            if (input(readerIndex).equals('{')) {
              val size = findObjectSize(input.substring(readerIndex, inputSize).view, CS_OPEN_BRACKET, CS_CLOSE_BRACKET)
              val subStr1 = input.substring(readerIndex + 1, readerIndex + size - 1)
              readerIndex += size
              SonArray(request, subStr1)
            } else {
              //First - Read key until '['
              val arrKeySize = findObjectSize(input.substring(readerIndex, inputSize).view, CS_CLOSE_RECT_BRACKET, CS_OPEN_RECT_BRACKET)
              val subKey = input.substring(readerIndex + 1, readerIndex + arrKeySize)
              //Second - Read actual Array until ']'
              val arrSize = findObjectSize(input.substring(readerIndex + arrKeySize, inputSize).view, CS_OPEN_RECT_BRACKET, CS_CLOSE_RECT_BRACKET)
              val subArr = input.substring(readerIndex + arrKeySize, readerIndex + arrKeySize + arrSize)
              SonArray(request, subKey + subArr)
            }
          }
      }
    case SonString(request, _) =>
      request match {
        case CS_NAME | CS_NAME_NO_LAST_BYTE =>
          val charSliced: Char = input(readerIndex)
          if (charSliced == CS_COMMA || charSliced == CS_OPEN_BRACKET || charSliced == CS_OPEN_RECT_BRACKET)
            readerIndex += 1
          input(readerIndex) match {
            case CS_QUOTES =>
              val subStr = input.substring(readerIndex + 1, inputSize).indexOf(CS_QUOTES)
              val name = input.substring(readerIndex, readerIndex + subStr + 2)
              readerIndex += name.length
              SonString(request, name.substring(1, name.length - 1))
          }
        case CS_STRING | CS_ARRAY =>
          val index = input.substring(readerIndex, inputSize).indexOf(CS_QUOTES)
          readerIndex += index
          val endIndex = input.substring(readerIndex + 1, inputSize).indexOf(CS_QUOTES)
          val subStr1 = input.substring(readerIndex, readerIndex + endIndex + 2)
          readerIndex += subStr1.length
          SonString(request, subStr1.substring(1, subStr1.length - 1))
      }
    case SonNumber(request, _) =>
      request match {
        case CS_INTEGER =>
          val subStr1 = readNextNumber
          SonNumber(request, subStr1.toInt)
        case CS_DOUBLE =>
          val subStr1 = readNextNumber
          SonNumber(request, subStr1.toDouble)
        case CS_LONG =>
          val subStr1 = readNextNumber
          SonNumber(request, subStr1.toLong)
      }

    case SonNull(request, _) =>
      readNextNull
      SonNull(request, V_NULL)


    case SonBoolean(request, _) =>
      if (ignore) {
        readerIndex += 1
        SonBoolean(request, 0.toByte)
      }
      else {
        val subStr1: Byte = if (readNextBoolean.equals(CS_TRUE)) 1 else 0
        SonBoolean(request, subStr1)
      }
  }

  /**
    * readNextBoolean is used to consume the next boolean in the stream
    *
    * @return returns a String representing the boolean read (true/false)
    */
  def readNextBoolean: String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n >= 0).min
    val subStr = input.substring(readerIndex, readerIndex + indexMin)
    readerIndex += indexMin
    val subStr1 = subStr dropWhile (p => !p.equals(CS_T) && !p.equals(CS_F))
    subStr1
  }

  /**
    * readNextNull is used to consume the next null value from the stream
    *
    * @return returns a String representing the null value ('Null')
    */
  def readNextNull: String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n >= 0).min
    val subStr = input.substring(readerIndex, readerIndex + indexMin)
    readerIndex += indexMin
    val subStr1 = subStr dropWhile (p => !p.isDigit)
    subStr1
  }

  /**
    * readNextNumber is used to consume the next number in the stream
    *
    * @return returns a String representing the number read (Int/Long/Double/Float)
    */
  def readNextNumber: String = {
    while (!input(readerIndex).isDigit) {
      readerIndex += 1
    }
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n >= 0).min
    val subStr = input.substring(readerIndex, readerIndex + indexMin)
    readerIndex += indexMin
    val subStr1 = subStr.dropWhile(p => !p.isDigit)
    subStr1
  }

  /**
    * readNextBoolean is used to obtain without consuming the next boolean in the stream
    *
    * @return returns a String representing the boolean read (true/false)
    */
  def getNextBoolean: String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n >= 0).min
    val subStr = input.substring(readerIndex, indexMin)
    val subStr1 = subStr.dropWhile(p => !p.equals(CS_T) && !p.equals(CS_F))
    subStr1
  }

  /**
    * readNextNull is used to obtain without consuming the next null value from the stream
    *
    * @return returns a String representing the null value ('Null')
    */
  def getNextNull: String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n >= 0).min
    val subStr = input.substring(readerIndex, indexMin)
    val subStr1 = subStr dropWhile (p => !p.isDigit)
    subStr1
  }

  /**
    * readNextNumber is used to obtain without consuming the next number in the stream
    *
    * @return returns a String representing the number read (Int/Long/Double/Float)
    */
  def getNextNumber: String = {
    lazy val strSliced = input.substring(readerIndex, inputSize)
    lazy val indexMin = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(n => n >= 0).min
    val subStr = input.substring(readerIndex, indexMin)
    val subStr1 = subStr dropWhile (p => !p.isDigit)
    subStr1
  }

  /**
    * getSize is used to obtain the size of the next tokens, with consuming nothing
    *
    * @return this function return the size of the next token, if the next token is an Object, Array or String
    *         which are the case that make sense to obtain a size
    */
  override def getSize: Int = this.readSize

  /**
    * readSize is used to obtain the size of the next tokens, consuming the values from the stream
    *
    * @return this function return the size of the next token, if the next token is an Object, Array or String
    *         which are the case that make sense to obtain a size
    */
  override def readSize: Int = {
    input(readerIndex) match {
      case CS_OPEN_BRACKET | CS_OPEN_RECT_BRACKET if readerIndex == 0 => inputSize
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
        val size = findStringSize(inputAux)
        size
      case _ =>
        readerIndex += 1
        val s = readSize
        s + 1
    }
  }

  /**
    * findObjectSize is used to compute the size of the next JsonObject/jsonArray in stream, without consuming the value
    *
    * @param input stream where we have the JsonObject/jsonArray we wish to know the size
    * @param chO   Character which defines the start symbol of our type, either '{' or '['
    * @param chC   Character which defines the end symbol of our type, either '}' or ']'
    * @return the size of the next JsonObject/jsonArray in stream
    */
  def findObjectSize(input: Seq[Char], chO: Char, chC: Char): Int = {
    var counter: Int = 1
    var i = 1
    while (counter != 0) {
      val aux = input(i) match {
        case x if x.equals(chO) => 1
        case x if x.equals(chC) => -1
        case _ => 0
      }
      counter += aux
      i += 1
    }
    i
  }

  /**
    * findStringSize is used to compute the size of the next String in stream, without consuming the value
    *
    * @param input stream where we have the string we wish to know the size
    * @return the size of the next string in stream
    */
  def findStringSize(input: Seq[Char]): Int = {
    var counter: Int = 1
    var i = 1
    while (counter != 0) {
      val aux = input(i) match {
        case x if x.equals(CS_QUOTES) => -1
        case _ => 0
      }
      counter += aux
      i += 1
    }
    i
  }

  /**
    * rootType is used at the beginning of the first executed function (extract) to know if the input is a BsonObject/JsonObject
    * or BsonArray/JsonArray
    *
    * @return either a SonArray or SonObject representing a BsonArray/JsonArray root or BsonObject/JsonObject root
    */
  override def rootType: SonNamedType = {
    input.head match {
      case CS_OPEN_BRACKET => SonObject(C_DOT)
      case CS_OPEN_RECT_BRACKET => SonArray(C_DOT)
      case _ => SonZero
    }
  }

  /**
    * getDataType is used to obtain the type of the next value in stream, without consuming the value from the stream
    *
    * @return an Int representing a type in stream
    *         0: represents end of String, BsonObject/JsonObject, BsonArray/JsonArray
    *         1: represents float and doubles
    *         2: represents String, Array[Byte], Instants, CharSequences, Enumerates
    *         3: represents BsonObject/JsonObject
    *         4: represents BsonArray/JsonArray
    *         8: represents a Boolean
    *         10: represents a Null
    *         16: represents a Int
    *         18: represents a Long
    */
  override def getDataType: Int = this.readDataType()

  /**
    * readDataType is used to obtain the type of the next value in stream, consuming the value from the stream
    *
    * @return an Int representing a type in stream
    *         0: represents end of String, BsonObject/JsonObject, BsonArray/JsonArray
    *         1: represents float and doubles
    *         2: represents String, Array[Byte], Instants, CharSequences, Enumerates
    *         3: represents BsonObject/JsonObject
    *         4: represents BsonArray/JsonArray
    *         8: represents a Boolean
    *         10: represents a Null
    *         16: represents a Int
    *         18: represents a Long
    */
  override def readDataType(former: Int = 0): Int = {
    if (readerIndex == 0) readerIndex += 1
    val aux = if (input(readerIndex).equals(CS_COMMA) && former != 4) {
      readerIndex += 1
      readerIndex
    } else if (input(readerIndex).equals(CS_COMMA) && former == 4) readerIndex + 1
    else readerIndex
    //    if(input(readerIndex).equals(CS_COMMA)) readerIndex += 1
    //    input(readerIndex) match {
    input(aux) match {
      case CS_CLOSE_BRACKET | CS_CLOSE_RECT_BRACKET =>
        readerIndex += 1
        D_ZERO_BYTE
      case CS_QUOTES =>
        val rIndexAux = readerIndex + 1
        val finalIndex: Int = input.substring(rIndexAux, inputSize).indexOf(CS_QUOTES)
        //val value0 = input.substring(readerIndex, finalIndex)
        input(rIndexAux + finalIndex + 1) match {
          case CS_2DOT =>
            val a = input.substring(rIndexAux + finalIndex + 2, inputSize)
            a(0) match {
              case CS_QUOTES => D_ARRAYB_INST_STR_ENUM_CHRSEQ
              case CS_OPEN_BRACKET => D_BSONOBJECT
              case CS_OPEN_RECT_BRACKET => D_BSONARRAY
              case CS_T => D_BOOLEAN
              case CS_F => D_BOOLEAN
              case CS_N => D_NULL
              case x if x.isDigit =>
                val index = rIndexAux + finalIndex + 2
                lazy val strSliced = input.substring(index, inputSize)
                val bindex = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(v => v > 0).min
                val inputAux = input.substring(index, index + bindex)
                if (!inputAux.contains(CS_DOT)) {
                  Try(inputAux.toInt) match {
                    case Success(v) => D_INT
                    case Failure(_) => D_LONG
                  }
                } else D_FLOAT_DOUBLE
            }
          case _ => D_ARRAYB_INST_STR_ENUM_CHRSEQ
        }
      case CS_OPEN_BRACKET => D_BSONOBJECT
      case CS_OPEN_RECT_BRACKET =>
        if (former == 4) {
          val rIndexAux = readerIndex + 1
          input(rIndexAux) match {
            case CS_QUOTES => D_ARRAYB_INST_STR_ENUM_CHRSEQ
            case CS_OPEN_BRACKET => D_BSONOBJECT
            case CS_OPEN_RECT_BRACKET => D_BSONARRAY
            case CS_T => D_BOOLEAN
            case CS_F => D_BOOLEAN
            case CS_N => D_NULL
            case x if x.isDigit =>
              lazy val strSliced = input.substring(rIndexAux, inputSize)
              val bindex = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(v => v > 0).min
              val inputAux = input.substring(rIndexAux, rIndexAux + bindex)
              if (!inputAux.contains(CS_DOT)) {
                Try(inputAux.toInt) match {
                  case Success(v) => D_INT
                  case Failure(_) => D_LONG
                }
              } else D_FLOAT_DOUBLE
          }
        } else D_BSONARRAY
      case CS_T => D_BOOLEAN
      case CS_F => D_BOOLEAN
      case CS_N => D_NULL
      case x if x.isDigit =>
        lazy val strSliced = input.substring(readerIndex, inputSize)
        val bindex = List(strSliced.view.indexOf(CS_COMMA), strSliced.view.indexOf(CS_CLOSE_BRACKET), strSliced.view.indexOf(CS_CLOSE_RECT_BRACKET)).filter(v => v > 0).min
        val inputAux = input.substring(readerIndex, readerIndex + bindex)
        if (!inputAux.contains(CS_DOT)) {
          Try(inputAux.toInt) match {
            case Success(v) => D_INT
            case Failure(_) => D_LONG
          }
        } else D_FLOAT_DOUBLE
    }
  }

  /**
    * duplicate is used to create a duplicate of the codec, all information is duplicate so that operations
    * over duplicates dont affect the original codec
    *
    * @return a new duplicate Codec
    */
  override def duplicate: Codec = {
    val newCodec = new CodecJson(str)
    newCodec.setReaderIndex(readerIndex)
    newCodec.setWriterIndex(writerIndex)
    newCodec
  }

  /**
    * release is used to free the resources that are no longer used
    */
  override def release(): Unit = {}

  /**
    * downOneLevel is only used when dealing with JSON, it is used to consume the first Character of a BsonArray('[') or BsonObject('{')
    * when we want to process information inside this BsonArray or BsonObject
    */
  override def downOneLevel: Unit = {
    if (input(readerIndex).equals(CS_2DOT)) readerIndex += 1
    if (input(readerIndex).equals(CS_ARROW)) readerIndex += 2
    readerIndex += 1
  }

  /**
    * getArrayPosition is used to get the actual array position, without consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  /*override def getArrayPosition: Int = {
  lazy val substr = input.reverse.substring(inputSize-readerIndex, inputSize)
  val index = substr.view.indexOf(CS_OPEN_RECT_BRACKET)
  substr.substring(0, index).count(p => p.equals(CS_COMMA))
}*/
  override def getArrayPosition: Unit = {}

  /**
    * readArrayPosition is used to get the actual array position, consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  /* override def readArrayPosition: Int ={
   val substr = input.reverse.substring(inputSize-readerIndex, inputSize)
   val index = substr.view.indexOf(CS_OPEN_RECT_BRACKET)
   val str = substr.substring(0, index).view
   val list = StringBuilder.newBuilder
   var a = 0
   var i = 0
   while(i!=str.length){
     str(i)match{
       case x if x.equals(CS_OPEN_BRACKET)||x.equals(CS_OPEN_RECT_BRACKET) =>
         list.append(x)
         a+=1
       case  x if x.equals(CS_CLOSE_BRACKET)||x.equals(CS_CLOSE_RECT_BRACKET) =>
         list.append(x)
         a-=1
       case x =>
         if(a==0) list.append(x) else list.append(CS_ZERO)
     }
     i+=1
   }
   val res = list.count(p => p.equals(CS_COMMA))
   res
 }*/
  override def readArrayPosition: Unit = {}

  /**
    * consumeValue is used to consume some data from the stream that is unnecessary, this method gives better performance
    * since we want to ignore a value
    */
  override def consumeValue(seqType: Int): Unit = seqType match {
    case D_FLOAT_DOUBLE =>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value >= 0).min
      readerIndex += size

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value >= 0).min
      readerIndex += size
    //    case D_BSONOBJECT=> ???
    //    case D_BSONARRAY=> ???
    case D_BOOLEAN =>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value >= 0).min
      readerIndex += size
    case D_NULL =>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value >= 0).min
      readerIndex += size
    case D_INT =>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value >= 0).min
      readerIndex += size
    case D_LONG =>
      //println(input(readerIndex))
      val str = input.substring(readerIndex, inputSize)
      val size = List(str.indexOf(CS_COMMA), str.indexOf(CS_CLOSE_BRACKET), str.indexOf(CS_CLOSE_RECT_BRACKET)).filter(value => value >= 0).min
      readerIndex += size
  }

  /**
    * Method that reads a specified length and returns a new codec with that information
    */
  override def readSpecificSize(size: Int): Codec = ???

  /**
    * Method that retains only a slice, of a specified length, of the data structure.
    * Returns a new codec containing that slice
    *
    * @param length - The length of the slice to retain
    * @return - a new codec containing only a slice of the old codec's dataStructure
    */
  override def readSlice(length: Int): Codec = ???

  /**
    * Create a new codec from an Array of Bytes
    *
    * @param byteArray - The Array of Bytes from which to create the codec
    * @return a new codec with the information present in the array of byte
    */
  def fromByteArray(byteArray: Array[Byte]): Codec = new CodecJson(new String(byteArray))

  //
  //-------------------------------------Injector functions--------------------------

  /**
    * Method that duplicates the current codec, writes the information to the duplicated codec and returns it
    *
    * @param token - the token to write to the codec
    * @return a duplicated codec from the current codec, but with the new information
    */
  override def writeToken(outCodecOpt: Codec, token: SonNamedType, ignoreForJson: Boolean = false, ignoreForBson: Boolean = false, isKey: Boolean = false): Codec = {
    val duplicated: String = outCodecOpt.getCodecData.asInstanceOf[Right[ByteBuf, String]].value

    if (ignoreForJson) new CodecJson(duplicated) else {
      val resultString: String = token match {
        case SonBoolean(_, info) => duplicated + info.asInstanceOf[Boolean]

        case SonNumber(numberType, info) =>
          numberType match {
            case CS_BYTE => duplicated + info.asInstanceOf[Byte]

            case CS_INTEGER => duplicated + info.asInstanceOf[Int]

            case CS_DOUBLE => duplicated + info.asInstanceOf[Double]

            case CS_FLOAT => duplicated + info.asInstanceOf[Float]

            case CS_LONG => duplicated + info.asInstanceOf[Long]

          }

        case SonString(tokenString, info) => tokenString match {
          case CS_STRING_NO_QUOTES => duplicated + info.asInstanceOf[CharSequence]

          case _ => duplicated + "\"" + info.asInstanceOf[CharSequence] + "\""
        }

        case SonArray(_, info) => duplicated + info.asInstanceOf[CharSequence]

        case SonObject(_, info) => duplicated + info.asInstanceOf[CharSequence]

        case SonNull(_, _) => duplicated + "null"
      }
      if (isKey) {
        val newCodec = new CodecJson(resultString + ":")
        newCodec.setReaderIndex(readerIndex)
        newCodec
      } else new CodecJson(resultString + ",")
    }
  }

  /**
    * Method that returns a duplicate of the codec's data structure
    *
    * @return a duplicate of the codec's data structure
    */
  override def getCodecData: Either[ByteBuf, String] = {
    val duplicate = str
    Right(duplicate)
  }

  /**
    *
    * @param sumCodec
    * @return
    */
  override def +(sumCodec: Codec): Codec = {
    val sum = sumCodec.getCodecData match {
      case Right(jsonString) => jsonString
    }
    new CodecJson(str + sum)
  }

  /**
    * This method will remove the empty space in this codec.
    *
    * For CodecBson this method will set the byteBuf's capacity to the same index as writerIndex
    *
    */
  def removeEmptySpace: Unit = {}
}

