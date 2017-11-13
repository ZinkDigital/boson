package io.boson

import java.time.Instant

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.injectors.{EnumerationTest, Injector}
import io.boson.nettybson.NettyBson
import io.boson.scalaInterface.ScalaInterface
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by Ricardo Martins on 09/11/2017.
  */


@RunWith(classOf[JUnitRunner])
class InjectorsTest extends FunSuite {
  val inj: Injector = new Injector
  val ext = new ScalaInterface

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
  //val enum = EnumerationTest.A
  //val enum1 = EnumerationTest.B
  val obj: BsonObject = new BsonObject().put("field", 0).put("bool", bool).put("bsonArray", bsonArray).put("long", long).put("bObj", bObj).put("no", "ok").put("float", float).put("double", double).put("array", bytearray2).put("inst", ins)
  val netty: Option[NettyBson] = Some(ext.createNettyBson(obj.encode()))

  test("Injector: Int => Int") {

    val b1: Option[NettyBson] = inj.modify(netty, "field", x => x.asInstanceOf[Int]+1 )
    //println("test "+b1.getByteBuf.capacity())
    val b2: Option[NettyBson] = inj.modify(b1, "field", x => x.asInstanceOf[Int]+1)
    val b3: Option[NettyBson] = inj.modify(b2, "field", x => x.asInstanceOf[Int]*4)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "field", "first")
    }
    println(result.asInstanceOf[List[Int]].head)
    assert(result === List(8)
      , "Contents are not equal")
  }

  test("Injector: String/CharSequence => String/CharSequence") {

    val b1: Option[NettyBson] = inj.modify(netty, "no", x => "no")
    val b2: Option[NettyBson] = inj.modify(b1, "no", x => "yes")
    val b3: Option[NettyBson] = inj.modify(b2, "no", x => "maybe")
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "no", "first")
    }
    println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

  test("Injector: Array[Byte] => Array[Byte]") {

    val b1: Option[NettyBson] = inj.modify(netty, "array", x => bytearray1)
    val b2: Option[NettyBson] = inj.modify(b1, "array", x => bytearray2)
    val b3: Option[NettyBson] = inj.modify(b2, "array", x => bytearray1)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "array", "first")
    }
    println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "") === "AlguresPorAi"
      , "Contents are not equal")
  }

  test("Injector: Instant => Instant") {

    val b1: Option[NettyBson] = inj.modify(netty, "inst", x => ins1)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "inst", "first")
    }

    val s: String = new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "")

    println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(ins1 === Instant.parse(s)
      , "Contents are not equal")
  }

  test("Injector: Float => Float") {

    val b1: Option[NettyBson] = inj.modify(netty, "float", x => newFloat)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "float", "first")
    }
    val s: Double = result.asInstanceOf[List[Double]].head

    println(result.asInstanceOf[List[Double]].head)
    assert(newFloat === s
      , "Contents are not equal")

  }

  test("Injector: Double => Double") {

      val b1: Option[NettyBson] = inj.modify(netty, "double", x => newDouble)

      val result: Any = b1 match {
        case None => List()
        case Some(nb) => ext.parse(nb, "double", "first")
      }
      val s: Double = result.asInstanceOf[List[Double]].head

      println(result.asInstanceOf[List[Double]].head)
      assert(newDouble === s
        , "Contents are not equal")
    }

  test("Injector: BsonObject => BsonObject") {

    val b1: Option[NettyBson] = inj.modify(netty, "bObj", x => newbObj)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bObj", "first")
    }
    val s: BsonObject = result.asInstanceOf[List[BsonObject]].head

    println(result.asInstanceOf[List[BsonObject]].head)
    assert(newbObj === s
      , "Contents are not equal")
  }

  test("Injector: Boolean => Boolean") {

    val b1: Option[NettyBson] = inj.modify(netty, "bool", x => newBool)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bool", "first")
    }
    val s: Boolean = result.asInstanceOf[List[Boolean]].head

    println(result.asInstanceOf[List[Boolean]].head)
    assert(newBool === s
      , "Contents are not equal")
  }

  test("Injector: Long => Long") {

    val b1: Option[NettyBson] = inj.modify(netty, "long", x => newLong)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "long", "first")
    }
    val s: Long = result.asInstanceOf[List[Long]].head

    println(result.asInstanceOf[List[Long]].head)
    assert(newLong === s
      , "Contents are not equal")
  }

  test("Injector: BsonArray => BsonArray") {

    val b1: Option[NettyBson] = inj.modify(netty, "bsonArray", x => newbsonArray)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bsonArray", "first")
    }
    val s: BsonArray = result.asInstanceOf[List[BsonArray]].head

    println(result.asInstanceOf[List[BsonArray]].head)
    assert(newbsonArray === s
      , "Contents are not equal")
  }


  /*test("Injector: Enumeration => Enumeration") {

    val b1: Option[NettyBson] = inj.modify(netty, "enum", x => enum1)

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