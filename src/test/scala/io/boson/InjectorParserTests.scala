package io.boson



import java.util

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.Boson
import io.boson.bson.bsonValue.{BsBoson, BsException, BsSeq, BsValue}
import io.boson.scalaInterface.ScalaInterface
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ByteProcessor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.immutable.List

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

  val si: ScalaInterface = new ScalaInterface

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }

  test("First") {
    val key: String = "fridgeTemp"
    val netty: Option[Boson] = Some(si.createBoson(bsonEvent.encode().getBytes))
    val resultBoson: BsValue = si.parseInj(netty.get, key, x => x.asInstanceOf[Long]*4L,  "first")
    // println(resultParser)
    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson => si.parse(nb.getValue, "fridgeTemp", "all")
      case _ => List()
    }
    assert(BsSeq(List(20, 6, 3)) === resultParser.asInstanceOf[BsSeq])
  }

  test("All") {
    val key: String = "fridgeTemp"

    val netty: Option[Boson] = Some(si.createBoson(bsonEvent.encode().getBytes))

    val resultBoson: BsValue = si.parseInj(netty.get, key, x => x.asInstanceOf[Long]*4L,  "all")
    // println(resultParser)

    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson =>
        si.parse(nb.getValue, "fridgeTemp", "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(20, 24, 12)) === resultParser.asInstanceOf[BsSeq])
  }

  test("Last") {
    val key: String = "fridgeTemp"
    val netty: Option[Boson] = Some(si.createBoson(bsonEvent.encode().getBytes))

    val resultBoson: BsValue = si.parseInj(netty.get, key, x => x.asInstanceOf[Long]*4L,  "last")
    // println(resultParser)

    val resultParser: Any = resultBoson match {
      case ex: BsException => println(ex.getValue)
      case nb: BsBoson =>
        si.parse(nb.getValue, "fridgeTemp", "all")
      case _ => List()
    }
    println( resultParser.asInstanceOf[BsSeq])
    assert(BsSeq(List(5, 24, 3)) === resultParser.asInstanceOf[BsSeq])

  }

  /*test("Encoding") {

    val buf0 = obj1.encode().getByteBuf.array()

    val buf1 = new Boson().encode(Mapper.convert(obj1))

    println("buf0 size = " + buf0.length + "    buf1 size = " + buf1.length)

    println("+++++++++++++++")
   // buf0.foreach(b => println(b.toChar))

    //println("+++++++++++++++")
    //buf1.foreach(b => println(b.toChar))
    val a2 = buf0.zip(buf1)
    println("Bufs iguais= " + a2.forall( b => b._1==b._2))



    a2.foreach(b2 => println("char= " + b2._1.toChar + " int= " + b2._1.toInt + " byte= " + b2._1 + "   char= " + b2._2.toChar + " int= " + b2._2.toInt + " byte= " + b2._2 ))
  }*/

}
