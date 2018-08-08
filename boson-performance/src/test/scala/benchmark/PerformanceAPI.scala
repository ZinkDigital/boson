package benchmark

import bsonLib.{BsonArray, BsonObject}
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import com.jayway.jsonpath.{Configuration, DocumentContext, JsonPath, Option}
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.JsonObject
import io.zink.boson.Boson


import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import org.scalameter._

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.io.Source
import scala.xml.Document

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

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val jsonStr: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val jsonObj: JsonObject = new JsonObject(jsonStr)
  val json: String = jsonObj.encode()
  val bson: BsonObject = new BsonObject(jsonObj)
  val validatedByteArray: Array[Byte] = bson.encodeToBarray()

  def avgPerformance(timesBuffer: ListBuffer[Long]): Double = {
    val totalSize = timesBuffer.size
    val warmUpRounds = totalSize / 3
    val twoThirdsOfMeasures = timesBuffer.toList.drop(warmUpRounds)
    val avgMS: Double = (twoThirdsOfMeasures.sum / twoThirdsOfMeasures.size) / 1000000.0
    avgMS
  }

}

case class Tags(Type: String, line: String, traded_pre_match: String, traded_in_play: String, name: String, marketgroupid: String)

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

//  (0 to CYCLES).foreach(_ => {
//    val start = System.nanoTime()
//    val _: Tags = JsonPath.using(conf2).parse(Lib.bson.asJson().toString).read("$.Markets[1].Tags", classOf[Tags])
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//
//  println("JsonPath With Gson time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Markets[1].Tags")
//  timesBuffer.clear()
//  println()
//
//  val bosonClass: Boson = Boson.extractor(".Markets[1].Tags", (_: Tags) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  (0 to CYCLES).foreach(n => {
//
//    val start = System.nanoTime()
//    val fut = bosonClass.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//
//  println(s"Boson With Class time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[1].Tags")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val _: java.util.List[Tags] = JsonPath.using(conf2).parse(Lib.bson.asJson().toString).read("$.Markets[*].Tags", classOf[java.util.List[Tags]])
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath With Seq[Gson] time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Markets[*].Tags")
//  timesBuffer.clear()
//  println()
//
//  val bosonClass1: Boson = Boson.extractor(".Markets[all].Tags", (_: Seq[Tags]) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//
//  (0 to CYCLES).foreach(_ => {
//
//    val start = System.nanoTime()
//    val fut = bosonClass1.go(bArr)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//
//  println(s"Boson With Seq[Class] time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[all].Tags")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println("------------------------------------------------------------------------------------------")
//  println()
//
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    JsonPath.read(doc, "$.Epoch")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath1 time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Epoch")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//
//  val boson1: Boson = Boson.extractor(".Epoch", (_: Int) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = boson1.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//  println(s"Boson1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Epoch")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  val joson1: Boson = Boson.extractor(".Epoch", (_: Int) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = joson1.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//
//  println(s"Joson1 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Epoch")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  println("------------------------------------------------------------------------------------------")
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    JsonPath.read(doc, "$.Participants[1].Tags.SSLNLastName")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//
//  println("JsonPath2 time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Participants[1].Tags.SSLNLastName")
//  timesBuffer.clear()
//  println()
//
//  val boson2: Boson = Boson.extractor(".Participants[1].Tags.SSLNLastName", (_: String) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = boson2.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//
//  println(s"Boson2 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Participants[1].Tags.SSLNLastName")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  val joson2: Boson = Boson.extractor(".Participants[1].Tags.SSLNLastName", (_: String) => {
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = joson2.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//  })
//  println(s"Joson2 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Participants[1].Tags.SSLNLastName")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  println("------------------------------------------------------------------------------------------")
//  println()
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    val obj: Any = JsonPath.read(doc, "$.Markets[*].Tags")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath3 time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Markets[*].Tags   -> as [Any]")
//  timesBuffer.clear()
//  println()
//
//  val boson3: Boson = Boson.extractor(".Markets[all].Tags", (_: Array[Byte]) => {
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = boson3.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  println(s"Boson3 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[all].Tags  -> as[Seq[Array[Byte]]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  val joson3: Boson = Boson.extractor(".Markets[all].Tags", (_: String) => {
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = joson3.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//
//  println(s"Joson3 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[all].Tags  -> as[Seq[String]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  println("------------------------------------------------------------------------------------------")
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val doc: Any = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS).jsonProvider().parse(Lib.bson.asJson().toString)
//    JsonPath.read(doc, "$.Markets[3:5]")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//  println("JsonPath4 time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Markets[3:5]  -> as[Any]")
//  timesBuffer.clear()
//  println()
//
//  val boson4: Boson = Boson.extractor(".Markets[3 to 5]", (_: Array[Byte]) => {
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = boson4.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  println(s"Boson4 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[3 to 5]  -> as[Seq[Array[Byte]]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  val joson4: Boson = Boson.extractor(".Markets[3 to 5]", (_: String) => {
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = joson4.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  println(s"Joson4 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[3 to 5]  -> as[Seq[String]]")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//  println("------------------------------------------------------------------------------------------")
//
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val conf2: Configuration = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
//    JsonPath.using(conf2).parse(Lib.bson.asJson().toString).read("$.Markets[10].selectiongroupid")
//    val end = System.nanoTime()
//    timesBuffer.append(end - start)
//  })
//
//
//  println("JsonPath5 time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression: .Markets[10].selectiongroupid")
//  timesBuffer.clear()
//  println()
//
//  val boson5: Boson = Boson.extractor(".Markets[10].selectiongroupid", (_: Array[Byte]) => {
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = boson5.go(Lib.validatedByteArray)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  println(s"Boson5 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[10].selectiongroupid")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()
//
//
//  val joson5: Boson = Boson.extractor(".Markets[10].selectiongroupid", (_: String) => {
//  })
//  (0 to CYCLES).foreach(n => {
//    val start = System.nanoTime()
//    val fut = joson5.go(Lib.json)
//    Await.result(fut, Duration.Inf)
//    timesBuffer.append(start)
//    val end = System.nanoTime()
//    endTimeBuffer.append(end)
//  })
//  println(s"Joson5 time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: .Markets[10].selectiongroupid")
//  timesBuffer.clear()
//  endTimeBuffer.clear()
//  println()

  println("-------------------------INJECTORS----------------------------------")

  // .Markets[1].Tags

  val tag: Tags = new Tags("", "", "", "", "", "")

//  performanceJsonPath("$.Markets[1].Tags", tag)
//
//  // .Markets[first].Tags
//
//  performanceJsonPath("$.Markets[0].Tags", tag)
//
//  // .Markets[all].Tags
//
//  performanceJsonPath("$.Markets[*].Tags", tag)
//
//  performanceJsonPath("$.Markets[*].Tags", new Array[Byte](0))
//
//  // .Markets[end].Tags
//
//  performanceJsonPath("$.Markets.[-1].Tags", tag)
//
//  // .Markets[0 to 10].Tags
//
//  performanceJsonPath("$.Markets[0:10].Tags", tag)
//
//  // .Markets[0 until 10].Tags
//
//  performanceJsonPath("$.Markets[0:9].Tags", tag)
//
//  // .Markets[0 to end].Tags
//
//  performanceJsonPath("$.Markets[*].Tags", tag)
//
//  // .Markets[0 until end].Tags
//
//  performanceJsonPath("$.Markets[:-1].Tags", tag)
//
//  // .Epoch
//
//  performanceJsonPath("$.Epoch", new Integer(0))
//
//  //   .Participants[1].Tags.SSLNLastName
//
//  performanceJsonPath("$.Participants[1].Tags.SSLNLastName", "")
//
//  // .Markets[3 to 5]
//
//  performanceJsonPath("$.Markets[3:5]", "")
//
//  // .Markets[10].selectiongroupid
//
//  performanceJsonPath("$.Markets[10].selectiongroupid", "")
//
//  //  Relative Paths JsonPath
//
//  //  $.Markets.[-1].Tags
//  performanceJsonPath("$..Markets.[-1].Tags", tag)
//
//  // $..Markets[@Selections]..Id
//  performanceJsonPath("$..Markets[?(@.Selections)]..Id", "")
//
//  // $..marketgroupid
//  performanceJsonPath("$..marketgroupid", "")
//
//  // $..Selections..Tradable
//  performanceJsonPath("$..Selections..Tradable", false)
//
//  // $..Markets..Selections[@Id]
//  performanceJsonPath("$..Markets..Selections[?(@.Id)]", "")
//
//  // $..Markets[first].Tags
//  performanceJsonPath("$..Markets[0].Tags", tag)
//
//  // $..Markets[all].Tags
//  performanceJsonPath("$..Markets[*].Tags", tag)
//
//  //$..Markets[end].Tags..marketgroupid
//  performanceJsonPath("$..Markets[-1].Tags..marketgroupid", tag)


  //Injector .
  val bsonInj2: Boson = Boson.injector(".", (in: Array[Byte]) => in)
  val bsonInj2Json: Boson = Boson.injector(".", (in: String) => in)
  performanceAnalysis(bsonInj2, ".")
  performanceAnalysis(bsonInj2Json, ".", codecJson = true)

  //    Injector ..Markets[end].Tags
  val doubleDotInj1: Boson = Boson.injector("..Markets[end].Tags", (in: Tags) => in)
  performanceAnalysis(doubleDotInj1, "..Markets[end].Tags")
  performanceAnalysis(doubleDotInj1, "..Markets[end].Tags", codecJson = true)

  //      Injector .Markets[0 to 10].Tags
  val bosonArticle8: Boson = Boson.injector(".Markets[0 to 10].Tags", (in: Tags) => in)
  performanceAnalysis(bosonArticle8, ".Markets[0 to 10].Tags")
  performanceAnalysis(bosonArticle8, ".Markets[0 to 10].Tags", codecJson = true)

  //    Injector .Markets[0 to 9].Tags
  val bosonArticle9: Boson = Boson.injector(".Markets[0 to 9].Tags", (in: Tags) => in)
  performanceAnalysis(bosonArticle9, ".Markets[0 to 9].Tags")
  performanceAnalysis(bosonArticle9, ".Markets[0 to 9].Tags", codecJson = true)

  //    Injector .Markets[0 to end].Tags (.Markets[*].Tags)
  val bosonArticle10: Boson = Boson.injector(".Markets[0 to end].Tags", (in: Tags) => in)
  performanceAnalysis(bosonArticle10, ".Markets[0 to end].Tags")
  performanceAnalysis(bosonArticle10, ".Markets[0 to end].Tags", codecJson = true)

  //Injector ..Markets[@Selections]..Id
  val doubleDotInj2: Boson = Boson.injector("..Markets[@Selections]..Id", (in: String) => in)
  performanceAnalysis(doubleDotInj2, "..Markets[@Selections]..Id")
  performanceAnalysis(doubleDotInj2, "..Markets[@Selections]..Id", codecJson = true)


  //  Injector ..Markets[end].Tags..marketgroupid
  val doubleDotInj3: Boson = Boson.injector("..Markets[end].Tags..marketgroupid", (in: String) => in)
  performanceAnalysis(doubleDotInj3, "..Markets[end].Tags..marketgroupid")
    performanceAnalysis(doubleDotInj3, "..Markets[end].Tags..marketgroupid", codecJson = true)

  //  Injector ..marketgroupid
  val doubleDotInj4: Boson = Boson.injector("..marketgroupid", (in: String) => in)
  performanceAnalysis(doubleDotInj4, "..marketgroupid")
  performanceAnalysis(doubleDotInj4, "..marketgroupid", codecJson = true)


  //  Injector ..Selections..Tradable
  val doubleDotInj5: Boson = Boson.injector("..Selections..Tradable", (in: Boolean) => in)
  performanceAnalysis(doubleDotInj5, "..Selections..Tradable")
  performanceAnalysis(doubleDotInj5, "..Selections..Tradable", codecJson = true)


  //  Injector ..Markets..Selections[@Id]
  val doubleDotInj6: Boson = Boson.injector("..Markets..Selections[@Id]", (in: String) => in)
  performanceAnalysis(doubleDotInj6, "..Markets..Selections[@Id]")
  performanceAnalysis(doubleDotInj6, "..Markets..Selections[@Id]", codecJson = true)


  //  Injector ..Markets[first].Tags
  val doubleDotInj7: Boson = Boson.injector("..Markets[first].Tags", (in: Tags) => in)
  performanceAnalysis(doubleDotInj7, "..Markets[first].Tags")
  performanceAnalysis(doubleDotInj7, "..Markets[first].Tags", codecJson = true)


  //  Injector ..Markets[all].Tags
  val doubleDotInj8: Boson = Boson.injector("..Markets[all].Tags", (in: Tags) => in)
  performanceAnalysis(doubleDotInj8, "..Markets[all].Tags")
  performanceAnalysis(doubleDotInj8, "..Markets[all].Tags", codecJson = true)


  //    Injector .Epoch
  val bosonArticle1: Boson = Boson.injector(".Epoch", (in: Int) => in)
  performanceAnalysis(bosonArticle1, ".Epoch")
  performanceAnalysis(bosonArticle1, ".Epoch", codecJson = true)

  //      Injector .Participants[1].Tags.SSLNLastName
  val bosonArticle2: Boson = Boson.injector(".Participants[1].Tags.SSLNLastName", (in: String) => in)
  performanceAnalysis(bosonArticle2, ".Participants[1].Tags.SSLNLastName")
  performanceAnalysis(bosonArticle2, ".Participants[1].Tags.SSLNLastName", codecJson = true)

  //    Injector .Markets[all].Tags (.Markets[*].Tags) - Byte Array
  val bosonArticle3: Boson = Boson.injector(".Markets[all].Tags", (in: Array[Byte]) => in)
  val bosonArticle3Json: Boson = Boson.injector(".Markets[all].Tags", (in: String) => in)
  performanceAnalysis(bosonArticle3, ".Markets[all].Tags")
  performanceAnalysis(bosonArticle3Json, ".Markets[all].Tags", codecJson = true)

  //    Injector .Markets[3 to 5] (.Markets[3:5].Tags)
  val bosonArticle4: Boson = Boson.injector(".Markets[3 to 5]", (in: String) => in)
  performanceAnalysis(bosonArticle4, ".Markets[3 to 5]")
  performanceAnalysis(bosonArticle4, ".Markets[3 to 5]", codecJson = true)


  //    Injector .Markets[10].selectiongroupid
  val bosonArticle5: Boson = Boson.injector(".Markets[10].selectiongroupid", (in: String) => in)
  performanceAnalysis(bosonArticle5, ".Markets[10].selectiongroupid")
  performanceAnalysis(bosonArticle5, ".Markets[10].selectiongroupid", codecJson = true)


  //    Injector .Markets[1].Tags
  val bosonArticle6: Boson = Boson.injector(".Markets[1].Tags", (in: Tags) => in)
  performanceAnalysis(bosonArticle6, ".Markets[1].Tags")
  performanceAnalysis(bosonArticle6, ".Markets[1].Tags", codecJson = true)


  //    Injector .Markets[all].Tags (.Markets[*].Tags)
  val bosonArticle7: Boson = Boson.injector(".Markets[all].Tags", (in: Tags) => in)
  performanceAnalysis(bosonArticle7, ".Markets[all].Tags")
  performanceAnalysis(bosonArticle7, ".Markets[all].Tags", codecJson = true)


  /**
    * Private function to analise the performance of a given boson operation
    *
    * @param boson      - The boson operation (extractor or injector) to be analised
    * @param expression - The expression used inside the given boson operation
    */
  private def performanceAnalysis(boson: Boson, expression: String, codecJson: Boolean = false): Unit = {
    (0 to CYCLES).foreach(_ => {
      val start = System.nanoTime()
      val fut = if (codecJson) boson.go(Lib.json) else boson.go(Lib.validatedByteArray)
      Await.result(fut, Duration.Inf)
      val end = System.nanoTime()
      timesBuffer.append(start)
      endTimeBuffer.append(end)
    })

    println(s"INJECTORS Boson ${if (codecJson) "(CodecJson)" else "(CodecBson)"} With Class time -> ${Lib.avgPerformance(endTimeBuffer.zip(timesBuffer) map { case (e, s) => e - s })} ms, Expression: ${expression}")
    timesBuffer.clear()
    endTimeBuffer.clear()
    println()
  }

  private def performanceJsonPath(path: String, value: Any): Unit = {
    (0 to CYCLES).foreach(_ => {
      val start = System.nanoTime()
      val _: DocumentContext = JsonPath.using(conf2).parse(Lib.bson.asJson().toString).set(path, value)
      val end = System.nanoTime()
      timesBuffer.append(end - start)
    })

    println("JsonPath With Gson time -> " + Lib.avgPerformance(timesBuffer) + " ms, Expression:" + path)
    timesBuffer.clear()
    println()
  }


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

  val boson11: Boson = Boson.extractor(".Store.Book[all].Price", (out: Seq[Any]) => {
    val end = System.nanoTime()
//    println(s"(.Store.Book[all].Price) Extracted -> $out")
    //endTimeBuffer.append(end)
  })

  for (_ <- 0 to 0) yield {
    val start = System.nanoTime()
    val fut = boson11.go(validatedByteArr)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  }

  println()

  val boson1: Boson = Boson.extractor("Price", (out: Seq[Any]) => {
    //val end = System.nanoTime()
//    println(s"(Price) Extracted -> $out")
    //endTimeBuffer.append(end)
  })

  for (_ <- 0 to 0) yield {
    val start = System.nanoTime()
    val fut = boson1.go(validatedByteArr)
    Await.result(fut, Duration.Inf)
    timesBuffer.append(start)
  }

  println()
}
