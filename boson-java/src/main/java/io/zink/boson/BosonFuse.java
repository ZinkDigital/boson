package io.zink.boson;

import io.zink.bosonInterface.Boson;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class BosonFuse implements Boson {
    Boson first;
    Boson second;

    public BosonFuse(Boson first, Boson second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                   CompletableFuture<byte[]> firstFuture = first.go(bsonByteEncoding);
                   return second.go(firstFuture.join()).join();
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    CompletableFuture<ByteBuffer> firstFuture = first.go(bsonByteBufferEncoding);
                    return second.go(firstFuture.join()).join();
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this,boson);
    }
}
