package io.boson

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue.{BsException, BsNumber, BsSeq, BsValue}
import io.boson.bson.bsonValue

/**
  * Created by Tiago Filipe on 25/10/2017.
  */

@RunWith(classOf[JUnitRunner])
class HorribleTests extends FunSuite {

  val br4: BsonArray = new BsonArray().add("Insecticida")
  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
  val obj11: BsonObject = new BsonObject().put("José", br1)
  val br2: BsonArray = new BsonArray().add("Spider")
  val obj2: BsonObject = new BsonObject().put("José", br2)
  val br3: BsonArray = new BsonArray().add("Fly")
  val obj3: BsonObject = new BsonObject().put("José", br3)
  val arr11: BsonArray = new BsonArray().add(obj11).add(obj2).add(obj3).add(br4)
  val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr11)

  val obj1: BsonObject = new BsonObject()
  val arr1: BsonArray = new BsonArray()

  val bsonEvent: BsonObject = new BsonObject().put("tempReadings", "Zero")
  val arrEvent: BsonArray = new BsonArray().add(bsonEvent)
  val arr: BsonArray = new BsonArray().add(1).add(2)

  def callParse(boson: BosonImpl, key: String, expression: String): BsValue = {
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

  test("Empty ObjEvent") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(obj1.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Empty ArrEvent") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Empty ByteBuf") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val boson: BosonImpl = new BosonImpl()
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Bad parser expression V1") {
    val key: String = "tempReadings"
    val expression: String = "Something Wrong [2 to 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V2") {
    val key: String = ""
    val expression: String = "[0 to 1] kunhnfvgklhu "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V3") {
    val key: String = ""
    val expression: String = ""
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V4") {
    val key: String = ""
    val expression: String = "[1 xx 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V5") {
    val key: String = ""
    val expression: String = "first ?= 4.0 "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("IndexOutOfBounds") {
    val key: String = ""
    val expression: String = "[2 to 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract array when doesn't exists V1") {
    val key: String = "tempReadings"
    val expression: String = "[2 until 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract array when doesn't exists V2") {
    val key: String = ""
    val expression: String = "[2 until 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract value with wrong Key") {
    val key: String = "tempReadingS"
    val expression: String = "first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Extract value when only exists BsonArray") {
    val key: String = "tempReadingS"
    val expression: String = "first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val resultParser = callParse(boson, key, expression)
    assert(BsSeq(Seq()) === resultParser)
  }

  test("Only WhiteSpaces in Expression") {
    val key: String = ""
    val expression: String = "  "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsException("Failure parsing!") === result)
  }
    //  TODO:remake this 4 tests without size expression
//  test("array prob 1") {
//    val key: String = "José"
//    val expression: String = "   all    [     0    to   end      ]   size  "
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
//    val result: BsValue = callParse(boson, key, expression)
//    assert(BsNumber(3) === result)
//  }
//
//  test("array prob 2") {
//    val key: String = ""
//    val expression: String = "   all    [     0    to   end      ]   size  "
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr11.encode().getBytes))
//    val result: BsValue = callParse(boson, key, expression)
//    println(s"result in Test: $result")
//    assert(BsNumber(4) === result)
//  }
//
//  test("array prob 3") {
//    val key: String = ""
//    val expression: String = "   first    [     0    to   end      ]   size  "
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr11.encode().getBytes))
//    val result: BsValue = callParse(boson, key, expression)
//    assert(BsNumber(1) === result)
//  }
//
//  test("array prob 4") {
//    val key: String = "José"
//    val expression: String = "   first    [     0    to   end      ]   size  "
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
//    val result: BsValue = callParse(boson, key, expression)
//    assert(BsNumber(3) === result)
//  }

  test("array prob 5") {
    br3.add("Wrong")
    br2.add("some")
    val key: String = "José"
    val expression: String = "José.[0 until end]  "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, key, expression)
    assert(BsSeq(List(List("Tarantula", "Aracnídius"), List("Spider"), List("Fly"))) === result)
  }

}
