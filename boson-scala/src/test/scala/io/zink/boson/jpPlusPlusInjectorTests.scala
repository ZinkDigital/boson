package io.zink.boson

import java.time.Instant
import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import com.fasterxml.jackson.core.JsonFactory
import io.zink.boson.bson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.ArrExpr
import io.zink.boson.bson.bsonValue.BsValue
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector
import mapper.Mapper
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
  private val sEditions2 = new BsonArray().add(edition2).add(5L).add(true)
  private val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
  private val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  private val sEditions1 = new BsonArray().add(edition1)
  private val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
  private val books = new BsonArray().add(title1).add(title2).add(title3)
  private val store = new BsonObject().put("Book", books).put("Hat", hats)
  private val bson = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    val edition1x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    //val edition1x: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsoneventx: BsonObject = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

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
    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Hat", "Cap"))
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
    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("ten", 10))
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
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = rootx.encodeToBarray()

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
    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("ten", 10))
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
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = rootx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Inj ..Books[0 until end]"){

    val book4: BsonObject = new BsonObject().put("Title", "C")
    val book3: BsonObject = new BsonObject().put("Title", "C++")
    val book2: BsonObject = new BsonObject().put("Title", "Java")
    val book1: BsonObject = new BsonObject().put("Title", "Scala")
    val books2: BsonArray = new BsonArray().add(book3).add(book4).add(book3)
    val books1: BsonArray = new BsonArray().add(book1).add(book2).add(book3)
    val sector2: BsonObject = new BsonObject().put("Books", books2)
    val sector1: BsonObject = new BsonObject().put("Books", books1)
    val sectors: BsonArray = new BsonArray().add(sector1).add(sector2)
    val root: BsonObject = new BsonObject().put("Library", sectors)

    val expression: String =  "..[1 until end]"
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
    val injFuture: CompletableFuture[Array[Byte]] = bosonI.go(root.encodeToBarray())

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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)
    val bson: BsonObject = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

    val expression: String = "."
    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Another", "field"))
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
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
    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
      val newL: List[Any] = l.:+("Store3ComingSoon")
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
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
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
    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
      val newM: Map[String, Any] = m.+(("Store3ComingSoon", "VerySoon"))
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
  test("Inj key1.[#]"){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 6)
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(bOlvl3)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA).put("field3","someStuff")


    val bO1lvl3: BsonObject = new BsonObject().put("field2", 6)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(bO1lvl3).add(6)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1).put("field3","someStuff")

    val boson: Boson = Boson.injector(".field1.[1]",(x: Array[Byte]) => {
      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
      val newL: List[Any] = l.:+(6)
      val res: ByteBuf = Mapper.encode(newL)
      if(res.hasArray)
        res.array()
      else {
        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
        val array: Array[Byte] = buf.array()
        buf.release()
        array
      }
    } )


    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }
  test("Inj ."){
    val bOlvl3: BsonObject = new BsonObject().put("field2", 6)
    val bAlvl2: BsonArray = new BsonArray().add(4).add(5).add(bOlvl3)
    val bOlvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA: BsonArray = new BsonArray().add(1).add(bAlvl2).add(bOlvl2)
    val bsonA: BsonObject = new BsonObject().put("field1", bA).put("field3","someStuff")


    val bO1lvl3: BsonObject = new BsonObject().put("field2", 6)
    val bA1lvl2: BsonArray = new BsonArray().add(4).add(5).add(bO1lvl3)
    val bO1lvl2: BsonObject = new BsonObject().put("field2", 7)
    val bA1: BsonArray = new BsonArray().add(1).add(bA1lvl2).add(bO1lvl2)
    val bsonA1: BsonObject = new BsonObject().put("field1", bA1).put("field3","someStuff")

    val boson: Boson = Boson.injector(".",(x:Int)=> 10)


    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(bsonA.encodeToBarray())

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(bsonA1.encodeToBarray().length === bosonRes.join().length)
    assertTrue(bsonA1.encodeToBarray().zip(bosonRes.join()).forall(db => db._1.equals(db._2)))
  }
  test("match key but ain´t array ."){
    val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2: BsonObject  = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1: BsonObject  = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3: BsonObject  = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject  = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject  = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject  = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject  = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject  = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject  = new BsonObject().put("Book", books).put("Hat", hats)
    val bson: BsonObject  = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

    val bsonEvent: BsonObject = bson
    val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

    //-------------------------------------------------------------
    val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2x: BsonObject  = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1x: BsonObject  = new BsonObject().put("Price", 48).put("Color", "Red")
    val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
    val edition3x: BsonObject  = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3x: BsonArray = new BsonArray().add(edition3x)
    val title3x: BsonObject  = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
    val edition2x: BsonObject  = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject  = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    val edition1x: BsonObject  = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject  = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject  = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsonx: BsonObject  = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)

    val bsonEventx: BsonObject = bsonx
    val validatedByteArrx: Array[Byte] = bsonEventx.encodeToBarray()


    val expression: String = ".Store[@Book]"

    val boson: Boson = Boson.injector(expression,(x: Array[Byte])=> x.reverse)


    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(validatedByteArrx.length === bosonRes.join().length)
    assertTrue(validatedByteArrx.zip(bosonRes.join()).forall(db => db._1.equals(db._2)))


  }
  test("match key but ain´t array .."){
    val hat3: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2: BsonObject  = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1: BsonObject  = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats: BsonArray = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3: BsonObject  = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject  = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject  = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2).add(5L).add(true)
    val title2: BsonObject  = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1: BsonObject  = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject  = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject  = new BsonObject().put("Book", books).put("Hat", hats)
    val bson: BsonObject  = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)

    val bsonEvent: BsonObject = bson
    val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

    //-------------------------------------------------------------
    val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2x: BsonObject  = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1x: BsonObject  = new BsonObject().put("Price", 48).put("Color", "Red")
    val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
    val edition3x: BsonObject  = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3x: BsonArray = new BsonArray().add(edition3x)
    val title3x: BsonObject  = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3x)
    val edition2x: BsonObject  = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2x: BsonArray = new BsonArray().add(edition2x).add(5L).add(true)
    val title2x: BsonObject  = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2x)
    val edition1x: BsonObject  = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1x: BsonArray = new BsonArray().add(edition1x)
    val title1x: BsonObject  = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1x)
    val booksx: BsonArray = new BsonArray().add(title1x).add(title2x).add(title3x)
    val storex: BsonObject  = new BsonObject().put("Book", booksx).put("Hat", hatsx)
    val bsonx: BsonObject  = new BsonObject().put("Store", storex).put("tables#", 5L).put("buy?", true)

    val bsonEventx: BsonObject = bsonx
    val validatedByteArrx: Array[Byte] = bsonEventx.encodeToBarray()


    val expression: String = "..Store[@Book]"

    val boson: Boson = Boson.injector(expression,(x: Array[Byte])=> x.reverse)


    val bosonRes: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    //bosonRes.join().foreach(b => println(s"[${b.toChar}|${b.toInt}]"))

    assert(validatedByteArrx.length === bosonRes.join().length)
    assertTrue(validatedByteArrx.zip(bosonRes.join()).forall(db => db._1.equals(db._2)))


  }
  test("testing * V1") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala!!!")
    val store1x: BsonObject = new BsonObject().put("Store",store3x )
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x )
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 )
    val bsonevent: BsonObject = new BsonObject().put("Store", store1)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".*re.St*.*o*K"
    val boson: Boson = Boson.injector(expression, (x:String) => x.concat("!!!") )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("testing * V2") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala!!!")
    val store1x: BsonObject = new BsonObject().put("Store",store3x )
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x )
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 )
    val bsonevent: BsonObject = new BsonObject().put("Store", store1)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".St*e.S*re.BooK"
    val boson: Boson = Boson.injector(expression, (x:String) => x.concat("!!!") )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("testing * V3") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1x: BsonObject = new BsonObject().put("Store",store3x )
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x )
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 )
    val bsonevent: BsonObject = new BsonObject().put("Store", store1)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".Store.Store.*oo"
    val boson: Boson = Boson.injector(expression, (x:String) => x.concat("!!!") )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("testing * V4") {
    val store3x: BsonObject = new BsonObject()
    val store1x: BsonObject = new BsonObject().put("Store", store3x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", store1x).put("BooKBooK", "Scala!!!")
    //-----------------------------
    val store3: BsonObject = new BsonObject()
    val store1: BsonObject = new BsonObject().put("Store", store3)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("BooKBooK", "Scala")
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".*oo*B*K"
    val boson: Boson = Boson.injector(expression, (x: String) => x.concat("!!!"))
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("testing * V5") {
    val store3x: BsonObject = new BsonObject()
    val store1x: BsonObject = new BsonObject().put("Store",store3x )
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("BooKBooK", "Scala!!!")
    //-----------------------------
    val store3: BsonObject = new BsonObject()
    val store1: BsonObject = new BsonObject().put("Store",store3 )
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("BooKBooK", "Scala")
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".Boo*B*K"
    val boson: Boson = Boson.injector(expression, (x:String) => x.concat("!!!") )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V1") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala!!!")
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("Store", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x )
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("Store", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".St*e..S*re.BooK"
    val boson: Boson = Boson.injector(expression, (x:String) => x.concat("!!!") )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V2") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala!!!")
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("Store", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x )
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("Store", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".St*e.S*re.BooK"
    val boson: Boson = Boson.injector(expression, (x:String) => x.concat("!!!") )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V3 modifierAll Float ") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 16.0f)
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".other"
    val boson: Boson = Boson.injector(expression, (x:Float) => x+0.7f )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V4 modifierAll Double ") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 16.0)
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".other"
    val boson: Boson = Boson.injector(expression, (x:Double) => x+0.7 )
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V5 modifierAll String as Array ") {
    val store3x: BsonObject = new BsonObject().put("BooK", "Scala!!!")
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 15.3f)
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", "Scala")
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..BooK"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => {
      val str: String = new String(x)
      println(str)
      val result: String = str.concat("!!!")
      println(result)
      result.getBytes()
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V6 modifierAll Instant ") {
    val instant: Instant = Instant.now()
    val store3x: BsonObject = new BsonObject().put("BooK", instant.plusMillis(1000))
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 15.3f)
    //-----------------------------
    val store3: BsonObject = new BsonObject().put("BooK", instant)
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..BooK"
    val boson: Boson = Boson.injector(expression, (x:Instant) => {
      x.plusMillis(1000)
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V7 modifierAll BsonArray ") {
    val arrayx: BsonArray = new BsonArray().add(1L).add(2L).add(3L)
    val store3x: BsonObject = new BsonObject().put("BooK", arrayx)
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 15.3f)
    //-----------------------------
    val array: BsonArray = new BsonArray().add(1L).add(2L)
    val store3: BsonObject = new BsonObject().put("BooK",  array)
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..BooK"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => {
      val bson: BosonImpl = new BosonImpl(byteArray = Option(x))
      val list: List[Any] = Mapper.decodeBsonArray(bson.getByteBuf)
      val newList: List[Any] = list.:+(3L)
      val buf: ByteBuf = Mapper.encode(newList)
      if(buf.hasArray)
        buf.array()
      else{
        Unpooled.buffer(buf.capacity()).writeBytes(buf).array()
      }
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V8 modifierAll Long ") {
    val arrayx: BsonArray = new BsonArray().add(1L).add(2L)
    val store3x: BsonObject = new BsonObject().put("BooK", arrayx)
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 10L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 15.3f)
    //-----------------------------
    val array: BsonArray = new BsonArray().add(1L).add(2L)
    val store3: BsonObject = new BsonObject().put("BooK",  array)
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".Store.number"
    val boson: Boson = Boson.injector(expression, (x:Long) => {
     x+5L
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V9 modifierAll Boolean ") {
    val arrayx: BsonArray = new BsonArray().add(1L).add(2L)
    val store3x: BsonObject = new BsonObject().put("BooK", arrayx)
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", false)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 15.3f)
    //-----------------------------
    val array: BsonArray = new BsonArray().add(1L).add(2L)
    val store3: BsonObject = new BsonObject().put("BooK",  array)
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".Store.isTrue?"
    val boson: Boson = Boson.injector(expression, (x:Boolean) => {
      !x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V10 modifierAll wrong type Double/String ") {
    val arrayx: BsonArray = new BsonArray().add(1L).add(2L)
    val store3x: BsonObject = new BsonObject().put("BooK", arrayx)
    val store1x: BsonObject = new BsonObject().put("Store",store3x ).put("number", 5L).put("isTrue?", true)
    val bsoneventx: BsonObject = new BsonObject().put("Store",store1x ).put("other", 15.3f)
    //-----------------------------
    val array: BsonArray = new BsonArray().add(1L).add(2L)
    val store3: BsonObject = new BsonObject().put("BooK",  array)
    val store1: BsonObject = new BsonObject().put("Store",store3 ).put("number", 5L).put("isTrue?", true)
    val bsonevent: BsonObject = new BsonObject().put("Store", store1).put("other", 15.3f)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = ".other"
    val boson: Boson = Boson.injector(expression, (x:String) => {
      x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V11 modifierEnd Double ") {
    val arrayBooksx: BsonArray = new BsonArray().add(13.0).add(14.0).add(15.0)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(12.0).add(13.0).add(14.0)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V12 modifierEnd Float ") {
    val arrayBooksx: BsonArray = new BsonArray().add(13.0f).add(14.0f).add(15.0f)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(12.0f).add(13.0f).add(14.0f)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Float) => {
      x+1.0f
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V13 modifierEnd Float/Double Exception ") {
    val arrayBooksx: BsonArray = new BsonArray().add("feff").add(13.0).add(14.0)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add("feff").add(13.0).add(14.0)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Float) => {
      x+1.0f
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V14 modifierEnd String as Array[Byte] ") {
    val arrayBooksx: BsonArray = new BsonArray().add("10").add("20").add("30")
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add("1").add("2").add("3")
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => {
      val str: String = new String(x)
      val res: String = str.concat("0")
      res.getBytes()
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V15 modifierEnd String as String ") {
    val arrayBooksx: BsonArray = new BsonArray().add("10").add("20").add("30")
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add("1").add("2").add("3")
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:String) => {
     x.concat("0")
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V16 modifierEnd String as Instant ") {
    val ins: Instant = Instant.now()

    val arrayBooksx: BsonArray = new BsonArray().add(ins.plusMillis(1000)).add(ins.plusMillis(1000)).add(ins.plusMillis(1000))
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(ins).add(ins).add(ins)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Instant) => {
      x.plusMillis(1000)
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V17 modifierEnd String/Array/Instant Exception") {

    val arrayBooksx: BsonArray = new BsonArray().add(2).add(3).add(4)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(2).add(3).add(4)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Instant) => {
      x.plusMillis(1000)
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V18 modifierEnd Boolean") {
    val arrayBooksx: BsonArray = new BsonArray().add(true).add(true).add(false)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(false).add(false).add(true)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Boolean) => {
      !x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V19 modifierEnd Boolean Exception") {
    val arrayBooksx: BsonArray = new BsonArray().add(false).add(false).add(true)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(false).add(false).add(true)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()

    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V20 modifierEnd Long ") {
    val arrayBooksx: BsonArray = new BsonArray().add(2L).add(3L).add(4L)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(1L).add(2L).add(3L)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Long) => {
      x+1L
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V21 modifierEnd Long Exception"){
    val arrayBooksx: BsonArray = new BsonArray().add(1L).add(2L).add(3L)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(1L).add(2L).add(3L)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V22 modifierEnd BsonObject Exception"){
    val bson1x: BsonObject = new BsonObject()//.put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(bson1x).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(bson1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[all]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V23 modifierEnd BsonObject Exception"){
    val bson1x: BsonObject = new BsonObject()//.put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(bson1x).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(bson1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[all]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V24 modifierEnd Int"){
    val bson1x: BsonObject = new BsonObject().put("cenas", 16)
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(10).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(10).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[all].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V25 modifierEnd BsonObject Exception"){
    val bson1x: BsonObject = new BsonObject()//.put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(bson1x).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(bson1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[end]"
    val boson: Boson = Boson.injector(expression, (x:String) => {
      x.concat("!!")
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V26 modifierEnd Int Exception"){
    val bson1x: BsonObject = new BsonObject()//.put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[end]"
    val boson: Boson = Boson.injector(expression, (x:String) => {
      x.concat("!!")
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V27 modifierEnd Int Exception"){
    val bson1x: BsonObject = new BsonObject()//.put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[end]"
    val boson: Boson = Boson.injector(expression, (x:String) => {
      x.concat("!!")
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V28 modifyArrayEnd Int "){
    val bson1x: BsonObject = new BsonObject()
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(2)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[1 to end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V29 modifyArrayEnd Int "){
    val bson1x: BsonObject = new BsonObject()
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(2)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[1 to end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V30 modifyArrayEnd BsonObject "){
    val bson1x: BsonObject = new BsonObject()
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(bson1x).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(bson1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[1 to 2]"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V31 modifyArrayEnd Int "){
    val bson1x: BsonObject = new BsonObject()
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[1 to 2]"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V32 modifyArrayEnd Int "){
    val bson1x: BsonObject = new BsonObject()
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject()
    val arrayBooks: BsonArray = new BsonArray().add(1).add(1).add(1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[1 to 2]"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V33 modifyArrayEnd Int "){
    val bson1x: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(1).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[1 to 2].casa"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V34 modifyArrayEnd Int "){
    val bson1x: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(1).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[1 to 2].casa"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V35 modifyArrayEnd not an array "){
    val bson1x: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(1).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", bson1x)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", bson1)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[1 to 2].casa"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V36 modifyArrayEnd until / Exception "){
    val bson1x: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(bson1x).add(bson1x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("casa", "home")
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(bson1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books.[0 until 2]"
    val boson: Boson = Boson.injector(expression, (x:Double) => {
      x+1.0
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V37 modifyArrayEndWithKey until   Objects"){
    val bson1x: BsonObject = new BsonObject().put("cenas", 15).put("some", "stuff")
    val bson2x: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(bson1x).add(bson1x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson1).add(bson1).add(bson1)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Books[0 until end]"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => {
      val boson: BosonImpl = new BosonImpl(byteArray = Option(x))
      val map: Map[String, Any] = Mapper.decodeBsonObject(boson.getByteBuf)
      val map1: Map[String, Any] = map.+(("some", "stuff"))
      Mapper.encode(map1).array()
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V38 modifyArrayEndWithKey until   Int"){
    val bson1x: BsonObject = new BsonObject().put("cenas", 15).put("some", "stuff")
    val bson2x: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsoneventx: BsonObject = new BsonObject().put("Books", bson2x)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", bson1)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Books[0 until end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V39 modifyArrayEndWithKey until   Int") {
    val bson1x: BsonObject = new BsonObject().put("cenas", 15).put("some", "stuff")
    val bson2x: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsoneventx: BsonObject = new BsonObject().put("Books", bson2x)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", bson1)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[0 until end]"
    val boson: Boson = Boson.injector(expression, (x: Int) => {
      x + 1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("[" + p._1.toChar + "|" + p._2.toChar + "]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V40 modifyArrayEndWithKey until   Int key doesnt match"){
    val bson1x: BsonObject = new BsonObject().put("cenas", 15).put("some", "stuff")
    val bson2x: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsoneventx: BsonObject = new BsonObject().put("Boks", bson2x)
    //-----------------------------
    val bson1: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Boks", bson1)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[0 until end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V41 modifyArrayEndWithKey   until   Int "){

    val arrayBooksx: BsonArray = new BsonArray().add(2).add(3).add(3)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[0 until end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V41 modifyArrayEndWithKey   to   Int "){


    val arrayBooksx: BsonArray = new BsonArray().add(2).add(3).add(4)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[0 to end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    //bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V42 modifyArrayEndWithKey   to   Int "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 16)
    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Books[0 to end].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V43 modifyArrayEndWithKey   to   Int/Null/Boolean/Long "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", bson2x)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Books", bson2)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Books[0 to end].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V44 modifyArrayEndWithKey   to   Int/Null/Boolean/Long "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 16).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[0 to end].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V45 modifyArrayEndWithKey   until   Int/Null/Boolean/Long "){
    val bson2x: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[0 until end].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V46 modifyArrayEndWithKey   end    "){

      val bson2x: BsonObject = new BsonObject().put("cenas", 16).put("Lnumber", 4L).putNull("Null").put("bool", true)
      val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(bson2x)
      val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
      //-----------------------------
      val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
      val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(bson2)
      val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

      val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
      val expression: String = ".Books[end].cenas"
      val boson: Boson = Boson.injector(expression, (x:Int) => {
        x+1
      })
      val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
      val result: Array[Byte] = future.join()
      val bE: Array[Byte] = bsoneventx.encodeToBarray()
      ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
      assertTrue(bE.zip(result).forall(p => p._1 == p._2))
    }
  test("Coverage V47 modifyArrayEndWithKey   end    "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 16).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Books[end].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V48 modifyArrayEndWithKey   end    "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 16).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(4)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V49 modifyArrayEndWithKey   end    "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 16).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(1).add(2).add(4)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Books[end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V50 modifyArrayEndWithKey   all    "){

    val bson2x: BsonObject = new BsonObject().put("cenas", 16).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooksx: BsonArray = new BsonArray().add(2).add(3).add(4)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("cenas", 15).put("Lnumber", 4L).putNull("Null").put("bool", true)
    val arrayBooks: BsonArray = new BsonArray().add(1).add(2).add(3)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[all]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V51 modifyHasElem Boolean/Long "){

    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 16)
    val arrayBooksx: BsonArray = new BsonArray().add(1L).add(true).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Books", arrayBooksx)
    //-----------------------------
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1L).add(true).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[@cenas].cenas"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V52 "){

    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1L).add(true).add(bson2).addNull()
    val bsonevent: BsonObject = new BsonObject().put("Books", arrayBooks)
    //-----------------------------


    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Books[3]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsonevent.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V53 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1L).add(true).addNull().add(10).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1L).add(true).addNull().add(10).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[end].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V54 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(1L).add(true).addNull().add(10).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(1L).add(true).addNull().add(10).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Store[end].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V55 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[0 to end].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V56 "){
    val arrayBooks: BsonArray = new BsonArray().add(10).add(10)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val arrayBooksx: BsonArray = new BsonArray().add(10).add(10)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[0 to end].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V57 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Store[0 to end].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V58 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Store[0 to 1].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V59 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[0 to 1].bool"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V60 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V61 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Stor[end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V62 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", false).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[0 until 1].bool"
    val boson: Boson = Boson.injector(expression, (x:Boolean) => {
      !x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V63 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", false).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..[0 until 1].bool"
    val boson: Boson = Boson.injector(expression, (x:Boolean) => {
      !x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V64 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", 4L).putNull("Null").put("bool", false).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[first].bool"
    val boson: Boson = Boson.injector(expression, (x:Boolean) => {
      !x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V65 "){
    val bO: BsonObject = new BsonObject()
    val bA: BsonArray = new BsonArray()
    val bson2: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Store[@bool]"
    val boson: Boson = Boson.injector(expression, (x:Boolean) => {
      !x
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V66 "){
    val bO: BsonObject = new BsonObject()
    val bA: BsonArray = new BsonArray()
    val bson2: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(new BsonObject()).add(new BsonObject())
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Store[@bool]"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => new BsonObject().encodeToBarray())
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V67 "){
    val bO: BsonObject = new BsonObject()
    val bA: BsonArray = new BsonArray()
    val bson2: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".Store[@booeel]"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => new BsonObject().encodeToBarray())
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
    test("Coverage V68 "){
    val bO: BsonObject = new BsonObject()
    val bA: BsonArray = new BsonArray()
    val bson2: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val arrayBooksx: BsonArray = new BsonArray()
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".*ore"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => new BsonArray().encodeToBarray())
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V69"){
    val bO: BsonObject = new BsonObject()
    val bA: BsonArray = new BsonArray()
    val bson2: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val arrayBooksx: BsonArray = new BsonArray()
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".*"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => new BsonArray().encodeToBarray())
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V70"){
    val bO: BsonObject = new BsonObject()
    val bA: BsonArray = new BsonArray()
    val bson2: BsonObject = new BsonObject().put("Lnumber", bO).put("LnumberA", bA).putNull("Null").put("bool", true).put("cenas", 15)
    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks).putNull("null")

    //-----------------------------


    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = ".null"
    val boson: Boson = Boson.injector(expression, (x:Array[Byte]) => new BsonArray().encodeToBarray())
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsonevent.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
  test("Coverage V71 "){
    val bson2: BsonObject = new BsonObject().put("Lnumber",  new BsonObject()).putNull("Null").put("bool", new BsonArray()).put("cenas", 15)

    val arrayBooks: BsonArray = new BsonArray().add(bson2).add(bson2)
    val bsonevent: BsonObject = new BsonObject().put("Store", arrayBooks)
    //-----------------------------
    val bson2x: BsonObject = new BsonObject().put("Lnumber",  new BsonObject()).putNull("Null").put("bool", new BsonArray()).put("cenas", 15)

    val arrayBooksx: BsonArray = new BsonArray().add(bson2x).add(bson2x)
    val bsoneventx: BsonObject = new BsonObject().put("Store", arrayBooksx)

    val validatedByteArr: Array[Byte] = bsonevent.encodeToBarray()
    val expression: String = "..Stor[end]"
    val boson: Boson = Boson.injector(expression, (x:Int) => {
      x+1
    })
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)
    val result: Array[Byte] = future.join()
    val bE: Array[Byte] = bsoneventx.encodeToBarray()
    ////bE.zip(result).foreach(p => println("["+p._1.toChar+"|"+p._2.toChar+"]"))
    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }
}
