package benchmark

import java.util.concurrent.CompletableFuture

import bsonLib.BsonObject
import io.boson.bson.Boson
import io.boson.bson.bsonValue.BsValue
import io.boson.json.Joson
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
    * Testing performance of extracting a top value of a BsonObject using Boson
    */
  val result1Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Epoch"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result1: ${future.join().getValue}")
  }
  println()
  println(s"result1Boson time: $result1Boson, expression: Epoch")
  println()

  /**
    * Testing performance of extracting a top value of a BsonObject using Joson
    */
  val result1Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Epoch"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result1: ${future.join().getValue}")
  }
  println()
  println(s"result1Joson time: $result1Joson, expression: Epoch")
  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject using Boson
    */
  val result2Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "SSLNLastName"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
  }
  println()
  println(s"result2Boson time: $result2Boson, expression: SSLNLastName")
  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject using Joson
    */
  val result2Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "SSLNLastName"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
  }
  println()
  println(s"result2Joson time: $result2Joson, expression: SSLNLastName")
  println()

  /**
    * Testing performance of extracting all 'Tags' values using Boson
    */
  val result3Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Tags"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result3: ${future.join().getValue}")
  }

  println()
  println(s"result3Boson time: $result3Boson, expression: Tags")
  println()

  /**
    * Testing performance of extracting all 'Tags' values using Joson
    */
  val result3Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Tags"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result3: ${future.join().getValue}")
  }

  println()
  println(s"result3Joson time: $result3Joson, expression: Tags")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray using Boson
    */
  val result4Boson = bestTimeMeasure {
    val expression: String = "Markets.[3 to 5]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4Boson time: $result4Boson, expression: Markets.[3 to 5]")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray using Joson
    */
  val result4Joson = bestTimeMeasure {
    val expression: String = "Markets.[3 to 5]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4Joson time: $result4Joson, expression: Markets.[3 to 5]")
  println()

  /**
  *  Testing performance of extracting with two keys, extracting nothing using Boson
  * */
  val result5Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Markets.[10].selectiongroupid"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5Boson time: $result5Boson, expression: Markets.[10].selectiongroupid")
  println()

  /**
    *  Testing performance of extracting with two keys, extracting nothing using Joson
    * */
  val result5Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Markets.[10].selectiongroupid"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5Joson time: $result5Joson, expression: Markets.[10].selectiongroupid")
  println()


}
