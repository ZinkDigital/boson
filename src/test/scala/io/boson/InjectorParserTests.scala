package io.boson



import java.util
import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl

import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.{Boson, bsonValue}
import io.boson.bson.bsonValue._
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.{ByteProcessor, ResourceLeakDetector}
import io.vertx.core.buffer.Buffer
import mapper.Mapper
import org.junit.runner.RunWith

import scala.collection.mutable
//import org.scalameter.Events.Failure
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.immutable.List
import scala.util.{Failure, Success, Try}
import org.junit.Assert.assertEquals
/**
  * Created by Ricardo Martins on 15/12/2017.
  */
/*object Mapper  {
  val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
  val module = new SimpleModule
  module.addSerializer(classOf[util.Map[String, _]], BsonObjectSerializer)
  /*module.addSerializer(classOf[util.List[_]], BsonArraySerializer)*/
  mapper.registerModule(module)



  def encode(obj: Any): Array[Byte] = {
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Try(mapper.writeValue(os, obj)) match {
      case Success(_) =>
        os.flush()
        os.toByteArray
      case Failure(e) =>
        throw new RuntimeException(e)
    }
  }




}*/



@RunWith(classOf[JUnitRunner])
class InjectorParserTests extends FunSuite {
  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5L).put("fanVelocity", 20.5).put("doorOpen", false).putNull("null")
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 6L).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3L).put("fanVelocity", 20.5).put("doorOpen", true)
  val obj4: BsonObject = new BsonObject().put("vhjxfjgcv", obj1).put("fanVelocity", 0.0).put("doorOpen", true)
  val obj5: BsonObject = new BsonObject().put("fanVelocity", 20.5).put("doorOpen", true)
  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
  val arr1: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).add(obj5)
  val arr3: BsonArray = new BsonArray().add(obj4).add(arr)

  val arr4: BsonArray = new BsonArray().add(4).add(5).add(3)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)
  val bsonEvent1: BsonObject = new BsonObject().put("fridgeReadings", arr3)
  val bsonEventArray: BsonArray = new BsonArray().add(1).add(2).add(3).add(4).add(5)
  val bsonEventArray1: BsonArray = new BsonArray().add(1).add(2).add(3).add(4).add(true)
  val arr2: BsonArray = new BsonArray().add(1).add(2).add(3)
  val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
  val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
  val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))


  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }

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

  def parseInj[T](netty: BosonImpl,f: T => T, expression: String):bsonValue.BsValue = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program], Option(f)).run()
        case parser.Error(msg, _) => println("Error")
          bsonValue.BsObject.toBson(msg)

        case parser.Failure(msg, _) => println("Failure")
          bsonValue.BsObject.toBson(msg)

      }
    }catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("All") {
    val key: String = "fridgeTemp"
    val expression: String = "fridgeReadings.[0 to end].fridgeTemp"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Long]*4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => callParse(nb.getValue,expression)
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(Vector(20, 24, 12)) === resultParser.asInstanceOf[BsSeq])
  }
  test("[0 to end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Int) => x*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    //println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(Vector(Seq(4, 8, 12, 16, 20))) === resultParser.asInstanceOf[BsSeq])
  }
  test("[0 until end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(Vector(Seq(4, 8, 12, 16))) === resultParser.asInstanceOf[BsSeq])
  }
  test("[2 until 4] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[2 until 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson =>callParse(nb.getValue, expression)
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(Vector(Seq(12, 16))) === resultParser.asInstanceOf[BsSeq])
  }
  test("[2 to 4] BsonArray as Root") {
    val key: String = ""
    val expression: String =  "[2 to 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(Vector(Seq(12, 16, 20))) === resultParser.asInstanceOf[BsSeq])
  }
  test("[2 to 4] BsonArray as Root Test Type Consistency") {
    val key: String = ""
    val expression: String =  "[2 to 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray1.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException =>
        println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, ".all")
      case _ => List()
    }
    println( resultParser)
    assert("Type Error. Cannot Cast boolean inside the Injector Function." === resultParser.asInstanceOf[BsException].getValue)
  }
  test("All [0 to end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    assert(Vector(Seq(4, 8, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("All [0 until end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    assert(Vector(Seq(4, 8, 12, 16)) === resultParser.asInstanceOf[BsSeq].getValue)
    }
  test("[1 until end] BsonArray as Root") {

    val key: String = ""
    val expression: String = "[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    assert(Vector(Seq(8, 12, 16)) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("[1 to end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[1 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    assert(Vector(Seq(8, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("[1 to 2] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[1 to 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    assert(Vector(Seq(8, 12)) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("[1 until 2] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[1 until 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, expression)
      case _ => List()
    }
    assert(Vector(Seq(8)) === resultParser.asInstanceOf[BsSeq].getValue)
  }

  test("fridgeReadings.[1 until 2] Int => Int"){

    val arr2: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))


    val key: String = "fridgeReadings"
    val expression: String = "[0 to end].fridgeReadings.[0 to end]"
    val expression1: String = "[0 to end].fridgeReadings.[1 until 2]"
    //lazy val res: (BosonImpl, BosonImpl) = boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(), key,(x:Int) => x*4 , "1", "2")

    lazy val resultBoson: BsValue = parseInj(boson,(x:Int) => x*4, expression1 )
    //resultBoson.asInstanceOf[BsBoson].getValue.getByteBuf.forEachByte(bP)

    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) => v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }

    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> callParse(nb, expression)
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    println(result2)
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert(Vector(List(1, 8, 3), List(1, 8, 3), List(1, 8, 3)) === resultParser)
  }
  test("fridgeReadings.[1 until 2] BsonObject => BsonObject"){

    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28).put("country", "Portugal")
    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28).put("country", "Spain")
    val obj3: BsonObject = new BsonObject().put("name", "Joao").put("age", 28).put("country", "Germany")
    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))

    val key: String = "fridgeReadings"
    val expression: String = "[0 to end].fridgeReadings.[0 to end]"
    val expression1: String = "[0 to end].fridgeReadings.[1 until 2]"

    lazy val resultBoson: BsValue = parseInj(boson1,(x:Map[String, Any]) => x.+(("nick", "Ritchy")), expression1 )
    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) =>
        v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }
    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=>callParse(nb, expression1)
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert(Vector(List(Map("age" -> 28, "country" -> "Spain", "name" -> "Tiago", "nick" -> "Ritchy")), List(Map("age" -> 28, "country" -> "Spain", "name" -> "Tiago", "nick" -> "Ritchy")), List(Map("age" -> 28, "country" -> "Spain", "name" -> "Tiago", "nick" -> "Ritchy"))) === resultParser)
  }
  test("fridge*Readings BsonArray => BsonArray"){
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28).put("country", "Portugal")
    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28).put("country", "Spain")
    val obj3: BsonObject = new BsonObject().put("name", "Joao").put("age", 28).put("country", "Germany")
    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28).put("country", "France")
    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))

    val key: String = "fridgeReadings"
    val expression: String = "[1 until 2]"
    val expression1: String = "[0 to end].fridge*Readings"

    lazy val resultBoson: BsValue = parseInj(boson1,(x:List[Any]) => x:+ Mapper.convert(obj4), expression1 )
    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) =>
        v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }
    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsBoson(nb)=>callParse(nb, "[0 to end].fridge*Readings.[3 to 3]")
      case _ => /* Never Gets Here */ println("Never Gets Here")
    }
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert(Vector(List(Map("age" -> 28, "country" -> "France", "name" -> "Pedro")), List(Map("age" -> 28, "country" -> "France", "name" -> "Pedro")), List(Map("age" -> 28, "country" -> "France", "name" -> "Pedro")))=== resultParser)
  }
  test("age.all Double => Double"){

    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28.0).put("country", "Portugal")
    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28.0).put("country", "Spain")
    val obj3: BsonObject = new BsonObject().put("name", "Joao").put("age", 28.0).put("country", "Germany")
    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28.0).put("country", "France")
    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))

    val key: String = "age"
    val expression: String = "[0 to end].fridgeReadings.[0 to end].age"
    val expression1: String = "[0 to end].fridgeReadings.[0 to end].age"

    lazy val resultBoson: BsValue = parseInj(boson1,(x:Double) => x*2.0, expression1 )
    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) =>
        v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }
    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsBoson(nb)=>callParse(nb, expression)
      case _ => /* Never Gets Here */ println("Never Gets Here")
    }
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert(Vector(56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0)=== resultParser)
  }

  test("name.all String => String"){

    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28.0).put("country", "Portugal")
    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28.0).put("country", "Spain")
    val obj3: BsonObject = new BsonObject().put("name", "João").put("age", 28.0).put("country", "Germany")
    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28.0).put("country", "France")
    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))

    val key: String = "name"
    val expression: String = "[0 to end].fridgeReadings.[0 to end].name"
    val expression1: String = "[0 to end].fridgeReadings.[0 to end].name"

    lazy val resultBoson: BsValue = parseInj(boson1,(x:String) => x.concat("MINE"), expression1 )
    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) =>
        v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }
    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsBoson(nb)=>callParse(nb, expression)
      case _ => /* Never Gets Here */ println("Never Gets Here")
    }
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert( Vector("RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE")

      === resultParser)
  }

  test("name String => String"){

    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28.0).put("country", "Portugal")
    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28.0).put("country", "Spain")
    val obj3: BsonObject = new BsonObject().put("name", "João").put("age", 28.0).put("country", "Germany")
    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28.0).put("country", "France")
    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))

    val key: String = "name"
    val expression: String = "[0 to end].fridgeReadings.[0 to end].name"
    val expression1: String = "[0 to end].fridgeReadings.[0 to end].name"

    lazy val resultBoson: BsValue = parseInj(boson1,(x:String) => x.concat("MINE"), expression1 )
    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) =>
        v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }
    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsBoson(nb)=>callParse(nb, expression)
      case _ => /* Never Gets Here */ println("Never Gets Here")
    }
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e//.replaceAll("\\p{C}", "")) //TODO problema com strings
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert( Vector("RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE")

      === resultParser)
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
    assertEquals(BsSeq(Vector(Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10), Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10), Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10))),finalResult )
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
    assertEquals(BsSeq(Vector(Map("array" -> List(Map("damnnn" -> "DAMMN"), Map("damnnn" -> "DAMMN"), Map("damnnn" -> "DAMMN"), Map("creep" -> "DAMMN"), Map("WHAT!!!" -> 10))))),finalResult )
  }
  /*test("test"){
    import scala.collection.JavaConverters._
    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("sage", 28).put("age", 28).put("country", "Portugal")
    val obj2: BsonObject = new BsonObject().put("country", "Portugal").put("name", "Ricardo").put("sage", 28).put("age", 28)
    val obj1Enc: ByteBuf = obj1.encode().getByteBuf

   val boson: BosonImpl = new BosonImpl(byteArray = Option(obj1Enc.duplicate().array()))

   val m: Map[String, Any] = boson.decodeBsonObject(obj1Enc)

   /* val enc: ByteBuf = boson.encode(m)

    println(obj1Enc.capacity() + "    " + enc.capacity())
    val ziped: Array[(Byte, Byte)] = enc.array().zip(obj1Enc.array())

    ziped.foreach(c => println(s"enc=${c._1.toChar}   Obj1Enc=${c._2.toChar}" ))*/

    val map1: Any = Mapper.convert(obj1)
    println(map1)
    val map2: Any = Mapper.convert(obj2)
    println(map2)
    println(m)
  assert(map2 == m)
  }*/

}
