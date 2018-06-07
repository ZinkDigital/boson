package io.zink.boson

import java.util.concurrent.CountDownLatch

import bsonLib.{BsonArray, BsonObject}
import io.netty.util.ResourceLeakDetector
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class APIwithByteArrTests extends FunSuite with Matchers {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)

  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  val validatedByteArray: Array[Byte] = arr.encodeToBarray()
  val validatedByteArrayObj: Array[Byte] = bsonEvent.encodeToBarray()

  test("extract PosV1 w/ key") {
    val expression: String = "[1 until 3]"
    val expected: List[Array[Byte]] = List(arr.getBsonObject(1).encodeToBarray(), arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal: Boolean = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV2 w/ key") {
    val expression: String = "[1 to 2]"
    val expected: List[Array[Byte]] = List(arr.getBsonObject(1).encodeToBarray(), arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal: Boolean = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV3 w/ key") {
    val expression: String = "[1 until end]"
    val expected: List[Array[Byte]] = List(arr.getBsonObject(1).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV4 w/ key") {
    val expression: String = "[2 to end]"
    val expected: List[Array[Byte]] = List(arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV5 w/ key") {
    val expression: String = "[2]"
    val expected: List[Array[Byte]] = List(arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract with 2nd Key PosV1 w/ key") {
    val expression: String = "[1 to 1].fanVelocity"
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    assert(mutableBuffer.head == 20.6)
  }

  test("extract with 2nd Key PosV2 w/ key") {
    val expression: String = "[1 until 3].fanVelocity"
    val expected: List[Double] = List(20.6, 20.5)
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.equals(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract with 2nd Key PosV3 w/ key") {
    val expression: String = "[0 until end].fanVelocity"
    val expected: List[Double] = List(20.5, 20.6)
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.equals(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract with 2nd Key PosV4 w/ key") {
    val expression: String = "[2 to end].fridgeTemp"
    val mutableBuffer: ArrayBuffer[Float] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Float) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    assert(mutableBuffer.head == 3.854f)
  }

  test("extract with 2nd Key PosV5 w/ key") {
    val expression: String = "[2].fridgeTemp"
    val mutableBuffer: ArrayBuffer[Float] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Float) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    assert(mutableBuffer.head == 3.854f)
  }

  test("extract PosV1") {
    val expression: String = "fridgeReadings[1 until 3]"
    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(1).encodeToBarray(), arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV2") {
    val expression: String = "fridgeReadings[1 to 2]"
    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(1).encodeToBarray(), arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size === mutableBuffer.size)
  }

  test("extract PosV3") {
    val expression: String = "fridgeReadings[1 until end]"
    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(1).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV4") {
    val expression: String = "fridgeReadings[2 to end]"
    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract PosV5") {
    val expression: String = "fridgeReadings[1]"
    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(1).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract with 2nd Key PosV1") {
    val expression: String = "fridgeReadings[1 to 1].fanVelocity"
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    assert(mutableBuffer.head == 20.6)
  }

  test("extract with 2nd Key PosV2") {
    val expression: String = "fridgeReadings[1 until 3].fanVelocity"
    val expected: Seq[Double] = Seq(20.6, 20.5)
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.equals(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract with 2nd Key PosV3") {
    val expression: String = "fridgeReadings[0 until end].fanVelocity"
    val expected: Seq[Double] = Seq(20.5, 20.6)
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.equals(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract with 2nd Key PosV4") {
    val expression: String = "fridgeReadings[2 to end].fridgeTemp"
    val mutableBuffer: ArrayBuffer[Float] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Float) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    assert(mutableBuffer.head == 3.854f)
  }

  test("extract with 2nd Key PosV5") {
    val expression: String = "fridgeReadings[2].fridgeTemp"
    val mutableBuffer: ArrayBuffer[Float] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Float) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj)
    Await.result(result, Duration.Inf)
    assert(mutableBuffer.head == 3.854f)
  }

  test("extract all elements containing partial key") {
    val expression: String = "*city"
    val expected: Seq[Double] = Seq(20.5, 20.6, 20.5)
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equal = expected.zip(mutableBuffer).forall(ex => ex._1.equals(ex._2))
    assert(equal && expected.size == mutableBuffer.size)
  }

  test("extract all elements of root") {
    val expression: String = ".*"
    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(0).encodeToBarray(), arr.getBsonObject(1).encodeToBarray(), arr.getBsonObject(2).encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equals = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equals && expected.size == mutableBuffer.size)
  }

  test("extract all elements of a key") {
    val expression: String = "fanVelocity"
    val expected: Seq[Double] = Seq(20.5, 20.6, 20.5)
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArray)
    Await.result(result, Duration.Inf)
    val equals = expected.zip(mutableBuffer).forall(ex => ex._1.equals(ex._2))
    assert(equals && expected.size == mutableBuffer.size)
  }

  test("extract objects with a certain element") {
    val obj4: BsonObject = new BsonObject().put("something", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
    val obj5: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
    val obj6: BsonObject = new BsonObject().put("fridgeTemp11", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)
    val arr2: BsonArray = new BsonArray().add(obj4).add(obj5).add(obj6)
    val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val validatedByteArrayObj1: Array[Byte] = bsonEvent.encodeToBarray()

    val expression: String = "fridgeReadings[@fridgeTemp]"
    val expected: Seq[Array[Byte]] = Seq(obj5.encodeToBarray())
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
    })
    val result = boson.go(validatedByteArrayObj1)
    Await.result(result, Duration.Inf)
    val equals = expected.zip(mutableBuffer).forall(ex => ex._1.sameElements(ex._2))
    assert(equals && expected.size == mutableBuffer.size)
  }

  test("extract inside loop w/ key") {
    val expression: String = "[2 to end]"
    val latch: CountDownLatch = new CountDownLatch(5)
    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assert( Seq(obj3.encodeToBarray) == in)
      latch.countDown()
    })
    for (range <- 0 until 5) {
      arr.add(range)
      boson.go(arr.encodeToBarray())
    }
  }

}
