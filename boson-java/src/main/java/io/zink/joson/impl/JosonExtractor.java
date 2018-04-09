//package io.zink.joson.impl;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import de.undercouch.bson4jackson.BsonFactory;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import io.zink.boson.bson.bsonImpl.BosonImpl;
//import io.zink.boson.bson.bsonPath.Interpreter;
//import io.zink.boson.bson.bsonPath.Program;
//import io.zink.boson.bson.bsonPath.TinyLanguage;
//import io.zink.joson.Joson;
//import scala.Option;
//import scala.util.parsing.combinator.Parsers;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//public class JosonExtractor<T> implements Joson {
//
//
//    private String expression;
//    private Consumer<T> extractFunction;
//
//
//    public JosonExtractor(String expression, Consumer<T> extractFunction) {
//        this.expression = expression;
//        this.extractFunction = extractFunction;
//    }
//
//    //private Function<String, BsValue> writer = BsException$.MODULE$::apply;
//
//    private void callParse(BosonImpl boson, String expression){
//        TinyLanguage parser = new TinyLanguage();
//        try{
//            Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
//            if(pr.successful()){
//                Interpreter interpreter = new Interpreter(boson, (Program) pr.get(), Option.empty(), Option.apply(extractFunction));
//                interpreter.run();
//            }else{
//                throw new RuntimeException("Failure/Error parsing!");
//                //return BsObject$.MODULE$.toBson("Failure/Error parsing!", Writes$.MODULE$.apply1(writer));
//            }
//        }catch (RuntimeException e){
//            throw new RuntimeException(e.getMessage());
//            //return BsObject$.MODULE$.toBson(e.getMessage(), Writes$.MODULE$.apply1(writer));
//        }
//    };
//
//    @Override
//    public CompletableFuture<String> go(String jsonStr) {
//
//        JsonObject a = new JsonObject(jsonStr);
//        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(JsonObject.class, new Joson.JsonObjectSerializer());
//        module.addSerializer(JsonArray.class, new Joson.JsonArraySerializer());
//        mapper.registerModule(module);
//        byte[] bsonByteEncoding;
//        CompletableFuture<String> future;
//        try {
//            mapper.writeValue(os, a);
//            bsonByteEncoding = os.toByteArray();
//            os.flush();
//            future =
//                    CompletableFuture.supplyAsync(() -> {
//                        Option<byte[]> opt = Option.apply(bsonByteEncoding);
//                        Option e = Option.empty();
//                        BosonImpl boson = new BosonImpl(opt, e,e,e);
//                        callParse(boson, expression);
//                        //extractFunction.accept((T)value);
//                        return jsonStr;
//                    });
//        }catch (IOException e){
//            System.out.println(e.getMessage());
//            future = new CompletableFuture<>();
//            future.complete(jsonStr);
//
//        }
//        return future;
//    }
//
//    @Override
//    public Joson fuse(Joson joson) {
//        return new JosonFuse(this, joson);
//    }
//}
