package io.zink.boson.bson.codec.impl

import java.nio.charset.Charset
import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.{Value, ValueObject}
import io.zink.bsonLib.{BsonArray, BsonObject}

import scala.collection.mutable.ListBuffer
import scala.util.hashing.ByteswapHashing

/**
  * Class that represents the Codec to deal with Bson Values
  *
  * @param arg Value of type Array[Byte] representing the Bson after being encodec
  * @param opt Value of type Option[ByteBuf] used when creating a duplicate
  */
class CodecBson(arg: ByteBuf, opt: Option[ByteBuf] = None) extends Codec {
  //val alloc: PooledByteBufAllocator = PooledByteBufAllocator.DEFAULT
  /**
    * buff is a value of type ByteBuf to process the value received by the user
    */
  val buff: ByteBuf = opt match {
    case None => arg //alloc.directBuffer(arg.length).writeBytes(arg)
    case Some(x) => x
  }

  /**
    * getReaderIndex is used to get the actual reader index position in the stream
    *
    * @return an Int representing the position on the stream
    */
  override def getReaderIndex: Int = buff.readerIndex()

  /**
    * setReaderIndex is used to set the reader index position in the stream
    *
    * @param value is the new value of the reader index
    */
  override def setReaderIndex(value: Int): Unit = if (value >= 0) buff.readerIndex(value) else {
    buff.readerIndex(buff.readerIndex() + value)
  }

  /**
    * getWriterIndex is used to get the actual writer index position in the stream
    *
    * @return An Int representing the position on the stream
    */
  override def getWriterIndex: Int = buff.writerIndex()

  /**
    * setWriterIndex is used to set the writer index position in the stream
    *
    * @param value is the new value of the writer index
    */
  override def setWriterIndex(value: Int): Unit = buff.writerIndex(value)

  /**
    * getToken is used to obtain a value correponding to the SonNamedType request, without consuming the value from the stream
    *
    * @param tkn is a value from out DSL trait representing the requested type
    * @return returns the same SonNamedType request with the value obtained.
    */
  override def getToken(tkn: SonNamedType): SonNamedType = tkn match {
    case SonBoolean(x, _) =>
      SonBoolean(x, buff.getByte(buff.readerIndex) == 1)
    case SonArray(x, _) =>
      x match {
        case C_DOT =>
          //val array: Array[Byte] = new Array[Byte](buff.capacity())
          val b: ByteBuf = buff.copy(0, buff.capacity)
          //buff.getBytes(0, array)
          SonArray(x, b)
        case CS_ARRAY =>
          val size = buff.getIntLE(buff.readerIndex - 4)
          //val arr: Array[Byte] = new Array[Byte](size)
          val b: ByteBuf = buff.copy(buff.readerIndex - 4, size)
          //buff.getBytes(buff.readerIndex()-4, arr)
          SonArray(x, b)
      }
    case SonObject(x, _) =>
      x match {
        case C_DOT =>
          //val array: Array[Byte] = new Array[Byte](buff.capacity())
          val b: ByteBuf = buff.copy(0, buff.capacity)
          //buff.getBytes(0, array)
          SonObject(x, b)
        case CS_OBJECT =>
          val size = buff.getIntLE(buff.readerIndex - 4)
          //val arr: Array[Byte] = new Array[Byte](size)
          val b: ByteBuf = buff.copy(buff.readerIndex - 4, size)
          //buff.getBytes(buff.readerIndex()-4, arr)
          SonObject(x, b)

        case CS_OBJECT_WITH_SIZE =>
          val size = buff.getIntLE(buff.readerIndex)
          val b: ByteBuf = buff.copy(buff.readerIndex, size)
          SonObject(x, b)
      }
    case SonString(x, _) =>
      x match {
        case CS_NAME =>
          val buf0 = buff.duplicate()
          var i: Int = buff.readerIndex()
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          while (buf0.getByte(i) != 0) {
            key.append(buf0.readByte())
            i += 1
          }
          SonString(x, new String(key.toArray))
        case CS_STRING =>
          val valueLength: Int = buff.getIntLE(buff.readerIndex())
          val arr: Array[Byte] = new Array[Byte](valueLength)
          buff.getBytes(buff.readerIndex() + 4, arr)
          SonString(x, arr)

        case _ =>
          val valueLength: Int = buff.getIntLE(buff.readerIndex())
          SonString(x, buff.readBytes(valueLength))
      }
    case SonNumber(x, _) =>
      x match {
        case CS_DOUBLE =>
          SonNumber(x, buff.getDoubleLE(buff.readerIndex()))
        case CS_INTEGER =>
          SonNumber(x, buff.getIntLE(buff.readerIndex()))
        case CS_LONG =>
          SonNumber(x, buff.getLongLE(buff.readerIndex()))
      }
    case SonNull(x, _) =>
      x match {
        case CS_NULL =>
          SonNull(x, V_NULL)
      }
  }

  /**
    * readToken is used to obtain a value corresponding to the SonNamedType request, consuming the value from the stream
    *
    * @param tkn is a value from out DSL trait representing the requested type
    * @return returns the same SonNamedType request with the value obtained.
    */
  override def readToken(tkn: SonNamedType, ignore: Boolean = false): SonNamedType = tkn match {
    case SonBoolean(x, _) => SonBoolean(x, buff.readByte)

    case SonArray(x, _) =>
      x match {
        case C_DOT =>
          val b: ByteBuf = buff.copy(0, buff.capacity)
          SonArray(x, b)

        case CS_ARRAY =>
          val size = buff.getIntLE(buff.readerIndex - 4)
          val endIndex = buff.readerIndex - 4 + size
          val b = buff.copy(buff.readerIndex - 4, size)
          buff.readerIndex(endIndex)
          SonArray(x, b)

        case CS_ARRAY_WITH_SIZE | CS_ARRAY_INJ =>
          val size = buff.getIntLE(buff.readerIndex)
          val endIndex = buff.readerIndex + size
          val newBuff = buff.copy(buff.readerIndex, size)
          buff.readerIndex(endIndex)
          SonArray(x, newBuff)
      }

    case SonObject(x, _) =>
      x match {
        case C_DOT =>
          val b: ByteBuf = buff.copy(0, buff.capacity)
          buff.readerIndex(buff.capacity)
          SonObject(x, b)

        case CS_OBJECT =>
          val size = buff.getIntLE(buff.readerIndex - 4)
          val endIndex = buff.readerIndex - 4 + size
          val b = buff.copy(buff.readerIndex - 4, size)
          buff.readerIndex(endIndex)
          SonObject(x, b)

        case CS_OBJECT_WITH_SIZE|CS_OBJECT_INJ =>
          val size = buff.getIntLE(buff.readerIndex) //Get the object without its size
        val endIndex = buff.readerIndex + size
          val b = buff.copy(buff.readerIndex, size)
          buff.readerIndex(endIndex)
          SonObject(x, b)

//        case CS_OBJECT_INJ =>
//          val size = buff.getIntLE(buff.readerIndex)
//          val endIndex = buff.readerIndex + size
//          val b = buff.copy(buff.readerIndex, size)
//          buff.readerIndex(endIndex)
//          SonObject(x, b)
      }
    case SonString(x, _) =>
      x match {
        case CS_NAME | CS_NAME_NO_LAST_BYTE =>
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          var i: Int = buff.readerIndex()
          while (buff.getByte(i) != 0) {
            key.append(buff.readByte())
            i += 1
          }

          if (x equals CS_NAME) buff.readByte()

          SonString(x, new String(key.toArray).filter(p => p != 0))

        case CS_STRING =>
          val valueLength: Int = buff.readIntLE()
          SonString(x, buff.readCharSequence(valueLength, charset).toString.filter(b => b != 0))

        case _ =>
          val valueLength: Int = buff.readIntLE()
          SonString(x, buff.readBytes(valueLength))

      }
    case SonNumber(x, _) =>
      x match {
        case CS_DOUBLE =>
          val d = buff.readDoubleLE()
          SonNumber(x, d)
        case CS_INTEGER =>
          SonNumber(x, buff.readIntLE())
        case CS_LONG =>
          SonNumber(x, buff.readLongLE())
      }
    case SonNull(x, _) =>
      x match {
        case CS_NULL =>
          SonNull(x, V_NULL)
      }
  }

  /**
    * New method of reading tokens to substitute the above one. This method returns a Value type
    *
    * @param dt - The dataType of the Token to read
    * @return a Value containing the Token
    */
  override def readToken2(dt: Int): Value = {
    val info = dt match {
      case D_BSONOBJECT =>
        val size = buff.getIntLE(buff.readerIndex)
        val endIndex = buff.readerIndex + size
        val b = buff.copy(buff.readerIndex, size)
        //val b = buff.readBytes(size)
        buff.readerIndex(endIndex)
        b.array()
      case D_BSONARRAY =>
        val size = buff.getIntLE(buff.readerIndex)
        val endIndex = buff.readerIndex + size
        val b = buff.copy(buff.readerIndex, size)
        buff.readerIndex(endIndex)
        b.array()
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buff.readIntLE()
        buff.readCharSequence(valueLength, charset).toString.filter(b => b != 0)
      case D_INT =>
        buff.readIntLE()
      case D_FLOAT_DOUBLE =>
        buff.readDoubleLE()
      case D_LONG =>
        buff.readLongLE()
      case D_BOOLEAN =>
        buff.readByte == 1
      case D_NULL =>
        null
    }
    ValueObject.toValue(info)
  }

  /**
    * Method that reads key and Byte and returns them.
    * In codec Json the byte is irrelevant
    *
    * @return a String contaion the key read and the byte read
    */
  override def readKey: (String, Byte) = {
    val key: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = buff.readerIndex()
    while (buff.getByte(i) != 0) {
      key.append(buff.readByte())
      i += 1
    }
    (new String(key.toArray).filter(p => p != 0), buff.readByte())
  }

  /**
    * Method that reads a single byte from the codec
    *
    * @return the byte read
    */
  override def readByte: Byte = 0.toByte

  /**
    * Method that return the next data of information in codec form
    *
    * @param dt - The data type of the information do be read
    * @return a new codec with the information read
    */
  override def getPartialCodec(dt: Int): Codec = {
    val size = buff.getIntLE(buff.readerIndex)
    val endIndex = buff.readerIndex + size
    val b = buff.copy(buff.readerIndex, size)
    buff.readerIndex(endIndex)
    CodecObject.toCodec(b)
  }

  /**
    * getSize is used to obtain the size of the next tokens, without consuming
    *
    * @return this function return the size of the next token, if the next token is an Object, Array or String
    *         which are the case that make sense to obtain a size
    */
  override def getSize: Int = buff.getIntLE(buff.readerIndex())

  /**
    * readSize is used to obtain the size of the next tokens, consuming the values from the stream
    *
    * @return this function return the size of the next token, if the next token is an Object, Array or String
    *         which are the case that make sense to obtain a size
    */
  override def readSize: Int = buff.readIntLE

  /**
    * rootType is used at the beginning of the first executed function (extract) to know if the input is a BsonObject/JsonObject
    * or BsonArray/JsonArray
    *
    * @return either a SonArray or SonObject representing a BsonArray/JsonArray root or BsonObject/JsonObject root
    */
  override def rootType: SonNamedType = {
    val buf = buff.duplicate
    if (buf.capacity > 5) {
      buf.readerIndex(5)
      val key: ListBuffer[Byte] = new ListBuffer[Byte]
      while (buf.getByte(buf.readerIndex()) != 0 || key.lengthCompare(1) < 0) {
        val b: Byte = buf.readByte()
        key.append(b)
      }
      val _: Byte = buf.readByte()
      if (key.forall(p => p.toChar.isDigit))
        SonArray(C_DOT)
      else
        SonObject(C_DOT)
    } else {
      SonZero
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
  override def getDataType: Int = buff.getByte(buff.readerIndex())

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
  override def readDataType(former: Int = 0): Int = buff.readByte()


  /**
    * duplicate is used to create a duplicate of the codec, all information is duplicated so that operations
    * over duplicates don't affect the original codec
    *
    * @return a new duplicated Codec
    */
  override def duplicate: Codec = internalDuplicate(buff)

  /**
    * release is used to free the resources that are no longer used
    */
  override def release(): Unit = buff.release()

  /**
    * downOneLevel is only used when dealing with JSON. When dealing with BSON the function doesn't perform any work
    */
  override def downOneLevel: Unit = {}

  /**
    * readArrayPosition is used to get the actual buffer position, consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  override def readArrayPosition: Unit = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]

    while (buff.getByte(buff.readerIndex()) != 0) {
      list.+=(buff.readByte())
    }
    if (list.nonEmpty) list.+=(buff.readByte())
  }

  /**
    * consumeValue is used to consume some data from the stream that is unnecessary, this method gives better performance
    * since we want to ignore a value
    */
  override def consumeValue(seqType: Int): Unit = seqType match {
    case D_FLOAT_DOUBLE => buff.skipBytes(8)
    case D_ARRAYB_INST_STR_ENUM_CHRSEQ => buff.skipBytes(buff.readIntLE())
    case D_BOOLEAN => buff.skipBytes(1)
    case D_INT => buff.skipBytes(4)
    case D_LONG => buff.skipBytes(8)
    case _ =>
  }

  //-------------------------------------Injector functions--------------------------

  /**
    * Method that duplicates the current codec, writes the information to the duplicated codec and returns it
    *
    * @param token - the token to write to the codec
    * @return a duplicated codec from the current codec, but with the new information
    */
  override def writeToken(token: SonNamedType, ignoreForJson: Boolean = false, ignoreForBson: Boolean = false, isKey: Boolean = false): Codec = {
    if (!ignoreForBson) {
      token match {

        case SonNull(_, _) =>

        case SonBoolean(_, info) =>
          buff.writeBoolean(info.asInstanceOf[Boolean]) // write the boolean to the buff ByteBuf

        case SonNumber(numberType, info) =>
          numberType match {
            case CS_BYTE => buff.writeByte(info.asInstanceOf[Byte])

            case CS_INTEGER => buff.writeIntLE(info.asInstanceOf[Int])

            case CS_DOUBLE => buff.writeDoubleLE(info.asInstanceOf[Double])

            case CS_FLOAT => buff.writeFloatLE(info.asInstanceOf[Float])

            case CS_LONG => buff.writeLongLE(info.asInstanceOf[Long])
          }

        case SonString(objType, info) =>
          objType match {
            case CS_ARRAY_WITH_SIZE =>
              val valueLength: Int = buff.readIntLE()
              val bytes: ByteBuf = buff.readBytes(valueLength)
              buff.writeIntLE(valueLength)
              buff.writeBytes(bytes)
              bytes.release()

            case _ => buff.writeCharSequence(info.asInstanceOf[CharSequence], Charset.defaultCharset())
          }

        case SonArray(_, info) => buff.writeBytes(info.asInstanceOf[Array[Byte]])

        case SonObject(dataType, info) =>
          val infoToUse: Array[Byte] = info match {
            case byteBuff: ByteBuf =>
              byteBuff.array
            case byteArr: Array[Byte] =>
              byteArr
          }
          dataType match {

            case CS_OBJECT_WITH_SIZE => buff.writeBytes(infoToUse.asInstanceOf[ByteBuf].array())

            case _ => buff.writeBytes(infoToUse.asInstanceOf[Array[Byte]])
          }
      }
    }
    this
  }

  /**
    * Method that write the data type onto the codec
    *
    * @param dt - the data type to be written
    * @return the codec with the written data type
    */
  override def writeDataType(dt: Int): Codec = {
    buff.writeByte(dt.toByte)
    this
  }

  /**
    * Method that writes the key of the current data being analized to the codec
    *
    * @param key - Key to be written
    * @param b   - Byte 0
    * @return the codec with the written key and byte
    */
  override def writeKey(key: String, b: Byte): Codec = {
    buff.writeCharSequence(key.asInstanceOf[CharSequence], Charset.defaultCharset())
    buff.writeByte(b)
    this
  }

  /**
    * Method that writes the key of an array
    *
    * @param key - Key to be written
    * @param b   - Byte 0
    * @return the codec with the written key
    */
  override def writeArrayKey(key: String, b: Byte): Codec = writeKey(key,b)

  /**
    * Method that writes a String to the codec
    *
    * @param info - The information to be written
    * @return the codec with the string written
    */
  override def writeString(info: String): Codec = {
    buff.writeIntLE(info.length + 1)
    buff.writeCharSequence(info, Charset.defaultCharset())
    buff.writeByte(0.toByte)
    this
  }

  /**
    * Method that writes an Object in String form to the codec
    *
    * @param info - The information to be written
    * @return the codec with the object written
    */
  override def writeObject(info: String): Codec = this

  /**
    * Method that writes an Object in Array[Byte] form to the codec
    *
    * @param info - The information to be written
    * @return the codec with the object written
    */
  override def writeObject(info: Array[Byte]): Codec = {
    buff.writeBytes(info)
    this
  }

  /**
    * Method that writes an Int to the codec
    *
    * @param info - The information to be written
    * @return the codec with the Int written
    */
  override def writeInt(info: Int): Codec = {
    buff.writeIntLE(info)
    this
  }

  /**
    * Method the writes a Long to the codec
    *
    * @param info - The information to be written
    * @return the codec with the Long written
    */
  override def writeLong(info: Long): Codec = {
    buff.writeLongLE(info)
    this
  }

  /**
    * Method that writes a Float to the codec
    *
    * @param info - The information to be written
    * @return the codec with the Float written
    */
  override def writeFloat(info: Float): Codec = {
    buff.writeFloatLE(info)
    this
  }

  /**
    * Method that writes a Double to the codec
    *
    * @param info - The information to be written
    * @return th codec with the information written
    */
  override def writeDouble(info: Double): Codec = {
    buff.writeDoubleLE(info)
    this
  }

  /**
    * Method that writes a Boolean to the codec
    *
    * @param info - The information to be written
    * @return the codec with the Boolean written
    */
  override def writeBoolean(info: Boolean): Codec = {
    buff.writeBoolean(info)
    this
  }

  /**
    * Method that writes a Null to the codec
    *
    * @param info - The information to be written
    * @return the codec with the Null written
    */
  override def writeNull(info: Null): Codec = {
    this
  }

  /**
    * Method that writes an Array[Byte] to the codec
    *
    * @param info - The information to be written
    * @return the codec with the information written
    */
  override def writeBarray(info: Array[Byte]): Codec = {
    buff.writeBytes(info)
    this
  }


  /**
    * Method that returns a duplicate of the codec's data structure
    *
    * @return a duplicate of the codec's data structure
    */
  override def getCodecData: Either[ByteBuf, String] = Left(buff)

  /**
    * Method that adds 2 Codecs and returns the result codec
    *
    * @param sumCodec - codec to be added to the first
    * @return a codec with the added information of the other 2
    */
  override def +(sumCodec: Codec): Codec = {
    sumCodec.setReaderIndex(0)
    sumCodec.getCodecData match {
      case Left(x) => buff.writeBytes(x)
    }
    buff.capacity(buff.writerIndex())
    this
  }

  /**
    * Method that removes the trailing of a CodecJson in order to create a correct json
    * This method, in case of CodecBson, simply returns the codec passed as argument
    *
    * @param codec - codec we wish to remove the trailing comma
    * @return a new codec that does not have the last trailing comma in it
    */
  def removeTrailingComma(codec: Codec, rectBrackets: Boolean = false, checkOpenRect: Boolean = false, noBrackets: Boolean = false): Codec = this

  /**
    * Method that creates a new codec with exactly the same information as the current codec but with the size information written in it.
    * In case the current codec is a CodecJson this method simply returns and empty CodecJson representing a codec with a size inside it (nothing)
    *
    * @return A new codec with exactly the same information as the current codec but with the size information written in it
    */
  def writeCodecSize: Codec = { //TODO - refactor
    //    CodecObject.toCodec((getWriterIndex+4).asInstanceOf[ByteBuf]) + this
    val duplicated = Unpooled.buffer().writeIntLE(getWriterIndex + 4)
    duplicated.writeBytes(buff)
    duplicated.capacity(getWriterIndex + 4)

    new CodecBson(duplicated)
  }

  /**
    * Method that skips the next character in the current codec's data structure
    */
  def skipChar(back: Boolean = false): Unit = {}


  /**
    * Method that adds a comma to the end of a CodecJson data structure
    * In case the current codec is a CodecBson this method simply returns the current codec
    *
    * @return A codec that has exactly the same information but adds a comma to the end of this codecs data structure in case it's a CodecJson
    */
  def addComma: Codec = this

  /**
    * Method that upon receiving two distinct codecs, will decide which one to use based on the current codec type
    * Since the writting and reading of Bson and Json is not identical some edge cases are necessary, this method
    * allows us to not expose the codec type in BosonInjectorImpl.scala
    *
    * @param codecForBson - Codec to use in case the current codec is of type CodecBson
    * @param codecForJson - Codec to use in case the current codec is of type CodecJson
    * @return The codec to use according to the current codec's type
    */
  def decideCodec(codecForBson: => Codec, codecForJson: Codec): Codec = codecForBson

  /**
    * Method that decides if a codec is able to read a key or not. If the codec type is CodecBson this method will always return true
    * If the method is of type CodecJson this method will check if the initial character is not an open array bracket
    * for that would break the reading process in the Json case
    *
    * @return a Boolean saying if the codec is able to read a key or not
    */
  def canReadKey(searchAndModify: Boolean = false): Boolean = true

  /**
    * Method that decides if the type of the current key is an array or not
    *
    * @param formerType
    * @param key
    * @return A Boolean specifying if the type of the current key is an array or not
    */
  def isArray(formerType: Int, key: String): Boolean = key.forall(b => b.isDigit)

  /**
    * Method that changes the brackets of a json string from curly brackets to rectangular brackets
    * In case the current codec is of type CodecBson this method simple returns a duplicated codec
    *
    * @param dataType - The data type of value to change
    * @return A new codec with exactly the same information but with the brackets changed
    */
  def changeBrackets(dataType: Int, curlyToRect: Boolean = true): Codec = this

  /**
    * Method that wraps a CodecJson in curly or rectangular brackets.
    * For CodecBson this method simply returns a copy of this codec
    *
    * @param rectBracket - Boolean flag specifying if the brackets should be curly or rectangular
    * @param key         - Json field to be written before this codec's content (optional)
    * @return A new codec with the same information as before but with brackets encapsulating it
    */
  def wrapInBrackets(rectBracket: Boolean = false, key: String = "", dataType: Int = -1): Codec = this

  def removeBrackets: Codec = this

  /**
    * Method that decides if a CodecJson can be wrapped in curly braces or not.
    * For CodecBson this method simply returns false
    *
    * @return A Boolean specifying if this codec can be wrapped in curly braces or not
    */
  def wrappable: Boolean = false

  /**
    * Method that creates a Codec with an empty data structure inside it.
    *
    * For CodecBson it creates a ByteBuf with capacity 256.
    * For CodecJson it creates an empty String
    *
    * @return a Codec with an empty data structure inside it
    */
  def createEmptyCodec()(implicit emptyBuf: ByteBuf): Codec = internalDuplicate(emptyBuf)

  /**
    * This methods clears all the information inside the codec so it can be rewritten
    *
    * @return
    */
  def clear: Codec = {
    buff.clear()
    this
  }

  /**
    * this method is called to complete writing the rest of an array when the desired modifications have been made
    *
    * @param codec    - The Codec to read the information from
    * @param dataType - The current data Type (not used in CodecBson
    * @return
    */
  def writeRest(codec: Codec, dataType: Int): Codec = {
    val rest = codec.getCodecData.asInstanceOf[Left[ByteBuf, String]].value
    val aux = rest.copy(codec.getReaderIndex, codec.getWriterIndex - codec.getReaderIndex - 1)
    buff.writeBytes(aux)
    codec.setReaderIndex(codec.getWriterIndex - 1)
    this
  }

  private def internalDuplicate(byteBuf: ByteBuf): Codec = {
    val newB = byteBuf.copy(0, byteBuf.writerIndex)
    newB.readerIndex(byteBuf.readerIndex)
    newB.writerIndex(byteBuf.writerIndex)
    new CodecBson(newB)
  }
}
