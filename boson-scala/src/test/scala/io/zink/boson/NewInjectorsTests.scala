package io.zink.boson

import java.time.Instant

import bsonLib.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  case class Book(name: String, pages: Int)

  val bsonHuman: BsonArray = new BsonArray().add("person1").add("person2").add("person3")
  val bsonObjArray: BsonObject = new BsonObject().put("person", bsonHuman)
  val bsonObjArrayEncoded: Array[Byte] = bsonObjArray.encodeToBarray

  val bsonAlien: BsonArray = new BsonArray().add("et").add("predator").add("alien")
  val bsonObjArray1: BsonObject = new BsonObject().put("alien", bsonAlien)

  val bsonEvent: BsonObject = new BsonObject().put("person", bsonHuman).put("alien", bsonAlien)
  val bsonSpeciesObj: BsonObject = new BsonObject().put("species", bsonEvent)
  val bsonSpeciesEncoded: Array[Byte] = bsonSpeciesObj.encodeToBarray


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
    val bson = new BsonObject().put("name", "john doe")
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
    val person = new BsonObject().put("name", "john doe")
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)
    val ex = ".client.person.name"
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
    val person1 = new BsonObject().put("name", "John Doe")
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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
    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(equals)
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

  test("Nested key injection - Multiple Layers- Double dots") {
    val person = new BsonObject().put("name", "john doe")
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)

    val ex = "..name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase()
    })
    val future = bsonInj.go(bson.encodeToBarray())
    future onComplete {
      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE");
      case Failure(e) => println(e); fail
    }
    Await.result(future, Duration.Inf)
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
    assert(resultValue.zip(expectedBson.encodeToBarray()).forall(bt => bt._1 == bt._2))
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
    assert(resultValue.zip(expectedBson.encodeToBarray()).forall(bt => bt._1 == bt._2))
  }

//  test("Root injection - Double dots case class") {
//    val bson = new BsonObject().put("book", Book("Title", 1))
//    val ex = "..name"
//    val bsonInj = Boson.injector(ex, (in: Book) => {
//      println("inside: " + in.name)
//      println("inside: " + in.pages)
//      Book("LOTR", 320)
//    })
//    val bsonEncoded = bson.encodeToBarray()
//    val future = bsonInj.go(bsonEncoded)
//    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
//
//  }
}
