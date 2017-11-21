package io.boson.performanceTests

import io.boson.bson.{BsonArray, BsonObject}
import io.boson.bsonValue.BsSeq
import io.boson.nettybson.NettyBson
import io.boson.scalaInterface.ScalaInterface
import io.vertx.core.json.JsonObject

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
object PerformanceTest extends App{

  val sI: ScalaInterface = new ScalaInterface

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close

  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  val netty: NettyBson = sI.createNettyBson(bson.encode().getBytes)

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns -> " + (t1 - t0)*scala.math.pow(10, -9) + "s" )
    result
  }


  /**
    * Testing performance of extracting a top value of a BsonObject
    */
  val result1 = time { sI.parse(netty.duplicate, "Epoch", "first") }
  println(result1.asInstanceOf[BsSeq].getValue.head)
  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject
    */
  val result2 = time { sI.parse(netty.duplicate, "SSLNLastName", "last") }
  println(new String(result2.asInstanceOf[BsSeq].value.head.asInstanceOf[Array[Byte]]))
  println()

  /**
    * Testing performance of extracting all 'Tags' values
    */
  val result3 = time { sI.parse(netty.duplicate, "Tags", "all") }
  println(result3.asInstanceOf[BsSeq].getValue)
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray
    */
  val result4 = time { sI.parse(netty.duplicate, "Markets", "[3 to 5]") }
  println(result4.asInstanceOf[BsSeq].getValue.asInstanceOf[Seq[BsonArray]].head.forEach(e => println(e)))
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray and selecting one
    */
  val result5 = time { sI.parse(netty.duplicate, "Markets", "last [3 to 5]") }
  println(result5.asInstanceOf[BsSeq].getValue)
  println(result5.asInstanceOf[BsSeq].getValue.asInstanceOf[Seq[BsonArray]].head.forEach(e => println(e)))
  println()

}


