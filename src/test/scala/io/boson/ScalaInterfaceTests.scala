package io.boson

import io.boson.nettybson.NettyBson
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


    test("extractWithScalaInterface") {
      val key: String = ""
      val language: String = "first"
      val netty: NettyBson = sI.createNettyBson(ba1.encode())
      val result: BsValue = sI.parse(netty, key, language)

      assert(BsSeq(Seq("ArrayField")) === result)
    }


  }