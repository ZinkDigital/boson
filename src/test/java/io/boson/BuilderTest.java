package io.boson;


import io.boson.json.JsonExtractor;

import io.boson.json.ObjectExtractor;
import org.junit.Test;

import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.StringReader;

import static io.boson.json.JsonExtractorBuilder.Str;
import static org.junit.Assert.assertEquals;


public class BuilderTest {

    /* For example
    {"menu": {
        "id": "file",
                "value": "File",
                "popup": {
            "menuitem": [
                {"value": "New", "onclick": "CreateNewDoc()"},
                {"value": "Open", "onclick": "OpenDoc()"},
                {"value": "Close", "onclick": "CloseDoc()"}
                ]
        }
    }}
    */

    final String json = "{\"value\": 27, \"onclick\": \"CreateNewDoc()\", \"bool\": false }";

    @Test
    public void buildExtractors() {
        JsonParser parser = Json.createParser(new StringReader(json));

        JsonExtractor<String> ext  = new ObjectExtractor<>( Str("onclick") );

        String result = ext.apply(parser).getResult().toString();

        assertEquals(result, "CreateNewDoc()" );

        // to get the OpenDoc() method name
//        Extractor ext = BuildExtractor
//                    .Obj()
//                        .Obj("menu")
//                            .Arr("menuitem")
//                                .Obj()
//                                    .



    }
}
