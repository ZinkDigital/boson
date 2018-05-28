package io.zink.boson

import bsonLib.BsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success}


@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  test("Root modification") {
    val bson = new BsonObject().put("Name", "Lucas")
    val ex = "."
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase()
    })
    val future = bsonInj.go(bson.encodeToBarray())
    future onComplete {
      case Success(smt) => println("no sucesso: " + new String(smt))
      case Failure(e) => println("noooooooooo " + e)
    }
    //    BsonArray services = new BsonArray();
    //        BsonObject rootx = new BsonObject().put("Store", services);
    //        String expression = ".Store";
    //        Boson bosonInjector = Boson.injector(expression, (byte[] bl) -> new BsonArray().encodeToBarray());
    //        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
    //        byte[] result = midResult1.join();
    //        assertArrayEquals(rootx.encodeToBarray(), result);
  }

}
