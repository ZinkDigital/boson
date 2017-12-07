package io.boson

import io.boson.nettyboson.Boson
import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue._
import io.boson.scalaInterface.ScalaInterface
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


  @RunWith(classOf[JUnitRunner])
  class ScalaInterfaceTests extends FunSuite {

    val bo1: BsonObject = new BsonObject().put("field2", 0)
    val bo2: BsonObject = new BsonObject().put("field1", 1)
    val ba1: BsonArray = new BsonArray().add("ArrayField").add(bo1).add(bo2)
    val sI: ScalaInterface = new ScalaInterface


    test("extractSeqWithScalaInterface") {
      val key: String = ""
      val language: String = "first"
      val boson: Boson = sI.createBoson(ba1.encode().getBytes)
      val result: BsValue = sI.parse(boson, key, language)
      assert(BsSeq(Seq("ArrayField")) === result)
    }

    test("extractIntWithScalaInterface") {
      val key: String = ""
      val language: String = "[5 until 6] size"
      val boson: Boson = sI.createBoson(ba1.encode().getBytes)
      val result: BsValue = sI.parse(boson, key, language)

      assert(BsNumber(BigDecimal(0)) === result)
    }

    test("extractBoolWithScalaInterface") {
      val key: String = ""
      val language: String = "[5 to end] isEmpty"
      val boson: Boson = sI.createBoson(ba1.encode().getBytes)
      val result: BsValue = sI.parse(boson, key, language)

      assert(BsBoolean(true) === result)
    }

    test("extractExceptionWithScalaInterface") {
      val key: String = "field1"
      val language: String = "last [0 until end] in"

      val boson: Boson = sI.createBoson(ba1.encode().getBytes)
      val result: BsValue = sI.parse(boson, key, language)

      assert(BsException("`isEmpty' expected but `i' found") === result)
    }

  }