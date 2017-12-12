package io.boson;


import io.boson.json.extractors.*;
import org.junit.Test;

import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class ExtractorTest   {

    final String json = "{\"value\": 27, \"onclick\": \"CreateNewDoc()\", \"bool\": false }";

    // START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY, KEY_NAME, VALUE_STRING,
    // VALUE_NUMBER, VALUE_TRUE, VALUE_FALSE, and VALUE_NULL.

    @Test
    public void extractString() {

        JsonParser parser = Json.createParser(new StringReader(json));

//        JsonExtractor<String> ext = new ObjectExtractor( new StringExtractor("onclick") );
//
//        String result = ext.apply(parser).getResult().toString();
//
//        assertEquals(result, "CreateNewDoc()" );
    }


    @Test
    public void extractNumber() {

        JsonParser parser = Json.createParser(new StringReader(json));

//        JsonExtractor<BigDecimal> ext = new ObjectExtractor<>( new NumberExtractor("value") );
//
//        BigDecimal result = ext.apply(parser).getResult();
//
//        assertEquals(result, new BigDecimal(27) );
    }


    @Test
    public void extractBoolean() {

        JsonParser parser = Json.createParser(new StringReader(json));

//        JsonExtractor<Boolean> ext = new ObjectExtractor<>( new BooleanExtractor("bool") );
//
//        Boolean result = ext.apply(parser).getResult();
//
//        assertEquals(result, Boolean.FALSE );
    }


}
