package io.boson

import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bson.bsonValue._
import io.boson.bson.bsonValue

/**
  * Created by Tiago Filipe on 18/10/2017.
  */

@RunWith(classOf[JUnitRunner])
class LanguageTests extends FunSuite {

  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)

  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  def callParse(boson: BosonImpl, expression: String): io.boson.bson.bsonValue.BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) =>  bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>  bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e:RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("All") {
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, "fridgeTemp")

    assert(BsSeq(Vector(5.2f, 5.0f, 3.854f)) === resultParser)
  }

  test("[# .. end]") {
    val expression: String = "fridgeReadings.[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false
      )))) === resultParser)
  }

  test("[# to #]") {
    val expression: String = "fridgeReadings.[1 to 1]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false
      )))) === resultParser)
  }

  test("[# until #]") {
    val expression: String = "fridgeReadings.[1 until 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false
      )))) === resultParser)
  }

  val obj4: BsonObject = new BsonObject().put("fridgeTemp", 5.336f).put("fanVelocity", 40.2).put("doorOpen", true)
  val arrEvent: BsonArray = new BsonArray().add(arr).add(obj4).add("Temperature").add(2.5)

//  test("First [# until #] w/key") {
//    val expression: String = "first [0 until 2]"
//    val key: String = ""
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
//    val resultParser: BsValue = callParse(boson, key, expression)
//    assert(BsSeq(List(List(
//      Map("fridgeTemp" -> 5.199999809265137, "fanVelocity" -> 20.5, "doorOpen" -> false),
//      Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false),
//      Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true)))) === resultParser)
//  }
//
//  test("Last [# until #] w/key") {
//    val expression: String = "last [0 until 2]"
//    val key: String = ""
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
//    val resultParser: BsValue = callParse(boson, key, expression)
//    println(s"resultParser: $resultParser")
//    assert(BsSeq(List(
//      Map("fridgeTemp" -> 5.335999965667725, "fanVelocity" -> 40.2, "doorOpen" -> true))) === resultParser)
//  }
//
//  test("First [# to #] w/key") {
//    val expression: String = "first [1 to 2]"
//    val key: String = ""
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
//    val resultParser: BsValue = callParse(boson, key, expression)
//    assert(BsSeq(List(
//      Map("fridgeTemp" -> 5.335999965667725, "fanVelocity" -> 40.2, "doorOpen" -> true))) === resultParser)
//  }
//
//  test("Last [# to #] w/key") {
//    val expression: String = "last [0 to 2]"
//    val key: String = ""
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
//    val resultParser: BsValue = callParse(boson, key, expression)
//    assert(BsSeq(Seq(arrEvent.getString(2))) === resultParser)
//  }
//
//  test("First [# .. end] w/key") {
//    val expression: String = "first [0 until end]"
//    val key: String = ""
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
//    val resultParser: BsValue = callParse(boson, key, expression)
//    assert(BsSeq(List(List(
//      Map("fridgeTemp" -> 5.199999809265137, "fanVelocity" -> 20.5, "doorOpen" -> false),
//      Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false),
//      Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true)))) === resultParser)
//  }
//
//  test("All [# .. end] w/key") {
//    val expression: String = "all [0 to end]"
//    val key: String = ""
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
//    val resultParser: BsValue = callParse(boson, key, expression)
//    assert(BsSeq(List(List(
//      Map("fridgeTemp" -> 5.199999809265137, "fanVelocity" -> 20.5, "doorOpen" -> false),
//      Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false),
//      Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true)),
//      Map("fridgeTemp" -> 5.335999965667725, "fanVelocity" -> 40.2, "doorOpen" -> true),
//      "Temperature",
//      2.5)) === resultParser)
//  }

  test("[# .. end] w/key") {
    val expression: String = "[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.335999965667725, "fanVelocity" -> 40.2, "doorOpen" -> true),
      "Temperature"
    ))) === resultParser)
  }

  test("[# to #] w/key") {
    val expression: String = "[1 to 1]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.335999965667725, "fanVelocity" -> 40.2, "doorOpen" -> true
      )))) === resultParser)
  }

  test("[# until #] w/key") {
    val expression: String = "[1 until 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.335999965667725, "fanVelocity" -> 40.2, "doorOpen" -> true
      )))) === resultParser)
  }

}
