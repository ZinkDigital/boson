package io.zink.josonInterface
import java.io.IOException
import java.util.concurrent.CompletableFuture

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import io.vertx.core.json.{JsonArray, JsonObject}

object JosonObject extends Joson {
  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  override def go(jsonStr: String): CompletableFuture[String] = ???

  /**
    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
    * joson that is the parameter in teh case of update/read conflicts.
    * the immutable String being returned unmodified.
    *
    * @param the Joson to fuse to.
    * @return the fused Joson
    */
  override def fuse(joson: Joson): Joson = ???

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
