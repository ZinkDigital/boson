package io.boson

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue.{BsBoolean, BsNumber, BsSeq, BsValue}
import io.boson.nettyboson.Boson
import io.boson.scalaInterface.ScalaInterface
import io.vertx.core.json.JsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
@RunWith(classOf[JUnitRunner])
class LongInputTests extends FunSuite {

  val sI: ScalaInterface = new ScalaInterface

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  val boson: Boson = sI.createBoson(bson.encode().getBytes)

  test("extract top field") {
    val result: BsValue = sI.parse(boson.duplicate, "Epoch", "first")
    assert(bson.getInteger("Epoch") === result.asInstanceOf[BsSeq].value.head)
  }

  test("extract bottom field") {
    val result: BsValue = sI.parse(boson.duplicate, "SSLNLastName", "last")
    assert( "de Huanuco" ===
      new String(result.asInstanceOf[BsSeq].value.head.asInstanceOf[Array[Byte]]).replaceAll("\\p{C}", ""))
  }

  test("extract all occurrences of Key") {
    val result: BsValue = sI.parse(boson.duplicate, "Tags", "all")
    println(result.asInstanceOf[BsSeq].value)
    assert(true)
  }

  test("extract positions of an Array") {
    val result: BsValue = sI.parse(boson.duplicate, "Markets", "[3 to 5]")
    println(result.asInstanceOf[BsSeq].getValue
      .asInstanceOf[Seq[BsonArray]].head.forEach(e => println(e)))
    assert(true)
  }

  test("select one Pos of array extraction") {
    val result: BsValue = sI.parse(boson.duplicate, "Markets", "first [50 to 55]")
    println(result.asInstanceOf[BsSeq].getValue.head.asInstanceOf[BsonObject])
    assert(true)
  }

  test("size of all occurrences of Key") {
    val result: BsValue = sI.parse(boson.duplicate, "Price", "all size")
    assert(283 === result.asInstanceOf[BsNumber].value)
  }

  test("existence of a Key") {
    val result: BsValue = sI.parse(boson.duplicate, "WrongKey", "in")
    assert(false === result.asInstanceOf[BsBoolean].value)
  }

  test("sizes of filtered arrays") {
    val result: BsValue = sI.parse(boson.duplicate, "Selections", "[1 to 2] size")
    assert(Seq(2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2) ===
      result.asInstanceOf[BsSeq].value)
  }

  test("size of specific filtered arrays") {
    val result: BsValue = sI.parse(boson.duplicate, "Selections", "last [0 until end] size")
    assert(Seq(2) === result.asInstanceOf[BsSeq].value)
  }

  test("emptiness of filtered Array") {
    val result: BsValue = sI.parse(boson.duplicate, "Selections", "[5 to end] isEmpty")
    assert(false === result.asInstanceOf[BsBoolean].value)
  }
}
