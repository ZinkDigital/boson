package io.boson.scalaInterface

import java.nio.ByteBuffer
import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.nettyboson.Boson
import io.netty.buffer.ByteBuf
import io.vertx.core.buffer.Buffer
import scala.collection.mutable.ArrayBuffer
import io.boson.bsonValue

/**
  * Created by Ricardo Martins on 03/11/2017.
  */
class ScalaInterface {

  def createNettyBson(byteArray: Array[Byte]):Boson = {
     new Boson(byteArray = Option(byteArray))
  }
  def createNettyBson(byteBuf: ByteBuf):Boson = {
    new Boson(byteBuf = Option(byteBuf))
  }
  def createNettyBson(byteBuffer: ByteBuffer):Boson = {
    new Boson(javaByteBuf = Option(byteBuffer))
  }
  def createNettyBson(vertxBuffer: Buffer):Boson = {
    new Boson(vertxBuff = Option(vertxBuffer))
  }
  def createNettyBson(arrayBuffer: ArrayBuffer[Byte]):Boson = {
    new Boson(scalaArrayBuf = Option(arrayBuffer))
  }


  def parse(netty: Boson, key: String, expression: String): bsonValue.BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(netty, key, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) =>  bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>  bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e:RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

}
