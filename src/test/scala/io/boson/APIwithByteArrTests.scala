package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class APIwithByteArrTests extends FunSuite {

  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)

  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  val validatedByteArray: Array[Byte] = arr.encodeToBarray()
  val validatedByteArrayObj: Array[Byte] = bsonEvent.encodeToBarray()

  test("extract PosV1 w/ key") {
    val expression: String = "[1 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    )), future.join())
  }

  test("extract PosV2 w/ key") {
    val expression: String = "[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    )), future.join())
  }

  test("extract PosV3 w/ key") {
    val expression: String = "[1 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false)
    )), future.join())
  }

  test("extract PosV4 w/ key") {
    val expression: String = "[2 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    )), future.join())
  }

  test("extract PosV5 w/ key") {
    val expression: String = "[2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    )), future.join())
  }

  test("extract with 2nd Key PosV1 w/ key") {
    val expression: String = "[1 to 1].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      20.6
    )), future.join())
  }

  test("extract with 2nd Key PosV2 w/ key") {
    val expression: String = "[1 until 3].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      20.6, 20.5
    )), future.join())
  }

  test("extract with 2nd Key PosV3 w/ key") {
    val expression: String = "[0 until end].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      20.5, 20.6
    )), future.join())
  }

  test("extract with 2nd Key PosV4 w/ key") {
    val expression: String = "[2 to end].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      3.854f
    )), future.join())
  }

  test("extract with 2nd Key PosV5 w/ key") {
    val expression: String = "[2].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Vector(
      3.854f
    )), future.join())
  }

  test("extract PosV1") {
    val expression: String = "fridgeReadings[1 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }

  test("extract PosV2") {
    val expression: String = "fridgeReadings[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }

  test("extract PosV3") {
    val expression: String = "fridgeReadings[1 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false)
    ))), future.join())
  }

  test("extract PosV4") {
    val expression: String = "fridgeReadings[2 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }

  test("extract PosV5") {
    val expression: String = "fridgeReadings[1]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false)
    ))), future.join())
  }

  test("extract with 2nd Key PosV1") {
    val expression: String = "fridgeReadings[1 to 1].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(
      20.6
    )), future.join())
  }

  test("extract with 2nd Key PosV2") {
    val expression: String = "fridgeReadings[1 until 3].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(
      20.6, 20.5
    )), future.join())
  }

  test("extract with 2nd Key PosV3") {
    val expression: String = "fridgeReadings[0 until end].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(
      20.5, 20.6
    )), future.join())
  }

  test("extract with 2nd Key PosV4") {
    val expression: String = "fridgeReadings[2 to end].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(
      3.854f
    )), future.join())
  }

  test("extract with 2nd Key PosV5") {
    val expression: String = "fridgeReadings[2].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Vector(
      3.854f
    )), future.join())
  }

  test("extract all elements containing partial key") {
    val expression: String = "*city"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Vector(20.5,20.6,20.5)),
      future.join())
  }

  test("extract everything") {
    val expression: String = "*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Vector(
        Map("fridgeTemp" -> 5.199999809265137, "fanVelocity" -> 20.5, "doorOpen" -> false),
        Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false),
        Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true)
      )),
      future.join())
  }

  test("extract all elements of a key") {
    val expression: String = "fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Vector(
        20.5,
        20.6,
        20.5
      )),
      future.join())
  }

  test("extract objects with a certain element") {
    val obj4: BsonObject = new BsonObject().put("something", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
    val obj5: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
    val obj6: BsonObject = new BsonObject().put("fridgeTemp11", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)
    val arr2: BsonArray = new BsonArray().add(obj4).add(obj5).add(obj6)
    val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val validatedByteArrayObj1: Array[Byte] = bsonEvent.encodeToBarray()

    val expression: String = "fridgeReadings[@fridgeTemp]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj1)
    assertEquals(
      BsSeq(Vector(
        Map("fridgeTemp" -> 5.0, "fanVelocity" -> 20.6, "doorOpen" -> false)
      )),
      future.join())
  }

  test("extract inside loop w/ key") {
    val expression: String = "[2 to end]"
    val latch: CountDownLatch = new CountDownLatch(5)

     val boson: Boson = Boson.extractor(expression, (in: BsValue) => {
      //println(s"result of extraction -> ${in.getValue}")
      assertEquals(
        Vector(Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true)),
        in.getValue
      )
      latch.countDown()
    })

    for(range <- 0 until 5) {
      arr.add(range)
      boson.go(arr.encodeToBarray())
    }
  }

}
