package io.zink.boson.bson.bsonImpl

import io.netty.buffer.ByteBuf
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec.{Codec, CodecObject}

class BosonImpl2 {
  type DataStructure = Either[ByteBuf, String]
  type StatementsList = List[(Statement, String)]

  /**
    *
    * @param dataStructure
    * @param statements
    * @param injFunction
    * @tparam T
    * @return
    */
  def inject[T](dataStructure: DataStructure, statements: StatementsList, injFunction: T => T ): Codec = {
    val codec: Codec = dataStructure match{
      case Right(jsonString) => CodecObject.toCodec(jsonString)
      case Left(byteBuf) => CodecObject.toCodec(byteBuf)
    }

    statements.head._1 match {
      case ROOT => ??? //execRootInjection(codec, f)

      case Key(key: String) => ??? //modifyAll(statements, codec, key, injFunction) (key = fieldID)

      case HalfName(half: String) => ??? //modifyAll(statements, codec, half, injFunction)

      case HasElem(key: String, elem: String) => ??? //modifyHasElem(statements, codec, key, elem, f)

      case _ => ???
    }
  }

  /**
    *
    * @param statementsList
    * @param codec
    * @param key
    * @param injFunction
    * @tparam T
    * @return
    */
  def modifyAll[T](statementsList: StatementsList, codec: Codec, key: String, injFunction: T => T): Codec = {
    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize

    while ((codec.getReaderIndex - startReader) < originalSize){
      val dataType = codec.readDataType
      dataType match {
        case 0 => //This is the end
        case _ =>
      }
    }
    ???
  }

}
