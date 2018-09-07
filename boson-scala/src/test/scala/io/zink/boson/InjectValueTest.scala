package io.zink.boson

import bsonLib.BsonObject
import bsonLib.BsonArray
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration


@RunWith(classOf[JUnitRunner])
class InjectValueTest extends FunSuite {

  //  test("CodecBson - Top level key inject String value") {
  //    val expected = new BsonObject().put("name", "Albertina").encodeToBarray()
  //    val bson = new BsonObject().put("name", "Albert")
  //    val ex = ".name"
  //    val jsonInj = Boson.injector(ex, "Albertina")
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level key inject Int value") {
  //    val expected = new BsonObject().put("age", 3).encodeToBarray()
  //    val bson = new BsonObject().put("age", 20)
  //    val ex = ".age"
  //    val jsonInj = Boson.injector(ex, 3)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level key inject String Nested value") {
  //    val expectedLayer1 = new BsonObject().put("age", 3)
  //    val expected = new BsonObject().put("person", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonObject().put("age", 20)
  //    val bson = new BsonObject().put("person", bsonLayer1)
  //    val ex = ".person.age"
  //    val jsonInj = Boson.injector(ex, 3)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject key[single] value") {
  //    val expectedLayer1 = new BsonArray().add(911).add(112)
  //    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonArray().add(100).add(112)
  //    val bson = new BsonObject().put("emergency", bsonLayer1)
  //    val ex = ".emergency[0]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject key[all] values") {
  //    val expectedLayer1 = new BsonArray().add(911).add(911).add(911).add(911).add(911)
  //    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val bson = new BsonObject().put("emergency", bsonLayer1)
  //    val ex = ".emergency[0 to end]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject Key[1 to 3] values") {
  //    val expectedLayer1 = new BsonArray().add(100).add(911).add(911).add(911).add(400)
  //    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val bson = new BsonObject().put("emergency", bsonLayer1)
  //    val ex = ".emergency[1 to 3]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject key[1 until 4] values") {
  //    val expectedLayer1 = new BsonArray().add(100).add(911).add(911).add(911).add(400)
  //    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val bson = new BsonObject().put("emergency", bsonLayer1)
  //    val ex = ".emergency[1 until 4]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject key[end] values") {
  //    val expectedLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(911)
  //    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val bson = new BsonObject().put("emergency", bsonLayer1)
  //    val ex = ".emergency[end]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject key[0 until end] values") {
  //    val expectedLayer1 = new BsonArray().add(911).add(911).add(911).add(911).add(400)
  //    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
  //    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val bson = new BsonObject().put("emergency", bsonLayer1)
  //    val ex = ".emergency[0 until end]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject [single] value") {
  //    val expected = new BsonArray().add(911).add(112).encodeToBarray()
  //    val bson = new BsonArray().add(100).add(112)
  //    val ex = ".[0]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject [all] values") {
  //    val expected = new BsonArray().add(911).add(911).add(911).add(911).add(911).encodeToBarray()
  //    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val ex = ".[0 to end]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject [1 to 3] values") {
  //    val expected = new BsonArray().add(100).add(911).add(911).add(911).add(400).encodeToBarray()
  //    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val ex = ".[1 to 3]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject [1 until 4] values") {
  //    val expected = new BsonArray().add(100).add(911).add(911).add(911).add(400).encodeToBarray()
  //    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val ex = ".[1 until 4]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject [end] values") {
  //    val expected = new BsonArray().add(100).add(112).add(200).add(300).add(911).encodeToBarray()
  //    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val ex = ".[end]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject [0 until end] values") {
  //    val expected = new BsonArray().add(911).add(911).add(911).add(911).add(400).encodeToBarray()
  //    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
  //    val ex = ".[0 until end]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //    assert(equals)
  //  }
  //
  //  test("Nested key injection - .person[0].age") {
  //    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
  //    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
  //    val persons = new BsonArray().add(person1).add(person2)
  //    val bson = new BsonObject().put("person", persons)
  //    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
  //    val personsExpected = new BsonArray().add(person1Expected).add(person2)
  //    val bsonExpected = new BsonObject().put("person", personsExpected).encodeToBarray
  //    val ex = ".person[0].age"
  //    val bsonInj = Boson.injector(ex, 20)
  //    val future = bsonInj.go(bson.encodeToBarray)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //
  //    assertArrayEquals(result, bsonExpected)
  //  }
  //
  //  test("CodecBson - HasElem injection test") {
  //    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
  //    val person2Ex = new BsonObject().put("name", "Something")
  //    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
  //    val expected = new BsonObject().put("persons", bsonArrayEx).encodeToBarray()
  //    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
  //    val person2 = new BsonObject().put("name", "Jane Doe")
  //    val bsonArray = new BsonArray().add(person1).add(person2)
  //    val bson = new BsonObject().put("persons", bsonArray)
  //    val ex = ".persons[@name]"
  //    val bsonInj = Boson.injector(ex, "Something")
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - HasElem DOUBLE_DOTS injection test") {
  //    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
  //    val person2Ex = new BsonObject().put("name", "Something")
  //    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
  //    val expectedLayer = new BsonObject().put("persons", bsonArrayEx)
  //    val expected = new BsonObject().put("layer1", expectedLayer).encodeToBarray()
  //    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
  //    val person2 = new BsonObject().put("name", "Jane Doe")
  //    val bsonArray = new BsonArray().add(person1).add(person2)
  //    val bsonLayer = new BsonObject().put("persons", bsonArray)
  //    val bson = new BsonObject().put("layer1", bsonLayer)
  //    val ex = "..persons[@name]"
  //    val bsonInj = Boson.injector(ex, "Something")
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - HasElem injection test - Middle object not applicable") {
  //    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
  //    val person2Ex = new BsonObject().put("name", "Something")
  //    val person3Ex = new BsonObject().put("person", "Helloo")
  //    val bsonArrayEx = new BsonArray().add(person1Ex).add(person3Ex).add(person2Ex)
  //    val expected = new BsonObject().put("persons", bsonArrayEx).encodeToBarray()
  //    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
  //    val person2 = new BsonObject().put("name", "Jane Doe")
  //    val bsonArray = new BsonArray().add(person1).add(person3Ex).add(person2)
  //    val bson = new BsonObject().put("persons", bsonArray)
  //    val ex = ".persons[@name]"
  //    val bsonInj = Boson.injector(ex, "Something")
  //    val bsonEncoded = bson.encodeToBarray()
  //    val future = bsonInj.go(bsonEncoded)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //
  //    assert(equals)
  //  }
  //
  //  test("CodecBson - Top level array inject DOUBLE_DOTS key[single] value") {
  //    val expectedLayer2 = new BsonArray().add(911).add(112)
  //    val expectedLayer1 = new BsonObject().put("emergency", expectedLayer2)
  //    val expected = new BsonObject().put("another", expectedLayer1).encodeToBarray
  //    val bsonLayer2 = new BsonArray().add(100).add(112)
  //    val bsonLayer1 = new BsonObject().put("emergency", bsonLayer2)
  //    val bson = new BsonObject().put("another", bsonLayer1)
  //    val ex = "..emergency[0]"
  //    val jsonInj = Boson.injector(ex, 911)
  //    val jsonEncoded = bson.encodeToBarray()
  //    val future = jsonInj.go(jsonEncoded)
  //    val result = Await.result(future, Duration.Inf)
  //    val equals = result.zip(expected).forall(b => b._1 == b._2)
  //
  //    println("Exp: " + expected.mkString(", "))
  //    println("Res: " + result.mkString(", "))
  //
  //    assert(equals)
  //  }
  //
  //  test("Nested key injection - ..person.age") {
  //    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
  //    val bson = new BsonObject().put("person", person1)
  //    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
  //    val bsonExpected = new BsonObject().put("person", person1Expected).encodeToBarray
  //    val ex = "..person.age"
  //    val bsonInj = Boson.injector(ex, 20)
  //    val future = bsonInj.go(bson.encodeToBarray)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //
  //    assertArrayEquals(result, bsonExpected)
  //  }
  //
  //  test("Nested key injection - ..age") {
  //    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
  //    val bson = new BsonObject().put("person", person1)
  //    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
  //    val bsonExpected = new BsonObject().put("person", person1Expected).encodeToBarray
  //    val ex = "..age"
  //    val bsonInj = Boson.injector(ex, 20)
  //    val future = bsonInj.go(bson.encodeToBarray)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //
  //    assertArrayEquals(result, bsonExpected)
  //  }
  //
  //  test("Nested key injection - ..person..age") {
  //    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
  //    val bson = new BsonObject().put("person", person1)
  //    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
  //    val bsonExpected = new BsonObject().put("person", person1Expected).encodeToBarray
  //    val ex = "..person..age"
  //    val bsonInj = Boson.injector(ex, 20)
  //    val future = bsonInj.go(bson.encodeToBarray)
  //    val result: Array[Byte] = Await.result(future, Duration.Inf)
  //
  //    assertArrayEquals(result, bsonExpected)
  //  }

  test("CodecJson - Top level key inject String value") {
    val expected = new BsonObject().put("name", "Albertina").encodeToString
    val bson = new BsonObject().put("name", "Albert")
    val ex = ".name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
//    val equals = result.zip(expected).forall(b => b._1 == b._2)
    println(result)
    assert(result.equals(expected))
  }
}
