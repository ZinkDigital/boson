package io.zink.boson

import bsonLib.BsonObject
import io.netty.util.ResourceLeakDetector
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
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  //TODO - Refactoring tests to be up-to-date with the current code

  /*val boson: BosonImpl = new BosonImpl(byteArray = Option(bson.encode().getBytes))

  test("extract top field") {
    val expression: String = ".Epoch"
    val boson: Boson = Boson.extractor(expression, (out: Int) => {
      assertTrue(3 == out)
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("extract bottom field") {
    val expression: String = "SSLNLastName"
    val expected: Seq[String] = Seq("de Huanuco")
    val boson: Boson = Boson.extractor(expression, (out: Seq[String]) => {
      assertTrue(expected.zip(out).forall(e => e._1.equals(e._2)))
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

//  test("extract all occurrences of Key") {
//    val result: BsValue = callParse(boson.duplicate, "Tags")
//    println(result.asInstanceOf[BsSeq].value)
//    val t: Boolean = true
//    assert(t)
//  }
//
//  test("extract positions of an Array") {
//    val result: BsValue = callParse(boson.duplicate, "Markets[3 to 5]")
//    println(result.asInstanceOf[BsSeq].getValue)
//    val t: Boolean = true
//    assert(t)
//  }
//
//  test("extract further positions of an Array") {
//    val result: BsValue = callParse(boson.duplicate, "Markets[50 to 55]")
//    println(result.asInstanceOf[BsSeq].getValue.head)
//    val t: Boolean = true
//    assert(t)
//  }

  test("size of all occurrences of Key") {
    val expression: String = "Price"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Double]) => {
      assertTrue(195 == out.size)
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }*/

}
