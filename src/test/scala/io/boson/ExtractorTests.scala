package io.boson

import java.time.Instant

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import io.netty.buffer.{ByteBuf, Unpooled}
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by Tiago Filipe on 04/10/2017.
  */
@RunWith(classOf[JUnitRunner])
class ExtractorTests extends FunSuite {

  val obj1: BsonObject = new BsonObject().put("André", 975).put("António", 975L)
  val obj2: BsonObject = new BsonObject().put("Pedro", 1250L).put("José", false)
  val obj3: BsonObject = new BsonObject().put("Américo", 1500).putNull("Amadeu")

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)

  val now: Instant = Instant.now

  val globalObj: BsonObject = new BsonObject().put("Salary", 1000).put("AnualSalary", 12000L).put("Unemployed", false)
    .put("Residence", "Lisboa").putNull("Experience").put("BestFriend", obj1).put("Colleagues", arr)
    .put("UpdatedOn", now).put("FavoriteSentence", "Be the best".getBytes)

  test("Extract Int") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    assert(1500 === bosonBson.extract(bosonBson.getByteBuf, "Américo", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Long") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    assert(1250L === bosonBson.extract(bosonBson.getByteBuf, "Pedro", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Boolean") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    assert(false === bosonBson.extract(bosonBson.getByteBuf, "José", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Null") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    assert("Null" === bosonBson.extract(bosonBson.getByteBuf, "Amadeu", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Instant") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    assertEquals(now.toString, new String(bosonBson.extract(bosonBson.getByteBuf, "UpdatedOn", "first").get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]]).replaceAll("\\p{C}", ""))
  }

  test("Extract String") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes))
    assertEquals("Lisboa", new String(bosonBson.extract(bosonBson.getByteBuf, "Residence", "first")
      .get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]]).replaceAll("\\p{C}", ""))
  }

  test("Extract Array[Byte] w/ Netty") {
    val help: ByteBuf = Unpooled.buffer()
    val finalBuf: ByteBuf = Unpooled.buffer()
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    help.writeBytes(bosonBson.extract(bosonBson.getByteBuf, "FavoriteSentence", "first").get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]])
    finalBuf.writeBytes(io.netty.handler.codec.base64.Base64.decode(help))
    assert("Be the best".getBytes === new String(finalBuf.array()).replaceAll("\\p{C}", "").getBytes)
  }

  test("Extract Array[Byte] w/ String") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes))
    assert("Be the best".getBytes === new String(java.util.Base64.getMimeDecoder
      .decode(new String(bosonBson.extract(bosonBson.getByteBuf, "FavoriteSentence", "first")
        .get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]]))).getBytes)
  }

  test("Extract BsonObject") {
    val bsonEvent: BsonObject = new BsonObject().put("First", obj1).put("Second", obj2).put("Third", obj3)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    assert(Map("Pedro" -> 1250, "José" -> false) === bosonBson.extract(bosonBson.getByteBuf, "Second", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract BsonArray") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    val result = bosonBson.extract(bosonBson.getByteBuf, "Colleagues", "first").get.asInstanceOf[List[Array[Any]]]
    assert(Seq(
      Map("André" -> 975, "António" -> 975),
        Map("Pedro" -> 1250, "José" -> false),
        Map("Américo" -> 1500, "Amadeu" -> null))
     === result.head)
  }

  test("Extract deep layer") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val arr2: BsonArray = new BsonArray().add("Day3").add("Day20").add("Day31")
    obj2.put("JoséMonthLeave", arr2)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    assert(
      Seq("Day3", "Day20", "Day31")
        === bosonBson.extract(bosonBson.getByteBuf, "JoséMonthLeave", "first").get.asInstanceOf[List[Any]].head
    )
  }

  test("Extract nonexistent key") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    assert(None === bosonBson.extract(bosonBson.getByteBuf, "Random", "first"))
  }
}
