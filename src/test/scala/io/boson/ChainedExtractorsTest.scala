package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ChainedExtractorsTest extends FunSuite{

  private val store = new BsonObject().put("Book", "Scala")
  private val bson = new BsonObject().put("Store", store)

  test("Simple extractor") {

    val expression: String = ".Store.Book"
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsSeq) => println(s"I got it: $in"))
    boson.go(bson.encode.getBytes).join()

    //Thread.sleep(1500)

//    val expected: Vector[Any] = Vector("Scala")
//    val result = future.join().getValue.asInstanceOf[Vector[Any]]
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte], r: Array[Byte]) =>
//        println(s"e: $e, r: $r, are they equal? ${e.sameElements(r)}")
//        e.sameElements(r)
//      case (e, r) => e.equals(r)
//    })
  }

}
