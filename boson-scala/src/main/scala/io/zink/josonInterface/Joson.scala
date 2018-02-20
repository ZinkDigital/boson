package io.zink.josonInterface

import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.zink.joson.{JosonExtractor, JosonInjector}

trait Joson {
  /**
    * Make an Extractor that will call the extract function (Consumer) according to
    * the given expression.
    *
    * @param expression
    * @param extractFunction
    * @param < T>
    * @return a BosonImpl that is a BosonExtractor
    */
  def extractor[T](expression: String, extractFunction: Consumer[T]) = { // TODO construct an extractor
    new JosonExtractor[T](expression, extractFunction)
  }

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression
    * @param injectFunction
    * @param < T>
    * @return
    */
  def injector[T](expression: String, injectFunction: Function[T, T]) = { // TODO construct an injector
    new JosonInjector[T](expression, injectFunction)
  }

  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  def go(jsonStr: String): CompletableFuture[String]


  /**
    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
    * joson that is the parameter in teh case of update/read conflicts.
    * the immutable String being returned unmodified.
    *
    * @param the Joson to fuse to.
    * @return the fused Joson
    */
  def fuse(joson: Joson): Joson


  class JsonObjectSerializer extends JsonSerializer[JsonObject] {
    @throws[IOException]
    override def serialize(value: JsonObject, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
      jgen.writeObject(value.getMap)
    }
  }

  class JsonArraySerializer extends JsonSerializer[JsonArray] {
    @throws[IOException]
    override def serialize(value: JsonArray, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
      jgen.writeObject(value.getList)
    }
  }
}
