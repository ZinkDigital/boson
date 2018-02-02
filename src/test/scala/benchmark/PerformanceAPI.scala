package benchmark

import java.util.concurrent.CompletableFuture

import bsonLib.BsonObject
import io.boson.bson.Boson
import io.boson.bson.bsonValue.BsValue
import io.vertx.core.json.JsonObject
import org.scalameter._

import scala.io.Source

object PerformanceAPI extends App {

  def bestTimeMeasure[R](block: => R): Quantity[Double] = {
    val time = config(
      Key.exec.benchRuns -> 50
    ) withWarmer {
      new Warmer.Default
    } measure {
      block
    }
    time
  }

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)

  val validatedByteArray: Array[Byte] = bson.encodeToBarray()

  /**
    * Testing performance of extracting a top value of a BsonObject
    */
  val result1: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Epoch"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result1: ${future.join().getValue}")
  }
  println()
  println(s"result1 time: $result1, expression: Epoch")
  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject
    */
  val result2: Quantity[Double] = bestTimeMeasure {
    val expression: String = "SSLNLastName"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
  }
  println()
  println(s"result2 time: $result2, expression: SSLNLastName")
  println()

  /**
    * Testing performance of extracting all 'Tags' values
    */
  val result3: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Tags"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result3: ${future.join().getValue}")
  }

  println()
  println(s"result3 time: $result3, expression: Tags")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray
    */
  val result4 = bestTimeMeasure {
    val expression: String = "Markets.[3 to 5]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4 time: $result4, expression: Markets.[3 to 5]")
  println()

  /**
  *  Testing performance of extracting with two keys, extracting nothing
  * */
  val result5: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Markets.[10].selectiongroupid"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5 time: $result5, expression: Markets.[10].selectiongroupid")
  println()


}
