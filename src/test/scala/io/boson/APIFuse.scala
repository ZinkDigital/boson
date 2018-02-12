package io.boson

import java.util.concurrent.{CompletableFuture, TimeUnit}
import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.BsValue
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertArrayEquals

@RunWith(classOf[JUnitRunner])
class APIFuse extends FunSuite{

  val obj1: BsonObject = new BsonObject().put("fridgeTemp111", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)
  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  val validatedByteArray: Array[Byte] = bsonEvent.encodeToBarray()


  test("Sequential fuse Extractor->Injector->Extractor") {
    val expression1: String = "fridgeTemp"
    val ext: Boson = Boson.extractor(expression1, (in: BsValue) => {
      println(s"----------------------------------- result of extraction: ${in.getValue.asInstanceOf[Seq[Any]]} ---------------------------")
      assert(Vector(5.0f,3.854f) === in.getValue || Vector(18.3f,18.3f) === in.getValue)
    })

    val newFridgeSerialCode: Float = 18.3f
    val expression2 = "fridgeTemp"
    val inj: Boson = Boson.injector(expression2, (_: Float) => newFridgeSerialCode)

    val fused: Boson = ext.fuse(inj)
    val future: CompletableFuture[Array[Byte]] = fused.go(validatedByteArray)

    val fused2: Boson = fused.fuse(ext)
    val future2: CompletableFuture[Array[Byte]] = fused2.go(future.join())

    //latch.await()
    assertArrayEquals(future.join(), future2.get(1, TimeUnit.SECONDS))
  }

}
