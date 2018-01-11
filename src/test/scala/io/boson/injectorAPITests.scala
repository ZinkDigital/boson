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

  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)
  val validatedByteArray: Array[Byte] = arr.encodeToBarray()
  val validatedByteArrayObj: Array[Byte] = bsonEvent.encodeToBarray()

  test("Inject API Double => Double") {
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
    val newFridgeSerialCode: Double = 1000.0
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "fanVelocity..first"
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
    val expression = "string..first"
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
    val expression = "bson..first"
    val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => in.+(("WHAT!!!", 10)))
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10))),future.join() )
  }
  test("Inject API List => List") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: String = "MAIS EU"
    val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray
    val expression = "bson..first"
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

  test("key.[@key1]"){
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bAux1: BsonObject = new BsonObject().put("creep", "DAMMN")
   //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    val expression = "array.[@damnnn]"
    val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => in.+(("WHAT!!!", 10)))

    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)
    val finalResult: BsValue = future.join()
    println(finalResult)
    assertEquals(BsSeq(List(List(Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10), Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10), Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10)))),finalResult )
  }

  test("*"){
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bAux1: BsonObject = new BsonObject().put("creep", "DAMMN")
    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    val expression = "*"
    // val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => in.+(("WHAT!!!", 10)))
    val boson: Boson = Boson.injector(expression, (in: List[Any]) => in.:+(Mapper.convertBsonObject(new BsonObject().put("WHAT!!!", 10))))
    val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: Array[Byte] = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)
    val finalResult: BsValue = future.join()
    println(finalResult)
    assertEquals(BsSeq(List(Map("array" -> List(Map("damnnn" -> "DAMMN"), Map("damnnn" -> "DAMMN"), Map("damnnn" -> "DAMMN"), Map("creep" -> "DAMMN"), Map("WHAT!!!" -> 10))))),finalResult )
  }
}
