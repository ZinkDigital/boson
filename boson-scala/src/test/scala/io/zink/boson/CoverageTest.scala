package io.zink.boson

import java.time.Instant

import bsonLib.{BsonArray, BsonObject}
import com.jayway.jsonpath.{Configuration, JsonPath}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.junit.Assert.{assertArrayEquals, assertEquals, assertTrue}

import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[JUnitRunner])
class CoverageTest extends FunSuite {

  case class BookExt(price: Double, title: String, edition: Int, forSale: Boolean, nPages: Long)

  case class Book1Ext(title: String, price: Double)

  case class SpecialEditions(title: String, price: Int, availability: Boolean)

  case class _BookExt(title: String, price: Double, specialEditions: Seq[SpecialEditions])

  case class _Book1Ext(title: String, price: Double, specialEditions: SpecialEditions)

  private val _book1 = new BsonObject().put("Title", "Scala").put("Price", 25.6).put("Edition", 10).put("ForSale", true).put("nPages", 750L)
  private val _store = new BsonObject().put("Book", _book1)
  private val _bson = new BsonObject().put("Store", _store)

  case class Author(firstName: String, lastName: String, age: Int)

  case class NestedBook(name: String, pages: Int, author: Author)

  case class Book(name: String, pages: Int)

  case class Book1(pages: Double, someLong: Long, someBoolean: Boolean)

  val bsonHuman: BsonArray = new BsonArray().add("person1").add("person2").add("person3")
  val bsonObjArray: BsonObject = new BsonObject().put("person", bsonHuman)
  val bsonObjArrayEncoded: Array[Byte] = bsonObjArray.encodeToBarray

  val bsonAlien: BsonArray = new BsonArray().add("et").add("predator").add("alien")
  val bsonObjArray1: BsonObject = new BsonObject().put("alien", bsonAlien)

  val bsonEvent: BsonObject = new BsonObject().put("person", bsonHuman).put("alien", bsonAlien)
  val bsonSpeciesObj: BsonObject = new BsonObject().put("species", bsonEvent)
  val bsonSpeciesEncoded: Array[Byte] = bsonSpeciesObj.encodeToBarray

  val book: BsonObject = new BsonObject().put("name", "Title1").put("pages", 1)
  val bsonBook: BsonObject = new BsonObject().put("book", book)

  val book2: BsonObject = new BsonObject().put("name", "Some book").put("pages", 123)
  val bsonBook2: BsonObject = new BsonObject().put("book", book2)

  val booksArr: BsonArray = new BsonArray().add(bsonBook).add(bsonBook2)
  val storeObj: BsonObject = new BsonObject().put("books", booksArr)
  val storeBson: BsonObject = new BsonObject().put("store", storeObj)

  val bookAux: BsonObject = new BsonObject().put("pages", 1.0).put("someLong", 1L).put("someBoolean", true)
  val bookAux2: BsonObject = new BsonObject().put("pages", 23.10).put("someLong", 100000L).put("someBoolean", false)
  val bookAux3: BsonObject = new BsonObject().put("pages", -3.0).put("someLong", 789456L).put("someBoolean", true)

  val bsonBookAux: BsonObject = new BsonObject().put("book", bookAux)
  val bsonBookAux2: BsonObject = new BsonObject().put("book", bookAux2)
  val bsonBookAux3: BsonObject = new BsonObject().put("book", bookAux3)

  val booksAux: BsonArray = new BsonArray().add(bsonBookAux).add(bsonBookAux2).add(bsonBookAux3)
  val storeAux: BsonObject = new BsonObject().put("books", booksAux)
  val storeBsonAux: BsonObject = new BsonObject().put("store", storeAux)

  val expected: BsonObject = new BsonObject().put("name", "Title1").put("pages", 101)
  val bsonBookExpected: BsonObject = new BsonObject().put("book", expected)

  val expected2: BsonObject = new BsonObject().put("name", "Some book").put("pages", 223)
  val bsonBook2Expected: BsonObject = new BsonObject().put("book", expected2)

  val booksExpected: BsonArray = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected)
  val storeExpected: BsonObject = new BsonObject().put("books", booksExpected)
  val storeBsonExpected: BsonObject = new BsonObject().put("store", storeExpected)

  val nestedAuthor: BsonObject = new BsonObject().put("firstName", "John").put("lastName", "Doe").put("age", 21)
  val nestedBook: BsonObject = new BsonObject().put("name", "Some Book").put("pages", 100).put("author", nestedAuthor)
  val nestedBson: BsonObject = new BsonObject().put("book", nestedBook)

  val nestedAuthor2: BsonObject = new BsonObject().put("firstName", "Jane").put("lastName", "Doe").put("age", 12)
  val nestedBook2: BsonObject = new BsonObject().put("name", "A Title").put("pages", 999).put("author", nestedAuthor2)
  val nestedBson2: BsonObject = new BsonObject().put("book", nestedBook2)

  val nestedAuthorExpected: BsonObject = new BsonObject().put("firstName", "JOHN").put("lastName", "DOE").put("age", 41)
  val nestedBookExpected: BsonObject = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthorExpected)
  val nestedBsonExpected: BsonObject = new BsonObject().put("book", nestedBookExpected)

  val nestedAuthor2Expected: BsonObject = new BsonObject().put("firstName", "JANE").put("lastName", "DOE").put("age", 32)
  val nestedBook2Expected: BsonObject = new BsonObject().put("name", "A TITLE").put("pages", 1099).put("author", nestedAuthor2Expected)
  val nestedBson2Expected: BsonObject = new BsonObject().put("book", nestedBook2Expected)


  //**************            Extractor Tests            *********************

  test("Extract Type class Book") {
    val expression: String = ".Store.Book"
    val boson = Boson.extractor(expression, (in: BookExt) => {
      assertEquals(BookExt(25.6, "Scala", 10, true, 750L), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Type class Book as byte[]") {
    val expression: String = ".Store.Book"
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(_book1.encodeToBarray(), in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Seq[Type class Book]") {

    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5)
    val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books)
    val bson = new BsonObject().put("Store", store)

    val expression: String = ".Store.Book[0 to 1]"
    val mutableBuffer: ArrayBuffer[Book1Ext] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Book1Ext) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(Book1Ext("Java", 15.5), Book1Ext("Scala", 21.5)), mutableBuffer)

  }

  test("Extract Seq[Type class Book] as Seq[byte[]]") {

    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5)
    val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books)
    val bson = new BsonObject().put("Store", store)

    val expression: String = ".Store.Book[0 to 1]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] = Seq(title1.encodeToBarray(), title2.encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected.size == mutableBuffer.size && expected.zip(mutableBuffer).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract Embedded a Case Class") {
    val e = new BsonObject().put("Title", "ScalaMachine").put("Price", 40).put("Availability", true)
    val c = new BsonObject().put("Title", "Scala").put("Price", 30.5).put("SpecialEditions", e)
    val b = new BsonArray().add(c)
    val a = new BsonObject().put("Book", b)
    val bsonEvent = new BsonObject().put("Store", a)

    val expression: String = ".Store.Book[0]"
    val boson = Boson.extractor(expression, (in: _Book1Ext) => {
      assertEquals(_Book1Ext("Scala", 30.5, SpecialEditions("ScalaMachine", 40, true)), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Embedded List of Case Classes") {
    val e = new BsonObject().put("Title", "ScalaMachine").put("Price", 40).put("Availability", true)
    val d = new BsonArray().add(e)
    val c = new BsonObject().put("Title", "Scala").put("Price", 30.5).put("SpecialEditions", d)
    val b = new BsonArray().add(c)
    val a = new BsonObject().put("Book", b)
    val bsonEvent = new BsonObject().put("Store", a)

    val expression: String = ".Store.Book[0]"
    val boson = Boson.extractor(expression, (in: _BookExt) => {
      assertEquals(_BookExt("Scala", 30.5, Seq(SpecialEditions("ScalaMachine", 40, true))), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bsonEvent.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Long") {
    val expression: String = ".Store.Book.nPages"
    val boson: Boson = Boson.extractor(expression, (in: Long) => {
      assertEquals(750L, in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Boolean") {
    val expression: String = ".Store.Book.ForSale"
    val boson: Boson = Boson.extractor(expression, (in: Boolean) => {
      assertEquals(true, in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Int") {
    val expression: String = ".Store.Book.Edition"
    val boson: Boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(10, in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Double") {
    val expression: String = ".Store.Book.Price"
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      assert(25.6 === in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract String") {
    val expression: String = ".Store.Book.Title"
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      assertEquals("Scala", in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract byte[]") {

    val obj = new BsonObject().put("byte", "Scala".getBytes)
    val expression: String = ".byte"
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals("Scala".getBytes, in)
      println("APPLIED")
    })
    val res = boson.go(obj.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Instant") {
    val now = Instant.now
    val obj = new BsonObject().put("time", now)
    val expression: String = ".time"
    val boson: Boson = Boson.extractor(expression, (in: Instant) => {
      assertEquals(now, in)
      println("APPLIED")
    })
    val res = boson.go(obj.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Float") {

    val obj = new BsonObject().put("floating", 2.2f)
    val expression: String = ".floating"
    val boson: Boson = Boson.extractor(expression, (in: Float) => {
      assertTrue(2.2f === in)
      println("APPLIED")
    })
    val res = boson.go(obj.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Iterate simple Seq[Boolean]") {
    val arr = new BsonArray().add(true).add(true).add(false)

    val expression: String = ".[all]"
    val mutableBuffer: ArrayBuffer[Boolean] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Boolean) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(mutableBuffer.containsSlice(Seq(true, true, false)))
  }

  test("Iterate simple Seq[Int]") {
    val arr = new BsonArray().add(1).add(2).add(3)
    val expression: String = ".[all]"
    val mutableBuffer: ArrayBuffer[Int] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Int) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(mutableBuffer.containsSlice(Seq(1, 2, 3)))
  }

  test("Iterate simple Seq[String]") {
    val arr = new BsonArray().add("one").add("two").add("three")
    val expression: String = ".[all]"
    val mutableBuffer: ArrayBuffer[String] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(mutableBuffer.containsSlice(Seq("one", "two", "three")))
  }

  test("Iterate simple Seq[Long]") {
    val arr = new BsonArray().add(1000L).add(1001L).add(1002L)
    val expression: String = ".[all]"
    val mutableBuffer: ArrayBuffer[Long] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Long) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(mutableBuffer.containsSlice(Seq(1000L, 1001L, 1002L)))
  }

  test("Iterate simple Seq[Double]") {
    val arr = new BsonArray().add(1.1).add(2.2).add(3.3)
    val expression: String = ".[all]"
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(mutableBuffer.containsSlice(Seq(1.1, 2.2, 3.3)))

  }

  private val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
  private val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
  private val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
  private val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
  private val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  private val sEditions3 = new BsonArray().add(edition3)
  private val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
  private val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  private val sEditions2 = new BsonArray().add(edition2)
  private val title2 = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", sEditions2)
  private val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  private val sEditions1 = new BsonArray().add(edition1)
  private val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
  private val books = new BsonArray().add(title1).add(title2).add(title3)
  private val store = new BsonObject().put("Book", books).put("Hatk", hats)
  private val bson = new BsonObject().put("Store", store)

  test("Iterate through a key.* V1") {
    val expression: String = ".Store.Book[0].SpecialEditions[0].*"
    val mutableBuffer: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Any) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq("JavaMachine", 39), mutableBuffer)
  }

  test("Iterate through a key.* V2") {
    val expression: String = ".Store.Book.*"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(Seq(title1.encodeToBarray, title2.encodeToBarray, title3.encodeToBarray).zip(mutableBuffer).forall(e => e._1.sameElements(e._2)))
  }

  test("Iterate through a complex Seq[String]") {
    val expression: String = ".Store.Book[1 to end].Title"
    val mutableBuffer: ArrayBuffer[String] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq("Scala", "C++"), mutableBuffer)
  }

  test("Iterate through a complex Seq[Int]") {
    val expression: String = ".Store.Book[0 until end].SpecialEditions[all].Price"
    val mutableBuffer: ArrayBuffer[Int] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Int) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(39, 40), mutableBuffer)
  }

  test("Iterate through a complex Seq[Double]") {
    val expression: String = ".Store.Book[1 to 3].Price"
    val mutableBuffer: ArrayBuffer[Double] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      mutableBuffer += in
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(12.6), mutableBuffer)
  }

  test("Extract Root") {
    val expression: String = "."
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(_bson.encodeToBarray(), in)
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Iterate through ..Key, Seq[String]") {
    val expression: String = "..Title"
    val mutableBuffer: ArrayBuffer[String] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq("Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine"), mutableBuffer)
  }

  test("Iterate through ..Key, Seq[Any]") {
    val expression: String = "..Price"
    val mutableBuffer: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Any) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(15.5, 39, 40, 12.6, 38, 48, 35, 38), mutableBuffer)
  }

  //TODO:
  //    val doc: Any = Configuration.defaultConfiguration().jsonProvider().parse(bson.asJson().toString)
  //    val list: java.util.List[String] = JsonPath.read(doc, "$..Title")
  //    println(list)

  test("Iterate through ..*y[#]..[#], Seq[Array[Byte]]") {
    val expression: String = "..*k[all]..[0]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] = Seq(edition1.encodeToBarray, edition2.encodeToBarray, edition3.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall(b => b._1.sameElements(b._2)))
  }

  test("Iterate through ..*y[#]..Key2, Seq[Array[Byte]]") {
    val expression: String = "..*k[all]..Price"
    val mutableBuffer: ArrayBuffer[Any] = ArrayBuffer()
    val expected: Seq[Any] = Seq(15.5, 39, 40, 12.6, 38, 48, 35, 38)
    val boson: Boson = Boson.extractor(expression, (in: Any) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected === mutableBuffer)
  }

  test("Iterate through ..*y[#], Seq[Array[Byte]]") {
    val expression: String = "..*k[all]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] = Seq(title1.encodeToBarray(), title2.encodeToBarray(), title3.encodeToBarray(), hat1.encodeToBarray, hat2.encodeToBarray, hat3.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall(b => b._1.sameElements(b._2)))
  }

  test("Iterate through ..Key[#], Seq[Array[Byte]] V1") {
    val expression: String = "..Book[all]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] = Seq(title1.encodeToBarray(), title2.encodeToBarray(), title3.encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall(b => b._1.sameElements(b._2)))
  }

  test("Iterate through ..[#], Seq[Array[Byte]] V1") {
    val expression: String = "..[all]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] =
      Seq(title1.encodeToBarray, edition1.encodeToBarray, title2.encodeToBarray, edition2.encodeToBarray, title3.encodeToBarray, edition3.encodeToBarray, hat1.encodeToBarray, hat2.encodeToBarray, hat3.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall(b => b._1.sameElements(b._2)))
  }

  test("Iterate through .key..[#], Seq[Array[Byte]]") {
    val expression: String = ".Store..[0]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] =
      Seq(title1.encodeToBarray, edition1.encodeToBarray, edition2.encodeToBarray, edition3.encodeToBarray, hat1.encodeToBarray)
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall(b => b._1.sameElements(b._2)))
  }

  test("Iterate through ..Key1..Key2, Seq[Any] V1") { //TODO:implement search inside match
    val expression: String = "..Book..Price"
    val mutableBuffer: ArrayBuffer[Any] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Any) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(15.5, 39, 40, 12.6, 38), mutableBuffer)
  }

  test("Iterate through .Key1.Key2..Key3, Seq[String]") {
    val expression: String = ".Store.Book..Title"
    val mutableBuffer: ArrayBuffer[String] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq("Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine"), mutableBuffer)
  }

  test("Iterate through .Key1.Key2..Key3, Seq[Int]") { //TODO:implement the applyFunc
    val expression: String = ".Store.SpecialEditions..Price"
    val mutableBuffer: ArrayBuffer[Int] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Int) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(), mutableBuffer)
  }

  test("Extract .Key1..Key2..Key3, Seq[Int]") {
    val expression: String = ".Store..SpecialEditions..Price"
    val mutableBuffer: ArrayBuffer[Int] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: Int) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertEquals(Seq(39, 40, 38), mutableBuffer)
  }

  private val xHat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
  private val xHat2 = new BsonObject().put("Price", 35).put("Color", "White")
  private val xHat1 = new BsonObject().put("Price", 48).put("Color", "Red")
  private val xHats = new BsonArray().add(xHat1).add(xHat2).add(xHat3)
  private val xEdition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  private val xsEditions3 = new BsonArray().add(xEdition3)
  private val xTitle3 = new BsonObject().put("Year", 2007).put("Title", "C++").put("Price", 12.6).put("SpecialEditions", xsEditions3)
  private val xEdition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  private val xsEditions2 = new BsonArray().add(xEdition2)
  private val xTitle2 = new BsonObject().put("Year", 2007).put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", xsEditions2)
  private val xEdition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  private val xsEditions1 = new BsonArray().add(xEdition1)
  private val xTitle1 = new BsonObject().put("Year", 2007).put("Title", "Java").put("Price", 15.5).put("SpecialEditionsk", xsEditions1)
  private val xBooks = new BsonArray().add(xTitle1).add(xTitle2).add(xTitle3)
  private val xItemsHead = new BsonArray().add(xHats).add(hats)
  private val xItems = new BsonArray().add(xBooks).add(books)
  private val xStore = new BsonObject().put("Book", xItems).put("Hatk", xItemsHead)
  private val xBson = new BsonObject().put("Store", xStore)

  test("Extract ..[#], Seq[Array[Byte]] V2") {
    val expression: String = "..[all]"

    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] =
      Seq(xBooks.encodeToBarray, xTitle1.encodeToBarray, xEdition1.encodeToBarray, xTitle2.encodeToBarray, xEdition2.encodeToBarray, xTitle3.encodeToBarray,
        xEdition3.encodeToBarray, books.encodeToBarray, title1.encodeToBarray, edition1.encodeToBarray, title2.encodeToBarray, edition2.encodeToBarray, title3.encodeToBarray,
        edition3.encodeToBarray, xHats.encodeToBarray, xHat1.encodeToBarray, xHat2.encodeToBarray, xHat3.encodeToBarray,
        hats.encodeToBarray, hat1.encodeToBarray, hat2.encodeToBarray, hat3.encodeToBarray)

    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })

    val res = boson.go(xBson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall { b => b._1.sameElements(b._2) })
  }

  test("Extract ..*y[#], Seq[Array[Byte]] V2") {
    val expression: String = "..*k[all]"
    val mutableBuffer: ArrayBuffer[Array[Byte]] = ArrayBuffer()
    val expected: Seq[Array[Byte]] =
      Seq(xBooks.encodeToBarray, xEdition1.encodeToBarray, books.encodeToBarray, xHats.encodeToBarray, hats.encodeToBarray)

    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })

    val res = boson.go(xBson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assertTrue(expected.size === mutableBuffer.size && expected.zip(mutableBuffer).forall { b => b._1.sameElements(b._2) })
  }

  test("Extract ..key1..key2, Seq[String] V2") {
    val expression: String = "..*k..Title"

    val expected: Seq[String] =
      Seq("Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine", "Java", "JavaMachine", "Scala", "ScalaMachine", "C++", "C++Machine", "JavaMachine")

    val mutableBuffer: ArrayBuffer[String] = ArrayBuffer()
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      mutableBuffer += in
      println(s"in: $in")
      println("APPLIED")
    })

    val res = boson.go(xBson.encode.getBytes)
    Await.result(res, Duration.Inf)
    assert(expected === mutableBuffer)
  }

  //**************            Injectors Tests            *********************

  test("Root modification") {
    val bson = new BsonObject().put("name", "john doe")
    val ex = "."
    val bsonInj = Boson.injector(ex, (in: Array[Byte]) => {
      new String(in).toUpperCase.getBytes
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("Root Injection") {
    val bson = new BsonObject().put("name", "john doe")
    val ex = "."
    val bsonInj = Boson.injector(ex, (in: Array[Byte]) => {
      new String(in).replace("john doe", "Jane Doe").getBytes
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((new String(resultValue) contains "Jane Doe") && resultValue.length == bsonEncoded.length)
  }

  test("Top level key modification") {
    val bson = new BsonObject().putNull("nullKey").put("name", "john doe")
    val ex = ".name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("Nested key modification - Single Dots") {
    val person = new BsonObject().put("name", "john doe")
    val bson = new BsonObject().put("person", person)
    val ex = ".person.name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("Nested key modification - Single Dots - Multiple layers") {
    val obj = new BsonObject().put("name", "john doe")
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val bson = new BsonObject().put("Wrapper", anotherObject)
    val ex = ".Wrapper.AnotherObject.SomeObject.client.person.name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("Nested key modification, two fields - Single Dots - Multiple layers") {
    val person = new BsonObject().put("name", "john doe").put("age", 21)
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)
    val ex = ".client.person.age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Top level halfkey modification - Single Dots") {
    val bson = new BsonObject().put("name", "John Doe")
    val ex1 = ".*ame"
    val ex2 = ".nam*"
    val ex3 = ".n*me"

    val bsonInj1 = Boson.injector(ex1, (in: String) => {
      in.toUpperCase
    })
    val bsonInj2 = Boson.injector(ex2, (in: String) => {
      in.toLowerCase
    })
    val bsonInj3 = Boson.injector(ex3, (in: String) => {
      in + " Hello"
    })

    val bsonEncoded = bson.encodeToBarray()

    val future1 = bsonInj1.go(bsonEncoded)
    val resultValue1: Array[Byte] = Await.result(future1, Duration.Inf)

    val future2 = bsonInj2.go(bsonEncoded)
    val resultValue2: Array[Byte] = Await.result(future2, Duration.Inf)

    val future3 = bsonInj3.go(bsonEncoded)
    val resultValue3: Array[Byte] = Await.result(future3, Duration.Inf)

    assert(
      ((new String(resultValue1) contains "JOHN DOE") && resultValue1.length == bsonEncoded.length) &&
        ((new String(resultValue2) contains "john doe") && resultValue2.length == bsonEncoded.length) &&
        (new String(resultValue3) contains "John Doe Hello")
    )
  }

  test("HasElem injection test") {
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@name]"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase()
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    val resultString = new String(resultValue)
    assert(resultString.contains("JOHN DOE") && resultString.contains("JANE DOE") && resultValue.length == bsonEncoded.length)
  }

  test("HasElem injection test, one object does not contain the elem") {
    val person1 = new BsonObject().put("name", "John Doe")
    val person2 = new BsonObject().put("surname", "Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@name]"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase()
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    val resultString = new String(resultValue)
    assert(resultString.contains("JOHN DOE") && resultString.contains("Doe") && resultValue.length == bsonEncoded.length)
  }

  test("HasElem with multiple keys in an object") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@age]"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(Array(41, 0, 0, 0)) && resultValue.containsSlice(Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Double dot HasElem with multiple keys in an object") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val bson = new BsonObject().put("client", client)
    val ex = "..persons[@age]"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(Array(41, 0, 0, 0)) && resultValue.containsSlice(Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Double dot HasElem with multiple keys in an object Multiple Layers") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val bson = new BsonObject().put("client", client)
    val ex = "..client..persons[@age]"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(Array(41, 0, 0, 0)) && resultValue.containsSlice(Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Root injection - Double dots") {
    val bson = new BsonObject().put("name", "John Doe")
    val ex = "..name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - Double dots") {
    val person = new BsonObject().put("name", "john doe")
    val bson = new BsonObject().put("person", person)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    println(bsonEncoded.mkString(" ") + "\n" + resultValue.mkString(" "))
    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("Key with Array Exp .Key[0 to 1] modification toUpperCase - Single Dots") {
    val expr = ".person[0 to 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[0 until 1] modification - Single Dots") {
    val expr = ".person[0 until 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[1 to end] toUpperCase - Single Dots") {
    val expr = ".person[1 to end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[1 until end] toUpperCase - Single Dots") {
    val expr = ".person[1 until end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[end] toUpperCase - Single Dots") {
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val expr = ".person[end]"
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[all] toUpperCase - Single Dots") {
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val expr = ".person[all]"
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[first] toUpperCase - Single Dots") {
    val expr = ".person[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .Key[1] toUpperCase - Single Dots") {
    val expr = ".person[1]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonObjArrayEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[0 to 1] modification toUpperCase - Single Dots") {
    val expr = ".[0 to 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[0 to end] modification toUpperCase - Single Dots") {
    val expr = ".[0 to end]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[0 until 1] modification toUpperCase - Single Dots") {
    val expr = ".[0 until 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[0 until end] modification toUpperCase - Single Dots") {
    val expr = ".[0 until end]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[2] modification toUpperCase - Single Dots") {
    val expr = ".[2]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[all] modification toUpperCase - Single Dots") {
    val expr = ".[all]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[first] modification toUpperCase - Single Dots") {
    val expr = ".[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp .[end] modification toUpperCase - Single Dots") {
    val expr = ".[end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Expr .species.person[0] toUpperCase - No/Double Dots") {
    val expr = ".species.person[0]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val bsonExpected = new BsonObject().put("person", bsonArrayExpected).put("alien", bsonAlien)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Expr .species.person[0 to 1] toUpperCase - No/Double Dots") {
    val expr = ".species.person[0 to 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val bsonExpected = new BsonObject().put("person", bsonArrayExpected).put("alien", bsonAlien)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Expr .species.person[0 until 2] toUpperCase - No/Double Dots") {
    val expr = ".species.person[0 until 2]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val bsonExpected = new BsonObject().put("person", bsonArrayExpected).put("alien", bsonAlien)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Expr .species.alien[all] toUpperCase - No/Double Dots") {
    val expr = ".species.alien[all]"
    val bsonArrayExpected = new BsonArray().add("ET").add("PREDATOR").add("ALIEN")
    val bsonExpected = new BsonObject().put("person", bsonHuman).put("alien", bsonArrayExpected)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Expr .species.alien[end] toUpperCase - No/Double Dots") {
    val expr = ".species.alien[end]"
    val bsonArrayExpected = new BsonArray().add("et").add("predator").add("ALIEN")
    val bsonExpected = new BsonObject().put("person", bsonHuman).put("alien", bsonArrayExpected)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Expr .species.*[0] toUpperCase - Single Dot/Multiple Layers/Multiple Arrays") {
    val expr = ".species.*[0]"
    val bsonArrayExpectedHuman = new BsonArray().add("PERSON1").add("person2").add("person3")
    val bsonArrayExpectedAlien = new BsonArray().add("ET").add("predator").add("alien")
    val bsonExpected = new BsonObject().put("person", bsonArrayExpectedHuman).put("alien", bsonArrayExpectedAlien)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Nested key injection .client.person[0].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[0].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[0 to 1].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[0 to 1].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[0 until 2].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[0 until 2].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[all].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[all].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(30, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[end].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[end].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(21, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && (resultValue containsSlice Array(30, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[first].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[first].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[1 to end].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[1 to end].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(21, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(30, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection .client.person[0 until end].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = ".client.person[0 until end].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection ..[0].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    //    val client = new BsonObject().put("person", persons)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..[0].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = persons.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection ..[0 to 2].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    //    val client = new BsonObject().put("person", persons)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..[0 to 2].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = persons.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(30, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection ..[first].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    //    val client = new BsonObject().put("person", persons)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..[first].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = persons.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection ..[0 until 2].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    //    val client = new BsonObject().put("person", persons)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..[0 until 2].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = persons.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection ..[0 to end].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    //    val client = new BsonObject().put("person", persons)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..[0 to end].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = persons.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(30, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - Multiple Layers- Double dots - Second argument") {
    val person = new BsonObject().put("name", "john doe").put("age", 21)
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)

    val ex = "..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - Multiple Layers- Single then Double dots") {
    val person = new BsonObject().put("name", "john doe").put("age", 21)
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)

    val ex = ".client..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - Multiple Layers- double then Double dots") {
    val person = new BsonObject().put("name", "john doe").put("age", 21)
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)

    val ex = "..person..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[first]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[first]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[end]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(21, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[1 to end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[1 to end]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(21, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[all]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[all]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[0 to 1]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[0 to 1]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[0 until 2]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[0 until 2]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Double dot HasElem with HalfWord") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val bson = new BsonObject().put("client", client)
    val ex = "..per*[@age]"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(Array(41, 0, 0, 0)) && resultValue.containsSlice(Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Double dot HasElem with HalfWord on key and elem") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val bson = new BsonObject().put("client", client)
    val ex = "..per*[@ag*]"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(Array(41, 0, 0, 0)) && resultValue.containsSlice(Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Double dot HasElem with HalfWord on key and elem- multiple keys") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val bson = new BsonObject().put("client", client)
    val ex = "..clie*..per*[@ag*]"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(Array(41, 0, 0, 0)) && resultValue.containsSlice(Array(32, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Java Instant injection") {
    val ins = Instant.now()
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(1000))
    val client = new BsonObject().put("person", person1)
    val bson = new BsonObject().put("client", client)

    val expectedPerson = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(2000))
    val expectedClient = new BsonObject().put("person", expectedPerson)
    val expectedBson = new BsonObject().put("client", expectedClient)

    val ex = "..instant"
    val bsonInj = Boson.injector(ex, (in: Instant) => {
      in.plusMillis(1000)
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, expectedBson.encodeToBarray())
  }

  test("Java Instant injection - HasElem") {
    val ins = Instant.now()
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(1000))
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val expectedPerson = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(2000))
    val expectedPersons = new BsonArray().add(expectedPerson).add(person2)
    val expectedClient = new BsonObject().put("person", expectedPersons)
    val expectedBson = new BsonObject().put("client", expectedClient)

    val ex = "..person[@instant]"
    val bsonInj = Boson.injector(ex, (in: Instant) => {
      in.plusMillis(1000)
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, expectedBson.encodeToBarray())
  }

  test("Key case class injection") {
    val book = new BsonObject().put("name", "Title1").put("pages", 1)
    val bson = new BsonObject().put("book", book)

    val expected = new BsonObject().put("name", "LOTR").put("pages", 320).encodeToBarray

    val ex = ".book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book("LOTR", 320)
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice expected)
  }

  test("Multiple key case class injection") {

    val book: BsonObject = new BsonObject().put("name", "LOTR").put("pages", 320)
    val bsonBook: BsonObject = new BsonObject().put("book", book)

    val books: BsonArray = new BsonArray().add(bsonBook).add(bsonBook2)
    val store: BsonObject = new BsonObject().put("books", books)
    val storeBsonExpected: BsonObject = new BsonObject().put("store", store)

    val ex = ".store.books[0].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book("LOTR", 320)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - [all]") {
    val ex = ".store.books[all].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - [0 to end]") {
    val ex = ".store.books[0 to end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - [0 until end]") {
    val books = new BsonArray().add(bsonBookExpected).add(bsonBook2)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[0 until end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - [end]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - [1]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[1].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - ..books[1]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)

    val ex = "..books[1].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - .store.books[@book]") {
    val ex = ".store.books[@book]"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case case injection - ..books[@book]") {
    val ex = "..books[@book]"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    println(bsonEncoded.mkString(" "))
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Double dot key case case injection") {
    val ex = "..book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("Case class injection - arr expression ..[0]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray()
    val ex = "..[0].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[1]") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToBarray()

    val ex = "..[1].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[end]") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToBarray()

    val ex = "..[end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    println(resultValue.mkString(" "))
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[first]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray()
    val ex = "..[first].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[0 to end]") {
    val ex = "..[0 to end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected.encodeToBarray())
  }

  test("Case class injection - arr expression ..[0 until end]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray()

    val ex = "..[0 until end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[0 to 1]") {
    val ex = "..[0 to 1].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected.encodeToBarray())
  }

  test("Case class injection - arr expression ..[0 until 1]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray()

    val ex = "..[0 until 1].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[end] - single array") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToBarray()

    val ex = "..[end].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    println(resultValue.mkString(" "))
    assertArrayEquals(resultValue, booksExpected)
  }

  test("Case class injection - arr expression ..[all]") {
    val ex = "..[all].book"
    val bsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    println(resultValue.mkString(" "))
    assertArrayEquals(resultValue, booksExpected.encodeToBarray())
  }

  test("Multiple key case case injection - Book1") {
    val expected = new BsonObject().put("pages", 10.0).put("someLong", 10L).put("someBoolean", false).encodeToBarray()

    val ex = ".store.books[0].book"
    val bsonInj = Boson.injector(ex, (in: Book1) => {
      Book1(in.pages + 9.0, in.someLong + 9L, !in.someBoolean)
    })
    val bsonEncoded = storeBsonAux.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(expected))
  }

  test("Double dot key injection - Book1") {
    val expected = new BsonObject().put("pages", 2.0).put("someLong", 2L).put("someBoolean", false).encodeToBarray
    val expected2 = new BsonObject().put("pages", 46.20).put("someLong", 200000L).put("someBoolean", true).encodeToBarray
    val expected3 = new BsonObject().put("pages", -6.0).put("someLong", 1578912L).put("someBoolean", false).encodeToBarray

    val ex = "..book"
    val bsonInj = Boson.injector(ex, (in: Book1) => {
      Book1(in.pages * 2, in.someLong * 2L, !in.someBoolean)
    })
    val bsonEncoded = storeBsonAux.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(expected) && resultValue.containsSlice(expected2) && resultValue.containsSlice(expected3))
  }

  test("Double dot HasElem injection - Book1") {
    val expected = new BsonObject().put("pages", 2.0).put("someLong", 2L).put("someBoolean", false).encodeToBarray
    val expected2 = new BsonObject().put("pages", 46.20).put("someLong", 200000L).put("someBoolean", true).encodeToBarray
    val expected3 = new BsonObject().put("pages", -6.0).put("someLong", 1578912L).put("someBoolean", false).encodeToBarray

    val ex = "..books[@book]"
    val bsonInj = Boson.injector(ex, (in: Book1) => {
      Book1(in.pages * 2, in.someLong * 2L, !in.someBoolean)
    })
    val bsonEncoded = storeBsonAux.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue.containsSlice(expected) && resultValue.containsSlice(expected2) && resultValue.containsSlice(expected3))
  }

  test("Key with Array Exp ..[0] - Double Dots") {
    val expr = "..[0]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[first] - Double Dots") {
    val expr = "..[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[all] - Double Dots") {
    val expr = "..[all]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[end] - Double Dots") {
    val expr = "..[end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[0 to 1] - Double Dots") {
    val expr = "..[0 to 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[0 until 1] - Double Dots") {
    val expr = "..[0 until 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[0 to end] - Double Dots") {
    val expr = "..[0 to end]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Key with Array Exp ..[0 until end] - Double Dots") {
    val expr = "..[0 until end]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonHuman.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Nested case class injection - Root Key") {
    val expr = ".book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(nestedBson.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, nestedBsonExpected.encodeToBarray)
  }

  test("Nested case class injection - Double Dot") {
    val expr = "..book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(nestedBson.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, nestedBsonExpected.encodeToBarray)
  }

  test("Nested case class injection - KeyWithArrExpr - [all]") {
    val bsonArr = new BsonArray().add(nestedBson).add(nestedBson2)
    val bsonObj = new BsonObject().put("books", bsonArr)

    val bsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2Expected)
    val bsonObjExpected = new BsonObject().put("books", bsonArrExpected)

    val expr = "..books[all].book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(bsonObj.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonObjExpected.encodeToBarray)
  }

  test("Nested case class injection - KeyWithArrExpr - [0 to end]") {
    val bsonArr = new BsonArray().add(nestedBson).add(nestedBson2)
    val bsonObj = new BsonObject().put("books", bsonArr)

    val bsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2Expected)
    val bsonObjExpected = new BsonObject().put("books", bsonArrExpected)

    val expr = "..books[0 to end].book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(bsonObj.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonObjExpected.encodeToBarray)
  }

  test("Nested case class injection - KeyWithArrExpr - [0]") {
    val bsonArr = new BsonArray().add(nestedBson).add(nestedBson2)
    val bsonObj = new BsonObject().put("books", bsonArr)

    val bsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2)
    val bsonObjExpected = new BsonObject().put("books", bsonArrExpected)

    val expr = "..books[0].book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(bsonObj.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonObjExpected.encodeToBarray)
  }

  test("Nested case class injection - KeyWithArrExpr - [first]") {
    val bsonArr = new BsonArray().add(nestedBson).add(nestedBson2)
    val bsonObj = new BsonObject().put("books", bsonArr)

    val bsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2)
    val bsonObjExpected = new BsonObject().put("books", bsonArrExpected)

    val expr = "..books[first].book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(bsonObj.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonObjExpected.encodeToBarray)
  }

  test("Nested case class injection - KeyWithArrExpr - double dot") {
    val bsonArr = new BsonArray().add(nestedBson).add(nestedBson2)
    val bsonObj = new BsonObject().put("books", bsonArr)

    val bsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2Expected)
    val bsonObjExpected = new BsonObject().put("books", bsonArrExpected)

    val expr = "..book"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(bsonObj.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonObjExpected.encodeToBarray)
  }

  test("Nested case class injection - HasElem") {
    val bsonArr = new BsonArray().add(nestedBson)
    val bsonObj = new BsonObject().put("books", bsonArr)

    val bsonArrExpected = new BsonArray().add(nestedBsonExpected)
    val bsonObjExpected = new BsonObject().put("books", bsonArrExpected)

    val expr = ".books[@book]"

    val bsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = bsonInj.go(bsonObj.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonObjExpected.encodeToBarray)
  }

  test("Key with Array Expr .species.alien[first] toUpperCase - No/Double Dots") {
    val expr = ".species.alien[first]"
    val bsonArrayExpected = new BsonArray().add("ET").add("predator").add("alien")
    val bsonExpected = new BsonObject().put("person", bsonHuman).put("alien", bsonArrayExpected)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToBarray
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expectedEncoded)
  }

  test("Nested key injection ..[0 until end].age - Single Dots") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "doe jane").put("age", 10)
    val persons = new BsonArray().add(person1).add(person2).add(person3)
    //    val client = new BsonObject().put("person", persons)
    //    val bson = new BsonObject().put("client", client)

    val ex = "..[0 until end].age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = persons.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(41, 0, 0, 0)) && (resultValue containsSlice Array(32, 0, 0, 0)) && (resultValue containsSlice Array(10, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("Nested key injection - ..person[1 until end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("client", client)

    val ex = "..person[1 until end]..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert((resultValue containsSlice Array(21, 0, 0, 0)) && (resultValue containsSlice Array(12, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  }

  test("HasElem 10 - processTypesHasElem Array Test") {
    val arr = new BsonArray().add("Something")
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(arr).add(person2)

    val json = new BsonObject().put("something", persons)

    val ex = "..persons[@age]"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToBarray)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, json.encodeToBarray)
  }

  test("HasElem 11 - processTypesHasElem Array Test") {
    val arr = new BsonArray().add("Something")
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(arr).add(person2)

    val json = new BsonObject().put("something", persons)

    val ex = ".persons[@age]"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToBarray)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, json.encodeToBarray)
  }

  test("HasElem 12") {
    val arr = new BsonArray().add("Something")
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val personBson2 = new BsonObject().put("subObject", person2)
    val persons = new BsonArray().add(arr).add(personBson2)
    val client = new BsonObject().put("persons", persons)

    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personBson2Expected = new BsonObject().put("subObject", person2Expected)
    val personsExpected = new BsonArray().add(arr).add(personBson2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)

    val ex = "..persons[@subObject]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(client.encodeToBarray)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, clientExpected.encodeToBarray)
  }

  test("HasElem 13") {
    val arr = new BsonArray().add("Something")
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val personBson2 = new BsonObject().put("subObject", person2)
    val persons = new BsonArray().add(arr).add(personBson2)
    val client = new BsonObject().put("persons", persons)

    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personBson2Expected = new BsonObject().put("subObject", person2Expected)
    val personsExpected = new BsonArray().add(arr).add(personBson2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)

    val ex = "..persons[@subObject].subObject.age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(client.encodeToBarray)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, clientExpected.encodeToBarray)
  }


  test("CodecJson - Root Injection") {
    val json = new BsonObject().put("name", "john doe")
    val ex = "."
    val jsonInj = Boson.injector(ex, (in: String) => {
      """
        |{
        | "lastName": "Not Doe"
        |}
      """.stripMargin
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue contains "Not Doe")
  }

  test("CodecJson - Top level key modification") {
    val bson = new BsonObject().put("name", "john doe")
    val ex = ".name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val jsonEncoded = bson.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert((resultValue contains "JOHN DOE") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Nested key modification - Single Dots") {
    val person = new BsonObject().putNull("nullKey").put("name", "john doe")
    val bson = new BsonObject().put("person", person)
    val ex = ".person.name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToString()
    val future = jsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("CodecJson - Nested key modification - Multiple unimportant keys- Single Dots") {
    val person = new BsonObject().put("intKey", 10).put("longKey", 10000000000L).put("boolKey", false).put("name", "john doe")
    val bson = new BsonObject().put("person", person)
    val ex = ".person.name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToString()
    val future = jsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("JOHN DOE") && resultValue.length == bsonEncoded.length)
  }

  test("CodecJson - Nested key modification - Different value types 1") {
    val person = new BsonObject().put("doubleKey", 1.0)
    val json = new BsonObject().put("person", person)

    val personExpected = new BsonObject().put("doubleKey", 2.0)
    val jsonExpected = new BsonObject().put("person", personExpected)

    val ex = ".person.doubleKey"
    val jsonInj = Boson.injector(ex, (in: Double) => {
      in * 2.0
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key modification - Different value types 2") {
    val person = new BsonObject().put("longKey", 10000000000L)
    val json = new BsonObject().put("person", person)

    val personExpected = new BsonObject().put("longKey", 20000000000L)
    val jsonExpected = new BsonObject().put("person", personExpected)
    val ex = ".person.longKey"
    val jsonInj = Boson.injector(ex, (in: Long) => {
      in * 2
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key modification - Different value types 3") {
    val person = new BsonObject().put("boolKey", false)
    val json = new BsonObject().put("person", person)

    val personExpected = new BsonObject().put("boolKey", true)
    val jsonExpected = new BsonObject().put("person", personExpected)

    val ex = ".person.boolKey"
    val jsonInj = Boson.injector(ex, (in: Boolean) => {
      !in
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key modification - Single Dots - wrong object") {
    val arr = new BsonArray().add("Something")
    val bson = new BsonObject().put("person", arr)
    val ex = ".person.name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToString()
    val future = jsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(bson.encodeToString))
  }

  test("CodecJson - Nested key modification - Single Dots - wrong object 2") {
    val arr = new BsonArray().add("Something")
    val bson = new BsonObject().put("person", arr)
    val ex = "..person"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in
    })
    val bsonEncoded = bson.encodeToString()
    val future = jsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(bson.encodeToString))
  }

  test("CodecJson - Nested key modification - Double Dots 2") {
    val person1 = new BsonObject().put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val arr = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("person", arr)

    val ex = "..person..name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bson.encodeToString())
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(bson.encodeToString))
  }

  test("CodecJson - Nested key modification, Multiple keys - Single Dots") {
    val obj = new BsonObject().put("name", "john doe")
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val json = new BsonObject().put("Wrapper", anotherObject)
    val ex = ".Wrapper.AnotherObject.SomeObject.client.person.name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("JOHN DOE") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Nested key modification, two fields- Single Dots") {
    val obj = new BsonObject().put("name", "john doe").put("age", 21)
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val json = new BsonObject().put("Wrapper", anotherObject)
    val ex = ".Wrapper.AnotherObject.SomeObject.client.person.age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("41") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Nested key modification, two fields - 2 - Single Dots") {
    val obj = new BsonObject().put("name", "john doe").put("age", 21).put("gender", "male")
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val json = new BsonObject().put("Wrapper", anotherObject)
    val ex = ".Wrapper.AnotherObject.SomeObject.client.person.gender"
    val jsonInj = Boson.injector(ex, (in: String) => {
      "female"
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("female") && resultValue.length == jsonEncoded.length + 2) // +2 because "female" is 2 character's longer than male
  }

  test("CodecJson - Nested key modification, two fields - 3 - Single Dots") {
    val obj = new BsonObject().put("name", "john doe").put("age", 21).put("gender", "male")
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val json = new BsonObject().put("Wrapper", anotherObject)
    val ex = ".Wrapper.AnotherObject.SomeObject.client.person.age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("41") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Nested key - Double Dots") {
    val json = new BsonObject().put("name", "john doe").put("age", 21)
    val ex = "..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("41") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Nested key 2 - Double Dots") {
    val obj = new BsonObject().put("name", "john doe").put("age", 21)
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val json = new BsonObject().put("Wrapper", anotherObject)
    val ex = "..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("41") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Nested key 3 - Double Dots") {
    val obj = new BsonObject().put("name", "john doe").put("age", 21)
    val person = new BsonObject().put("person", obj)
    val client = new BsonObject().put("client", person)
    val someObject = new BsonObject().put("SomeObject", client)
    val anotherObject = new BsonObject().put("AnotherObject", someObject)
    val json = new BsonObject().put("Wrapper", anotherObject)
    val ex = "..client..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.contains("41") && resultValue.length == jsonEncoded.length)
  }

  test("CodecJson - Java Instant injection") {
    val ins = Instant.now()
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(1000))
    val client = new BsonObject().put("person", person1)
    val json = new BsonObject().put("client", client)

    val expectedPerson = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(2000))
    val expectedClient = new BsonObject().put("person", expectedPerson)
    val expectedJson = new BsonObject().put("client", expectedClient)

    val ex = "..instant"
    val jsonInj = Boson.injector(ex, (in: Instant) => {
      in.plusMillis(1000)
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(expectedJson.encodeToString))
  }

  test("CodecJson - HasElem injection test") {
    val person1 = new BsonObject().putNull("nullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val json = new BsonObject().put("persons", bsonArray)

    val person1Expected = new BsonObject().putNull("nullKey").put("name", "JOHN DOE")
    val person2Expected = new BsonObject().put("name", "JANE DOE")
    val bsonArrayExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val jsonExpected = new BsonObject().put("persons", bsonArrayExpected)

    val ex = ".persons[@name]"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase()
    })
    val jsonEncoded = json.encodeToString()
    val future = jsonInj.go(jsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem injection test, one object does not contain the elem") {
    val person1 = new BsonObject().put("name", "John Doe")
    val person2 = new BsonObject().put("surname", "Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val json = new BsonObject().put("persons", bsonArray)

    val person1Expected = new BsonObject().put("name", "JOHN DOE")
    val bsonArrayExpected = new BsonArray().add(person1Expected).add(person2)
    val jsonExpected = new BsonObject().put("persons", bsonArrayExpected)


    val ex = ".persons[@name]"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 3") {
    val bsonArray = new BsonArray().add("Not an Object").add("Something")
    val json = new BsonObject().put("persons", bsonArray)

    val bsonArrayExpected = new BsonArray().add("Not an Object").add("Something")
    val jsonExpected = new BsonObject().put("persons", bsonArrayExpected)

    val ex = ".persons[@name]"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 4") {
    val person1 = new BsonObject().put("name", "John Doe")
    val bsonArray = new BsonArray().add(person1).add("Something")
    val json = new BsonObject().put("persons", bsonArray)

    val person1Expected = new BsonObject().put("name", "JOHN DOE")
    val bsonArrayExpected = new BsonArray().add(person1Expected).add("Something")
    val jsonExpected = new BsonObject().put("persons", bsonArrayExpected)


    val ex = ".persons[@name]"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 5") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val bsonArray = new BsonArray().add(person1).add(person2)
    val json = new BsonObject().put("persons", bsonArray)

    val person1Expected = new BsonObject().put("name", "John Doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val bsonArrayExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val jsonExpected = new BsonObject().put("persons", bsonArrayExpected)

    val ex = ".persons[@age]"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 6") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "John Doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = ".client.persons[@age]"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 7") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "John Doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..persons[@age]"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 8") {
    val person1 = new BsonObject().put("name", "John Doe").put("age", 21)
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("persons", persons)
    val clientJson = new BsonObject().put("client", client)
    val obj = new BsonObject().put("obj", clientJson)
    val json = new BsonObject().put("wrapper", obj)

    val person1Expected = new BsonObject().put("name", "John Doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)
    val clientJsonExpected = new BsonObject().put("client", clientExpected)
    val objExpected = new BsonObject().put("obj", clientJsonExpected)
    val jsonExpected = new BsonObject().put("wrapper", objExpected)

    val ex = "..obj..persons[@age]"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - HasElem 12") {
    val arr = new BsonArray().add("Something")
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val personBson2 = new BsonObject().put("subObject", person2)
    val persons = new BsonArray().add(arr).add(personBson2)
    val client = new BsonObject().put("persons", persons)

    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personBson2Expected = new BsonObject().put("subObject", person2Expected)
    val personsExpected = new BsonArray().add(arr).add(personBson2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)

    val ex = "..persons[@subObject]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(client.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(clientExpected.encodeToString))
  }

  test("CodecJson - HasElem 13") {
    val arr = new BsonArray().add("Something")
    val person2 = new BsonObject().put("name", "Jane Doe").put("age", 12)
    val personBson2 = new BsonObject().put("subObject", person2)
    val persons = new BsonArray().add(arr).add(personBson2)
    val client = new BsonObject().put("persons", persons)

    val person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32)
    val personBson2Expected = new BsonObject().put("subObject", person2Expected)
    val personsExpected = new BsonArray().add(arr).add(personBson2Expected)
    val clientExpected = new BsonObject().put("persons", personsExpected)

    val ex = "..persons[@subObject].subObject.age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(client.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(clientExpected.encodeToString))
  }

  test("CodecJson - Key case class injection") {
    val book = new BsonObject().put("name", "Title1").put("pages", 1)
    val json = new BsonObject().put("book", book)

    val expected = new BsonObject().put("name", "LOTR").put("pages", 320)
    val jsonExpected = new BsonObject().put("book", expected)

    val ex = ".book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book("LOTR", 320)
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodeJson - HasElem case class injection") {
    val ex = ".store.books[@book]"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeBsonExpected.encodeToString))
  }

  test("CodeJson - HasElem case class injection 2") {
    val ex = "..books[@book]"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeBsonExpected.encodeToString))
  }

  test("CodeJson - HasElem case class injection 3") {
    val ex = "..store..books[@book]"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeBsonExpected.encodeToString))
  }

  test("CodecJson - Nested case class injection - root injection ") {
    val expectedBook = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthor)
    val expectedJson = new BsonObject().put("book", expectedBook)

    val expr = ".book"
    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      NestedBook(in.name.toUpperCase, in.pages + 100, in.author)
    })

    val future = jsonInj.go(nestedBson.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedJson.encodeToString))
  }

  test("CodecJson - Nested case class injection - double dot - root injection ") {
    val expectedBook = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthor)
    val expectedJson = new BsonObject().put("book", expectedBook)

    val expr = "..book"
    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      NestedBook(in.name.toUpperCase, in.pages + 100, in.author)
    })

    val future = jsonInj.go(nestedBson.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedJson.encodeToString))
  }

  test("CodecJson - Nested case class injection - HasElem") {
    val jsonArr = new BsonArray().add(nestedBson)
    val jsonObj = new BsonObject().put("books", jsonArr)

    val jsonArrExpected = new BsonArray().add(nestedBsonExpected)
    val jsonObjExpected = new BsonObject().put("books", jsonArrExpected)

    val expr = ".books[@book]"

    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = jsonInj.go(jsonObj.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(jsonObjExpected.encodeToString))
  }

  test("CodecJson - Nested case class injection - KeyWithArrExpr - [all]") {
    val jsonArr = new BsonArray().add(nestedBson).add(nestedBson2)
    val jsonObj = new BsonObject().put("books", jsonArr)

    val jsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2Expected)
    val jsonObjExpected = new BsonObject().put("books", jsonArrExpected)

    val expr = "..books[all].book"

    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = jsonInj.go(jsonObj.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(jsonObjExpected.encodeToString))
  }

  test("CodecJson - Nested case class injection - KeyWithArrExpr - [0 to end]") {
    val jsonArr = new BsonArray().add(nestedBson)
    val jsonObj = new BsonObject().put("books", jsonArr)

    val jsonArrExpected = new BsonArray().add(nestedBsonExpected)
    val jsonObjExpected = new BsonObject().put("books", jsonArrExpected)

    val expr = "..books[0 to end].book"

    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = jsonInj.go(jsonObj.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(jsonObjExpected.encodeToString))
  }

  test("CodecJson - Nested case class injection - KeyWithArrExpr - [0]") {
    val jsonArr = new BsonArray().add(nestedBson)
    val jsonObj = new BsonObject().put("books", jsonArr)

    val jsonArrExpected = new BsonArray().add(nestedBsonExpected)
    val jsonObjExpected = new BsonObject().put("books", jsonArrExpected)

    val expr = "..books[0].book"

    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = jsonInj.go(jsonObj.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(jsonObjExpected.encodeToString))
  }

  test("CodecJson - Nested case class injection - KeyWithArrExpr - [first]") {
    val jsonArr = new BsonArray().add(nestedBson)
    val jsonObj = new BsonObject().put("books", jsonArr)

    val jsonArrExpected = new BsonArray().add(nestedBsonExpected)
    val jsonObjExpected = new BsonObject().put("books", jsonArrExpected)

    val expr = "..books[first].book"

    val jsonInj = Boson.injector(expr, (in: NestedBook) => {
      val newAuthor = Author(in.author.firstName.toUpperCase, in.author.lastName.toUpperCase, in.author.age + 20)
      NestedBook(in.name.toUpperCase, in.pages + 100, newAuthor)
    })

    val future = jsonInj.go(jsonObj.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(jsonObjExpected.encodeToString))
  }


  test("CodeJson - Multiple key case class injection") {
    val book: BsonObject = new BsonObject().put("name", "LOTR").put("pages", 320)
    val bsonBook: BsonObject = new BsonObject().put("book", book)

    val books: BsonArray = new BsonArray().add(bsonBook).add(bsonBook2)
    val store: BsonObject = new BsonObject().put("books", books)
    val storeJsonExpected: BsonObject = new BsonObject().put("store", store)

    val ex = ".store.books[0].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book("LOTR", 320)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeJsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - [first]") {
    val books = new BsonArray().add(bsonBookExpected).add(bsonBook2)
    val store = new BsonObject().put("books", books)
    val storeJsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[first].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeJsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - [1]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeJsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[1].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeJsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - [all]") {
    val ex = ".store.books[all].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeBsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - [0 to end]") {
    val ex = ".store.books[0 to end].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeBsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - [0 until end]") {
    val books = new BsonArray().add(bsonBookExpected).add(bsonBook2)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[0 until end].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeBsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - [end]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeJsonExpected = new BsonObject().put("store", store)

    val ex = ".store.books[end].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeJsonExpected.encodeToString))
  }

  test("CodecJson - Case case injection - ..books[1]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeJsonExpected = new BsonObject().put("store", store)

    val ex = "..books[1].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })

    val future = jsonInj.go(storeBson.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(storeJsonExpected.encodeToString))
  }

  test("CodecJson - Case class injection - arr expression ..[0]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString
    val ex = "..[0].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[first]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString
    val ex = "..[first].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }


  test("CodecJson - Case class injection - arr expression ..[1]") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToString
    val ex = "..[1].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[end]") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToString
    val ex = "..[end].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[0 to end]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected).encodeToString
    val ex = "..[0 to end].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[0 until end]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString
    val ex = "..[0 until end].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[0 to 1]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected).encodeToString
    val ex = "..[0 to 1].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[0 until 1]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString
    val ex = "..[0 until 1].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Case class injection - arr expression ..[all]") {
    val booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected).encodeToString
    val ex = "..[all].book"
    val jsonInj = Boson.injector(ex, (in: Book) => {
      Book(in.name, in.pages + 100)
    })
    val future = jsonInj.go(booksArr.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(booksExpected))
  }

  test("CodecJson - Top level halfkey modification") {
    val bson = new BsonObject().put("name", "John Doe")
    val ex1 = ".*ame"
    val ex2 = ".nam*"
    val ex3 = ".n*me"

    val jsonInj1 = Boson.injector(ex1, (in: String) => {
      in.toUpperCase
    })
    val jsonInj2 = Boson.injector(ex2, (in: String) => {
      in.toLowerCase
    })
    val jsonInj3 = Boson.injector(ex3, (in: String) => {
      in + " Hello"
    })

    val jsonEncoded = bson.encodeToString()

    val future1 = jsonInj1.go(jsonEncoded)
    val resultValue1: String = Await.result(future1, Duration.Inf)

    val future2 = jsonInj2.go(jsonEncoded)
    val resultValue2: String = Await.result(future2, Duration.Inf)

    val future3 = jsonInj3.go(jsonEncoded)
    val resultValue3: String = Await.result(future3, Duration.Inf)

    assert(
      ((new String(resultValue1) contains "JOHN DOE") && resultValue1.length == jsonEncoded.length) &&
        ((new String(resultValue2) contains "john doe") && resultValue2.length == jsonEncoded.length) &&
        (new String(resultValue3) contains "John Doe Hello")
    )
  }

  test("CodecJson - Key with Array Exp .Key[1] modification toUpperCase - Single Dots") {
    val expr = ".person[1]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[0 to 1] modification toUpperCase - Single Dots") {
    val expr = ".person[0 to 1]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[0 until 2] modification toUpperCase - Single Dots") {
    val expr = ".person[0 until 2]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[first] modification toUpperCase - Single Dots") {
    val expr = ".person[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[all] modification toUpperCase - Single Dots") {
    val expr = ".person[all]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[end] modification toUpperCase - Single Dots") {
    val expr = ".person[end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[1 to end] modification toUpperCase - Single Dots") {
    val expr = ".person[1 to end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .Key[0 until end] modification toUpperCase - Single Dots") {
    val expr = ".person[0 until end]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[1] modification toUpperCase - Double Dots") {
    val expr = "..person[1]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[first] modification toUpperCase - Double Dots") {
    val expr = "..person[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[0] modification toUpperCase - Single Dots") {
    val expr = ".[0]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[first] modification toUpperCase - Single Dots") {
    val expr = ".[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[all] modification toUpperCase - Single Dots") {
    val expr = ".[all]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[end] modification toUpperCase - Single Dots") {
    val expr = ".[end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[1 to 2] modification toUpperCase - Single Dots") {
    val expr = ".[1 to 2]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[0 until 2] modification toUpperCase - Single Dots") {
    val expr = ".[0 until 2]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[1 to end] modification toUpperCase - Single Dots") {
    val expr = ".[1 to end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp .[1 until end] modification toUpperCase - Single Dots") {
    val expr = ".[1 until end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..[0] - Double Dots") {
    val expr = "..[0]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[first] - Double Dots") {
    val expr = "..[first]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[all] - Double Dots") {
    val expr = "..[all]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[end] - Double Dots") {
    val expr = "..[end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[1 to 2] - Double Dots") {
    val expr = "..[1 to 2]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[1 until 2] - Double Dots") {
    val expr = "..[1 until 2]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[1 to end] - Double Dots") { //TODO
    val expr = "..[1 to end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..[1 until end] - Double Dots") {
    val expr = "..[1 until end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedEncoded = bsonArrayExpected.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonHuman.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Expr .species.alien[first] toUpperCase - Single Dots") {
    val expr = ".species.alien[first]"
    val bsonArrayExpected = new BsonArray().add("ET").add("predator").add("alien")
    val bsonExpected = new BsonObject().put("person", bsonHuman).put("alien", bsonArrayExpected)
    val expectedEncoded = new BsonObject().put("species", bsonExpected).encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    println(bsonSpeciesObj.encodeToString)
    val future = jsonInj.go(bsonSpeciesObj.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result equals expectedEncoded)
  }

  test("CodecJson - Key with Array Exp ..Key[all] modification toUpperCase - Double Dots") {
    val expr = "..person[all]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[end] modification toUpperCase - Double Dots") {
    val expr = "..person[end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("person2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[1 to 2] modification toUpperCase - Double Dots") {
    val expr = "..person[1 to 2]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[1 until 2] modification toUpperCase - Double Dots") {
    val expr = "..person[1 until 2]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[1 to end] modification toUpperCase - Double Dots") {
    val expr = "..person[1 to end]"
    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Key with Array Exp ..Key[0 until end] modification toUpperCase - Double Dots") {
    val expr = "..person[0 until end]"
    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
    val expectedEncoded = expectedBson.encodeToString
    val jsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = jsonInj.go(bsonObjArray.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expectedEncoded))
  }

  test("CodecJson - Nested key injection - ..person[all]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[all]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[1]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[1]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[first].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[first].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[all].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[all].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[end].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[end].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 to 1].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 to 1].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 until 1].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 until 1].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 to end].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 to end].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 until end].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 until end].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }


  test("CodecJson - Nested key injection - ..person[first]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[first]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 to end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 to end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 until end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 until end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 to 1]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 to 1]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..person[0 until 1]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val client = new BsonObject().put("person", persons)
    val json = new BsonObject().put("client", client)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val clientExpected = new BsonObject().put("person", personsExpected)
    val jsonExpected = new BsonObject().put("client", clientExpected)

    val ex = "..person[0 until 1]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(json.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(jsonExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[0].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[first].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[first].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[all].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[all].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[end].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[end].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0 to 1].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[0 to 1].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0 until 1].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[0 until 1].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0 to end].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[0 to end].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0 until end].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = "..[0 until end].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 15)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[0]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[all]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 35)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[all]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 35)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[0 to 1]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 15)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[0 to 1]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[1 until 2]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 15)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[1 until 2]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[1 to end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 35)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[1 to end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - ..[1 until end]..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val person3 = new BsonObject().put("name", "fn ln").put("age", 15)
    val persons = new BsonArray().add(person1).add(person2).add(person3)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val person3Expected = new BsonObject().put("name", "fn ln").put("age", 15)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected).add(person3Expected)

    val ex = "..[1 until end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - .[0 until end]..age injection") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = ".[0 until end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - .[0 to end]..age injection") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = ".[0 to end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - .[all]..age injection") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = ".[all]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - .[end]..age injection") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 32)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = ".[end]..age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - ..key[0]..key Same key - Double") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsArr = new BsonArray().add(person1).add(person2)
    val persons = new BsonObject().put("name", personsArr)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "JANE DOE").put("age", 12)
    val personsArrExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val personsExpected = new BsonObject().put("name", personsArrExpected)

    val ex = "..name[end]..name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - .key[0]..key Same key - Single/Double") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsArr = new BsonArray().add(person1).add(person2)
    val persons = new BsonObject().put("name", personsArr)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 21)
    val person2Expected = new BsonObject().put("name", "JANE DOE").put("age", 12)
    val personsArrExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val personsExpected = new BsonObject().put("name", personsArrExpected)

    val ex = ".name[end]..name"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Nested key injection - .[first].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)

    val person1Expected = new BsonObject().put("name", "john doe").put("age", 41)
    val person2Expected = new BsonObject().put("name", "jane doe").put("age", 12)
    val personsExpected = new BsonArray().add(person1Expected).add(person2Expected)

    val ex = ".[first].age"
    val jsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Array within array .Array[0].Array[0] - Single") {
    val person1 = new BsonArray().add("name").add("john doe").add("age")
    val person2 = new BsonArray().add("name").add("jane doe").add("age")
    val personsArr = new BsonArray().add(person1).add(person2)
    val persons = new BsonObject().put("persons", personsArr)

    println(persons)

    val person1Expected = new BsonArray().add("name").add("john doe").add("age")
    val person2Expected = new BsonArray().add("name").add("JANE DOE").add("age")
    val personsArrExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val personsExpected = new BsonObject().put("persons", personsArrExpected)

    val ex = ".persons[1].[1]"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

  test("CodecJson - Array within array ..Array[0].Array[0] - Double/Single") {
    val person1 = new BsonArray().add("name").add("john doe").add("age")
    val person2 = new BsonArray().add("name").add("jane doe").add("age")
    val personsArr = new BsonArray().add(person1).add(person2)
    val persons = new BsonObject().put("persons", personsArr)

    println(persons)

    val person1Expected = new BsonArray().add("name").add("john doe").add("age")
    val person2Expected = new BsonArray().add("name").add("JANE DOE").add("age")
    val personsArrExpected = new BsonArray().add(person1Expected).add(person2Expected)
    val personsExpected = new BsonObject().put("persons", personsArrExpected)

    val ex = "..persons[1].[1]"
    val jsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })

    val future = jsonInj.go(persons.encodeToString)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue.equals(personsExpected.encodeToString))
  }

}
