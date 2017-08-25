package io.boson.json;


import io.boson.valid.Validation;

import javax.json.Json;
import javax.json.stream.JsonParser;
import java.util.function.Function;

/**
 * Given a JsonParser an Extractor can be implemented
 * @param <O> the output type of this extractor
 */
public interface JsonExtractor<O> extends Function<JsonParser, Validation<O>> {

    Validation<O> apply(JsonParser src);



}
