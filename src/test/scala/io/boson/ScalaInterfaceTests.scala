package io.boson

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue._
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
      val boson: BosonImpl = sI.createBoson(ba1.encode().getBytes)
      val result: BsValue = sI.parse(boson, key, language)
      assert(BsSeq(Seq("ArrayField")) === result)
    }

//    test("extractIntWithScalaInterface") {
//      val key: String = ""
//      val language: String = "[5 until 6] size"
//      val boson: BosonImpl = sI.createBoson(ba1.encode().getBytes)
//      val result: BsValue = sI.parse(boson, key, language)
//
//      assert(BsNumber(BigDecimal(0)) === result)
//    }

//    test("extractBoolWithScalaInterface") {
//      val key: String = ""
//      val language: String = "[5 to end] isEmpty"
//      val boson: BosonImpl = sI.createBoson(ba1.encode().getBytes)
//      val result: BsValue = sI.parse(boson, key, language)
//
//      assert(BsBoolean(true) === result)
//    }

    test("extractExceptionWithScalaInterface") {
      val key: String = "field1"
      val language: String = "[0 until "

      val boson: BosonImpl = sI.createBoson(ba1.encode().getBytes)
      val result: BsValue = sI.parse(boson, key, language)

      assert(BsException("`end' expected but end of source found") === result)
    }

  }