package io.zink.boson

import bsonLib.BsonObject
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NewInjectorsTests extends FunSuite {

  test("Root modification") {
    val bson = new BsonObject().put("Name", "Lucas")
    val ex = "."
    val bsonInj = Boson.injector(ex, (in: String) => {
      in.toUpperCase()
    })
    bsonInj.go(bson.encodeToBarray())
    //    BsonArray services = new BsonArray();
    //        BsonObject rootx = new BsonObject().put("Store", services);
    //        String expression = ".Store";
    //        Boson bosonInjector = Boson.injector(expression, (byte[] bl) -> new BsonArray().encodeToBarray());
    //        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
    //        byte[] result = midResult1.join();
    //        assertArrayEquals(rootx.encodeToBarray(), result);
  }

}
