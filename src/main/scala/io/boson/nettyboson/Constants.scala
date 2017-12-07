package io.boson.nettyboson

import java.nio.charset.Charset

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bsonValue
import io.boson.bsonValue.BsValue


object Constants {
  val EMPTY_CONSTRUCTOR: String = "EmptyConstructor"
  val SCALA_ARRAYBUF: String = "ArrayBuffer"
  val JAVA_BYTEBUFFER: String = "HeapByteBuffer"
  val ARRAY_BYTE: String = "byte[]"

  val D_ZERO_BYTE: Int = 0
  val D_FLOAT_DOUBLE: Int = 1
  val D_ARRAYB_INST_STR_ENUM_CHRSEQ: Int = 2
  val D_BSONOBJECT: Int = 3
  val D_BSONARRAY: Int = 4
  val D_BOOLEAN: Int = 8
  val D_NULL: Int = 10
  val D_INT: Int = 16
  val D_LONG: Int = 18

  // Our Own Netty Buffer Implementation
  val charset: Charset = java.nio.charset.Charset.availableCharsets().get("UTF-8")
}

object testBsonObject extends App {
  val bsonEvent: BsonObject = new BsonObject().put("tempReadings", "Zero")
  val arrEvent: BsonArray = new BsonArray().add(bsonEvent)
  def callParse(boson: Boson, key: String, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          val interpreter = new Interpreter(boson, key, r.asInstanceOf[Program])
          interpreter.run()
        case parser.Error(_, _) => bsonValue.BsObject.toBson("Error parsing!")
        case parser.Failure(_, _) => bsonValue.BsObject.toBson("Failure parsing!")
      }
    } catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  val key: String = ""
  val expression: String = "   first    [     0    to   end      ]   size  "
  val boson: Boson = new Boson(byteArray = Option(arrEvent.encode().getBytes))
  val result: BsValue = callParse(boson, key, expression)
  println(result)
}
