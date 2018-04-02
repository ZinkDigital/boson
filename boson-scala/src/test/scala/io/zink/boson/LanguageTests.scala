package io.zink.boson

import bsonLib.{BsonArray, BsonObject}
import io.netty.util.ResourceLeakDetector
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Tiago Filipe on 18/10/2017.
  */

@RunWith(classOf[JUnitRunner])
class LanguageTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val arr5: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp",12))
  val arr4: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 18))
  val arr3: BsonArray = new BsonArray().add(new BsonObject().put("doorOpen",arr5)/*.put("fridgeTemp", 20)*/)
  val arr2: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 15))
  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", arr2)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("thing", arr3)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", arr4)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).addNull().add(100L).add(2.3f).add(false).add(24)

  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  test("All") {
    val expression = "fridgeTemp"
    val boson: Boson = Boson.extractor(expression, (in: Seq[Any]) => {
      assertTrue(Seq(5.2f, 15, 5.0, 12, 3.854f, 18).zip(in).forall(e=>e._1 == e._2))
      println("APPLIED")
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("[# .. end]") {
    val expression: String = "fridgeReadings[1 until end]"
    val expected: Seq[Any] = Seq(obj2.encodeToBarray(),obj3.encodeToBarray(),"Null",100L,2.3f,false)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall{
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
        case (e,r: Double) => e == r
        case (e,r) if e == null && r == null => true
        case (e,r) => e.equals(r)
      })
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("[# to #](all Pos)") {
    val expression: String = "fridgeReadings[0 to 7]"
    val expected: Seq[Any] = Seq(obj1.encodeToBarray(),obj2.encodeToBarray(),obj3.encodeToBarray(),"Null",100L,2.3f,false,24)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall{
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
        case (e,r: Double) => e == r
        case (e,r) if e == null && r == null => true
        case (e,r) => e.equals(r)
      })
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("all Pos without limits") {
    val expression: String = "fridgeReadings"
    val expected: Seq[Array[Byte]] = Seq(arr.encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall{
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("last Pos with limits") {
    val expression: String = "fridgeReadings[7 to end]"
    val expected: Seq[Int] = Seq(24)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Int]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall{
        case (e,r) => e == r
      })
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  val arr1: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2)
    .add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three"))
  val bE: BsonObject = new BsonObject().put("Store",arr1)

  test(".key[#]..key2") {
    val expression: String = ".Store[7]..Quantity"
    val expected: Seq[Long] = Seq(500L)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Long]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e, r) => e == r
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("..key[@elem], matches the elem") {
    val expression: String = "..Store[@SomeObj]"
    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("..key[@elem], doesn't match the elem") {
    val expression: String = "..Store[@Nothing]"
    val expected: Seq[Array[Byte]] = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("..key[@elem], elem match with bool") {
    val expression: String = "..Store[@one]"
    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("..key[@elem], elem match with Long") {
    val expression: String = "..Store[@Quantity]"
    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("..key[@elem], elem match with Null") {
    val expression: String = "..Store[@three]"

    val expected: Seq[Array[Byte]] = Seq(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }


  test(".[#to#].[#].[#] No Output") {
    val expression: String = ".[5 to 7].[1].[0]"
    val expected: Seq[Array[Byte]] = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test(".[#to#].[#].[#]") {
    val expression: String = ".[5 to 7].[0]"
    val expected: Seq[String] = Seq("Null")
    val boson: Boson = Boson.extractor(expression, (out: Seq[String]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e ,r) => e.equals(r)
      })
    })
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test(".[#toend].[#].[#]") {
    val expression: String = ".[5 to end].[1].[0]"
    val expected: Seq[Array[Byte]] = Seq()
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(arr1.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test(".key..key2, key matches with array") {
    val expression: String = ".Store..Quantity"
    val expected: Seq[Long] = Seq(500L)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Long]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e,r) => e == r
      })
    })
    val res = boson.go(bE.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("[# to #]") {
    val expression: String = "fridgeReadings[1 to 1]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("[# until #]") {
    val expression: String = "fridgeReadings[1 until 2]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  val obj4: BsonObject = new BsonObject().put("fridgeTemp", 5.336f).put("fanVelocity", 40.2).put("doorOpen", true)
  val arrEvent: BsonArray = new BsonArray().add(arr).add(obj4).add("Temperature").add(2.5)


  test("[# .. end] w/key") {
    val expression: String = "[1 until end]"
    val expected: Seq[Any] = Seq(obj2.encodeToBarray(),obj3.encodeToBarray(), "Null", 100L, 2.3f, false,obj4.encodeToBarray(), "Temperature")
    val boson: Boson = Boson.extractor(expression, (out: Seq[Any]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e,r: Double) => e == r
        case (e,r) if e == null && r == null => true
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
        case (e,r) => e.equals(r)
      })
    })
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("[# to #] w/key") {
    val expression: String = "[1 to 1]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray,obj4.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("[# until #] w/key") {
    val expression: String = "[1 until 2]"
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray,obj4.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (out: Seq[Array[Byte]]) => {
      assert(expected.size === out.size)
      assertTrue(expected.zip(out).forall {
        case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      })
    })
    val res = boson.go(arrEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }



}
