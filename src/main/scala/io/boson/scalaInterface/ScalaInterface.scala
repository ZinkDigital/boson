package io.boson.scalaInterface

import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}

import scala.collection.mutable.ArrayBuffer
import io.boson.bson.bsonValue


/**
  * Created by Ricardo Martins on 03/11/2017.
  */
class ScalaInterface {

  def createBoson(byteArray: Array[Byte]):BosonImpl = {
     new BosonImpl(byteArray = Option(byteArray))
  }

  def createBoson(arrayBuffer: ArrayBuffer[Byte]):BosonImpl = {
    new BosonImpl(scalaArrayBuf = Option(arrayBuffer))
  }


  def parse(netty: BosonImpl, key: String, expression: String): bsonValue.BsValue = {
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
