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
    private Function1<T,T> anon;

    public BosonInjector(String expression, Function<T, T> injectFunction) {
        this.expression = expression;
        this.anon = new Function1<T, T>() {
            @Override
            public T apply(T v1) {
                return injectFunction.apply(v1);
            }
        };
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        Option opt = apply(bsonByteEncoding);
        Option e = empty();
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                    return bsonByteEncoding;
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        Option opt = apply(bsonByteBufferEncoding);
        Option e = empty();
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    return bsonByteBufferEncoding;
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this,boson);
    }



}
