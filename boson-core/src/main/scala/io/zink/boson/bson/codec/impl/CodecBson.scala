package io.zink.boson.bson.codec.impl

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
class CodecBson(var arg: ByteBuf, opt: Option[ByteBuf] = None) extends Codec {
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
      }
    case SonNumber(x, y) =>
      x match {
        case CS_DOUBLE =>
          SonNumber(x, buff.getDoubleLE(buff.readerIndex()))
        case CS_INTEGER =>
          SonNumber(x, buff.getIntLE(buff.readerIndex()))
        case CS_LONG =>
          SonNumber(x, buff.getLongLE(buff.readerIndex()))
      }
    case SonNull(x, y) =>
      x match {
        case CS_NULL =>
          SonNull(x, V_NULL)
      }
  }

  /**
    * getArrayPosition is used to get the actual array position, without consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  override def readToken(tkn: SonNamedType): SonNamedType = tkn match { //TODO:Unpooled, does it fit?
    case SonBoolean(x, _) =>
      SonBoolean(x, buff.readByte)
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
      }
    case SonObject(x, _) =>
      x match {
        case C_DOT =>
          val b: ByteBuf = buff.copy(0, buff.capacity)
          buff.readerIndex(buff.capacity)
          SonObject(x, b)
        case CS_OBJECT =>
          //          val size = buff.getIntLE(buff.readerIndex-4)
          //          buff.readerIndex(buff.readerIndex-4) //TODO: rethink this situation, going back and forth
          //          val b: ByteBuf = Unpooled.buffer(size)
          //          buff.readBytes(b)
          //          SonObject(x, b)
          val size = buff.getIntLE(buff.readerIndex - 4)
          val endIndex = buff.readerIndex - 4 + size
          val b = buff.copy(buff.readerIndex - 4, size)
          buff.readerIndex(endIndex)
          SonObject(x, b)
      }
    case SonString(x, _) =>
      x match {
        case CS_NAME =>
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          var i: Int = buff.readerIndex()
          while (buff.getByte(i) != 0) {
            key.append(buff.readByte())
            i += 1
          }
          buff.readByte()
          SonString(x, new String(key.toArray).filter(p => p != 0))
        case CS_STRING =>
          val valueLength: Int = buff.readIntLE()
          SonString(x, buff.readCharSequence(valueLength, charset).toString.filter(b => b != 0))
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
  override def readDataType: Int = buff.readByte()


  /**
    * duplicate is used to create a duplicate of the codec, all information is duplicated so that operations
    * over duplicates don't affect the original codec
    *
    * @return a new duplicated Codec
    */
  override def duplicate: Codec = {
    val newB = buff.copy(0, buff.capacity) //TODO:this is too heavy, find another way
    newB.readerIndex(buff.readerIndex)
    val c = new CodecBson(arg, Some(newB))
    c

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
    * getArrayPosition is used to get the actual buffer position, without consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  /* override def getArrayPosition: Int = {
     val list: ListBuffer[Byte] = new ListBuffer[Byte]
     var i = 0
     while (buff.getByte(buff.readerIndex()+i) != 0) {
       list.+=(buff.getByte(buff.readerIndex()+i))
       i+=1
     }
     new String(list.toArray.filter((b:Byte) => b != 0)).toInt
   }*/
  override def getArrayPosition: Unit = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    var i = 0
    while (buff.getByte(buff.readerIndex() + i) != 0) {
      list.+=(buff.getByte(buff.readerIndex() + i))
      i += 1
    }
  }

  /**
    * readArrayPosition is used to get the actual buffer position, consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  /*override def readArrayPosition: Int = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]

    while (buff.getByte(buff.readerIndex()) != 0) {
      list.+=(buff.readByte())
    }
    if(list.nonEmpty)list.+=(buff.readByte())
    new String(list.toArray.filter((b:Byte) => b != 0)).toInt
  }*/
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

  override def writeInformation(byte: Int): Codec = ???

  override def readKey: ListBuffer[Byte] = {
    val key: ListBuffer[Byte] = new ListBuffer[Byte]
    while (arg.getByte(arg.readerIndex()) != 0 || key.lengthCompare(1) < 0) {
      val b: Byte = arg.readByte()
      key.append(b)
    }
    key
  }

  override def readNextInformation: Int = arg.readByte()

}
