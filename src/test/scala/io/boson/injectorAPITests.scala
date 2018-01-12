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

  //"Store.Hat.[@ric].Title..last"
  //val expression = "Store.Hat.*ric.*..last"

//  test("MoreKeys"){
//    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
//    val bAux1: BsonObject = new BsonObject().put("creep", "DAMMN")
//    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
//    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)
//
//    //val newFridgeSerialCode: String = " what?"
//    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
//    val expression = "[2].José.[@elem].[1]..all" //"a*rray.dam*nnn"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
//    val boson: Boson = Boson.injector(expression, (in: List[Any]) => in.:+(Mapper.convertBsonObject(new BsonObject().put("WHAT!!!", 10))))
//    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
//
//    val result: Array[Byte] = midResult.join()
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

}
