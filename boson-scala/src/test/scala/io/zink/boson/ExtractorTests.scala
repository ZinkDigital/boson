package io.zink.boson

import java.time.Instant

import bsonLib.{BsonArray, BsonObject}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector
import io.zink.boson.bson.bsonImpl.BosonImpl
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by Tiago Filipe on 04/10/2017.
  */
@RunWith(classOf[JUnitRunner])
class ExtractorTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
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
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(bsonEvent.encodeToBarray()))
    assert(1500 === bosonBson.extract(either, List(("Américo","allDots")), List((None,None,""))).head)
  }

  test("Extract Long") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(bsonEvent.encodeToBarray()))
    assert(1250L === bosonBson.extract(either, List(("Pedro", "allDots")), List((None,None,""))).head)
  }

  test("Extract Boolean") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(bsonEvent.encodeToBarray()))
    assert(false === bosonBson.extract(either, List(("José","allDots")), List((None,None,""))).head)
  }

  test("Extract Null") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(bsonEvent.encodeToBarray()))
    assert("Null" === bosonBson.extract(either, List(("Amadeu","allDots")), List((None,None,""))).head)
  }

  test("Extract Instant") {
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(globalObj.encodeToBarray()))
    assertEquals(now.toString, bosonBson.extract(either, List(("UpdatedOn","allDots")), List((None,None,""))).head)
  }

  test("Extract String") {
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(globalObj.encodeToBarray()))
    assertEquals("Lisboa", bosonBson.extract(either, List(("Residence","allDots")), List((None,None,""))).head)
  }

  test("Extract Array[Byte] w/ Netty") {
    val help: ByteBuf = Unpooled.buffer()
    val finalBuf: ByteBuf = Unpooled.buffer()
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(globalObj.encodeToBarray()))
    help.writeBytes(bosonBson.extract(either, List(("FavoriteSentence","allDots")), List((None,None,""))).head.asInstanceOf[String].getBytes)
    finalBuf.writeBytes(io.netty.handler.codec.base64.Base64.decode(help))
    assert("Be the best".getBytes === new String(finalBuf.array()).replaceAll("\\p{C}", "").getBytes)
  }

  test("Extract Array[Byte] w/ String") {
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(globalObj.encodeToBarray()))
    assert("Be the best".getBytes === java.util.Base64.getMimeDecoder
      .decode(bosonBson.extract(either, List(("FavoriteSentence", "allDots")), List((None, None, "")))
        .head.asInstanceOf[String]))
  }

  test("Extract BsonObject") {
    val bsonEvent: BsonObject = new BsonObject().put("First", obj1).put("Second", obj2).put("Third", obj3)
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(bsonEvent.encodeToBarray()))
    val result = bosonBson.extract(either, List(("Second","allDots")), List((None,None,""))).asInstanceOf[Seq[ByteBuf]].map(_.array)
    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray())
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract BsonArray") {
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(globalObj.encodeToBarray()))
    val result = bosonBson.extract(either, List(("Colleagues","allDots")), List((None,None,""))).asInstanceOf[Seq[ByteBuf]].map(_.array)

    val expected: Seq[Array[Byte]] = Seq(arr.encodeToBarray())
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract deep layer") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val arr2: BsonArray = new BsonArray().add("Day3").add("Day20").add("Day31")
    obj2.put("JoséMonthLeave", arr2)
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(bsonEvent.encodeToBarray()))
    val result = bosonBson.extract(either, List(("JoséMonthLeave","allDots")), List((None,None,""))).asInstanceOf[Seq[ByteBuf]].map(_.array)
    val expected: Seq[Array[Byte]] = Seq(arr2.encodeToBarray())
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract nonexistent key") {
    val bosonBson: BosonImpl = new BosonImpl()
    val either: Either[ByteBuf, String] = Left(Unpooled.copiedBuffer(globalObj.encodeToBarray()))
    assert(Seq() === bosonBson.extract(either, List(("Random","allDots")), List((None,None,""))))
  }
}
