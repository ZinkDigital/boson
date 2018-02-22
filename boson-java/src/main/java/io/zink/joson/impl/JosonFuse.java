package io.zink.joson.impl;

import io.zink.joson.Joson;

import java.util.concurrent.CompletableFuture;

public class JosonFuse implements Joson {

    private Joson first;
    private Joson second;

    JosonFuse(Joson first, Joson second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public CompletableFuture<String> go(String jsonStr) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> firstFuture = first.go(jsonStr);
            return second.go(firstFuture.join()).join();
        });
    }

    @Override
    public Joson fuse(Joson joson) {
        return new JosonFuse(this, joson);
    }
}
