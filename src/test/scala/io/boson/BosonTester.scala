package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson

object BosonTester extends App {

  val obj1: BsonObject = new BsonObject().put("John", "João").put("Tiago", "Filipe")
  val arr: BsonArray = new BsonArray().add(1).add(obj1).add(2L)


  val validatedByteArray: Array[Byte] = arr.encodeToBarray()

  val expression: String = "[1 until 3]"

  val future: CompletableFuture[Map[String, Any]] = new CompletableFuture[Map[String, Any]]()

  val boson: Boson = Boson.extractor(expression, (in: Map[String, Any]) => future.complete(in))

  boson.go(validatedByteArray)

  println(s"result: ${future.join()}")


}
