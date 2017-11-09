package io.boson

import io.boson.bson.BsonObject
import io.boson.injectors.Injector

import io.boson.nettybson.NettyBson
import io.boson.scalaInterface.ScalaInterface
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by Ricardo Martins on 09/11/2017.
  */

@RunWith(classOf[JUnitRunner])
class InjectorsTest extends FunSuite {
  val inj: Injector = new Injector
  val ext = new ScalaInterface
  val obj: BsonObject = new BsonObject().put("field", 0).put("no", "ok")
  val netty: NettyBson = ext.createNettyBson(obj.encode())


  test("Injector: Int => Int") {

    val b1: NettyBson= inj.modify(netty, "field", x => x.asInstanceOf[Int]+1 )
    val b2: NettyBson = inj.modify(b1, "field", x => x.asInstanceOf[Int]+1)
    val b3: NettyBson = inj.modify(b2, "field", x => x.asInstanceOf[Int]*4)
    val result: Any = ext.parse(b3, "field", "first")

    assert(result === List(8)
      , "Contents are not equal")
  }


  test("Injector: String => String") {

    val b1= inj.modify(netty, "no", x => "no")
    val b2= inj.modify(b1, "no", x => "yes")
    val b3= inj.modify(b2, "no", x => "maybe")
    val result: Any = ext.parse(b3, "no", "first")
    //println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

}