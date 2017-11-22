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
class LongInputTests extends FunSuite {

  val sI: ScalaInterface = new ScalaInterface

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  test("extract top field") {
    val netty: NettyBson = sI.createNettyBson(bson.encode().getBytes)
    val result: BsValue = sI.parse(netty, "Epoch", "first")
    assert(bson.getInteger("Epoch") === result.asInstanceOf[BsSeq].value.head)
  }

}
