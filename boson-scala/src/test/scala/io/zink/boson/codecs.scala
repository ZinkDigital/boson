package io.zink.boson

import java.time.Instant

import bsonLib.{BsonArray, BsonObject}
import com.jayway.jsonpath.{Configuration, JsonPath}
import io.vertx.core.json.{JsonArray, JsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.junit.Assert.{assertArrayEquals, assertEquals, assertTrue}

import scala.io.Source




case class Numbers(number0: Int, number1: Int, number2: Int)


@RunWith(classOf[JUnitRunner])
class codecs extends FunSuite{

  //BsonObject as Root
  test("BsonObject Extract . BSON") {
    val jsonStr: String = """{"Epoch":3}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "."
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(validatedByteArray, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract . JSON") {
    val jsonStr: String = """{"Epoch":3}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "."
    val joson = Boson.extractor(expression, (in: String) => {
      assertEquals(json, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch Int BSON") {
    val jsonStr: String = """{"Epoch":3}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch Int JSON") {
    val jsonStr: String = """{"Epoch":3}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch Double BSON") {
    val jsonStr: String = """{"Epoch":3.0}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: Double) => {
      assert(3.0== in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch Double JSON") {
    val jsonStr: String = """{"Epoch":3.0}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Double) => {
      assert(3.0 == in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch long BSON") {
    val jsonStr: String = """{"Epoch":30000000000000000}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: Long) => {
      assert(30000000000000000L== in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch long JSON") {
    val jsonStr: String = """{"Epoch":30000000000000000}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Long) => {

      assert(30000000000000000L == in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch true BSON") {
    val jsonStr: String = """{"Epoch":true}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch true JSON") {
    val jsonStr: String = """{"Epoch":true}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Boolean) => {

      assertTrue( in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }
  
  test("BsonObject Extract Epoch false BSON") {
    val jsonStr: String = """{"Epoch":false}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch false JSON") {
    val jsonStr: String = """{"Epoch":false}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Boolean) => {

      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch null BSON") {
    val jsonStr: String = """{"Epoch":null}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: String) => {
      assert(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch null JSON") {
    val jsonStr: String = """{"Epoch":null}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: String) => {

      assert(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch String BSON") {
    val jsonStr: String = """{"Epoch":"notNull"}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: String) => {
      assert(in.equals("notNull"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch String JSON") {
    val jsonStr: String = """{"Epoch":"notNull"}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: String) => {

      assert(in.equals("notNull"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch byte[] BSON") {
    val bytes = "notNull".getBytes

    val jsonObj: JsonObject = new JsonObject().put("Epoch", bytes)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assert(new String(in).equals("notNull"))
      println(s"in: ${new String(in)}")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch byte[] JSON") {
    val bytes = "notNull".getBytes
    val jsonObj: JsonObject =  new JsonObject().put("Epoch", bytes)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Array[Byte]) => {

      assert(new String(in).equals("notNull"))
      println(s"in: ${new String(in)}")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch Instant BSON") {
    val inst = Instant.now()

    val jsonObj: JsonObject = new JsonObject().put("Epoch", inst)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in:Instant) => {
      assert(in.equals(inst))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch Instant JSON") {
    val inst = Instant.now()
    val jsonObj: JsonObject =  new JsonObject().put("Epoch", inst)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Instant) => {

      assert(in.equals(inst))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch Object BSON") {
    val jsonStr: String = """{"Epoch":{"key":"value"}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in:Array[Byte]) => {
      assertArrayEquals(in,new bsonLib.BsonObject(new JsonObject().put("key", "value")).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch Object JSON") {
    val jsonStr: String = """{"Epoch":{"key":"value"}}"""
    val jsonObj: JsonObject =  new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: String) => {

      assertEquals(in,"""{"key":"value"}""")
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract Epoch Array BSON") {
    val jsonStr: String = """{"Epoch":["key","value"]}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch"
    val boson = Boson.extractor(expression, (in:Array[Byte]) => {
      assertArrayEquals(in,new BsonArray(new JsonArray().add("key").add("value")).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract Epoch Array JSON") {
    val jsonStr: String = """{"Epoch":["key","value"]}"""
    val jsonObj: JsonObject =  new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: String) => {

      assertEquals(in,"""["key","value"]""")
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX Int BSON") {
    val jsonStr: String = """{"Epoch":3, "EpochX":4}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(4, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX Int JSON") {
    val jsonStr: String = """{"Epoch":3,"EpochX":4}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Int) => {
      assertEquals(4, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX Double BSON") {
    val jsonStr: String = """{"Epoch":3.0,"EpochX":4.0}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: Double) => {
      assert(4.0== in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX Double JSON") {
    val jsonStr: String = """{"Epoch":3.0,"EpochX":4.0}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Double) => {
      assert(4.0 == in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX long BSON") {
    val jsonStr: String = """{"Epoch":10000000000000000,"EpochX":30000000000000000}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: Long) => {
      assert(30000000000000000L== in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX long JSON") {
    val jsonStr: String = """{"Epoch":10000000000000000,"EpochX":30000000000000000}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Long) => {

      assert(30000000000000000L == in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX true BSON") {
    val jsonStr: String = """{"Epoch":false,"EpochX":true}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX true JSON") {
    val jsonStr: String = """{"Epoch":false,"EpochX":true}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Boolean) => {

      assertTrue( in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX false BSON") {
    val jsonStr: String = """{"Epoch":true,"EpochX":false}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX false JSON") {
    val jsonStr: String = """{"Epoch":true,"EpochX":false}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Boolean) => {

      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX null BSON") {
    val jsonStr: String = """{"Epoch":null,"EpochX":null}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: String) => {
      assert(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX null JSON") {
    val jsonStr: String = """{"Epoch":null,"EpochX":null}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: String) => {

      assert(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX String BSON") {
    val jsonStr: String = """{"Epoch":"someNull","EpochX":"notNull"}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: String) => {
      assert(in.equals("notNull"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX String JSON") {
    val jsonStr: String = """{"Epoch":"notNull","EpochX":"someNull"}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: String) => {

      assert(in.equals("someNull"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX byte[] BSON") {
    val bytes1 = "notNull".getBytes
    val bytes0 = "someNull".getBytes
    val jsonObj: JsonObject = new JsonObject().put("Epoch", bytes0).put("EpochX", bytes1)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assert(new String(in).equals("notNull"))
      println(s"in: ${new String(in)}")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX byte[] JSON") {
    val bytes1 = "notNull".getBytes
    val bytes0 = "someNull".getBytes
    val jsonObj: JsonObject = new JsonObject().put("Epoch", bytes0).put("EpochX", bytes1)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Array[Byte]) => {

      assert(new String(in).equals("notNull"))
      println(s"in: ${new String(in)}")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX Instant BSON") {
    val inst = Instant.now()
    val inst0 = Instant.now()
    val jsonObj: JsonObject = new JsonObject().put("Epoch", inst0).put("EpochX", inst)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in:Instant) => {
      assert(in.equals(inst))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX Instant JSON") {
    val inst = Instant.now()
    val inst0 = Instant.now()
    val jsonObj: JsonObject =  new JsonObject().put("Epoch", inst0).put("EpochX", inst)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: Instant) => {

      assert(in.equals(inst))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX Object BSON") {
    val jsonStr: String = """{"Epoch":{"key0":"value0"},"EpochX":{"key":"value"}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in:Array[Byte]) => {
      assertArrayEquals(in,new bsonLib.BsonObject(new JsonObject().put("key", "value")).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX Object JSON") {
    val jsonStr: String = """{"Epoch":{"key0":"value0"},"EpochX":{"key":"value"}}"""
    val jsonObj: JsonObject =  new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: String) => {

      assertEquals(in,"""{"key":"value"}""")
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract EpochX Array BSON") {
    val jsonStr: String = """{"Epoch":["key0","value0"],"EpochX":["key","value"]}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".EpochX"
    val boson = Boson.extractor(expression, (in:Array[Byte]) => {
      assertArrayEquals(in,new BsonArray(new JsonArray().add("key").add("value")).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract EpochX Array JSON") {
    val jsonStr: String = """{"Epoch":["key0","value0"],"EpochX":["key","value"]}"""
    val jsonObj: JsonObject =  new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".EpochX"
    val joson = Boson.extractor(expression, (in: String) => {

      assertEquals(in,"""["key","value"]""")
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }
  
  //BsonArray as Root
  test("BsonArray Extract . BSON") {
    val jsonStr: String = """["Epoch",3]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "."
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(validatedByteArray, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract . JSON") {
    val jsonStr: String = """["Epoch",3]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = "."
    val joson = Boson.extractor(expression, (in: String) => {
      assertEquals(json, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch Int BSON") {
    val jsonStr: String = """[3]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch Int JSON") {
    val jsonStr: String = """[3]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch Double BSON") {
    val jsonStr: String = """[3.0]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Double) => {
      assertTrue(3.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch Double JSON") {
    val jsonStr: String = """[3.0]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: Double) => {
      assertTrue(3.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch Long BSON") {
    val jsonStr: String = """[300000000000000000]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Long) => {
      assertTrue(300000000000000000L === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch Long JSON") {
    val jsonStr: String = """[300000000000000000]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: Long) => {
      assertTrue(300000000000000000L === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch true BSON") {
    val jsonStr: String = """[true]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch true JSON") {
    val jsonStr: String = """[true]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch false BSON") {
    val jsonStr: String = """[false]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch false JSON") {
    val jsonStr: String = """[false]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch null BSON") {
    val jsonStr: String = """[null]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch null JSON") {
    val jsonStr: String = """[null]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch String BSON") {
    val jsonStr: String = """["Epoch"]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Epoch"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch String JSON") {
    val jsonStr: String = """["Epoch"]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Epoch"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch byte[] BSON") {
    //val jsonStr: String = """["Epoch"]"""
    val bytes = "someBytes".getBytes()
    val jsonArr: JsonArray = new JsonArray().add(bytes)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, bytes)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch byte[] JSON") {
    val bytes = "someBytes".getBytes()
    val jsonArr: JsonArray = new JsonArray().add(bytes)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, bytes)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch instant BSON") {
    //val jsonStr: String = """["Epoch"]"""
    val instant = Instant.now()
    val jsonArr: JsonArray = new JsonArray().add(instant)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Instant) => {
      assertEquals(in, instant)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch instant JSON") {
    val instant = Instant.now()
    val jsonArr: JsonArray = new JsonArray().add(instant)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in:Instant) => {
      assertEquals(in, instant )
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch object BSON") {
    val jsonStr: String = """[{"Epoch":3}]"""

    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, new BsonObject().put("Epoch",3).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch object JSON") {
    val jsonStr = """[{"Epoch":3}]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in:String) => {
      assertEquals(in, """{"Epoch":3}""" )
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract Epoch array BSON") {
    val jsonStr: String = """[["Epoch",3]]"""

    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[0]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, new BsonArray().add("Epoch").add(3).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract Epoch array JSON") {
    val jsonStr = """[["Epoch",3]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[0]"
    val joson = Boson.extractor(expression, (in:String) => {
      assertEquals(in, """["Epoch",3]""" )
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX Int BSON") {
    val jsonStr: String = """[0,3]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX Int JSON") {
    val jsonStr: String = """[0,3]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX Double BSON") {
    val jsonStr: String = """[0.0,3.0]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Double) => {
      assertTrue(3.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX Double JSON") {
    val jsonStr: String = """[0.0,3.0]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: Double) => {
      assertTrue(3.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX Long BSON") {
    val jsonStr: String = """[100000000000000000,300000000000000000]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Long) => {
      assertTrue(300000000000000000L === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX Long JSON") {
    val jsonStr: String = """[100000000000000000,300000000000000000]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: Long) => {
      assertTrue(300000000000000000L === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX true BSON") {
    val jsonStr: String = """[false,true]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX true JSON") {
    val jsonStr: String = """[false,true]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX false BSON") {
    val jsonStr: String = """[true,false]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX false JSON") {
    val jsonStr: String = """[true,false]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: Boolean) => {
      assertTrue(!in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX null BSON") {
    val jsonStr: String = """[null,null]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX null JSON") {
    val jsonStr: String = """[null,null]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Null"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX String BSON") {
    val jsonStr: String = """["notEpoch","Epoch"]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Epoch"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX String JSON") {
    val jsonStr: String = """["notEpoch","Epoch"]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: String) => {
      assertTrue(in.equals("Epoch"))
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX byte[] BSON") {
    //val jsonStr: String = """["Epoch"]"""
    val bytes = "someBytes".getBytes()
    val bytes0 = "noneBytes".getBytes()
    val jsonArr: JsonArray = new JsonArray().add(bytes0).add(bytes)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, bytes)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX byte[] JSON") {
    val bytes = "someBytes".getBytes()
    val bytes0 = "noneBytes".getBytes()
    val jsonArr: JsonArray = new JsonArray().add(bytes0).add(bytes)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, bytes)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX instant BSON") {
    //val jsonStr: String = """["Epoch"]"""
    val instant = Instant.now()
    val instant0 = Instant.now()
    val jsonArr: JsonArray = new JsonArray().add(instant0).add(instant)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Instant) => {
      assertEquals(in, instant)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX instant JSON") {
    val instant = Instant.now()
    val instant0 = Instant.now()
    val jsonArr: JsonArray = new JsonArray().add(instant0).add(instant)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in:Instant) => {
      assertEquals(in, instant )
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX object BSON") {
    val jsonStr: String = """[{"Some":0,"None":1},{"Epoch":3}]"""

    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, new BsonObject().put("Epoch",3).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX object JSON") {
    val jsonStr = """[{"Some":0,"None":1},{"Epoch":3}]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in:String) => {
      assertEquals(in, """{"Epoch":3}""" )
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract EpochX array BSON") {
    val jsonStr: String = """[["Some",0],["Epoch",3]]"""

    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(in, new BsonArray().add("Epoch").add(3).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract EpochX array JSON") {
    val jsonStr = """[["Some",0],["Epoch",3]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1]"
    val joson = Boson.extractor(expression, (in:String) => {
      assertEquals(in, """["Epoch",3]""" )
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  //Case class
  test("BsonArray Extract Epoch Case class BSON") {
    val jsonStr: String = """{"numbers":{"number0":0,"number1":1,"number2":2}}"""
    val jsonArr: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".numbers"
    val boson = Boson.extractor(expression, (in: Numbers) => {
      assertEquals(Numbers(0,1,2), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }

//  test("BsonArray Extract Epoch Case class JSON") {
//    val jsonStr: String = """{"numbers":{"number0":0,"number1":1,"number2":2}}"""
//    val jsonArr: JsonObject = new JsonObject(jsonStr)
//    val json: String = jsonArr.encode()
//
//    val expression: String = ".numbers"
//    val boson = Boson.extractor(expression, (in: Numbers) => {
//      assertEquals(Numbers(0,1,2), in)
//      println(s"in: $in")
//      println("APPLIED")
//    })
//    val res = boson.go(json)
//    Await.result(res, Duration.Inf)
//  }

  // 2 keys BsonObject
  test("BsonObject Extract 2keys Int BSON") {
    val jsonStr: String = """{"field":{"Epoch":3}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys Int JSON") {
    val jsonStr: String = """{"field":{"Epoch":3}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Int) => {
      assertEquals(3, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys Double BSON") {
    val jsonStr: String = """{"field":{"Epoch":3.0}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Double) => {
      assertTrue(3.0===in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys Double JSON") {
    val jsonStr: String = """{"field":{"Epoch":3.0}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Double) => {
      assertTrue(3.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys Long BSON") {
    val jsonStr: String = """{"field":{"Epoch":3000000000000000}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Long) => {
      assertEquals(3000000000000000L, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys Long JSON") {
    val jsonStr: String = """{"field":{"Epoch":3000000000000000}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Long) => {
      assertEquals(3000000000000000L, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys true BSON") {
    val jsonStr: String = """{"field":{"Epoch":true}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertEquals(true, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys true JSON") {
    val jsonStr: String = """{"field":{"Epoch":true}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Boolean) => {
      assertEquals(true, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys false BSON") {
    val jsonStr: String = """{"field":{"Epoch":false}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Boolean) => {
      assertEquals(false, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys false JSON") {
    val jsonStr: String = """{"field":{"Epoch":false}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Boolean) => {
      assertEquals(false, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys null BSON") {
    val jsonStr: String = """{"field":{"Epoch":null}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: String) => {
      assertEquals("Null", in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys null JSON") {
    val jsonStr: String = """{"field":{"Epoch":null}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: String) => {
      assertEquals("Null", in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys string BSON") {
    val jsonStr: String = """{"field":{"Epoch":"string"}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: String) => {
      assertEquals("string", in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys string JSON") {
    val jsonStr: String = """{"field":{"Epoch":"string"}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: String) => {
      assertEquals("string", in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys byte[] BSON") {
    val jsonStr: Array[Byte] = "string"getBytes()
    val json0: JsonObject = new JsonObject().put("Epoch", jsonStr)
    val bson0: BsonObject = new BsonObject(json0)
    val bson: BsonObject = new BsonObject().put("field",bson0 )
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(jsonStr, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys byte[] JSON") {
    val jsonStr: Array[Byte] = "string".getBytes()
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", jsonStr))
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(jsonStr, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys instant BSON") {
    val jsonStr: Instant = Instant.now()
    val json0: JsonObject =  new JsonObject().put("Epoch", jsonStr)
    val bson0: BsonObject = new BsonObject(json0)
    val bson: BsonObject = new BsonObject().put("field", bson0)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch"
    val boson = Boson.extractor(expression, (in: Instant) => {
      assertEquals(jsonStr, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys instant JSON") {
    val jsonStr: Instant = Instant.now()
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch",jsonStr ))
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch"
    val joson = Boson.extractor(expression, (in: Instant) => {
      assertEquals(jsonStr, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys Object BSON") {
    val jsonStr: String = """{"Epoch":{"key":{"value":3}}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch.key"
    val boson = Boson.extractor(expression, (in:Array[Byte]) => {
      assertArrayEquals(in,new bsonLib.BsonObject(new JsonObject().put("value", 3)).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys Object JSON") {
    val jsonStr: String = """{"Epoch":{"key":{"value":3}}}"""
    val jsonObj: JsonObject =  new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch.key"
    val joson = Boson.extractor(expression, (in: String) => {

      assertEquals(in,"""{"value":3}""")
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract 2keys Array BSON") {
    val jsonStr: String = """{"Epoch":{"field":["key","value"]}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".Epoch.field"
    val boson = Boson.extractor(expression, (in:Array[Byte]) => {
      assertArrayEquals(in,new BsonArray(new JsonArray().add("key").add("value")).encodeToBarray())
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract 2keys Array JSON") {
    val jsonStr: String = """{"Epoch":{"field":["key","value"]}}"""
    val jsonObj: JsonObject =  new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = ".Epoch.field"
    val joson = Boson.extractor(expression, (in: String) => {

      assertEquals(in,"""["key","value"]""")
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  //2keys BsonArray
  test("BsonArray Extract 2keys Int BSON") {
    val jsonStr: String = """[[0],[1]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1].[0]"
    val boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(1, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract 2keys Int JSON") {
    val jsonStr: String = """[[0],[1]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1].[0]"
    val joson = Boson.extractor(expression, (in: Int) => {
      assertEquals(1, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract 2keys Double BSON") {
    val jsonStr: String = """[[0.0],[1.0]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1].[0]"
    val boson = Boson.extractor(expression, (in: Double) => {
      assertTrue(1.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract 2keys Double JSON") {
    val jsonStr: String = """[[0.0],[1.0]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1].[0]"
    val joson = Boson.extractor(expression, (in: Double) => {
      assertTrue(1.0 === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract 2keys Long BSON") {
    val jsonStr: String = """[[100000000000000000],[300000000000000000]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1].[0]"
    val boson = Boson.extractor(expression, (in: Long) => {
      assertTrue(300000000000000000L === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract 2keys Long JSON") {
    val jsonStr: String = """[[100000000000000000],[300000000000000000]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1].[0]"
    val joson = Boson.extractor(expression, (in: Long) => {
      assertTrue(300000000000000000L === in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract 2keys object BSON") {
    val jsonStr: String = """[[{"field1":1}],[{"field2":2}]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1].[0]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(new BsonObject().put("field2", 2).encodeToBarray(), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract 2keys object JSON") {
    val jsonStr: String = """[[{"field1":1}],[{"field2":2}]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1].[0]"
    val joson = Boson.extractor(expression, (in: String) => {
      assertEquals("""{"field2":2}""", in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonArray Extract 2keys array BSON") {
    val jsonStr: String = """[[["field1",1]],[["field2",2]]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val bson: BsonArray = new BsonArray(jsonArr)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".[1].[0]"
    val boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(new BsonArray().add("field2").add(2).encodeToBarray(), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonArray Extract 2keys array JSON") {
    val jsonStr: String = """[[["field1",1]],[["field2",2]]]"""
    val jsonArr: JsonArray = new JsonArray(jsonStr)
    val json: String = jsonArr.encode()

    val expression: String = ".[1].[0]"
    val joson = Boson.extractor(expression, (in: String) => {
      assertEquals("""["field2",2]""", in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  // doubledot '..'
  test("BsonObject Extract doubledot Int BSON") {
    val jsonStr: String = """{"field":{"Epoch":3}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Int]) => {
      assertEquals(Seq(3), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot Int JSON") {
    val jsonStr: String = """{"field":{"Epoch":3}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Int]) => {
      assertEquals(Seq(3), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot Double BSON") {
    val jsonStr: String = """{"field":{"Epoch":3.0}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Double]) => {
      assertEquals(Seq(3.0), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot Double JSON") {
    val jsonStr: String = """{"field":{"Epoch":3.0}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Double]) => {
      assertEquals(Seq(3.0), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot Long BSON") {
    val jsonStr: String = """{"field":{"Epoch":300000000000000000}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Long]) => {
      assertEquals(Seq(300000000000000000L), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot Long JSON") {
    val jsonStr: String = """{"field":{"Epoch":300000000000000000}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Long]) => {
      assertEquals(Seq(300000000000000000L), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot true BSON") {
    val jsonStr: String = """{"field":{"Epoch":true}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Boolean]) => {
      assertEquals(Seq(true), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot true JSON") {
    val jsonStr: String = """{"field":{"Epoch":true}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Boolean]) => {
      assertEquals(Seq(true), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot false BSON") {
    val jsonStr: String = """{"field":{"Epoch":false}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Boolean]) => {
      assertEquals(Seq(false), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot false JSON") {
    val jsonStr: String = """{"field":{"Epoch":false}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Boolean]) => {
      assertEquals(Seq(false), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot null BSON") {
    val jsonStr: String = """{"field":{"Epoch":null}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(Seq("Null"), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot null JSON") {
    val jsonStr: String = """{"field":{"Epoch":null}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(Seq("Null"), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot string BSON") {
    val jsonStr: String = """{"field":{"Epoch":"Value"}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(Seq("Value"), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot string JSON") {
    val jsonStr: String = """{"field":{"Epoch":"Value"}}"""
    val jsonObj: JsonObject = new JsonObject(jsonStr)
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(Seq("Value"), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot byte[] BSON") {
    val bytes = "Value".getBytes()
    //val jsonStr: String = """{"field":{"Epoch":null}}"""
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", bytes))
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertArrayEquals(bytes, in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot byte[] JSON") {
    val bytes = "Value".getBytes()
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", bytes))
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertArrayEquals(bytes, in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot instant BSON") {
    val bytes = Instant.now()
    //val jsonStr: String = """{"field":{"Epoch":null}}"""
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", bytes))
    val bson: BsonObject = new BsonObject(jsonObj)
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Instant]) => {
      assertEquals(bytes, in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot instant JSON") {
    val bytes = Instant.now()
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", bytes))
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[Instant]) => {
      assertEquals(bytes, in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot object BSON") {

    val bson: BsonObject = new BsonObject().put("field", new bsonLib.BsonObject().put("Epoch", new bsonLib.BsonObject().put("newO","newV")))
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertArrayEquals( new bsonLib.BsonObject().put("newO","newV").encodeToBarray(), in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot object JSON") {
    val bytes = new JsonObject().put("newObj", "newValue")
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", new JsonObject().put("newObj", "newValue")))
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(bytes.encode(), in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  test("BsonObject Extract doubledot array BSON") {

    val bson: BsonObject = new BsonObject().put("field", new bsonLib.BsonObject().put("Epoch", new BsonArray().add(3).add("three")))
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = "..Epoch"
    val boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assertArrayEquals(new BsonArray().add(3).add("three").encodeToBarray(), in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("BsonObject Extract doubledot array JSON") {
    val bytes = new JsonArray().add(3).add("three")
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", new JsonArray().add(3).add("three")))
    val json: String = jsonObj.encode()

    val expression: String = "..Epoch"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(bytes.encode(), in.head)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  // testes de arrays
  test("Arrays Extract [0 to 2] BSON") {

    val bson: BsonObject = new BsonObject().put("field", new bsonLib.BsonObject().put("Epoch", new BsonArray().add(3).add("three").add(true)))
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field.Epoch.[0 to 2]"
    val boson = Boson.extractor(expression, (in: Seq[Any]) => {
      println(s"in: $in")
      assertEquals(Seq(3,"three",true), in)

      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("Arrays Extract [0 to 2] JSON") {
    val bytes = new JsonArray().add(3).add("three")
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonObject().put("Epoch", new JsonArray().add(3).add("three").add(true)))
    val json: String = jsonObj.encode()

    val expression: String = ".field.Epoch.[0 to 2]"
    val joson = Boson.extractor(expression, (in: Seq[Any]) => {
      println(s"in: $in")
      assertEquals(Seq(3,"three",true), in)
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  // testes @ HasElem
  test("@ Extract [@Epoch] BSON") {

    val bson: BsonObject = new BsonObject().put("field", new BsonArray().add(new bsonLib.BsonObject().put("Epoch", new BsonArray().add(3).add("three").add(true))))
    val validatedByteArray: Array[Byte] = bson.encodeToBarray()

    val expression: String = ".field[@Epoch]"
    val boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      println(s"in: $in")
      assertArrayEquals( new bsonLib.BsonObject().put("Epoch", new BsonArray().add(3).add("three").add(true)).encodeToBarray(), in.head)

      println("APPLIED")
    })
    val res = boson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }
  test("@ Extract [@Epoch] JSON") {
    val bytes = new JsonArray().add(3).add("three")
    val jsonObj: JsonObject = new JsonObject().put("field", new JsonArray().add(new JsonObject().put("Epoch", new JsonArray().add(3).add("three").add(true))))
    val json: String = jsonObj.encode()

    val expression: String = ".field[@Epoch]"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      println(s"in: $in")
      assertEquals(new JsonObject().put("Epoch", new JsonArray().add(3).add("three").add(true)).encode(), in.head)
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val jsonStr: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val jsonObj: JsonObject = new JsonObject(jsonStr)
  val json: String = jsonObj.encode()
  val bson: BsonObject = new BsonObject(jsonObj)
  val validatedByteArray: Array[Byte] = bson.encodeToBarray()


  test("jsonOutPut Epoch BSON") {
    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Int) => {
      println(s"in: $in")
      assertEquals(3, in)
      println("APPLIED")
    })
    val res = joson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }

  test("jsonOutPut Epoch JSON") {
    val expression: String = ".Epoch"
    val joson = Boson.extractor(expression, (in: Int) => {
      println(s"in: $in")
      assertEquals(3, in)
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }


  test("jsonOutPut SSLNLastName BSON") {
    val expression: String = "..SSLNLastName"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      println(s"in: $in")
      assertEquals("de Huanuco", in.head)
      println("APPLIED")
    })
    val res = joson.go(validatedByteArray)
    Await.result(res, Duration.Inf)
  }

  test("jsonOutPut SSLNLastName JSON") {
    val expression: String = "..SSLNLastName"
    val joson = Boson.extractor(expression, (in: Seq[String]) => {
      println(s"in: $in")
      assertEquals("de Huanuco", in.head)
      println("APPLIED")
    })
    val res = joson.go(json)
    Await.result(res, Duration.Inf)
  }


}

