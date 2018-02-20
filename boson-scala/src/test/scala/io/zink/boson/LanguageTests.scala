package io.zink.boson

import java.nio.{Buffer, ByteBuffer}

import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import bsonLib.{BsonArray, BsonObject}
import io.zink.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.zink.boson.bson.bsonValue._
import io.zink.boson.bson.bsonValue
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.{ByteProcessor, ResourceLeakDetector}
import org.junit.Assert.{assertEquals,assertTrue}

import scala.util.{Failure, Success, Try}

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

  def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) =>
            bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
                 bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e:RuntimeException =>
        bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("All") {
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, "fridgeTemp")

    assert(BsSeq(Vector(5.2f, 15, 5.0, 12, 3.854f, 18)) === resultParser)
  }

  test("[# .. end]") {
    val expression: String = "fridgeReadings[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Any] = Vector(obj2.encodeToBarray(),obj3.encodeToBarray(),"Null",100L,2.3f,false)
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }

  test("[# to #](all Pos)") {
    val expression: String = "fridgeReadings[0 to 7]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    val expected: Vector[Any] = Vector(obj1.encodeToBarray(),obj2.encodeToBarray(),obj3.encodeToBarray(),"Null",100L,2.3f,false,24)
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }

  test("all Pos without limits") {
    val expression: String = "fridgeReadings"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Array[Byte]] = Vector(arr.encodeToBarray())
    val result = resultParser.getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("last Pos with limits") {
    val expression: String = "fridgeReadings[7 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    val expected: Vector[Any] = Vector(24)
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }

  val arr1: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2)
    .add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three"))
  val bE: BsonObject = new BsonObject().put("Store",arr1)

  test(".key[#]..key2") {
    val expression: String = ".Store[7]..Quantity"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(500L)), result)
  }

  test("..key[@elem], matches the elem") {
    val expression: String = "..Store[@SomeObj]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    val expected: Vector[Any] = Vector(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val res = result.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }
  test("..key[@elem], doesn't match the elem") {
    val expression: String = "..Store[@Nothing]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }
  test("..key[@elem], elem match with bool") {
    val expression: String = "..Store[@one]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    val expected: Vector[Any] = Vector(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val res = result.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }
  test("..key[@elem], elem match with Long") {
    val expression: String = "..Store[@Quantity]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    val expected: Vector[Any] = Vector(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val res = result.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }
  test("..key[@elem], elem match with Null") {
    val expression: String = "..Store[@three]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    val expected: Vector[Any] = Vector(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray())
    val res = result.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e,r) => e.equals(r)
    })
  }


  test(".[#to#].[#].[#] No Output") {
    val expression: String = ".[5 to 7].[1].[0]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test(".[#to#].[#].[#]") {
    val expression: String = ".[5 to 7].[0]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector("Null")), result)
  }

  test(".[#toend].[#].[#]") {
    val expression: String = ".[5 to end].[1].[0]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test(".key..key2, key matches with array") {
    val expression: String = ".Store..Quantity"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(500L)), result)
  }

  test("[# to #]") {
    val expression: String = "fridgeReadings[1 to 1]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Any] = Vector(obj2.encodeToBarray())
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("[# until #]") {
    val expression: String = "fridgeReadings[1 until 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Any] = Vector(obj2.encodeToBarray())
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  val obj4: BsonObject = new BsonObject().put("fridgeTemp", 5.336f).put("fanVelocity", 40.2).put("doorOpen", true)
  val arrEvent: BsonArray = new BsonArray().add(arr).add(obj4).add("Temperature").add(2.5)


  test("[# .. end] w/key") {
    val expression: String = "[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Any] = Vector(obj4.encodeToBarray(), "Temperature", obj2.encodeToBarray(), obj3.encodeToBarray(), "Null", 100L, 2.3f, false)
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("[# to #] w/key") {
    val expression: String = "[1 to 1]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Any] = Vector(obj4.encodeToBarray(), obj2.encodeToBarray())
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("[# until #] w/key") {
    val expression: String = "[1 until 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)

    val expected: Vector[Any] = Vector(obj4.encodeToBarray(), obj2.encodeToBarray())
    val res = resultParser.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }



}
