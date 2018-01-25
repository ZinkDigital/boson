package io.boson

import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.BsValue
import io.netty.buffer.Unpooled
import org.scalatest.FunSuite
import org.junit.Assert._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
/**
  * Created by Ricardo Martins on 23/01/2018.
  */
@RunWith(classOf[JUnitRunner])
class jpPlusPlusInjectorTests extends FunSuite {

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


  test(".Store.Book..0.Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book..0.Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
   // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store..Book.0.Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = ".Store..Book.0.Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store.Book.0..Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book.0..Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store.Book..0..Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book..0..Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store..Book..0..Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = ".Store..Book..0..Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test("Store..Book..0..Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = "Store..Book..0..Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store.Book.[@Price].Title"){
    val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    val sEditions3 = new BsonArray().add(edition3)
    val title3 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2 = new BsonArray().add(edition2)
    val title2 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1 = new BsonArray().add(edition1)
    val title1 = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books).put("Hat", hats)
    val bsonevent = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book.[@Price].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store..Book.[@Price].Title"){
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
    val edition1:BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store..Book.[@Price].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store..Book.[@Price]..Title"){
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
    val edition1:BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store..Book.[@Price]..Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test("Book.[@Price].Title"){
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
    val edition1:BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = "Book.[@Price].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store.Book.[0].Title"){
    //val edition4x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    //val lastBooksx: BsonArray = new BsonArray().add(edition4x)
    val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
    val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)//.put("Book", lastBooksx)
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
    val edition3: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)//.put("Book", lastBooks)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1:BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book.[0].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store.Book..[0].Title"){
    //val edition4x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    //val lastBooksx: BsonArray = new BsonArray().add(edition4x)
    val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
    val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)//.put("Book", lastBooksx)
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
    val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)//.put("Book", lastBooks)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1:BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book..[0].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test("..[0].Title"){
    //val edition4x: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    //val lastBooksx: BsonArray = new BsonArray().add(edition4x)
    val hat3x: BsonObject = new BsonObject().put("Price", 38).put("Color", "Blue")
    val hat2x: BsonObject = new BsonObject().put("Price", 35).put("Color", "White")
    val hat1x: BsonObject = new BsonObject().put("Price", 48).put("Color", "Red")
    val hatsx: BsonArray = new BsonArray().add(hat1x).add(hat2x).add(hat3x)
    val edition3x: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)//.put("Book", lastBooksx)
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
    val edition3: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 38)//.put("Book", lastBooks)
    val sEditions3: BsonArray = new BsonArray().add(edition3)
    val title3: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    val edition2: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 40)
    val sEditions2: BsonArray = new BsonArray().add(edition2)
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1:BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = "..[0].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store..Book.[0 to 1].Title"){
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
    val edition1:BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store..Book.[0 to 1].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store.Book.[0 to 1].Title"){
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
    val edition1:BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store.Book.[0 to 1].Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  test(".Store..Book.[0 to 1]..Title"){
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
    val edition1:BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "SUPERJAVA").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = ".Store..Book.[0 to 1]..Title"
    val boson: Boson = Boson.injector(expression, (x: String)=> "SUPERJAVA")
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }

  /*test("."){
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
    val title2: BsonObject = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
    val edition1:BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    val sEditions1: BsonArray = new BsonArray().add(edition1)
    val title1: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    val books: BsonArray = new BsonArray().add(title1).add(title2).add(title3)
    val store: BsonObject = new BsonObject().put("Book", books).put("Hat", hats).put("String",12)

    /**/
    val bsonevent: BsonObject = new BsonObject().put("Store", store)

    val expression:String = "."
    val boson: Boson = Boson.injector(expression, (x: Map[String, Any])=> x.+(("String", 12)))
    val future: CompletableFuture[Array[Byte]] = boson.go(validatedByteArr)

    val result: Array[Byte] = future.join()
    println()
    result.foreach(b => println("final buffer "+ b.toChar + "  " + b.toInt))

    val bE: Array[Byte] = bsonevent.encodeToBarray()
    // assertEquals(1,1)

    println(s"Sizes: object=${bE.length}  result=${result.length}")
    println(bE.zip(result).forall(p => p._1 == p._2))

    assertTrue(bE.zip(result).forall(p => p._1 == p._2))
  }*/
}
