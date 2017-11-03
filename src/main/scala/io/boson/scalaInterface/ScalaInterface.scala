package io.boson.scalaInterface

import java.nio.ByteBuffer

import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.nettybson.NettyBson
import io.netty.buffer.ByteBuf
import io.vertx.core.buffer.Buffer

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Ricardo Martins on 03/11/2017.
  */
class ScalaInterface {

  def createNettyBson(byteArray: Array[Byte]):NettyBson = {
     new NettyBson(byteArray = Option(byteArray))
  }
  def createNettyBson(byteBuf: ByteBuf):NettyBson = {
    new NettyBson(byteBuf = Option(byteBuf))
  }
  def createNettyBson(byteBuffer: ByteBuffer):NettyBson = {
    new NettyBson(javaByteBuf = Option(byteBuffer))
  }
  def createNettyBson(vertxBuffer: Buffer):NettyBson = {
    new NettyBson(vertxBuff = Option(vertxBuffer))
  }
  def createNettyBson(arrayBuffer: ArrayBuffer[Byte]):NettyBson = {
    new NettyBson(scalaArrayBuf = Option(arrayBuffer))
  }


  def parse(netty: NettyBson, key: String, expression: String): Any = {
    val parser = new TinyLanguage

    parser.parseAll(parser.program, expression) match {
      case parser.Success(r, _) =>
        val interpreter = new Interpreter(netty, key, r.asInstanceOf[Program])
        try {
          val result: Any = interpreter.run()
          println("SUCCESS: " + result)
          result
        } catch {
          case e: RuntimeException => println("Error inside run() " + e.getMessage)
        }
      case parser.Error(msg, _) => println("Error parsing: " + msg)
      case parser.Failure(msg, _) => println("Failure parsing: " + msg)
    }

  }

}
