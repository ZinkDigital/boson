package io.joson

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.CustomException
import io.boson.json.jsonPath.{JsonInterpreter, JsonProgram, JsonTinyLanguage}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.vertx.core.json.{Json, JsonObject}
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by Ricardo Martins on 19/01/2018.
  */
@RunWith(classOf[JUnitRunner])
class JosonTests extends FunSuite{
  val json = "{\"Store\":{\"Book\":[{\"Title\":\"Java\",\"Price\":15.5,\"SpecialEditions\":[{\"Title\":\"JavaMachine\",\"Price\":39}]},{\"Title\":\"Scala\",\"Price\":21.5,\"SpecialEditions\":[{\"Title\":\"ScalaMachine\",\"Price\":40}]},{\"Title\":\"C++\",\"Price\":12.6,\"SpecialEditions\":[{\"Title\":\"C++Machine\",\"Price\":38}]}],\"Hat\":[{\"Price\":48,\"Color\":\"Red\"},{\"Price\":35,\"Color\":\"White\"},{\"Price\":38,\"Color\":\"Blue\"}]}}"

  val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
  val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
  val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
  val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
  val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  val sEditions3: BsonArray = new BsonArray().add(edition3)
  val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
  val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  val sEditions2: BsonArray = new BsonArray().add(edition2)
  val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
  val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  val sEditions1: BsonArray = new BsonArray().add(edition1)
  val title1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
  val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
  val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
  val bson: BsonObject = new BsonObject().put("Store", store)

  test("Joson"){
    val parser = new JsonTinyLanguage
    val buffer: ByteBuf = try {
      parser.parseAll(parser.programJson, json) match {
        case parser.Success(r, _) =>
          new JsonInterpreter().runJsonEncoder(r.asInstanceOf[JsonProgram])
        case parser.Error(msg, _) => throw CustomException(msg)
        case parser.Failure(msg, _) => throw CustomException(msg)
      }
    } catch {
      case e: Throwable =>
        println(e.getMessage)
        Unpooled.EMPTY_BUFFER
    }
    val bsonEncoded: ByteBuf = bson.encode().getByteBuf
    val a:Array[Byte] = buffer.array()
    val b:Array[Byte] = bsonEncoded.array()

    assertTrue(a.zip(b).forall(p => p._1==p._2))
  }

  test("Array to Json"){
    val parser = new JsonTinyLanguage
    val buffer: ByteBuf = try {
      parser.parseAll(parser.programJson, json) match {
        case parser.Success(r, _) =>
          new JsonInterpreter().runJsonEncoder(r.asInstanceOf[JsonProgram])
        case parser.Error(msg, _) => throw CustomException(msg)
        case parser.Failure(msg, _) => throw CustomException(msg)
      }
    } catch {
      case e: Throwable =>
        println(e.getMessage)
        Unpooled.EMPTY_BUFFER
    }
    val bsonEncoded: ByteBuf = bson.encode().getByteBuf
    val a:Array[Byte] = buffer.array()
    val str: String = new JsonInterpreter().decodeJson(buffer)
    val j1: JsonObject = new JsonObject(str)
    val j2: JsonObject = new JsonObject(json)

    assert(j1.equals(j2))
  }
}
