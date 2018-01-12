package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.{BosonImpl, BosonInjector}
import io.boson.bson.bsonValue._
import mapper.Mapper
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class injectorAPITests extends FunSuite {


  test("MoreKeys"){
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bAux1: BsonObject = new BsonObject().put("creep", "DAMMN")
    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    val expression = "a*rray.dam*nnn"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val boson: Boson = Boson.injector(expression, (in: List[Any]) => in.:+(Mapper.convertBsonObject(new BsonObject().put("WHAT!!!", 10))))
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

    val result: Array[Byte] = midResult.join()
  }
}
