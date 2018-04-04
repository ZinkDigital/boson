package io.zink.boson

import java.nio.ByteBuffer
import bsonLib.BsonObject
import io.netty.util.ResourceLeakDetector
import io.zink.boson.bson.bsonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.junit.Assert.assertArrayEquals

/**
  * Created by Tiago Filipe on 26/09/2017.
  */
@RunWith(classOf[JUnitRunner])
class BuffersTest extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val bsonEvent: BsonObject = new BsonObject().put("kitchen", "dirty".getBytes).put("Grade", 'C').put("CharSequence", "It WORKS!!!")

  val exampleBoson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))

  test("Java ByteBuffer") {
    val array: Array[Byte] = bsonEvent.encode().getBytes
    val javaBuffer: ByteBuffer = ByteBuffer.allocate(array.length)
    javaBuffer.put(array)
    javaBuffer.flip()
    val bosonFromJava = new BosonImpl(javaByteBuf = Option(javaBuffer))
    assertArrayEquals(javaBuffer.array(), bosonFromJava.getByteBuf.array())
  }

  test("Array[Byte]") {
    val array: Array[Byte] = bsonEvent.encodeToBarray
    val bosonFromScala = new BosonImpl(byteArray = Option(array))
    assertArrayEquals(array, bosonFromScala.getByteBuf.array())
  }

}
