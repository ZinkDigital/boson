package io.boson

import java.nio.ByteBuffer

import io.boson.bson.BsonObject
import io.boson.injectors.Injector
import io.boson.injectors.Injector.netty
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
  val obj: BsonObject = new BsonObject().put("field", 0).put("no", "ok")
  val netty: NettyBson = new NettyBson(vertxBuff = Option(obj.encode()))
  val inj: Injector = new Injector
  val ext = new ScalaInterface

  test("Injector: Int => Int") {

    val b1: NettyBson= inj.modify(netty, "field", x => x+1)


    val b2: NettyBson = inj.modify(b1, "field", x => x+1)

    val b3: NettyBson = inj.modify(b2, "field", x => x*4)
    val result: Any = ext.parse(b3, "field", "first")


    assert(result === List(8)
      , "Content from ByteBuffer(java) it's different from nettyFromJava")
  }
}