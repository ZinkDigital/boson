package io.zink.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch}

import bsonLib.{BsonArray, BsonObject}
import io.zink.boson.bson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonValue._
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector
import mapper.Mapper
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.{assertEquals,assertTrue}

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class injectorAPITests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)

  val b11: BsonObject = new BsonObject().put("Title","C++Machine").put("Price", 38)
  val br5: BsonArray = new BsonArray().add(b11)
  val b10: BsonObject = new BsonObject().put("Title","JavaMachine").put("Price", 39)
  val br4: BsonArray = new BsonArray().add(b10)
  val b9: BsonObject = new BsonObject().put("Title","ScalaMachine").put("Price", 40)
  val br3: BsonArray = new BsonArray().add(b9)
  val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
  val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
  val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
  val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7)
  val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions",br3)
  val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions",br4)
  val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions",br5)
  val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
  val b2: BsonObject = new BsonObject().put("Book", br1).put("Hat",br2)
  val bsonEvent: BsonObject = new BsonObject().put("Store", b2)

  val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

//  val bAux2: BsonObject = new BsonObject().put("google", "DAMMN")
//  val bsonArrayEvent1: BsonArray = new BsonArray().add(bAux2)
//  val bAux: BsonObject = new BsonObject().put("damnnn", bsonArrayEvent1)
//  val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux)
//  val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)
//  val validatedByteArr111: Array[Byte] = bsonObjectRoot.encodeToBarray()

  test("Experiments ") {
    val expression = "Store.Book[0 until 3].SpecialEditions[0].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "JavaMachine",
      "ScalaMachine",
      "C++Machine"
    )), future.join())
  }

  test("extract Key.Key") {
    val expression = "Store.Book"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br1.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.Key[@elem]") {
    val expression = "Store.Book[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(br1.getBsonObject(0).encodeToBarray(),br1.getBsonObject(2).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.*halfKey") {
    val expression = "Store.*at"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.halfKey*") {
    val expression = "Store.H*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.halfKey*halfKey") {
    val expression = "Store.H*t"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.*") {
    val expression = "Store.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br1.encodeToBarray(),br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key[@*elem]") {
    val expression = "Book[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br1.getBsonObject(0).encodeToBarray(),br1.getBsonObject(2).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract *[@elem]") {
    val expression = "*[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Any] =
      Vector(b3.encodeToBarray(),b10.encodeToBarray(), b9.encodeToBarray(), b8.encodeToBarray(), b11.encodeToBarray(), b5.encodeToBarray(), b6.encodeToBarray(), b7.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    println(s"result: $result")
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) =>
        println(s"e: $e, r: $r, are they equal? ${e.sameElements(r)}")
        e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("extract *[@*elem]") {
    val expression = "*[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] =
      Vector(b3.encodeToBarray(),b10.encodeToBarray(), b9.encodeToBarray(), b8.encodeToBarray(), b11.encodeToBarray(), b5.encodeToBarray(), b6.encodeToBarray(), b7.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.Key.Key") {
    val expression = "Store.Book.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java",
      "Scala",
      "C++"
    )), future.join())
  }

  test("extract Key.*halfKey[@*elem]") {
    val expression = "Store.*ok[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br1.getBsonObject(0).encodeToBarray(),br1.getBsonObject(2).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.*halfKey[@*elem].Key") {
    val expression = "Store.*ok[@*ce].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java",
      "C++"
    )), future.join())
  }

  test("extract Key.*halfKey[@*elem].K*y") {
    val expression = "Store.B*ok[@*ce].Ti*le"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java",
      "C++"
    )), future.join())
  }

  test("extract Key.Key[#].Key") {
    val expression = "Store.Book[1].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Scala"
    )), future.join())
  }

  test("extract Key.Key[#to#].Key") {
    val expression = "Store.Book[1 to 2].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Scala",
      "C++"
    )), future.join())
  }

  test("extract Key.Key[#until#].Key") {
    val expression = "Store.Book[0 until 2].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java",
      "Scala"
    )), future.join())
  }

  test("extract Key.Key[#until end].Key") {
    val expression = "Store.Book[1 until end].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Scala"
    )), future.join())
  }

  test("extract Key.Key[#to end].Key") {
    val expression = "Store.Book[1 to end].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Scala",
      "C++"
    )), future.join())
  }

  test("extract Key.Key[#to#].Key[#to#]") {
    val expression = "Store.Book[0].SpecialEditions[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    println("result: " + new String(result.head)+ s", expected: ${new String(expected.head)}")
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.Key[#].Key[@elem]") {
    val expression = "Store.Book[1].SpecialEditions[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br3.getBsonObject(0).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.Key[@*elem].Key[#]") {
    val expression = "Store.Book[@*ce].SpecialEditions[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br4.getBsonObject(0).encodeToBarray(),br5.getBsonObject(0).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract Key.Key[@elem].Key[@elem]") {
    val expression = "Store.Book[@Price].SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br4.getBsonObject(0).encodeToBarray(),br5.getBsonObject(0).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("MoreKeys 5 bson11.array21[0 until 2].[0 until 2]"){
    val bsonObjectLvl31: BsonObject = new BsonObject().put("field1", 0)
    val bsonObjectLvl32: BsonObject = new BsonObject().put("int1", 1)
    val bsonObjectLvl33: BsonObject = new BsonObject().put("int2", 2)
    val bsonArray31: BsonArray = new BsonArray().add(0).add(1).add(5.2)
    val bsonArray32: BsonArray = new BsonArray().add(bsonObjectLvl31).add(bsonObjectLvl32).add(bsonObjectLvl33)
    val bsonArray33: BsonArray = new BsonArray().add(bsonObjectLvl31).add(bsonObjectLvl32).add(bsonObjectLvl33)
    val bsonArrayLvl21: BsonArray = new BsonArray().add(bsonArray31).add(bsonArray31).add(bsonArray31)
    val bsonArrayLvl22: BsonArray = new BsonArray().add(bsonArray31).add(bsonArray31).add(bsonArray31)
    val bsonObjectLvl11: BsonObject = new BsonObject().put("array21", bsonArrayLvl21)
    val bsonObjectLvl12: BsonObject = new BsonObject().put("array22", bsonArrayLvl22)
    val bsonObjectRoot: BsonObject = new BsonObject().put("bson11", bsonObjectLvl11).put("bson12", bsonObjectLvl12)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
      //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
      val expression = "bson11.array21[0 until 2].[0 until 2]" //.[0 to 1].fie*"
      val expression1 = "bson11.array21[0 until 2].[0 until 2]"
      val boson: Boson = Boson.injector(expression, (in: Int) => in + 2)
      val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
      val result: Array[Byte] = midResult.join()
      //println(s"result size = ${result.length}   resultBYteBuf size = ${s.readIntLE()}   validBsonArray = ${validBsonArray.length}")
      //validBsonArray.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
      //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
      boson1.go(result)
      //println(future.join()/*.getValue.asInstanceOf[Vector[String]]*/)
      val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
      assertEquals(Vector(2, 3, 2, 3), a)
  }


  test("MoreKeys 4 bson11.array21[0 until 2]"){

    val bsonObjectLvl31: BsonObject = new BsonObject().put("field1", 0)
    val bsonObjectLvl32: BsonObject = new BsonObject().put("int1", 1)
    val bsonObjectLvl33: BsonObject = new BsonObject().put("int2", 2)
    val bsonArray31: BsonArray = new BsonArray().add(0).add(1).add("ds")
    val bsonArray32: BsonArray = new BsonArray().add(bsonObjectLvl31).add(bsonObjectLvl32).add(bsonObjectLvl33)
    val bsonArray33: BsonArray = new BsonArray().add(bsonObjectLvl31).add(bsonObjectLvl32).add(bsonObjectLvl33)
    val bsonArrayLvl21: BsonArray = new BsonArray().add(bsonArray31).add(bsonArray32).add(bsonArray33)
    val bsonArrayLvl22: BsonArray = new BsonArray().add(bsonArray31).add(bsonArray32).add(bsonArray33)
    val bsonObjectLvl11: BsonObject = new BsonObject().put("array21", bsonArray31)
    val bsonObjectLvl12: BsonObject = new BsonObject().put("array22", bsonArrayLvl22)
    val bsonObjectRoot: BsonObject = new BsonObject().put("bson11", bsonObjectLvl11).put("bson12", bsonObjectLvl12)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val expression = "bson11.array21[0 until 2]" //.[0 to 1].fie*"
    val expression1 = "bson11.array21[0 until 2]"
    val boson: Boson = Boson.injector(expression, (in: Int) => in + 2)
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    val s: ByteBuf = Unpooled.copiedBuffer(result)
    //println(s"result size = ${result.length}   resultBYteBuf size = ${s.readIntLE()}   validBsonArray = ${validBsonArray.length}")
    //validBsonArray.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    //println(future.join()/*.getValue.asInstanceOf[Vector[String]]*/)
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector(2, 3),a  )


  }

  test("MoreKeys 3 bson11.array21[@i*nt].int"){

    val bsonObjectLvl31: BsonObject = new BsonObject().put("int", 0)
    val bsonObjectLvl32: BsonObject = new BsonObject().put("int1", 1)
    val bsonObjectLvl33: BsonObject = new BsonObject().put("int2", 2)
    val bsonArrayLvl21: BsonArray = new BsonArray().add(bsonObjectLvl31).add(bsonObjectLvl32).add(bsonObjectLvl33)
    val bsonArrayLvl22: BsonArray = new BsonArray().add(bsonObjectLvl31).add(bsonObjectLvl32).add(bsonObjectLvl33)
    val bsonObjectLvl11: BsonObject = new BsonObject().put("array21", bsonArrayLvl21)
    val bsonObjectLvl12: BsonObject = new BsonObject().put("array22", bsonArrayLvl22)
    val bsonObjectRoot: BsonObject = new BsonObject().put("bson11", bsonObjectLvl11).put("bson12", bsonObjectLvl12)

    //val newFridgeSerialCode: String = " what?"
    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
    //val expression = "a*rray.dam*nnn.cree*.goo*e"//.[2 to 3].efwwf.efwfwefwef.[@elem].*.[0]..first"
    val expression = "bson11.array21[@i*nt].int"
    val expression1 = "int"
    val boson: Boson = Boson.injector(expression, (in: Int) => in + 2)
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    val s: ByteBuf = Unpooled.copiedBuffer(result)
    //println(s"result size = ${result.length}   resultBYteBuf size = ${s.readIntLE()}   validBsonArray = ${validBsonArray.length}")
    //validBsonArray.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    //println(future.join()/*.getValue.asInstanceOf[Vector[String]]*/)
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector(2, 0),a  )

  }

  test("MoreKeys 2 array[0].damnnn[1].google"){
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
    val expression = "array[0].damnnn[1].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    //println(future.join().getValue.asInstanceOf[Vector[String]])
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector("DAMMN", "sdfsf", "DAMMN", "DAMMN", "DAMMN", "DAMMN"),a  )

  }

  test("MoreKeys 1 arr*ay[0 until 1].damn*n[0 until 1].google"){
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
    val expression = "arr*ay[0 until 1].damn*n[0 until 1].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    //println(future.join().getValue.asInstanceOf[Vector[String]])
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector("sdfsf", "DAMMN", "DAMMN", "DAMMN", "DAMMN", "DAMMN"),a  )

  }


  test("MoreKeys array[@damnnn].damnnn[@google].google"){
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
    val expression = "array[@damnnn].damnnn[@google].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector("sdfsf", "sdfsf", "sdfsf", "sdfsf", "sdfsf", "sdfsf", "DAMMN"),a  )
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
  }

  test("$.Store.* => Store") {
    /*
    * Montagem do Event de testes
    * */
    //println("|-------- Construct the Test Event --------|\n")
    val SEdition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val SEdition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val SEdition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val SpecialEditions1:BsonArray = new BsonArray().add(SEdition1)
    val SpecialEditions2:BsonArray = new BsonArray().add(SEdition2)
    val SpecialEditions3:BsonArray = new BsonArray().add(SEdition3)
    val Book1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", SpecialEditions1)
    val Book2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", SpecialEditions2)
    val Book3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", SpecialEditions3)
    val Book: BsonArray = new BsonArray().add(Book1).add(Book2).add(Book3)
    val Hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val Hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val Hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val Hat: BsonArray = new BsonArray().add(Hat1).add(Hat2).add(Hat3)
    val Store: BsonObject = new BsonObject().put("Book", Book).put("Hat", Hat)
    val Event: BsonObject = new BsonObject().put("Store", Store)

    /*
    * Injection
    * */
    //println("|-------- Perform Injection --------|\n\n")
    val validBsonArray: Array[Byte] = Event.encodeToBarray

    val expression = "Store"
    val boson: Boson = Boson.injector(expression,(x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("WHAT!!!", 10))
      val res: ByteBuf = Mapper.encode(newM)
      if(res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println("Char="+ b.toChar + "  Int="+b.toInt))
    //println("|-------- Perform Extraction --------|\n\n")
    //val expression1 = "array.[@damnnn].damnnn.[@google].google"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(result)
    //val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]

    val _SEdition1: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val _SEdition2: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val _SEdition3: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val _SpecialEditions1:BsonArray = new BsonArray().add(_SEdition1)
    val _SpecialEditions2:BsonArray = new BsonArray().add(_SEdition2)
    val _SpecialEditions3:BsonArray = new BsonArray().add(_SEdition3)
    val _Book1: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", _SpecialEditions1).put("Title", "Java")
    val _Book2: BsonObject = new BsonObject().put("Price", 21.5).put("SpecialEditions", _SpecialEditions2).put("Title", "Scala")
    val _Book3: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", _SpecialEditions3).put("Title", "C++")
    val _Book: BsonArray = new BsonArray().add(_Book1).add(_Book2).add(_Book3)
    val _Hat1: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val _Hat2: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val _Hat3: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val _Hat: BsonArray = new BsonArray().add(_Hat1).add(_Hat2).add(_Hat3)
    val _Store: BsonObject = new BsonObject().put("Book", _Book).put("Hat", _Hat).put("WHAT!!!", 10)

    val expected: Vector[Array[Byte]] = Vector(_Store.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall(b => b._1.sameElements(b._2)))
    //println("|-------- Perform Assertion --------|\n\n")
    //assertEquals(Vector(Map("Book" -> List(Map("Price" -> 15.5, "SpecialEditions" -> List(Map("Price" -> 39, "Title" -> "JavaMachine")), "Title" -> "Java"), Map("Price" -> 21.5, "SpecialEditions" -> List(Map("Price" -> 40, "Title" -> "ScalaMachine")), "Title" -> "Scala"), Map("Price" -> 12.6, "SpecialEditions" -> List(Map("Price" -> 38, "Title" -> "C++Machine")), "Title" -> "C++")), "Hat" -> List(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38)), "WHAT!!!" -> 10)),future.join().getValue  )
  }

  test("API Injetor f= Float => Float") {
    /*
    * Montagem do Event de testes
    * */
    //println("|-------- Construct the Test Event --------|\n")
    val SEdition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val SEdition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val SEdition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val SpecialEditions1:BsonArray = new BsonArray().add(SEdition1)
    val SpecialEditions2:BsonArray = new BsonArray().add(SEdition2)
    val SpecialEditions3:BsonArray = new BsonArray().add(SEdition3)
    val Book1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", SpecialEditions1)
    val Book2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", SpecialEditions2)
    val Book3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", SpecialEditions3)
    val Book: BsonArray = new BsonArray().add(Book1).add(Book2).add(Book3)
    val Hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val Hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val Hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val Hat: BsonArray = new BsonArray().add(Hat1).add(Hat2).add(Hat3)
    val Store: BsonObject = new BsonObject().put("Book", Book).put("Hat", Hat).put("float", 12.3f)
    val Event: BsonObject = new BsonObject().put("Store", Store)

    /*
    * Injection
    * */

    val validBsonArray: Array[Byte] = Event.encodeToBarray
    val expression = ".Store.float"
    val boson: Boson = Boson.injector(expression, (x:Float) => x+2.2f)
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson1.go(result)
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector(14.5) ,future.join().getValue  )
  }

}



