package io.zink.boson.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.*;
import io.zink.boson.Boson;
import scala.Function1;
import scala.Option;
import scala.util.Try;
import scala.util.parsing.json.Parser;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static scala.Option.apply;
import static scala.Option.empty;


public class BosonInjector<T> implements Boson {
    private String expression;
   // private Function<T, T> injectFunction;
    private Function1<T,T> anon;
    //Function1 c = (Function1) injectFunction;
    public BosonInjector(String expression, Function<T, T> injectFunction) {
        this.expression = expression;
        //this.injectFunction = injectFunction;
        this.anon = new Function1<T, T>() {
            @Override
            public T apply(T v1) {
                return injectFunction.apply(v1);
            }
        };
    }

    //private Function<String,BsValue> writer = BsException$.MODULE$::apply;

    private byte[] parseInj(BosonImpl netty, Function1<T,T> injectFunc, String expression){
        DSLParser parser = new DSLParser(expression);
        try{
            Try<MoreKeys1> pr = parser.finalRun();
            if(pr.isSuccess()){
                Interpreter interpreter = new Interpreter(netty, pr.get(), apply(injectFunc),empty());
                return (byte[])interpreter.run();
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
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        Option opt = apply(bsonByteEncoding);
        Option e = empty();
        BosonImpl boson = new BosonImpl(opt, e);
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                    //byte[] res =
                    return parseInj(boson, anon, expression);
//                    try{
//                        switch (res.getClass().getSimpleName()){
//                            case "BsException": return bsonByteEncoding;
//                            case "BsBoson": return ((BsBoson) res).getValue().getByteBuf().array();
//                            default:  return bsonByteEncoding;
//                        }
//
//
//                    }catch(ClassCastException ex){
//                        System.out.println(ex.getMessage());
//                        return bsonByteEncoding;
//                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        Option opt = apply(bsonByteBufferEncoding);
        Option e = empty();
        BosonImpl boson = new BosonImpl(opt, e);
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    byte[] res =  parseInj(boson, anon, expression);
                    ByteBuf b = Unpooled.copiedBuffer(res);
                    return b.nioBuffer();
//                    switch (res.getClass().getSimpleName()){
//                        case "BsException": return bsonByteBufferEncoding;
//                        case "BsBoson": return ((BsBoson) res).getValue().getByteBuf().nioBuffer();
//                        default:  return bsonByteBufferEncoding;
//                    }
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this,boson);
    }



}
