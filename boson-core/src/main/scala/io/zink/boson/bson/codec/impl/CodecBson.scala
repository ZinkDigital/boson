package io.zink.boson.bson.codec.impl

import java.nio.charset.Charset

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._

import scala.collection.mutable.ListBuffer

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
          while (buf0.getByte(i) != 0) { //  || key.length<1 ??? TODO
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
    * readToken is used to obtain a value correponding to the SonNamedType request, consuming the value from the stream
    *
    * @param tkn is a value from out DSL trait representing the requested type
    * @return returns the same SonNamedType request with the value obtained.
    */
  override def readToken(tkn: SonNamedType, ignore: Boolean = false): SonNamedType = tkn match { //TODO:Unpooled, does it fit?
    case SonBoolean(x, _) => SonBoolean(x, buff.readByte)

    case SonArray(x, _) =>
      x match {
        case C_DOT =>
          val b: ByteBuf = buff.copy(0, buff.capacity)
          buff.readerIndex(buff.capacity) //TODO: probably isn't needed
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

        case CS_OBJECT_WITH_SIZE =>
          val size = buff.getIntLE(buff.readerIndex) //Get the object without its size
        val endIndex = buff.readerIndex + size
          val b = buff.copy(buff.readerIndex, size)
          buff.readerIndex(endIndex)
          SonObject(x, b)

        case CS_OBJECT_INJ =>
          val size = buff.getIntLE(buff.readerIndex)
          val endIndex = buff.readerIndex + size
          val b = buff.copy(buff.readerIndex, size)
          buff.readerIndex(endIndex)
          SonObject(x, b)
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
    val buf = buff.duplicate()
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
  override def duplicate: Codec = {
    val newB = buff.copy(0, buff.capacity) //TODO:this is too heavy, find another way
    newB.readerIndex(buff.readerIndex)
    new CodecBson(arg, Some(newB))
  }

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
    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      buff.skipBytes(buff.readIntLE())
    //    case D_BSONOBJECT=>buff.skipBytes(buff.readIntLE())
    //    case D_BSONARRAY=>buff.skipBytes(buff.readIntLE())
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
  override def writeToken(outCodecOpt: Codec, token: SonNamedType, ignoreForJson: Boolean = false, ignoreForBson: Boolean = false, isKey: Boolean = false): Codec = {
    val duplicated: ByteBuf = {
      val byteBuf = outCodecOpt.getCodecData.asInstanceOf[Left[ByteBuf, String]].value
      val newBuf = byteBuf.copy(0, byteBuf.capacity())
      newBuf.readerIndex(byteBuf.readerIndex()) //Will always be 0
      newBuf.writerIndex(byteBuf.writerIndex())
    }
    if (ignoreForBson) new CodecBson(arg, Some(duplicated)) else {
      token match {

        case SonNull(_, _) => new CodecBson(arg, Some(duplicated))

        case SonBoolean(_, info) =>
          val writableBoolean = info.asInstanceOf[Boolean] //cast received info as boolean or else throw an exception
          duplicated.writeBoolean(writableBoolean) // write the boolean to the duplicated ByteBuf
          new CodecBson(arg, Some(duplicated)) //return a new codec with the duplicated ByteBuf

        case SonNumber(numberType, info) =>
          val manipulatedBuf: ByteBuf = numberType match {

            case CS_BYTE =>
              val writableByte = info.asInstanceOf[Byte]
              duplicated.writeByte(writableByte)

            case CS_INTEGER =>
              val writableInt = info.asInstanceOf[Int]
              duplicated.writeIntLE(writableInt)

            case CS_DOUBLE =>
              val writableDouble = info.asInstanceOf[Double]
              duplicated.writeDoubleLE(writableDouble)

            case CS_FLOAT =>
              val writableFloat = info.asInstanceOf[Float]
              duplicated.writeFloatLE(writableFloat)

            case CS_LONG =>
              val writableLong = info.asInstanceOf[Long]
              duplicated.writeLongLE(writableLong)

          }
          new CodecBson(arg, Some(manipulatedBuf))

        case SonString(objType, info) =>
          objType match {
            case CS_ARRAY_WITH_SIZE =>
              val valueLength: Int = buff.readIntLE()
              val bytes: ByteBuf = buff.readBytes(valueLength)
              duplicated.writeIntLE(valueLength)
              duplicated.writeBytes(bytes)
              bytes.release()

            case _ =>
              val writableCharSeq = info.asInstanceOf[CharSequence]
              duplicated.writeCharSequence(writableCharSeq, Charset.defaultCharset())
          }

          new CodecBson(arg, Some(duplicated))

        case SonArray(_, info) =>
          val writableByteSeq = info.asInstanceOf[Array[Byte]] //TODO exactly the same as bellow
          duplicated.writeBytes(writableByteSeq)
          new CodecBson(arg, Some(duplicated))

        case SonObject(dataType, info) =>
          dataType match {

            case CS_OBJECT_WITH_SIZE =>
              val writableByteBuf = info.asInstanceOf[ByteBuf]
              val writableByteSeq = writableByteBuf.array()
              duplicated.writeBytes(writableByteSeq)
              new CodecBson(arg, Some(duplicated))

            case _ =>
              val writableByteSeq = info.asInstanceOf[Array[Byte]]
              duplicated.writeBytes(writableByteSeq)
              new CodecBson(arg, Some(duplicated))
          }
      }
    }
  }

  /**
    * Private method that returns an exact copy of this codec's ByteBuf in order to manipulate this copied ByteBuf
    *
    * @return an exact copy of the current codec's ByteBuf
    */
  private def copyByteBuf: ByteBuf = {
    val newBuf = buff.copy(0, buff.capacity)
    newBuf.readerIndex(buff.readerIndex)
  }

  /**
    * Method that returns a duplicate of the codec's data structure
    *
    * @return a duplicate of the codec's data structure
    */
  override def getCodecData: Either[ByteBuf, String] = {
    val newB = buff.copy(0, buff.capacity) //TODO:this is too heavy, find another way
    newB.readerIndex(buff.readerIndex) //Set the reader index to be the same as it is in this moment
    newB.writerIndex(buff.writerIndex()) //Set the writer index to be the same as it is in this moment
    Left(newB)
  }

  /**
    * Method that adds 2 Codecs and returns the result codec
    *
    * @param sumCodec - codec to be added to the first
    * @return a codec with the added information of the other 2
    */
  override def +(sumCodec: Codec): Codec = {
    sumCodec.setReaderIndex(0)
    val duplicated = copyByteBuf
    duplicated.writerIndex(buff.writerIndex())
    sumCodec.getCodecData match {
      case Left(x) => duplicated.writeBytes(x)
    }
    duplicated.capacity(duplicated.writerIndex())
    new CodecBson(arg, Some(duplicated))
  }

  /**
    * This method will remove the empty space in this codec.
    *
    * For CodecBson this method will set the ByteBuf's capacity to the same index as writerIndex
    */
  def removeEmptySpace: Unit = buff.capacity(buff.writerIndex())

  /**
    * Method that removes the trailing of a CodecJson in order to create a correct json
    * This method, in case of CodecBson, simply returns the codec passed as argument
    *
    * @param codec - codec we wish to remove the trailing comma
    * @return a new codec that does not have the last trailing comma in it
    */
  def removeTrailingComma(codec: Codec, rectBrackets: Boolean = false, checkOpenRect: Boolean = false): Codec = codec

  /**
    * Method that creates a new codec with exactly the same information as the current codec but with the size information written in it.
    * In case the current codec is a CodecJson this method simply returns and empty CodecJson representing a codec with a size inside it (nothing)
    *
    * @return A new codec with exactly the same information as the current codec but with the size information written in it
    */
  def writeCodecSize: Codec = {
    val duplicatedWithSize = Unpooled.buffer().writeIntLE(getWriterIndex + 4)
    new CodecBson(arg, Some(duplicatedWithSize))
  }

  /**
    * Method that skips the next character in the current codec's data structure
    */
  def skipChar: Unit = {}
}
