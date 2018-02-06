package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue.{BsException, BsSeq, BsValue}
import io.boson.bson.{Boson, bsonValue}
import io.netty.util.ResourceLeakDetector
import org.junit.Assert.{assertEquals,assertTrue}

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

  def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          val interpreter = new Interpreter(boson, r.asInstanceOf[Program])
          interpreter.run()
        case parser.Error(_, _) => bsonValue.BsObject.toBson("Error parsing!")
        case parser.Failure(_, _) => bsonValue.BsObject.toBson("Failure parsing!")
      }
    } catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("Empty ObjEvent") {
    val expression: String = "tempReadings"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(obj1.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Empty ArrEvent") {
    val expression: String = "tempReadings"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Empty ByteBuf") {
    val expression: String = "tempReadings"
    val boson: BosonImpl = new BosonImpl()
    val resultParser: BsValue = callParse(boson, expression)
    assert(Vector() === BsSeq.unapply(resultParser.asInstanceOf[BsSeq]).get)
  }

  test("Bad parser expression V1") {
    val expression: String = "tempReadings.Something Wrong [2 to 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert("Failure parsing!" === BsException.unapply(result.asInstanceOf[BsException]).get)
  }

  test("Bad parser expression V2") {
    val expression: String = "[0 to 1] kunhnfvgklhu "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException.apply("Failure parsing!") === result)
  }

  test("Bad parser expression V3") {
    val expression: String = ""
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V4") {
    val expression: String = "[1 xx 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("Bad parser expression V5") {
    val expression: String = "first ?= 4.0 "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("IndexOutOfBounds") {
    val expression: String = "[2 to 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq.apply(Vector()) === resultParser)
  }

  test("Extract array when doesn't exists V1") {
    val expression: String = "tempReadings[2 until 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract array when doesn't exists V2") {
    val expression: String = "[2 until 3]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract value with wrong Key") {
    val expression: String = "tempReadingS"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arrEvent.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Extract value when only exists BsonArray") {
    val expression: String = "tempReadingS"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val resultParser: BsValue = callParse(boson, expression)
    assert(BsSeq(Vector()) === resultParser)
  }

  test("Only WhiteSpaces in Expression") {
    val expression: String = "  "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assert(BsException("Failure parsing!") === result)
  }

  test("array prob 1") {
    val expression: String = "   José[     0    to   end      ]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      "Tarantula",
      "Aracnídius",
      Seq("Insecticida"),
      "Spider",
      "Fly"
    )), result)
  }

  test("array prob 2") {
    val expression: String = "[     0    to   end      ]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr11.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    val expected: Vector[Any] =
      Vector(
        arr11.getBsonObject(0).encodeToBarray(),
        arr11.getBsonObject(1).encodeToBarray(),
        arr11.getBsonObject(2).encodeToBarray(),
        Seq("Insecticida"),
        "Tarantula",
        "Aracnídius",
        Seq("Insecticida"),
        "Insecticida",
        "Spider",
        "Fly",
        "Insecticida")
    val res = result.getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("array prob 5") {
    br3.add("Wrong")
    br2.add("some")
    val expression: String = "José[0 until end]  "
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      "Tarantula",
      "Aracnídius",
      "Spider",
      "Fly"
    )), result)
  }

  val b1: BsonObject = new BsonObject().put("one",1).put("two",2).put("three",3)
  val b2: BsonObject = new BsonObject().put("one",1.1).put("two",2.2).put("three",3.3)
  val b3: BsonObject = new BsonObject().put("one",1f).put("two",2f).put("three",3f)
  val a1: BsonArray = new BsonArray().add(b1).add(0.1).add(b2).add(0.2).add(b3).add(0.3)
  val b4: BsonObject = new BsonObject().put("stuff",true).put("arr", a1).put("things",false)
  val b5: BsonObject = new BsonObject().put("stuff",false).put("arr", a1).put("things",true)
  val a2: BsonArray = new BsonArray().add(b4).add(b4).add(b5)

  test("key doesn't match with an array") {
    val expression: String = "things[2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("1stKey exists many times") {
    val expression: String = "arr[2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    val expected: Vector[Array[Byte]] = Vector(a1.getBsonObject(2).encodeToBarray(),a1.getBsonObject(2).encodeToBarray(),a1.getBsonObject(2).encodeToBarray())
    val res = result.getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall(b => b._1.sameElements(b._2)))
  }

  test("1stKey exists many times, looking for another key") {
    val expression: String = "arr[2].two"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      2.2,
      2.2,
      2.2
    )), result)
  }

  test("1stKey matches, 2ndKey doen't exists") {
    val expression: String = "[2].zombie"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("key doesn't match with an array with 2ndKey") {
    val expression: String = "things[2].one"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a2.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("until end Problem") {
    val array: BsonArray = new BsonArray().add(1.1).add(2.2).add(3.3).add(4.4)
    val expression: String = "[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(array.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(
      2.2,3.3
    )), result)
  }

  test("try extract obj with certain elem, but isn't obj") {
    val expression: String = "one[@smth]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("try extract obj with certain elem, but elem emptyt") {
    val expression: String = "one.[@]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(a1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsException("Failure parsing!"), result)
  }

  test("try extract obj with certain elem, but elem is deep") {
    val arr111: BsonArray = new BsonArray().add(obj11).add(obj2).add(obj3).add(br4)
      .add(new BsonObject().put("smth",new BsonArray().add(new BsonObject().put("extract", false))))
    val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr111)
    val expression: String = "StartUp[@extract]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }
  test("Key doesn't match with keys from root Obj") {
    val obj1: BsonObject = new BsonObject().put("Hat", false).put("Clothe",true)
    val bsonEvent: BsonObject = new BsonObject().put("Store",obj1).put("Quantity",500L)

    val expression: String = ".Hat"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("KeyWithArr doesn't match with keys from root Obj") {
    val obj1: BsonObject = new BsonObject().put("Hat", false).put("Clothe",true)
    val bsonEvent: BsonObject = new BsonObject().put("Store",obj1).put("Quantity",500L)

    val expression: String = ".Hat[0]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("Key match inside and outside obj") {
    val obj1: BsonObject = new BsonObject().put("Hat", false).put("Clothe",true).put("Quantity",10.2f)
    val bsonEvent: BsonObject = new BsonObject().put("Store",obj1).put("Quantity",500L)

    val expression: String = "..Quantity"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector(10.2f,500L)), result)
  }

  test(".*..key, root Bsonarray and key doesn't match") {
    val arr: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2).add(new BsonObject().put("Quantity",500L))
    val expression: String = ".*..Nothing"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(arr.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test(".key[#]..key2, key2 doesn't exists") {
    val arr1: BsonArray = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull()).add(2).add(new BsonObject().put("Quantity",500L))
    val bE: BsonObject = new BsonObject().put("Store",arr1)
    val expression: String = ".Store[7 to end]..Nothing"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bE.encode().getBytes))
    val result: BsValue = callParse(boson, expression)
    assertEquals(BsSeq(Vector()), result)
  }

  test("Bad Expression using new API") {
    val expression = "...Store"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent1.encodeToBarray())

    assertEquals(
      BsException("Failure parsing!"),
      future.join())
  }

}
