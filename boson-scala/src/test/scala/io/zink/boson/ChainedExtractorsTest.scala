package io.zink.boson

import bsonLib.BsonObject
import io.zink.boson.bson.bsonValue.BsSeq
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class ChainedExtractorsTest extends FunSuite{

  private val store = new BsonObject().put("Book", 10L)
  private val bson = new BsonObject().put("Store", store)

  test("Simple String extractor") {

    val expression: String = ".Store.Book"
    val boson: Boson = Boson.extractor(expression, (in: BsSeq) => println(s"I got it: $in"))
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)

  }

}
