package io.zink.boson.impl;

import io.zink.boson.Boson;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class BosonFuse implements Boson {
    private Boson first;
    private Boson second;

    BosonFuse(Boson first, Boson second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
           CompletableFuture<byte[]> firstFuture = first.go(bsonByteEncoding);
           return second.go(firstFuture.join()).join();
        });
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<ByteBuffer> firstFuture = first.go(bsonByteBufferEncoding);
            return second.go(firstFuture.join()).join();
        });
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this,boson);
    }
}
