package io.boson

import bsonLib.BsonObject
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import io.vertx.core.json.JsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
@RunWith(classOf[JUnitRunner])
class LongInputTests extends FunSuite {

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

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  val boson: BosonImpl = new BosonImpl(byteArray = Option(bson.encode().getBytes))

  test("extract top field") {
    val result: BsValue = callParse(boson.duplicate, "Epoch.first")
    assert(bson.getInteger("Epoch") === result.asInstanceOf[BsSeq].value.head)
  }

  test("extract bottom field") {
    val result: BsValue = callParse(boson.duplicate, "SSLNLastName.last")
    assert( "de Huanuco" ===
      new String(result.asInstanceOf[BsSeq].value.head.asInstanceOf[Array[Byte]]).replaceAll("\\p{C}", ""))
  }

  test("extract all occurrences of Key") {
    val result: BsValue = callParse(boson.duplicate, "Tags.all")
    println(result.asInstanceOf[BsSeq].value)
    assert(true)
  }

  test("extract positions of an Array") {
    val result: BsValue = callParse(boson.duplicate, "Markets.[3 to 5]")
    println(result.asInstanceOf[BsSeq].getValue)
    assert(true)
  }

  test("extract further positions of an Array") {
    val result: BsValue = callParse(boson.duplicate, "Markets.[50 to 55]")
    println(result.asInstanceOf[BsSeq].getValue.head)
    assert(true)
  }

  test("size of all occurrences of Key") {
    val result: BsValue = callParse(boson.duplicate, "Price.all")
    assert(283 === result.asInstanceOf[BsSeq].value.size)
  }

}
