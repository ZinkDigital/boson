package io.boson

import io.boson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.nettybson.NettyBson
import io.boson.bson.{BsonArray, BsonObject}
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


    test("first") {
      val key: String = ""
      val language: String = "first"
      val netty: NettyBson = sI.createNettyBson(ba1.encode().getBytes)
      val result: Any = sI.parse(netty, key, language)

      assert(List("ArrayField") === result)
    }


  }