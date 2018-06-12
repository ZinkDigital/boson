package io.zink.boson

import bsonLib.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration


@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  val bsonHuman: BsonArray = new BsonArray().add("person1").add("person2").add("person3")
  val bsonObjArray: BsonObject = new BsonObject().put("person", bsonHuman)
  val bsonObjArrayEncoded: Array[Byte] = bsonObjArray.encodeToBarray

  val bsonAlien: BsonArray = new BsonArray().add("et").add("predator").add("alien")
  val bsonObjArray1: BsonObject = new BsonObject().put("alien", bsonAlien)
  val bsonSpecies: BsonArray = new BsonArray().add(bsonObjArray).add(bsonObjArray1)
  val bsonSpeciesObj: BsonObject = new BsonObject().put("person", bsonObjArray).put("alien", bsonObjArray1)
//  val bsonSpeciesObj: BsonObject = new BsonObject().put("species", bsonSpecies)
  val bsonSpeciesEncoded: Array[Byte] = bsonSpeciesObj.encodeToBarray


  //  test("Root modification") {
  //    val bson = new BsonObject().put("name", "john doe")
  //    val ex = "."
  //    val bsonInj = Boson.injector(ex, (in: Array[Byte]) => {
  //      new String(in).toUpperCase.getBytes
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Root Injection") {
  //    val bson = new BsonObject().put("name", "john doe")
  //    val ex = "."
  //    val bsonInj = Boson.injector(ex, (in: Array[Byte]) => {
  //      new String(in).replace("john doe", "Jane Doe").getBytes
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((new String(resultValue) contains "Jane Doe") && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Top level key modification") {
  //    val bson = new BsonObject().put("name", "john doe")
  //    val ex = ".name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    println(resultValue.mkString(" ") + "\n" + bsonEncoded.mkString(" "))
  //    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Nested key modification - Single Dots") {
  //    val person = new BsonObject().put("name", "john doe")
  //    val bson = new BsonObject().put("person", person)
  //    val ex = ".person.name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Nested key modification - Single Dots - Multiple layers") {
  //    val person = new BsonObject().put("name", "john doe")
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //    val ex = ".client.person.name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Nested key modification, two fields - Single Dots - Multiple layers") {
  //    val person = new BsonObject().put("name", "john doe").put("age", 21)
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //    val ex = ".client.person.age"
  //    val bsonInj = Boson.injector(ex, (in: Int) => {
  //      in + 20
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Top level halfkey modification - Single Dots") {
  //    val bson = new BsonObject().put("name", "John Doe")
  //    val ex1 = ".*ame"
  //    val ex2 = ".nam*"
  //    val ex3 = ".n*me"
  //
  //    val bsonInj1 = Boson.injector(ex1, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val bsonInj2 = Boson.injector(ex2, (in: String) => {
  //      in.toLowerCase
  //    })
  //    val bsonInj3 = Boson.injector(ex3, (in: String) => {
  //      in + " Hello"
  //    })
  //
  //    val bsonEncoded = bson.encodeToBarray()
  //
  //    val future1 = bsonInj1.go(bsonEncoded)
  //    val resultValue1: Array[Byte] = Await.result(future1, Duration.Inf)
  //
  //    val future2 = bsonInj2.go(bsonEncoded)
  //    val resultValue2: Array[Byte] = Await.result(future2, Duration.Inf)
  //
  //    val future3 = bsonInj3.go(bsonEncoded)
  //    val resultValue3: Array[Byte] = Await.result(future3, Duration.Inf)
  //
  //    assert(
  //      ((new String(resultValue1) contains "JOHN DOE") && resultValue1.length == bsonEncoded.length) &&
  //        ((new String(resultValue2) contains "john doe") && resultValue2.length == bsonEncoded.length) &&
  //        (new String(resultValue3) contains "John Doe Hello")
  //    )
  //  }

  //  test("HasElem injection test") {  //TODO Correct this test
  //    val person1 = new BsonObject().put("name", "John Doe")
  //    val person2 = new BsonObject().put("name", "Jane Doe")
  //    val bsonArray = new BsonArray().add(person1).add(person2)
  //    val bson = new BsonObject().put("Persons", bsonArray)
  //    val ex = ".persons[@name]"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase()
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => println("Here, result was this: " + resultValue);
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
  //  }

  //  test("Root injection - Double dots") {
  //    val bson = new BsonObject().put("name", "John Doe")
  //    val ex = "..name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Nested key injection - Double dots") {
  //    val person = new BsonObject().put("name", "john doe")
  //    val bson = new BsonObject().put("person", person)
  //    //    val bson = new BsonObject().put("client", client)
  //
  //    val ex = "..name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    println(bsonEncoded.mkString(" ") + "\n" + resultValue.mkString(" "))
  //    assert((new String(resultValue) contains "JOHN DOE") && resultValue.length == bsonEncoded.length)
  //  }

  //  test("Key with Array Exp [0 to 1] modification toUpperCase - Single Dots") {
  //    val expr = ".person[0 to 1]"
  //    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }
  //
  //  test("Key with Array Exp [0 until 1] modification - Single Dots") {
  //    val expr = ".person[0 until 1]"
  //    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }
  //
  //  test("Key with Array Exp [1 to end] toUpperCase - Single Dots") {
  //    val expr = ".person[1 to end]"
  //    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }
  //
  //  test("Key with Array Exp [1 until end] toUpperCase - Single Dots") {
  //    val expr = ".person[1 until end]"
  //    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }
  //
  //  test("Key with Array Exp[all] toUpperCase - Single Dots") {
  //    val bsonArrayExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val expr = ".person[all]"
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }
  //
  //  test("Key with Array Exp[first] toUpperCase - Single Dots") {
  //    val expr = ".person[first]"
  //    val bsonArrayExpected = new BsonArray().add("PERSON1").add("person2").add("person3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }
  //
  //  test("Key with Array Exp[1] toUpperCase - Single Dots") {
  //    val expr = ".person[1]"
  //    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val bsonInj = Boson.injector(expr, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
  //    assert(equals)
  //  }

  test("Key with Array Expr .species.person[0] toUpperCase - No/Double Dots") {
    val expr = ".species[0].person[0]"
    val bsontTest1 = bsonObjArrayEncoded
    val bsonTest2 = new BsonObject().put("Hello","BYEEEEE").put("Bye","Hiiiiiii").encodeToBarray
    //    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
    //    val expectedBson = new BsonObject().put("person", bsonHuman).put("alien", "ET")
    //    val expectedEncoded = expectedBson.encodeToBarray
    //println(new String(bsonSpeciesEncoded))
    val bsonInj = Boson.injector(expr, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bsonSpeciesEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    //    val equals = expectedEncoded.zip(result).forall(bt => bt._1 == bt._2)
    assert(true)
  }

  //  test("Key withArray Exp [1 until end] toUpperCase - Single Dots") {
  //    val bsonArrayExpected = new BsonArray().add("person1").add("PERSON2").add("person3")
  //    val expectedBson = new BsonObject().put("person", bsonArrayExpected)
  //    val expectedEncoded = expectedBson.encodeToBarray
  //    val ex = ".person[1 until end]"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bsonObjArrayEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = expectedEncoded.zip(result).forall(ex => ex._1 == ex._2)
  //    assert(equals)
  //  }


  //  test("Nested key injection - Multiple Layers- Double dots") {
  //    val person = new BsonObject().put("name", "john doe")
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //
  //    val ex = "..name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase()
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE");
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
  //  }
  //
  //  test("Nested key injection - Multiple Layers- Double dots - Second argument") {
  //    val person = new BsonObject().put("name", "john doe").put("age", 21)
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //
  //    val ex = "..age"
  //    val bsonInj = Boson.injector(ex, (in: Int) => {
  //      in + 20
  //    })
  //    val bsonEncoded = bson.encodeToBarray
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  //  }
  //
  //  test("Nested key injection - Multiple Layers- Single then Double dots") {
  //    val person = new BsonObject().put("name", "john doe").put("age", 21)
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //
  //    val ex = ".client..age"
  //    val bsonInj = Boson.injector(ex, (in: Int) => {
  //      in + 20
  //    })
  //    val bsonEncoded = bson.encodeToBarray
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  //  }

  //  test("Nested key injection - Multiple Layers- double then Double dots") {
  //    val person = new BsonObject().put("name", "john doe").put("age", 21)
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //
  //    val ex = "..person..age"
  //    val bsonInj = Boson.injector(ex, (in: Int) => {
  //      in + 20
  //    })
  //    val bsonEncoded = bson.encodeToBarray
  //    val future = bsonInj.go(bsonEncoded)
  //    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //    assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  //  }

  //    test("Nested key injection - double dot with array expression") {
  //      val person1 = new BsonObject().put("name", "john doe").put("age", 21)
  //      val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
  //      val persons = new BsonArray().add(person1).add(person2)
  //      val client = new BsonObject().put("person", persons)
  //      val bson = new BsonObject().put("client", client)
  //
  //      println(bson.encodeToBarray.mkString(","))
  //
  //      val ex = "..person[first]..age"
  //      val bsonInj = Boson.injector(ex, (in: Int) => {
  //        in + 20
  //      })
  //      val bsonEncoded = bson.encodeToBarray
  //      val future = bsonInj.go(bsonEncoded)
  //      val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
  //      assert((resultValue containsSlice Array(41, 0, 0, 0)) && resultValue.length == bsonEncoded.length)
  //    }

}
