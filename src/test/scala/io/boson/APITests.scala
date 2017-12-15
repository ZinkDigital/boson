package io.boson

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class APITests extends FunSuite {

  val obj1: BsonObject = new BsonObject().put("John", "João").put("Tiago", "Filipe")
  val arr: BsonArray = new BsonArray().add(1).add(obj1).add(2L)

  val validatedByteArray: Array[Byte] = arr.encodeToBarray()

  val validatedByteBuffer: ByteBuffer = ByteBuffer.allocate(validatedByteArray.length)
  validatedByteBuffer.put(validatedByteArray)
  validatedByteBuffer.flip()



  test("extract pos from array with Array[Byte]") {
    val expression: String = "[1 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)

    assertEquals(BsSeq(Seq(Seq(Map("John" -> "João", "Tiago" -> "Filipe"),2))), future.join())
  }

  test("extract pos from array with ByteBuffer") {
    val expression: String = "[1 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(Map("John" -> "João", "Tiago" -> "Filipe"),2))), future.join())
  }

}
