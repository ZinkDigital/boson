package io.zink.joson.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.Interpreter;
import io.zink.boson.bson.bsonPath.Program;
import io.zink.boson.bson.bsonPath.TinyLanguage;
import io.zink.joson.Joson;
import scala.Option;
import scala.util.parsing.combinator.Parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class JosonValidate<T> implements Joson {

    private String expression;
    private Consumer<T> validateFunction;


    public JosonValidate(String expression, Consumer<T> validateFunction) {
        this.expression = expression;
        this.validateFunction = validateFunction;
    }


    private void callParse(BosonImpl boson, String expression){
        TinyLanguage parser = new TinyLanguage();
        try{
            Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
            if(pr.successful()){
                Interpreter interpreter = new Interpreter(boson, (Program) pr.get(), Option.empty(), Option.apply(validateFunction), Option.empty());
                interpreter.run();
            }else{
                throw new RuntimeException("Failure/Error parsing!");
            }
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public CompletableFuture<String> go(String jsonStr) {
        JsonObject a = new JsonObject(jsonStr);
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonObject.class, new Joson.JsonObjectSerializer());
        module.addSerializer(JsonArray.class, new Joson.JsonArraySerializer());
        mapper.registerModule(module);
        byte[] bsonByteEncoding;
        CompletableFuture<String> future;
        try {
            mapper.writeValue(os, a);
            bsonByteEncoding = os.toByteArray();
            os.flush();
            future =
                    CompletableFuture.supplyAsync(() -> {
                        Option<byte[]> opt = Option.apply(bsonByteEncoding);
                        Option e = Option.empty();
                        BosonImpl boson = new BosonImpl(opt, e,e);
                        callParse(boson, expression);
                        //validateFunction.accept((T)value);
                        return jsonStr;
                    });
        }catch (IOException e){
            System.out.println(e.getMessage());
            future = new CompletableFuture<>();
            future.complete(jsonStr);

        }
        return future;
    }


    @Override
    public Joson fuse(Joson joson) {
        return null;
    }
}
