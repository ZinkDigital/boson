package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.Dictionary._
import io.boson.bson.bsonValue.BsValue
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.{ByteProcessor, ResourceLeakDetector}

import scala.util.{Failure, Success, Try}

object BosonTester extends App {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
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

  /*val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }*/

  val obj1: BsonObject = new BsonObject().put("fridgeTemp111", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)
  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)
  //----------------------------------------------------------------------------------------------------------------------------
//  val alternativeBsonObj1: BsonObject = new BsonObject().put("fridgeReadings", obj1)
//  val alternativeBsonObj2: BsonObject = new BsonObject().put("fridgeReadings", obj2)
//  val alternativeBsonObj3: BsonObject = new BsonObject().put("fridgeReadings", obj3)
//  val alternativeBsonArr: BsonArray = new BsonArray().add(alternativeBsonObj1).add(alternativeBsonObj2).add(alternativeBsonObj3)
//  val validatedByteArray3: Array[Byte] = alternativeBsonArr.encodeToBarray()
  //----------------------------------------------------------------------------------------------------------------------------
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

//  val expression: String = "fridgeTemp.all"
//  val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//  boson.go(validatedByteArray)
//  println("result of extracting \"" + expression + "\" -> " + future.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression2: String = "fridgeReadings.[1 to 1]"
//  val future2: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson2: Boson = Boson.extractor(expression2, (in: BsValue) => future2.complete(in))
//  boson2.go(validatedByteArray)
//  println("result of extracting \"" + expression2 + "\" -> " + future2.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression3: String = "[1 to 1]"
//  val future3: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson3: Boson = Boson.extractor(expression3, (in: BsValue) => future3.complete(in))
//  boson3.go(validatedByteArray2)
//  println("result of extracting \"" + expression3 + "\" -> " + future3.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression4: String = "first"
//  val future4: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson4: Boson = Boson.extractor(expression4, (in: BsValue) => future4.complete(in))
//  boson4.go(validatedByteArray2)
//  println("result of extracting \"" + expression4 + "\" -> " + future4.join())
//  println()
//  println()
//  println()
////  println("-------------------------------------------------------------------------------------------------------------")
////  val expression5: String = "fridgeReadings.first.fridgeTemp"
////  val future5: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
////  val boson5: Boson = Boson.extractor(expression5, (in: BsValue) => future5.complete(in))
////  boson5.go(validatedByteArray3)
////  println("result of extracting \"" + expression5 + "\" -> " + future5.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression6: String = "fridgeReadings.[1]"
//  val future6: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson6: Boson = Boson.extractor(expression6, (in: BsValue) => future6.complete(in))
//  boson6.go(validatedByteArray)
//  println("result of extracting \"" + expression6 + "\" -> " + future6.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression7: String = "[1]"
//  val future7: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson7: Boson = Boson.extractor(expression7, (in: BsValue) => future7.complete(in))
//  boson7.go(validatedByteArray2)
//  println("result of extracting \"" + expression7 + "\" -> " + future7.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression8: String = "fridgeReadings.[0 to end].fanVelocity"
//  val future8: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson8: Boson = Boson.extractor(expression8, (in: BsValue) => future8.complete(in))
//  boson8.go(validatedByteArray)
//  println("result of extracting \"" + expression8 + "\" -> " + future8.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression9: String = "fridgeReadings.[1].fanVelocity"
//  val future9: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson9: Boson = Boson.extractor(expression9, (in: BsValue) => future9.complete(in))
//  boson9.go(validatedByteArray)
//  println("result of extracting \"" + expression9 + "\" -> " + future9.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression10: String = "[1].fanVelocity"
//  val future10: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson10: Boson = Boson.extractor(expression10, (in: BsValue) => future10.complete(in))
//  boson10.go(validatedByteArray2)
//  println("result of extracting \"" + expression10 + "\" -> " + future10.join())
//  println("-------------------------------------------------------------------------------------------------------------")
//  val expression11: String = "[1 to end].fanVelocity"
//  val future11: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson11: Boson = Boson.extractor(expression11, (in: BsValue) => future11.complete(in))
//  boson11.go(validatedByteArray2)
//  println("result of extracting \"" + expression11 + "\" -> " + future11.join())
//
//  println("-------------------------------------------------------------------------------------------------------------")
//  val a: BsonArray = new BsonArray().add(1).add(new BsonObject().put("one", "bla")).add(3)
//  val b: BsonObject = new BsonObject().put("one", 1).put("two", a).put("three", 3)
//  val expression12: String = "[0 until end]"
//  val future12: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//  val boson12: Boson = Boson.extractor(expression12, (in: BsValue) => future12.complete(in))
//  boson12.go(a.encodeToBarray())
//  println("result of extracting \"" + expression12 + "\" -> " + future12.join())

  val bson: BsonObject = new BsonObject().put("obj", new BsonObject().put("one", 1))
  val bA: Array[Byte] = bson.encodeToBarray()

//    val expression: String = ".obj"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//    boson.go(bA)
//    println("result of extracting \""+ expression+ "\" -> " + future.join())
//    println("-------------------------------------------------------------------------------------------------------------")
  val obj555: BsonObject = new BsonObject().put("Store", new BsonArray())
  val arr444: BsonArray = new BsonArray().add(obj555)//.add(obj555)
  val obj333: BsonObject = new BsonObject().put("Store", arr444)
  val arr222: BsonArray = new BsonArray().add(obj333)//.add(obj333)
  //put("Store",new BsonObject())
  val obj111: BsonObject = new BsonObject().put("Store", arr222)

  val expression: String = ".Store[@Store]"
  val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
  boson.go(obj111.encodeToBarray())
  println("result of extracting \"" + expression + "\" -> " + future.join())

}
