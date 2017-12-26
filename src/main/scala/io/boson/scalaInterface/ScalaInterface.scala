package io.boson.scalaInterface

import io.boson.bson.bsonImpl.Boson
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}

import scala.collection.mutable.ArrayBuffer
import io.boson.bson.bsonValue
import io.boson.bson.bsonImpl.injectors.{InterpreterInj, ProgramInj, TinyLanguageInj}


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

  def parseInj(netty: Boson, key: String,f: Any => Any, expression: String):bsonValue.BsValue = {
    val parser = new TinyLanguageInj
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new InterpreterInj(netty, key, f, r.asInstanceOf[ProgramInj]).run()
        case parser.Error(msg, _) => bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) => bsonValue.BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

}
