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

  test("MoreKeys 3"){
    val bAux2: BsonObject = new BsonObject().put("google", "DAMMN")
    val bsonArrayEvent1: BsonArray = new BsonArray().add(bAux2).add(bAux2)//.add(bAux2)
    val bAux1: BsonObject = new BsonObject().put("creep", bAux2)
    val bAux: BsonObject = new BsonObject().put("damnnn", bsonArrayEvent1)
    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux)//.add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val expression = "array.[0].damnnn.[1].google" //..last"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))

  }
  test("MoreKeys 2"){
    val bAux2: BsonObject = new BsonObject().put("google", "DAMMN")
    val bsonArrayEvent1: BsonArray = new BsonArray().add(bAux2).add(bAux2)//.add(bAux2)
    val bAux1: BsonObject = new BsonObject().put("creep", bAux2)
    val bAux: BsonObject = new BsonObject().put("damnnn", bsonArrayEvent1)
    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux)//.add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val expression = "array.[0].damnnn.[1].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    println(future.join().getValue.asInstanceOf[Vector[Array[Byte]]].map(entry => new String(entry)))
    val a: Vector[Array[Byte]] = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assertEquals(Vector("DAMMN", "sdfsf", "DAMMN", "DAMMN", "DAMMN", "DAMMN"),a.map(entry => new String(entry))  )

  }

  test("MoreKeys 1"){
    val bAux2: BsonObject = new BsonObject().put("google", "DAMMN")
    val bsonArrayEvent1: BsonArray = new BsonArray().add(bAux2).add(bAux2)//.add(bAux2)
    val bAux1: BsonObject = new BsonObject().put("creep", bAux2)
    val bAux: BsonObject = new BsonObject().put("damnnn", bsonArrayEvent1)
    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux)//.add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val expression = "arr*ay.[0 until 1].damn*n.[0 until 1].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
println(future.join().getValue.asInstanceOf[Vector[Array[Byte]]].map(entry => new String(entry)))
   val a: Vector[Array[Byte]] = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assertEquals(Vector("sdfsf", "DAMMN", "DAMMN", "DAMMN", "DAMMN", "DAMMN"),a.map(entry => new String(entry))  )

  }

  test("MoreKeys"){
    val bAux2: BsonObject = new BsonObject().put("google", "DAMMN")
    val bsonArrayEvent1: BsonArray = new BsonArray().add(bAux2).add(bAux2)
    val bAux1: BsonObject = new BsonObject().put("creep", bAux2)
    val bAux: BsonObject = new BsonObject().put("damnnn", bsonArrayEvent1)
    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val expression = "array.[@damnnn].damnnn.[@google].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    val a: Vector[Array[Byte]] = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assertEquals(Vector("sdfsf", "sdfsf", "sdfsf", "sdfsf", "sdfsf", "sdfsf", "DAMMN"),a.map(entry => new String(entry))  )
    result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
  }
}
