package io.zink.joson;

import io.zink.josonInterface.Joson;

import java.util.concurrent.CompletableFuture;

public class JosonFuse implements Joson {

    Joson first;
    Joson second;

    public JosonFuse(Joson first, Joson second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public CompletableFuture<String> go(String jsonStr) {
     CompletableFuture<String> future =
                CompletableFuture.supplyAsync(() -> {
                    CompletableFuture<String> firstFuture = first.go(jsonStr);
                    return second.go(firstFuture.join()).join();
                });
     return future;
    }

    @Override
    public Joson fuse(Joson joson) {
        return new JosonFuse(this, joson);
    }
}
