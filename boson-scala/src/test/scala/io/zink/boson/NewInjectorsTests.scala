package io.zink.boson

import bsonLib.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  //    test("Root modification") {
  //      val bson = new BsonObject().put("name", "john doe")
  //      val ex = "."
  //      val bsonInj = Boson.injector(ex, (in: String) => {
  //        in.toUpperCase()
  //      })
  //      val future = bsonInj.go(bson.encodeToBarray())
  //      future onComplete {
  //        case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE") //TODO maybe get rid of overhead information
  //        case Failure(e) => println(e); fail
  //      }
  //    Await.result(future, Duration.Inf)
  //    }
  //
  //    test("Root Injection") {
  //      val bson = new BsonObject().put("name", "john doe")
  //      val ex = "."
  //      val bsonInj = Boson.injector(ex, (in: String) => {
  //        "Jane Doe"
  //      })
  //      val future = bsonInj.go(bson.encodeToBarray())
  //      future onComplete {
  //        case Success(resultValue) => assert(new String(resultValue) contains "Jane Doe")
  //        case Failure(e) => println(e); fail
  //      }
  //    }
  //
  //    test("Top level key modification") {
  //      val bson = new BsonObject().put("name", "john doe")
  //      val ex = ".name"
  //      val bsonInj = Boson.injector(ex, (in: String) => {
  //        in.toUpperCase
  //      })
  //      val future = bsonInj.go(bson.encodeToBarray())
  //      future onComplete {
  //        case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
  //        case Failure(e) => println(e); fail
  //      }
  //      Await.result(future, Duration.Inf)
  //    }
  //
  //      test("Nested key modification - Single Dots") {
  //        val person = new BsonObject().put("name", "john doe")
  //        val bson = new BsonObject().put("person", person)
  //        val ex = ".person.name"
  //        val bsonInj = Boson.injector(ex, (in: String) => {
  //          in.toUpperCase
  //        })
  //        val future = bsonInj.go(bson.encodeToBarray())
  //        future onComplete {
  //          case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
  //          case Failure(e) => println(e); fail
  //        }
  //        Await.result(future, Duration.Inf)
  //      }
  //
  //      test("Nested key modification - Single Dots - Multiple layers") {
  //        val person = new BsonObject().put("name", "john doe")
  //        val client = new BsonObject().put("person", person)
  //        val bson = new BsonObject().put("client", client)
  //        val ex = ".client.person.name"
  //        val bsonInj = Boson.injector(ex, (in: String) => {
  //          in.toUpperCase
  //        })
  //        val future = bsonInj.go(bson.encodeToBarray())
  //        future onComplete {
  //          case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
  //          case Failure(e) => println(e); fail
  //        }
  //        Await.result(future, Duration.Inf)
  //      }

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

  //  test("HasElem injection test") {
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
      case Success(resultValue) => println("Here, result was this: " + new String(resultValue));
      case Failure(e) => println(e); fail
    }
    Await.result(future, Duration.Inf)
  }
}
