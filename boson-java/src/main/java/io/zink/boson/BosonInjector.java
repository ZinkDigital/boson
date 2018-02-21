package io.zink.boson;

import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.Interpreter;
import io.zink.boson.bson.bsonPath.Program;
import io.zink.boson.bson.bsonPath.TinyLanguage;
import io.zink.boson.bson.bsonValue.*;
import io.zink.bosonInterface.Boson;
import scala.Option;
import scala.util.parsing.json.Parser;
import scala.compat.java8.FunctionConverters.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import static scala.Option.*;

public class BosonInjector<T> implements Boson {
    private String expression;
    private Function<T, T> injectFunction;

    public BosonInjector(String expression, Function<T, T> injectFunction) {
        this.expression = expression;
        this.injectFunction = injectFunction;
    }
    private Function<String,BsValue> writer = BsException$.MODULE$::apply;

    private BsValue parseInj(BosonImpl netty, Function<T,T> injectFunc, String expression){
        TinyLanguage parser = new TinyLanguage();
        try{
            Parser.ParseResult pr = parser.parseAll(parser.program(), expression);
            if(pr.successful()){
                Interpreter interpreter = new Interpreter(netty, (Program) pr.get(), apply(injectFunc));
                BsValue res = interpreter.run();

                return res;
            }else{
                return BsObject$.MODULE$.toBson("Error inside interpreter.run() ", Writes$.MODULE$.apply1(writer));
            }
        }catch (RuntimeException e){
            return BsObject$.MODULE$.toBson(e.getMessage(), Writes$.MODULE$.apply1(writer));
        }
    };

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        Option opt = apply(bsonByteEncoding);
        Option e = empty();
        BosonImpl boson = new BosonImpl(opt, e, e);
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                    BsValue res =  parseInj(boson, injectFunction, expression);
                    try{
                        switch (res.getClass().getSimpleName()){
                            case "BsException": return bsonByteEncoding;
                            case "BsBoson": return ((BsBoson) res).getValue().getByteBuf().array();
                            default:  return bsonByteEncoding;
                        }


                    }catch(ClassCastException ex){
                        System.out.println(ex.getMessage());
                        return bsonByteEncoding;
                    }
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        Option opt = apply(bsonByteBufferEncoding);
        Option e = empty();
        BosonImpl boson = new BosonImpl(opt, e, e);
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    BsValue res =  parseInj(boson, injectFunction, expression);
                    switch (res.getClass().getSimpleName()){
                        case "BsException": return bsonByteBufferEncoding;
                        case "BsBoson": return ((BsBoson) res).getValue().getByteBuf().nioBuffer();
                        default:  return bsonByteBufferEncoding;
                    }
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this,boson);
    }
}
