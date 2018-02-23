package io.zink.joson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.zink.joson.impl.JosonExtractor;
import io.zink.joson.impl.JosonInjector;
import io.zink.joson.impl.JosonValidate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;


public interface Joson {


    static <T> Joson validate(String expression, Consumer<T> validateFunction) {

        return new JosonValidate<>(expression,validateFunction);
    }


    /**
     * Make an Extractor that will call the extract function (Consumer) according to
     * the given expression.
     * @param expression
     * @param extractFunction
     * @param <T>
     * @return a BosonImpl that is a BosonExtractor
     */
    static <T> Joson extractor(String expression, Consumer<T> extractFunction) {
        // TODO construct an extractor
        return new JosonExtractor<>(expression, extractFunction);
    }

    /**
     * Make an Injector that will call the inject function (of T -> T) according to
     * the given expression.
     * @param expression
     * @param injectFunction
     * @param <T>
     * @return
     */
    static <T> Joson injector(String expression, Function<T, T> injectFunction) {
        // TODO construct an injector
        return new JosonInjector<>(expression, injectFunction);
    }

    /**
     * Apply this Joson to the String that arrives and at some point in the future complete
     * the future with the resulting String. In the case of an Extractor this will result in
     * the immutable String being returned unmodified.
     * @param the Json string.
     * @return
     */
    CompletableFuture<String> go(final String jsonStr);


    /**
     * Fuse one Joson to another. The joson that is 'this' should be executed first before the
     * joson that is the parameter in teh case of update/read conflicts.
     * the immutable String being returned unmodified.
     * @param the Joson to fuse to.
     * @return the fused Joson
     */
    Joson fuse(final Joson joson);


    static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
        @Override
        public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getMap());
        }
    }

    static class JsonArraySerializer extends JsonSerializer<JsonArray> {
        @Override
        public void serialize(JsonArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getList());
        }
    }
}
