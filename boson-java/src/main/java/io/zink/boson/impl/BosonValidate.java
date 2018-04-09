package io.zink.boson.impl;

import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.*;
import io.zink.boson.Boson;
import scala.Option;
import scala.util.Try;
import scala.util.parsing.combinator.Parsers;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonValidate<T> implements Boson {

    private String expression;
    private Consumer<T> validateFunction;

    public BosonValidate(String expression, Consumer<T> validateFunction) {
        this.expression = expression;
        this.validateFunction = validateFunction;
    }

    //private Function<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    private void callParse(BosonImpl boson, String expression){
        DSLParser parser = new DSLParser(expression);
        try{
            Try<MoreKeys1> pr = parser.finalRun();
            if(pr.isSuccess()){
                Interpreter interpreter = new Interpreter(boson, pr.get(), Option.empty(), Option.apply(validateFunction));
                interpreter.run();
            }else{
                throw new RuntimeException("Failure/Error parsing!");
                //return BsObject$.MODULE$.toBson("Failure/Error parsing!", Writes$.MODULE$.apply1(writer));
            }
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
            //return BsObject$.MODULE$.toBson(e.getMessage(), Writes$.MODULE$.apply1(writer));
        }
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                    Option<byte[]> opt = Option.apply(bsonByteEncoding);
                    Option e = Option.empty();
                    BosonImpl boson = new BosonImpl(opt, e);
                    callParse(boson, expression);
                    //validateFunction.accept((T)value);
                    return bsonByteEncoding;
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    Option<ByteBuffer> opt = Option.apply(bsonByteBufferEncoding);
                    Option e = Option.empty();
                    BosonImpl boson = new BosonImpl(e,opt);
                    callParse(boson, expression);
                    //validateFunction.accept((T)value);
                    return bsonByteBufferEncoding;
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return null;
    }
}
