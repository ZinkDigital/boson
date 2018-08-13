package benchmark

import java.io.ByteArrayInputStream


import bsonLib.BsonObject
import com.fasterxml.jackson.core.JsonToken
import de.undercouch.bson4jackson.BsonParser
import io.vertx.core.json.JsonObject
import io.zink.boson.Boson

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source


object BosonUndercouch extends App {

  // get the input doc into JSON and encoding to BSON
  val bufferedSource: Source = Source.fromURL(getClass.getResource("/SportingMarkets.json"))
  val jsonStr: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val jsonObj: JsonObject = new JsonObject(jsonStr)
  val json: String = jsonObj.encode()
  val bsonObject: BsonObject = new BsonObject(jsonObj)
  val bson : Array[Byte] = bsonObject.encodeToBarray()


  val CYCLES = 256


  val startTimeBuffer: ListBuffer[Long] = new ListBuffer[Long]
  val endTimeBuffer: ListBuffer[Long] = new ListBuffer[Long]

  val expression = ".Markets[1].Tags"

  println("****** Boson *******")

  val extract = Boson.extractor(expression, (_: Tags) => {
    val end = System.nanoTime()
    endTimeBuffer.append(end)
  })

  (0 to CYCLES).foreach(n =>{
    val start = System.nanoTime
    val fut = extract.go(bson)
    Await.result(fut, Duration.Inf)
    startTimeBuffer.append(start)
  })


  val bosonTimes = startTimeBuffer zip(endTimeBuffer) map {  case ( start: Long, end: Long)  => end - start }
//  bosonTimes.foreach(println _)
//  println ("Avg :" + (bosonTimes.sum / bosonTimes.size))

  println("****** Undercouch Jackson *******")

  startTimeBuffer clear()
  endTimeBuffer clear()

  import de.undercouch.bson4jackson.BsonFactory

  val factory = new BsonFactory
  val bais = new ByteArrayInputStream(bson)


  (0 to CYCLES).foreach(n => {

    startTimeBuffer.append(System.nanoTime)

    bais.reset
    val parser = factory.createJsonParser(bais)

    try {
      parser.nextToken() match {
        case JsonToken.START_OBJECT => consumeObject(parser, "", expression)
        case JsonToken.START_ARRAY => consumeArray(parser, "", expression)
      }
    } catch {
      case e: Exception => if (e.getMessage.contains(expression) ){
        endTimeBuffer.append(System.nanoTime);
      } else {
        throw e
      }
    }
  })

  val bsonJacksonTimes = startTimeBuffer zip(endTimeBuffer) map {  case ( start: Long, end: Long)  => end - start }
//  bsonJacksonTimes.foreach(println _)
//  println ("Avg :" + (bsonJacksonTimes.sum / bsonJacksonTimes.size))



  def consumeObject( parser : BsonParser , location : String, expression : String ) : BsonParser = {

    var endObject = false
    val here = location + "."

    while (!endObject) {
      val name = parser.getCurrentName
      val next = parser.nextToken()
      next match {
        case JsonToken.START_OBJECT => consumeObject(parser, here+name, expression )
        case JsonToken.START_ARRAY => consumeArray(parser, here+name, expression )
        case JsonToken.END_OBJECT => endObject = true
        case JsonToken.END_ARRAY => println("Unexpected Array end")
        case _ => {
          // println(parser.getCurrentName)
        }
      }
    }

    // check if I just consumed the appropriate object
    if (location.contains(expression)) throw new Exception(s"Found $expression at $location" )
    // else println(location)

    parser
  }




  def consumeArray( parser : BsonParser, location : String, expression : String) : BsonParser = {

    var endArray = false
    val here = location + "["

    var index = 0
    while (!endArray) {
      val next = parser.nextToken()

      next match {
        case JsonToken.START_OBJECT => consumeObject(parser,  here+index+"]", expression)
        case JsonToken.START_ARRAY => consumeArray(parser, here+index, expression)
        case JsonToken.END_OBJECT => println("Unexpected Object end")
        case JsonToken.END_ARRAY => endArray = true
        case _ => {
          // println(parser.getCurrentName)
        }
      }
      index += 1
    }
    // check if I just consumed the appropriate array element
    if (location.contains(expression)) throw new Exception(s"Found $expression at $location" )
    // else println( location)
    parser
  }



}
