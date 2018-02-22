package io.zink.boson

import bsonLib.BsonObject
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.JsonObject
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue
import io.zink.boson.bson.bsonValue.{BsSeq, BsValue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
@RunWith(classOf[JUnitRunner])
class LongInputTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
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
    val result: BsValue = callParse(boson.duplicate, "Epoch")
    assert(bson.getInteger("Epoch") === result.asInstanceOf[BsSeq].value.head)
  }

  test("extract bottom field") {
    val result: BsValue = callParse(boson.duplicate, "SSLNLastName")
    assert( "de Huanuco" === result.asInstanceOf[BsSeq].value.head)
  }

  test("extract all occurrences of Key") {
    val result: BsValue = callParse(boson.duplicate, "Tags")
    println(result.asInstanceOf[BsSeq].value)
    val t: Boolean = true
    assert(t)
  }

  test("extract positions of an Array") {
    val result: BsValue = callParse(boson.duplicate, "Markets[3 to 5]")
    println(result.asInstanceOf[BsSeq].getValue)
    val t: Boolean = true
    assert(t)
  }

  test("extract further positions of an Array") {
    val result: BsValue = callParse(boson.duplicate, "Markets[50 to 55]")
    println(result.asInstanceOf[BsSeq].getValue.head)
    val t: Boolean = true
    assert(t)
  }

  test("size of all occurrences of Key") {
    val result: BsValue = callParse(boson.duplicate, "Price")
    assert(283 === result.asInstanceOf[BsSeq].value.size)
  }

}
