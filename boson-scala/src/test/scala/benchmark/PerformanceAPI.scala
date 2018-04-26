package benchmark

import java.io.ByteArrayInputStream
import java.util.concurrent.CountDownLatch

import benchmark.PerformanceTests.{endTimeBuffer, timesBuffer}
import bsonLib.{BsonArray, BsonObject}
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import com.jayway.jsonpath.{Configuration, JsonPath, Option}
import io.netty.buffer.Unpooled
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.JsonObject
import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.extractLabels
import io.zink.boson.impl.{BosonExtractor, BosonExtractorObj}
import org.bson.{BSONDecoder, BSONObject, BasicBSONDecoder}

import scala.collection.mutable.ListBuffer
import org.scalameter._
import shapeless.{LabelledGeneric, TypeCase, Typeable, the}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
//import com.jayway.jsonpath.spi.cache.Cache


//object PerformanceAPI extends App {
//
//  def bestTimeMeasure[R](block: => R): Quantity[Double] = {
//    val time = config(
//      Key.exec.benchRuns -> 100,
//      Key.exec.minWarmupRuns -> 200,
//      Key.exec.maxWarmupRuns -> 200
//    ) withWarmer {
//      new Warmer.Default
//    } measure {
//      block
//    }
//    time
//  }
//  /**
//    * Testing performance of extracting a top value of a BsonObject using JsonPath
//    */
//  val result1JsonPath: Quantity[Double] = bestTimeMeasure{
//    val _: Any = JsonPath.read(Lib.doc,"$.Epoch")
//  }
//  println()
//  println(s"result1JsonPath time: $result1JsonPath, expression: .Epoch")
//  println()
//
//  /**
//    * Testing performance of extracting a top value of a BsonObject using Joson
//    */
//  val joson1: Joson = Joson.extractor(".Epoch", (_: String) => /*future.complete(in)*/{})
//  val result1Joson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson1Result: Future[String] = joson1.go(Lib.jsonStr)
//    val result: String = Await.result(joson1Result, Duration.Inf)
//    //future.join()
//    //println(s"result1: ${future.join().getValue}")
//  }
//  println()
//  println(s"result1Joson time: $result1Joson, expression: .Epoch")
//  println()
//
//  /**
//    * Testing performance of extracting a top value of a BsonObject using Boson
//    */
//  val boson1: Boson = Boson.extractor(".Epoch", (_: String) => /*future.complete(in)*/{})
//  val result1Boson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson1Result: Future[Array[Byte]] = boson1.go(Lib.validatedByteArray)
//    val result:Array[Byte] = Await.result(boson1Result, Duration.Inf)
//    //future.join()
//    //println(s"result1: ${future.join().getValue}")
//  }
//  println()
//  println(s"result1Boson time: $result1Boson, expression: .Epoch")
//  println("-----------------------------------------------------------------------------------------")
//
//
//  /**
//    * Testing performance of extracting a bottom value of a BsonObject using JsonPath
//    */
//  val result2JsonPath: Quantity[Double] = bestTimeMeasure{
//    val _: Any = JsonPath.read(Lib.doc,"$..SSLNLastName")
//  }
//  println()
//  println(s"result2JsonPath time: $result2JsonPath, expression: ..SSLNLastName")
//  println()
//
//  /**
//    * Testing performance of extracting a bottom value of a BsonObject using Joson
//    */
//  val joson2: Joson = Joson.extractor("SSLNLastName", (_: String) => /*future.complete(in)*/{})
//  val result2Joson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson2Result: Future[String] = joson2.go(Lib.jsonStr)
//    val result = Await.result(joson2Result, Duration.Inf)
//    //future.join()
//    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
//  }
//  println()
//  println(s"result2Joson time: $result2Joson, expression: SSLNLastName")
//  println()
//
//  /**
//    * Testing performance of extracting a bottom value of a BsonObject using Boson
//    */
//  val boson2: Boson = Boson.extractor("SSLNLastName", (_: String) => /*future.complete(in)*/{})
//  val result2Boson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson2Result: Future[Array[Byte]] = boson2.go(Lib.validatedByteArray)
//    val result: Array[Byte] = Await.result(boson2Result, Duration.Inf)
//    //future.join()
//    //println(s"result2: ${new String(future.join().getValue.asInstanceOf[Seq[Array[Byte]]].head)}")
//  }
//  println()
//  println(s"result2Boson time: $result2Boson, expression: SSLNLastName")
//  println("-----------------------------------------------------------------------------------------")
//
//
//  /**
//    * Testing performance of extracting all occurrences of a BsonObject using JsonPath
//    */
//  val result3JsonPath: Quantity[Double] = bestTimeMeasure{
//    val o: Any = JsonPath.read(Lib.doc, "$..Tags")
//  }
//  println()
//  println(s"result3JsonPath time: $result3JsonPath, expression: ..Tags")
//  println()
//
//  /**
//    * Testing performance of extracting all 'Tags' values using Joson
//    */
//  val joson3: Joson = Joson.extractor("Tags", (_: Seq[Any]) => /*future.complete(in)*/{})
//  val result3Joson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson3Result: Future[String] = joson3.go(Lib.jsonStr)
//    val result: String = Await.result(joson3Result, Duration.Inf)
//    //future.join()
//    //println(s"result3: ${future.join().getValue}")
//  }
//
//  println()
//  println(s"result3Joson time: $result3Joson, expression: Tags")
//  println()
//
//  /**
//    * Testing performance of extracting all 'Tags' values using Boson
//    */
//  val boson3: Boson = Boson.extractor("Tags", (_: Seq[Any]) => /*future.complete(in)*/{})
//  val result3Boson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson3Result: Future[Array[Byte]] = boson3.go(Lib.validatedByteArray)
//    val result: Array[Byte] = Await.result(boson3Result, Duration.Inf)
//    //future.join()
//    //println(s"result3: ${future.join().getValue}")
//  }
//
//  println()
//  println(s"result3Boson time: $result3Boson, expression: Tags")
//  println("-----------------------------------------------------------------------------------------")
//
//
//  /**
//    * Testing performance of extracting values of some positions of a BsonArray using JsonPath
//    */
//  val result4JsonPath: Quantity[Double] = bestTimeMeasure{
//    val o: Any = JsonPath.read(Lib.doc, "$..Markets[3:5]")
//  }
//  println()
//  println(s"result4JsonPath time: $result4JsonPath, expression: ..Markets[3:5]")
//  println()
//
//  /**
//    * Testing performance of extracting values of some positions of a BsonArray using Joson
//    */
//  val joson4: Joson = Joson.extractor("Markets[3 to 5]", (_: Seq[Any]) => /*future.complete(in)*/{})
//  val result4Joson = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson4Result: Future[String] = joson4.go(Lib.jsonStr)
//    val result: String = Await.result(joson4Result, Duration.Inf)
//    //future.join()
//    //println(s"result4: ${future.join().getValue}")
//  }
//  println()
//  println(s"result4Joson time: $result4Joson, expression: Markets[3 to 5]")
//  println()
//
//  /**
//    * Testing performance of extracting values of some positions of a BsonArray using Boson
//    */
//  val boson4: Boson = Boson.extractor("Markets[3 to 5]", (_: Seq[Any]) => /*future.complete(in)*/{})
//  val result4Boson = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson4Result: Future[Array[Byte]] = boson4.go(Lib.validatedByteArray)
//    val result: Array[Byte] = Await.result(boson4Result, Duration.Inf)
//    //future.join()
//    //println(s"result4: ${future.join().getValue}")
//  }
//  println()
//  println(s"result4Boson time: $result4Boson, expression: Markets[3 to 5]")
//  println("-----------------------------------------------------------------------------------------")
//
//
//  /**
//    *  Testing performance of extracting with two keys, extracting nothing using JsonPath
//    * */
//  val result5JsonPath: Quantity[Double] = bestTimeMeasure {
//    val o: Any = JsonPath.read(Lib.doc, "$..Markets[10].selectiongroupid")
//  }
//  println()
//  println(s"result5JsonPath time: $result5JsonPath, expression: Markets.[10].selectiongroupid")
//  println()
//
//  /**
//    *  Testing performance of extracting with two keys, extracting nothing using Joson
//    * */
//  val joson5: Joson = Joson.extractor("Markets[10].selectiongroupid", (_: Seq[Any]) => /*future.complete(in)*/{})
//  val result5Joson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val joson5Result: Future[String] = joson5.go(Lib.jsonStr)
//    val result: String = Await.result(joson5Result, Duration.Inf)
//    //future.join()
//    //println(s"result5: ${future.join().getValue}")
//  }
//  println()
//  println(s"result5Joson time: $result5Joson, expression: Markets[10].selectiongroupid")
//  println()
//
//  /**
//    *  Testing performance of extracting with two keys, extracting nothing using Boson
//    * */
//  val boson5: Boson = Boson.extractor("Markets[10].selectiongroupid", (_: Seq[Any]) => /*future.complete(in)*/{})
//  val result5Boson: Quantity[Double] = bestTimeMeasure {
//    //val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson5Result: Future[Array[Byte]] = boson5.go(Lib.validatedByteArray)
//    val result: Array[Byte] = Await.result(boson5Result, Duration.Inf)
//    //future.join()
//    //println(s"result5: ${future.join().getValue}")
//  }
//  println()
//  println(s"result5Boson time: $result5Boson, expression: Markets[10].selectiongroupid")
//  println("-----------------------------------------------------------------------------------------")
//
//
////  /**
////    *  Testing performance of extracting with complex expression using Joson
////    * */
////  val result6JsonPath: Quantity[Double] = Lib.bestTimeMeasure {
////    val expression: String = "$.store.book[*].display-price"
////    val o: Any = JsonPath.read(Lib.DOCUMENT,expression)
////  }
////  println()
////  println(s"result6JsonPath time: $result6JsonPath, expression: .store.book[*].display-price")
////  println()
////
////  /**
////    *  Testing performance of extracting with complex expression using Joson
////    * */
////  val result6Joson: Quantity[Double] = Lib.bestTimeMeasure {
////    val expression: String = ".store.book[0 to end].display-price"
////    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
////    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
////    joson.go(Lib.JSON_DOCUMENT)
////    future.join()
////  }
////  println()
////  println(s"result6Joson time: $result6Joson, expression: .store.book[*].display-price")
////  println()
////
////  /**
////    *  Testing performance of extracting with complex expression using Boson
////    * */
////  val result6Boson: Quantity[Double] = Lib.bestTimeMeasure {
////    val expression: String = ".store.book[0 to end].display-price"
////    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
////    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
////    boson.go(Lib.VALID_BSON_DOC)
////    future.join()
////  }
////  println()
////  println(s"result6Boson time: $result6Boson, expression: .store.book[*].display-price")
////  println("-----------------------------------------------------------------------------------------")
////
////  /**
////    *  Testing performance of extracting with complex expression using Joson
////    * */
////  val result7JsonPath: Quantity[Double] = Lib.bestTimeMeasure {
////    val expression: String = "$..book[*].display-price"
////    val o: Any = JsonPath.read(Lib.DOCUMENT,expression)
////  }
////  println()
////  println(s"result7JsonPath time: $result7JsonPath, expression: ..book[*].display-price")
////  println()
////
////  /**
////    *  Testing performance of extracting with complex expression using Joson
////    * */
////  val result7Joson: Quantity[Double] = Lib.bestTimeMeasure {
////    val expression: String = "book[0 to end].display-price"
////    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
////    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
////    joson.go(Lib.JSON_DOCUMENT)
////    future.join()
////  }
////  println()
////  println(s"result7Joson time: $result7Joson, expression: book[0 to end].display-price")
////  println()
////
////  /**
////    *  Testing performance of extracting with complex expression using Boson
////    * */
////  val result7Boson: Quantity[Double] = Lib.bestTimeMeasure {
////    val expression: String = "book[0 to end].display-price"
////    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
////    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
////    boson.go(Lib.VALID_BSON_DOC)
////    future.join()
////  }
////  println()
////  println(s"result7Boson time: $result7Boson, expression: book[0 to end].display-price")
////  println("-----------------------------------------------------------------------------------------")
//
//
//
//}

object Lib {

  def bestTimeMeasure[R](block: => R): Quantity[Double] = {
    val time = config(
      Key.exec.benchRuns -> 5000,
      Key.exec.minWarmupRuns -> 5000,
      Key.exec.maxWarmupRuns -> 5000
    ) withWarmer {
      new Warmer.Default
    } measure {
      block
    }
    time
  }

  //----------------------------------------------------------------------------------------------//
  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val jsonStr: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val jsonObj: JsonObject = new JsonObject(jsonStr)
  val json: String = jsonObj.encode()
  val bson: BsonObject = new BsonObject(jsonObj)
  val validatedByteArray: Array[Byte] = bson.encodeToBarray()

  def avgPerformance(timesBuffer: ListBuffer[Long]): Double = {
    val totalSize = timesBuffer.size
    val warmUpRounds = totalSize/3
    val twoThirdsOfMeasures = timesBuffer.toList.drop(warmUpRounds)
    val avgMS: Double = (twoThirdsOfMeasures.sum/twoThirdsOfMeasures.size)/1000000.0
    avgMS
  }

}

case class Tags(Type: String,line: String, traded_pre_match: String, traded_in_play: String, name: String, marketgroupid: String)


object PerformanceTests extends App {

  val firstResultBuffer: ListBuffer[Array[Byte]] = new ListBuffer[Array[Byte]]

  var rangeResults: List[Array[Byte]] = _

  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val timesBuffer: ListBuffer[Long] = new ListBuffer[Long]
  val endTimeBuffer: ListBuffer[Long] = new ListBuffer[Long]

  val bArr: Array[Byte] = Lib.validatedByteArray

  import com.jayway.jsonpath.spi.cache.CacheProvider
  import com.jayway.jsonpath.spi.cache.NOOPCache
  val cache: NOOPCache = new NOOPCache
  CacheProvider.setCache(cache)

  val conf2: Configuration = Configuration
    .builder()
    .mappingProvider(new GsonMappingProvider())
    .jsonProvider(new GsonJsonProvider())
    .build
val CYCLES = 10000

//  (0 to CYCLES).foreach(_ =>{
//    val start = System.nanoTime()
//    val _: Tags =  JsonPath.using(conf2).parse(Lib.bson.asJson().toString).read("$.Markets[1].Tags",classOf[Tags])
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//
//  println("JsonPath With Gson time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Markets[1].Tags")
//  timesBuffer.clear()
//  println()
/*
  val bosonClass: Boson = Boson.extractor(".Markets[1].Tags", (_: Tags) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach(n =>{

    val start = System.nanoTime()
    val fut = bosonClass.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })

  println(s"Boson With Class time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[1].Tags")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
*/

//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val _: java.util.List[Tags] = JsonPath.using(conf2).parse(Lib.bson.asJson().toString).read("$.Markets[*].Tags",classOf[java.util.List[Tags]])
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath With Seq[Gson] time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Markets[*].Tags")
//  timesBuffer.clear()
//  println()
  /*
  var c = -1
  val bclass1 = Boson.extractor(".Markets[all]", (out: Seq[Array[Byte]]) => {
    val end = System.nanoTime()
    c+=1
    if(c == CYCLES)
      rangeResults = out.toList
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach(_ =>{
    val start = System.nanoTime()
    val fut = bclass1.go(bArr)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"bclass1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[all]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

  var rangeByteBufs = rangeResults.map{ elem =>
    Unpooled.copiedBuffer(elem)
  }
  val typeable = Typeable[Tags]
  implicit val typeCase: scala.Option[TypeCase[Tags]] = Some(TypeCase[Tags])
  implicit val gen = LabelledGeneric[Tags]
  implicit val t = the[extractLabels[Tags]]
  val bclass11 = new BosonExtractorObj(".Tags", Some((_: Seq[Tags]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  }))

  (0 to CYCLES).foreach(_ =>{
    val start = System.nanoTime()
    val fut = bclass11.go2(rangeByteBufs)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
    rangeByteBufs = rangeResults.map{ elem =>
      Unpooled.copiedBuffer(elem)
    }
  })
  println(s"bclass11 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Tags -> as Case Class")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
*/


  val bosonClass1: Boson = Boson.extractor(".Markets[all].Tags", (_: Seq[Tags]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })


  (0 to CYCLES).foreach(_ =>{

    val start = System.nanoTime()
    val fut = bosonClass1.go(bArr)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })

  println(s"Boson With Seq[Class] time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[all].Tags")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println("------------------------------------------------------------------------------------------")
  println()

/*
//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    JsonPath.read(doc, "$.Epoch")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath1 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Epoch")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  val boson1: Boson = Boson.extractor(".Epoch", (_: Int) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = boson1.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Boson1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Epoch")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
//
//  val boson11: Boson = Boson.extractor("..Epoch", (_: Seq[Int]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val fut = boson11.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//
//  println(s"Boson11 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Epoch")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  val joson1: Boson = Boson.extractor(".Epoch", (_: Int) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = joson1.go(Lib.json)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })

  println(s"Joson1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Epoch")
  timesBuffer.clear()
  endTimeBuffer.clear()

  println()

//
//  val joson11: Boson = Boson.extractor("..Epoch", (_: Seq[Int]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//    //println(Lib.avgPerformance(ListBuffer((endTimeBuffer.last,timesBuffer.last)) map { case (e,s) => e-s}))
//  })
//
//  for(x <- 0 to 10000) yield {
//    println(x)
//    val start = System.nanoTime()
//    val fut = joson11.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//
//
//  println(s"Joson11 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Epoch")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
  println("------------------------------------------------------------------------------------------")

//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    JsonPath.read(doc, "$.Participants[1].Tags.SSLNLastName")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//
//  println("JsonPath2 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Participants[1].Tags.SSLNLastName")
//  timesBuffer.clear()
//  println()

  val boson2: Boson = Boson.extractor(".Participants[1].Tags.SSLNLastName", (_: String) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = boson2.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })

  println(s"Boson2 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Participants[1].Tags.SSLNLastName")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

//  val boson21: Boson = Boson.extractor("..SSLNLastName", (_: Seq[String]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = boson21.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Boson21 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..SSLNLastName")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  val joson2: Boson = Boson.extractor(".Participants[1].Tags.SSLNLastName", (_: String) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = joson2.go(Lib.json)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Joson2 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Participants[1].Tags.SSLNLastName")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

//  val joson21: Boson = Boson.extractor("..SSLNLastName", (_: Seq[String]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = joson21.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Joson21 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..SSLNLastName")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
  println("------------------------------------------------------------------------------------------")
  println()

//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    val obj: Any = JsonPath.read(doc, "$.Markets[*].Tags")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath3 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Markets[*].Tags   -> as [Any]")
//  timesBuffer.clear()
//  println()

  val boson3: Boson = Boson.extractor(".Markets[all].Tags", (_: Seq[Array[Byte]]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = boson3.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Boson3 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[all].Tags  -> as[Seq[Array[Byte]]]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
*/
/*
  var c: Int = -1
  val boson3: Boson = Boson.extractor(".Markets", (out: Array[Byte]) => {
    val end = System.nanoTime()
    c+=1
    if(c == CYCLES)
    firstResultBuffer.append(out)
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach {_ =>
    val start = System.nanoTime()
    val fut = boson3.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  }
  println(s"Boson3 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets  -> as[Array[Byte]]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

  c = -1
  val boson33: Boson = Boson.extractor(".[all]", (out: Seq[Array[Byte]]) => {
    val end = System.nanoTime()
    c+=1
    if(c == CYCLES)
    rangeResults = out.toList
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach {_ =>
    val start = System.nanoTime()
    val fut = boson33.go(firstResultBuffer.head)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  }
  println(s"Boson33 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .[all]  -> as[Seq[Array[Byte]]]")
  firstResultBuffer.clear()
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

  implicit val typeCase: scala.Option[TypeCase[Seq[Array[Byte]]]] = None
  val boson333 = new BosonExtractor[Seq[Array[Byte]]](".Tags", (out: Seq[Array[Byte]]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })

  var rangeByteBufs = rangeResults.map{ elem =>
    Unpooled.copiedBuffer(elem)
  }
  //rangeResults = Nil
//  println(s"range of Results -> ${rangeByteBufs.size}")
  (0 to CYCLES).foreach {_ =>
    val start = System.nanoTime()
    val fut = boson333.go2(rangeByteBufs)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
    rangeByteBufs = rangeResults.map{ elem =>
      Unpooled.copiedBuffer(elem)
    }
  }
  println(s"Boson333 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Tags  -> as[Seq[Array[Byte]]]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
*/

/*
//  val boson33: Boson = Boson.extractor("..Markets[all].Tags", (_: Seq[Array[Byte]]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = joson31.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Joson31 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Tags  -> as[Seq[String]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  val joson3: Boson = Boson.extractor(".Markets[all].Tags", (_: Seq[String]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = joson3.go(Lib.json)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })

  println(s"Joson3 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[all].Tags  -> as[Seq[String]]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

  println("------------------------------------------------------------------------------------------")

//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    JsonPath.read(doc, "$.Markets[3:5]")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath4 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Markets[3:5]  -> as[Any]")
//  timesBuffer.clear()
//  println()

  val boson4: Boson = Boson.extractor(".Markets[3 to 5]", (_: Seq[Array[Byte]]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = boson4.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Boson4 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[3 to 5]  -> as[Seq[Array[Byte]]]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
//  val boson41: Boson = Boson.extractor("..Markets[3 to 5]", (_: Seq[Array[Byte]]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = boson41.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Boson41 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Markets[3 to 5]  -> as[Seq[Array[Byte]]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  val joson4: Boson = Boson.extractor(".Markets[3 to 5]", (_: Seq[String]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = joson4.go(Lib.json)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Joson4 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[3 to 5]  -> as[Seq[String]]")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

//  val joson41: Boson = Boson.extractor("..Markets[3 to 5]", (_: Seq[String]) => {

//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = joson41.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Joson41 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Markets[3 to 5]  -> as[Seq[String]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()

  println("------------------------------------------------------------------------------------------")

//  (0 to CYCLES).foreach(n =>{
//    val start = System.nanoTime()
//    val conf2: Configuration = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
//    JsonPath.using(conf2).parse(Lib.bson.asJson().toString).read("$.Markets[10].selectiongroupid")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//
//
//  println("JsonPath5 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Markets[10].selectiongroupid")
//  timesBuffer.clear()
//  println()

  val boson5: Boson = Boson.extractor(".Markets[10].selectiongroupid", (_: Seq[Array[Byte]]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = boson5.go(Lib.validatedByteArray)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Boson5 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[10].selectiongroupid")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()

//  val boson51: Boson = Boson.extractor("..Markets[10].selectiongroupid", (_: Seq[Array[Byte]]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = boson51.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Boson51 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Markets[10].selectiongroupid")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  val joson5: Boson = Boson.extractor(".Markets[10].selectiongroupid", (_: Seq[String]) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })
  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime()
    val fut = joson5.go(Lib.json)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  })
  println(s"Joson5 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Markets[10].selectiongroupid")
  timesBuffer.clear()
  endTimeBuffer.clear()
  println()
//  val joson51: Boson = Boson.extractor("..Markets[10].selectiongroupid", (_: Seq[String]) => {

//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  for(_ <- 0 to 10000) yield {
//    val start = System.nanoTime()
//    val fut = joson51.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  }
//  println(s"Joson51 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Markets[10].selectiongroupid")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
  println("------------------------------------------------------------------------------------------")
*/
}

object Experiment extends App {
  import com.jayway.jsonpath.spi.cache.CacheProvider
  import com.jayway.jsonpath.spi.cache.NOOPCache
  val cache: NOOPCache = new NOOPCache
  CacheProvider.setCache(cache)

  val b11: BsonObject = new BsonObject().put("Title", "C++Machine").put("Price", 38)
  val br5: BsonArray = new BsonArray().add(b11)
  val b10: BsonObject = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
  val br4: BsonArray = new BsonArray().add(b10)
  val b9: BsonObject = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
  val br3: BsonArray = new BsonArray().add(b9)
  val b7: BsonObject = new BsonObject().put("Color", "Blue").put("Price", 38)
  val b6: BsonObject = new BsonObject().put("Color", "White").put("Price", 35)
  val b5: BsonObject = new BsonObject().put("Color", "Red").put("Price", 48)
  val b4: BsonObject = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", br3)
  val b3: BsonObject = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", br4)
  val b8: BsonObject = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", br5)
  val br1: BsonArray = new BsonArray().add(b3).add(b4).add(b8)
  val br2: BsonArray = new BsonArray().add(b5).add(b6).add(b7).add(b3)
  val b2: BsonObject = new BsonObject().put("Book", br1).put("Hatk", br2)
  val bsonEvent: BsonObject = new BsonObject().put("Store", b2)

  val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()

  val timesBuffer: ListBuffer[Long] = new ListBuffer[Long]
  val endTimeBuffer: ListBuffer[Long] = new ListBuffer[Long]

//  for(_ <- 0 to 100000) yield {
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(bsonEvent.asJson().toString)
//    JsonPath.read(doc, "$.Store.Book[*].Price")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  }
//  println("JsonPath1 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: .Store.Book[*].Price")
//  timesBuffer.clear()
//  println()

  val boson11: Boson = Boson.extractor(".Store.Book[all].Price", (out: Seq[Any]) => {
    val end = System.nanoTime()
    println(s"(.Store.Book[all].Price) Extracted -> $out")
    //endTimeBuffer.append(end)
  })

  for(_ <- 0 to 0) yield {
    val start = System.nanoTime()
    val fut = boson11.go(validatedByteArr)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  }
  //println(s"Boson1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: .Store.Book[all].Price")
  //timesBuffer.clear()
  //endTimeBuffer.clear()
  println()

//  for(_ <- 0 to 100000) yield {
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(bsonEvent.asJson().toString)
//    JsonPath.read(doc, "$..Price")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  }
//  println("JsonPath1 time -> "+Lib.avgPerformance(timesBuffer)+" ms, Expression: ..Price")
//  timesBuffer.clear()
//  println()

  val boson1: Boson = Boson.extractor("Price", (out: Seq[Any]) => {
    //val end = System.nanoTime()
    println(s"(Price) Extracted -> $out")
    //endTimeBuffer.append(end)
  })

  for(_ <- 0 to 0) yield {
    val start = System.nanoTime()
    val fut = boson1.go(validatedByteArr)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  }
  //println(s"Boson1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e,s) => e-s})} ms, Expression: ..Price")
  //timesBuffer.clear()
  //endTimeBuffer.clear()
  println()

}
