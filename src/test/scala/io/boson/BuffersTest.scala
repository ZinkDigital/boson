package io.boson

import java.nio.ByteBuffer

import io.boson.bson.BsonObject
import io.boson.nettyboson.Boson
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
  val exampleNetty: Boson = new Boson(vertxBuff = Option(bsonEvent.encode()))

  test("Java ByteBuffer"){
    val array: Array[Byte] = bsonEvent.encode().getBytes
    val javaBuffer: ByteBuffer = ByteBuffer.allocate(array.size)
    javaBuffer.put(array)
    javaBuffer.flip()
    val nettyFromJava = new Boson(javaByteBuf = Option(javaBuffer))
    assert(javaBuffer.array() === nettyFromJava.getByteBuf.array()
      , "Content from ByteBuffer(java) it's different from nettyFromJava")
  }

  test("Vertx ByteBuffer"){
    val vertxBuf: Buffer = Buffer.buffer(exampleNetty.array.length)
    vertxBuf.appendBytes(exampleNetty.array)
    val nettyFromVertx = new Boson(vertxBuff = Option(vertxBuf))
    assert(vertxBuf.getBytes === nettyFromVertx.getByteBuf.array()
      , "Content from ByteBuffer(Vertx) it's different from nettyFromVertx")
  }

  test("Scala ArrayBuffer[Byte]"){
    val scalaBuffer: ArrayBuffer[Byte] = new ArrayBuffer[Byte](256)
    exampleNetty.array.foreach(b => scalaBuffer.append(b))
    val nettyFromScala = new Boson(scalaArrayBuf = Option(scalaBuffer))
    assert(scalaBuffer.toArray === nettyFromScala.getByteBuf.array()
      , "Content from ArrayBuffer[Byte](Scala) it's different from nettyFromScala")
  }
}
