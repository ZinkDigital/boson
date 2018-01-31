package io.boson.json;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import io.boson.json.extractors.*;
import org.junit.Test;


import java.io.StringReader;

import java.util.function.Consumer;


import static org.junit.Assert.assertTrue;


public class ExtractorTest   {

    final String json = "{\"value\": 27, \"onclick\": \"CreateNewDoc()\", \"bool\": false }";

    // START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY, KEY_NAME, VALUE_STRING,
    // VALUE_NUMBER, VALUE_TRUE, VALUE_FALSE, and VALUE_NULL.

    @Test
    public void extractString() {

       // JsonParser parser = Json.createParser(new StringReader(json));

//        JsonExtractor<String> ext = new ObjectExtractor( new StringExtractor("onclick") );
//
//        String result = ext.apply(parser).getResult().toString();
//
//        assertEquals(result, "CreateNewDoc()" );
    }


    @Test
    public void extractNumber() {

      //  JsonParser parser = Json.createParser(new StringReader(json));

//        JsonExtractor<BigDecimal> ext = new ObjectExtractor<>( new NumberExtractor("value") );
//
//        BigDecimal result = ext.apply(parser).getResult();
//
//        assertEquals(result, new BigDecimal(27) );
    }


    @Test
    public void extractBoolean() throws Exception {

             String source = "{\n" +

                    "  \"firstname\":\"Garrison\",\n" +
                    "  \"lastname\":\"Paul\",\n" +
                    "  \"phone\":847332223,\n" +
                    "  \"boolean\": true,\n" +
                    "   \"address\":[\"Unit - 232\",\"Sofia Streat\",\"Mumbai\"]\n" +
                    "}";

                JsonFactory jsonfactory = new JsonFactory();
                JsonParser parser = jsonfactory.createParser(source);
                parser.nextToken();
                Consumer<Boolean> func = (Boolean b) -> assertTrue(b);

                BooleanExtractor ext = new BooleanExtractor("boolean", func );

                ext.apply(parser);
            }

            
}
