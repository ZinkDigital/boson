package io.boson

import io.boson.nettybson.NettyBson
import io.boson.bson.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bsonValue.{BsException, BsSeq, BsValue}

/**
  * Created by Tiago Filipe on 25/10/2017.
  */

@RunWith(classOf[JUnitRunner])
class HorribleTests extends FunSuite {

  val obj1: BsonObject = new BsonObject()
  val arr1: BsonArray = new BsonArray()

  val bsonEvent: BsonObject = new BsonObject().put("tempReadings", "Zero")
  val arrEvent: BsonArray = new BsonArray().add(bsonEvent)
  val arr: BsonArray = new BsonArray().add(1).add(2)

  def callParse(netty: NettyBson, key: String, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          val interpreter = new Interpreter(netty, key, r.asInstanceOf[Program])
          interpreter.run()
        case parser.Error(_, _) => bsonValue.BsObject.toBson("Error parsing!")
        case parser.Failure(_, _) => bsonValue.BsObject.toBson("Failure parsing!")
      }
    } catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("Empty ObjEvent") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(obj1.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Empty ArrEvent") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr1.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Empty ByteBuf") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson()
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Bad parser expression V1") {
    val key: String = "tempReadings"
    val expression: String = "Something Wrong [2 to 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr1.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V2") {
    val key: String = ""
    val expression: String = "[0 to 1] kunhnfvgklhu "
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V3") {
    val key: String = ""
    val expression: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V4") {
    val key: String = ""
    val expression: String = "[1 xx 2]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V5") {
    val key: String = ""
    val expression: String = "first ?= 4.0 "
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("IndexOutOfBounds") {
    val key: String = ""
    val expression: String = "[2 to 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract array when doesn't exists V1") {
    val key: String = "tempReadings"
    val expression: String = "[2 until 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract array when doesn't exists V2") {
    val key: String = ""
    val expression: String = "[2 until 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract value with wrong Key") {
    val key: String = "tempReadingS"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract value when only exists BsonArray") {
    val key: String = "tempReadingS"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val resultParser = callParse(netty, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Check if key exists when key is empty") {
    val key: String = ""
    val expression: String = "in"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Expressions in/Nin aren't available with Empty Key") === result)
  }

  test("Check if key  doesn't exists when key is empty") {
    val key: String = ""
    val expression: String = "Nin"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Expressions in/Nin aren't available with Empty Key") === result)
  }

  test("Mixing in/Nin with other expressions") {
    val key: String = ""
    val expression: String = "all > 5 Nin"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Mixing size/isEmpty with other expressions") {
    val key: String = ""
    val expression: String = "all size Nin"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val result: BsValue = callParse(netty, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

}
