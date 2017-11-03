package io.boson.bsonPath

import io.boson.nettybson.NettyBson
import io.boson.bson.{BsonArray, BsonObject}

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
      val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).add(br4)
      val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
      val arrTest: BsonArray = new BsonArray().add(2.2).add(2.4).add(2.6)
      val netty: NettyBson = new NettyBson(vertxBuff = Option(arrTest.encode()))
      val key: String = ""
      val language: String = "[0 to end]"
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
