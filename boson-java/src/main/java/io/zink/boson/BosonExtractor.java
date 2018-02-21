package io.zink.boson;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.Interpreter;
import io.zink.boson.bson.bsonPath.Program;
import io.zink.boson.bson.bsonPath.TinyLanguage;
import io.zink.boson.bson.bsonValue.*;
import io.zink.bosonInterface.Boson;
import scala.Function1;
import scala.Option;
import scala.util.parsing.combinator.Parsers;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonExtractor<T> implements Boson {

    private String expression;
    private Consumer<T> extractFunction;


    public BosonExtractor(String expression, Consumer<T> extractFunction) {
        this.expression = expression;
        this.extractFunction = extractFunction;
    }

    private Function<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    private BsValue callParse(BosonImpl boson, String expression){
        TinyLanguage parser = new TinyLanguage();
        try{
         Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
         if(pr.successful()){
             Interpreter interpreter = new Interpreter<>(boson, (Program) pr.get(),Option.empty());
             return interpreter.run();
         }else{
             return BsObject$.MODULE$.toBson("Failure/Error parsing!", Writes$.MODULE$.apply1(writer));
         }
        }catch (RuntimeException e){
            return BsObject$.MODULE$.toBson(e.getMessage(), Writes$.MODULE$.apply1(writer));
        }
    };

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            Option<byte[]> opt = Option.apply(bsonByteEncoding);
            Option e = Option.empty();
            BosonImpl boson = new BosonImpl(opt, e,e);
            BsValue value = callParse(boson, expression);
            extractFunction.accept((T)value);
            return bsonByteEncoding;
        });
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            Option<ByteBuffer> opt = Option.apply(bsonByteBufferEncoding);
            Option e = Option.empty();
            BosonImpl boson = new BosonImpl(e,opt,e);
            BsValue value = callParse(boson, expression);
            extractFunction.accept((T)value);
            return bsonByteBufferEncoding;
        });
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this, boson);
    }
}
