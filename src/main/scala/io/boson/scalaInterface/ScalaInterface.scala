package io.boson.scalaInterface

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

  def createBoson(byteArray: Array[Byte]):Boson = {
     new Boson(byteArray = Option(byteArray))
  }

  def createBoson(arrayBuffer: ArrayBuffer[Byte]):Boson = {
    new Boson(scalaArrayBuf = Option(arrayBuffer))
  }


  def parse(boson: Boson, key: String, expression: String): bsonValue.BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, key, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) =>  bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>  bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e:RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

}
