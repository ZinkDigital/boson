package io.boson.json;

import bsonLib.BsonObject;


import io.boson.bson.Boson;
import io.boson.bson.bsonValue.BsSeq;
import io.boson.bson.bsonValue.BsValue;
import org.junit.Test;
import scala.collection.Seq;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


import java.util.ArrayList;
import java.util.List;
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
        io.boson.bson.Boson boson = io.boson.bson.Boson.extractor(expression, name  -> {
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

    @Test
    public void simpleInjector() throws Exception {

        BsonObject  bson = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the");
        String newFridgeSerialCode = " what?";


        byte[] validBsonArray = bson.encodeToBarray();

        String expression = "string.first";

        Boson boson = Boson.injector(expression, (String in) -> in.concat(newFridgeSerialCode));

        CompletableFuture<byte[]> result = boson.go(validBsonArray);

        // apply an extractor to get the new serial code as above.
        byte[] resultValue =  result.join();



        CompletableFuture<BsValue> future = new CompletableFuture<BsValue>();
        Boson boson1 = Boson.extractor(expression, future::complete);
        boson1.go(resultValue);

        String str = "the what?";
        BsSeq bs = (BsSeq)future.join();
        byte[] b = (byte[])bs.getValue().head();
        assertEquals(str, new String(b) );
    }

}
