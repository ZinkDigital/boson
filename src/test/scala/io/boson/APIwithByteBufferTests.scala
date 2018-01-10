package io.boson

import java.nio.ByteBuffer
import java.util.concurrent.{CompletableFuture, CountDownLatch, TimeUnit}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import mapper.Mapper
import org.junit.Assert.{assertArrayEquals, assertEquals}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class APIwithByteBufferTests extends FunSuite{

  val br4: BsonArray = new BsonArray().add("Insecticida")
  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
  val obj1: BsonObject = new BsonObject().put("José", br1)
  val br2: BsonArray = new BsonArray().add("Spider")
  val obj2: BsonObject = new BsonObject().put("José", br2)
  val br3: BsonArray = new BsonArray().add("Fly")
  val obj3: BsonObject = new BsonObject().put("José", br3)
  val arr: BsonArray = new BsonArray().add(2.2f).add(obj1).add(obj2).add(obj3).add(br4)
  val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)

  val validatedByteArray: Array[Byte] = arr.encodeToBarray()
  val validatedByteArrayObj: Array[Byte] = bsonEvent.encodeToBarray()

  val validatedByteBuffer: ByteBuffer = ByteBuffer.allocate(validatedByteArray.length)
  validatedByteBuffer.put(validatedByteArray)
  validatedByteBuffer.flip()

  val validatedByteBufferObj: ByteBuffer = ByteBuffer.allocate(validatedByteArrayObj.length)
  validatedByteBufferObj.put(validatedByteArrayObj)
  validatedByteBufferObj.flip()

  test("extract PosV1 w/ key") {
    val expression: String = "[2 to 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Spider")),
      Map("José" -> Seq("Fly"))
    ))), future.join())
  }
  test("extract PosV2 w/ key") {
    val expression: String = "[2 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Spider"))
    ))), future.join())
  }
  test("extract PosV3 w/ key") {
    val expression: String = "[2 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Spider")),
      Map("José" -> Seq("Fly"))
    ))), future.join())
  }
  test("extract PosV4 w/ key") {
    val expression: String = "[2 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Spider")),
      Map("José" -> Seq("Fly")),
      Seq("Insecticida")
    ))), future.join())
  }
  test("extract PosV5 w/ key") {
    val expression: String = "[3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Fly"))
    ))), future.join())
  }

  test("extract with 2nd Key PosV1 w/ key") {
    val expression: String = "[2 to 3].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider"),
      Seq("Fly")
    ))), future.join())
  }
  test("extract with 2nd Key PosV2 w/ key") {
    val expression: String = "[2 until 3].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider")
    ))), future.join())
  }
  test("extract with 2nd Key PosV3 w/ key") {
    val expression: String = "[2 until end].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider"),
      Seq("Fly")
    ))), future.join())
  }
  test("extract with 2nd Key PosV4 w/ key") {
    val expression: String = "[2 to end].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider"),
      Seq("Fly")
    ))), future.join())
  }
  test("extract with 2nd Key PosV5 w/ key") {
    val expression: String = "[3].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(Seq(
      Seq("Fly")
    ))), future.join())
  }

  test("extract first w/ key") {
    val expression: String = "first"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(
      2.2f
    )), future.join())
  }
  test("extract last w/ key") {
    val expression: String = "last"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(
      Seq("Insecticida")
    )), future.join())
  }
  test("extract all w/ key") {
    val expression: String = "all"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    assertEquals(BsSeq(Seq(
      2.2f,
      Map("José" -> Seq("Tarantula", "Aracnídius", Seq("Insecticida"))),
      Map("José" -> Seq("Spider")),
      Map("José" -> Seq("Fly")),
      Seq("Insecticida")
    )), future.join())
  }

  test("extract PosV1") {
    val expression: String = "José.[0 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq(
      "Tarantula", "Aracnídius"
    ))), future.join())
  }
  test("extract PosV2") {
    val expression: String = "José.[0 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)
    assertEquals(BsSeq(Seq(
      Seq("Tarantula", "Aracnídius", Seq("Insecticida")),
      Seq("Spider"),
      Seq("Fly")
    )), future.join())
  }
  test("extract PosV3") {
    val expression: String = "José.[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)
    assertEquals(BsSeq(Seq(Seq(
      "Aracnídius",
      Seq("Insecticida")
    ))), future.join())
  }
  test("extract PosV4") {
    val expression: String = "StartUp.[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)
    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Tarantula", "Aracnídius", Seq("Insecticida"))),
      Map("José" -> Seq("Spider"))
    ))), future.join())
  }
  test("extract PosV5") {
    val expression: String = "StartUp.[3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    assertEquals(BsSeq(Seq(Seq(
      Map("José" -> Seq("Fly"))
    ))), future.join())
  }

  test("extract with 2nd Key PosV1") {
    val expression: String = "StartUp.[0 until end].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq(
      Seq("Tarantula", "Aracnídius", Seq("Insecticida")),
      Seq("Spider"),
      Seq("Fly")
    ))), future.join())
  }
  test("extract with 2nd Key PosV2") {
    val expression: String = "StartUp.[2 to end].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider"),
      Seq("Fly")
    ))), future.join())
  }
  test("extract with 2nd Key PosV3") {
    val expression: String = "StartUp.[2 to 3].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider"),
      Seq("Fly")
    ))), future.join())
  }
  test("extract with 2nd Key PosV4") {
    val expression: String = "StartUp.[2 until 3].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq(
      Seq("Spider")
    ))), future.join())
  }
  test("extract with 2nd Key PosV5") {
    val expression: String = "StartUp.[4].José"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Seq(Seq())), future.join())
  }

  test("extract first") {
    val expression: String = "StartUp.first"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    assertEquals(BsSeq(Seq(
      2.200000047683716,
      Map("José" -> List("Tarantula", "Aracnídius", List("Insecticida"))),
      Map("José" -> List("Spider")),
      Map("José" -> List("Fly")),
      List("Insecticida")
    )), future.join())
  }
  test("extract last") {
    val expression: String = "José.last"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    assertEquals(BsSeq(Seq(
      "Fly"
    )), future.join())
  }
  test("extract all") {
    val expression: String = "José.all"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    assertEquals(BsSeq(Seq(
      Seq("Tarantula", "Aracnídius", Seq("Insecticida")),
      Seq("Spider"),
      Seq("Fly")
    )), future.join())
  }

  test("Inject API Double => Double ByteBuffer") {
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
    val newFridgeSerialCode: Double = 1000.0
    val validBsonArray: ByteBuffer = bsonEvent.encode().getByteBuf.nioBuffer()
    val expression = "fanVelocity.first"
    val boson: Boson = Boson.injector(expression, (in: Double) => newFridgeSerialCode)
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(List(1000.0), future.join().getValue )
  }
  test("Inject API String => String ByteBuffer") {
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the")
    val newFridgeSerialCode: String = " what?"
    val validBsonArray: ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "string.first"
    val boson: Boson = Boson.injector(expression, (in: String) => in.concat(newFridgeSerialCode))
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue:ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(List("the what?").head, new String(future.join().getValue.asInstanceOf[List[Array[Byte]]].head) )
  }
  test("Inject API Map => Map ByteBuffer") {
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)

    val newFridgeSerialCode: String = " what?"
    val validBsonArray: ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => in.+(("WHAT!!!", 10)))
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(Map("damnnn" -> "DAMMN", "WHAT!!!" -> 10))),future.join() )
  }
  test("Inject API Map => Map 1 ByteBuffer") {
    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
    val bAux1: BsonObject = new BsonObject().put("DAMNNNNN", "damnn")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)

    val newFridgeSerialCode: Map[String, Any] = Mapper.convert(bAux1).asInstanceOf[ Map[String, Any]]
    val validBsonArray:ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => newFridgeSerialCode)
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue:ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(Map("DAMNNNNN" -> "damnn"))),future.join() )
  }
  test("Inject API List => List ByteBuffer") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: String = "MAIS EU"
    val validBsonArray: ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: List[Any]) => {
      val s: List[Any] = in.:+(newFridgeSerialCode)
      s})
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(12, "sddd", "MAIS EU")),future.join() )
  }
  test("Inject API List => List 1 ByteBuffer") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bAux1: BsonArray = new BsonArray().add("sddd").add(12)
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode:List[Any] = Mapper.convertBsonArray(bAux1)
    val validBsonArray: ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "bson.first"
    val boson: Boson = Boson.injector(expression, (in: List[Any]) => newFridgeSerialCode)
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List("sddd", 12)),future.join() )
  }
  test("Inject API Int => Int ByteBuffer") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("int", 10).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: Int = 2
    val validBsonArray: ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "int.first"
    val boson: Boson = Boson.injector(expression, (in: Int) => in*newFridgeSerialCode)
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(20)),future.join() )
  }
  test("Inject API Long => Long ByteBuffer") {
    val bAux: BsonArray = new BsonArray().add(12).add("sddd")
    val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("long", 9L).put("fanVelocity", 20.5).put("doorOpen", false).put("string", 1).put("bson", bAux)

    val newFridgeSerialCode: Long = 2
    val validBsonArray: ByteBuffer = bsonEvent.encode.getByteBuf.nioBuffer()
    val expression = "long.first"
    val boson: Boson = Boson.injector(expression, (in: Long) => in*newFridgeSerialCode)
    val result: CompletableFuture[ByteBuffer] = boson.go(validBsonArray)

    // apply an extractor to get the new serial code as above.
    val resultValue: ByteBuffer = result.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(resultValue)

    assertEquals(BsSeq(List(18)),future.join() )
  }

  test("fuse Extractor->Injector->Extractor") {
    val latch = new CountDownLatch(1)
    val expression1: String = "[0]"
    val ext: Boson = Boson.extractor(expression1, (in: BsValue) => {
      println(s"----------------------------------- result of extraction: ${in.getValue.asInstanceOf[Seq[Any]].head} ---------------------------")
      assert(Seq(Seq(2.2f)) === in.getValue || Seq(Seq(5.5f)) === in.getValue)
      latch.countDown()
    })

    val newFridgeSerialCode: Float = 5.5f
    val expression2 = "[0 to 0]"
    val inj: Boson = Boson.injector(expression2, (_: Float) => newFridgeSerialCode)

    val fused: Boson = ext.fuse(inj)
    val future: CompletableFuture[ByteBuffer] = fused.go(validatedByteBuffer)
    val fused2: Boson = fused.fuse(ext)
    val future2: CompletableFuture[ByteBuffer] = fused2.go(future.join())

    //latch.await()
    assertArrayEquals(future.join().array(), future2.get(1, TimeUnit.SECONDS).array())
  }

}
