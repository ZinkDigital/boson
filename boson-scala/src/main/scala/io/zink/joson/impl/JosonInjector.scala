package io.zink.joson.impl

import java.io.ByteArrayOutputStream

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import de.undercouch.bson4jackson.BsonFactory
import io.vertx.core.json.{JsonArray, JsonObject}
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.joson.Joson
import io.zink.joson.Joson.{JsonArraySerializer, JsonObjectSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Ricardo Martins on 19/01/2018.
  */
class JosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Joson{

  val anon: T => T = injectFunction

  def parseInj[K](netty: BosonImpl, injectFunction: K => K , expression: String): Array[Byte] = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program], Option(injectFunction)).run()
          netty.getByteBuf.array()
        case parser.Error(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException =>
        throw new Exception(e.getMessage)
        //BsObject.toBson(e.getMessage)
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
      val byteArr = parseInj(boson, anon, expression)
      val s: JsonNode = mapper.readTree(byteArr)
      s.toString
//      val r: String = parseInj(boson, anon, expression) match {
//        case ex: BsException =>
//          println(ex.getValue)
//          jsonStr
//        case nb: BsBoson =>
//          val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
//          val os = new ByteArrayOutputStream
//          val module = new SimpleModule
//          module.addSerializer(classOf[JsonObject],new JsonObjectSerializer)
//          module.addSerializer(classOf[JsonArray], new JsonArraySerializer)
//          mapper.registerModule(module)
//
//          val s: JsonNode = mapper.readTree(nb.getValue.getByteBuf.array())
//          s.toString
//
//        case x => jsonStr
//      }
//      r
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
