package io.zink.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration

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
  private val title2 = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", sEditions2)
  private val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  private val sEditions1 = new BsonArray().add(edition1)
  private val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
  private val books = new BsonArray().add(title1).add(title2).add(title3)
  private val store = new BsonObject().put("Book", books).put("Hat", hats)
  private val bson = new BsonObject().put("Store", store)

  test("Matched in complex event") {
    val obj555: BsonObject = new BsonObject().put("Store", new BsonArray())
    val arr444: BsonArray = new BsonArray().add(obj555)//.add(obj555)
    val obj666: BsonObject = new BsonObject().put("Store",new BsonArray().add(new BsonObject().put("Store",1.1)))
    val obj333: BsonObject = new BsonObject().put("jtbfi",obj666)//.put("Store", arr444)
    val arr222: BsonArray = new BsonArray().add(obj333)//.add(obj333)
    //put("Store",new BsonObject())
    val obj111: BsonObject = new BsonObject().put("Store", arr222)
    val expression: String = "Store[@Store]..Store"
    val future: CompletableFuture[Seq[Double]] = new CompletableFuture[Seq[Double]]()
    val boson: Boson = Boson.extractor(expression, (in: Seq[Double]) => future.complete(in))
    boson.go(obj111.encodeToBarray())
    val res = future.join()
    //res.foreach(elem => println(s"res: ${new String(elem)}"))
    val expected: Seq[Double] = Vector(1.1)
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e,r) => e == r
    })
  }

  test("Matched obj in simple event V1") {
    val expression: String = ".Store"
    val future: CompletableFuture[Array[Byte]] = new CompletableFuture[Array[Byte]]()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => future.complete(in))
    boson.go(bson.encodeToBarray())
    assertArrayEquals(store.encodeToBarray(), future.join())
  }

  test("Matched obj in simple event V2") {
    val expression: String = "Store"
    val expected: Seq[Array[Byte]] = Seq(store.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertTrue(expected.size === in.size)
      assertTrue(expected.zip(in).forall(b => b._1.sameElements(b._2)))
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("return obj inside ArrayPos V1") {
    val expression: String = ".[1]"
    val future: CompletableFuture[Array[Byte]] = new CompletableFuture[Array[Byte]]()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => future.complete(in))
    boson.go(books.encodeToBarray())
    assertArrayEquals(books.getBsonObject(1).encodeToBarray(), future.join())
  }

  test("return obj inside ArrayPos V2") {
    val expression: String = ".[1 until end]"
    val expected: Seq[Array[Byte]] = Seq(books.getBsonObject(1).encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertTrue(expected.size === in.size)
      assertTrue(expected.zip(in).forall(b => b._1.sameElements(b._2)))
    })
    val res = boson.go(books.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("return obj inside ArrayPos V3") {
    val expression: String = "[1]"
    val expected: Seq[Array[Byte]] = Seq(books.getBsonObject(1).encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertTrue(expected.size === in.size)
      assertTrue(expected.zip(in).forall(b => b._1.sameElements(b._2)))
    })
    val res = boson.go(books.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("return obj inside ArrayPos V4") {
    val expression: String = "Book[@Title]"
    val expected: Seq[Array[Byte]] =
      Seq(books.getBsonObject(0).encodeToBarray(),books.getBsonObject(1).encodeToBarray(),books.getBsonObject(2).encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertTrue(expected.size === in.size)
      assertTrue(expected.zip(in).forall(b => b._1.sameElements(b._2)))
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("return obj inside ArrayPos V5") {
    val expression: String = ".Store.Book"
    val expected: Array[Byte] = books.encodeToBarray()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(expected, in)
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

}
