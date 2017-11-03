package io.boson
import java.time.Instant

import io.boson.nettybson.NettyBson
import io.boson.bson.{BsonArray, BsonObject}
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
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    assert(1500 === nettyBson.extract(nettyBson.getByteBuf, "Américo", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Long") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    assert(1250L === nettyBson.extract(nettyBson.getByteBuf, "Pedro", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Boolean") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    assert(false === nettyBson.extract(nettyBson.getByteBuf, "José", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Null") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    assert("Null" === nettyBson.extract(nettyBson.getByteBuf, "Amadeu", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract Instant") {
    println(s"globalObj -> $globalObj")
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))
    assertEquals(now.toString, new String(nettyBson.extract(nettyBson.getByteBuf, "UpdatedOn", "first").get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]]).replaceAll("\\p{C}", ""))
  }

  test("Extract String") {
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))
    assertEquals("Lisboa", new String(nettyBson.extract(nettyBson.getByteBuf, "Residence", "first")
      .get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]]).replaceAll("\\p{C}", ""))
  }

  test("Extract Array[Byte] w/ Netty") {
    val help: ByteBuf = Unpooled.buffer()
    val finalBuf: ByteBuf = Unpooled.buffer()

    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))

    help.writeBytes(nettyBson.extract(nettyBson.getByteBuf, "FavoriteSentence", "first").get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]])
    finalBuf.writeBytes(io.netty.handler.codec.base64.Base64.decode(help))
    assert("Be the best".getBytes === new String(finalBuf.array()).replaceAll("\\p{C}", "").getBytes)
  }

  test("Extract Array[Byte] w/ String") {
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))
    assert("Be the best".getBytes === new String(java.util.Base64.getMimeDecoder
      .decode(new String(nettyBson.extract(nettyBson.getByteBuf, "FavoriteSentence", "first")
        .get.asInstanceOf[List[Any]].head.asInstanceOf[Array[Byte]]))).getBytes)
  }

  test("Extract BsonObject") {
    val bsonEvent: BsonObject = new BsonObject().put("First", obj1).put("Second", obj2).put("Third", obj3)
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    assert(obj2 === nettyBson.extract(nettyBson.getByteBuf, "Second", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract BsonArray") {
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))
    assert(arr === nettyBson.extract(nettyBson.getByteBuf, "Colleagues", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract deep layer") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val arr2: BsonArray = new BsonArray().add("Day3").add("Day20").add("Day31")
    obj2.put("JoséMonthLeave", arr2)
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    assert(arr2 === nettyBson.extract(nettyBson.getByteBuf, "JoséMonthLeave", "first").get.asInstanceOf[List[Any]].head)
  }

  test("Extract nonexistent key") {
    val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))
    assert(None === nettyBson.extract(nettyBson.getByteBuf, "Random", "first"))
  }
}
