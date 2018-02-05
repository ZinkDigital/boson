package benchmark

import java.util.concurrent.CompletableFuture

import bsonLib.BsonObject
import com.jayway.jsonpath.Configuration
import io.boson.bson.Boson
import io.boson.bson.bsonValue.BsValue
import io.boson.json.Joson
import io.vertx.core.json.JsonObject
import org.scalameter._
import com.jayway.jsonpath.JsonPath._

import scala.io.Source

object PerformanceAPI extends App {

  def bestTimeMeasure[R](block: => R): Quantity[Double] = {
    val time = config(
      Key.exec.benchRuns -> 100
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
  val doc: Any = Configuration.defaultConfiguration().jsonProvider().parse(finale)

  val validatedByteArray: Array[Byte] = bson.encodeToBarray()


  /**
    * Testing performance of extracting a top value of a BsonObject using JsonPath
    */
  val result1JsonPath: Quantity[Double] = bestTimeMeasure{
    val expression: String = "$..Epoch"
    val o: Any = read(doc,expression)
  }
  println()
  println(s"result1JsonPath time: $result1JsonPath, expression: ..Epoch")
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
  println("-----------------------------------------------------------------------------------------")


  /**
    * Testing performance of extracting a bottom value of a BsonObject using JsonPath
    */
  val result2JsonPath: Quantity[Double] = bestTimeMeasure{
    val expression: String = "$..SSLNLastName"
    val o: Any = read(doc,expression)
  }
  println()
  println(s"result2JsonPath time: $result2JsonPath, expression: ..SSLNLastName")
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
  println("-----------------------------------------------------------------------------------------")


  /**
    * Testing performance of extracting all occurrences of a BsonObject using JsonPath
    */
  val result3JsonPath: Quantity[Double] = bestTimeMeasure{
    val expression: String = "$..Tags"
    val o: Any = read(doc, expression)
  }
  println()
  println(s"result3JsonPath time: $result3JsonPath, expression: ..Tags")
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
  println("-----------------------------------------------------------------------------------------")


  /**
    * Testing performance of extracting values of some positions of a BsonArray using JsonPath
    */
  val result4JsonPath: Quantity[Double] = bestTimeMeasure{
    val expression: String = "$..Markets[3:5]"
    val o: Any = read(doc, expression)
  }
  println()
  println(s"result4JsonPath time: $result4JsonPath, expression: ..Markets[3:5]")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray using Joson
    */
  val result4Joson = bestTimeMeasure {
    val expression: String = "Markets[3 to 5]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4Joson time: $result4Joson, expression: Markets[3 to 5]")
  println()

  /**
    * Testing performance of extracting values of some positions of a BsonArray using Boson
    */
  val result4Boson = bestTimeMeasure {
    val expression: String = "Markets[3 to 5]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result4: ${future.join().getValue}")
  }
  println()
  println(s"result4Boson time: $result4Boson, expression: Markets[3 to 5]")
  println("-----------------------------------------------------------------------------------------")


  /**
    *  Testing performance of extracting with two keys, extracting nothing using JsonPath
    * */
  val result5JsonPath: Quantity[Double] = bestTimeMeasure {
    val expression: String = "$..Markets[10].selectiongroupid"
    val o: Any = read(doc, expression)
  }
  println()
  println(s"result5JsonPath time: $result5JsonPath, expression: Markets.[10].selectiongroupid")
  println()

  /**
    *  Testing performance of extracting with two keys, extracting nothing using Joson
    * */
  val result5Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Markets[10].selectiongroupid"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(finale)
    future.join()
    //println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5Joson time: $result5Joson, expression: Markets[10].selectiongroupid")
  println()

  /**
    *  Testing performance of extracting with two keys, extracting nothing using Boson
    * */
  val result5Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "Markets[10].selectiongroupid"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArray)
    future.join()
    //println(s"result5: ${future.join().getValue}")
  }
  println()
  println(s"result5Boson time: $result5Boson, expression: Markets[10].selectiongroupid")
  println("-----------------------------------------------------------------------------------------")


  /**
    *  Testing performance of extracting with complex expression using Joson
    * */
  val result6JsonPath: Quantity[Double] = bestTimeMeasure {
    val expression: String = "$.store.book[*].display-price"
    val o: Any = parse(Documents.JSON_DOCUMENT).read(expression)
  }
  println()
  println(s"result6JsonPath time: $result6JsonPath, expression: .store.book[*].display-price")
  println()

  /**
    *  Testing performance of extracting with complex expression using Joson
    * */
  val result6Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = ".store.book[0 to end].display-price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(Documents.JSON_DOCUMENT)
    future.join()
  }
  println()
  println(s"result6Joson time: $result6Joson, expression: .store.book[*].display-price")
  println()

  /**
    *  Testing performance of extracting with complex expression using Boson
    * */
  val result6Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = ".store.book[0 to end].display-price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(Documents.VALID_BSON_DOC)
    future.join()
  }
  println()
  println(s"result6Boson time: $result6Boson, expression: .store.book[*].display-price")
  println("-----------------------------------------------------------------------------------------")

  /**
    *  Testing performance of extracting with complex expression using Joson
    * */
  val result7JsonPath: Quantity[Double] = bestTimeMeasure {
    val expression: String = "$..book[*].display-price"
    val o: Any = read(Documents.DOCUMENT,expression)
  }
  println()
  println(s"result7JsonPath time: $result7JsonPath, expression: ..book[*].display-price")
  println()

  /**
    *  Testing performance of extracting with complex expression using Joson
    * */
  val result7Joson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "book[0 to end].display-price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val joson: Joson = Joson.extractor(expression, (in: BsValue) => future.complete(in))
    joson.go(Documents.JSON_DOCUMENT)
    future.join()
  }
  println()
  println(s"result7Joson time: $result7Joson, expression: book[0 to end].display-price")
  println()

  /**
    *  Testing performance of extracting with complex expression using Boson
    * */
  val result7Boson: Quantity[Double] = bestTimeMeasure {
    val expression: String = "book[0 to end].display-price"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(Documents.VALID_BSON_DOC)
    future.join()
  }
  println()
  println(s"result7Boson time: $result7Boson, expression: book[0 to end].display-price")
  println("-----------------------------------------------------------------------------------------")



}

object Documents {

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

}
