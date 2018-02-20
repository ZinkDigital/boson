package io.zink.boson;

import bsonLib.BsonObject;

import io.zink.bosonInterface.Boson;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BosonTest {

    private final String key = "Name";
    private final String value = "José";
    private BsonObject obj1 = new BsonObject().put(key,value);


    @Test
    public void simplestPossibleExtractor() throws Exception {

        String expression = key;

        CountDownLatch latch = new CountDownLatch(1);
        // Java / Scala name collision we need to work this out a bit.
        Boson boson = Boson.extractor(expression, name  -> {
            // check that the name is correctly extracted
            assertEquals(value, name);
            // click the latch so that it doesnt block
            latch.countDown();
        } );

        // boson is set up so now we could loop over various values of incoming encoded bson
        // for the moment lets just do one
        final byte [] bsonEncodedBytes = obj1.encode().getBytes();
 //  TODO  ->    CompletableFuture<byte[]> fut = boson.go(bsonEncodedBytes);

        // wait for the extractor to be called so the at the check is made
 // TODO ->  latch.await();

        // and wait for the future to complete with an unmodified byte array
 // TODO ->   assertArrayEquals( bsonEncodedBytes,  fut.get(1, TimeUnit.SECONDS) );
    }


}
