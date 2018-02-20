package io.zink.boson.json.jsonImpl

import java.io.{ByteArrayOutputStream, IOException}
import java.util.concurrent.CompletableFuture

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import io.vertx.core.json.{JsonArray, JsonObject}
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsObject, BsValue}
import io.zink.boson.json.Joson
import io.zink.boson.json.Joson.{JsonArraySerializer, JsonObjectSerializer}


/**
  * Created by Ricardo Martins on 19/01/2018.
  */



class JosonExtractor[T](expression: String, extractFunction: java.util.function.Consumer[T]) extends Joson {
  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  private def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) => BsObject.toBson(msg)
        case parser.Failure(msg, _) => BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException => BsObject.toBson(e.getMessage)
    }
  }

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

    val future: CompletableFuture[String] =
      CompletableFuture.supplyAsync(() => {
        val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        callParse(boson, expression) match {
          case (res: BsValue) =>
            extractFunction.accept(res.asInstanceOf[T])
          case _ => throw new RuntimeException("JosonExtractor -> go() default case!!!")
        }
        jsonStr
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
