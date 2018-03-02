package io.zink.boson

import bsonLib.BsonObject
import io.vertx.core.json.JsonObject
import org.scalatest.{FlatSpec, Matchers}


class StreamerTest extends FlatSpec with Matchers {


  val json : String = """  {"Id":"86_7my18RureglZl5ScWb3VRYDA","Tags":{"type":"total_goals_over/under"}}"""
  val bson : Array[Byte] = new BsonObject(new JsonObject(json)).encodeToBarray()


  "Streamers" should " construct with correct types" in {

      val jStream = JsonStreamer(json)
      val bStream = BsonStreamer( bson)
      // corectly doesnt compile
      // JsonStreamer(bson)
      // BsonStreamer(json)
  }

  it should "read the anon object" in {
    val jStream = JsonStreamer(json)
    jStream.readNamedType(SonEnd()) equals SonObject("")

    val bStream = BsonStreamer( bson)
    bStream.readNamedType(SonEnd()) equals SonObject("")

  }





}
