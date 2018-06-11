package io.zink.boson

import java.nio.ByteBuffer

import bsonLib.{BsonArray, BsonObject}
import io.netty.util.ResourceLeakDetector
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class APIwithByteBufferTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
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

  val validatedByteBufferObj: ByteBuffer = ByteBuffer.allocate(validatedByteArrayObj.length)
  validatedByteBufferObj.put(validatedByteArrayObj)


//  test("extract PosV1 w/ key") {
//    val expression: String = "[2 to 3]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(br4.encodeToBarray(),obj2.encodeToBarray(),obj3.encodeToBarray())
//    val result = future.join()
//    println(result)
//
//    result.head.foreach(b =>print(s"$b"))
//    println()
//    println(new String(result.head))
//    expected.head.foreach(b=> print(s"$b"))
//    println()
//    println(new String(expected.head))
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte], r: Array[Byte]) =>println(e.sameElements(r)); e.sameElements(r)
//    })
//  }
//
//  test("extract PosV2 w/ key") {
//    val expression: String = "[2 until 3]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(br4.encodeToBarray(),obj2.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
//  test("extract PosV3 w/ key") {
//    val expression: String = "[2 until end]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(obj2.encodeToBarray(),obj3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
//  test("extract PosV4 w/ key") {
//    val expression: String = "[2 to end]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(br4.encodeToBarray(),obj2.encodeToBarray(),obj3.encodeToBarray(),br4.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
//  test("extract PosV5 w/ key") {
//    val expression: String = "[3]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(obj3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract with 2nd Key PosV1 w/ key") {
//    val expression: String = "[2 to 3].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(br2.encodeToBarray(), br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract with 2nd Key PosV2 w/ key") {
//    val expression: String = "[2 until 3].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Vector(br2.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract with 2nd Key PosV3 w/ key") {
//    val expression: String = "[2 until end].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Vector(br2.encodeToBarray(), br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract with 2nd Key PosV4 w/ key") {
//    val expression: String = "[2 to end].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(br2.encodeToBarray(), br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract with 2nd Key PosV5 w/ key") {
//    val expression: String = "[3].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Array[Byte]] = Seq(br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract PosV1") {
//    val expression: String = "José[0 until end]"
//    val future: CompletableFuture[Seq[String]] = new CompletableFuture[Seq[String]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[String]) => future.complete(in))
//    boson.go(validatedByteArrayObj)
//    assertEquals(Seq(
//      "Tarantula", "Aracnídius"
//    ), future.join())
//  }
//
//  test("extract PosV2") {
//    val expression: String = "José[0 to end]"
//    val future: CompletableFuture[Seq[Any]] = new CompletableFuture[Seq[Any]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Any]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//
//    val expected: Seq[Any] = Seq( "Tarantula", "Aracnídius",br4.encodeToBarray(),"Spider","Fly")
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//      case (e,r) => e.equals(r)
//    })
//  }
//
//  test("extract PosV3") {
//    val expression: String = "José[1 to 2]"
//    val future: CompletableFuture[Seq[Any]] = new CompletableFuture[Seq[Any]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Any]) => future.complete(in))
//    boson.go(validatedByteBufferObj)
//    val expected: Seq[Any] = Seq("Aracnídius",br4.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//      case (e,r) => e.equals(r)
//    })
//  }
//
//  test("extract PosV4") {
//    val expression: String = "StartUp[1 to 2]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBufferObj)
//
//    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(1).encodeToBarray(),arr.getBsonObject(2).encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract PosV5") {
//    val expression: String = "StartUp[3]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBufferObj)
//
//    val expected: Seq[Array[Byte]] = Seq(arr.getBsonObject(3).encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test("extract with 2nd Key PosV1") {
//    val expression: String = "StartUp[0 until end].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteArrayObj)
//
//    val expected: Seq[Any] = Seq( br1.encodeToBarray(),br2.encodeToBarray(),br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//      case (e,r) => e.equals(r)
//    })
//  }
//
//  test("extract with 2nd Key PosV2") {
//    val expression: String = "StartUp[2 to end].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteArrayObj)
//    val expected: Seq[Array[Byte]] = Seq(br2.encodeToBarray(),br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
//  test("extract with 2nd Key PosV3") {
//    val expression: String = "StartUp[2 to 3].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteArrayObj)
//
//    val expected: Seq[Array[Byte]] = Seq(br2.encodeToBarray(),br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
//  test("extract with 2nd Key PosV4") {
//    val expression: String = "StartUp[2 until 3].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteArrayObj)
//
//    val expected: Seq[Array[Byte]] = Seq(br2.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
//  test("extract with 2nd Key PosV5") {
//    val expression: String = "StartUp[4].José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteArrayObj)
//    assertEquals(Seq(), future.join())
//  }
//
//  test("extract all") {
//    val expression: String = "José"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBufferObj)
//
//    val expected: Seq[Array[Byte]] = Seq( br1.encodeToBarray(),br2.encodeToBarray(),br3.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//    })
//  }
//
////  test("extract all elements containing partial key") {
////    val expression: String = "*os"
////    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
////    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
////    boson.go(validatedByteBufferObj)
////
////    assertEquals(BsSeq(Vector(
////      "Tarantula", "Aracnídius", Seq("Insecticida"),
////      "Spider",
////      "Fly"
////    )), future.join())
////  }
//  // TODO:Bug with halfKey matching a key(see trello)
//
//  test("extract all elements of root") {
//    val expression: String = ".*"
//    val future: CompletableFuture[Seq[Any]] = new CompletableFuture[Seq[Any]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Any]) => future.complete(in))
//    boson.go(validatedByteBuffer)
//    val res = future.join()
//    val expected: Seq[Any] =
//      Seq(2.2f,obj1.encodeToBarray(),obj2.encodeToBarray(),obj3.encodeToBarray(),br4.encodeToBarray())
//    assert(expected.size === res.size)
//    assertTrue(expected.zip(res).forall{
//      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
//      case (e,r: Double) => e == r
//      case (e,r) => e.equals(r)
//    })
//  }
//
//  test("extract objects with a certain element") {
//    val br4: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius")
//    val obj4: BsonObject = new BsonObject().put("Joséééé", br4)
//    val br5: BsonArray = new BsonArray().add("Spider")
//    val obj5: BsonObject = new BsonObject().put("José", br5)
//    val arr1: BsonArray = new BsonArray().add(2.2f).add(obj4).add(true).add(obj5)
//    val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr1)
//    val validatedByteArrayObj1: Array[Byte] = bsonEvent1.encodeToBarray()
//    val validatedByteBufferObj1: ByteBuffer = ByteBuffer.allocate(validatedByteArrayObj1.length)
//    validatedByteBufferObj1.put(validatedByteArrayObj1)
//    validatedByteBufferObj1.flip()
//
//    val expression: String = "StartUp[@José]"
//    val future: CompletableFuture[Seq[Array[Byte]]] = new CompletableFuture[Seq[Array[Byte]]]()
//    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => future.complete(in))
//    boson.go(validatedByteBufferObj1)
//
//    val expected: Seq[Array[Byte]] = Seq(obj5.encodeToBarray())
//    val result = future.join()
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }

}
