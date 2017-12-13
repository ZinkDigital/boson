package io.boson

import java.time.Instant

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue.BsSeq
import io.boson.injectors.{EnumerationTest, Injector}
import io.boson.nettyboson.Boson
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

  object enum extends Enumeration{
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
  val enumJava: EnumerationTest = io.boson.injectors.EnumerationTest.A
  val newEnumJava = io.boson.injectors.EnumerationTest.B
  //val enum = EnumerationTest.A
  //val enum1 = EnumerationTest.B
  val obj: BsonObject = new BsonObject().put("field", 0).put("bool", bool).put("enumScala", enum.A.toString).put("bsonArray", bsonArray).put("long", long).put("bObj", bObj).put("no", "ok").put("float", float).put("double", double).put("array", bytearray2).put("inst", ins)//.put("enumJava", enumJava)
  val objArray: BsonArray = new BsonArray().add(long).add(bytearray1).add(ins).add(float).add(double).add(obj).add(bool).add(bsonArray)//.add(enum.A.toString)//.add(enumJava)
  val netty: Option[Boson] = Some(ext.createBoson(obj.encode().getBytes))
  val nettyArray: Option[Boson] = Some(ext.createBoson(objArray.encode().getBytes))

  test("Injector: Deep Level bsonObject Root") {
    val obj3: BsonObject = new BsonObject().put("John", "Nobody")
    val obj2: BsonObject = new BsonObject().put("John", "Locke")
    val arr2: BsonArray = new BsonArray().add(obj2)
    val obj1: BsonObject = new BsonObject().put("hey", "me").put("will", arr2)
    val array1: BsonArray = new BsonArray().add(1).add(2).add(obj1)
    val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!").put("finally", obj3)
    val inj: Injector = new Injector

    val netty: Option[Boson] = Some(new Boson(byteArray = Option(bsonEvent.encode().getBytes)))

    val b1: Option[Boson] = inj.modify(netty, "John", _ => "Somebody")

    val result: Any = b1 match {
      case Some(v) =>
        for (elem <- ext.parse(v, "John", "all").asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]]) yield new String(elem).replaceAll("\\p{C}", "")
      case None => List()
    }
    assert(List("Somebody","Nobody") === result)
  }

  test("Injector: Deep Level bsonArray Root") {
    val obj3: BsonObject = new BsonObject().put("John", "Nobody")
    val obj2: BsonObject = new BsonObject().put("John", "Locke")
    val arr2: BsonArray = new BsonArray().add(obj2)
    val obj1: BsonObject = new BsonObject().put("hey", "me").put("will", arr2)
    val array1: BsonArray = new BsonArray().add(1).add(2).add(obj1)
    val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!").put("finally", obj3)
    val arrayEvent: BsonArray = new BsonArray().add("Dog").add(bsonEvent).addNull()
    val inj: Injector = new Injector

    val netty: Option[Boson] = Some(new Boson(byteArray = Option(bsonEvent.encode().getBytes)))

    val b1: Option[Boson] = inj.modify(netty, "John", _ => "Somebody")

    val result: Any = b1 match {
      case Some(v) =>
        for (elem <- ext.parse(v, "John", "all").asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]]) yield new String(elem).replaceAll("\\p{C}", "")
      case None => List()
    }
    assert(List("Somebody","Nobody") === result)
  }

  test("Injector: Int => Int") {

    val b1: Option[Boson] = inj.modify(netty, "field", x => x.asInstanceOf[Int]+1 )
    //println("test "+b1.getByteBuf.capacity())
    val b2: Option[Boson] = inj.modify(b1, "field", x => x.asInstanceOf[Int]+1)
    val b3: Option[Boson] = inj.modify(b2, "field", x => x.asInstanceOf[Int]*4)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "field", "all")
    }
    println(result)
    assert(result === BsSeq(List(8))
      , "Contents are not equal")
  }

  test("Injector: String/CharSequence => String/CharSequence") {

    val b1: Option[Boson] = inj.modify(netty, "no", _ => "maybe")
    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "no", "all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

  test("Injector: Array[Byte] => Array[Byte]") {

    val b1: Option[Boson] = inj.modify(netty, "array", _ => bytearray1)
    val b2: Option[Boson] = inj.modify(b1, "array", _ => bytearray2)
    val b3: Option[Boson] = inj.modify(b2, "array", _ => bytearray1)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "array", "all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "") === "AlguresPorAi"
      , "Contents are not equal")
  }

  test("Injector: Instant => Instant") {

    val b1: Option[Boson] = inj.modify(netty, "inst", _ => ins1)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "inst", "all")
    }

    val s: String = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(ins1 === Instant.parse(s)
      , "Contents are not equal")
  }

  test("Injector: Float => Float") {

    val b1: Option[Boson] = inj.modify(netty, "float", _ => newFloat)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "float", "all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Double]].head

    assert(newFloat === s
      , "Contents are not equal")

  }

  test("Injector: Double => Double") {

      val b1: Option[Boson] = inj.modify(netty, "double", _ => newDouble)

      val result: Any = b1 match {
        case None => List()
        case Some(nb) => ext.parse(nb, "double", "all")
      }
      val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Double]].head

      assert(newDouble === s
        , "Contents are not equal")
    }

  test("Injector: BsonObject => BsonObject") {

    val b1: Option[Boson] = inj.modify(netty, "bObj", _ => newbObj)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bObj", "all")
    }
    val s: Any = result.asInstanceOf[BsSeq].value.head

    assert(Map("newbsonObj" -> "newbsonObj") === s
      , "Contents are not equal")
  }

  test("Injector: Boolean => Boolean") {

    val b1: Option[Boson] = inj.modify(netty, "bool", _ => newBool)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bool", "all")
    }
    val s: Boolean = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Boolean]].head

    assert(newBool === s
      , "Contents are not equal")
  }

  test("Injector: Long => Long") {

    val b1: Option[Boson] = inj.modify(netty, "long", _ => newLong)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "long", "all")
    }
    val s: Long = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Long]].head

    assert(newLong === s
      , "Contents are not equal")
  }

  test("Injector: BsonArray => BsonArray") {
    val b1: Option[Boson] = inj.modify(netty, "bsonArray", _ => newbsonArray)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bsonArray", "all")
    }
    println("nheee " + result)
    val s: Any = result.asInstanceOf[BsSeq].value

    assert(List(List(3, 4, "Bye")) === s,
      "Contents are not equal")
  }


//  test("Injector: JavaEnum => JavaEnum") {
//
//    val b1: Option[Boson] = inj.modify(netty, "enumJava", _ => newEnumJava)
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

    val b1: Option[Boson] = inj.modify(netty, "enumScala", _ => enum.B.toString)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "enumScala", "all")
    }
    val s: Any = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(enum.B.toString === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Int => Int") {

    val b1: Option[Boson] = inj.modify(nettyArray, "field", _ => 1)
    val b2: Option[Boson] = inj.modify(b1, "field", _ => 2)
    val b3: Option[Boson] = inj.modify(b2, "field", _ => 3)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "field", "all")
    }
    assert(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Int]].head === 3
      , "Contents are not equal")
  }

  test("Injector BsonArray: String/CharSequence => String/CharSequence") {

    val b1: Option[Boson] = inj.modify(nettyArray, "no", _ => "maybe")
    val b2: Option[Boson] = inj.modify(b1, "no", _ => "yes")
    val b3: Option[Boson] = inj.modify(b2, "no", _ => "maybe")
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "no", "all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

  test("Injector BsonArray: Array[Byte] => Array[Byte]") {

    val b1: Option[Boson] = inj.modify(nettyArray, "array", _ => bytearray1)
    val b2: Option[Boson] = inj.modify(b1, "array", _ => bytearray2)
    val b3: Option[Boson] = inj.modify(b2, "array", _ => bytearray1)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "array", "all")
    }
    assert(new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "") === "AlguresPorAi"
      , "Contents are not equal")
  }

  test("Injector BsonArray: Instant => Instant") {

    val b1: Option[Boson] = inj.modify(nettyArray, "inst", _ => ins1)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "inst", "all")
    }

    val s: String = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(ins1 === Instant.parse(s)
      , "Contents are not equal")
  }

  test("Injector BsonArray: Float => Float") {

    val b1: Option[Boson] = inj.modify(nettyArray, "float", _ => newFloat)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "float", "all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Double]].head

    assert(newFloat === s
      , "Contents are not equal")

  }

  test("Injector BsonArray: Double => Double") {

    val b1: Option[Boson] = inj.modify(nettyArray, "double", _ => newDouble)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "double", "all")
    }
    val s: Double = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Double]].head

    assert(newDouble === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: BsonObject => BsonObject") {

    val b1: Option[Boson] = inj.modify(nettyArray, "bObj", _ => newbObj)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bObj", "all")
    }
    val s: Any = result.asInstanceOf[BsSeq].value.head

    assert(Map("newbsonObj" -> "newbsonObj") === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Boolean => Boolean") {

    val b1: Option[Boson] = inj.modify(nettyArray, "bool", _ => newBool)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bool", "all")
    }
    val s: Boolean = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Boolean]].head

    assert(newBool === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Long => Long") {

    val b1: Option[Boson] = inj.modify(nettyArray, "long", _ => newLong)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "long", "all")
    }
    val s: Long = result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Long]].head

    assert(newLong === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: BsonArray => BsonArray") {

    val b1: Option[Boson] = inj.modify(nettyArray, "bsonArray", _ => newbsonArray)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "bsonArray", "all")
    }
    val s: Any = result.asInstanceOf[BsSeq].value

    assert(Seq(Seq(3, 4, "Bye")) === s
      , "Contents are not equal")
  }

//  test("Injector BsonArray: JavaEnum => JavaEnum") {
//
//    val b1: Option[Boson] = inj.modify(nettyArray, "enumJava", x => newEnumJava)
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

    val b1: Option[Boson] = inj.modify(nettyArray,"enumScala", _ => enum.B.toString)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "enumScala", "all")
    }
    val s: Any = new String(result.asInstanceOf[BsSeq].value.asInstanceOf[Seq[Array[Byte]]].head).replaceAll("\\p{C}", "")

    assert(enum.B.toString === s
      , "Contents are not equal")
  }

  test("Injector BsonArray: Failure") {

    val b1: Option[Boson] = inj.modify(nettyArray, "noField", _ => enum.B.toString)

    val result: Any = b1 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "noField", "all")
    }
    assert(List() === result
      , "Contents are not equal")
  }
  /*test("Injector: Enumeration => Enumeration") {

    val b1: Option[Boson] = inj.modify(netty, "enum", x => enum1)

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