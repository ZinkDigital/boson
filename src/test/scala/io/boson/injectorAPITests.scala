package io.boson

import java.util.concurrent.{CompletableFuture, CountDownLatch}

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.{BosonImpl, BosonInjector}
import io.boson.bson.bsonValue._
import mapper.Mapper
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class injectorAPITests extends FunSuite {
  val b11: BsonObject = new BsonObject().put("Title","C++Machine").put("Price", 38)
  val br5: BsonArray = new BsonArray().add(b11)
  val b10: BsonObject = new BsonObject().put("Title","JavaMachine").put("Price", 39)
  val br4: BsonArray = new BsonArray().add(b10)
  val b9: BsonObject = new BsonObject().put("Title","ScalaMachine").put("Price", 40)
  val br3: BsonArray = new BsonArray().add(b9)//.add(new BsonObject().put("Ti","shit"))
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
//  test("extract ") {
//    val expression = "array.[@damnnn].damnnn.[@google]"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
//    boson.go(validatedByteArr111)
//
//    assertEquals(BsSeq(Vector(
//      Seq("Java")
//    )), future.join())
//  }

  test("extract Key.Key") {
    val expression = "Store.Book"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
      Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
      Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))
    ))), future.join())
  }

  test("extract Key.Key.[@elem]") {
    val expression = "Store.Book.[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
      Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))
    ))), future.join())
  }

  test("extract Key.*halfKey") {
    val expression = "Store.*at"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Color" -> "Red", "Price" -> 48),
      Map("Color" -> "White", "Price" -> 35),
      Map("Color" -> "Blue", "Price" -> 38)
    ))), future.join())
  }

  test("extract Key.halfKey*") {
    val expression = "Store.H*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Color" -> "Red", "Price" -> 48),
      Map("Color" -> "White", "Price" -> 35),
      Map("Color" -> "Blue", "Price" -> 38)
    ))), future.join())
  }

  test("extract Key.halfKey*halfKey") {
    val expression = "Store.H*t"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Color" -> "Red", "Price" -> 48),
      Map("Color" -> "White", "Price" -> 35),
      Map("Color" -> "Blue", "Price" -> 38)
    ))), future.join())
  }

  test("extract Key.*") {
    val expression = "Store.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
        Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
        Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
      Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38))
    )), future.join())
  }

  test("extract *") {
    val expression = "*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Map("Store" -> Map(
        "Book" -> Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
          Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> Seq(Map("Title" -> "ScalaMachine", "Price" -> 40))),
          Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
        "Hat" -> Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38))))
    )), future.join())
  }

  test("extract Key.[@*elem]") {
    val expression = "Book.[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
      Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))
    ))), future.join())
  }

  test("extract *.[@elem]") {
    val expression = "*.[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
        Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
      Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38))
    )), future.join())
  }

  test("extract *.[@*elem]") {
    val expression = "*.[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq(Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
        Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))),
      Seq(Map("Color" -> "Red", "Price" -> 48), Map("Color" -> "White", "Price" -> 35), Map("Color" -> "Blue", "Price" -> 38))
    )), future.join())
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

  test("extract Key.Key.Key..first") {
    val expression = "Store.Book.Title..first"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      "Java"
    )), future.join())
  }

  test("extract Key.Key..last") {
    val expression = "Store.Book..last"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))
    )), future.join())
  }

  test("extract Key.*halfKey.[@*elem]") {
    val expression = "Store.*ok.[@*ce]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(Seq(
      Map("Title" -> "Java", "Price" -> 15.5, "SpecialEditions" -> Seq(Map("Title" -> "JavaMachine", "Price" -> 39))),
      Map("Title" -> "C++", "Price" -> 12.6, "SpecialEditions" -> Seq(Map("Title" -> "C++Machine", "Price" -> 38)))
    ))), future.join())
  }

  test("extract Key.*halfKey.[@*elem].Key") {
    val expression = "Store.*ok.[@*ce].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Java", "C++")
    )), future.join())
  }

  test("extract Key.*halfKey.[@*elem].K*y") {
    val expression = "Store.*ok.[@*ce].Ti*le"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Java","C++")
    )), future.join())
  }

  test("extract Key.Key.[#].Key") {
    val expression = "Store.Book.[1].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Scala")
    )), future.join())
  }

  test("extract Key.Key.[#to#].Key") {
    val expression = "Store.Book.[1 to 2].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Scala","C++")
    )), future.join())
  }

  test("extract Key.Key.[#until#].Key") {
    val expression = "Store.Book.[0 until 2].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Java","Scala")
    )), future.join())
  }

  test("extract Key.Key.[#until end].Key") {
    val expression = "Store.Book.[1 until end].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Scala")
    )), future.join())
  }

  test("extract Key.Key.[#to end].Key") {
    val expression = "Store.Book.[1 to end].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(BsSeq(Vector(
      Seq("Scala","C++")
    )), future.join())
  }

//  test("extract Key.Key.[#].Key.[@elem]") {
//    val expression = "Store.Book.[1].SpecialEditions.[@Title]"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression,(out: BsValue) => future.complete(out))
//    boson.go(validatedByteArr)
//
//    assertEquals(BsSeq(Vector(
//      Map("Title" -> "ScalaMachine", "Price" -> 40)
//    )), future.join())
//  } //  still implementing it
  test("MoreKeys 2"){
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
    val expression = "array.[0].damnnn.[1].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    println(future.join().getValue.asInstanceOf[Vector[String]])
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector("DAMMN", "sdfsf", "DAMMN", "DAMMN", "DAMMN", "DAMMN"),a  )

  }

  test("MoreKeys 1"){
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
    val expression = "arr*ay.[0 until 1].damn*n.[0 until 1].google"
    val expression1 = "google"
    val boson: Boson = Boson.injector(expression, (in: String) => "sdfsf")
    val midResult: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)
    val result: Array[Byte] = midResult.join()
    //result.foreach(b => println(s"byte=${b.toByte}    char=${b.toChar}"))
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1: Boson = Boson.extractor(expression1, (in: BsValue) => future.complete(in))
    boson1.go(result)
    println(future.join().getValue.asInstanceOf[Vector[String]])
    val a: Vector[String] = future.join().getValue.asInstanceOf[Vector[String]]
    assertEquals(Vector("sdfsf", "DAMMN", "DAMMN", "DAMMN", "DAMMN", "DAMMN"),a  )

  }

  test("MoreKeys"){
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
    val expression = "array.[@damnnn].damnnn.[@google].google"
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
}
