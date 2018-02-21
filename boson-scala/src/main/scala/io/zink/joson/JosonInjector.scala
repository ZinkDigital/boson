package io.zink.joson

import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import de.undercouch.bson4jackson.BsonFactory
import io.vertx.core.json.{JsonArray, JsonObject}
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsBoson, BsException, BsObject, BsValue}
import io.zink.boson.json.Joson.{JsonArraySerializer, JsonObjectSerializer}
import io.zink.josonInterface.Joson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.compat.java8.FunctionConverters._
import scala.concurrent.Future

/**
  * Created by Ricardo Martins on 19/01/2018.
  */
class JosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Joson{

  val anon: T => T = injectFunction

  def parseInj[K](netty: BosonImpl, injectFunction: K => K , expression: String):BsValue = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program], Option(injectFunction)).run()
        case parser.Error(msg, _) =>
          BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => BsObject.toBson(e.getMessage)
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

  override def go(jsonStr: String): Future[String] = {

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
    val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))

    val future: Future[String] =
    Future{
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
    }
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
  override def fuse(joson: Joson): Joson = new JosonFuse(this,joson)
}
