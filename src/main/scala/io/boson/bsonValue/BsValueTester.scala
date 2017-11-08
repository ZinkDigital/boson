package io.boson.bsonValue

import io.boson.bson.BsonObject

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
object BsValueTester {

  //  val br4: BsonArray = new BsonArray().add("Insecticida")
  //  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
  val obj1: BsonObject = new BsonObject().put("José", 50)
  //  val br2: BsonArray = new BsonArray().add("Spider")
  //  val obj2: BsonObject = new BsonObject().put("José", br2)
  //  val br3: BsonArray = new BsonArray().add("Fly")
  //  val obj3: BsonObject = new BsonObject().put("José", br3)
  //  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).add(br4)
  val bsonEvent: BsonObject = new BsonObject().put("StartUp1", obj1)

  def main(args: Array[String]): Unit = {

    val bsonNumber: BsValue = BsObject.toBson(2)
    val bsonBoolean: BsValue = BsObject.toBson(true)
    val bsonSeq: BsValue = BsObject.toBson(Seq(1,2,3))


    println(s"bsonNumber: $bsonNumber, bsonBoolean: $bsonBoolean, bsonSeq: $bsonSeq")
    println(s"bsonEvent: $bsonEvent")
  }

}
