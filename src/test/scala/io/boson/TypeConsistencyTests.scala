package io.boson

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue.BsSeq
import io.boson.injectors.Injector
import io.boson.nettyboson.Boson
import io.boson.scalaInterface.ScalaInterface
import io.netty.util.ByteProcessor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.util.{Failure, Success, Try}
/**
  * Created by Ricardo Martins on 13/12/2017.
  */
@RunWith(classOf[JUnitRunner])
class TypeConsistencyTests extends FunSuite {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val inj: Injector = new Injector
  val ext = new ScalaInterface


  test("Injector: Type Consistency Int -> String") {
   val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => "w").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 1L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    // b1.get.getByteBuf.forEachByte(bP)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println("Success "+value)
        value
      case Failure(e) =>

        println("Failure "+e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    // b1.get.getByteBuf.forEachByte(bP)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.1).get)
    // b1.get.getByteBuf.forEachByte(bP)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.1f).get)
    // b1.get.getByteBuf.forEachByte(bP)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonObject().put("anything", 5).getMap).get)
    // b1.get.getByteBuf.forEachByte(bP)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_INT" === result)
  }
  test("Injector: Type Consistency Int -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi", 1).put("Bye", 2)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonArray().add("anything").getList).get)
    // b1.get.getByteBuf.forEachByte(bP)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_INT" === result)
  }

  test("Injector: Type Consistency Long -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 1000).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.3).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.3f).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonObject().put("anything", 2).getMap).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonArray().add("anything").getList).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_LONG" === result)
  }
  test("Injector: Type Consistency Long -> String") {
    val bson: BsonObject = new BsonObject().put("Hi", 10L).put("Bye", 200L)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => "String").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_LONG" === result)
  }

  test("Injector: Type Consistency Boolean -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 851).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 851L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> String") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => "String").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonObject().put("sdfw", 2).getMap).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonArray().add("sdfw").getList).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.3).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_BOOLEAN" === result)
  }
  test("Injector: Type Consistency Boolean -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", true).put("Bye", false)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.3f).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_BOOLEAN" === result)
  }

  test("Injector: Type Consistency BsonArray -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 851).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 851L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonObject().put("anything", 2).getMap).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> String") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => "String").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.3).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }
  test("Injector: Type Consistency BsonArray -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(1)).put("Bye", new BsonArray().add(2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 2.3f).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_BSONARRAY (java List or scala Array)" === result)
  }

  test("Injector: Type Consistency BsonObject -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 851).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23.0f).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonArray().add(2).getList).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> String") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => "fde").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Injector: Type Consistency BsonObject -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonObject().put("Ola", 1)).put("Bye", new BsonObject().put("Adeus", 2))
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23.0).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }


  test("Injector: Type Consistency String/Array[Byte] -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23.0f).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> null") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> None") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23.0).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonObject().put("anything", 2).getMap).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }
  test("Injector: Type Consistency String/Array[Byte] -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi","Olá").put("Bye","Adeus")
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonArray().add("anything").getList).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ" === result)
  }

  test("Injector: Type Consistency Float/Double -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => 23L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> null") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> None") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonObject().put("anything", 2).getMap).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => new BsonArray().add(2).getList).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_FLOAT_DOUBLE" === result)
  }
  test("Injector: Type Consistency Float/Double -> String") {
    val bson: BsonObject = new BsonObject().put("Hi",1.0f).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Hi", _ => "dcfdf").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Hi", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_FLOAT_DOUBLE" === result)
  }

  test("Horrible Simple Injector: Type Consistency Int -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 23.0f).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 23.0).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 23L).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => true).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => null).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => None).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> String") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => "sdvknsd").get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> BsonObject") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => new BsonObject().put("anything", 2).getMap ).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type LinkedHashMap. Value type require D_INT" === result)
  }
  test("Horrible Simple Injector: Type Consistency Int -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2", 10))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => new BsonArray().add(2).getList).get)
    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_INT" === result)
  }

  test("Horrible Simple Injector: Type Consistency BsonObject -> BsonArray") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => new BsonArray().add(2).getList).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type ArrayList. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> Int") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 1).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Integer. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> Long") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 12L).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Long. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> Boolean") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => true).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Boolean. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> null") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => null).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> None") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => None).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type None$. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> String") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => "svwv").get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type String. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> Float") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 23.0f).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Float. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }
  test("Horrible Simple Injector: Type Consistency BsonObject -> Double") {
    val bson: BsonObject = new BsonObject().put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "key2", _ => 52.0).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "key2", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("Wrong inject type. Injecting type Double. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])" === result)
  }

  test("Horrible Simple Injector: Type Consistency Null -> Double") {
    val bson: BsonObject = new BsonObject().putNull("Null").put("Hi", new BsonArray().add(0).add(1).add(2).add(new BsonObject().put("key", "code").put("key1", new BsonArray().add(new BsonArray().add(1000L)).add(new BsonObject().put("key2",new BsonObject().put("key3", "code3")))))).put("Bye",25.1f)
    val netty: Option[Boson] = Option(ext.createBoson(bson.encode().getBytes))
    val b1: Try[Boson] = Try(inj.modify(netty, "Null", _ => 52.0).get)

    val result: Any = b1 match {
      case Success(v) =>
        val sI: ScalaInterface = new ScalaInterface
        println("Extracting the field injected with value: ")
        val value: Any = sI.parse(v, "Null", "all").asInstanceOf[BsSeq].value.head
        println(value)
        value
      case Failure(e) =>
        println(e.getMessage)
        e.getMessage
    }
    assert("NULL field. Can not be changed" === result)
  }
}