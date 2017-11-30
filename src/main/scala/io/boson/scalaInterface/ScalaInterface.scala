package io.boson.scalaInterface

import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.nettybson.NettyBson
import scala.collection.mutable.ArrayBuffer


/**
  * Created by Ricardo Martins on 03/11/2017.
  */
class ScalaInterface {

  def createNettyBson(byteArray: Array[Byte]):NettyBson = {
     new NettyBson(byteArray = Option(byteArray))
  }

  def createNettyBson(arrayBuffer: ArrayBuffer[Byte]):NettyBson = {
    new NettyBson(scalaArrayBuf = Option(arrayBuffer))
  }


  def parse(netty: NettyBson, key: String, expression: String): Any = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(netty, key, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) =>  msg
        case parser.Failure(msg, _) =>  msg
      }
    } catch {
      case e:RuntimeException => e.getMessage
    }
  }

}
