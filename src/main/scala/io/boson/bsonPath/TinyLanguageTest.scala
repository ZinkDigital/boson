package io.boson.bsonPath

import io.boson.nettyboson.Boson
import io.boson.bson.{BsonArray, BsonObject}
import io.vertx.core.json.{JsonArray, JsonObject}

/**
  * Created by Tiago Filipe on 03/11/2017.
  */
object TinyLanguageTest {

    def main(args: Array[String]) = {
      val br4: BsonArray = new BsonArray().add("Insecticida")
      val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
      val obj1: BsonObject = new BsonObject().put("José", br1)
      val br2: BsonArray = new BsonArray().add("Spider")
      val obj2: BsonObject = new BsonObject().put("José", br2)
      val br3: BsonArray = new BsonArray().add("Fly")
      val obj3: BsonObject = new BsonObject().put("José", br3)

      val json: String = """
  {
    "name" : "Watership Down",
    "location" : {
      "lat" : 51.235685,
      "long" : -1.309197
    },
    "residents" : [ {
      "name" : "Fiver",
      "age" : 4,
      "role" : null
    }, {
      "name" : "Bigwig",
      "age" : 6,
      "role" : "Owsla"
    } ]
  }
  """
      val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).add(br4)
      val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)

      val otherJsonObject: JsonObject = bsonEvent.asJson()
      println(s"otherJsonObject: $otherJsonObject")
      val otherJsonArray: JsonArray = arr.asJson()
      println(s"otherJsonArray: $otherJsonArray")

      val otherBsonObject: BsonObject = new BsonObject().asBson(otherJsonObject)
      println(s"otherBsonObject: $otherBsonObject")
      val otherBsonArray: BsonArray = new BsonArray().asBson(otherJsonArray)
      println(s"otherBsonArray: $otherBsonArray")


      val arrTest: BsonArray = new BsonArray().add(2.2).add(2.4).add(2.6)
      val netty: Boson = new Boson(byteArray = Option(bsonEvent.encode().getBytes))
      val key: String = "José"
      val language: String = "[0 to end] size"
      val parser = new TinyLanguage
      parser.parseAll(parser.program, language) match {
        case parser.Success(r, _) =>
          val interpreter = new Interpreter(netty, key, r.asInstanceOf[Program])
          try {
            println("SUCCESS: " + interpreter.run())
          } catch {
            case e: RuntimeException => println("Error inside run() " + e.getMessage)
          }
        case parser.Error(msg, _) => println("Error parsing: " + msg)
        case parser.Failure(msg, _) => println("Failure parsing: " + msg)
      }
    }

}
