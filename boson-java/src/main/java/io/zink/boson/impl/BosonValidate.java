package io.zink.boson.impl;

import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.*;
import io.zink.boson.Boson;
import scala.Function1;
import scala.Option;
import scala.runtime.BoxedUnit;
import scala.util.Try;
import scala.util.parsing.combinator.Parsers;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonValidate<T> implements Boson {

    private String expression;
    private Consumer<T> validateFunction;
    private Function1<T, BoxedUnit> anon;

    public BosonValidate(String expression, Consumer<T> validateFunction) {
        this.expression = expression;
        this.validateFunction = validateFunction;
        this.anon = new Function1<T, BoxedUnit>() {
            @Override
            public BoxedUnit apply(T v1) {
                validateFunction.accept(v1);
                return BoxedUnit.UNIT;
            }
        };
    }

//    private void callParse(BosonImpl boson, String expression){
//        DSLParser parser = new DSLParser(expression);
//        try{
//            Try<ProgStatement> pr = parser.Parse();
//            if(pr.isSuccess()){
//                Interpreter interpreter = new Interpreter<>(boson, pr.get(), Option.empty(), Option.apply(anon));
//                interpreter.run();
//            }else{
//                throw new RuntimeException("Failure/Error parsing!");
//            }
//        }catch (RuntimeException e){
//            throw new RuntimeException(e.getMessage());
//        }
//    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
//            Option<byte[]> opt = Option.apply(bsonByteEncoding);
//            Option e = Option.empty();
//            BosonImpl boson = new BosonImpl(opt, e,e);
//            try {
//                callParse(boson, expression);
//                return bsonByteEncoding;
//            } catch (Exception ex) {
//                validateFunction.accept(null);
//                return null;
//            }
            return bsonByteEncoding;
        });
    }

    @Override
    public CompletableFuture<String> go(String bsonByteBufferEncoding) {
        return CompletableFuture.supplyAsync(() -> {
//            Option<ByteBuffer> opt = Option.apply(bsonByteBufferEncoding);
//            Option e = Option.empty();
//            BosonImpl boson = new BosonImpl(e, opt,e);
//            try {
//                callParse(boson, expression);
//                return bsonByteBufferEncoding;
//            } catch (Exception ex) {
//                validateFunction.accept(null);
//                return null;
//            }
            return bsonByteBufferEncoding;
        });
    }

//    @Override
//    public Boson fuse(Boson boson) {
//        return null;
//    }
}
