package io.zink.boson.bson.codec

import io.netty.buffer.ByteBuf
import io.zink.boson.bson.codec.impl.{CodecBson, CodecJson}


sealed trait SonNamedType
case class SonStart(name : String = "Start", result: Any = None) extends SonNamedType

case class SonString(name : String, result: Any = None) extends SonNamedType
case class SonNumber(name : String, result: Any = None) extends SonNamedType
case class SonObject(name : String, result: Any = None) extends SonNamedType
case class SonArray(name : String, result: Any = None)  extends SonNamedType
case class SonBoolean(name : String, result: Any = None)extends SonNamedType
case class SonTrue(name : String, result: Any = None)   extends SonNamedType
case class SonFalse(name : String, result: Any = None)  extends SonNamedType
case class SonNull(name : String, result: Any = None)   extends SonNamedType
case object SonZero   extends SonNamedType


case class SonEnd(name : String = "End", result: Any = None) extends SonNamedType

trait Codec {

  def getToken(tkn: SonNamedType): SonNamedType
  def readToken(tkn: SonNamedType): SonNamedType

  def getArrayPosition: Int
  def readArrayPosition: Int
  def getReaderIndex: Int
  def setReaderIndex(value: Int): Unit
  def getWriterIndex: Int
  def setWriterIndex(value: Int): Unit

  def getSize: Int
  def readSize : Int

  def downOneLevel: Unit
  def rootType: SonNamedType

//  def getValue: SonNamedType
//  def readValue: SonNamedType

  def getDataType: Int
  def readDataType: Int

  def duplicate: Codec

  def printCodec()

  def release()


}



sealed trait CodecFacade{
  def toCodec[T](a: T)(implicit c: Codecs[T]) : Codec
}
object CodecObject extends CodecFacade{
  override def toCodec[T](a: T)(implicit c: Codecs[T]): Codec = c.applyFunc(a)
}

sealed trait Codecs[T]{
  def applyFunc(arg: T): Codec
}

object Codecs extends DefaultCodecs{
  def apply[T](f: T => Codec, a: T): Codec = f(a)
}

sealed trait DefaultCodecs {

  implicit object StringCodec extends Codecs[String] {
    override def applyFunc(arg: String): CodecJson  = new CodecJson(arg)


  }

  implicit object ArrayCodec extends Codecs[Array[Byte]] {
    override def applyFunc(arg:Array[Byte]): CodecBson = new CodecBson(arg) //call the array bytes codec
  }
}