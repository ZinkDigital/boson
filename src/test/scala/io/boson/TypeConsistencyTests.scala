package io.boson

import java.time.Instant

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue.BsSeq
import io.boson.injectors.Testing1.{b1, bP}
import io.boson.injectors.{EnumerationTest, Injector}
import io.boson.nettyboson.Boson
import io.boson.scalaInterface.ScalaInterface
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success, Try}
/**
  * Created by Ricardo Martins on 13/12/2017.
  */
@RunWith(classOf[JUnitRunner])
class TypeConsistencyTests extends FunSuite {

  val inj: Injector = new Injector
  val ext = new ScalaInterface


  test("Injector: Type Consistency Ints") {
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

  test("Injector: Type Consistency Long") {
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

  test("Injector: Type Consistency Boolean") {
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

  test("Injector: Type Consistency BsonArray") {
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

  test("Injector: Type Consistency BsonObject") {
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

  test("Injector: Type Consistency String/Array[Byte]") {
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

  test("Injector: Type Consistency Float/Double") {
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

}