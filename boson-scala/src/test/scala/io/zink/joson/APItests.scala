package io.zink.joson

import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import de.undercouch.bson4jackson.BsonFactory
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.{JsonArray, JsonObject}
import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonValue.BsValue
import mapper.Mapper
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.{Await, CanAwait, Future}
import scala.concurrent.duration.Duration
/**
  * Created by Ricardo Martins on 22/01/2018.
  */
@RunWith(classOf[JUnitRunner])
class APItests extends FunSuite  {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)

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

  val json = "{\"Store\":{\"Book\":[{\"Title\":\"Java\",\"SpecialEditions\":[{\"Title\":\"JavaMachine\",\"Price\":39}],\"Price\":15.5},{\"Title\":\"Scala\",\"Price\":21.5,\"SpecialEditions\":[{\"Title\":\"ScalaMachine\",\"Price\":40}]},{\"Title\":\"C++\",\"Price\":12.6,\"SpecialEditions\":[{\"Title\":\"C++Machine\",\"Price\":38}]}],\"Hat\":[{\"Price\":48,\"Color\":\"Red\"},{\"Price\":35,\"Color\":\"White\"},{\"Price\":38,\"Color\":\"Blue\"}]}}"

  test("json"){
    //String => Json Object
    val a: JsonObject = new JsonObject(json)
    //Set the Mapper with JsonObject and JsonArray Serializer
    val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
    val os = new ByteArrayOutputStream
    val module = new SimpleModule
    module.addSerializer(classOf[JsonObject],new Joson.JsonObjectSerializer)
    module.addSerializer(classOf[JsonArray], new Joson.JsonArraySerializer)
    mapper.registerModule(module)
    //convert JsonObject
    mapper.writeValue(os, a)
    //convert from byte[] to JsonNode
    val s: JsonNode = mapper.readTree(os.toByteArray)
    os.flush()
    //read and print json received and original json
    println(s.toString)
    println(json)
  }

  test("json == bson") {
    val expression = "Store"
    println("WORK WITH JOSON\n")
    println("|-------- Perform Injection --------|\n")
    val joson: Joson = Joson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("newField!", 100))
      val res: ByteBuf = Mapper.encode(newM)
      if(res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })

    val midResult: Future[String] = joson.go(json)
    val result: String = Await.result(midResult, Duration.Inf)
    println("|-------- Perform Extraction --------|\n")
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson1: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson1.go(result)
    val json1: Array[Byte] = future.join().getValue.asInstanceOf[Vector[Array[Byte]]].head
    println(json1)

    println("WORK WITH BOSON\n")
    println("|-------- Perform Injection --------|\n")
    val validBsonArray: Array[Byte] = bson.encodeToBarray
    val boson: Boson = Boson.injector(expression,(x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("newField!", 100))
      val res: ByteBuf = Mapper.encode(newM)
      if(res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val midResult1: Future[Array[Byte]] = boson.go(validBsonArray)
    //val result1: Array[Byte] = midResult1.join()
    val result1 = Await.result(midResult1, Duration.Inf)
    //val result1: Array[Byte] = midResult1.result(Duration.Inf)
    println("|-------- Perform Extraction --------|\n")
    val future1: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future1.complete(in))
    boson1.go(result1)
    val bson1: Array[Byte] = future1.join().getValue.asInstanceOf[Vector[Array[Byte]]].head
    println(bson1)

    println("|-------- Perform Assertion --------|\n\n")
    assertArrayEquals(json1, bson1)

  }
}

