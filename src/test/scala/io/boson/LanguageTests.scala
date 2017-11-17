package io.boson

import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.nettybson.NettyBson
import io.boson.bson.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bsonValue._

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

  def callParse(netty: NettyBson, key: String, expression: String): bsonValue.BsValue = {
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

  test("First") {
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "first")
    assert(BsSeq(List(arr)) === resultParser)
  }

  test("Last") {
    val key: String = "doorOpen"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "last")
    assert(BsSeq(List(true)) === resultParser)
  }

  test("All") {
    val key: String = "fridgeTemp"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "all")
    assert(BsSeq(List(5.2f, 5.0f, 3.854f)) === resultParser)
  }

  test("First [# until #]") {
    val expression: String = "first [0 until 2]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(0))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1)))) === resultParser)
  }

  test("Last [# until #]") {
    val expression: String = "last [0 until 2]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(0))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1)))) === resultParser)
  }

  test("First [# to #]") {
    val expression: String = "first [1 to 2]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(2)))) === resultParser)
  }

  test("Last [# to #]") {
    val expression: String = "last [0 to 2]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(0))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(2)))) === resultParser)
  }

  test("First [# .. end]") {
    val expression: String = "first [0 until end]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(0))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1)))) === resultParser)
  }

  test("Last [# .. end]") {
    val expression: String = "last [0 to end]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(0))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1))
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(2)))) === resultParser)
  }

  test("[# .. end]") {
    val expression: String = "[1 until end]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray()
      .add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1)))) === resultParser)
  }

  test("[# to #]") {
    val expression: String = "[1 to 1]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1)))) === resultParser)
  }

  test("[# until #]") {
    val expression: String = "[1 until 2]"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(bsonEvent.getBsonArray("fridgeReadings").getBsonObject(1)))) === resultParser)
  }

  test("In") {
    val expression: String = "in"
    val key: String = "fridgeTemp"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(true) === resultParser)
  }

  test("Nin") {
    val expression: String = "Nin"
    val key: String = "fridgeTemp"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(false) === resultParser)
  }

  test("(all|first|last) size") {
    val expression: String = "all size"
    val key: String = "fanVelocity"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsNumber(4) === resultParser)
  }

  test("(all|first|last) isEmpty") {
    val expression: String = "first isEmpty"
    val key: String = "fanVelocity"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(false) === resultParser)
  }

  test("[# .. #] size") {
    val expression: String = "[1 to 2] size"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(2)) === resultParser)
  }

  test("[# .. #] isEmpty") {
    val expression: String = "[1 until end] isEmpty"
    val key: String = "fridgeTemp"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(true) === resultParser)
  }

  test("(all|first|last) [# .. #] size") {
    val expression: String = "all [0 to 1] size"
    val key: String = "fridgeReadings"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(Seq(2)) === resultParser)
  }

  test("(all|first|last) [# .. #] isEmpty") {
    val expression: String = "first [0 to 1] isEmpty"
    val key: String = "doorOpen"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(true) === resultParser)
  }

  test("First w/key BobjRoot") {
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "first")
    assert(BsSeq(List()) === resultParser)
  }

  val obj4: BsonObject = new BsonObject().put("fridgeTemp", 5.336f).put("fanVelocity", 40.2).put("doorOpen", true)
  val arrEvent: BsonArray = new BsonArray().add(arr).add(obj4).add("Temperature").add(2.5)

  test("First w/key BarrRoot") {
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "first")
    assert(BsSeq(List(arr)) === resultParser)
  }

  test("Last w/key") {
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "last")
    assert(BsSeq(List(arrEvent.getDouble(3))) === resultParser)
  }

  test("All w/key") {
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, "all")
    assert(BsSeq(List(arrEvent)) === resultParser)
  }

  test("First [# until #] w/key") {
    val expression: String = "first [0 until 2]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(Seq(arrEvent.getBsonArray(0))) === resultParser)
  }

  test("Last [# until #] w/key") {
    val expression: String = "last [0 until 2]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(Seq(arrEvent.getBsonObject(1))) === resultParser)
  }

  test("First [# to #] w/key") {
    val expression: String = "first [1 to 2]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(Seq(arrEvent.getBsonObject(1))) === resultParser)
  }

  test("Last [# to #] w/key") {
    val expression: String = "last [0 to 2]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(Seq(arrEvent.getString(2))) === resultParser)
  }

  test("First [# .. end] w/key") {
    val expression: String = "first [0 until end]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(Seq(arrEvent.getBsonArray(0))) === resultParser)
  }

  test("Last [# .. end] w/key") {
    val expression: String = "all [0 to end]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(arrEvent.getBsonArray(0))
      .add(arrEvent.getBsonObject(1))
      .add(arrEvent.getString(2))
      .add(arrEvent.getDouble(3)))) === resultParser)
  }

  test("[# .. end] w/key") {
    val expression: String = "[1 until end]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(arrEvent.getBsonObject(1))
      .add(arrEvent.getString(2)))) === resultParser)
  }

  test("[# to #] w/key") {
    val expression: String = "[1 to 1]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(arrEvent.getBsonObject(1)))) === resultParser)
  }

  test("[# until #] w/key") {
    val expression: String = "[1 until 2]"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(new BsonArray().add(arrEvent.getBsonObject(1)))) === resultParser)
  }


  val anotherArr: BsonArray = new BsonArray().add(2.2).add(1.5).add(3.1).add(2.5)

  test("(all|first|last) size w/key") {
    val expression: String = "all size"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsNumber(4) === resultParser)
  }

  test("(all|first|last) isEmpty w/key") {
    val expression: String = "first isEmpty"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(false) === resultParser)
  }

  test("[# .. #] size w/key") {
    val expression: String = "[1 to 2] size"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsSeq(List(2)) === resultParser)
  }

  test("[# .. #] isEmpty w/key") {
    val expression: String = "[1 until end] isEmpty"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(false) === resultParser)
  }

  test("(all|first|last) [# .. #] size w/key") {
    val expression: String = "all [0 to 1] size"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsNumber(2) === resultParser)
  }

  test("(all|first|last) [# .. #] isEmpty w/key") {
    val expression: String = "first [0 to 1] isEmpty"
    val key: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser: BsValue = callParse(netty, key, expression)
    assert(BsBoolean(true) === resultParser)
  }

}
