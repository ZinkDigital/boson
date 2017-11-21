package io.boson

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue.{BsNumber, BsSeq, BsValue}
import io.boson.nettybson.NettyBson
import io.boson.scalaInterface.ScalaInterface
import io.vertx.core.json.JsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
@RunWith(classOf[JUnitRunner])
class PerformanceTests extends FunSuite {

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }


  val sI: ScalaInterface = new ScalaInterface

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  println(bson)

  test("extract field in beggining with first") {

    val netty: NettyBson = sI.createNettyBson(bson.encode().getBytes)
    val result = time { sI.parse(netty, "Epoch", "first") }
    println(bson.getInteger("Epoch"))
    println(result)
    assert(bson.getInteger("Epoch") === result.asInstanceOf[BsSeq].value.head)

  }

}
