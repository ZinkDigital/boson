package io.zink.joson.impl;

import com.fasterxml.jackson.databind.JsonNode;
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
import scala.util.parsing.json.Parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class JosonInjector<T> implements Joson {

    private String expression;
    private Function<T,T> injectFunction;


    public JosonInjector(String expression, Function<T, T> injectFunction) {
        this.expression = expression;
        this.injectFunction = injectFunction;
    }


    //private Function<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    private byte[] parseInj(BosonImpl netty, Function injectFunc, String expression){
        TinyLanguage parser = new TinyLanguage();
        try{
            Parser.ParseResult pr = parser.parseAll(parser.program(), expression);
            if(pr.successful()){
                Interpreter interpreter = new Interpreter(netty, (Program) pr.get(), Option.apply(injectFunc), Option.empty(),Option.empty());
                return interpreter.run();
            }else{
                throw new RuntimeException("Error inside interpreter.run() ");
                //return BsObject$.MODULE$.toBson("Error inside interpreter.run() ", Writes$.MODULE$.apply1(writer));
            }
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
            //return BsObject$.MODULE$.toBson(e.getMessage(), Writes$.MODULE$.apply1(writer));
        }
    }



    @Override
    public CompletableFuture<String> go(String jsonStr) {
        JsonObject a = new JsonObject();
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonObject.class,new Joson.JsonObjectSerializer());
        module.addSerializer(JsonArray.class, new Joson.JsonArraySerializer());
        mapper.registerModule(module);
        CompletableFuture<String> future;
        try {
            mapper.writeValue(os, a);
            byte[] bsonByteEncoding = os.toByteArray();
            os.flush();
            Option opt = Option.apply(bsonByteEncoding);
            Option e = Option.empty();
            BosonImpl boson = new BosonImpl(opt, e, e);
            future =
                    CompletableFuture.supplyAsync(() -> {
                        byte[] res =  parseInj(boson, injectFunction, expression);
                        try {
                            JsonNode s = mapper.readTree(res);
                            return s.toString();
                        }catch(IOException ex){
                            System.out.println(ex.getMessage());
                            return jsonStr;
                        }
//                        switch (res.getClass().getSimpleName()){
//                            case "BsException": return jsonStr;
//                            case "BsBoson":
//
//                            default:  return jsonStr;
//                        }
                    });
        }catch(IOException e){
            System.out.println(e.getMessage());
            future = new CompletableFuture<>();
            future.complete(jsonStr);
        }
        return future;
    }

    @Override
    public Joson fuse(Joson joson) {
        return new JosonFuse(this, joson);
    }
}
