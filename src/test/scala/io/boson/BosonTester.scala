package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.Constants._
import io.boson.bson.bsonValue.BsValue
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ByteProcessor

import scala.util.{Failure, Success, Try}

object BosonTester extends App {
  def tester[T](f: T => T): Any = {
    val double: T = 2.5.asInstanceOf[T]
    Try(f(double)) match {
      case Success(v) =>
        println("value selected has same type as provided")
        v
      case Failure(m) =>
        println("value selected DOESNT MATCH with the provided")
        throw new RuntimeException(m)
    }
  }

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }

  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)

  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)


  //val newField: Double = 3.4
  //println(s"value -> ${tester[Double]((_: Double) => newField )}")

  //val arr: BsonArray = new BsonArray().add(1.1.toFloat).add(2.2).add("END")
  //val validatedByteArray: Array[Byte] = arr.encodeToBarray()
  //val obj1: BsonObject = new BsonObject().put("string", "Hi").put("bytearray", "ola".getBytes)
  val validatedByteArray2: Array[Byte] = arr.encodeToBarray()
  val validatedByteArray: Array[Byte] = bsonEvent.encodeToBarray()
  //val buffer: ByteBuf = Unpooled.copiedBuffer(validatedByteArray2)
  //buffer.forEachByte(bP)
  //  println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  //  println("totalSize: "+buffer.readIntLE())
  //  println("type: " + buffer.readByte())
  //  println("key: " + buffer.readCharSequence(6,charset))
  //  //buffer.forEachByte(bP)
  //  println("value: " + buffer.readCharSequence(3,charset))
  //  println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  //  val expression: String = "[0 to end]"
  //  val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  //  val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
  //  boson.go(validatedByteArray)
  //  println("result of extracting \""+ expression+ "\" -> " + future.join())
  //  println("-------------------------------------------------------------------------------------------------------------")
  //  val expression2: String = "all"
  //  val boson2: Boson = Boson.injector(expression2, (_:String) => "Hi!!!")
  //  val result: CompletableFuture[Array[Byte]] = boson2.go(validatedByteArray2)
  //  println("result of extracting \""+ expression2+ "\" -> " + result.join())

  //  println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
  //  val buf: ByteBuf = Unpooled.copiedBuffer(result.join())
  //  println("totalSize: "+buf.readIntLE())
  //  println("type: " + buf.readByte())
  //  println("key: " + buf.readCharSequence(6,charset))
  //  //buf.forEachByte(bP)
  //  println("value: " + buf.readDoubleLE())
  //  println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")

  val expression: String = "fridgeTemp.all"
  val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
  boson.go(validatedByteArray)
  println("result of extracting \"" + expression + "\" -> " + future.join())
  println("-------------------------------------------------------------------------------------------------------------")
  val expression2: String = "fridgeReadings.[1 to 1]"
  val future2: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  val boson2: Boson = Boson.extractor(expression2, (in: BsValue) => future2.complete(in))
  boson2.go(validatedByteArray)
  println("result of extracting \"" + expression2 + "\" -> " + future2.join())
  println("-------------------------------------------------------------------------------------------------------------")
  val expression3: String = "[1 to 1]"
  val future3: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  val boson3: Boson = Boson.extractor(expression3, (in: BsValue) => future3.complete(in))
  boson3.go(validatedByteArray2)
  println("result of extracting \"" + expression3 + "\" -> " + future3.join())
}
