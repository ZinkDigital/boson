package io.zink.boson.bson.codec

import io.netty.buffer.ByteBuf
import io.zink.boson.bson.codec.impl.{CodecBson, CodecJson}


/**
  * Trait that represents the DSL used by the codecs to interchange data between codec and BosonImpl
  *
  * @example SonString: used to represent all types of D_ARRAYB_INST_STR_ENUM_CHRSEQ
  *          SonNumber: used to represent all types of D_FLOAT_DOUBLE/D_INT/D_LONG
  *          SonObject: used to represent all types of D_BSONOBJECT
  *          SonArray: used to represent all types of D_BSONARRAY
  *          SonBoolean: used to represent all types of D_BOOLEAN
  *          SonNull: used to represent all types of D_NULL
  */
sealed trait SonNamedType

case class SonString(name: String, info: Any = None) extends SonNamedType

case class SonNumber(name: String, info: Any = None) extends SonNamedType

case class SonObject(name: String, info: Any = None) extends SonNamedType

case class SonArray(name: String, info: Any = None) extends SonNamedType

case class SonBoolean(name: String, info: Any = None) extends SonNamedType

case class SonNull(name: String, info: Any = None) extends SonNamedType

case object SonZero extends SonNamedType


/**
  * Trait that represents the Codecs
  */
trait Codec {
  /**
    * getToken is used to obtain a value correponding to the SonNamedType request, without consuming the value from the stream
    *
    * @param tkn is a value from out DSL trait representing the requested type
    * @return returns the same SonNamedType request with the value obtained.
    */
  def getToken(tkn: SonNamedType): SonNamedType

  /**
    * readToken is used to obtain a value correponding to the SonNamedType request, consuming the value from the stream
    *
    * @param tkn is a value from out DSL trait representing the requested type
    * @return returns the same SonNamedType request with the value obtained.
    */
  def readToken(tkn: SonNamedType, ignore: Boolean = false): SonNamedType

  /**
    * readArrayPosition is used to get the actual array position, consuming the value from stream
    *
    * @return this method doesn't return anything because this data is not usefull for extraction
    *         however, in the future when dealing with injection, we may have the need to work with this value
    *         (this is why there is a commented function with the same but returning a Int)
    */
  //def readArrayPosition: Int
  def readArrayPosition: Unit

  /**
    * getReaderIndex is used to get the actual reader index position in the stream
    *
    * @return an Int representing the position on the stream
    */
  def getReaderIndex: Int

  /**
    * setReaderIndex is used to set the reader index position in the stream
    *
    * @param value is the new value of the reader index
    */
  def setReaderIndex(value: Int): Unit

  /**
    * getWriterIndex is used to get the actual writer index position in the stream
    *
    * @return An Int representing the position on the stream
    */
  def getWriterIndex: Int

  /**
    * setWriterIndex is used to set the writer index position in the stream
    *
    * @param value is the new value of the writer index
    */
  def setWriterIndex(value: Int): Unit

  /**
    * getSize is used to obtain the size of the next tokens, with consuming nothing
    *
    * @return this function return the size of the next token, if the next token is an Object, Array or String
    *         which are the case that make sense to obtain a size
    */
  def getSize: Int

  /**
    * readSize is used to obtain the size of the next tokens, consuming the values from the stream
    *
    * @return this function return the size of the next token, if the next token is an Object, Array or String
    *         which are the case that make sense to obtain a size
    */
  def readSize: Int

  /**
    * downOneLevel is only used when dealing with JSON, it is used to consume the first Character of a BsonArray('[') or BsonObject('{')
    * when we want to process information inside this BsonArray or BsonObject
    */
  def downOneLevel: Unit

  /**
    * rootType is used at the beginning of the first executed function (extract) to know if the input is a BsonObject/JsonObject
    * or BsonArray/JsonArray
    *
    * @return either a SonArray or SonObject representing a BsonArray/JsonArray root or BsonObject/JsonObject root
    */
  def rootType: SonNamedType

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
  def getDataType: Int

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
  def readDataType(former: Int = 0): Int

  /**
    * duplicate is used to create a duplicate of the codec, all information is duplicate so that operations
    * over duplicates dont affect the original codec
    *
    * @return a new duplicate Codec
    */
  def duplicate: Codec

  /**
    * release is used to free the resources that are no longer used
    */
  def release()

  /**
    * consumeValue is used to consume some data from the stream that is unnecessary, this method gives better performance
    * since we want to ignore a value
    */
  def consumeValue(seqType: Int): Unit

  //--------------------------------------------Injector functions-----------------------------------------

  /**
    * Method that duplicates the current codec, writes the information to the duplicated codec and returns it
    *
    * @param token - the token to write to the codec
    * @return a duplicated codec from the current codec, but with the new information
    */
  def writeToken(outCodec: Codec, token: SonNamedType, ignoreForJson: Boolean = false, ignoreForBson: Boolean = false, isKey: Boolean = false): Codec

  /**
    * Method that returns a duplicate of the codec's data structure
    *
    * @return a duplicate of the codec's data structure
    */
  def getCodecData: Either[ByteBuf, String]

  /**
    * Method that adds 2 codecs and returns the result codec
    *
    * @param sumCodec - Codec to be added to the first
    * @return a codec with the added information of the other 2
    */
  def +(sumCodec: Codec): Codec

  /**
    * This method will remove the empty space in this codec.
    *
    * For CodecBson this method will set the byteBuf's capacity to the same index as writerIndex
    */
  def removeEmptySpace: Unit

  /**
    * Method that removes the trailing of a CodecJson in order to create a correct json
    * This method, in case of CodecBson, simply returns the codec passed as argument
    *
    * @param codec - codec we wish to remove the trailing comma
    * @return a new codec that does not have the last trailing comma in it
    */
  def removeTrailingComma(codec: Codec, rectBrackets: Boolean = false, checkOpenRect: Boolean = false): Codec

  /**
    * Method that creates a new codec with exactly the same information as the current codec but with the size information written in it.
    * In case the current codec is a CodecJson this method simply returns and empty CodecJson representing a codec with a size inside it (nothing)
    *
    * @return A new codec with exactly the same information as the current codec but with the size information written in it
    */
  def writeCodecSize: Codec

  /**
    * Method that skips the next character in the current codec's data structure
    */
  def skipChar: Unit
}

sealed trait CodecFacade {
  def toCodec[T](a: T)(implicit c: Codecs[T]): Codec
}

object CodecObject extends CodecFacade {
  override def toCodec[T](a: T)(implicit c: Codecs[T]): Codec = c.applyFunc(a)
}

sealed trait Codecs[T] {
  def applyFunc(arg: T): Codec
}

object Codecs extends DefaultCodecs {
  def apply[T](f: T => Codec, a: T): Codec = f(a)
}

sealed trait DefaultCodecs {

  implicit object StringCodec extends Codecs[String] {
    override def applyFunc(arg: String): CodecJson = new CodecJson(arg)
  }

  //  implicit object ArrayCodec extends Codecs[Array[Byte]] {
  //    override def applyFunc(arg:Array[Byte]): CodecBson = new CodecBson(arg) //call the array bytes codec
  //  }
  implicit object ByteBufCodec extends Codecs[ByteBuf] {
    override def applyFunc(arg: ByteBuf): CodecBson = new CodecBson(arg) //call the array bytes codec
  }

}