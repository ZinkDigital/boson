package io.boson.json;

import io.boson.json.JsonExtractor;
import io.boson.valid.Result;
import io.boson.valid.Trace;
import io.boson.valid.Validation;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import static javax.json.stream.JsonParser.Event.KEY_NAME;


public class BooleanExtractor implements JsonExtractor<Boolean> {

    public final String keyName;

    public BooleanExtractor(String keyName) {
        this.keyName = keyName;
    }

    public Validation<Boolean> apply(JsonParser jsonStream) {

        JsonParser.Event event = jsonStream.next();
        Validation<Boolean> validation;
        if ( event == KEY_NAME ) {
            String key = jsonStream.getString();
            event = jsonStream.next();
            if (key.equals(keyName)) {
                JsonValue val = jsonStream.getValue();
                if (val == JsonValue.TRUE) {
                    validation = new Result<>(Boolean.TRUE);
                } else if (val == JsonValue.FALSE) {
                    validation = new Result<>(Boolean.FALSE);
                } else {
                    validation = new Trace<>("Key " + keyName + " is not of type Boolean");
                }
            } else {
                validation = apply(jsonStream);
            }
        } else {
            validation = new Trace<>("Unexpected event " + event.name() + " expecting " + KEY_NAME.name());
        }

        return validation;
    }



}
