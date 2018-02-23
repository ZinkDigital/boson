package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ChainedExtractorsTest extends FunSuite{

  private val store = new BsonObject().put("Book", 10L)
  private val bson = new BsonObject().put("Store", store)

  test("Simple String extractor") {

    val expression: String = ".Store.Book"
    val boson: Boson = Boson.extractor(expression, (in: BsSeq) => println(s"I got it: $in"))
    boson.go(bson.encode.getBytes).join()

  }

}
