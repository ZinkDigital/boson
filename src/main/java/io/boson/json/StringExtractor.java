package io.boson.json;

import io.boson.valid.Result;
import io.boson.valid.Trace;
import io.boson.valid.Validation;


import javax.json.stream.JsonParser;

import static javax.json.stream.JsonParser.Event.KEY_NAME;


public class StringExtractor implements JsonExtractor<String> {

    public final String keyName;

    public StringExtractor(String keyName) {
        this.keyName = keyName;
    }

    public Validation<String> apply(JsonParser jsonStream) {

        JsonParser.Event event = jsonStream.next();
        Validation<String> validation;
        if ( event == KEY_NAME ) {
            String key = jsonStream.getString();
            event = jsonStream.next();
            if (key.equals(keyName)) {
                // TODO - check the value is a string value
                validation = new Result<String>(jsonStream.getString());
            } else {
                validation = apply(jsonStream);
            }
        } else {
            validation = new Trace<String>("Unexpected event " + event.name() + " expecting " + KEY_NAME.name());
        }

        return validation;
    }



}
