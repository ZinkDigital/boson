package io.boson

import java.util.concurrent.CompletableFuture
import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class jpPlusPlusTests extends FunSuite {

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

  test("Ex .key") {
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

  test("Ex .key1.key2") {
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

  test("Ex .key1.key2[@elem1].key3[@elem2]") {
    val expression = ".Store.Book[@Price].SpecialEditions[@Title]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      Map("Title" -> "JavaMachine", "Price" -> 39),
      Map("Title" -> "C++Machine", "Price" -> 38)
    ), future.join().getValue)
  } //$.Store.Book[?(@.Price)].SpecialEditions[?(@.Title)] -> checked

  test("Ex .key1.key2[#]") {
    val expression = ".Store.Book[1]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      List(Map("Title" -> "Scala", "Pri" -> 21.5, "SpecialEditions" -> List(Map("Title" -> "ScalaMachine", "Price" -> 40))))
    ), future.join().getValue)
  } //$.Store.Book[1] -> checked

  test("Ex .key1.key2[# to end].key3") {
    val expression = ".Store.Book[0 to end].Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      15.5, 12.6
    ), future.join().getValue)
  } //$.Store.Book[:].Price -> checked

  test("Ex .key1.key2[@elem].key3") {
    val expression = ".Store.Book[@Price].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      "Java", "C++"
    ), future.join().getValue)
  } //$.Store.Book[?(@.Price)].Title -> checked

  test("Ex .key1.key2[#].key3[@elem].k*y") {
    val expression = ".Store.Book[0 to end].SpecialEditions[@Price].T*le"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine", "ScalaMachine", "C++Machine"
    ), future.join().getValue)
  } //$.Store.Book[:].SpecialEditions[?(@.Price)].Title -> checked

  //-----------------------------------------------------------------------------------------------------//

  test("Ex ..key V1") {
    val expression = "..Title"  // || "Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      "Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine", "Java", "JavaMachine"
    ), future.join().getValue)
  } //$..Title -> checked

  test("Ex ..key V2") {
    val expression = "..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
    ), future.join().getValue)
  } //$..Price -> checked

  test("Ex ..key1.key2") {
    val expression = "..Book.Title" //  || "Book.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      "Java", "Scala", "C++"
    ), future.join().getValue)
  } //$..Book[:].Title -> checked

  test("Ex ..key1[# to end].key2") {
    val expression = "..Book.Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      15.5, 12.6
    ), future.join().getValue)
  } //$..Book[:].Price -> checked

  test("Ex ..key[@elem].key") {
    val expression = "..SpecialEditions[@Price].Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine", "ScalaMachine", "C++Machine", "JavaMachine"
    ), future.join().getValue)
  } //$..SpecialEditions[?(@.Price)].Title -> checked

  test("Ex ..key[@elem]") {
    val expression = "..SpecialEditions[@Price]"
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

  test("Ex ..key[#] V1") {
    val expression = "..SpecialEditions[0]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)

    assertEquals(Vector(
      List(Map("Title" -> "JavaMachine", "Price" -> 39)), List(Map("Title" -> "ScalaMachine", "Price" -> 40)), List(Map("Title" -> "C++Machine", "Price" -> 38)), List(Map("Title" -> "JavaMachine", "Price" -> 39))
    ), future.join().getValue)
  } //$..SpecialEditions[0] -> checked

  test("Ex ..key[#] V2") {
    val arr5: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp",12))
    val arr4: BsonArray = new BsonArray().add(new BsonObject().put("fridgeTemp", 18))
    val arr3: BsonArray = new BsonArray().add(new BsonObject().put("doorOpen",arr5))
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

  test("Ex ..*y1[@elem1].key2[@elem2]") {
    val expression = "..*k[@Price].SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      Map("Title" -> "JavaMachine", "Price" -> 39),
      Map("Title" -> "C++Machine", "Price" -> 38),
      Map("Title" -> "JavaMachine", "Price" -> 39)
    ), future.join().getValue)
  } //$..Book[?(@.Price)].SpecialEditions[?(@.Price)] && $..Hatk[?(@.Price)].SpecialEditions[?(@.Price)] -> checked

  test("Ex ..*y1[@elem1].key2") {
    val expression = "..*k[@SpecialEditions].Pr*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 21.5, 12.6, 15.5
    ), future.join().getValue)
  } //$..Book[?(@.SpecialEditions)].Price && $..Hatk[?(@.SpecialEditions)].Price -> checked

  //---------------------------------------------------------------------------------------------------//

  test("Ex .key1..key2") {
    val expression = ".Store..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
    ), future.join().getValue)
  } //$.Store..Price -> checked

  test("Ex .key1..key2[@elem]") {
    val expression = ".Store..SpecialEditions[@Price]"
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

  test("Ex .key1..key2[#]") {
    val expression = ".Store..SpecialEditions[0]"
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

  test("Ex .key1..key2[#]..key3") {
    val expression = ".Store..Book[0 until end]..Title"
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

  test("Ex .key1..key2[#]..key3[@elem]") {
    val expression = ".Store..Book[1 until end]..SpecialEditions[@Price]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      Map("Title" -> "ScalaMachine", "Price" -> 40)
    ), future.join().getValue)
  } //$.Store..Book[1:2]..SpecialEditions[?(@.Price)] -> checked

  test("Ex .*y1..*y2[#]..*y3..key4") {
    val expression = ".*ore..*k[1 until end]..*Editions..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector("ScalaMachine"), future.join().getValue)
  } //$.Store..Book[1:2]..SpecialEditions..Title -> checked

  test("Ex .key1..key2[@elem1]..key3[@elem2]") {
    val expression = ".Store..*k[@Price]..SpecialEditions[@Title]"
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

  test("Ex ..key1[#]..key2") {
    val expression = "..Book[0 to end]..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38
    ), future.join().getValue)
  } //$..Book[:]..Price -> checked

  test("Ex ..key1..key2") {
    val expression = "Store..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      15.5, 39, 40, 12.6, 38, 48, 35, 38, 15.5, 39
    ), future.join().getValue)
  } //$..Store..Price -> checked

  test("Ex ..key1[@elem]..key2") {
    val expression = "Book[@Price]..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "Java", "JavaMachine", "C++", "C++Machine"
    ), future.join().getValue)
  } //$..Book[?(@.Price)]..Title -> checked

  test("Ex ..key1..*ey2..*ey3") {
    val expression = "Store..*k..*itions"
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

  test("Ex ..key1.key2..key3") {
    val expression = "..Book.SpecialEditions..Price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      39,40,38
    ), future.join().getValue)
  } //$..Book[:].SpecialEditions..Price -> checked

  //---------------------------------------------------------------------------------------------------//

  test("Ex .key.*") {
    val expression = ".Store.*"
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

  test("Ex ..key.*") {
    val expression = "SpecialEditions.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      Seq(Map("Title" -> "JavaMachine", "Price" -> 39)),
        Seq(Map("Title" -> "ScalaMachine", "Price" -> 40)),
        Seq(Map("Title" -> "C++Machine", "Price" -> 38)),
        Seq(Map("Title" -> "JavaMachine", "Price" -> 39))
    ), future.join().getValue)
  }

  test("Ex ..key[#].*") {
    val expression = "SpecialEditions[0 to end].*"
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

  test("Ex ..key1.*.key2") {
    val expression = "Book.*.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "Java",
      "Scala",
      "C++"
    ), future.join().getValue)
  }

  test("Ex ..key1.*..key2") {
    val expression = "Book.*..Title"
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

  test("Ex ..key1[#].*.key2") {
    val expression = "Book[0 to end].*.Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(), future.join().getValue)
  }

  test("Ex ..key1[#].*..key2") {
    val expression = "Book[0 to end].*..Title"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      "JavaMachine",
      "ScalaMachine",
      "C++Machine"
    ), future.join().getValue)
  }

  test("Ex ..key1[#].*.*") {
    val expression = "Book[0 to end].*.*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(validatedByteArr)
    assertEquals(Vector(
      Seq(Map("Title" -> "JavaMachine", "Price" -> 39)),
      Seq(Map("Title" -> "ScalaMachine", "Price" -> 40)),
      Seq(Map("Title" -> "C++Machine", "Price" -> 38))
    ), future.join().getValue)
  }

  test("Ex ..key1.*.*") {
    val expression = "Book.*.*"
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

}
