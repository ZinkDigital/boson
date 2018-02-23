package benchmark

import java.util.concurrent.CountDownLatch

import bsonLib.BsonObject
import com.jayway.jsonpath.{Configuration, JsonPath}
import io.vertx.core.json.JsonObject
import io.zink.boson.Boson
import io.zink.boson.bson.bsonValue.BsValue
import io.zink.joson.Joson
import org.scalameter._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

object PerformanceAPI extends App {

  def bestTimeMeasure[R](block: => R): Quantity[Double] = {
    val time = config(
      Key.exec.benchRuns -> 100,
      Key.exec.minWarmupRuns -> 200,
      Key.exec.maxWarmupRuns -> 200
    ) withWarmer {
      new Warmer.Default
    } measure {
      block
    }
    time
  }
  /**
    * Testing performance of extracting a top value of a BsonObject using JsonPath
    */
  val result1JsonPath: Quantity[Double] = bestTimeMeasure{
    val _: Any = JsonPath.read(Lib.doc,"$.Epoch")
  }
  println()
  println(s"result1JsonPath time: $result1JsonPath, expression: .Epoch")
  println()

  /**
    * Testing performance of extracting a top value of a BsonObject using Joson
    */
  val joson1: Joson = Joson.extractor(".Epoch", (_: BsValue) => /*future.complete(in)*/{})
  val result1Joson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson1Result: Future[String] = joson1.go(Lib.jsonStr)
    val result: String = Await.result(joson1Result, Duration.Inf)
    //future.join()
    //println(s"result1: ${future.join().getValue}")
  }
  println()
  println(s"result1Joson time: $result1Joson, expression: .Epoch")
  println()

  /**
    * Testing performance of extracting a top value of a BsonObject using Boson
    */
  val boson1: Boson = Boson.extractor(".Epoch", (_: BsValue) => /*future.complete(in)*/{})
  val result1Boson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson1Result: Future[Array[Byte]] = boson1.go(Lib.validatedByteArray)
    val result:Array[Byte] = Await.result(boson1Result, Duration.Inf)
    //future.join()
    //println(s"result1: ${future.join().getValue}")
  }
  println()
  println(s"result1Boson time: $result1Boson, expression: .Epoch")
  println("-----------------------------------------------------------------------------------------")


  /**
    * Testing performance of extracting a bottom value of a BsonObject using JsonPath
    */
  val result2JsonPath: Quantity[Double] = bestTimeMeasure{
    val _: Any = JsonPath.read(Lib.doc,"$..SSLNLastName")
  }
  println()
  println(s"result2JsonPath time: $result2JsonPath, expression: ..SSLNLastName")
  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject using Joson
    */
  val joson2: Joson = Joson.extractor("SSLNLastName", (_: BsValue) => /*future.complete(in)*/{})
  val result2Joson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson2Result: Future[String] = joson2.go(Lib.jsonStr)
    val result = Await.result(joson2Result, Duration.Inf)
    //future.join()
    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
  }
  println()
  println(s"result2Joson time: $result2Joson, expression: SSLNLastName")
  println()

  /**
    * Testing performance of extracting a bottom value of a BsonObject using Boson
    */
  val boson2: Boson = Boson.extractor("SSLNLastName", (_: BsValue) => /*future.complete(in)*/{})
  val result2Boson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson2Result: Future[Array[Byte]] = boson2.go(Lib.validatedByteArray)
    val result: Array[Byte] = Await.result(boson2Result, Duration.Inf)
    //future.join()
    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
  }
  println()
  println(s"result2Boson time: $result2Boson, expression: SSLNLastName")
  println("-----------------------------------------------------------------------------------------")


  /**
    * Testing performance of extracting all occurrences of a BsonObject using JsonPath
    */
  val result3JsonPath: Quantity[Double] = bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc, "$..Tags")
  }
  println()
  println(s"result3JsonPath time: $result3JsonPath, expression: ..Tags")
  println()

  /**
    * Testing performance of extracting all 'Tags' values using Joson
    */
  val joson3: Joson = Joson.extractor("Tags", (_: BsValue) => /*future.complete(in)*/{})
  val result3Joson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson3Result: Future[String] = joson3.go(Lib.jsonStr)
    val result: String = Await.result(joson3Result, Duration.Inf)
    //future.join()
    //println(s"result3: ${future.join().getValue}")
  }

  println()
  println(s"result3Joson time: $result3Joson, expression: Tags")
  println()

  /**
    * Testing performance of extracting all 'Tags' values using Boson
    */
  val boson3: Boson = Boson.extractor("Tags", (_: BsValue) => /*future.complete(in)*/{})
  val result3Boson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson3Result: Future[Array[Byte]] = boson3.go(Lib.validatedByteArray)
    val result: Array[Byte] = Await.result(boson3Result, Duration.Inf)
    //future.join()
    //println(s"result3: ${future.join().getValue}")
  }

  println()
  println(s"result3Boson time: $result3Boson, expression: Tags")
  println("-----------------------------------------------------------------------------------------")


  /**
    * Testing performance of extracting values of some positions of a BsonArray using JsonPath
    */
  val result4JsonPath: Quantity[Double] = bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc, "$..Markets[3:5]")
  }
  println()
  println(s"result4JsonPath time: $result4JsonPath, expression: ..Markets[3:5]")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray using Joson
    */
  val joson4: Joson = Joson.extractor("Markets[3 to 5]", (_: BsValue) => /*future.complete(in)*/{})
  val result4Joson = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson4Result: Future[String] = joson4.go(Lib.jsonStr)
    val result: String = Await.result(joson4Result, Duration.Inf)
    //future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4Joson time: $result4Joson, expression: Markets[3 to 5]")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray using Boson
    */
  val boson4: Boson = Boson.extractor("Markets[3 to 5]", (_: BsValue) => /*future.complete(in)*/{})
  val result4Boson = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson4Result: Future[Array[Byte]] = boson4.go(Lib.validatedByteArray)
    val result: Array[Byte] = Await.result(boson4Result, Duration.Inf)
    //future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4Boson time: $result4Boson, expression: Markets[3 to 5]")
  println("-----------------------------------------------------------------------------------------")


  /**
    *  Testing performance of extracting with two keys, extracting nothing using JsonPath
    * */
  val result5JsonPath: Quantity[Double] = bestTimeMeasure {
    val o: Any = JsonPath.read(Lib.doc, "$..Markets[10].selectiongroupid")
  }
  println()
  println(s"result5JsonPath time: $result5JsonPath, expression: Markets.[10].selectiongroupid")
  println()

  /**
    *  Testing performance of extracting with two keys, extracting nothing using Joson
    * */
  val joson5: Joson = Joson.extractor("Markets[10].selectiongroupid", (_: BsValue) => /*future.complete(in)*/{})
  val result5Joson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson5Result: Future[String] = joson5.go(Lib.jsonStr)
    val result: String = Await.result(joson5Result, Duration.Inf)
    //future.join()
    //println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5Joson time: $result5Joson, expression: Markets[10].selectiongroupid")
  println()

  /**
    *  Testing performance of extracting with two keys, extracting nothing using Boson
    * */
  val boson5: Boson = Boson.extractor("Markets[10].selectiongroupid", (_: BsValue) => /*future.complete(in)*/{})
  val result5Boson: Quantity[Double] = bestTimeMeasure {
    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson5Result: Future[Array[Byte]] = boson5.go(Lib.validatedByteArray)
    val result: Array[Byte] = Await.result(boson5Result, Duration.Inf)
    //future.join()
    //println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5Boson time: $result5Boson, expression: Markets[10].selectiongroupid")
  println("-----------------------------------------------------------------------------------------")


//  /**
//    *  Testing performance of extracting with complex expression using Joson
//    * */
//  val result6JsonPath: Quantity[Double] = Lib.bestTimeMeasure {
//    val expression: String = "$.store.book[*].display-price"
//    val o: Any = JsonPath.read(Lib.DOCUMENT,expression)
//  }
//  println()
//  println(s"result6JsonPath time: $result6JsonPath, expression: .store.book[*].display-price")
//  println()
//
//  /**
//    *  Testing performance of extracting with complex expression using Joson
//    * */
//  val result6Joson: Quantity[Double] = Lib.bestTimeMeasure {
//    val expression: String = ".store.book[0 to end].display-price"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
//    joson.go(Lib.JSON_DOCUMENT)
//    future.join()
//  }
//  println()
//  println(s"result6Joson time: $result6Joson, expression: .store.book[*].display-price")
//  println()
//
//  /**
//    *  Testing performance of extracting with complex expression using Boson
//    * */
//  val result6Boson: Quantity[Double] = Lib.bestTimeMeasure {
//    val expression: String = ".store.book[0 to end].display-price"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//    boson.go(Lib.VALID_BSON_DOC)
//    future.join()
//  }
//  println()
//  println(s"result6Boson time: $result6Boson, expression: .store.book[*].display-price")
//  println("-----------------------------------------------------------------------------------------")
//
//  /**
//    *  Testing performance of extracting with complex expression using Joson
//    * */
//  val result7JsonPath: Quantity[Double] = Lib.bestTimeMeasure {
//    val expression: String = "$..book[*].display-price"
//    val o: Any = JsonPath.read(Lib.DOCUMENT,expression)
//  }
//  println()
//  println(s"result7JsonPath time: $result7JsonPath, expression: ..book[*].display-price")
//  println()
//
//  /**
//    *  Testing performance of extracting with complex expression using Joson
//    * */
//  val result7Joson: Quantity[Double] = Lib.bestTimeMeasure {
//    val expression: String = "book[0 to end].display-price"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
//    joson.go(Lib.JSON_DOCUMENT)
//    future.join()
//  }
//  println()
//  println(s"result7Joson time: $result7Joson, expression: book[0 to end].display-price")
//  println()
//
//  /**
//    *  Testing performance of extracting with complex expression using Boson
//    * */
//  val result7Boson: Quantity[Double] = Lib.bestTimeMeasure {
//    val expression: String = "book[0 to end].display-price"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//    boson.go(Lib.VALID_BSON_DOC)
//    future.join()
//  }
//  println()
//  println(s"result7Boson time: $result7Boson, expression: book[0 to end].display-price")
//  println("-----------------------------------------------------------------------------------------")



}

object Lib {

  def bestTimeMeasure[R](block: => R): Quantity[Double] = {
    val time = config(
      Key.exec.benchRuns -> 100,
      Key.exec.minWarmupRuns -> 50,
      Key.exec.maxWarmupRuns -> 50
    ) withWarmer {
      new Warmer.Default
    } measure {
      block
    }
    time
  }

//  Key.exec.minWarmupRuns -> 50,
//  Key.exec.maxWarmupRuns -> 50

  val JSON_DOCUMENT: String = "{\n" +
    "   \"string-property\" : \"string-value\", \n" +
    "   \"int-max-property\" : " + Integer.MAX_VALUE + ", \n" +
    "   \"long-max-property\" : " + java.lang.Long.MAX_VALUE + ", \n" +
    "   \"boolean-property\" : true, \n" +
    "   \"null-property\" : null, \n" +
    "   \"int-small-property\" : 1, \n" +
    "   \"max-price\" : 10, \n" +
    "   \"store\" : {\n" +
    "      \"book\" : [\n" +
    "         {\n" +
    "            \"category\" : \"reference\",\n" +
    "            \"author\" : \"Nigel Rees\",\n" +
    "            \"title\" : \"Sayings of the Century\",\n" +
    "            \"display-price\" : 8.95\n" +
    "         },\n" +
    "         {\n" +
    "            \"category\" : \"fiction\",\n" +
    "            \"author\" : \"Evelyn Waugh\",\n" +
    "            \"title\" : \"Sword of Honour\",\n" +
    "            \"display-price\" : 12.99\n" +
    "         },\n" +
    "         {\n" +
    "            \"category\" : \"fiction\",\n" +
    "            \"author\" : \"Herman Melville\",\n" +
    "            \"title\" : \"Moby Dick\",\n" +
    "            \"isbn\" : \"0-553-21311-3\",\n" +
    "            \"display-price\" : 8.99\n" +
    "         },\n" +
    "         {\n" +
    "            \"category\" : \"fiction\",\n" +
    "            \"author\" : \"J. R. R. Tolkien\",\n" +
    "            \"title\" : \"The Lord of the Rings\",\n" +
    "            \"isbn\" : \"0-395-19395-8\",\n" +
    "            \"display-price\" : 22.99\n" +
    "         }\n" +
    "      ],\n" +
    "      \"bicycle\" : {\n" +
    "         \"foo\" : \"baz\",\n" +
    "         \"escape\" : \"Esc\\b\\f\\n\\r\\t\\n\\t\\u002A\",\n" +
    "         \"color\" : \"red\",\n" +
    "         \"display-price\" : 19.95,\n" +
    "         \"foo:bar\" : \"fooBar\",\n" +
    "         \"dot.notation\" : \"new\",\n" +
    "         \"dash-notation\" : \"dashes\"\n" +
    "      }\n" +
    "   },\n" +
    "   \"foo\" : \"bar\",\n" +
    "   \"@id\" : \"ID\"\n" +
    "}";

  val DOCUMENT: Any = Configuration.defaultConfiguration().jsonProvider().parse(JSON_DOCUMENT)

  private val jsonHelp: JsonObject = new JsonObject(JSON_DOCUMENT)
  val BSON_DOCUMENT: BsonObject = new BsonObject(jsonHelp)
  val VALID_BSON_DOC: Array[Byte] = BSON_DOCUMENT.encodeToBarray()

  //----------------------------------------------------------------------------------------------//

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
  val jsonStr: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val jsonObj: JsonObject = new JsonObject(jsonStr)
  val bson: BsonObject = new BsonObject(jsonObj)
  val doc: Any = Configuration.defaultConfiguration().jsonProvider().parse(jsonStr)

  val validatedByteArray: Array[Byte] = bson.encodeToBarray()

}

object PerformanceTests extends App {

  val result1JsonPath: Quantity[Double] = Lib.bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc,"$.Epoch")
    //println(s"::: $o")
  }
  println(s"result1JsonPath time: $result1JsonPath, expression: .Epoch")
  println()

  val latchB1: CountDownLatch = new CountDownLatch(150)
  val boson1: Boson = Boson.extractor(".Epoch", (in: BsValue) => {
    //println(s"->$in")
    latchB1.countDown()})
    val result1Boson: Quantity[Double] = Lib.bestTimeMeasure {
    boson1.go(Lib.validatedByteArray)//.join()
  }
  println(s"result1Boson time: $result1Boson, expression: .Epoch")
  println()
  latchB1.await()

  val latchJ1: CountDownLatch = new CountDownLatch(150)
  val joson1: Joson = Joson.extractor(".Epoch", (_: BsValue) => {latchJ1.countDown()} )
  val result1Joson: Quantity[Double] = Lib.bestTimeMeasure {
    joson1.go(Lib.jsonStr)//.join()
  }
  println(s"result1Joson time: $result1Joson, expression: .Epoch")
  println("-----------------------------------------------------------------------------------------")
  latchJ1.await()

  val result2JsonPath: Quantity[Double] = Lib.bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc,"$..SSLNLastName")
    //println(s"::: $o")
  }
  println(s"result2JsonPath time: $result2JsonPath, expression: ..SSLNLastName")
  println()

  val latchB2: CountDownLatch = new CountDownLatch(150)
  val boson2: Boson = Boson.extractor("SSLNLastName", (_: BsValue) => {latchB2.countDown()})
  val result2Boson: Quantity[Double] = Lib.bestTimeMeasure {
    boson2.go(Lib.validatedByteArray)//.join()
  }
  println(s"result2Boson time: $result2Boson, expression: SSLNLastName")
  println()
  latchB2.await()

  val latchJ2: CountDownLatch = new CountDownLatch(150)
  val joson2: Joson = Joson.extractor("SSLNLastName", (_: BsValue) => {latchJ2.countDown()} )
  val result2Joson: Quantity[Double] = Lib.bestTimeMeasure {
    joson2.go(Lib.jsonStr)//.join()
  }
  println(s"result2Joson time: $result2Joson, expression: SSLNLastName")
  println("-----------------------------------------------------------------------------------------")
  latchJ2.await()

  val result3JsonPath: Quantity[Double] = Lib.bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc,"$..Tags")
    //println(s"::: $o")
  }
  println(s"result3JsonPath time: $result3JsonPath, expression: ..Tags")
  println()

  val latchB3: CountDownLatch = new CountDownLatch(150)
  val boson3: Boson = Boson.extractor("Tags", (_: BsValue) => {latchB3.countDown()})
  val result3Boson: Quantity[Double] = Lib.bestTimeMeasure {
    boson3.go(Lib.validatedByteArray)//.join()
  }
  println(s"result3Boson time: $result3Boson, expression: Tags")
  println()
  latchB3.await()

  val latchJ3: CountDownLatch = new CountDownLatch(150)
  val joson3: Joson = Joson.extractor("Tags", (_: BsValue) => {latchJ3.countDown()} )
  val result3Joson: Quantity[Double] = Lib.bestTimeMeasure {
    joson3.go(Lib.jsonStr)//.join()
  }
  println(s"result3Joson time: $result3Joson, expression: Tags")
  println("-----------------------------------------------------------------------------------------")
  latchJ3.await()

  val result4JsonPath: Quantity[Double] = Lib.bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc,"$..Markets[3:5]")
    //println(s"::: $o")
  }
  println(s"result4JsonPath time: $result4JsonPath, expression: ..Markets[3:5]")
  println()

  val latchB4: CountDownLatch = new CountDownLatch(150)
  val boson4: Boson = Boson.extractor("Markets[3 to 5]", (_: BsValue) => {latchB4.countDown()})
  val result4Boson: Quantity[Double] = Lib.bestTimeMeasure {
    boson4.go(Lib.validatedByteArray)//.join()
  }
  println(s"result4Boson time: $result4Boson, expression: Markets[3 to 5]")
  println()
  latchB4.await()

  val latchJ4: CountDownLatch = new CountDownLatch(150)
  val joson4: Joson = Joson.extractor("Markets[3 to 5]", (_: BsValue) => {latchJ4.countDown()} )
  val result4Joson: Quantity[Double] = Lib.bestTimeMeasure {
    joson4.go(Lib.jsonStr)//.join()
  }
  println(s"result4Joson time: $result4Joson, expression: Markets[3 to 5]")
  println("-----------------------------------------------------------------------------------------")
  latchJ4.await()

  val result5JsonPath: Quantity[Double] = Lib.bestTimeMeasure{
    val o: Any = JsonPath.read(Lib.doc,"$..Markets[10].selectiongroupid")
    //println(s"::: $o")
  }
  println(s"result5JsonPath time: $result5JsonPath, expression: ..Markets[10].selectiongroupid")
  println()

  val latchB5: CountDownLatch = new CountDownLatch(150)
  val boson5: Boson = Boson.extractor("Markets[10].selectiongroupid", (_: BsValue) => {latchB5.countDown()})
  val result5Boson: Quantity[Double] = Lib.bestTimeMeasure {
    boson5.go(Lib.validatedByteArray)//.join()
  }
  println(s"result5Boson time: $result5Boson, expression: Markets[10].selectiongroupid")
  println()
  latchB5.await()

  val latchJ5: CountDownLatch = new CountDownLatch(150)
  val joson5: Joson = Joson.extractor("Markets[10].selectiongroupid", (_: BsValue) => {latchJ5.countDown()} )
  val result5Joson: Quantity[Double] = Lib.bestTimeMeasure {
    joson5.go(Lib.jsonStr)//.join()
  }
  println(s"result5Joson time: $result5Joson, expression: Markets[10].selectiongroupid")
  println("-----------------------------------------------------------------------------------------")
  latchJ5.await()

}
