package io.zink.boson

import bsonLib.BsonObject
import bsonLib.BsonArray
import io.vertx.core.json.JsonObject
import org.junit.Assert.assertArrayEquals
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source


@RunWith(classOf[JUnitRunner])
class InjectValueTest extends FunSuite {

  test("CodecBson - Top level key inject String value") {
    val expected = new BsonObject().put("name", "Albertina").encodeToBarray
    val expectedJson = new BsonObject().put("name", "Albertina").encodeToString
    val bson = new BsonObject().put("name", "Albert")
    val ex = ".name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }


  test("CodecBson - Top level key inject Int value") {
    val expected = new BsonObject().put("age", 3).encodeToBarray()
    val bson = new BsonObject().put("age", 20)
    val ex = ".age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level key inject String Nested value") {
    val expectedLayer1 = new BsonObject().put("age", 3)
    val expected = new BsonObject().put("person", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonObject().put("age", 20)
    val bson = new BsonObject().put("person", bsonLayer1)
    val ex = ".person.age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject key[single] value") {
    val expectedLayer1 = new BsonArray().add(911).add(112)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[0]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject key[all] values") {
    val expectedLayer1 = new BsonArray().add(911).add(911).add(911).add(911).add(911)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[0 to end]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject Key[1 to 3] values") {
    val expectedLayer1 = new BsonArray().add(100).add(911).add(911).add(911).add(400)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[1 to 3]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject key[1 until 4] values") {
    val expectedLayer1 = new BsonArray().add(100).add(911).add(911).add(911).add(400)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[1 until 4]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject key[end] values") {
    val expectedLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(911)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[end]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject key[0 until end] values") {
    val expectedLayer1 = new BsonArray().add(911).add(911).add(911).add(911).add(400)
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToBarray()
    val bsonLayer1 = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[0 until end]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject [single] value") {
    val expected = new BsonArray().add(911).add(112).encodeToBarray()
    val bson = new BsonArray().add(100).add(112)
    val ex = ".[0]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject [all] values") {
    val expected = new BsonArray().add(911).add(911).add(911).add(911).add(911).encodeToBarray()
    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val ex = ".[0 to end]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject [1 to 3] values") {
    val expected = new BsonArray().add(100).add(911).add(911).add(911).add(400).encodeToBarray()
    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val ex = ".[1 to 3]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject [1 until 4] values") {
    val expected = new BsonArray().add(100).add(911).add(911).add(911).add(400).encodeToBarray()
    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val ex = ".[1 until 4]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject [end] values") {
    val expected = new BsonArray().add(100).add(112).add(200).add(300).add(911).encodeToBarray()
    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val ex = ".[end]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject [0 until end] values") {
    val expected = new BsonArray().add(911).add(911).add(911).add(911).add(400).encodeToBarray()
    val bson = new BsonArray().add(100).add(112).add(200).add(300).add(400)
    val ex = ".[0 until end]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("Nested key injection - .person[0].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("person", persons)
    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
    val personsExpected = new BsonArray().add(person1Expected).add(person2)
    val bsonExpected = new BsonObject().put("person", personsExpected).encodeToBarray
    val ex = ".person[0].age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonExpected)
  }

  test("CodecBson - HasElem injection test") {
    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
    val person2Ex = new BsonObject().put("name", "Something")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expected = new BsonObject().put("persons", bsonArrayEx).encodeToBarray()
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@name]"
    val bsonInj = Boson.injector(ex, "Something")
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - HasElem DOUBLE_DOTS injection test") {
    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
    val person2Ex = new BsonObject().put("name", "Something")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expectedLayer = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("layer1", expectedLayer).encodeToBarray()
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bsonLayer = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("layer1", bsonLayer)
    val ex = "..persons[@name]"
    val bsonInj = Boson.injector(ex, "Something")
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - HasElem injection test - Middle object not applicable") {
    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
    val person2Ex = new BsonObject().put("name", "Something")
    val person3Ex = new BsonObject().put("person", "Helloo")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person3Ex).add(person2Ex)
    val expected = new BsonObject().put("persons", bsonArrayEx).encodeToBarray()
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person3Ex).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@name]"
    val bsonInj = Boson.injector(ex, "Something")
    val bsonEncoded = bson.encodeToBarray()
    val future = bsonInj.go(bsonEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Top level array inject DOUBLE_DOTS key[single] value") {
    val expectedLayer2 = new BsonArray().add(911).add(112)
    val expectedLayer1 = new BsonObject().put("emergency", expectedLayer2)
    val expected = new BsonObject().put("another", expectedLayer1).encodeToBarray
    val bsonLayer2 = new BsonArray().add(100).add(112)
    val bsonLayer1 = new BsonObject().put("emergency", bsonLayer2)
    val bson = new BsonObject().put("another", bsonLayer1)
    val ex = "..emergency[0]"
    val jsonInj = Boson.injector(ex, 911)
    val jsonEncoded = bson.encodeToBarray()
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    val equals = result.zip(expected).forall(b => b._1 == b._2)
    assert(equals)
  }

  test("CodecBson - Nested key injection - ..person.age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val bson = new BsonObject().put("person", person1)
    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
    val bsonExpected = new BsonObject().put("person", person1Expected).encodeToBarray
    val ex = "..person.age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonExpected)
  }

  test("CodecBson - Nested key injection - ..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val bson = new BsonObject().put("person", person1)
    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
    val bsonExpected = new BsonObject().put("person", person1Expected).encodeToBarray
    val ex = "..age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonExpected)
  }

  test("CodecBson - Nested key injection - ..person..age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val bson = new BsonObject().put("person", person1)
    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
    val bsonExpected = new BsonObject().put("person", person1Expected).encodeToBarray
    val ex = "..person..age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToBarray)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, bsonExpected)
  }

  test("CodecBson - Nested HasElem injection test") {
    val person1LayerEx = new BsonObject().put("age", 20)
    val person1Ex = new BsonObject().put("name", "john doe").put("some", person1LayerEx)
    val person2Ex = new BsonObject().put("name", "jane doe")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expectedLayer1 = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("some", expectedLayer1).encodeToBarray
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bsonLayer1 = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("some", bsonLayer1)
    val ex = ".some.persons[@some].age"
    val bsonInj = Boson.injector(ex, 20)
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expected)
  }

  test("CodecBson - Nested HasElem DOUBLE_DOTS injection test") {
    val person1LayerEx = new BsonObject().put("age", 20)
    val person1Ex = new BsonObject().put("name", "john doe").put("some", person1LayerEx)
    val person2Ex = new BsonObject().put("name", "jane doe").put("some", person1LayerEx)
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expectedLayer1 = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("some", expectedLayer1).encodeToBarray
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe").put("some", person1Layer)
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bsonLayer1 = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("some", bsonLayer1)
    val ex = "..persons[@some].age"
    val bsonInj = Boson.injector(ex, 20)
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expected)
  }

  test("CodecBson - Nested HasElem injection test - Middle object not applicable") {
    val person1LayerEx = new BsonObject().put("age", 20)
    val person1Ex = new BsonObject().put("name", "john doe").put("some", person1LayerEx)
    val person2Ex = new BsonObject().put("name", "jane doe")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex).add(person1Ex)
    val expectedLayer1 = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("some", expectedLayer1).encodeToBarray
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe")
    val bsonArray = new BsonArray().add(person1).add(person2).add(person1)
    val bsonLayer1 = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("some", bsonLayer1)
    val ex = "..persons[@some].age"
    val bsonInj = Boson.injector(ex, 20)
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val result: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(result, expected)
  }

  test("CodecJson - Top level key inject String value") {
    val expected = new BsonObject().put("name", "Albertina").encodeToString
    val bson = new BsonObject().put("name", "Albert")
    val ex = ".name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level key inject Int value") {
    val expected = new BsonObject().put("age", 3).encodeToString
    val bson = new BsonObject().put("age", 20)
    val ex = ".age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level Nested key inject String value") {
    val expectedLayer1 = new BsonObject().put("name", "Albertina")
    val expected = new BsonObject().put("person", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonObject().put("name", "Albert")
    val bson = new BsonObject().put("person", bsonLayer1)
    val ex = ".person.name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject key[single] value") {
    val expectedLayer1 = new BsonArray().add("Help").add("Ajuda")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[0]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject key[all] values") {
    val expectedLayer1 = new BsonArray().add("Help").add("Help").add("Help").add("Help").add("Help").add("Help")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[0 to end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject key[1 to 3] values") {
    val expectedLayer1 = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[1 to 3]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject key[1 until 4] values") {
    val expectedLayer1 = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[1 until 4]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject key[end] values") {
    val expectedLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Help")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = ".emergency[end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject [single] value") {
    val expected = new BsonArray().add("Help").add("Ajuda").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda")
    val ex = ".[0]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject [all] values") {
    val expected = new BsonArray().add("Help").add("Help").add("Help").add("Help").add("Help").add("Help").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = ".[0 to end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject [1 to 3] values") {
    val expected = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = ".[1 to 3]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject [1 until 4] values") {
    val expected = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = ".[1 until 4]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array inject [end] values") {
    val expected = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Help").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = ".[end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level key DOUBLE_DOTS inject String value") {
    val expected = new BsonObject().put("name", "Albertina").encodeToString
    val bson = new BsonObject().put("name", "Albert")
    val ex = "..name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level key DOUBLE_DOTS inject Int value") {
    val expected = new BsonObject().put("age", 3).encodeToString
    val bson = new BsonObject().put("age", 20)
    val ex = "..age"
    val jsonInj = Boson.injector(ex, 3)
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level Nested key DOUBLE_DOTS inject String value") {
    val expectedLayer1 = new BsonObject().put("name", "Albertina")
    val expectedLayer2 = new BsonObject().put("Something", expectedLayer1)
    val expected = new BsonObject().put("person", expectedLayer2).encodeToString
    val bsonLayer1 = new BsonObject().put("name", "Albert")
    val bsonLayer2 = new BsonObject().put("Something", bsonLayer1)
    val bson = new BsonObject().put("person", bsonLayer2)
    val ex = "..name"
    val jsonInj = Boson.injector(ex, "Albertina")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Nested key injection - .person[0].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("person", persons)
    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
    val personsExpected = new BsonArray().add(person1Expected).add(person2)
    val bsonExpected = new BsonObject().put("person", personsExpected).encodeToString
    val ex = ".person[0].age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(bsonExpected))
  }

  test("CodecJson - HasElem injection test") {
    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
    val person2Ex = new BsonObject().put("name", "Something")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expected = new BsonObject().put("persons", bsonArrayEx).encodeToString
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@name]"
    val bsonInj = Boson.injector(ex, "Something")
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - HasElem DOUBLE_DOTS injection test") {
    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
    val person2Ex = new BsonObject().put("name", "Something")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expectedLayer = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("layer1", expectedLayer).encodeToString
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bsonLayer = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("layer1", bsonLayer)
    val ex = "..persons[@name]"
    val bsonInj = Boson.injector(ex, "Something")
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - HasElem injection test - Middle object not applicable") {
    val person1Ex = new BsonObject().putNull("NullKey").put("name", "Something")
    val person2Ex = new BsonObject().put("name", "Something")
    val person3Ex = new BsonObject().put("person", "Helloo")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person3Ex).add(person2Ex)
    val expected = new BsonObject().put("persons", bsonArrayEx).encodeToString
    val person1 = new BsonObject().putNull("NullKey").put("name", "John Doe")
    val person2 = new BsonObject().put("name", "Jane Doe")
    val bsonArray = new BsonArray().add(person1).add(person3Ex).add(person2)
    val bson = new BsonObject().put("persons", bsonArray)
    val ex = ".persons[@name]"
    val bsonInj = Boson.injector(ex, "Something")
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject key[single] value") {
    val expectedLayer1 = new BsonArray().add("Help").add("Ajuda")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = "..emergency[0]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject key[all] values") {
    val expectedLayer1 = new BsonArray().add("Help").add("Help").add("Help").add("Help").add("Help").add("Help")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = "..emergency[0 to end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject key[1 to 3] values") {
    val expectedLayer1 = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = "..emergency[1 to 3]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject key[1 until 4] values") {
    val expectedLayer1 = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = "..emergency[1 until 4]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject key[end] values") {
    val expectedLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Help")
    val expected = new BsonObject().put("emergency", expectedLayer1).encodeToString
    val bsonLayer1 = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val bson = new BsonObject().put("emergency", bsonLayer1)
    val ex = "..emergency[end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject [single] value") {
    val expected = new BsonArray().add("Help").add("Ajuda").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda")
    val ex = "..[0]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject [all] values") {
    val expected = new BsonArray().add("Help").add("Help").add("Help").add("Help").add("Help").add("Help").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = "..[0 to end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject [1 to 3] values") {
    val expected = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = "..[1 to 3]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject [1 until 4] values") {
    val expected = new BsonArray().add("Blu").add("Help").add("Help").add("Help").add("Blu").add("Ajuda").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = "..[1 until 4]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Top level array DOUBLE_DOTS inject [end] values") {
    val expected = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Help").encodeToString
    val bson = new BsonArray().add("Blu").add("Ajuda").add("Blu").add("Ajuda").add("Blu").add("Ajuda")
    val ex = "..[end]"
    val jsonInj = Boson.injector(ex, "Help")
    val jsonEncoded = bson.encodeToString
    val future = jsonInj.go(jsonEncoded)
    val result = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Nested key injection - ..person[0].age") {
    val person1 = new BsonObject().put("name", "john doe").put("age", 21)
    val person2 = new BsonObject().put("name", "jane doe").put("age", 12)
    val persons = new BsonArray().add(person1).add(person2)
    val bsonLayer1 = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("some", bsonLayer1)
    val person1Expected = new BsonObject().put("name", "john doe").put("age", 20)
    val personsExpected = new BsonArray().add(person1Expected).add(person2)
    val bsonExpectedLayer1 = new BsonObject().put("person", personsExpected)
    val bsonExpected = new BsonObject().put("some", bsonExpectedLayer1).encodeToString
    val ex = "..person[0].age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(bsonExpected))
  }


  test("CodecJson - Nested key injection - .person[0]..age") {
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe").put("some", person1Layer)
    val persons = new BsonArray().add(person1).add(person2)
    val bson = new BsonObject().put("person", persons)
    val person1ExpectedLayer = new BsonObject().put("age", 20)
    val person1Expected = new BsonObject().put("name", "john doe").put("some", person1ExpectedLayer)
    val personsExpected = new BsonArray().add(person1Expected).add(person2)
    val bsonExpected = new BsonObject().put("person", personsExpected).encodeToString
    val ex = ".person[0]..age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(bsonExpected))
  }

  test("CodecJson - Nested key injection - ..person[0]..age") {
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe").put("some", person1Layer)
    val persons = new BsonArray().add(person1).add(person2)
    val bsonLayer1 = new BsonObject().put("person", persons)
    val bson = new BsonObject().put("some", bsonLayer1)
    val person1ExpectedLayer = new BsonObject().put("age", 20)
    val person1Expected = new BsonObject().put("name", "john doe").put("some", person1ExpectedLayer)
    val personsExpected = new BsonArray().add(person1Expected).add(person2)
    val bsonExpectedLayer1 = new BsonObject().put("person", personsExpected)
    val bsonExpected = new BsonObject().put("some", bsonExpectedLayer1).encodeToString
    val ex = "..person[0]..age"
    val bsonInj = Boson.injector(ex, 20)
    val future = bsonInj.go(bson.encodeToString)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(bsonExpected))
  }

  test("CodecJson - Nested HasElem injection test") {
    val person1LayerEx = new BsonObject().put("age", 20)
    val person1Ex = new BsonObject().put("name", "john doe").put("some", person1LayerEx)
    val person2Ex = new BsonObject().put("name", "jane doe")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expectedLayer1 = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("some", expectedLayer1).encodeToString
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe")
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bsonLayer1 = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("some", bsonLayer1)
    val ex = ".some.persons[@some].age"
    val bsonInj = Boson.injector(ex, 20)
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Nested HasElem DOUBLE_DOTS injection test") {
    val person1LayerEx = new BsonObject().put("age", 20)
    val person1Ex = new BsonObject().put("name", "john doe").put("some", person1LayerEx)
    val person2Ex = new BsonObject().put("name", "jane doe").put("some", person1LayerEx)
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex)
    val expectedLayer1 = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("some", expectedLayer1).encodeToString
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe").put("some", person1Layer)
    val bsonArray = new BsonArray().add(person1).add(person2)
    val bsonLayer1 = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("some", bsonLayer1)
    val ex = "..persons[@some].age"
    val bsonInj = Boson.injector(ex, 20)
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  test("CodecJson - Nested HasElem injection test - Middle object not applicable") {
    val person1LayerEx = new BsonObject().put("age", 20)
    val person1Ex = new BsonObject().put("name", "john doe").put("some", person1LayerEx)
    val person2Ex = new BsonObject().put("name", "jane doe")
    val bsonArrayEx = new BsonArray().add(person1Ex).add(person2Ex).add(person1Ex)
    val expectedLayer1 = new BsonObject().put("persons", bsonArrayEx)
    val expected = new BsonObject().put("some", expectedLayer1).encodeToString
    val person1Layer = new BsonObject().put("age", 12)
    val person1 = new BsonObject().put("name", "john doe").put("some", person1Layer)
    val person2 = new BsonObject().put("name", "jane doe")
    val bsonArray = new BsonArray().add(person1).add(person2).add(person1)
    val bsonLayer1 = new BsonObject().put("persons", bsonArray)
    val bson = new BsonObject().put("some", bsonLayer1)
    val ex = "..persons[@some].age"
    val bsonInj = Boson.injector(ex, 20)
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val result: String = Await.result(future, Duration.Inf)
    assert(result.equals(expected))
  }

  case class BookExt(price: Double, title: String, edition: Int, forSale: Boolean, nPages: Long)

  case class Book1Ext(title: String, price: Double)

  case class SpecialEditions(title: String, price: Int, availability: Boolean)

  case class _BookExt(title: String, price: Double, specialEditions: Seq[SpecialEditions])

  case class _Book1Ext(title: String, price: Double, specialEditions: SpecialEditions)

  private val _book1 = new BsonObject().put("Title", "Scala").put("Price", 25.6).put("Edition", 10).put("ForSale", true).put("nPages", 750000000000L)
  private val _store = new BsonObject().put("Book", _book1)
  private val _bson = new BsonObject().put("Store", _store)

  case class Author(firstName: String, lastName: String, age: Int)

  case class NestedBook(name: String, pages: Int, author: Author)

  case class Book(name: String, pages: Int)

  case class Book1(pages: Double, someLong: Long, someBoolean: Boolean)

  case class Tags(Type: String, line: String, traded_pre_match: String, traded_in_play: String, name: String, marketgroupid: String)

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

  val expected: BsonObject = new BsonObject().put("name", "LOTR").put("pages", 320)
  val bsonBookExpected: BsonObject = new BsonObject().put("book", expected)

  val expected2: BsonObject = new BsonObject().put("name", "LOTR").put("pages", 320)
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

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val jsonStr: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val jsonObj: JsonObject = new JsonObject(jsonStr)
  val json: String = jsonObj.encode()
  val bson1: BsonObject = new BsonObject(jsonObj)
  val validatedByteArray: Array[Byte] = bson1.encodeToBarray()
  val tag: Tags = new Tags("", "", "", "", "", "")

  test("CodecBson - Key case class injection") {
    val book = new BsonObject().put("name", "Title1").put("pages", 1)
    val bson = new BsonObject().put("book", book)
    val expected = new BsonObject().put("name", "LOTR").put("pages", 320).encodeToBarray
    val ex = ".book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = bson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice expected)
  }

  test("CodecBson - Multiple key case class injection") {
    val book: BsonObject = new BsonObject().put("name", "LOTR").put("pages", 320)
    val bsonBook: BsonObject = new BsonObject().put("book", book)

    val books: BsonArray = new BsonArray().add(bsonBook).add(bsonBook2)
    val store: BsonObject = new BsonObject().put("books", books)
    val storeBsonExpected: BsonObject = new BsonObject().put("store", store)

    val ex = ".store.books[0].book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray())
  }

  test("CodecBson - Case case injection - [all]") {
    val ex = ".store.books[all].book"
    val expected = new BsonObject().put("name", "LOTR").put("pages", 320).encodeToBarray
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice expected) //TODO - Change expected here
  }

  test("CodecBson - Case case injection - [end]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)
    val expected = new BsonObject().put("name", "LOTR").put("pages", 320).encodeToBarray
    val ex = ".store.books[end].book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice expected)
  }

  test("CodecBson - Case case injection - ..books[@book]") {
    val ex = "..books[@book]"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray)
  }

  test("CodecBson - Case class injection - arr expression ..[end]") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToBarray()
    val ex = "..[end].book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = booksArr.encodeToBarray
    val future = bsonInj.go(bsonEncoded)
    val resultValue: Array[Byte] = Await.result(future, Duration.Inf)
    assertArrayEquals(resultValue, booksExpected)
  }

  test("CodecJson - Key case class injection") {
    val book = new BsonObject().put("name", "Title1").put("pages", 1)
    val bson = new BsonObject().put("book", book)
    val expected = new BsonObject().put("name", "LOTR").put("pages", 320).encodeToString
    val ex = ".book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = bson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice expected)
  }

  test("CodecJson - Multiple key case class injection") {
    val book: BsonObject = new BsonObject().put("name", "LOTR").put("pages", 320)
    val bsonBook: BsonObject = new BsonObject().put("book", book)
    val books: BsonArray = new BsonArray().add(bsonBook).add(bsonBook2)
    val store: BsonObject = new BsonObject().put("books", books)
    val storeBsonExpected: BsonObject = new BsonObject().put("store", store)
    val ex = ".store.books[0].book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice storeBsonExpected.encodeToString)
  }

  test("CodecJson - Case case injection - [all]") {
    val ex = ".store.books[all].book"
    val book = new BsonObject().put("name", "LOTR").put("pages", 320)
    val bookObj = new BsonObject().put("book", book)
    val bookArr = new BsonArray().add(bookObj).add(bookObj)
    val arrObj = new BsonObject().put("books", bookArr)
    val expected = new BsonObject().put("store", arrObj).encodeToString
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue equals expected)
  }

  test("CodecJson - Case case injection - [end]") {
    val books = new BsonArray().add(bsonBook).add(bsonBook2Expected)
    val store = new BsonObject().put("books", books)
    val storeBsonExpected = new BsonObject().put("store", store)
    val expected = new BsonObject().put("name", "LOTR").put("pages", 320).encodeToString
    val ex = ".store.books[end].book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice expected)
  }

  test("CodecJson - Case case injection - ..books[@book]") {
    val ex = "..books[@book]"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = storeBson.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice storeBsonExpected.encodeToString)
  }

  test("CodecJson - Case class injection - arr expression ..[end]") {
    val booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToString
    val ex = "..[end].book"
    val bsonInj = Boson.injector(ex, Book("LOTR", 320))
    val bsonEncoded = booksArr.encodeToString
    val future = bsonInj.go(bsonEncoded)
    val resultValue: String = Await.result(future, Duration.Inf)
    assert(resultValue containsSlice booksExpected)
  }
}
