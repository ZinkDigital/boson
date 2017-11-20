package io.boson

import java.nio.ByteBuffer

import io.boson.bson.BsonObject
import io.boson.nettybson.NettyBson
import io.vertx.core.buffer.Buffer
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable.ArrayBuffer
/**
  * Created by Tiago Filipe on 26/09/2017.
  */
@RunWith(classOf[JUnitRunner])
class BuffersTest extends FunSuite {
  val bsonEvent: BsonObject = new BsonObject().put("kitchen", "dirty".getBytes).put("Grade", 'C').put("CharSequence", "It WORKS!!!")
  val exampleNetty: NettyBson = new NettyBson(byteArray = Option(bsonEvent.encode().getBytes))

  test("Java ByteBuffer"){
    val array: Array[Byte] = bsonEvent.encode().getBytes
    val javaBuffer: ByteBuffer = ByteBuffer.allocate(array.size)
    javaBuffer.put(array)
    javaBuffer.flip()
    val nettyFromJava = new NettyBson(javaByteBuf = Option(javaBuffer))
    assert(javaBuffer.array() === nettyFromJava.getByteBuf.array()
      , "Content from ByteBuffer(java) it's different from nettyFromJava")
  }

  test("Scala ArrayBuffer[Byte]"){
    val scalaBuffer: ArrayBuffer[Byte] = new ArrayBuffer[Byte](256)
    exampleNetty.array.foreach(b => scalaBuffer.append(b))
    val nettyFromScala = new NettyBson(scalaArrayBuf = Option(scalaBuffer))
    assert(scalaBuffer.toArray === nettyFromScala.getByteBuf.array()
      , "Content from ArrayBuffer[Byte](Scala) it's different from nettyFromScala")
  }
}
