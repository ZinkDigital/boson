package io.zink.boson

import bsonLib.BsonObject
import bsonLib.BsonArray
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration


@RunWith(classOf[JUnitRunner])
class InjectValueTest extends FunSuite {

  test("CodecJson - Top level key inject String value") {
    val expected = new BsonObject().put("name", "Albertina").encodeToBarray()
    val bson = new BsonObject().put("name", "Albert")
    val ex = ".name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall( b => b._1 == b._2)

    println("Exp: "+ expected.mkString(", "))
    println("Res: "+result.mkString(", "))

    assert(equals == true)
  }

  test("CodecJson - Top level key inject Int value") {
    val expected = new BsonObject().put("age", 3).encodeToBarray()
    val bson = new BsonObject().put("age", 20)
    val ex = ".age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall( b => b._1 == b._2)

    println("Exp: "+ expected.mkString(", "))
    println("Res: "+result.mkString(", "))

    assert(equals == true)
  }

  test("CodecJson - Top level key inject String Nested value") {
    val expectedLayer1 = new BsonObject().put("age", 3)
    val expected = new BsonObject().put("person", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonObject().put("age", 20)
    val bson = new BsonObject().put("person", bsonLayer1)
    val ex = ".person.age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall( b => b._1 == b._2)

    println("Exp: "+ expected.mkString(", "))
    println("Res: "+result.mkString(", "))

    assert(equals == true)
  }

  test("CodecJson - Top level array inject value") {
    val expectedLayer1 = new BsonArray().add(911).add(112)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[0]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall( b => b._1 == b._2)

    println("Exp: "+ expected.mkString(", "))
    println("Res: "+result.mkString(", "))

    assert(equals == true)
  }

}
