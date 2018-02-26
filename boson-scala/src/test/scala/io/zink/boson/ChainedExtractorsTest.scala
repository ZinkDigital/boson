package io.zink.boson

import bsonLib.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import io.zink.boson.bson.bsonImpl.Transform._

@RunWith(classOf[JUnitRunner])
class ChainedExtractorsTest extends FunSuite{
  private val book3 = new BsonObject().put("Title", "C++").put("Price", 21.8)
  private val book2 = new BsonObject().put("Title", "Java").put("Price", 20.3)
  private val book1 = new BsonObject().put("Title", "Scala").put("Price", 25.6)
  private val books = new BsonArray().add(book1).add(book2).add(5).add(book3)
  private val store = new BsonObject().put("Book", books)
  private val bson = new BsonObject().put("Store", store)


  private val arr = new BsonArray().add("Store").add("Book").add("allBooks")

  test("Simple String extractor") {

    toPrimitive((in: Seq[Int]) => println(s"Extracted a seq: $in"), Seq(1, 2))
//    val expression: String = ".[all]"
//    val boson: Boson = Boson.extractor(expression, (in: Seq[String]) => println(s"Extracted a seq: $in"))
//    val res = boson.go(arr.encode.getBytes)
//    Await.result(res, Duration.Inf)

  }

}
