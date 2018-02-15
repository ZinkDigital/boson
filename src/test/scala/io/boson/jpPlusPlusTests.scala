package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch, TimeUnit}
import io.boson.bson.bsonImpl.Dictionary._
import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import io.boson.json.Joson
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector
import mapper.Mapper
import org.junit.Assert
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert._

@RunWith(classOf[JUnitRunner])
class jpPlusPlusTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  val br5: BsonArray = new BsonArray().add(b11)
  val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  val br4: BsonArray = new BsonArray().add(b10)
  val b9: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  val br3: BsonArray = new BsonArray().add(b9)
  val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
  val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
  val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
  val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
  val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
  val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
  val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
  val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
  val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
  val bsonEvent: BsonObject = new BsonObject().put("Store", b2)

  val _b11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
  val _br5: BsonArray = new BsonArray().add(_b11)
  val _b10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
  val _br4: BsonArray = new BsonArray().add(_b10)
  val _b9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
  val _br3: BsonArray = new BsonArray().add(_b9)
  val _b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
  val _b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
  val _b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
  val _b4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", _br3).put("Title", "Scala")
  val _b3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", _br4).put("Title", "Java")
  val _b8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", _br5).put("Title", "C++")
  val _br1: BsonArray = new BsonArray().add(_b3).add(_b4).add(_b8)
  val _br2: BsonArray = new BsonArray().add(_b5).add(_b6).add(_b7).add(_b3)
  val _b2: BsonObject = new BsonObject().put("Book", _br1).put("Hatk", _br2)
  val _bsonEvent: BsonObject = new BsonObject().put("Store", _b2)

  val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

  test("Ex .key") {
    println(bsonEvent)
    val expression = ".Store"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store -> checked

  test("Inj .key") {
    val expression: String = ".Store"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val bosonE: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    bosonE.go(injFuture.join())

    val expected: Vector[Array[Byte]] = Vector(_b2.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1.key2") {
    val expression = ".Store.Book"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)


    val expected: Vector[Array[Byte]] = Vector(br1.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store.Book -> checked

  test("Inj .key1.key2") {
    val expression: String = ".Store.Book"

    //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.+:("Street?"))

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
      val newL: List[Any] = l.+:("Street?")
      val res: ByteBuf = Mapper.encode(newL)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })

    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())
    val help: BsonArray = new BsonArray().add("Street?").add(_b3).add(_b4).add(_b8)

    val expected: Vector[Array[Byte]] = Vector(help.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1.key2[@elem1].key3[@elem2]") {
    val expression: String = ".Store.Book[@Price].SpecialEditions[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b11.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store.Book[?(@.Price)].SpecialEditions[?(@.Title)] -> checked

  test("Inj .key1.key2[@elem1].key3[@elem2]") {
    val expression: String = ".Store.Book[@Price].SpecialEditions[@Title]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })

    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] = Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1.key2[#]") {
    val expression: String = ".Store.Book[1]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b4.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store.Book[1] -> checked

  test("Inj .key1.key2[#]") {
    val expression: String = ".Store.Book[1]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] = Vector(_b4.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1.key2[# to end].key3") {
    val expression: String = ".Store.Book[0 to end].Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      15.5, 12.6
    ), future.join().getValue)
  } //$.Store.Book[:].Price -> checked

  test("Inj .key1.key2[# to end].key3") {
    val expression: String = ".Store.Book[0 to end].Price"

    val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(25.5, 22.6)", future.join().getValue.toString)
  }

  test("Ex .key1.key2[@elem].key3") {
    val expression: String = ".Store.Book[@Price].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      "Java", "C++"
    ), future.join().getValue)
  } //$.Store.Book[?(@.Price)].Title -> checked

  test("Inj .key1.key2[@elem].key3") {
    val expression: String = ".Store.Book[@Price].Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, C++!!!)", future.join().getValue.toString)
  }

  test("Ex .key1.key2[#].key3[@elem].k*y") {
    val expression: String = ".Store.Book[0 to end].SpecialEditions[@Price].T*le"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine", "ScalaMachine", "C++Machine"
    ), future.join().getValue)
  } //$.Store.Book[:].SpecialEditions[?(@.Price)].Title -> checked

  //-----------------------------------------------------------------------------------------------------//

  test("Inj .key1.key2[#].key3[@elem].k*y") {
    val expression: String = ".Store.Book[0 to end].SpecialEditions[@Price].T*le"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key V1") {
    val expression: String = "..Title" // || "Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      "Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine", "Java", "JavaMachine"
    ), future.join().getValue)
  } //$..Title -> checked

  test("Inj ..key V1") {
    val expression: String = "..Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, JavaMachine!!!, Scala!!!, ScalaMachine!!!, C++!!!, C++Machine!!!, Java!!!, JavaMachine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key V2") {
    val expression: String = "..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
    ), future.join().getValue)
  } //$..Price -> checked

  test("Inj ..key V2") {
    val expression: String = "..Book.*.Price"

    val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(25.5, 22.6)", future.join().getValue.toString)
  }

  test("Ex ..key1.key2") {
    val expression: String = "..Book.Title" //  || "Book.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      "Java", "Scala", "C++"
    ), future.join().getValue)
  } //$..Book[:].Title -> checked

  test("Inj ..key1.key2") {
    val expression: String = "..Book.Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java, Scala, C++)", future.join().getValue.toString)
  } //No change is perform, because Book is a Array.

  test("Ex ..key1[# to end].key2") {
    val expression: String = "..Book[0 to end].Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      15.5, 12.6
    ), future.join().getValue)
  } //$..Book[:].Price -> checked

  test("Inj ..key1[# to end].key2") {
    val expression: String = "..Book[0 to end].Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, Scala!!!, C++!!!)", future.join().getValue.toString)
  } //Change is perform, because Book is a Array.

  test("Ex ..key[@elem].key") {
    val expression: String = "..SpecialEditions[@Price].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine", "ScalaMachine", "C++Machine", "JavaMachine"
    ), future.join().getValue)
  } //$..SpecialEditions[?(@.Price)].Title -> checked

  test("Inj ..key[@elem].key") {
    val expression: String = "..SpecialEditions[@Price].Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!, JavaMachine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key[@elem]") {
    val expression: String = "..SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$..SpecialEditions[?(@.Price)] -> checked

  test("Inj ..key[@elem]") {
    val expression: String = "..SpecialEditions[@Price]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b9.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray(), _b10.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key[#] V1") {
    println(bsonEvent)
    val expression: String = "..SpecialEditions[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    println(s"result: $result")
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$..SpecialEditions[0] -> checked

  test("Inj ..key[#] V1") {
    val expression: String = "..SpecialEditions[0]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b9.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray(), _b10.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key[#] V2") {
    val arr5: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 12))
    val arr4: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 18))
    val arr3: BsonArray = new BsonArray().add(new BsonObject().put("doorOpen", arr5))
    val arr2: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 15))
    val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", arr2)
    val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("thing", arr3)
    val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", arr4)
    val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

    val expression: String = "..doorOpen[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Array[Byte]] =
      Vector(
        new BsonObject().put("fridgeTemp", 15).encodeToBarray(),
        new BsonObject().put("fridgeTemp", 12).encodeToBarray(),
        new BsonObject().put("fridgeTemp", 18).encodeToBarray()
      )
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key[#] V2") {
    val arr5: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 12))
    val arr4: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 18))
    val arr3: BsonArray = new BsonArray().add(new BsonObject().put("doorOpen", arr5))
    val arr2: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 15))
    val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", arr2)
    val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("thing", arr3)
    val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", arr4)
    val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
    val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

    val expression: String = "..doorOpen[0]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(bsonEvent.encodeToBarray())

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(new BsonObject().put("fridgeTemp", 15).put("Street?", "im Lost").encodeToBarray(), new BsonObject().put("fridgeTemp", 12).put("Street?", "im Lost").encodeToBarray(), new BsonObject().put("fridgeTemp", 18).put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..*y1[@elem1].key2[@elem2]") {
    val expression: String = "..*k[@Price].SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$..Book[?(@.Price)].SpecialEditions[?(@.Price)] && $..Hatk[?(@.Price)].SpecialEditions[?(@.Price)] -> checked

  test("Inj ..*y1[@elem1].key2[@elem2]") {
    val expression: String = "..*k[@Price].SpecialEditions[@Price]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray(), _b10.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..*y1[@elem1].key2") {
    val expression: String = "..*k[@SpecialEditions].Pr*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 21.5, 12.6, 15.5
    ), future.join().getValue)
  } //$..Book[?(@.SpecialEditions)].Price && $..Hatk[?(@.SpecialEditions)].Price -> checked

  //---------------------------------------------------------------------------------------------------//

  test("Inj ..*y1[@elem1].key2") {
    val expression: String = "..*k[@SpecialEditions].Pr*"

    val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(25.5, 31.5, 22.6, 25.5)", future.join().getValue.toString)
  }

  test("Ex .key1..key2") {
    val expression: String = ".Store..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
    ), future.join().getValue)
  } //$.Store..Price -> checked

  test("Inj .key1..key2") {
    //No Change is perform, because not all values are of the same type
    val expression: String = ".Store..Price"

    val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39)", future.join().getValue.toString)
  } //No Change is perform, because not all values are of the same type

  test("Ex .key1..key2[@elem]") {
    val expression: String = ".Store..SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store..SpecialEditions[?(@.Price)] -> checked

  test("Inj .key1..key2[@elem]") {
    //No Change is perform, because not all values are of the same type
    val expression: String = ".Store..SpecialEditions[@Price]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b9.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray(), _b10.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1..key2[#]") {
    val expression: String = ".Store..SpecialEditions[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store..SpecialEditions[0] -> checked

  test("Inj .key1..key2[#]") {
    //No Change is perform, because not all values are of the same type
    val expression: String = ".Store..SpecialEditions[0]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b9.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray(), _b10.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1..key2[#]..key3") {
    val expression: String = ".Store..Book[0 until end]..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "Java",
      "JavaMachine",
      "Scala",
      "ScalaMachine"
    ), future.join().getValue)
  } //$.Store..Book[0:2]..Title -> checked

  test("Inj .key1..key2[#]..key3") {
    val expression: String = ".Store..Book[0 until end]..Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, JavaMachine!!!, Scala!!!, ScalaMachine!!!)", future.join().getValue.toString)
  }

  test("Ex .key1..key2[#]..key3[@elem]") {
    val expression: String = ".Store..Book[1 until end]..SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b9.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store..Book[1:2]..SpecialEditions[?(@.Price)] -> checked

  test("Inj .key1..key2[#]..key3[@elem]") {
    val expression: String = ".Store..Book[1 until end]..SpecialEditions[@Price]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    //println("injFuture=" + new String(injFuture.join()))

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b9.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .*y1..*y2[#]..*y3..key4") {
    val expression: String = ".*ore..*k[1 until end]..*Editions..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector("ScalaMachine"), future.join().getValue)
  } //$.Store..Book[1:2]..SpecialEditions..Title -> checked

  test("Inj .*y1..*y2[#]..*y3..key4") {
    val expression: String = ".*ore..*k[1 until end]..*Editions..Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    //println("injFuture=" + new String(injFuture.join()))

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(ScalaMachine!!!)", future.join().getValue.toString)
  }

  test("Ex .key1..key2[@elem1]..key3[@elem2]") {
    val expression: String = ".Store..*k[@Price]..SpecialEditions[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.Store..Book[?(@.Price)]..SpecialEditions[?(@.Title)] && $.Store..Hatk[?(@.Price)]..SpecialEditions[?(@.Title)] -> checked

  //---------------------------------------------------------------------------------------------------//

  test("Inj .key1..key2[@elem1]..key3[@elem2]") {
    val expression: String = ".Store..*k[@Price]..SpecialEditions[@Title]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(_b10.put("Street?", "im Lost").encodeToBarray(), _b11.put("Street?", "im Lost").encodeToBarray(), _b10.put("Street?", "im Lost").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key1[#]..key2") {
    val expression: String = "..Book[0 to end]..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38
    ), future.join().getValue)
  } //$..Book[:]..Price -> checked

  test("Inj ..key1[#]..key2") {
    val expression: String = "..Book[0 to end]..Price"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    //println("injFuture=" + new String(injFuture.join()))

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(15.5, 39, 40, 12.6, 38)", future.join().getValue.toString)
  } //No Change is perform, because not all values are of the same type

  test("Ex ..key1..key2") {
    val expression: String = "Store..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
    ), future.join().getValue)
  } //$..Store..Price -> checked

  test("Inj ..key1..key2") {
    val expression: String = "Store..Price"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    // println("injFuture=" + new String(injFuture.join()))

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39)", future.join().getValue.toString)
  } //No Change is perform, because not all values are of the same type

  test("Ex ..key1[@elem]..key2") {
    val expression: String = "Book[@Price]..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "Java", "JavaMachine", "C++", "C++Machine"
    ), future.join().getValue)
  } //$..Book[?(@.Price)]..Title -> checked

  test("Inj ..key1[@elem]..key2") {
    val expression: String = "Book[@Price]..Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, JavaMachine!!!, C++!!!, C++Machine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key1..*ey2..*ey3") {
    val expression: String = "Store..*k..*itions"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(br4.encodeToBarray(), br3.encodeToBarray(), br5.encodeToBarray(), br4.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$..Store..Book..SpecialEditions && $..Store..Hatk..SpecialEditions -> checked

  test("Inj ..key1..*ey2..*ey3") {
    val expression: String = "Store..*k..*itions"

    //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.:+("NewEdition!"))
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
      val newL: List[Any] = l.:+("NewEdition!")
      val res: ByteBuf = Mapper.encode(newL)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    // println("injFuture=" + new String(injFuture.join()))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())
    val xbr4 = new BsonArray().add(new BsonObject().put("Price", 39).put("Title", "JavaMachine")).add("NewEdition!").encodeToBarray()
    val xbr3 = new BsonArray().add(new BsonObject().put("Price", 40).put("Title", "ScalaMachine")).add("NewEdition!").encodeToBarray()
    val xbr5 = new BsonArray().add(new BsonObject().put("Price", 38).put("Title", "C++Machine")).add("NewEdition!").encodeToBarray()

    val expected: Vector[Array[Byte]] = Vector(xbr4, xbr3, xbr5, xbr4)
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key1.key2..key3") {
    val expression: String = "..Book.SpecialEditions..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      39, 40, 38
    ), future.join().getValue)
  } //$..Book[:].SpecialEditions..Price -> checked

  //---------------------------------------------------------------------------------------------------//

  test("Inj ..key1.key2..key3") {
    val expression: String = "..Book.SpecialEditions..Price"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(39, 40, 38)", future.join().getValue.toString)
  } // Change is not perform because Book is an array, and the expression misses the Array specification

  test("Ex .key.*") {
    val expression: String = ".Store.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(br1.encodeToBarray(), br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj .key.*") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)

    val expression: String = ".Store.*"

    //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.:+("newField!"))
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
      val newL: List[Any] = l.:+("newField!")
      val res: ByteBuf = Mapper.encode(newL)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] = Vector(xbr1.add("newField!").encodeToBarray(), xbr2.add("newField!").encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key.*") {
    val expression: String = "SpecialEditions.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key.*") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)

    val expression: String = "SpecialEditions.*"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("newField!", 100))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb10.put("newField!", 100).encodeToBarray(), xb9.put("newField!", 100).encodeToBarray(), xb11.put("newField!", 100).encodeToBarray(), xb10.put("newField!", 100).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key[#].*") {
    val expression: String = "SpecialEditions[0 to end].*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine",
      39,
      "ScalaMachine",
      40,
      "C++Machine",
      38,
      "JavaMachine",
      39
    ), future.join().getValue)
  }

  test("Inj ..key[#].*") {
    val expression: String = "SpecialEditions[0 to end].*"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(JavaMachine, 39, ScalaMachine, 40, C++Machine, 38, JavaMachine, 39)", future.join().getValue.toString)
  } // Change is not perform because the values are not the same type

  test("Ex ..key1.*.key2") {
    val expression: String = "Book.*.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "Java",
      "Scala",
      "C++"
    ), future.join().getValue)
  }

  test("Inj ..key1.*.key2") {
    val expression: String = "Book.*.Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, Scala!!!, C++!!!)", future.join().getValue.toString)
  }

  test("Ex ..key1.*..key2") {
    val expression: String = "Book.*..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "Java",
      "JavaMachine",
      "Scala",
      "ScalaMachine",
      "C++",
      "C++Machine"
    ), future.join().getValue)
  }

  test("Inj ..key1.*..key2") {
    val expression: String = "Book.*..Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Java!!!, JavaMachine!!!, Scala!!!, ScalaMachine!!!, C++!!!, C++Machine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key1[#].*.key2") {
    val expression: String = "Book[0 to end].*.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(), future.join().getValue)
  }

  test("Inj ..key1[#].*.key2") {
    val expression: String = "Book[0 to end].*.Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector()", future.join().getValue.toString)
  }

  test("Ex ..key1[#].*..key2") {
    val expression: String = "Book[0 to end].*..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine",
      "ScalaMachine",
      "C++Machine"
    ), future.join().getValue)
  }

  test("Inj ..key1[#].*..key2") {
    val expression: String = "Book[0 to end].*..Title"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key1.*.key2[@elem]") {
    val expression: String = "Book.*.SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key1.*.key2[@elem]") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = "Book.*.SpecialEditions[@Price]"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb10.put("Street", 1000).encodeToBarray(), xb9.put("Street", 1000).encodeToBarray(), xb11.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key[@elem].*") {
    val expression: String = "SpecialEditions[@Price].*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine",
      39,
      "ScalaMachine",
      40,
      "C++Machine",
      38,
      "JavaMachine",
      39
    ), future.join().getValue)
  }

  test("Inj ..key[@elem].*") {
    val expression: String = "SpecialEditions[@Price].*"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(JavaMachine, 39, ScalaMachine, 40, C++Machine, 38, JavaMachine, 39)", future.join().getValue.toString)
  } // Change is not perform because the values are not the same type

  test("Ex ..key1[#].*.key2[@elem]") {
    val expression: String = "Book[0 to end].*..SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(), future.join().getValue)
  }

  test("Inj ..key1[#].*.key2[@elem]") {
    val expression: String = "Book[0 to end].*..SpecialEditions[@Price]"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector()", future.join().getValue.toString)
  }

  test("Ex ..key1.*.[#]") {
    val expression: String = "Book.*.[0 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(), future.join().getValue)
  }

  test("Inj ..key1.*.[#]") {
    val expression: String = "Book.*.[0 to end]"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector()", future.join().getValue.toString)
  }

  test("Ex ..key1[#].*.[#]") {
    val expression: String = "Book[0 to end].*.[0 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key1[#].*.[#]") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = "Book[0 to end].*.[0 to end]"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb10.put("Street", 1000).encodeToBarray(), xb9.put("Street", 1000).encodeToBarray(), xb11.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key1[#].*..[#]") {
    val expression: String = "Book[0 to end].*..[0 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key1[#].*..[#]") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = "Book[0 to end].*..[0 to end]"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb10.put("Street", 1000).encodeToBarray(), xb9.put("Street", 1000).encodeToBarray(), xb11.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key1[#].*..[#]..k*y2") {
    val expression: String = "Book[0 to end].*..[0 to end]..Ti*e"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine",
      "ScalaMachine",
      "C++Machine"
    ), future.join().getValue)
  }

  test("Inj ..key1[#].*..[#]..k*y2") {
    val expression: String = "Book[0 to end].*..[0 to end]..Ti*e"

    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!)", future.join().getValue.toString)
  }

  test("Ex ..key1[#].*.[#].key2") {
    val expression: String = "Book[0 to end].*..[0 to end].Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      39, 40, 38
    ), future.join().getValue)
  }

  test("Inj ..key1[#].*.[#].key2") {
    val expression: String = "Book[0 to end].*..[0 to end].Price"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(49, 50, 48)", future.join().getValue.toString)
  }

  test("Ex ..key1[#].*.*") {
    val expression: String = "Book[0 to end].*.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key1[#].*.*") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = "Book[0 to end].*.*"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb10.put("Street", 1000).encodeToBarray(), xb9.put("Street", 1000).encodeToBarray(), xb11.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key1.*.*") {
    val expression: String = "Book.*.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Any] = Vector("Java", 15.5, br4.encodeToBarray(), "Scala", 21.5, br3.encodeToBarray(), "C++", 12.6, br5.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Inj ..key1.*.*") {
    val expression: String = "Book.*.*"

    val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Any] = Vector("Java", 15.5, br4.encodeToBarray(), "Scala", 21.5, br3.encodeToBarray(), "C++", 12.6, br5.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  } // Change is not perform because the values are not the same type

  test("Ex .key1.*.*") {
    val expression: String = "Store.*.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b3.encodeToBarray(), b4.encodeToBarray(), b8.encodeToBarray(), b5.encodeToBarray(), b6.encodeToBarray(), b7.encodeToBarray(), b3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj .key1.*.*") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = "Store.*.*"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb3.put("Street", 1000).encodeToBarray(), xb4.put("Street", 1000).encodeToBarray(), xb8.put("Street", 1000).encodeToBarray(), xb5.put("Street", 1000).encodeToBarray(), xb6.put("Street", 1000).encodeToBarray(), xb7.put("Street", 1000).encodeToBarray(), xb3.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .key1.*.*.*.*") {
    val expression: String = "Store.*.*.*.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    val expected: Vector[Array[Byte]] = Vector(b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj .key1.*.*.*.*") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = "Store.*.*.*.*"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)


    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb10.put("Street", 1000).encodeToBarray(), xb9.put("Street", 1000).encodeToBarray(), xb11.put("Street", 1000).encodeToBarray(), xb10.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex .*") {
    val expression: String = ".*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    val expected: Vector[Array[Byte]] = Vector(b2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //$.* -> checked

  test("Inj .*") {
    val xb11: BsonObject = new BsonObject().put("Price", 38).put("Title", "C++Machine")
    val xbr5: BsonArray = new BsonArray().add(xb11)
    val xb10: BsonObject = new BsonObject().put("Price", 39).put("Title", "JavaMachine")
    val xbr4: BsonArray = new BsonArray().add(xb10)
    val xb9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
    val xbr3: BsonArray = new BsonArray().add(xb9)
    val xb7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val xb6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val xb5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val xb4: BsonObject = new BsonObject().put("Pri", 21.5).put("SpecialEditions", xbr3).put("Title", "Scala")
    val xb3: BsonObject = new BsonObject().put("Price", 15.5).put("SpecialEditions", xbr4).put("Title", "Java")
    val xb8: BsonObject = new BsonObject().put("Price", 12.6).put("SpecialEditions", xbr5).put("Title", "C++")
    val xbr1: BsonArray = new BsonArray().add(xb3).add(xb4).add(xb8)
    val xbr2: BsonArray = new BsonArray().add(xb5).add(xb6).add(xb7).add(xb3)
    val xb2: BsonObject = new BsonObject().put("Book", xbr1).put("Hatk", xbr2)
    val xbsonEvent: BsonObject = new BsonObject().put("Store", xb2)
    val expression: String = ".*"
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Array[Byte]] =
      Vector(xb2.put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  //    test("Ex ..*"){
  //      val expression: String = "..*"
  //      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  //      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
  //      boson.go(validatedByteArr)
  //
  //      assertEquals(Vector(
  //        Map("Book" -> Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
  //          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
  //          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
  //          "Hatk" -> Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38),
  //            Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39)))))
  //      ), future.join().getValue)
  //    } //TODO: Not implemented yet, so this test is wrong

  //    test("Inj ..* V1"){
  //      val expression: String = "..*"
  //
  //      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
  //        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
  //        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
  //        val newM: Map[String, Any] = m.+(("Street", 1000))
  //        val res: ByteBuf = Mapper.encode(newM)
  //        if(res.hasArray)
  //          res.array()
  //        else {
  //          val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
  //          val array: Array[Byte] = buf.array()
  //          buf.release()
  //          array
  //        }
  //      })
  //      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
  //      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
  //      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
  //      boson.go(injFuture.join())
  //
  //      assertEquals("Vector(Map(Book -> List(Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39))), Map(Title -> Scala, Pri -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40))), Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38)))), Hatk -> List(Map(Color -> Red, Price -> 48), Map(Color -> White, Price -> 35), Map(Color -> Blue, Price -> 38), Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39))))))", future.join().getValue.toString)
  //    } // No change is perform because the values are not the same type //TODO: Not implemented yet(extractor), so this test is wrong

  /*test("Inj ..* V1"){
    val expression: String = "..*"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
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
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Map(Book -> List(Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39))), Map(Title -> Scala, Pri -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40))), Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38)))), Hatk -> List(Map(Color -> Red, Price -> 48), Map(Color -> White, Price -> 35), Map(Color -> Blue, Price -> 38), Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39))))))", future.join().getValue.toString)
  } // No change is perform because the values are not the same type*/
  ////TODO: Not implemented yet(extractor), so this test is wrong

  test("Inj ..* V2") {
    val expression: String = "..*"
    val rootx: BsonObject = new BsonObject().put("field1", "OneWord!!").put("field2", "TwoWords!!").put("field3", "ThreeWords!!").put("field4", "FourWords!!")
    val root: BsonObject = new BsonObject().put("field1", "OneWord").put("field2", "TwoWords").put("field3", "ThreeWords").put("field4", "FourWords")
    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!"))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(root.encodeToBarray())
    assertTrue(rootx.encodeToBarray().zip(injFuture.join()).forall(p => p._1 == p._2))
  }

  test("Inj ..* V3") {
    val expression: String = "..*"
    val field2x: BsonObject = new BsonObject().put("field4", new BsonObject().put("newField", 10)).put("newField", 10)
    val field1x: BsonObject = new BsonObject().put("field3", new BsonObject().put("newField", 10)).put("newField", 10)
    val rootx: BsonObject = new BsonObject().put("field1", field1x).put("field2", field2x)


    val field2: BsonObject = new BsonObject().put("field4", new BsonObject())
    val field1: BsonObject = new BsonObject().put("field3", new BsonObject())
    val root: BsonObject = new BsonObject().put("field1", field1).put("field2", field2)

    //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.+:("ADDED"))
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newL: Map[String, Any] = l.+(("newField", 10))
      val res: ByteBuf = Mapper.encode(newL)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(root.encodeToBarray())

    rootx.encodeToBarray().zip(injFuture.join()).forall(p => p._1 == p._2)

    assertTrue(rootx.encodeToBarray().zip(injFuture.join()).forall(p => p._1 == p._2))
  }

  test("Ex ..key, but multiple keys with same name") {
    val obj2: BsonObject = new BsonObject().put("Store", 1000L)
    val obj1: BsonObject = new BsonObject().put("Store", obj2)
    val expression: String = "..Store"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj1.encodeToBarray())

    val expected: Vector[Any] = Vector(obj2.encodeToBarray(), 1000L)
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Inj ..key, but multiple keys with same name V1") {
    val obj2: BsonObject = new BsonObject().put("Store", 1000L)
    val obj1: BsonObject = new BsonObject().put("Store", obj2)
    val expression: String = "..Store"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(obj1.encodeToBarray())

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val expected: Vector[Any] = Vector(obj2.encodeToBarray(), 1000L)
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Inj ..key, but multiple keys with same name V2") {
    val obj22: BsonObject = new BsonObject().put("Store", new BsonObject())
    val obj11: BsonObject = new BsonObject().put("Store", obj22)
    println(obj11)
    val expression: String = "..Store"

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(obj11.encodeToBarray())

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val xobj22: BsonObject = new BsonObject().put("Store", new BsonObject().put("Street", 1000))

    val expected: Vector[Array[Byte]] =
      Vector(xobj22.put("Street", 1000).encodeToBarray(), new BsonObject().put("Street", 1000).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Inj ..key1[@elem1]") {
    val obj555: BsonObject = new BsonObject().put("Store", new BsonArray())
    val arr444: BsonArray = new BsonArray().add(obj555).add(obj555)
    val obj333: BsonObject = new BsonObject().put("Store", arr444)
    val arr222: BsonArray = new BsonArray().add(obj333).add(obj333)
    //put("Store",new BsonObject())
    val obj111: BsonObject = new BsonObject().put("Store", arr222)
    val expression: String = "..Store[@Store]"
    println(obj111)

    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)

      val newM: Map[String, Any] = m.+(("Street", 1000))
      val res: ByteBuf = Mapper.encode(newM)
      if (res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(obj111.encodeToBarray())

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    val xobj555: BsonObject = new BsonObject().put("Store", new BsonArray()).put("Street", 1000)
    val xobj333: BsonObject = new BsonObject().put("Store", new BsonArray().add(xobj555).add(xobj555)).put("Street", 1000)

    val expected: Vector[Array[Byte]] = Vector(xobj333.encodeToBarray(), xobj555.encodeToBarray(), xobj555.encodeToBarray(), xobj333.encodeToBarray(), xobj555.encodeToBarray(), xobj555.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex ..key1[@elem1]..key2[@elem2]") {
    val obj555: BsonObject = new BsonObject().put("Store", new BsonArray())
    val arr444: BsonArray = new BsonArray().add(obj555).add(obj555)
    val obj333: BsonObject = new BsonObject().put("Store", arr444)
    val arr222: BsonArray = new BsonArray().add(obj333).add(obj333)
    //put("Store",new BsonObject())
    val obj111: BsonObject = new BsonObject().put("Store", arr222)
    val expression: String = "..Store[@Store]..Store[@Store]"
    println(obj111)

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj111.encodeToBarray())

    val expected: Vector[Array[Byte]] = Vector(obj555.encodeToBarray(), obj555.encodeToBarray(), obj555.encodeToBarray(), obj555.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Byte]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Ex '.' BsonObject Root") {
    val expression: String = "."
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(bsonEvent.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex '.' BsonArray Root") {
    val expression: String = "."
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(br1.encodeToBarray())

    val expected: Vector[Any] = Vector(br1.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex .[first]") {
    val expression: String = ".[first]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(br1.encodeToBarray())

    val expected: Vector[Any] = Vector(b3.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex key1.key2[all]") {
    val expression: String = "Store.Book[all]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b3.encodeToBarray(),b4.encodeToBarray(),b8.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex key[end] V1") {
    val expression: String = "Book[end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b8.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [end]") {
    val expression: String = "[end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b8.encodeToBarray(), b10.encodeToBarray(), b9.encodeToBarray(), b11.encodeToBarray(), b3.encodeToBarray(), b10.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [end]..[end]") {
    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39).put("SpecialEditions", br5)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("SpecialEditions", br4).put("Title", "ScalaMachine").put("Price", 40)
    val br3: BsonArray = new BsonArray().add(b9).add(b10)
    val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
    val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
    val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
    val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
    val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
    val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
    val bsonEvent: BsonObject = new BsonObject().put("Store", b2)
    val expression: String = "[end]..[end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b11.encodeToBarray(), b11.encodeToBarray(), b11.encodeToBarray(), b11.encodeToBarray(), b10.encodeToBarray(), b11.encodeToBarray(), b11.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [end]..Key1") {
    val expression: String = "[end]..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector("C++",
      "C++Machine",
      "JavaMachine",
      "ScalaMachine",
      "C++Machine",
      "Java",
      "JavaMachine",
      "JavaMachine")
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [end]..[end].Key") {
    val expression: String = "[end]..[end].Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(38,39)
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [#to end]..[end]..*[@elem]") {
    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39).put("SpecialEditions", br5)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("SpecialEditions", br4).put("Title", "ScalaMachine").put("Price", 40)
    val br3: BsonArray = new BsonArray().add(b9).add(b10)
    val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
    val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
    val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
    val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
    val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
    val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
    val bsonEvent: BsonObject = new BsonObject().put("Store", b2)
    val expression: String = "[1 to end]..[end]..Special*[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b11.encodeToBarray(),b11.encodeToBarray(),b11.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [#to#]..[end]..*[@elem]") {
    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39).put("SpecialEditions", br5)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("SpecialEditions", br4).put("Title", "ScalaMachine").put("Price", 40)
    val br3: BsonArray = new BsonArray().add(b9).add(b10)
    val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
    val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
    val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
    val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
    val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
    val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
    val bsonEvent: BsonObject = new BsonObject().put("Store", b2)
    val expression: String = "[1]..[end]..Specia*ditions[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b11.encodeToBarray(),b11.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [end]..[end]..*[@elem]") {
    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39).put("SpecialEditions", br5)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("SpecialEditions", br4).put("Title", "ScalaMachine").put("Price", 40)
    val br3: BsonArray = new BsonArray().add(b9).add(b10)
    val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
    val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
    val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
    val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
    val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
    val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
    val bsonEvent: BsonObject = new BsonObject().put("Store", b2)
    val expression: String = "[end]..[end]..SpecialEditions[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b11.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex [# to end]..Key1") {
    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("SpecialEditions", br5).put("Title", "JavaMachine").put("Price", 39)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("SpecialEditions", br4).put("Title", "ScalaMachine").put("Price", 40)
    val br3: BsonArray = new BsonArray().add(b9).add(b10)
    val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
    val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
    val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
    val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
    val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
    val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
    val bsonEvent: BsonObject = new BsonObject().put("Store", b2)
    val expression: String = "[1 to end]..SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(
      b11.encodeToBarray(),
      b10.encodeToBarray(),
      b9.encodeToBarray(),
      b11.encodeToBarray(),
      b10.encodeToBarray(),
      b11.encodeToBarray(),
      b11.encodeToBarray(),
      b11.encodeToBarray(),
      b10.encodeToBarray()
    )
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) =>
        e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex key1[end]..key2 END has Obj") {
    val expression: String = "Book[end]..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector("C++","C++Machine")
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex key1[end]..key2 END has Arr") {
    val arr: BsonArray = new BsonArray().add(b3).add(b4).add(new BsonArray().add(new BsonObject().put("field", "value")))
    val expression: String = "[end]..field"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(arr.encodeToBarray())

    val expected: Vector[Any] = Vector("value","value")
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex Coverage all types inside Array") {
    val arr: BsonArray = new BsonArray().add(true).add(1.1).add("match").add(new BsonArray().add(new BsonObject().put("field", "value"))).add(new BsonObject().put("field", "value")).addNull().add(2).add(1500L)
    val obj: BsonObject = new BsonObject().put("field", arr)
    val expression: String = ".field.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj.encodeToBarray())

    val expected: Vector[Any] = Vector(
      true,
      1.1,
      "match",
      new BsonArray().add(new BsonObject().put("field", "value")).encodeToBarray(),
      new BsonObject().put("field", "value").encodeToBarray(),
      V_NULL,
      2,
      1500L
    )
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) if e == null && r == null => true
      case (e, r) => e.equals(r)
    })
  }

  test("Ex Coverage of expression key[@elem] case 1") {
    val arr: BsonArray = new BsonArray().add(new BsonObject().put("field", new BsonObject()).put("some", new BsonArray().add(2)))
    val obj: BsonObject = new BsonObject().put("This", arr)
    val expression: String = ".This[@some]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj.encodeToBarray())

    val expected: Vector[Any] = Vector(new BsonObject().put("field", new BsonObject()).put("some", new BsonArray().add(2)).encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex Coverage of expression key[@elem] case 2") {
    val arr: BsonArray = new BsonArray().add(new BsonObject().put("field", new BsonObject()).put("some", new BsonArray().add(new BsonObject().put("This", new BsonArray().add(new BsonObject().put("This","Tested"))))))
    val obj: BsonObject = new BsonObject().put("This", arr)
    val expression: String = "This[@This]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj.encodeToBarray())

    val expected: Vector[Any] = Vector(new BsonObject().put("This","Tested").encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex Coverage of expression key[@elem] case 3") {
    val arr: BsonArray = new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("This", new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()))))
    val obj: BsonObject = new BsonObject().put("This", arr)
    val expression: String = "This[@some]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj.encodeToBarray())

    val expected: Vector[Any] = Vector(new BsonObject().put("some", new BsonObject()).put("This", new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()))).encodeToBarray(), new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()).encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex Coverage of expression key[@elem] case 4") {
    val arr: BsonArray = new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("field", new BsonArray().add(2)))
    val obj: BsonObject = new BsonObject().put("This", arr)
    val expression: String = ".This[@some]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(obj.encodeToBarray())

    val expected: Vector[Any] = Vector(new BsonObject().put("some", new BsonObject()).put("field", new BsonArray().add(2)).encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

  test("Ex...") {
    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39).put("SpecialEditions", br5)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("SpecialEditions", br4).put("Title", "ScalaMachine").put("Price", 40)
    val br3: BsonArray = new BsonArray().add(b9).add(b10)
    val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
    val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
    val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
    val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
    val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
    val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
    val bsonEvent: BsonObject = new BsonObject().put("Store", b2)
    val expression: String = "SpecialEditions[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(bsonEvent.encodeToBarray())

    val expected: Vector[Any] = Vector(b10.encodeToBarray(), b11.encodeToBarray())
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    println(s"res: $res")
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall {
      case (e: Array[Byte], r: Array[Byte]) => e.sameElements(r)
      case (e, r) => e.equals(r)
    })
  }

}
