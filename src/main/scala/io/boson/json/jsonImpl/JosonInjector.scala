package io.boson.json.jsonImpl

import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
import java.util.function.Function

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.module.SimpleModule
import de.undercouch.bson4jackson.BsonFactory
import io.boson.bson.bsonImpl.{BosonImpl, CustomException}
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue.{BsBoson, BsException}
import io.boson.json.Joson
import io.boson.json.Joson.{JsonArraySerializer, JsonObjectSerializer}
import io.vertx.core.json.{JsonArray, JsonObject}
import scala.compat.java8.FunctionConverters._

/**
  * Created by Ricardo Martins on 19/01/2018.
  */
class JosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Joson{

  val anon: T => T = injectFunction.asScala

  def parseInj[K](netty: BosonImpl, injectFunction: K => K , expression: String):bsonValue.BsValue = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program], Option(injectFunction)).run()
        case parser.Error(msg, _) =>
          bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          bsonValue.BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */

  override def go(jsonStr: String): CompletableFuture[String] = {

    val a: JsonObject = new JsonObject(jsonStr)
    val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
    val os = new ByteArrayOutputStream
    val module = new SimpleModule
    module.addSerializer(classOf[JsonObject],new JsonObjectSerializer)
    module.addSerializer(classOf[JsonArray], new JsonArraySerializer)
    mapper.registerModule(module)
    mapper.writeValue(os, a)

    val bsonByteEncoding: Array[Byte] = os.toByteArray
    os.flush()
    val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))

    val future: CompletableFuture[String] =
    CompletableFuture.supplyAsync(() =>{
      val r: String = parseInj(boson, anon, expression) match {
        case ex: BsException =>
          println(ex.getValue)
          jsonStr
        case nb: BsBoson =>
          val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
          val os = new ByteArrayOutputStream
          val module = new SimpleModule
          module.addSerializer(classOf[JsonObject],new JsonObjectSerializer)
          module.addSerializer(classOf[JsonArray], new JsonArraySerializer)
          mapper.registerModule(module)

          val s: JsonNode = mapper.readTree(nb.getValue.getByteBuf.array())
          s.toString

        case x => jsonStr
      }
      r
    })
    future
  }

  /**
    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
    * joson that is the parameter in teh case of update/read conflicts.
    * the immutable String being returned unmodified.
    *
    * @param the Joson to fuse to.
    * @return the fused Joson
    */
  override def fuse(joson: Joson): Joson = ???
}
