package io.zink.boson

import java.nio.ByteBuffer
import bsonLib.{BsonArray, BsonObject}
import io.netty.util.ResourceLeakDetector
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Tiago Filipe on 25/10/2017.
  */

@RunWith(classOf[JUnitRunner])
class HorribleTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val br4: BsonArray = new BsonArray().add("Insecticida")
  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
  val obj11: BsonObject = new BsonObject().put("José", br1)
  val br2: BsonArray = new BsonArray().add("Spider")
  val obj2: BsonObject = new BsonObject().put("José", br2)
  val br3: BsonArray = new BsonArray().add("Fly")
  val obj3: BsonObject = new BsonObject().put("José", br3)
  val arr11: BsonArray = new BsonArray().add(obj11).add(obj2).add(obj3).add(br4)
  val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr11)

  val obj1: BsonObject = new BsonObject()
  val arr1: BsonArray = new BsonArray()

  val bsonEvent: BsonObject = new BsonObject().put("tempReadings", "Zero")
  val arrEvent: BsonArray = new BsonArray().add(bsonEvent)
  val arr: BsonArray = new BsonArray().add(1).add(2)

  test("Empty ObjEvent") {
    val expression: String = "tempReadings"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    val res = boson.go(obj1.encodeToBarray())
    Await.result(res, Duration.Inf)
  }

  test("Empty ArrEvent") {
    val expression: String = "tempReadings"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Bad parser expression V1") {
    val expression: String = "tempReadings.Something Wrong [2 to 3]"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    intercept[Exception] {
      val res = boson.go(arr1.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("Bad parser expression V2") {
    val expression: String = "[0 to 1] kunhnfvgklhu "
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    intercept[Exception] {
      val res = boson.go(arr.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("Bad parser expression V3") {
    val expression: String = ""
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    intercept[Exception] {
      val res = boson.go(arr.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("Bad parser expression V4") {
    val expression: String = "[1 xx 2]"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    intercept[Exception] {
      val res = boson.go(arr.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("Bad parser expression V5") {
    val expression: String = "first ?= 4.0 "
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    intercept[Exception] {
      val res = boson.go(arr.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("IndexOutOfBounds") {
    val expression: String = "[2 to 3]"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Int]) => {
      val expected: Seq[Int] = Seq()
      expected.zip(out).forall(e => e._1.equals(e._2))
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract array when doesn't exists V1") {
    val expression: String = "tempReadings[2 until 3]"
    val boson: Boson = Boson.extractor(expression, (out: Seq[String]) => {
      val expected: Seq[String] = Seq()
      expected.zip(out).forall(e => e._1.equals(e._2))
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract array when doesn't exists V2") {
    val expression: String = "[2 until 3]"
    val boson: Boson = Boson.extractor(expression, (out: Seq[String]) => {
      val expected: Seq[String] = Seq()
      expected.zip(out).forall(e => e._1.equals(e._2))
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract value with wrong Key") {
    val expression: String = "tempReadingS"
    val boson: Boson = Boson.extractor(expression, (out: Seq[String]) => {
      val expected: Seq[String] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract value when only exists BsonArray") {
    val expression: String = "tempReadingS"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      val expected: Seq[Any] = Seq()
      expected.zip(out).forall(e => e._1.equals(e._2))
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Only WhiteSpaces in Expression") {
    val expression: String = "  "
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      val expected: Seq[Array[Byte]] = Seq()
      expected.zip(out).forall(e => e._1.sameElements(e._2))
    })
    intercept[Exception]{
      val res = boson.go(bsonEvent.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("array prob 1") {
    val expression: String = "José[0    to   end]"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      val expected: Seq[Any] = Vector("Tarantula", "Aracnídius", br4.encodeToBarray(), "Spider", "Fly")
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
        case (e, r) => e.equals(r)
      })
    })
    val res = boson.go(bsonEvent1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("array prob 2") {
    val expression: String = "[0    to   end]"
    val expected: Seq[Any] =
      Seq(
        arr11.getBsonObject(0).encodeToBarray(),
        "Tarantula",
        "Aracnídius",
        br4.encodeToBarray(),
        "Insecticida",
        arr11.getBsonObject(1).encodeToBarray(),
        "Spider",
        arr11.getBsonObject(2).encodeToBarray(),
        "Fly",
        br4.encodeToBarray(),
        "Insecticida")
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
        case (e, r) => e.equals(r)
      })
    })
    val res = boson.go(arr11.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("array prob 5") {
    br3.add("Wrong")
    br2.add("some")
    val expression: String = "José[0 until end]"
    val expected: Seq[String] = Seq(
      "Tarantula",
      "Aracnídius",
      "Spider",
      "Fly"
    )
    val boson: Boson = Boson.extractor(expression, (out: Seq[String]) => {
      assertTrue(expected.zip(out).forall {
        case (e, r) => e.equals(r)
      })
    })
    val res = boson.go(a2.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  val b1: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
  val b2: BsonObject = new BsonObject().put("one", 1.1).put("two", 2.2).put("three", 3.3)
  val b3: BsonObject = new BsonObject().put("one", 1f).put("two", 2f).put("three", 3f)
  val a1: BsonArray = new BsonArray().add(b1).add(0.1).add(b2).add(0.2).add(b3).add(0.3)
  val b4: BsonObject = new BsonObject().put("stuff", true).put("arr", a1).put("things", false)
  val b5: BsonObject = new BsonObject().put("stuff", false).put("arr", a1).put("things", true)
  val a2: BsonArray = new BsonArray().add(b4).add(b4).add(b5)

  test("key doesn't match with an array") {
    val expression: String = "things[2]"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assertTrue(expected.zip(out).forall {
        case (e, r) => e.equals(r)
      })
    })
    val res = boson.go(a2.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("1stKey exists many times") {
    val expression: String = "arr[2]"
    val expected: Seq[Array[Byte]] = Vector(a1.getBsonObject(2).encodeToBarray(), a1.getBsonObject(2).encodeToBarray(), a1.getBsonObject(2).encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.sameElements(b._2)))
    })
    val res = boson.go(a2.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("1stKey exists many times, looking for another key") {
    val expression: String = "arr[2].two"
    val expected: Seq[Double] = Seq(2.2,2.2,2.2)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Double]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(a2.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("1stKey matches, 2ndKey doen't exists") {
    val expression: String = "[2].zombie"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(a2.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("key doesn't match with an array with 2ndKey") {
    val expression: String = "things[2].one"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(a2.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("until end Problem") {
    val array: BsonArray = new BsonArray().add(1.1).add(2.2).add(3.3).add(4.4)
    val expression: String = "[1 until end]"
    val expected = Seq(2.2,3.3)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Double]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(array.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("try extract obj with certain elem, but isn't obj") {
    val expression: String = "one[@smth]"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(a1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("try extract obj with certain elem, but elem emptyt") {
    val expression: String = "one.[@]"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    intercept[Exception]{
      val res = boson.go(a1.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("try extract obj with certain elem, but elem is deep") {
    val arr111: BsonArray = new BsonArray().add(obj11).add(obj2).add(obj3).add(br4)
      .add(new BsonObject().put("smth", new BsonArray().add(new BsonObject().put("extract", false))))
    val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr111)
    val expression: String = "StartUp[@extract]"
    val expected: Seq[Any] = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(bsonEvent1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Key doesn't match with keys from root Obj") {
    val obj1: BsonObject = new BsonObject().put("Hat", false).put("Clothe", true)
    val bsonEvent: BsonObject = new BsonObject().put("Store", obj1).put("Quantity", 500L)

    val expression: String = ".Hat"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("KeyWithArr doesn't match with keys from root Obj") {
    val obj1: BsonObject = new BsonObject().put("Hat", false).put("Clothe", true)
    val bsonEvent: BsonObject = new BsonObject().put("Store", obj1).put("Quantity", 500L)

    val expression: String = ".Hat[0]"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Key match inside and outside obj") {
    val obj1: BsonObject = new BsonObject().put("Hat", false).put("Clothe", true).put("Quantity", 10.2f)
    val bsonEvent: BsonObject = new BsonObject().put("Store", obj1).put("Quantity", 500L)

    val expression: String = "..Quantity"
    val expected: Seq[Any] = Seq(10.2f,500L)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1==b._2))
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test(".*..key, root Bsonarray and key doesn't match") {
    val arr: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2).add(new BsonObject().put("Quantity", 500L))
    val expression: String = ".*..Nothing"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test(".key[#]..key2, key2 doesn't exists") {
    val arr1: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2).add(new BsonObject().put("Quantity", 500L))
    val bE: BsonObject = new BsonObject().put("Store", arr1)
    val expression: String = ".Store[7 to end]..Nothing"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Bad Expression using new API") {
    val expression = "...Store"
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {

    })
    intercept[Exception]{
      val res = boson.go(bsonEvent1.encode.getBytes)
      Await.result(res, Duration.Inf)
    }
  }

  test("Empty buf") {
    val buf: ByteBuffer = ByteBuffer.allocate(0)
    val expression = "..Store"
    val expected = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall(b => b._1.equals(b._2)))
    })
    intercept[Exception]{
      val res = boson.go(buf)
      Await.result(res, Duration.Inf)
    }
  }

}
