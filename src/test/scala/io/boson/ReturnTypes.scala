package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.BsValue
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals
import org.junit.Assert._

@RunWith(classOf[JUnitRunner])
class ReturnTypes extends FunSuite{

  val arr: BsonArray = new BsonArray().add(50).add(new BsonObject().put("one",1)).add(true)
  val byteArrValid: Array[Byte] = arr.encodeToBarray()
  val _bson: BsonObject = new BsonObject().put("obj", new BsonObject().put("one", 1))
  val bA: Array[Byte] = _bson.encodeToBarray()

  private val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
  private val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
  private val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
  private val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
  private val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  private val sEditions3 = new BsonArray().add(edition3)
  private val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
  private val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  private val sEditions2 = new BsonArray().add(edition2)
  private val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
  private val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  private val sEditions1 = new BsonArray().add(edition1)
  private val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
  private val books = new BsonArray().add(title1).add(title2).add(title3)
  private val store = new BsonObject().put("Book", books).put("Hat", hats)
  private val bson = new BsonObject().put("Store", store)

  test("Matched obj in simple event V1") {
    val expression: String = ".Store"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(bson.encodeToBarray())
    assertArrayEquals(store.encodeToBarray(), future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head)
  }

  test("Matched obj in simple event V2") {
    val expression: String = "Store"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(bson.encodeToBarray())
    assertArrayEquals(store.encodeToBarray(), future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head)
  }

  test("return obj inside ArrayPos V1") {
    val expression: String = ".[1]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(books.encodeToBarray())
    assertArrayEquals(books.getBsonObject(1).encodeToBarray(), future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head)
  }

  test("return obj inside ArrayPos V2") {
    val expression: String = ".[1 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(books.encodeToBarray())
    assertArrayEquals(books.getBsonObject(1).encodeToBarray(), future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head)
  }

  test("return obj inside ArrayPos V3") {
    val expression: String = "[1]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(books.encodeToBarray())
    assertArrayEquals(books.getBsonObject(1).encodeToBarray(), future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head)
  }

  test("return obj inside ArrayPos V4") {
    val expression: String = "Book[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(bson.encodeToBarray())
    val expected: Vector[Array[Byte]] =
      Vector(books.getBsonObject(0).encodeToBarray(),books.getBsonObject(1).encodeToBarray(),books.getBsonObject(2).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    for(i <- result.indices) {
      assertArrayEquals(expected.apply(i),result.apply(i))
    }
  }

  test("return obj inside ArrayPos V5") {
    val expression: String = ".Store.Book"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(bson.encodeToBarray())
    val expected: Vector[Array[Byte]] =
      Vector(books.getBsonObject(0).encodeToBarray(),books.getBsonObject(1).encodeToBarray(),books.getBsonObject(2).encodeToBarray())
    val result: Vector[Array[Byte]] = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(e => e._1.sameElements(e._2)))
    //assertArrayEquals(books.encodeToBarray(), future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head)
  }

}
