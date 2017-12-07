package io.boson

import java.nio.ByteBuffer
import io.boson.bson.BsonObject
import io.boson.nettyboson.Boson
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

  val exampleBoson: Boson = new Boson(byteArray = Option(bsonEvent.encode().getBytes))

  test("Java ByteBuffer"){
    val array: Array[Byte] = bsonEvent.encode().getBytes
    val javaBuffer: ByteBuffer = ByteBuffer.allocate(array.length)
    javaBuffer.put(array)
    javaBuffer.flip()
    val bosonFromJava = new Boson(javaByteBuf = Option(javaBuffer))
    assert(javaBuffer.array() === bosonFromJava.getByteBuf.array()
      , "Content from ByteBuffer(java) it's different from bosonFromJava")
  }

  test("Scala ArrayBuffer[Byte]"){
    val scalaBuffer: ArrayBuffer[Byte] = new ArrayBuffer[Byte](256)
    exampleBoson.array.foreach(b => scalaBuffer.append(b))
    val bosonFromScala = new Boson(scalaArrayBuf = Option(scalaBuffer))
    assert(scalaBuffer.toArray === bosonFromScala.getByteBuf.array()
      , "Content from ArrayBuffer[Byte](Scala) it's different from bosonFromScala")
  }
}
