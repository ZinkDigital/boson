package io.boson



import java.util


import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonImpl.injectors.{InterpreterInj, ProgramInj, TinyLanguageInj}
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue._
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ByteProcessor
import io.vertx.core.buffer.Buffer
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
case class CustomException(smth:String) extends Exception {
  override def getMessage: String = smth
}

object Mapper {
  import scala.collection.JavaConverters._

  def convert(smth: Any): Any = {
    smth match {
      case bsonObject: BsonObject => convertBsonObject(bsonObject)
      case bsonArray: BsonArray => convertBsonArray(bsonArray)
      case _ => throw CustomException("No Type to Convert")
    }
  }

   private def convertBsonObject(bsonObject: BsonObject): Map[String, Any] = {
    //val map: mutable.Set[(String, Any)] = mutable.Set.empty
    val map =  new mutable.TreeMap[String, Any]()


    val bsonMap: Map[String, AnyRef] = bsonObject.getMap.asScala.toMap//.asInstanceOf[Map[String, Any]]
    //val entries: util.Set[util.Map.Entry[String, AnyRef]] =bsonObject.getMap.entrySet()// bsonMap.toSet
    val entries: Set[(String, AnyRef)] = bsonMap.toSet
    entries.foreach(entry => {
      val key: String = entry._1// getKey//_1
      val value: Any = entry._2//getValue//_2

      value match {
        case bsonObject: BsonObject =>
          map.put(key, convertBsonObject(bsonObject))
        case bsonArray: BsonArray =>
          map.put(key, convertBsonArray(bsonArray))
        case _ =>
          map.put(key, value)
      }

    })
    map.toMap
  }
  private def convertBsonArray(array: BsonArray): List[Any] = {
    //val list: util.List[Any] = new util.LinkedList[Any]()
    val list: mutable.ListBuffer[Any] = new mutable.ListBuffer[Any]
    val bsonList: List[Any] = array.getList.asScala.asInstanceOf[List[Any]]

    bsonList.foreach {
      case bsonObject: BsonObject =>
        list.append(convertBsonObject(bsonObject))
      case bsonArray: BsonArray =>
        list.append(convertBsonArray(bsonArray))
      case entry =>
        list.append(entry)
    }
    list.toList
  }

  def getAllTypes(any: Any, tabs: Int = 0): Any = {
    var tab: String = ""
    var t: Int = tabs
    while (t != 0){
      tab = tab.concat("\t")
      t=t-1
    }
    any match {
      case map: util.Map[_, _] =>
        map.entrySet().forEach( e => {
          val key: String =  e.getKey.asInstanceOf[String]
          val value: Any = e.getValue
          value match {
            case map1: util.Map[_, _] =>
              println(s"$tab$key -> ${map1.getClass.getSimpleName}")
              getAllTypes(map1, tabs + 1)
            case list1: util.List[_] =>
              println(s"$tab$key -> ${list1.getClass.getSimpleName}")
              getAllTypes(list1, tabs + 1)
            case null =>
              println(s"$tab$key -> ${"Null"}")
            case _ =>
              println(s"$tab$key -> ${value.getClass.getSimpleName}")
          }} )
      case list: util.List[_] =>
        var i = 0
        list.forEach {
          case map1: util.Map[_, _] =>
            println(s"$tab$i -> ${map1.getClass.getSimpleName}")
            i = i + 1
            getAllTypes(map1, tabs + 1)
          case list1: util.List[_] =>
            println(s"$tab$i -> ${list1.getClass.getSimpleName}")
            i = i + 1
            getAllTypes(list1, tabs + 1)
          case e: Any =>
            println(s"$tab$i -> ${e.getClass.getSimpleName}")
            i = i + 1
        }
      case _ => println("Wrong input type")
    }
  }
}



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
    val parser = new TinyLanguageInj
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new InterpreterInj(netty, f, r.asInstanceOf[ProgramInj]).run()
        case parser.Error(msg, _) => bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) => bsonValue.BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("First") {
    val key: String = "fridgeTemp"
    val expression: String = "fridgeTemp.first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Long]*4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
    }
    assert(BsSeq(List(20, 6, 3)) === resultParser.asInstanceOf[BsSeq])
  }
  test("All") {
    val key: String = "fridgeTemp"
    val expression: String = "fridgeTemp.all"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Long]*4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(20, 24, 12)) === resultParser.asInstanceOf[BsSeq])
  }
  test("Last") {
    val key: String = "fridgeTemp"
    val expression: String = "fridgeTemp.last"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Long]*4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(5, 6, 12)) === resultParser.asInstanceOf[BsSeq])
  }
  test("[0 to end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(4, 8, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq])
  }
  test("[0 until end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(4, 8, 12, 16, 5)) === resultParser.asInstanceOf[BsSeq])
  }
  test("[2 until 4] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[2 until 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson =>callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(1, 2, 12, 16, 5)) === resultParser.asInstanceOf[BsSeq])
  }
  test("[2 to 4] BsonArray as Root") {
    val key: String = ""
    val expression: String =  "[2 to 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(1, 2, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq])
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
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser)
    assert("Type Error. Cannot Cast boolean inside the Injector Function." === resultParser.asInstanceOf[BsException].getValue)
  }
  test("Last-1 BsonArray as Root") {

    val key: String = "fridgeTemp"
    val expression: String = "fridgeTemp.last"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Long] * 4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
            }

    assert(List(5, 6, 12) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("Last-2 BsonArray as Root") {

    val key: String = "fridgeTemp"
    val expression: String = "fridgeTemp.last"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent1.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Long] * 4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
    }

    assert(List(5, 5, 6, 12) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("All [0 to end] BsonArray as Root") {


    val key: String = ""
    val expression: String = "[0 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    assert(List(4, 8, 12, 16, 20) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("All [0 until end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[0 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    assert(List(4, 8, 12, 16, 5) === resultParser.asInstanceOf[BsSeq].getValue)
    }
  test("[1 until end] BsonArray as Root") {

    val key: String = ""
    val expression: String = "[1 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    assert(List(1, 8, 12, 16, 5) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("[1 to end] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[1 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    assert(List(1, 8, 12, 16, 20) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("[1 to 2] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[1 to 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    assert(List(1, 8, 12, 4, 5) === resultParser.asInstanceOf[BsSeq].getValue)
  }
  test("[1 until 2] BsonArray as Root") {
    val key: String = ""
    val expression: String = "[1 until 2]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    assert(List(1, 8, 3, 4, 5) === resultParser.asInstanceOf[BsSeq].getValue)
  }

  test("fridgeReadings.[1 until 2] Int => Int"){

    val arr2: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))


    val key: String = "fridgeReadings"
    val expression: String = "[1 until 2]"
    val expression1: String = "fridgeReadings.[1 until 2]"
    //lazy val res: (BosonImpl, BosonImpl) = boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(), key,(x:Int) => x*4 , "1", "2")

    lazy val resultBoson: BsValue = parseInj(boson,(x:Int) => x*4, expression1 )

    lazy val result1: BsValue = Try(resultBoson) match {
      case Success(v) => v
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }

    val result2: Any = result1 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e
      case BsBoson(nb)=> callParse(nb, "fridgeReadings.all")
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
    assert(List(List(1, 8, 3), List(1, 8, 3), List(1, 8, 3)) === resultParser)
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
    val expression: String = "[1 until 2]"
    val expression1: String = "fridgeReadings.[1 until 2]"

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
    assert(List(List(Map("age" -> 28, "country" -> "Spain", "name" -> "Tiago", "nick" -> "Ritchy")), List(Map("age" -> 28, "country" -> "Spain", "name" -> "Tiago", "nick" -> "Ritchy")), List(Map("age" -> 28, "country" -> "Spain", "name" -> "Tiago", "nick" -> "Ritchy"))) === resultParser)
  }
  test("fridgeReadings.all BsonArray => BsonArray"){

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
    val expression1: String = "fridgeReadings.all"

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
      case BsBoson(nb)=>callParse(nb, "fridgeReadings.[3 to 3]")
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
    assert(List(List(Map("age" -> 28, "country" -> "France", "name" -> "Pedro")), List(Map("age" -> 28, "country" -> "France", "name" -> "Pedro")), List(Map("age" -> 28, "country" -> "France", "name" -> "Pedro")))=== resultParser)
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
    val expression: String = "[1 until 2]"
    val expression1: String = "age.all"

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
      case BsBoson(nb)=>callParse(nb, "age.all")
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
    assert(List(56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0)=== resultParser)
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
    val expression: String = "[1 until 2]"
    val expression1: String = "name.all"

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
      case BsBoson(nb)=>callParse(nb, "name.all")
      case _ => /* Never Gets Here */ println("Never Gets Here")
    }
    val resultParser: Any = result2 match {
      case BsException(ex) =>
        println(ex)
        ex
      case BsSeq(e) => e.map(a => new String(a.asInstanceOf[Array[Byte]]))//.replaceAll("\\p{C}", "")) //TODO problema com strings
      case BsBoson(nb)=> nb
      case BsNumber(n) => n
      case BsBoolean(b) => b
    }
    assert( List("RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE")

      === resultParser)
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
