package io.zink.boson

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  //   test("Root modification") {
  //    val bson = new BsonObject().put("name", "john doe")
  //    val ex = "."
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase()
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE") //TODO maybe get rid of overhead information
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
  //  }
  //
  //  test("Root Injection") {
  //    val bson = new BsonObject().put("name", "john doe")
  //    val ex = "."
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      "Jane Doe"
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "Jane Doe")
  //      case Failure(e) => println(e); fail
  //    }
  //  }
  //
  //  test("Top level key modification") {
  //    val bson = new BsonObject().put("name", "john doe")
  //    val ex = ".name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
  //  }
  //
  //  test("Nested key modification - Single Dots") {
  //    val person = new BsonObject().put("name", "john doe")
  //    val bson = new BsonObject().put("person", person)
  //    val ex = ".person.name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
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
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
  //  }

  //  test("Nested key modification, two fields - Single Dots - Multiple layers") {
  //    val person = new BsonObject().put("name", "john doe").put("age", 21)
  //    val client = new BsonObject().put("person", person)
  //    val bson = new BsonObject().put("client", client)
  //    val ex = ".client.person.age"
  //    val bsonInj = Boson.injector(ex, (in: Int) => {
  //      in + 20
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => assert(resultValue containsSlice Array(41, 0, 0, 0))
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
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
  //    val future1 = bsonInj1.go(bson.encodeToBarray())
  //    future1 onComplete {
  //      case Success(resultValue) =>
  //        assert(new String(resultValue) contains "JOHN DOE")
  //
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future1, Duration.Inf)
  //    val future2 = bsonInj2.go(bson.encodeToBarray())
  //    future2 onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "john doe")
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future2, Duration.Inf)
  //    val future3 = bsonInj3.go(bson.encodeToBarray())
  //    future3 onComplete {
  //      case Success(resultValue) => assert(new String(resultValue) contains "John Doe Hello")
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future3, Duration.Inf)
  //  }
  //
  //  //  test("HasElem injection test") {  //TODO Correct this test
  //  //    val person1 = new BsonObject().put("name", "John Doe")
  //  //    val person2 = new BsonObject().put("name", "Jane Doe")
  //  //    val bsonArray = new BsonArray().add(person1).add(person2)
  //  //    val bson = new BsonObject().put("Persons", bsonArray)
  //  //    val ex = ".persons[@name]"
  //  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //  //      in.toUpperCase()
  //  //    })
  //  //    val future = bsonInj.go(bson.encodeToBarray())
  //  //    future onComplete {
  //  //      case Success(resultValue) => println("Here, result was this: " + resultValue);
  //  //      case Failure(e) => println(e); fail
  //  //    }
  //  //    Await.result(future, Duration.Inf)
  //  //  }
  //
  //  test("Root injection - Double dots") {
  //    val bson = new BsonObject().put("name", "John Doe")
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
  //  test("Nested key injection - Double dots") {
  //    val person = new BsonObject().put("name", "john doe")
  //    val bson = new BsonObject().put("person", person)
  //    //    val bson = new BsonObject().put("client", client)
  //
  //    val ex = "..name"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase()
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) => println("Here, result was this: " + new String(resultValue));
  //      case Failure(e) => println(e); fail
  //    }
  //    Await.result(future, Duration.Inf)
  //  }
  //
  //  test("Key with Array Exp [0 to 1] modification - Single Dots") {
  //    val bsonArray = new BsonArray().add("person1").add("person2").add("person3")
  //    val bson = new BsonObject().put("person", bsonArray)
  //    val ex = ".person[0 to 1]"
  //    val bsonInj = Boson.injector(ex, (in: String) => {
  //      in.toUpperCase
  //    })
  //    val future = bsonInj.go(bson.encodeToBarray())
  //    future onComplete {
  //      case Success(resultValue) =>
  //        println(resultValue.mkString(", "))
  //        val string = new String(resultValue)
  //        assert((string contains "PERSON1") && (string contains "PERSON2"))
  //      case Failure(e) =>
  //        println(e)
  //        fail
  //    }
  //    Await.result(future, Duration.Inf)
  //    println(bson.encodeToBarray().mkString(", "))
  //  }
  //
//  test("Key with Array Exp [0 until 1] modification - Single Dots") { //TODO - On Stand By
//    val bsonArray = new BsonArray().add("person1").add("person2").add("person3")
//    val bson = new BsonObject().put("person", bsonArray)
//    val ex = ".person[0 until 1]"
//    val bsonInj = Boson.injector(ex, (in: String) => {
//      in.toUpperCase
//    })
//    val future = bsonInj.go(bson.encodeToBarray())
//    future onComplete {
//      case Success(resultValue) =>
//        println("After: " + resultValue.mkString(", "))
//        val string = new String(resultValue)
//        assert((string contains "PERSON1") && (string contains "person2"))
//      case Failure(e) =>
//        println(e)
//        fail
//    }
//    Await.result(future, Duration.Inf)
//    println("Before: " + bson.encodeToBarray().mkString(", "))
//  }
  //
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

  test("Nested key injection - Multiple Layers- Double dots") { //TODO FIX
    val person = new BsonObject().put("name", "john doe").put("age", 21)
    val client = new BsonObject().put("person", person)
    val bson = new BsonObject().put("client", client)

    val ex = "..age"
    val bsonInj = Boson.injector(ex, (in: Int) => {
      in + 20
    })
    val future = bsonInj.go(bson.encodeToBarray())
    future onComplete {
      case Success(resultValue) => println(resultValue.mkString(" "));
      case Failure(e) => println(e); fail
    }
    Await.result(future, Duration.Inf)
  }
}
