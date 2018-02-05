package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import com.fasterxml.jackson.core.JsonFactory
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonPath.ArrExpr
import io.boson.bson.bsonValue.BsValue
import io.netty.buffer.Unpooled
import io.netty.util.ResourceLeakDetector
import org.scalatest.FunSuite
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Try
/**
  * Created by Ricardo Martins on 23/01/2018.
  */
@RunWith(classOf[JUnitRunner])
class jpPlusPlusInjectorTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  private val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
  private val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
  private val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
  private val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
  private val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  private val sEditions3 = new BsonArray().add(edition3)
  private val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
  private val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  private val sEditions2 = new BsonArray().add(edition2)
  private val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
  private val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  private val sEditions1 = new BsonArray().add(edition1)
  private val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
  private val books = new BsonArray().add(title1).add(title2).add(title3)
  private val store = new BsonObject().put("Book", books).put("Hat", hats)
  private val bson = new BsonObject().put("Store", store)

  val bsonEvent: BsonObject = bson
  val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

    test(".Store.Book..0.Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book..0.Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store..Book.0.Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store..Book.0.Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store.Book.0..Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book.0..Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store.Book..0..Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book..0..Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store..Book..0..Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store..Book..0..Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test("Store..Book..0..Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = "Store..Book..0..Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store.Book[@Price].Title") {
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book[@Price].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store..Book[@Price].Title") {
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooksx: BsonArray = new BsonArray().add(edition1x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store..Book[@Price].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store..Book[@Price]..Title") {
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooksx: BsonArray = new BsonArray().add(edition1x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38).put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store..Book[@Price]..Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test("Book[@Price].Title") {
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooksx: BsonArray = new BsonArray().add(edition1x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = "Book[@Price].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store.Book.[0].Title") {
      //val edition4x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      //val lastBooksx: BsonArray = new BsonArray().add(edition4x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      //.put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      //val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      //val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      //.put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book.[0].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store.Book..[0].Title") {
      //val edition4x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      //val lastBooksx: BsonArray = new BsonArray().add(edition4x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      //.put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      //val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      //val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
      //.put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book..[0].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test("..[0].Title") {
      //val edition4x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      //val lastBooksx: BsonArray = new BsonArray().add(edition4x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
      //.put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      //val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      //val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
      //.put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = "..[0].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store..Book.[0 to 1].Title") {
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooksx: BsonArray = new BsonArray().add(edition1x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store..Book.[0 to 1].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store.Book.[0 to 1].Title") {
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooksx: BsonArray = new BsonArray().add(edition1x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      val edition4: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book.[0 to 1].Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

    test(".Store..Book.[0 to 1]..Title") {
      val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
      val lastBooksx: BsonArray = new BsonArray().add(edition1x)
      val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
      val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooksx)
      val sEditions3x: BsonArray = new BsonArray().add(edition3x)
      val title3x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
      val edition2x: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
      val sEditions2x: BsonArray = new BsonArray().add(edition2x)
      val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
      //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1x: BsonArray = new BsonArray().add(edition1x)
      val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
      val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
      val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
      val bsoneventx: BsonObject = new BsonObject().put("Store", storex)
      val validatedByteArr: Array[Byte] = bsoneventx.encodeToBarray()
      /**/

      val edition4: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val lastBooks: BsonArray = new BsonArray().add(edition4)
      val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
      val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
      val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
      val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
      val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38).put("Book", lastBooks)
      val sEditions3: BsonArray = new BsonArray().add(edition3)
      val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
      val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
      val sEditions2: BsonArray = new BsonArray().add(edition2)
      val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
      val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
      val sEditions1: BsonArray = new BsonArray().add(edition1)
      val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
      val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
      val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

      /**/
      val bsonevent: BsonObject = new BsonObject().put("Store", store)

      val expression: String = ".Store..Book.[0 to 1]..Title"
      val boson: Boson = Boson.injector(expression, (x: String) => "SUPERJAVA")
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsonevent.encodeToBarray()
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }

  test("..Title") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala").put("Hat", "Cap")
    //val store2x: BsonObject = new BsonObject().put("Store", store3x)
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("Hat", "Cap")
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x )

    /**/


    /**/
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    //val store2: BsonObject = new BsonObject().put("Store", store3)
    val store1: BsonObject = new BsonObject().put("Store",store3 )
    val bsonevent: BsonObject = new BsonObject().put("Store", store1)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..*re"
    val boson: Boson = Boson.injector(expression, (x: Map[String, Any]) => x.+(("Hat", "Cap")))
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test("..[#]") {
    val value00x: BsonObject = new BsonObject().put("one", 1).put("ten", 10).put("three", 3).put("two", 2)
    val value01x: BsonObject = new BsonObject().put("five", 5).put("four", 4).put("six", 6)
    val value10x: BsonObject = new BsonObject().put("one", 1).put("ten", 10).put("three", 3).put("two", 2)
    val value11x: BsonObject = new BsonObject().put("five", 5).put("four", 4).put("six", 6)
    val value20x: BsonObject = new BsonObject().put("one", 1).put("three", 3).put("two", 2).put("ten", 10)
    val value21x: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value30x: BsonObject = new BsonObject().put("one", 1).put("three", 3).put("two", 2).put("ten", 10)
    val value31x: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val typeDListx: BsonArray = new BsonArray().add(value30x).add(value31x)
    val typeCListx: BsonArray = new BsonArray().add(value20x).add(value21x)
    val typeBListx: BsonArray = new BsonArray().add(value10x).add(value11x)
    val typeAListx: BsonArray = new BsonArray().add(value00x).add(value01x)
    val bookTypeBx: BsonObject = new BsonObject().put("TypeC", typeCListx).put("TypeD",typeDListx)
    val bookTypeAx: BsonObject = new BsonObject().put("TypeA", typeAListx).put("TypeB",typeBListx).put("ten", 10)
    val booksx: BsonArray = new BsonArray().add(bookTypeAx).add(bookTypeBx)
    val rootx: BsonObject = new BsonObject().put("Store", booksx)
    /**/

    val value00: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value01: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value10: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value11: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value20: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value21: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value30: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value31: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val typeDList: BsonArray = new BsonArray().add(value30).add(value31)
    val typeCList: BsonArray = new BsonArray().add(value20).add(value21)
    val typeBList: BsonArray = new BsonArray().add(value10).add(value11)
    val typeAList: BsonArray = new BsonArray().add(value00).add(value01)
    val bookTypeB: BsonObject = new BsonObject().put("TypeC", typeAList).put("TypeD",typeBList)
    val bookTypeA: BsonObject = new BsonObject().put("TypeA", typeAList).put("TypeB",typeBList)
    val books: BsonArray = new BsonArray().add(bookTypeA).add(bookTypeB)
    val root: BsonObject = new BsonObject().put("Store", books)
    /**/

    val validatedByteArr: Array[Byte] = root.encodeToBarray()

    val expression: String = "..[0]"
    val boson: Boson = Boson.injector(expression, (x: Map[String, Any]) => x.+(("ten", 10)))
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    //result.foreach(b => println("char= " + b.toChar + " byte= " + b ))
    val bE: Array[Byte] = rootx.encodeToBarray()

    //println("sizes= ("+ result.length +","+ bE.length+")")
    //result.zip(bE).foreach(pb => println("["+pb._1.toChar+","+pb._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test("..key[#]") {
    val value00x: BsonObject = new BsonObject().put("one", 1).put("three", 3).put("two", 2)
    val value01x: BsonObject = new BsonObject().put("five", 5).put("four", 4).put("six", 6)
    val value10x: BsonObject = new BsonObject().put("one", 1).put("three", 3).put("two", 2)
    val value11x: BsonObject = new BsonObject().put("five", 5).put("four", 4).put("six", 6)
    val value20x: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value21x: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value30x: BsonObject = new BsonObject().put("one", 1).put("ten", 10).put("three", 3).put("two", 2)
    val value31x: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val typeDListx: BsonArray = new BsonArray().add(value30x).add(value31x)
    val typeCListx: BsonArray = new BsonArray().add(value20x).add(value21x)
    val typeBListx: BsonArray = new BsonArray().add(value10x).add(value11x)
    val typeAListx: BsonArray = new BsonArray().add(value00x).add(value01x)
    val bookTypeBx: BsonObject = new BsonObject().put("TypeC", typeCListx).put("Store",typeDListx)
    val bookTypeAx: BsonObject = new BsonObject().put("TypeA", typeAListx).put("TypeB",typeBListx).put("ten", 10)
    val booksx: BsonArray = new BsonArray().add(bookTypeAx).add(bookTypeBx)
    val rootx: BsonObject = new BsonObject().put("Store", booksx)
    /**/

    val value00: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value01: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value10: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value11: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value20: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value21: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val value30: BsonObject = new BsonObject().put("one", 1).put("two", 2).put("three", 3)
    val value31: BsonObject = new BsonObject().put("four", 4).put("five", 5).put("six", 6)
    val typeDList: BsonArray = new BsonArray().add(value30).add(value31)
    val typeCList: BsonArray = new BsonArray().add(value20).add(value21)
    val typeBList: BsonArray = new BsonArray().add(value10).add(value11)
    val typeAList: BsonArray = new BsonArray().add(value00).add(value01)
    val bookTypeB: BsonObject = new BsonObject().put("TypeC", typeAList).put("Store",typeBList)
    val bookTypeA: BsonObject = new BsonObject().put("TypeA", typeAList).put("TypeB",typeBList)
    val books: BsonArray = new BsonArray().add(bookTypeA).add(bookTypeB)
    val root: BsonObject = new BsonObject().put("Store", books)
    /**/

    val validatedByteArr: Array[Byte] = root.encodeToBarray()

    val expression: String = "..Store[0]"
    val boson: Boson = Boson.injector(expression, (x: Map[String, Any]) => x.+(("ten", 10)))
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    //result.foreach(b => println("char= " + b.toChar + " byte= " + b ))
    val bE: Array[Byte] = rootx.encodeToBarray()

    //println("sizes= ("+ result.length +","+ bE.length+")")
    //result.zip(bE).foreach(pb => println("["+pb._1.toChar+","+pb._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test("Inj ..Books[0 until end]"){
    //val book6: BsonObject = new BsonObject().put("Title", "Prolog")
    //val book5: BsonObject = new BsonObject().put("Title", "Lisp")
    //val books3: BsonArray = new BsonArray().add(book5).add(book6).add(book5)
    val book4: BsonObject = new BsonObject().put("Title", "C")
    val book3: BsonObject = new BsonObject().put("Title", "C++")
    val book2: BsonObject = new BsonObject().put("Title", "Java")//.put("Books", books3)
    val book1: BsonObject = new BsonObject().put("Title", "Scala")
    val books2: BsonArray = new BsonArray().add(book3).add(book4).add(book3)
    val books1: BsonArray = new BsonArray().add(book1).add(book2).add(book3)
    val sector2: BsonObject = new BsonObject().put("Books", books2)
    val sector1: BsonObject = new BsonObject().put("Books", books1)
    val sectors: BsonArray = new BsonArray().add(sector1).add(sector2)
    val root: BsonObject = new BsonObject().put("Library", sectors)

    val expression: String =  "..[1 until end]"

    val bosonI: Boson = Boson.injector(expression, (x:Map[String, Any]) => x.+(("Street?", "im Lost")))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(root.encodeToBarray())
    //println("injFuture="+new String(injFuture.join()))




    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    //val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    //boson.go(injFuture.join())



    //val book6x: BsonObject = new BsonObject().put("Title", "Prolog").put("Street?", "im Lost")
    //val book5x: BsonObject = new BsonObject().put("Title", "Lisp")
    //val books3x: BsonArray = new BsonArray().add(book5x).add(book6x).add(book5x)
    val book4x: BsonObject = new BsonObject().put("Title", "C").put("Street?", "im Lost")
    val book3x: BsonObject = new BsonObject().put("Title", "C++")
    val book2x: BsonObject = new BsonObject().put("Title", "Java").put("Street?", "im Lost")//.put("Books", books3x)
    val book1x: BsonObject = new BsonObject().put("Title", "Scala")
    val books2x: BsonArray = new BsonArray().add(book3x).add(book4x).add(book3x)
    val books1x: BsonArray = new BsonArray().add(book1x).add(book2x).add(book3x)
    val sector2x: BsonObject = new BsonObject().put("Books", books2x)
    val sector1x: BsonObject = new BsonObject().put("Books", books1x)
    val sectorsx: BsonArray = new BsonArray().add(sector1x).add(sector2x)
    val rootx: BsonObject = new BsonObject().put("Library", sectorsx)



    //injFuture.join().zip(rootx.encodeToBarray()).foreach(pb => println("["+pb._1.toChar+","+pb._2.toChar+"]"))
    //println("sizes= ("+ injFuture.join().length +","+ rootx.encodeToBarray().length+")")


    //assertEquals("", future.join().getValue.toString)
    assertTrue(injFuture.join().zip(rootx.encodeToBarray()).forall(bs => bs._1==bs._2))

  }

  test("Inj .key1..key2[#]..key3[@elem]"){

    val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5: BsonArray = new BsonArray().add(b11)
    val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val br4: BsonArray = new BsonArray().add(b10)
    val b9: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine")
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

    val b11x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val br5x: BsonArray = new BsonArray().add(b11x)
    val b10x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val br4x: BsonArray = new BsonArray().add(b10x)
    val b9x: BsonObject = new BsonObject().put("Price", 40).put("Title", "ScalaMachine").put("Street?", "im Lost")
    val br3x: BsonArray = new BsonArray().add(b9x)
    val b7x: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
    val b6x: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
    val b5x: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
    val b4x: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3x)
    val b3x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4x)
    val b8x: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5x)
    val br1x: BsonArray = new BsonArray().add(b3x).add(b4x).add(b8x)
    val br2x: BsonArray = new BsonArray().add(b5x).add(b6x).add(b7x).add(b3x)
    val b2x: BsonObject = new BsonObject().put("Book", br1x).put("Hatk", br2x)
    val bsonEventx: BsonObject = new BsonObject().put("Store", b2x)
    val validatedByteArrx: Array[Byte] = bsonEventx.encodeToBarray()


    val expression: String =  ".Store..Book[1 until end]..SpecialEditions[@Price]"

    val bosonI: Boson = Boson.injector(expression, (x:Map[String, Any]) => x.+(("Street?", "im Lost")))
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(validatedByteArr)
    //println("injFuture="+new String(injFuture.join()))

    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
    boson.go(injFuture.join())

    //assertEquals("", future.join().getValue.toString)
    //injFuture.join().zip(validatedByteArrx).foreach(pb => println("["+pb._1.toChar+","+pb._2.toChar+"]"))
    //println("sizes= ("+ injFuture.join().length +","+ validatedByteArrx.length+")")


    //assertEquals("", future.join().getValue.toString)
    //assertTrue(injFuture.join().zip(rootx.encodeToBarray()).forall(bs => bs._1==bs._2))
    assertTrue(injFuture.join().zip(validatedByteArrx).forall(bs => bs._1==bs._2))
  }

  test("Inj ROOT OBJECT -> ."){
    val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bson: BsonObject = new BsonObject().put("Store", store)


    val expression: String = "."
    val boson: Boson = Boson.injector(expression, (x: Map[String, Any]) => x.+(("Another", "field")))
    val future: CompletableFuture[Array[Byte]] = boson.go(bson.encodeToBarray())
    val result: Array[Byte] = future.join()

    val futureEx: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val bosonEx: Boson = Boson.extractor(".Another", (out: BsValue) => futureEx.complete(out))
    bosonEx.go(result)

    assertEquals("field", futureEx.join().getValue.asInstanceOf[Vector[Any]].head)
  }

  test("Inj ROOT ARRAY -> ."){
    val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val store1: BsonObject = new BsonObject().put("Store1", store)
    val store2: BsonObject = new BsonObject().put("Store2", store)
    val bson: BsonArray = new BsonArray().add(store1).add(store2)



    val expression: String = "."
    val boson: Boson = Boson.injector(expression, (x: List[Any]) => x.:+("Store3ComingSoon"))
    val future: CompletableFuture[Array[Byte]] = boson.go(bson.encodeToBarray())
    val result: Array[Byte] = future.join()

    val futureEx: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val bosonEx: Boson = Boson.extractor(".[2]", (out: BsValue) => futureEx.complete(out))
    bosonEx.go(result)

    assertEquals("Store3ComingSoon", futureEx.join().getValue.asInstanceOf[Vector[String]].head)
  }

  test("Inj .[first]"){
    val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val store1: BsonObject = new BsonObject().put("Store1", store)
    val store2: BsonObject = new BsonObject().put("Store2", store)
    val bson: BsonArray = new BsonArray().add(store1).add(store2)



    val expression: String = ".[first]"
    val boson: Boson = Boson.injector(expression, (x: Map[String, Any]) => x.+(("Store3ComingSoon", "VerySoon")))
    val future: CompletableFuture[Array[Byte]] = boson.go(bson.encodeToBarray())
    val result: Array[Byte] = future.join()

    val futureEx: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val bosonEx: Boson = Boson.extractor(".Store3ComingSoon", (out: BsValue) => futureEx.complete(out))
    bosonEx.go(result)

    assertEquals("VerySoon", futureEx.join().getValue.asInstanceOf[Vector[String]].head)
  }

  test("Inj .[end]"){

    val bA: BsonArray = new BsonArray().add(1).add(2).add(3)

    val bA1: BsonArray = new BsonArray().add(1).add(2).add(9)

    val boson: BosonImpl = new BosonImpl(Option(bA.encodeToBarray()))
    val arr: ArrExpr = ArrExpr(0, Some("end"), None)
    val f: Int => Int = (x: Int) => x*3
    val bosonRes: BosonImpl = boson.modifyArrayEnd(List((arr,".")), boson.getByteBuf,f, "end", 0.toString )

    //bosonRes.getByteBuf.array().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bA1.encodeToBarray().length === bosonRes.getByteBuf.array().length)
    assertTrue(bA1.encodeToBarray().zip(bosonRes.getByteBuf.array()).forall(db => db._1.equals(db._2)))
  }

  test("Inj ..[end]"){
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(6)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(3)

    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(18)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(9)

    val boson: BosonImpl = new BosonImpl(Option(bA.encodeToBarray()))
    val arr: ArrExpr = ArrExpr(0, Some("end"), None)
    val f: Int => Int = (x: Int) => x*3
    val bosonRes: BosonImpl = boson.modifyArrayEnd(List((arr,"..")), boson.getByteBuf,f, "end", 0.toString )

    //bosonRes.getByteBuf.array().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bA1.encodeToBarray().length === bosonRes.getByteBuf.array().length)
    assertTrue( bA1.encodeToBarray().zip(bosonRes.getByteBuf.array()).forall(db => db._1.equals(db._2)))

  }

  test("Inj key1.[end]"){

    val bA: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonA: BsonObject = new BsonObject().put("field1", bA)
    val bA1: BsonArray = new BsonArray().add(1).add(2).add(9)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1)

    val boson: Boson = Boson.injector("field1.[end]", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj key1..[end]"){
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(6)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(3)
    val bsonA: BsonObject = new BsonObject().put("field1", bA)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(18)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(9)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1)

    val boson: Boson = Boson.injector("field1..[end]", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj .[end].key2"){
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(2).add(bOlvl2)
    //val bsonA: BsonObject = new BsonObject().put("field1", bA)

    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1: BsonArray = new BsonArray().add(1).add(2).add(bO1lvl2)
    //val bsonA1: BsonObject = new BsonObject().put("field1", bA1)

    val boson: Boson = Boson.injector(".[end].field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj .[end]..key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 6)
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(bOlvl3)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)

    val bO1lvl3: BsonObject = new BsonObject().put("field2", 6)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(bO1lvl3)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)

    val boson: Boson = Boson.injector(".[end]..field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj ..[end].key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 8)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bAlvl2: BsonArray = new BsonArray().add(2).add(3).add(bOlvl3)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA)

    val bO1lvl3: BsonObject = new BsonObject().put("field2", 24)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1lvl2: BsonArray = new BsonArray().add(2).add(3).add(bO1lvl3)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1)

    val boson: Boson = Boson.injector("..[end].field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj ..[end]..key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 6)
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(bOlvl3)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA)


    val bO1lvl3: BsonObject = new BsonObject().put("field2", 18)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(bO1lvl3)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1)

    val boson: Boson = Boson.injector("..[end]..field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj key1.[end].key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 8)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bAlvl2: BsonArray = new BsonArray().add(2).add(3).add(bOlvl3)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA).put("field3","someStuff")

    val bO1lvl3: BsonObject = new BsonObject().put("field2", 8)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1lvl2: BsonArray = new BsonArray().add(2).add(3).add(bO1lvl3)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1).put("field3","someStuff")

    val boson: Boson = Boson.injector(".field1.[end].field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj key1.[end]..key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 6)
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(bOlvl3)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA).put("field3","someStuff")


    val bO1lvl3: BsonObject = new BsonObject().put("field2", 6)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(bO1lvl3)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1).put("field3","someStuff")

    val boson: Boson = Boson.injector(".field1.[end]..field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj key1..[end].key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 8)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bAlvl2: BsonArray = new BsonArray().add(2).add(3).add(bOlvl3)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA).put("field3","someStuff")

    val bO1lvl3: BsonObject = new BsonObject().put("field2", 24)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1lvl2: BsonArray = new BsonArray().add(2).add(3).add(bO1lvl3)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1).put("field3","someStuff")

    val boson: Boson = Boson.injector(".field1..[end].field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }

  test("Inj key1..[end]..key2"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 6)
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(bOlvl3)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA).put("field3","someStuff")


    val bO1lvl3: BsonObject = new BsonObject().put("field2", 18)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(bO1lvl3)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 21)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1).put("field3","someStuff")

    val boson: Boson = Boson.injector(".field1..[end]..field2", (x: Int) => x*3 )

    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }


}
