package io.boson.json.extractors;



import javax.json.stream.JsonParser;

import static javax.json.stream.JsonParser.Event.START_OBJECT;


/**
 * The is the anonymous Object extractor such as one might find at the start of a json
 * or in an array.
 * @param <T>
 */
public class ObjectExtractor<T>  {

//    public ObjectExtractor(JsonExtractor<T> ext) {
//        super(ext);
//    }
//
//    @Override
//    public Validation<T> apply(JsonParser jsonStream) {
//        JsonParser.Event event = jsonStream.next();
//        Validation<T> validation;
//        if ( event == START_OBJECT ) {
//            validation = getNext().apply(jsonStream);
//        } else {
//            validation = new Trace("Unxpected event " + event.name() + " expecting " + START_OBJECT.name());
//        }
//        return validation;
//    }
}
