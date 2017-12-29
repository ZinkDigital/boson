package io.boson



import java.util

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonImpl.injectors.{InterpreterInj, ProgramInj, TinyLanguageInj}
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue.{BsBoson, BsException, BsSeq, BsValue}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ByteProcessor
import org.junit.runner.RunWith
//import org.scalameter.Events.Failure
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.immutable.List
import scala.util.{Failure, Success, Try}

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
  def convert(smth: Any): Any = {
    smth match {
      case bsonObject: BsonObject => convertBsonObject(bsonObject)
      case bsonArray: BsonArray => convertBsonArray(bsonArray)
      case _ => throw CustomException("No Type to Convert")
    }
  }

  private def convertBsonObject(bsonObject: BsonObject): util.Map[String, _] = {
    val map = new util.LinkedHashMap[String, AnyRef]()
    val bsonMap: util.Map[String, AnyRef] = bsonObject.getMap
    val entries: util.Set[util.Map.Entry[String, AnyRef]] = bsonMap.entrySet()

    entries.forEach(entry => {
      val key: String = entry.getKey
      val value: AnyRef = entry.getValue

      value match {
        case bsonObject: BsonObject =>
          map.put(key, convertBsonObject(bsonObject))
        case bsonArray: BsonArray =>
          map.put(key, convertBsonArray(bsonArray))
        case _ =>
          map.put(key, value)
      }

    })
    map
  }
  private def convertBsonArray(array: BsonArray): util.List[_] = {
    //val list: util.List[Any] = new util.LinkedList[Any]()
    val list: util.ArrayList[Any] = new util.ArrayList[Any]()
    val bsonList: util.List[_] = array.getList

    bsonList.forEach {
      case bsonObject: BsonObject =>
        list.add(convertBsonObject(bsonObject))
      case bsonArray: BsonArray =>
        list.add(convertBsonArray(bsonArray))
      case entry =>
        list.add(entry)
    }
    list
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
  val obj4: BsonObject = new BsonObject().put("fridgeTemp", obj1).put("fanVelocity", 0.0).put("doorOpen", true)
  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
  val arr1: BsonArray = new BsonArray().add(obj4).add(arr)
  val arr2: BsonArray = new BsonArray().addNull()
  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)
  val bsonEventArray: BsonArray = new BsonArray().add(1).add(2).add(3).add(4).add(5)
  val bsonEventArray1: BsonArray = new BsonArray().add(1).add(2).add(3).add(4).add(true)


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
  def parseInj(netty: BosonImpl, key: String,f: Any => Any, expression: String):bsonValue.BsValue = {
    val parser = new TinyLanguageInj
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new InterpreterInj(netty, key, f, r.asInstanceOf[ProgramInj]).run()
        case parser.Error(msg, _) => bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) => bsonValue.BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  test("First") {
    val key: String = "fridgeTemp"
    val expression: String = "first"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Long]*4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
    }
    assert(BsSeq(List(20, 6, 3)) === resultParser.asInstanceOf[BsSeq])
  }

  test("All") {
    val key: String = "fridgeTemp"
    val expression: String = "all"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Long]*4L, expression)
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
    val expression: String = "last"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Long]*4L, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => callParse(nb.getValue, "fridgeTemp.all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(5, 6, 12)) === resultParser.asInstanceOf[BsSeq])
  }

  test("[0 to end]") {
    val key: String = ""
    val expression: String = "[0 to end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(4, 8, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq])
  }

  test("[0 until end]") {
    val key: String = ""
    val expression: String = "[0 until end]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(4, 8, 12, 16, 5)) === resultParser.asInstanceOf[BsSeq])
  }

  test("[2 until 4]") {
    val key: String = ""
    val expression: String = "[2 until 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson =>callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(1, 2, 12, 16, 5)) === resultParser.asInstanceOf[BsSeq])
  }

  test("[2 to 4]") {
    val key: String = ""
    val expression: String =  "[2 to 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Int]*4, expression)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
        ex
      case nb: BsBoson => callParse(nb.getValue, "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(1, 2, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq])
  }

  test("[2 to 4] Test Type Consistency") {
    val key: String = ""
    val expression: String =  "[2 to 4]"
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray1.encode().getBytes))
    val resultBoson: BsValue = parseInj(boson, key, x => x.asInstanceOf[Int]*4, expression)
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

  /*test("Modify vs ModifyAll") {
    val obj3: BsonObject = new BsonObject().put("John", "Nobody")
    val obj2: BsonObject = new BsonObject().put("John", "Locke")
    val arr2: BsonArray = new BsonArray().add(obj2)
    val obj1: BsonObject = new BsonObject().put("hey", "me").put("will", arr2)
    val array1: BsonArray = new BsonArray().add(1).add(2).add(obj1)
    val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!").put("finally", obj3)
    val arrayEvent: BsonArray = new BsonArray().add("Dog").add(bsonEvent).addNull()
    val boson: Boson = new Boson(byteArray = Option(obj1.encode().getBytes))
    val netty: Option[Boson] = Some(boson)

    val b1: Option[Boson] = boson.modify(netty, "John", _ => "Somebody")
    val b2: Option[Boson] = Option(new Boson(byteArray = Option( boson.modifyAll(netty.get.getByteBuf, "John", _ => "Somebody", ocor = Option(0))._1.array())))

    val buf0 = b1.get.getByteBuf
    val buf1 = b2.get.getByteBuf

    println("Buf0 Size= " +  buf0.capacity() + " Buf1 Size= " + buf1.capacity())
    println("Iguais? " + buf0.array().zip(buf1.array()).forall(b => b._1==b._2))

    buf0.array().zip(buf1.array()).foreach(b =>  println("char= " + b._1.toChar + " int= " + b._1.toInt + " byte= " + b._1 + "char= " + b._2.toChar + " int= " + b._2.toInt + " byte= " + b._2))


    assert(buf0.array() === buf1.array())
  }*/

  /*test("reverse") {
    val fn: (Any) => Int = (x:Any) => {
      val s: Int = x.isInstanceOf[Int] match {
        case true => x.asInstanceOf[Int]
        case false => throw CustomException("Erro de tipos")
      }
s*4
    }
  }*/

}
