package io.boson

import io.boson.nettybson.NettyBson
import io.boson.bson.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bsonPath.TestTinyLanguage.{finalExpr, parse}
import io.boson.bsonPath.TinyLanguage

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

  test("Empty ObjEvent") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(obj1.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Empty ArrEvent") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr1.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Empty ByteBuf") {
    val key: String = "tempReadings"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson()
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Bad parser expression V1") {
    val key: String = "tempReadings"
    val expression: String = "Something Wrong [2 to 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr1.encode()))
    val parser = new TinyLanguage
    intercept[Exception] {
      parser.parseAll(parser.finalExpr(netty, key), expression) match {
        case parser.Success(r, _) => r
        case parser.Error(msg, _) => throw new RuntimeException(msg)
        case parser.Failure(msg, _) => throw new RuntimeException(msg)
      }
    }
  }

  test("Bad parser expression V2") {
    val key: String = ""
    val expression: String = "[0 to 1] kunhnfvgklhu "
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    intercept[RuntimeException] {
      parser.parseAll(parser.finalExpr(netty, key), expression) match {
        case parser.Success(r, _) => r
        case parser.Error(msg, _) => throw new RuntimeException(msg)
        case parser.Failure(msg, _) => throw new RuntimeException(msg)
      }
    }
  }

  test("Bad parser expression V3") {
    val key: String = ""
    val expression: String = ""
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    intercept[Exception] {
      parser.parseAll(parser.finalExpr(netty, key), expression) match {
        case parser.Success(r, _) => r
        case parser.Error(msg, _) => throw new RuntimeException(msg)
        case parser.Failure(msg, _) => throw new RuntimeException(msg)
      }
    }
  }

  test("Bad parser expression V4") {
    val key: String = ""
    val expression: String = "[1 xx 2]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    val thrown =
      intercept[Exception] {
        parser.parseAll(parser.finalExpr(netty, key), expression) match {
          case parser.Success(r, _) => r
          case parser.Error(msg, _) => throw new RuntimeException(msg)
          case parser.Failure(msg, _) => throw new RuntimeException(msg)
        }
      }
    assert(thrown.getMessage === "Array Expression is Invalid")
  }

  test("Bad parser expression V5") {
    val key: String = ""
    val expression: String = "first ?= 4.0 "
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    intercept[RuntimeException] {
      parser.parseAll(parser.finalExpr(netty, key), expression) match {
        case parser.Success(r, _) => r
        case parser.Error(msg, _) => throw new RuntimeException(msg)
        case parser.Failure(msg, _) => throw new RuntimeException(msg)
      }
    }
  }

  test("IndexOutOfBounds") {
    val key: String = ""
    val expression: String = "[2 to 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Extract array when doesn't exists V1") {
    val key: String = "tempReadings"
    val expression: String = "[2 until 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Extract array when doesn't exists V2") {
    val key: String = ""
    val expression: String = "[2 until 3]"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Extract value with wrong Key") {
    val key: String = "tempReadingS"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arrEvent.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Extract value when only exists BsonArray") {
    val key: String = "tempReadingS"
    val expression: String = "first"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val resultParser = parse(finalExpr(netty, key), expression).get
    assert(List() === resultParser)
  }

  test("Check if key exists when key is empty") {
    val key: String = ""
    val expression: String = "in"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    val thrown =
      intercept[Exception] {
        parser.parseAll(parser.finalExpr(netty, key), expression) match {
          case parser.Success(r, _) => r
          case parser.Error(msg, _) => throw new RuntimeException(msg)
          case parser.Failure(msg, _) => throw new RuntimeException(msg)
        }
      }
    assert(thrown.getMessage === "Expressions in/Nin aren't available with Empty Key")
  }

  test("Check if key  doesn't exists when key is empty") {
    val key: String = ""
    val expression: String = "Nin"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    val thrown =
      intercept[Exception] {
        parser.parseAll(parser.finalExpr(netty, key), expression) match {
          case parser.Success(r, _) => r
          case parser.Error(msg, _) => throw new RuntimeException(msg)
          case parser.Failure(msg, _) => throw new RuntimeException(msg)
        }
      }
    assert(thrown.getMessage === "Expressions in/Nin aren't available with Empty Key")
  }

  test("Mixing in/Nin with other expressions") {
    val key: String = ""
    val expression: String = "all > 5 Nin"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    val thrown =
      intercept[Exception] {
        parser.parseAll(parser.finalExpr(netty, key), expression) match {
          case parser.Success(r, _) => r
          case parser.Error(msg, _) => throw new RuntimeException(msg)
          case parser.Failure(msg, _) => throw new RuntimeException(msg)
        }
      }
    assert(thrown.getMessage === "Global Expression is Invalid")
  }

  test("Mixing size/isEmpty with other expressions") {
    val key: String = ""
    val expression: String = "all size Nin"
    val netty: NettyBson = new NettyBson(vertxBuff = Option(arr.encode()))
    val parser = new TinyLanguage
    val thrown =
      intercept[Exception] {
        parser.parseAll(parser.finalExpr(netty, key), expression) match {
          case parser.Success(r, _) => r
          case parser.Error(msg, _) => throw new RuntimeException(msg)
          case parser.Failure(msg, _) => throw new RuntimeException(msg)
        }
      }
    assert(thrown.getMessage === "Global Expression is Invalid")
  }

}
