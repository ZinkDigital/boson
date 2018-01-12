package io.boson

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue.{BsException, BsSeq, BsValue}
import io.boson.bson.bsonValue
import org.junit.Assert.assertEquals

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

  def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          val interpreter = new Interpreter(boson, r.asInstanceOf[Program])
          interpreter.run()
        case parser.Error(_, _) => bsonValue.BsObject.toBson("Error parsing!")
        case parser.Failure(_, _) => bsonValue.BsObject.toBson("Failure parsing!")
      }
    } catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("Empty ObjEvent") {
    val expression: String = "tempReadings..first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(obj1.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Empty ArrEvent") {
    val expression: String = "tempReadings..first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Empty ByteBuf") {
    val expression: String = "tempReadings..first"
    val boson: BosonImpl = new BosonImpl()
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Bad parser expression V1") {
    val expression: String = "tempReadings.Something Wrong [2 to 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V2") {
    val expression: String = "[0 to 1] kunhnfvgklhu "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V3") {
    val expression: String = ""
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V4") {
    val expression: String = "[1 xx 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V5") {
    val expression: String = "first ?= 4.0 "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("IndexOutOfBounds") {
    val expression: String = "[2 to 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract array when doesn't exists V1") {
    val expression: String = "tempReadings.[2 until 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract array when doesn't exists V2") {
    val expression: String = "[2 until 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract value with wrong Key") {
    val expression: String = "tempReadingS..first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract value when only exists BsonArray") {
    val expression: String = "tempReadingS..first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val resultParser = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Only WhiteSpaces in Expression") {
    val expression: String = "  "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("array prob 1") {
    val expression: String = "   José.[     0    to   end      ]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      List("Tarantula", "Aracnídius", List("Insecticida")),
      List("Spider"),
      List("Fly")
    )), result)
  }

  test("array prob 2") {
    val expression: String = "[     0    to   end      ]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr11.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(Seq(
        Map("José" -> List("Tarantula", "Aracnídius", List("Insecticida"))),
        Map("José" -> List("Spider")),
        Map("José" -> List("Fly")),
        List("Insecticida")
      ))), result)
  }

  test("array prob 3") {
    val expression: String = ".first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr11.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      Map("José" -> List("Tarantula", "Aracnídius", List("Insecticida")))
    )), result)
  }

  test("array prob 4") {
    val expression: String = "   José . . first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      "Tarantula",
      "Aracnídius",
      Seq("Insecticida")
    )), result)
  }

  test("array prob 5") {
    br3.add("Wrong")
    br2.add("some")
    val expression: String = "José.[0 until end]  "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      Seq("Tarantula", "Aracnídius"),
      Seq("Spider"),
      Seq("Fly")
    )), result)
  }

  val b1: BsonObject = new BsonObject().put("one",1).put("two",2).put("three",3)
  val b2: BsonObject = new BsonObject().put("one",1.1).put("two",2.2).put("three",3.3)
  val b3: BsonObject = new BsonObject().put("one",1f).put("two",2f).put("three",3f)
  val a1: BsonArray = new BsonArray().add(b1).add(0.1).add(b2).add(0.2).add(b3).add(0.3)
  val b4: BsonObject = new BsonObject().put("stuff",true).put("arr", a1).put("things",false)
  val b5: BsonObject = new BsonObject().put("stuff",false).put("arr", a1).put("things",true)
  val a2: BsonArray = new BsonArray().add(b4).add(b4).add(b5)

  test("key doesn't match with an array") {
    val expression: String = "things.[2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("1stKey exists many times") {
    val expression: String = "arr.[2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      Seq(Map("one" -> 1.1, "two" -> 2.2, "three" -> 3.3)),
      Seq(Map("one" -> 1.1, "two" -> 2.2, "three" -> 3.3)),
      Seq(Map("one" -> 1.1, "two" -> 2.2, "three" -> 3.3))
    )), result)
  }

  test("1stKey exists many times, looking for another key") {
    val expression: String = "arr.[2].two"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      Seq(2.2),
      Seq(2.2),
      Seq(2.2)
    )), result)
  }

  test("1stKey matches, 2ndKey doen't exists") {
    val expression: String = "[2].zombie"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(Seq())), result)
  }

  test("key doesn't match with an array with 2ndKey") {
    val expression: String = "things.[2].one"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("until end Problem") {
    val array: BsonArray = new BsonArray().add(1.1).add(2.2).add(3.3).add(4.4)
    val expression: String = "[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(array.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      Seq(2.2,3.3)
    )), result)
  }

  test("try extract obj with certain elem, but isn't obj") {
    val expression: String = "one.[@smth]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("try extract obj with certain elem, but elem emptyt") {
    val expression: String = "one.[@]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsException("Failure parsing!"), result)
  }

  test("try extract obj with certain elem, but elem is deep") {
    val arr111: BsonArray = new BsonArray().add(obj11).add(obj2).add(obj3).add(br4)
      .add(new BsonObject().put("smth",new BsonArray().add(new BsonObject().put("extract", false))))
    val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr111)
    val expression: String = "StartUp.[@extract]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

}
