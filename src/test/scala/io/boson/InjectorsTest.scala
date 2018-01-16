package io.boson

import java.time.Instant
import java.util

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import io.boson.bson.bsonImpl.injectors.EnumerationTest
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue
import io.netty.util.ByteProcessor

import io.netty.util.ByteProcessor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

/**
  * Created by Ricardo Martins on 09/11/2017.
  */


@RunWith(classOf[JUnitRunner])
class InjectorsTest extends FunSuite {

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

  object enum extends Enumeration {
    val A: enum.Value = Value("AAAA")
    val B: enum.Value = Value("BBBBB")
  }

  val bytearray1: Array[Byte] = "AlguresPorAi".getBytes()
  val bytearray2: Array[Byte] = "4".getBytes()
  val ins: Instant = Instant.now()
  val ins1: Instant = Instant.now().plusMillis(1000)
  val float: Float = 11.toFloat
  val newFloat: Float = 15.toFloat
  val double: Double = 21.toDouble
  val newDouble: Double = 25.toDouble
  val bObj: BsonObject = new BsonObject().put("bsonObj", "ola")
  val newbObj: BsonObject = new BsonObject().put("newbsonObj", "newbsonObj")
  val bool: Boolean = true
  val newBool: Boolean = false
  val long: Long = 100000001.toLong
  val newLong: Long = 200000002.toLong
  val bsonArray: BsonArray = new BsonArray().add(1).add(2).add("Hi")
  val newbsonArray: BsonArray = new BsonArray().add(3).add(4).add("Bye")
  val enumJava: EnumerationTest = io.boson.bson.bsonImpl.injectors.EnumerationTest.A
  val newEnumJava = io.boson.bson.bsonImpl.injectors.EnumerationTest.B
  //val enum = EnumerationTest.A
  //val enum1 = EnumerationTest.B
  val obj: BsonObject = new BsonObject().put("field", 0).put("bool", bool).put("enumScala", enum.A.toString).put("bsonArray", bsonArray).put("long", long).put("bObj", bObj).put("no", "ok").put("float", float).put("double", double).put("array", bytearray2).put("inst", ins)
  //.put("enumJava", enumJava)
  val objArray: BsonArray = new BsonArray().add(long).add(bytearray1).add(ins).add(float).add(double).add(obj).add(bool).add(bsonArray)
  //.add(enum.A.toString)//.add(enumJava)
  val netty: Option[BosonImpl] = Some(new BosonImpl(byteArray = Option(obj.encode().getBytes)))
  val nettyArray: Option[BosonImpl] = Some(new BosonImpl(byteArray = Option(objArray.encode().getBytes)))

  test("Injector: Deep Level bsonObject Root") {
    val obj3: BsonObject = new BsonObject().put("John", "Nobody")
    val obj2: BsonObject = new BsonObject().put("John", "Locke")
    val arr2: BsonArray = new BsonArray().add(obj2)
    val obj1: BsonObject = new BsonObject().put("hey", "me").put("will", arr2)
    val array1: BsonArray = new BsonArray().add(1).add(2).add(obj1)
    val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!").put("finally", obj3)
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val netty: Option[BosonImpl] = Some(boson)
    val b1: Option[BosonImpl] = boson.modify(netty, "John", (_: String) => "Somebody")
    //val b1: Option[Boson] =Option(new Boson(byteArray = Option( boson.modifyAll(netty.get.getByteBuf, "John", _ => "Somebody", ocor = Option(0))._1.array())))// boson.modifyAll(netty.get.getByteBuf, "John", _ => "Somebody")._1

    val result: Any = b1 match {
      case Some(v) =>
        for (elem <- callParse(v, "John..all").asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]]) yield new String(elem).replaceAll("\\p{C}", "")
      //println("EXCEPTION= " + ext.parse(v, "John", "all").asInstanceOf[BsException].getValue)
      case None => List()
    }
    assert(Vector("Somebody", "Nobody") === result)
  }

  test("Injector: Deep Level bsonArray Root") {
    val obj3: BsonObject = new BsonObject().put("John", "Nobody")
    val obj2: BsonObject = new BsonObject().put("John", "Locke")
    val arr2: BsonArray = new BsonArray().add(obj2)
    val obj1: BsonObject = new BsonObject().put("hey", "me").put("will", arr2)
    val array1: BsonArray = new BsonArray().add(1).add(2).add(obj1)
    val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!").put("finally", obj3)
    val arrayEvent: BsonArray = new BsonArray().add("Dog").add(bsonEvent).addNull()
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val netty: Option[BosonImpl] = Some(boson)

    val b1: Option[BosonImpl] = boson.modify(netty, "John", (_: String) => "Somebody")
    //val b1: Option[Boson] = Option(new Boson(byteArray = Option( boson.modifyAll(netty.get.getByteBuf, "John", _ => "Somebody", ocor = Option(0))._1.array())))
    val result: Any = b1 match {
      case Some(v) =>
        for (elem <- callParse(v, "John..all").asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]]) yield new String(elem).replaceAll("\\p{C}", "")
      case None => List()
    }
    assert(Vector("Somebody", "Nobody") === result)
  }

  test("Injector: Int => Int") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "field", (x: Int) => x + 1)
    //println("test "+b1.getByteBuf.capacity())
    val b2: Option[BosonImpl] = b1.get.modify(b1, "field", (x: Int) => x + 1)
    val b3: Option[BosonImpl] = b2.get.modify(b2, "field", (x: Int) => x * 4)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => callParse(nb, "field..all")
    }
    println(result)
    assert(result === BsSeq(Vector(8))
      , "Contents are not equal")
  }

  test("Injector: String/CharSequence => String/CharSequence") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "no", (_: String) => "maybe")
    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "no..all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

  test("Injector: Array[Byte] => Array[Byte]") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "array", (_: Array[Byte]) => bytearray1)
    val b2: Option[BosonImpl] = b1.get.modify(b1, "array", (_: Array[Byte]) => bytearray2)
    val b3: Option[BosonImpl] = b2.get.modify(b2, "array", (_: Array[Byte]) => bytearray1)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => callParse(nb, "array..all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "") === "AlguresPorAi"
      , "Contents are not equal")
  }

  test("Injector: Instant => Instant") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "inst", (_: Instant) => ins1)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "inst..all")
    }

    val s: String = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(ins1 === Instant.parse(s)
      , "Contents are not equal")
  }

  test("Injector: Float => Float") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "float", (_: Float) => newFloat)


    val bP: ByteProcessor = (value: Byte) => {
      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
      true
    }
    val result: Any = b1 match {
      case None => List()
      case Some(nb) =>
        println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        nb.getByteBuf.forEachByte(bP)
        println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        callParse(nb, "float..all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Double]].head

    assert(newFloat === s
      , "Contents are not equal")

  }

  test("Injector: Double => Double") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "double", (_: Double) => newDouble)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "double..all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Double]].head

    assert(newDouble === s
      , "Contents are not equal")
  }
  //  TODO:find a way to inject BsonObject
  //  test("Injector: BsonObject => BsonObject") {
  //
  //    val b1: Option[BosonImpl] = netty.get.modify(netty, "bObj", (_: java.util.Map[String, Object]) => newbObj.getMap) //  .asScala.toMap
  //
  //    val result: Any = b1 match {
  //      case None => List()
  //      case Some(nb) => ext.parse(nb, "bObj", "all")
  //    }
  //    val s: Any = result.asInstanceOf[BsSeq].value.head
  //
  //    assert(Map("newbsonObj" -> "newbsonObj") === s
  //      , "Contents are not equal")
  //  }

  test("Injector: Boolean => Boolean") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "bool", (_: Boolean) => newBool)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "bool..all")
    }
    val s: Boolean = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Boolean]].head

    assert(newBool === s
      , "Contents are not equal")
  }

  test("Injector: Long => Long") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "long", (_: Long) => newLong)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "long..all")
    }
    val s: Long = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Long]].head

    assert(newLong === s
      , "Contents are not equal")
  }
  //  TODO:find a way to inject BsonArray
  //  test("Injector: BsonArray => BsonArray") {
  //    val b1: Option[BosonImpl] = netty.get.modify(netty, "bsonArray", (_: java.util.List[_]) => Mapper.convert(newbsonArray)) //  .asScala.toArray[Any]
  //
  //    val result: Any = b1 match {
  //      case None => List()
  //      case Some(nb) => ext.parse(nb, "bsonArray", "all")
  //    }
  //    val s: Any = result.asInstanceOf[BsSeq].value
  //
  //    assert(List(List(3, 4, "Bye")) === s,
  //      "Contents are not equal")
  //  }


  //  test("Injector: JavaEnum => JavaEnum") {
  //
  //    val b1: Option[BosonImpl] = inj.modify(netty, "enumJava", _ => newEnumJava)
  //
  //    val result: Any = b1 match {
  //      case None => List()
  //      case Some(nb) => ext.parse(nb, "enumJava", "all")
  //    }
  //    val s: Any = new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "")
  //
  //    println(s)
  //    assert(newEnumJava.toString === s
  //      , "Contents are not equal")
  //  }

  test("Injector: ScalaEnum => ScalaEnum") {

    val b1: Option[BosonImpl] = netty.get.modify(netty, "enumScala", (_: String) => enum.B.toString)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "enumScala..all")
    }
    val s: Any = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(enum.B.toString === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Int => Int") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "field", (_: Int) => 1)
    val b2: Option[BosonImpl] = b1.get.modify(b1, "field", (_: Int) => 2)
    val b3: Option[BosonImpl] = b2.get.modify(b2, "field", (_: Int) => 3)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => callParse(nb, "field..all")
    }
    assert(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Int]].head === 3
      , "Contents are not equal")
  }

  test("Injector BsonArray: String/CharSequence => String/CharSequence") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "no", (_: String) => "maybe")
    val b2: Option[BosonImpl] = b1.get.modify(b1, "no", (_: String) => "yes")
    val b3: Option[BosonImpl] = b2.get.modify(b2, "no", (_: String) => "maybe")
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => callParse(nb, "no..all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

  test("Injector BsonArray: Array[Byte] => Array[Byte]") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "array", (_: Array[Byte]) => bytearray1)
    val b2: Option[BosonImpl] = b1.get.modify(b1, "array", (_: Array[Byte]) => bytearray2)
    val b3: Option[BosonImpl] = b2.get.modify(b2, "array", (_: Array[Byte]) => bytearray1)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => callParse(nb, "array..all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "") === "AlguresPorAi"
      , "Contents are not equal")
  }

  test("Injector BsonArray: Instant => Instant") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "inst", (_: Instant) => ins1)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "inst..all")
    }

    val s: String = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(ins1 === Instant.parse(s)
      , "Contents are not equal")
  }

  test("Injector BsonArray: Float => Float") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "float", (_: Float) => newFloat)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "float..all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Double]].head

    assert(newFloat === s
      , "Contents are not equal")

  }

  test("Injector BsonArray: Double => Double") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "double", (_: Double) => newDouble)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "double..all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Double]].head

    assert(newDouble === s
      , "Contents are not equal")
  }
  //  TODO:find a way to inject BsonObject
  //  test("Injector BsonArray: BsonObject => BsonObject") {
  //
  //    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "bObj", (_: java.util.Map[String, Object]) => Mapper.convert(newbObj)) //  .asScala.toMap
  //
  //    val result: Any = b1 match {
  //      case None => List()
  //      case Some(nb) => ext.parse(nb, "bObj", "all")
  //    }
  //    val s: Any = result.asInstanceOf[BsSeq].value.head
  //
  //    assert(Map("newbsonObj" -> "newbsonObj") === s
  //      , "Contents are not equal")
  //  }

  test("Injector BsonArray: Boolean => Boolean") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "bool", (_: Boolean) => newBool)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "bool..all")
    }
    val s: Boolean = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Boolean]].head

    assert(newBool === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Long => Long") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "long", (_: Long) => newLong)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "long..all")
    }
    val s: Long = result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Long]].head

    assert(newLong === s
      , "Contents are not equal")
  }
  //  TODO:find a way to inject BsonArray
  //  test("Injector BsonArray: BsonArray => BsonArray") {
  //
  //    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "bsonArray", (_: java.util.List[_]) => Mapper.convert(newbsonArray))
  //
  //    val result: Any = b1 match {
  //      case None => List()
  //      case Some(nb) => ext.parse(nb, "bsonArray", "all")
  //    }
  //    val s: Any = result.asInstanceOf[BsSeq].value
  //
  //    assert(Seq(Seq(3, 4, "Bye")) === s
  //      , "Contents are not equal")
  //  }

  //  test("Injector BsonArray: JavaEnum => JavaEnum") {
  //
  //    val b1: Option[BosonImpl] = inj.modify(nettyArray, "enumJava", x => newEnumJava)
  //
  //    val result: Any = b1 match {
  //      case None => List()
  //      case Some(nb) => ext.parse(nb, "enumJava", "first")
  //    }
  //    val s: Any = new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "")
  //
  //    println(s)
  //    assert(newEnumJava.toString === s
  //      , "Contents are not equal")
  //  }

  test("Injector BsonArray: ScalaEnum => ScalaEnum") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "enumScala", (_: String) => enum.B.toString)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "enumScala..all")
    }
    val s: Any = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Vector[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(enum.B.toString === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Failure") {

    val b1: Option[BosonImpl] = netty.get.modify(nettyArray, "noField", (_: String) => enum.B.toString)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => callParse(nb, "noField..all")
    }
    assert(Vector() === result
      , "Contents are not equal")
  }
  /*test("Injector: Enumeration => Enumeration") {

    val b1: Option[BosonImpl] = inj.modify(netty, "enum", x => enum1)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "enum", "first")
    }

    val s: String = new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "")

    println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(enum1.toString === s
      , "Contents are not equal")
  }*/


}