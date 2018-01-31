package io.boson.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.boson.json.extractors.BooleanExtractor;

import java.io.IOException;
import java.util.function.Consumer;

public class Test {

    static String source = "{\n" +

            "  \"firstname\":\"Garrison\",\n" +
            "  \"lastname\":\"Paul\",\n" +
            "  \"phone\":847332223,\n" +
            "  \"boolean\": true,\n" +
            "   \"address\":[\"Unit - 232\",\"Sofia Streat\",\"Mumbai\"]\n" +
            "}";


        public static void main(String args[]) throws Exception {

            System.out.println("Parsing JSON file by using Jackson Streaming API");
            // parseJSON();
            System.out.println("done");

            JsonFactory jsonfactory = new JsonFactory();
            JsonParser parser = jsonfactory.createJsonParser(source);
            Consumer<Boolean> func = (Boolean b) -> System.out.println("bingo " + b);

            BooleanExtractor ext = new BooleanExtractor("boolean", func );
            parser.nextToken();
            ext.apply(parser);
        }


        /*
         * This method parse JSON String by using Jackson Streaming API example.
         */
        public static void parseJSON() {
            try {
                JsonFactory jsonfactory = new JsonFactory();

                JsonParser parser = jsonfactory.createJsonParser(source);

                // starting parsing of JSON String
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String token = parser.getCurrentName();

                    if ("firstname".equals(token)) {
                        parser.nextToken();  //next token contains value
                        String fname = parser.getText();  //getting text field
                        System.out.println("firstname : " + fname);

                    }

                    if ("lastname".equals(token)) {
                        parser.nextToken();
                        String lname = parser.getText();
                        System.out.println("lastname : " + lname);

                    }

                    if ("phone".equals(token)) {
                        parser.nextToken();
                        int phone = parser.getIntValue();  // getting numeric field
                        System.out.println("phone : " + phone);

                    }

                    System.out.println( parser.getCurrentLocation() );

                    if ("address".equals(token)) {
                        System.out.println("address :");
                        parser.nextToken(); // next token will be '[' which means JSON array

                        // parse tokens until you find ']'
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            System.out.println(parser.getText());
                        }
                    }
                }
                parser.close();

            } catch (JsonGenerationException jge) {
                jge.printStackTrace();
            } catch (JsonMappingException jme) {
                jme.printStackTrace();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
}
