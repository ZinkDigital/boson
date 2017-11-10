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

  val bytearray1: Array[Byte] = "AlguresPorAi".getBytes()
  val bytearray2: Array[Byte] = "4".getBytes()

  val obj: BsonObject = new BsonObject().put("field", 0).put("no", "ok").put("array", bytearray2)
  val netty: Option[NettyBson] = Some(ext.createNettyBson(obj.encode()))

  test("Injector: Int => Int") {

    val b1: Option[NettyBson] = inj.modify(netty, "field", x => x.asInstanceOf[Int]+1 )
    //println("test "+b1.getByteBuf.capacity())
    val b2: Option[NettyBson] = inj.modify(b1, "field", x => x.asInstanceOf[Int]+1)
    val b3: Option[NettyBson] = inj.modify(b2, "field", x => x.asInstanceOf[Int]*4)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "field", "first")
    }
    println(result.asInstanceOf[List[Int]].head)
    assert(result === List(8)
      , "Contents are not equal")
  }

  test("Injector: String => String") {

    val b1: Option[NettyBson] = inj.modify(netty, "no", x => "no")
    val b2: Option[NettyBson] = inj.modify(b1, "no", x => "yes")
    val b3: Option[NettyBson] = inj.modify(b2, "no", x => "maybe")
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "no", "first")
    }
    println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "") === "maybe"
      , "Contents are not equal")
  }

  test("Injector: Array[Byte] => Array[Byte]") {

    val b1: Option[NettyBson] = inj.modify(netty, "array", x => bytearray1)
    val b2: Option[NettyBson] = inj.modify(b1, "array", x => bytearray2)
    val b3: Option[NettyBson] = inj.modify(b2, "array", x => bytearray1)
    val result: Any = b3 match {
      case None => List()
      case Some(nb) => ext.parse(nb, "array", "first")
    }
    println(new String(result.asInstanceOf[List[Array[Byte]]].head))
    assert(new String(result.asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "") === "AlguresPorAi"
      , "Contents are not equal")
  }


  }