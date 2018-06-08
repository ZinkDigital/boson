package io.zink.boson

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
    println(resultValue.mkString(" ") + "\n" + bsonEncoded.mkString(" "))
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

  test("Key with Array Exp [0 to 1] modification toUpperCase - Single Dots") {
    val bsonArray = new BsonArray().add("person1").add("person2").add("person3")
    val bson = new BsonObject().put("person", bsonArray)
    val ex = ".person[0 to 1]"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    val str = new String(resultValue)
    assert((str contains "PERSON1") && (str contains "PERSON2") && (resultValue.length == bsonEncoded.length))
  }

  test("Key with Array Exp [0 until 1] modification - Single Dots") { //TODO - On Stand By
    val bsonArray = new BsonArray().add("person1").add("person2").add("person3")
    val bson = new BsonObject().put("person", bsonArray)
    val expression = ".person[0 until 1]"
    val bsonInj = Boson.injector(expression, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bson.encodeToBarray())
    future onComplete {
      case Success(resultValue) =>
        println("After: " + resultValue.mkString(", "))
        val string = new String(resultValue)
        println(resultValue.size)
        assert((string contains "PERSON1") && (string contains "person2"))
      case Failure(e) =>
        println(e)
        fail
    }
    Await.result(future, Duration.Inf)
    println("Before: " + bson.encodeToBarray().mkString(", "))
  }

  test("Key withArray Exp [1 until end] toUpperCase - Single Dots") {
    val bsonArray = new BsonArray().add("person1").add("person2").add("person3")
    val bson = new BsonObject().put("person", bsonArray)
    val ex = ".person[1 until end]"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bson.encodeToBarray())
    future onComplete {
      case Success(resultValue) =>
        println(resultValue.mkString(", "))
        val string = new String(resultValue)
        assert((string contains "PERSON2") && (string contains "person3") && (resultValue.length == bson.encodeToBarray.length))
      case Failure(e) =>
        println(e)
        fail
    }
    Await.result(future, Duration.Inf)
    println(bson.encodeToBarray().mkString(", "))
  }

  test("Key with Array Exp[all] toUpperCase - Single Dots") {
    ???
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

}
