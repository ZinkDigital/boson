package io.boson.performanceTests

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.nettybson.NettyBson
import io.boson.scalaInterface.ScalaInterface
import io.vertx.core.json.JsonObject

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
object PerformanceTest extends App{

  val sI: ScalaInterface = new ScalaInterface

  val bufferedSource = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  val netty: NettyBson = sI.createNettyBson(bson.encode().getBytes)
  println(s"netty bytebuf capacity: ${netty.getByteBuf.capacity()}")
  println(s"netty bytebuf writerIndex: ${netty.getByteBuf.writerIndex()}")

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }


  /**
    * Testing performance of extracting a top value of a BsonObject
    */
//  val result1 = time { sI.parse(netty.duplicate, "Epoch", "first") }
//  println(result1)
//  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject
    */
//  val result2 = time { sI.parse(netty.duplicate, "SSLNLastName", "last") }
//  println(result2)
//  println()

  /**
    * Testing performance of extracting all Epoch values
    */
    //netty.extract(netty.getByteBuf, "Tradable", "last")
  val result3 = time { sI.parse(netty, "Tags", "all") }
  println(result3)
  //println(new String(result3.asInstanceOf[BsSeq].value.head.asInstanceOf[Array[Byte]]))
  //result3.asInstanceOf[BsSeq].getValue.foreach(elem => println(new String(elem.asInstanceOf[Array[Byte]])))
  //println()

}


