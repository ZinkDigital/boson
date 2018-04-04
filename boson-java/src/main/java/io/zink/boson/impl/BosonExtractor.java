package io.zink.boson.impl;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.Interpreter;
import io.zink.boson.bson.bsonPath.Program;
import io.zink.boson.bson.bsonPath.TinyLanguage;
//import io.zink.boson.bson.bsonValue.BsException$;
//import io.zink.boson.bson.bsonValue.BsObject$;
//import io.zink.boson.bson.bsonValue.BsValue;
//import io.zink.boson.bson.bsonValue.Writes$;
import io.zink.boson.Boson;

import scala.Function1;
import scala.Option;
import scala.Unit;
import scala.runtime.BoxedUnit;
import scala.util.parsing.combinator.Parsers;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonExtractor<T> implements Boson {

    private String expression;
    private Function1<T,BoxedUnit> anon;


    public BosonExtractor(String expression, Consumer<T> extractFunction) {
        this.expression = expression;
        this.anon = new Function1<T, BoxedUnit>(){
            @Override
            public BoxedUnit apply(T v1) {
                extractFunction.accept(v1);
                return BoxedUnit.UNIT;
            }
        };

    }


    //private Function<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    private void callParse(BosonImpl boson, String expression){
        TinyLanguage parser = new TinyLanguage();
        try{
         Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
         if(pr.successful()){
             Interpreter interpreter = new Interpreter<T>(boson, (Program) pr.get(),Option.empty(), Option.apply(anon));
             interpreter.run();
         }else{
             throw new RuntimeException("Failure/Error parsing!");
         }
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            Option<byte[]> opt = Option.apply(bsonByteEncoding);
            Option e = Option.empty();
            BosonImpl boson = new BosonImpl(opt, e,e);
            callParse(boson, expression);
            //extractFunction.accept((T)value);
            return bsonByteEncoding;
        });
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            Option<ByteBuffer> opt = Option.apply(bsonByteBufferEncoding);
            Option e = Option.empty();
            BosonImpl boson = new BosonImpl(e,opt,e);
            callParse(boson, expression);
            //extractFunction.accept((T)value);
            return bsonByteBufferEncoding;
        });
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this, boson);
    }
}
