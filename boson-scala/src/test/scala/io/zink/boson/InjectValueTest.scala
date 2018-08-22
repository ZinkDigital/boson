package io.zink.boson

import java.time.Instant
import java.util.concurrent.Executors

import bsonLib.{BsonArray, BsonObject}
import io.vertx.core.json.JsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.junit.Assert.{assertArrayEquals, assertEquals, assertTrue}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class InjectValueTest extends FunSuite {

  test("CodecJson - Top level key inject new value") {

    val bson = new BsonObject().put("age", 17)
    val ex = ".age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    Await.result(future, Duration.Inf)

//    import java.util.concurrent.Executors
//    import scala.concurrent.JavaConversions.asExecutionContext
//    implicit val context = asExecutionContext(Executors.newSingleThreadExecutor())
  }

}
