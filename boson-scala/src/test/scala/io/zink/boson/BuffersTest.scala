package io.zink.boson

import bsonLib.BsonObject
import io.netty.util.ResourceLeakDetector
import io.zink.boson.bson.bosonImpl.BosonImpl
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
  * Created by Tiago Filipe on 26/09/2017.
  */
@RunWith(classOf[JUnitRunner])
class BuffersTest extends FunSuite {  //TODO SHOULD THIS BE REMOVED ?
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val bsonEvent: BsonObject = new BsonObject().put("kitchen", "dirty".getBytes).put("Grade", 'C').put("CharSequence", "It WORKS!!!")

//
//  test("Java ByteBuffer"){
//    val array: Array[Byte] = bsonEvent.encode().getBytes
//    val javaBuffer: ByteBuffer = ByteBuffer.allocate(array.length)
//    javaBuffer.put(array)
//    javaBuffer.flip()
//    val bosonFromJava = new BosonImpl(javaByteBuf = Option(javaBuffer))
//    assert(javaBuffer.array() === bosonFromJava.getByteBuf.array()
//      , "Content from ByteBuffer(java) it's different from bosonFromJava")
//  }
//
//  test("Scala ArrayBuffer[Byte]"){
//    val scalaBuffer: ArrayBuffer[Byte] = new ArrayBuffer[Byte](256)
//    exampleBoson.array.foreach(b => scalaBuffer.append(b))
//    val bosonFromScala = new BosonImpl(scalaArrayBuf = Option(scalaBuffer))
//    assert(scalaBuffer.toArray === bosonFromScala.getByteBuf.array()
//      , "Content from ArrayBuffer[Byte](Scala) it's different from bosonFromScala")
//  }
}
