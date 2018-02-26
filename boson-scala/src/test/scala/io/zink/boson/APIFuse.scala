//package io.zink.boson
//
//import java.util.concurrent.{CompletableFuture, TimeUnit}
//
//import bsonLib.{BsonArray, BsonObject}
//import io.zink.boson.bson.bsonValue.BsValue
//import org.junit.Assert.assertArrayEquals
//import org.junit.runner.RunWith
//import org.scalatest.FunSuite
//import org.scalatest.junit.JUnitRunner
//
//import scala.concurrent.duration.Duration
//import scala.concurrent.{Await, Future}
//import scala.util.{Failure, Success}
//
//@RunWith(classOf[JUnitRunner])
//class APIFuse extends FunSuite{
//
//  val obj1: BsonObject = new BsonObject().put("fridgeTemp111", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
//  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
//  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)
//  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)
//
//  val validatedByteArray: Array[Byte] = bsonEvent.encodeToBarray()
//
//
//  test("Sequential fuse Extractor->Injector->Extractor") {
//    val expression1: String = "fridgeTemp"
//    val ext: Boson = Boson.extractor(expression1, (in: BsValue) => {
//      println(s"----------------------------------- result of extraction: ${in.getValue.asInstanceOf[Seq[Any]]} ---------------------------")
//      assert(Vector(5.0f,3.854f) === in.getValue || Vector(18.3f,18.3f) === in.getValue)
//    })
//
//    val newFridgeSerialCode: Float = 18.3f
//    val expression2 = "fridgeTemp"
//    val inj: Boson = Boson.injector(expression2, (_: Float) => newFridgeSerialCode)
//
//    val fused: Boson = ext.fuse(inj)
//    val future: Future[Array[Byte]] = fused.go(validatedByteArray)
//    Await.result(future, Duration.Inf)//fromNanos(10000000*1000))
//
//    val futureRes = future.value.get match{
//      case Success(value)=> value
//      case Failure(e)=>
//        println(e.getMessage)
//        validatedByteArray
//    }
//
//    val fused2: Boson = fused.fuse(ext)
//    val future2: Future[Array[Byte]] = fused2.go(futureRes)
//    Await.result(future2, Duration.Inf)//fromNanos(10000000*1000))
//    val future2Res = future2.value.get match{
//      case Success(value)=> value
//      case Failure(e)=>
//        println(e.getMessage)
//        futureRes
//    }
//    //latch.await()
//    assertArrayEquals(futureRes, future2Res)
//  }
//
//}
