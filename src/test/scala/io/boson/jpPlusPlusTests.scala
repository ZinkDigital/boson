package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch, TimeUnit}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import io.boson.json.Joson
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector

import mapper.Mapper
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class jpPlusPlusTests extends FunSuite{
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID )


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

  val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

    test("Ex .key"){
      val expression = ".Store"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        Map("Book" -> Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
          "Hatk" -> Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38),
            Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39)))))
      ), future.join().getValue)
    } //$.Store -> checked

    test("Inj .key"){
      val expression: String = ".Store"

      //val bosonI: Boson = Boson.injector(expression, (x: Map[String, Any]) => x.+(("Street?", "im Lost")))

      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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
      val bosonE: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      bosonE.go(injFuture.join())

      assertEquals("Vector(Map(Book -> List(Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java), Map(Pri -> 21.5, SpecialEditions -> List(Map(Price -> 40, Title -> ScalaMachine)), Title -> Scala), Map(Price -> 12.6, SpecialEditions -> List(Map(Price -> 38, Title -> C++Machine)), Title -> C++)), Hatk -> List(Map(Color -> Red, Price -> 48), Map(Color -> White, Price -> 35), Map(Color -> Blue, Price -> 38), Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java)), Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex .key1.key2"){
      val expression = ".Store.Book"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38))))
      ), future.join().getValue)
    } //$.Store.Book -> checked

    test("Inj .key1.key2"){
      val expression: String = ".Store.Book"

      //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.+:("Street?"))

      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
        val newL: List[Any] = l.+:("Street?")
        val res: ByteBuf = Mapper.encode(newL)
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

      assertEquals("Vector(List(Street?, Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java), Map(Pri -> 21.5, SpecialEditions -> List(Map(Price -> 40, Title -> ScalaMachine)), Title -> Scala), Map(Price -> 12.6, SpecialEditions -> List(Map(Price -> 38, Title -> C++Machine)), Title -> C++)))", future.join().getValue.toString)
    }

    test("Ex .key1.key2[@elem1].key3[@elem2]"){
      val expression: String = ".Store.Book[@Price].SpecialEditions[@Title]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "C++Machine", "Price" -> 38)
      ), future.join().getValue)
    } //$.Store.Book[?(@.Price)].SpecialEditions[?(@.Title)] -> checked

    test("Inj .key1.key2[@elem1].key3[@elem2]"){
      val expression: String = ".Store.Book[@Price].SpecialEditions[@Title]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost), Map(Price -> 38, Title -> C++Machine, Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex .key1.key2[#]"){
      val expression: String = ".Store.Book[1]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        List(Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> List(Map("Title" -> "ScalaMachine", "Price" -> 40))))
      ), future.join().getValue)
    } //$.Store.Book[1] -> checked

    test("Inj .key1.key2[#]"){
      val expression: String = ".Store.Book[1]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(List(Map(Pri -> 21.5, SpecialEditions -> List(Map(Price -> 40, Title -> ScalaMachine)), Title -> Scala, Street? -> im Lost)))", future.join().getValue.toString)
    }

    test("Ex .key1.key2[# to end].key3"){
      val expression: String = ".Store.Book[0 to end].Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        15.5, 12.6
      ), future.join().getValue)
    } //$.Store.Book[:].Price -> checked

    test("Inj .key1.key2[# to end].key3"){
      val expression: String = ".Store.Book[0 to end].Price"

      val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(25.5, 22.6)", future.join().getValue.toString)
    }

    test("Ex .key1.key2[@elem].key3"){
      val expression: String = ".Store.Book[@Price].Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        "Java", "C++"
      ), future.join().getValue)
    } //$.Store.Book[?(@.Price)].Title -> checked

    test("Inj .key1.key2[@elem].key3"){
      val expression: String = ".Store.Book[@Price].Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, C++!!!)", future.join().getValue.toString)
    }

    test("Ex .key1.key2[#].key3[@elem].k*y"){
      val expression: String = ".Store.Book[0 to end].SpecialEditions[@Price].T*le"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        "JavaMachine", "ScalaMachine", "C++Machine"
      ), future.join().getValue)
    } //$.Store.Book[:].SpecialEditions[?(@.Price)].Title -> checked

    //-----------------------------------------------------------------------------------------------------//

    test("Inj .key1.key2[#].key3[@elem].k*y"){
      val expression: String = ".Store.Book[0 to end].SpecialEditions[@Price].T*le"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key V1"){
      val expression: String = "..Title" // || "Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        "Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine", "Java", "JavaMachine"
      ), future.join().getValue)
    } //$..Title -> checked

    test("Inj ..key V1"){
      val expression: String = "..Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, JavaMachine!!!, Scala!!!, ScalaMachine!!!, C++!!!, C++Machine!!!, Java!!!, JavaMachine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key V2"){
      val expression: String = "..Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
      ), future.join().getValue)
    } //$..Price -> checked

    test("Inj ..key V2"){
      val expression: String = "..Book.*.Price"

      val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(25.5, 22.6)", future.join().getValue.toString)
    }

    test("Ex ..key1.key2"){
      val expression: String = "..Book.Title" //  || "Book.Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        "Java", "Scala", "C++"
      ), future.join().getValue)
    } //$..Book[:].Title -> checked

    test("Inj ..key1.key2"){
      val expression: String = "..Book.Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java, Scala, C++)", future.join().getValue.toString)
    } //No change is perform, because Book is a Array.

    test("Ex ..key1[# to end].key2"){
      val expression: String = "..Book[0 to end].Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        15.5, 12.6
      ), future.join().getValue)
    } //$..Book[:].Price -> checked

    test("Inj ..key1[# to end].key2"){
      val expression: String = "..Book[0 to end].Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, Scala!!!, C++!!!)", future.join().getValue.toString)
    } //Change is perform, because Book is a Array.

    test("Ex ..key[@elem].key"){
      val expression: String = "..SpecialEditions[@Price].Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        "JavaMachine", "ScalaMachine", "C++Machine", "JavaMachine"
      ), future.join().getValue)
    } //$..SpecialEditions[?(@.Price)].Title -> checked

    test("Inj ..key[@elem].key"){
      val expression: String = "..SpecialEditions[@Price].Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!, JavaMachine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key[@elem]"){
      val expression: String = "..SpecialEditions[@Price]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38),
        Map("Title" -> "JavaMachine", "Price" -> 39)
      ), future.join().getValue)
    } //$..SpecialEditions[?(@.Price)] -> checked

    test("Inj ..key[@elem]"){
      val expression: String = "..SpecialEditions[@Price]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
       val b: BosonImpl = new BosonImpl(byteArray = Option(x))
       val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
       val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost), Map(Price -> 40, Title -> ScalaMachine, Street? -> im Lost), Map(Price -> 38, Title -> C++Machine, Street? -> im Lost), Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex ..key[#] V1"){
      val expression: String = "..SpecialEditions[0]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        List(Map("Title" -> "JavaMachine", "Price" -> 39)), List(Map("Title" -> "ScalaMachine", "Price" -> 40)), List(Map("Title" -> "C++Machine", "Price" -> 38)), List(Map("Title" -> "JavaMachine", "Price" -> 39))
      ), future.join().getValue)
    } //$..SpecialEditions[0] -> checked

    test("Inj ..key[#] V1"){
      val expression: String = "..SpecialEditions[0]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(List(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost)), List(Map(Price -> 40, Title -> ScalaMachine, Street? -> im Lost)), List(Map(Price -> 38, Title -> C++Machine, Street? -> im Lost)), List(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost)))", future.join().getValue.toString)
    }

    test("Ex ..key[#] V2"){
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

      assertEquals(Vector(Seq(Map("fridgeTemp" -> 15)), Seq(Map("fridgeTemp" -> 12)), Seq(Map("fridgeTemp" -> 18))), future.join().getValue)
    }

    test("Inj ..key[#] V2"){
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
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(bsonEvent.encodeToBarray())

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(List(Map(fridgeTemp -> 15, Street? -> im Lost)), List(Map(fridgeTemp -> 12, Street? -> im Lost)), List(Map(fridgeTemp -> 18, Street? -> im Lost)))", future.join().getValue.toString)
    }

    test("Ex ..*y1[@elem1].key2[@elem2]"){
      val expression: String = "..*k[@Price].SpecialEditions[@Price]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "C++Machine", "Price" -> 38),
        Map("Title" -> "JavaMachine", "Price" -> 39)
      ), future.join().getValue)
    } //$..Book[?(@.Price)].SpecialEditions[?(@.Price)] && $..Hatk[?(@.Price)].SpecialEditions[?(@.Price)] -> checked

    test("Inj ..*y1[@elem1].key2[@elem2]"){
      val expression: String = "..*k[@Price].SpecialEditions[@Price]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost), Map(Price -> 38, Title -> C++Machine, Street? -> im Lost), Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex ..*y1[@elem1].key2"){
      val expression: String = "..*k[@SpecialEditions].Pr*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        15.5, 21.5, 12.6, 15.5
      ), future.join().getValue)
    } //$..Book[?(@.SpecialEditions)].Price && $..Hatk[?(@.SpecialEditions)].Price -> checked

    //---------------------------------------------------------------------------------------------------//

    test("Inj ..*y1[@elem1].key2"){
      val expression: String = "..*k[@SpecialEditions].Pr*"

      val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(25.5, 31.5, 22.6, 25.5)", future.join().getValue.toString)
    }

    test("Ex .key1..key2"){
      val expression: String = ".Store..Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
      ), future.join().getValue)
    } //$.Store..Price -> checked

    test("Inj .key1..key2"){
      //No Change is perform, because not all values are of the same type
      val expression: String = ".Store..Price"

      val bosonI: Boson = Boson.injector(expression, (x: Double) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39)", future.join().getValue.toString)
    } //No Change is perform, because not all values are of the same type

    test("Ex .key1..key2[@elem]"){
      val expression: String = ".Store..SpecialEditions[@Price]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38),
        Map("Title" -> "JavaMachine", "Price" -> 39)
      ), future.join().getValue)
    } //$.Store..SpecialEditions[?(@.Price)] -> checked

    test("Inj .key1..key2[@elem]"){
      //No Change is perform, because not all values are of the same type
      val expression: String = ".Store..SpecialEditions[@Price]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost), Map(Price -> 40, Title -> ScalaMachine, Street? -> im Lost), Map(Price -> 38, Title -> C++Machine, Street? -> im Lost), Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex .key1..key2[#]"){
      val expression: String = ".Store..SpecialEditions[0]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Seq(Map("Title" -> "JavaMachine", "Price" -> 39)),
        Seq(Map("Title" -> "ScalaMachine", "Price" -> 40)),
        Seq(Map("Title" -> "C++Machine", "Price" -> 38)),
        Seq(Map("Title" -> "JavaMachine", "Price" -> 39))
      ), future.join().getValue)
    } //$.Store..SpecialEditions[0] -> checked

    test("Inj .key1..key2[#]"){
      //No Change is perform, because not all values are of the same type
      val expression: String = ".Store..SpecialEditions[0]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(List(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost)), List(Map(Price -> 40, Title -> ScalaMachine, Street? -> im Lost)), List(Map(Price -> 38, Title -> C++Machine, Street? -> im Lost)), List(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost)))", future.join().getValue.toString)
    }

    test("Ex .key1..key2[#]..key3"){
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

    test("Inj .key1..key2[#]..key3"){
      val expression: String = ".Store..Book[0 until end]..Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, JavaMachine!!!, Scala!!!, ScalaMachine!!!)", future.join().getValue.toString)
    }

    test("Ex .key1..key2[#]..key3[@elem]"){
      val expression: String = ".Store..Book[1 until end]..SpecialEditions[@Price]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "ScalaMachine", "Price" -> 40)
      ), future.join().getValue)
    } //$.Store..Book[1:2]..SpecialEditions[?(@.Price)] -> checked

    test("Inj .key1..key2[#]..key3[@elem]"){
      val expression: String = ".Store..Book[1 until end]..SpecialEditions[@Price]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(Map(Price -> 40, Title -> ScalaMachine, Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex .*y1..*y2[#]..*y3..key4"){
      val expression: String = ".*ore..*k[1 until end]..*Editions..Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector("ScalaMachine"), future.join().getValue)
    } //$.Store..Book[1:2]..SpecialEditions..Title -> checked

    test("Inj .*y1..*y2[#]..*y3..key4"){
      val expression: String = ".*ore..*k[1 until end]..*Editions..Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
      //println("injFuture=" + new String(injFuture.join()))

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(ScalaMachine!!!)", future.join().getValue.toString)
    }

    test("Ex .key1..key2[@elem1]..key3[@elem2]"){
      val expression: String = ".Store..*k[@Price]..SpecialEditions[@Title]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "C++Machine", "Price" -> 38),
        Map("Title" -> "JavaMachine", "Price" -> 39)
      ), future.join().getValue)
    } //$.Store..Book[?(@.Price)]..SpecialEditions[?(@.Title)] && $.Store..Hatk[?(@.Price)]..SpecialEditions[?(@.Title)] -> checked

    //---------------------------------------------------------------------------------------------------//

    test("Inj .key1..key2[@elem1]..key3[@elem2]"){
      val expression: String = ".Store..*k[@Price]..SpecialEditions[@Title]"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("Street?", "im Lost"))
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost), Map(Price -> 38, Title -> C++Machine, Street? -> im Lost), Map(Price -> 39, Title -> JavaMachine, Street? -> im Lost))", future.join().getValue.toString)
    }

    test("Ex ..key1[#]..key2"){
      val expression: String = "..Book[0 to end]..Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        15.5, 39, 40, 12.6, 38
      ), future.join().getValue)
    } //$..Book[:]..Price -> checked

    test("Inj ..key1[#]..key2"){
      val expression: String = "..Book[0 to end]..Price"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
      //println("injFuture=" + new String(injFuture.join()))

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(15.5, 39, 40, 12.6, 38)", future.join().getValue.toString)
    } //No Change is perform, because not all values are of the same type

    test("Ex ..key1..key2"){
      val expression: String = "Store..Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
      ), future.join().getValue)
    } //$..Store..Price -> checked

    test("Inj ..key1..key2"){
      val expression: String = "Store..Price"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
     // println("injFuture=" + new String(injFuture.join()))

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39)", future.join().getValue.toString)
    } //No Change is perform, because not all values are of the same type

    test("Ex ..key1[@elem]..key2"){
      val expression: String = "Book[@Price]..Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        "Java", "JavaMachine", "C++", "C++Machine"
      ), future.join().getValue)
    } //$..Book[?(@.Price)]..Title -> checked

    test("Inj ..key1[@elem]..key2"){
      val expression: String = "Book[@Price]..Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, JavaMachine!!!, C++!!!, C++Machine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key1..*ey2..*ey3"){
      val expression: String = "Store..*k..*itions"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        List(Map("Title" -> "JavaMachine", "Price" -> 39)),
        List(Map("Title" -> "ScalaMachine", "Price" -> 40)),
        List(Map("Title" -> "C++Machine", "Price" -> 38)),
        List(Map("Title" -> "JavaMachine", "Price" -> 39))
      ), future.join().getValue)
    } //$..Store..Book..SpecialEditions && $..Store..Hatk..SpecialEditions -> checked

    test("Inj ..key1..*ey2..*ey3"){
      val expression: String = "Store..*k..*itions"

      //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.:+("NewEdition!"))
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
        val newL: List[Any] = l.:+("NewEdition!")
        val res: ByteBuf = Mapper.encode(newL)
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
     // println("injFuture=" + new String(injFuture.join()))
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(List(Map(Price -> 39, Title -> JavaMachine), NewEdition!), List(Map(Price -> 40, Title -> ScalaMachine), NewEdition!), List(Map(Price -> 38, Title -> C++Machine), NewEdition!), List(Map(Price -> 39, Title -> JavaMachine), NewEdition!))", future.join().getValue.toString)
    }

    test("Ex ..key1.key2..key3"){
      val expression: String = "..Book.SpecialEditions..Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        39, 40, 38
      ), future.join().getValue)
    } //$..Book[:].SpecialEditions..Price -> checked

    //---------------------------------------------------------------------------------------------------//

    test("Inj ..key1.key2..key3"){
      val expression: String = "..Book.SpecialEditions..Price"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(39, 40, 38)", future.join().getValue.toString)
    } // No change is perform because Book is an array, and the expression misses the Array specification

    test("Ex .key.*"){
      val expression: String = ".Store.*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
        Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38),
          Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))))
      ), future.join().getValue)
    }

    test("Inj .key.*"){
      val expression: String = ".Store.*"

      //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.:+("newField!"))
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
        val newL: List[Any] = l.:+("newField!")
        val res: ByteBuf = Mapper.encode(newL)
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

      assertEquals("Vector(List(Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java), Map(Pri -> 21.5, SpecialEditions -> List(Map(Price -> 40, Title -> ScalaMachine)), Title -> Scala), Map(Price -> 12.6, SpecialEditions -> List(Map(Price -> 38, Title -> C++Machine)), Title -> C++), newField!), List(Map(Color -> Red, Price -> 48), Map(Color -> White, Price -> 35), Map(Color -> Blue, Price -> 38), Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java), newField!))", future.join().getValue.toString)
    }

    test("Ex ..key.*"){
      val expression: String = "SpecialEditions.*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38),
        Map("Title" -> "JavaMachine", "Price" -> 39)
      ), future.join().getValue)
    }

    test("Inj ..key.*"){
      val expression: String = "SpecialEditions.*"
      val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
        val b: BosonImpl = new BosonImpl(byteArray = Option(x))
        val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
        val newM: Map[String, Any] = m.+(("newField!", 100))
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, newField! -> 100), Map(Price -> 40, Title -> ScalaMachine, newField! -> 100), Map(Price -> 38, Title -> C++Machine, newField! -> 100), Map(Price -> 39, Title -> JavaMachine, newField! -> 100))", future.join().getValue.toString)
    }

    test("Ex ..key[#].*"){
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

    test("Inj ..key[#].*"){
      val expression: String = "SpecialEditions[0 to end].*"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(JavaMachine, 39, ScalaMachine, 40, C++Machine, 38, JavaMachine, 39)", future.join().getValue.toString)
    } // No change is perform because the values are not the same type

    test("Ex ..key1.*.key2"){
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

    test("Inj ..key1.*.key2"){
      val expression: String = "Book.*.Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, Scala!!!, C++!!!)", future.join().getValue.toString)
    }

    test("Ex ..key1.*..key2"){
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

    test("Inj ..key1.*..key2"){
      val expression: String = "Book.*..Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java!!!, JavaMachine!!!, Scala!!!, ScalaMachine!!!, C++!!!, C++Machine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key1[#].*.key2"){
      val expression: String = "Book[0 to end].*.Title"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(), future.join().getValue)
    }

    test("Inj ..key1[#].*.key2"){
      val expression: String = "Book[0 to end].*.Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector()", future.join().getValue.toString)
    }

    test("Ex ..key1[#].*..key2"){
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

    test("Inj ..key1[#].*..key2"){
      val expression: String = "Book[0 to end].*..Title"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key1.*.key2[@elem]"){
      val expression: String = "Book.*.SpecialEditions[@Price]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38)
      ), future.join().getValue)
    }

    test("Inj ..key1.*.key2[@elem]"){
      val expression: String = "Book.*.SpecialEditions[@Price]"
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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street -> 1000), Map(Price -> 40, Title -> ScalaMachine, Street -> 1000), Map(Price -> 38, Title -> C++Machine, Street -> 1000))", future.join().getValue.toString)
    }

    test("Ex ..key[@elem].*"){
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

    test("Inj ..key[@elem].*"){
      val expression: String = "SpecialEditions[@Price].*"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(JavaMachine, 39, ScalaMachine, 40, C++Machine, 38, JavaMachine, 39)", future.join().getValue.toString)
    } // No change is perform because the values are not the same type

    test("Ex ..key1[#].*.key2[@elem]"){
      val expression: String = "Book[0 to end].*..SpecialEditions[@Price]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(), future.join().getValue)
    }

    test("Inj ..key1[#].*.key2[@elem]"){
      val expression: String = "Book[0 to end].*..SpecialEditions[@Price]"

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

      assertEquals("Vector()", future.join().getValue.toString)
    }

    test("Ex ..key1.*.[#]"){
      val expression: String = "Book.*.[0 to end]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(), future.join().getValue)
    }

    test("Inj ..key1.*.[#]"){
      val expression: String = "Book.*.[0 to end]"

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

      assertEquals("Vector()", future.join().getValue.toString)
    } //Root is not a BsonArray

    test("Ex ..key1[#].*.[#]"){
      val expression: String = "Book[0 to end].*.[0 to end]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38)
      ), future.join().getValue)
    }

    test("Inj ..key1[#].*.[#]"){
      val expression: String = "Book[0 to end].*.[0 to end]"

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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street -> 1000), Map(Price -> 40, Title -> ScalaMachine, Street -> 1000), Map(Price -> 38, Title -> C++Machine, Street -> 1000))", future.join().getValue.toString)
    }

    test("Ex ..key1[#].*..[#]"){
      val expression: String = "Book[0 to end].*..[0 to end]"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38)
      ), future.join().getValue)
    }

    test("Inj ..key1[#].*..[#]"){
      val expression: String = "Book[0 to end].*..[0 to end]"

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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street -> 1000), Map(Price -> 40, Title -> ScalaMachine, Street -> 1000), Map(Price -> 38, Title -> C++Machine, Street -> 1000))", future.join().getValue.toString)
    }

    test("Ex ..key1[#].*..[#]..k*y2"){
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

    test("Inj ..key1[#].*..[#]..k*y2"){
      val expression: String = "Book[0 to end].*..[0 to end]..Ti*e"

      val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(JavaMachine!!!, ScalaMachine!!!, C++Machine!!!)", future.join().getValue.toString)
    }

    test("Ex ..key1[#].*.[#].key2"){
      val expression: String = "Book[0 to end].*..[0 to end].Price"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        39, 40, 38
      ), future.join().getValue)
    }

    test("Inj ..key1[#].*.[#].key2"){
      val expression: String = "Book[0 to end].*..[0 to end].Price"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(49, 50, 48)", future.join().getValue.toString)
    }

    test("Ex ..key1[#].*.*"){
      val expression: String = "Book[0 to end].*.*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38)
      ), future.join().getValue)
    }

    test("Inj ..key1[#].*.*"){
      val expression: String = "Book[0 to end].*.*"

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

      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street -> 1000), Map(Price -> 40, Title -> ScalaMachine, Street -> 1000), Map(Price -> 38, Title -> C++Machine, Street -> 1000))", future.join().getValue.toString)
    }

    test("Ex ..key1.*.*"){
      val expression: String = "Book.*.*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        "Java",
        15.5,
        Seq(Map("Title" -> "JavaMachine", "Price" -> 39)),
        "Scala",
        21.5,
        Seq(Map("Title" -> "ScalaMachine", "Price" -> 40)),
        "C++",
        12.6,
        Seq(Map("Title" -> "C++Machine", "Price" -> 38))
      ), future.join().getValue)
    }

    test("Inj ..key1.*.*"){
      val expression: String = "Book.*.*"

      val bosonI: Boson = Boson.injector(expression, (x: Int) => x + 10)
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Java, 15.5, List(Map(Title -> JavaMachine, Price -> 39)), Scala, 21.5, List(Map(Title -> ScalaMachine, Price -> 40)), C++, 12.6, List(Map(Title -> C++Machine, Price -> 38)))", future.join().getValue.toString)
    } // No change is perform because the values are not the same type

    test("Ex .key1.*.*"){
      val expression: String = "Store.*.*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> List(Map("Title" -> "JavaMachine", "Price" -> 39))),
        Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> List(Map("Title" -> "ScalaMachine", "Price" -> 40))),
        Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> List(Map("Title" -> "C++Machine", "Price" -> 38))),
        Map("Color" -> "Red", "Price" -> 48),
        Map("Color" -> "White", "Price" -> 35),
        Map("Color" -> "Blue", "Price" -> 38),
        Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> List(Map("Title" -> "JavaMachine", "Price" -> 39)))
      ), future.join().getValue)
    }

    test("Inj .key1.*.*"){
      val expression: String = "Store.*.*"
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

      assertEquals("Vector(Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java, Street -> 1000), Map(Pri -> 21.5, SpecialEditions -> List(Map(Price -> 40, Title -> ScalaMachine)), Title -> Scala, Street -> 1000), Map(Price -> 12.6, SpecialEditions -> List(Map(Price -> 38, Title -> C++Machine)), Title -> C++, Street -> 1000), Map(Color -> Red, Price -> 48, Street -> 1000), Map(Color -> White, Price -> 35, Street -> 1000), Map(Color -> Blue, Price -> 38, Street -> 1000), Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java, Street -> 1000))" +
        "", future.join().getValue.toString)
    }

    test("Ex .key1.*.*.*.*"){
      val expression: String = "Store.*.*.*.*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)
      assertEquals(Vector(
        Map("Title" -> "JavaMachine", "Price" -> 39),
        Map("Title" -> "ScalaMachine", "Price" -> 40),
        Map("Title" -> "C++Machine", "Price" -> 38),
        Map("Title" -> "JavaMachine", "Price" -> 39)
      ), future.join().getValue)
    }

    test("Inj .key1.*.*.*.*"){
      val expression: String = "Store.*.*.*.*"

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


      assertEquals("Vector(Map(Price -> 39, Title -> JavaMachine, Street -> 1000), Map(Price -> 40, Title -> ScalaMachine, Street -> 1000), Map(Price -> 38, Title -> C++Machine, Street -> 1000), Map(Price -> 39, Title -> JavaMachine, Street -> 1000))", future.join().getValue.toString)
    }

    test("Ex .*"){
      val expression: String = ".*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        Map("Book" -> Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
          "Hatk" -> Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38),
            Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39)))))
      ), future.join().getValue)
    } //$.* -> checked

    test("Inj .*"){
      val expression: String = ".*"
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

      assertEquals("Vector(Map(Book -> List(Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java), Map(Pri -> 21.5, SpecialEditions -> List(Map(Price -> 40, Title -> ScalaMachine)), Title -> Scala), Map(Price -> 12.6, SpecialEditions -> List(Map(Price -> 38, Title -> C++Machine)), Title -> C++)), Hatk -> List(Map(Color -> Red, Price -> 48), Map(Color -> White, Price -> 35), Map(Color -> Blue, Price -> 38), Map(Price -> 15.5, SpecialEditions -> List(Map(Price -> 39, Title -> JavaMachine)), Title -> Java)), Street -> 1000))", future.join().getValue.toString)
    }

    test("Ex ..*"){
      val expression: String = "..*"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(validatedByteArr)

      assertEquals(Vector(
        Map("Book" -> Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
          "Hatk" -> Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38),
            Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39)))))
      ), future.join().getValue)
    }

 /*   test("Inj ..* V1"){
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
    } // No change is perform because the values are not the same type
*/ //TODO change assert
  
    test("Inj ..* V2"){
    val expression: String = "..*"




    val root: BsonObject = new BsonObject().put("field1", "OneWord").put("field2", "TwoWords").put("field3", "ThreeWords").put("field4", "FourWords")



    val bosonI: Boson = Boson.injector(expression, (x: String) => x.concat("!!") )
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(root.encodeToBarray())
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(OneWord!!, TwoWords!!, ThreeWords!!, FourWords!!)", future.join().getValue.toString)
  }

    test("Inj ..* V3"){
    val expression: String = "..*"

      val field2: BsonObject = new BsonObject().put("field4", new BsonObject())
      val field1: BsonObject = new BsonObject().put("field3", new BsonObject())
      val root: BsonObject = new BsonObject().put("field1",field1 ).put("field2", field2 )

    //val bosonI: Boson = Boson.injector(expression, (x: List[Any]) => x.+:("ADDED"))
    val bosonI: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: Map[String, Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newL: Map[String, Any] = l.+(("newField", 10))
      val res: ByteBuf = Mapper.encode(newL)
      if(res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    })
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(root.encodeToBarray())

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    assertEquals("Vector(Map(field3 -> Map(newField -> 10), newField -> 10), Map(field4 -> Map(newField -> 10), newField -> 10))", future.join().getValue.toString)
  }

    test("Ex ..key, but multiple keys with same name"){
      val obj2: BsonObject = new BsonObject().put("Store", 1000L)
      val obj1: BsonObject = new BsonObject().put("Store", obj2)
      val expression: String = "..Store"
      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(obj1.encodeToBarray())
      assertEquals(Vector(
        Map("Store" -> 1000),
        1000
      ), future.join().getValue)
    }

    test("Inj ..key, but multiple keys with same name V1"){
      val obj2: BsonObject = new BsonObject().put("Store", 1000L)
      val obj1: BsonObject = new BsonObject().put("Store", obj2)
      val expression: String = "..Store"

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
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(obj1.encodeToBarray())

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())
      var i: Int = 0

      assertEquals("Vector(Map(Store -> 1000), 1000)", future.join().getValue.toString)
    } // No change is perform because the values are not the same type

    test("Inj ..key, but multiple keys with same name V2"){
      val obj22: BsonObject = new BsonObject().put("Store", new BsonObject())
      val obj11: BsonObject = new BsonObject().put("Store", obj22)
      val expression: String = "..Store"

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
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(obj11.encodeToBarray())

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())
      var i: Int = 0

      assertEquals("Vector(Map(Store -> Map(Street -> 1000), Street -> 1000), Map(Street -> 1000))", future.join().getValue.toString)
    }

    test("Inj ..key1[@Key2]"){
      val obj555: BsonObject = new BsonObject().put("Store", new BsonArray())
      val arr444: BsonArray = new BsonArray().add(obj555).add(obj555)
      val obj333: BsonObject = new BsonObject().put("Store", arr444)
      val arr222: BsonArray = new BsonArray().add(obj333).add(obj333)
      //put("Store",new BsonObject())
      val obj111: BsonObject = new BsonObject().put("Store", arr222)
      val expression: String = "..Store[@Store]"

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
      val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(obj111.encodeToBarray())

      val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
      val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
      boson.go(injFuture.join())

      assertEquals("Vector(Map(Store -> List(Map(Store -> List(), Street -> 1000), Map(Store -> List(), Street -> 1000)), Street -> 1000), Map(Store -> List(Map(Store -> List(), Street -> 1000), Map(Store -> List(), Street -> 1000)), Street -> 1000))", future.join().getValue.toString)
    }

}
