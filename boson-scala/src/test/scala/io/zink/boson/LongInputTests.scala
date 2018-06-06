package io.zink.boson

import bsonLib.BsonObject
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.JsonObject
import io.zink.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
@RunWith(classOf[JUnitRunner])
class LongInputTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  val boson: BosonImpl = new BosonImpl()

  test("extract top field") {
    val expression: String = ".Epoch"
    val boson: Boson = Boson.extractor(expression, (out: Int) => {
      assertTrue(3 == out)
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("extract bottom field") {
    val expression: String = "SSLNLastName"
    val expected: String = "de Huanuco"
    val boson: Boson = Boson.extractor(expression, (out: String) => {
      assertTrue(expected.zip(out).forall(e => e._1.equals(e._2)))
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("extract positions of an Array") {
    val expression: String = "Markets[3 to 5]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => {
      mutableBuffer += out
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(3, mutableBuffer.size)
  }

  test("extract further positions of an Array") {
    val expression: String = "Markets[50 to 55]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Array[Byte]) => {
      mutableBuffer += out
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(6, mutableBuffer.size)
  }

  test("size of all occurrences of Key") {
    val expression: String = "Price"
    val mutableBuffer: ArrayBuffer[Float] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (out: Float) => {
      mutableBuffer += out
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(195, mutableBuffer.size)
  }

}
