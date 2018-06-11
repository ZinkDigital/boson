package io.zink.boson

import bsonLib.{BsonArray, BsonObject}
import io.netty.util.ResourceLeakDetector
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Tiago Filipe on 18/10/2017.
  */

@RunWith(classOf[JUnitRunner])
class LanguageTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val arr5: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 12))
  val arr4: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 18))
  val arr3: BsonArray = new BsonArray().add(new BsonObject().put("doorOpen", arr5) /*.put("fridgeTemp", 20)*/)
  val arr2: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 15))
  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", arr2)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("thing", arr3)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", arr4)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).addNull().add(100L).add(2.3f).add(false).add(24)

  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  test("All") {
    val expression = "fridgeTemp"
    val result: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Any) => result += in)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(Seq(5.2f, 15, 5.0, 12, 3.854f, 18).zip(result).forall(e => e._1 == e._2))
  }

  test("[# .. end]") {
    val expression: String = "fridgeReadings[1 until end]"
    val expected: Seq[Any] = Seq(obj2.encodeToBarray(), obj3.encodeToBarray(), "Null", 100L, 2.3f, false)
    val result: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Any) => result += out)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r: Double) => e == r
      case (e, r) if e == null && r == null => true
      case (e, r) => e.equals(r)
    })
  }

  test("[# to #](all Pos)") {
    val expression: String = "fridgeReadings[0 to 7]"
    val expected: Seq[Any] = Seq(obj1.encodeToBarray(), obj2.encodeToBarray(), obj3.encodeToBarray(), "Null", 100L, 2.3f, false, 24)
    val result: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Any) => result += out)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r: Double) => e == r
      case (e, r) if e == null && r == null => true
      case (e, r) => e.equals(r)
    })
  }

  test("all Pos without limits") {
    val expression: String = "fridgeReadings"
    val expected: Seq[Array[Byte]] = Seq(arr.encodeToBarray())
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  test("last Pos with limits") {
    val expression: String = "fridgeReadings[7 to end]"
    val expected: Seq[Int] = Seq(24)
    val result: ArrayBuffer[Int] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Int) => result += out)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e, r) => e == r
    })
  }

  val arr1: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2)
    .add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three"))
  val bE: BsonObject = new BsonObject().put("Store", arr1)

  test(".key[#]..key2") {
    val expression: String = ".Store[7]..Quantity"
    val expected: Seq[Long] = Seq(500L)
    val result: ArrayBuffer[Long] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Long) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e, r) => e == r
    })
  }

  test("..key[@elem], matches the elem") {
    val expression: String = "..Store[@SomeObj]"
    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray())
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  test("..key[@elem], doesn't match the elem") {
    val expression: String = "..Store[@Nothing]"
    val expected: Seq[Array[Byte]] = Seq()
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(result.isEmpty)
  }

  test("..key[@elem], elem match with bool") {
    val expression: String = "..Store[@one]"
    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray())
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  test("..key[@elem], elem match with Long") {
    val expression: String = "..Store[@Quantity]"
    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray())
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  test("..key[@elem], elem match with Null") {
    val expression: String = "..Store[@three]"

    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray())
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }


  test(".[#to#].[#].[#] No Output") {
    val expression: String = ".[5 to 7].[1].[0]"
    val expected: Seq[Array[Byte]] = Seq()
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(result.isEmpty)
  }

  test(".[#to#].[#].[#]") {
    val expression: String = ".[5 to 7].[0]"
    val expected: Seq[String] = Seq("Null")
    val result: ArrayBuffer[String] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: String) => result += out)
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e, r) => e.equals(r)
    })
  }

  test(".[#toend].[#].[#]") {
    val expression: String = ".[5 to end].[1].[0]"
    val expected: Seq[Array[Byte]] = Seq()
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(result.isEmpty)
  }

  test(".key..key2, key matches with array") {
    val expression: String = ".Store..Quantity"
    val expected: Seq[Long] = Seq(500L)
    val result: ArrayBuffer[Long] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Long) => result += out)
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e, r) => e == r
    })
  }

  test("[# to #]") {
    val expression: String = "fridgeReadings[1 to 1]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray)
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  test("[# until #]") {
    val expression: String = "fridgeReadings[1 until 2]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray)
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  val obj4: BsonObject = new BsonObject().put("fridgeTemp", 5.336f).put("fanVelocity", 40.2).put("doorOpen", true)
  val arrEvent: BsonArray = new BsonArray().add(arr).add(obj4).add("Temperature").add(2.5)


  test("[# .. end] w/key") {
    val expression: String = "[1 until end]"
    val expected: Seq[Any] = Seq(obj2.encodeToBarray(), obj3.encodeToBarray(), "Null", 100L, 2.3f, false, obj4.encodeToBarray(), "Temperature")
    val result: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Any) => result += out)
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e, r: Double) => e == r
      case (e, r) if e == null && r == null => true
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("[# to #] w/key") {
    val expression: String = "[1 to 1]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray, obj4.encodeToBarray)
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }

  test("[# until #] w/key") {
    val expression: String = "[1 until 2]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray, obj4.encodeToBarray)
    val result: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => result += out)
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
    })
  }
}
