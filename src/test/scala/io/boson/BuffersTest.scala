package io.boson

import java.nio.ByteBuffer
import java.nio.charset.Charset

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
  val charset: Charset = java.nio.charset.Charset.availableCharsets().get("UTF-8")

  val examplenettyBson: NettyBson = new NettyBson()
  val exampleNetty: NettyBson = examplenettyBson.writeBytes("kitchen","dirty".getBytes).writeChar("Grade",'C')
    .writeCharSequence("CharSequence","It WORKS!!!", charset)



  test("Java ByteBuffer"){
    val javaBuffer: ByteBuffer = ByteBuffer.allocate(256) // getClass.getSimpleName = HeapByteBuffer
    javaBuffer.put(exampleNetty.array)
    javaBuffer.flip()
    val nettyFromJava = new NettyBson(javaByteBuf = Option(javaBuffer))
    assert(new String(javaBuffer.array()) === new String(nettyFromJava.getByteBuf.array())
      , "Content from ByteBuffer(java) it's different from nettyFromJava")
  }

  test("Vertx ByteBuffer"){
    val vertxBuf: Buffer = Buffer.buffer()
    vertxBuf.appendBytes(exampleNetty.array)
    val nettyFromVertx = new NettyBson(vertxBuff = Option(vertxBuf))
    assert(new String(vertxBuf.getBytes) === new String(nettyFromVertx.getByteBuf.array())
      , "Content from ByteBuffer(Vertx) it's different from nettyFromVertx")
  }

  test("Scala ArrayBuffer[Byte]"){
    val scalaBuffer: ArrayBuffer[Byte] = new ArrayBuffer[Byte](256)
    exampleNetty.array.foreach(b => scalaBuffer.append(b))
    val nettyFromScala = new NettyBson(scalaArrayBuf = Option(scalaBuffer))
    assert(new String(scalaBuffer.toArray) === new String(nettyFromScala.getByteBuf.array())
      , "Content from ArrayBuffer[Byte](Scala) it's different from nettyFromScala")
  }
}
