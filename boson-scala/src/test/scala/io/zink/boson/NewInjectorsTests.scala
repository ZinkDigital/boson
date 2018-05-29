package io.zink.boson

import bsonLib.BsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite

import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  //  test("Root modification") {
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

  test("Key modification") {
    val bson = new BsonObject().put("name", "john doe")
    val ex = ".name"
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase
    })
    val future = bsonInj.go(bson.encodeToBarray())
    future onComplete {
      case Success(resultValue) => assert(new String(resultValue) contains "JOHN DOE")
      case Failure(e) => println(e); fail
    }
    Await.result(future, Duration.Inf)
    //    future onComplete {
    //      case Success(resultValue) =>assert(new String(resultValue) contains "JOHN DOE")
    //      case Failure(e) => println(e); fail
    //    }
  }

}
