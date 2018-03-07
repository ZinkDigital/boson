package io.zink.joson

import java.io.IOException

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.zink.joson.impl.{JosonExtractor, JosonInjector, JosonValidate}

import scala.concurrent.Future
import scala.reflect._
import scala.reflect.runtime.universe._

object Joson{
  def validate[T: TypeTag: ClassTag](expression: String, validateFunction: T => Unit) = new JosonValidate[T](expression, validateFunction)
  /**
    * Make an Extractor that will call the extract function (Consumer) according to
    * the given expression.
    *
    * @param expression
    * @param extractFunction
    * @param < T>
    * @return a BosonImpl that is a BosonExtractor
    */
  def extractor[T: TypeTag: ClassTag](expression: String, extractFunction: T => Unit) = new JosonExtractor[T](expression, extractFunction)

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression
    * @param injectFunction
    * @param < T>
    * @return
    */
  def injector[T](expression: String, injectFunction: T => T) = new JosonInjector[T](expression, injectFunction)

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

trait Joson {
  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  def go(jsonStr: String): Future[String]


  /**
    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
    * joson that is the parameter in teh case of update/read conflicts.
    * the immutable String being returned unmodified.
    *
    * @param the Joson to fuse to.
    * @return the fused Joson
    */
  def fuse(joson: Joson): Joson

}
