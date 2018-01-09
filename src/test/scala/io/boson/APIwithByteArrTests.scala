package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue._
import mapper.Mapper
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

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }
  test("extract PosV2 w/ key") {
    val expression: String = "[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }
  test("extract PosV3 w/ key") {
    val expression: String = "[1 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false)
    ))), future.join())
  }
  test("extract PosV4 w/ key") {
    val expression: String = "[2 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }
  test("extract PosV5 w/ key") {
    val expression: String = "[2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }

  test("extract with 2nd Key PosV1 w/ key") {
    val expression: String = "[1 to 1].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      20.6
    ))), future.join())
  }
  test("extract with 2nd Key PosV2 w/ key") {
    val expression: String = "[1 until 3].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      20.6, 20.5
    ))), future.join())
  }
  test("extract with 2nd Key PosV3 w/ key") {
    val expression: String = "[0 until end].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      20.5, 20.6
    ))), future.join())
  }
  test("extract with 2nd Key PosV4 w/ key") {
    val expression: String = "[2 to end].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      3.854f
    ))), future.join())
  }
  test("extract with 2nd Key PosV5 w/ key") {
    val expression: String = "[2].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(
      3.854f
    ))), future.join())
  }

  test("extract last w/ key") {
    val expression: String = "last"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Seq(Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true))),
      future.join())
  }
  test("extract first w/ key") {
    val expression: String = "first"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Seq(Map("fridgeTemp" -> 5.2f, "fanVelocity" -> 20.5, "doorOpen" -> false))),
      future.join())
  }
  test("extract all w/ key") {
    val expression: String = "all"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Seq(
        Map("fridgeTemp" -> 5.2f, "fanVelocity" -> 20.5, "doorOpen" -> false),
        Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
        Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5, "doorOpen" -> true))),
      future.join())
  }
  test("extract PosV1") {
    val expression: String = "fridgeReadings.[1 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }
  test("extract PosV2") {
    val expression: String = "fridgeReadings.[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false),
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }
  test("extract PosV3") {
    val expression: String = "fridgeReadings.[1 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false)
    ))), future.join())
  }
  test("extract PosV4") {
    val expression: String = "fridgeReadings.[2 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 3.854f, "fanVelocity" -> 20.5,"doorOpen" -> true)
    ))), future.join())
  }
  test("extract PosV5") {
    val expression: String = "fridgeReadings.[1]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      Map("fridgeTemp" -> 5.0f, "fanVelocity" -> 20.6, "doorOpen" -> false)
    ))), future.join())
  }

  test("extract with 2nd Key PosV1") {
    val expression: String = "fridgeReadings.[1 to 1].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      20.6
    ))), future.join())
  }
  test("extract with 2nd Key PosV2") {
    val expression: String = "fridgeReadings.[1 until 3].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      20.6, 20.5
    ))), future.join())
  }
  test("extract with 2nd Key PosV3") {
    val expression: String = "fridgeReadings.[0 until end].fanVelocity"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      20.5, 20.6
    ))), future.join())
  }
  test("extract with 2nd Key PosV4") {
    val expression: String = "fridgeReadings.[2 to end].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      3.854f
    ))), future.join())
  }
  test("extract with 2nd Key PosV5") {
    val expression: String = "fridgeReadings.[2].fridgeTemp"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    assertEquals(BsSeq(Seq(Seq(
      3.854f
    ))), future.join())
  }

  test("extract last") {
    val expression: String = "doorOpen.last"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Seq(true)),
      future.join())
  }
  test("extract first") {
    val expression: String = "fanVelocity.first"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    assertEquals(
      BsSeq(Seq(20.5)),
      future.join())
  }
  test("extract all") {
    val expression: String = "fanVelocity.all"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
      assertEquals(
      BsSeq(Seq(20.5,20.6,20.5)),
      future.join())
  }

  test("extract inside loop w/ key") {
    val expression: String = "[2 to end]"
    val latch: CountDownLatch = new CountDownLatch(5)

     val boson: Boson = Boson.extractor(expression, (in: BsValue) => {
      //println(s"result of extraction -> ${in.getValue}")
      assertEquals(
        List(Map("fridgeTemp" -> 3.8540000915527344, "fanVelocity" -> 20.5, "doorOpen" -> true)),
        in.getValue
      )
      latch.countDown()
    })

    for(range <- 0 until 5) {
      arr.add(range)
      boson.go(arr.encodeToBarray())
    }
  }
  test("Inject API Double => Double") {
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
    val newFridgeSerialCode: Double = 1000.0
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "fanVelocity.first"
    val boson: Boson = Boson.injector(expression, (in: Double) => newFridgeSerialCode)
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(List(1000.0), future.join().getValue )
  }
  test("Inject API String => String") {
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the")
    val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "string.first"
    val boson: Boson = Boson.injector(expression, (in: String) => in.concat(newFridgeSerialCode))
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(List("the what?").head, new String(future.join().getValue.asInstanceOf[List[Array[Byte]]].head) )
  }
  test("Inject API Map => Map") {
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)

    val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => in.+(("WHAT!!!", 10)))
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10))),future.join() )
  }
  test("Inject API Map => Map 1") {
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bAux1: BsonObject = new BsonObject().put("DAMNNNNN", "damnn")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)

    val newFridgeSerialCode: Map[String, Any] = Mapper.convert(bAux1).asInstanceOf[ Map[String, Any]]
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => newFridgeSerialCode)
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(Map("DAMNNNNN" -> "damnn"))),future.join() )
  }
  test("Inject API List => List") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: String = "MAIS EU"
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: List[Any]) => {
      val s: List[Any] = in.:+(newFridgeSerialCode)
      s})
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(12, "sddd", "MAIS EU")),future.join() )
  }
  test("Inject API List => List 1") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bAux1: BsonArray = new BsonArray().add("sddd").add(12)
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode:List[Any] = Mapper.convertBsonArray(bAux1)
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: List[Any]) => newFridgeSerialCode)
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List("sddd", 12)),future.join() )
  }
  test("Inject API Int => Int") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("int", 10).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: Int = 2
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "int.first"
    val boson: Boson = Boson.injector(expression, (in: Int) => in*newFridgeSerialCode)
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(20)),future.join() )
  }
  test("Inject API Long => Long") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("long", 9L).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: Long = 2
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "long.first"
    val boson: Boson = Boson.injector(expression, (in: Long) => in*newFridgeSerialCode)
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(18)),future.join() )
  }

}
