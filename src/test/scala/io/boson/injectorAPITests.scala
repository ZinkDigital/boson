package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.{BosonImpl, BosonInjector}
import io.boson.bson.bsonValue._
import mapper.Mapper
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class injectorAPITests extends FunSuite {

  val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
  val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
  val br2: BsonArray = new BsonArray().add(b5).add(b6)
  val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5)
  val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5)
  val br1: BsonArray = new BsonArray().add(b3).add(b4)
  val b2: BsonObject = new BsonObject().put("Book", br1).put("Hat",br2)
  val bsonEvent: BsonObject = new BsonObject().put("Store", b2)

  val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

//  val bAux2: BsonObject = new BsonObject().put("google", "DAMMN")
//  val bsonArrayEvent1: BsonArray = new BsonArray().add(bAux2)
//  val bAux: BsonObject = new BsonObject().put("damnnn", bsonArrayEvent1)
//  val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux)
//  val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)
//  val validatedByteArr111: Array[Byte] = bsonObjectRoot.encodeToBarray()
//  test("extract ") {
//    val expression = "array.[@damnnn].damnnn.[@google]"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
//    boson.go(validatedByteArr111)
//
//    assertEquals(BsSeq(Vector(
//      Seq("Java")
//    )), future.join())
//  }

  test("extract Key.Key") {
    val expression = "Store.Book"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5),
      Map("Title" -> "Scala", "Pri" -> 21.5)
    ))), future.join())
  }

  test("extract Key.Key.[@elem]") {
    val expression = "Store.Book.[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5)
    ))), future.join())
  }

  test("extract Key.*halfKey") {
    val expression = "Store.*at"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Color" -> "Red", "Price" -> 48),
      Map("Color" -> "White", "Price" -> 35)
    ))), future.join())
  }

  test("extract Key.halfKey*") {
    val expression = "Store.H*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Color" -> "Red", "Price" -> 48),
      Map("Color" -> "White", "Price" -> 35)
    ))), future.join())
  }

  test("extract Key.halfKey*halfKey") {
    val expression = "Store.H*t"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Color" -> "Red", "Price" -> 48),
      Map("Color" -> "White", "Price" -> 35)
    ))), future.join())
  }

  test("extract Key.*") {
    val expression = "Store.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq(Map("Title" -> "Java", "Price" -> 15.5), Map("Title" -> "Scala", "Pri" -> 21.5)),
      Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35))
    )), future.join())
  }

  test("extract *") {
    val expression = "*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Map("Store" -> Map(
        "Book" -> Seq(Map("Title" -> "Java", "Price" -> 15.5), Map("Title" -> "Scala", "Pri" -> 21.5)),
        "Hat" -> Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35))))
    )), future.join())
  }

  test("extract Key.[@*elem]") {
    val expression = "Book.[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5)
    ))), future.join())
  }

  test("extract *.[@elem]") {
    val expression = "*.[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq(Map("Title" -> "Java", "Price" -> 15.5)),
      Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35))
    )), future.join())
  }

  test("extract *.[@*elem]") {
    val expression = "*.[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq(Map("Title" -> "Java", "Price" -> 15.5)),
      Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35))
    )), future.join())
  }

  test("extract Key.Key.Key") {
    val expression = "Store.Book.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java",
      "Scala"
    )), future.join())
  }

  test("extract Key.Key.Key..first") {
    val expression = "Store.Book.Title..first"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java"
    )), future.join())
  }

  test("extract Key.Key..last") {
    val expression = "Store.Book..last"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Map("Title" -> "Scala", "Pri" -> 21.5)
    )), future.join())
  }

  test("extract Key.*halfKey.[@*elem]") {
    val expression = "Store.*ok.[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5)
    ))), future.join())
  }

  test("extract Key.*halfKey.[@*elem].Key") {
    val expression = "Store.*ok.[@*ce].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Java")
    )), future.join())
  }

  test("extract Key.*halfKey.[@*elem].K*y") {
    val expression = "Store.*ok.[@*ce].Ti*le"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Java")
    )), future.join())
  }

  test("extract Key.Key.[#].Key") {
    val expression = "Store.Book.[1].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Scala")
    )), future.join())
  }

}
