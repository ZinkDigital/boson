package io.boson.json.extractors;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.util.function.Consumer;


public class BooleanExtractor  {

    public final String expression;
    public final Consumer<Boolean> extractFunction;

    public BooleanExtractor(String expression, Consumer<Boolean> extractFunction)  {
        this.expression = expression;
        this.extractFunction = extractFunction;
    }

    public void apply(JsonParser parser) throws Exception {
        try {
            while (parser.currentToken() != JsonToken.END_OBJECT) {
                String tok = parser.currentToken().asString();
                if (!parser.currentToken().isStructStart() && parser.getCurrentName().equals(expression)) {
                    extractFunction.accept(parser.nextBooleanValue());
                    return;
                }
                parser.nextToken();
            }
        }
        catch (Exception exp ) {
            throw exp;
        }
        return;
    }



}
