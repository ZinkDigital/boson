package io.boson;
import bsonLib.BsonArray;
import bsonLib.BsonObject;
import io.boson.bson.Boson;
import io.boson.json.Joson;
import org.junit.Test;
import io.boson.bson.bsonValue.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ricardo Martins on 22/01/2018.
 */
public class APItests {

    public String json = "{\"Store\":{\"Book\":[{\"Title\":\"Java\",\"SpecialEditions\":[{\"Title\":\"JavaMachine\",\"Price\":39}],\"Price\":15.5},{\"Title\":\"Scala\",\"Price\":21.5,\"SpecialEditions\":[{\"Title\":\"ScalaMachine\",\"Price\":40}]},{\"Title\":\"C++\",\"Price\":12.6,\"SpecialEditions\":[{\"Title\":\"C++Machine\",\"Price\":38}]}],\"Hat\":[{\"Price\":48,\"Color\":\"Red\"},{\"Price\":35,\"Color\":\"White\"},{\"Price\":38,\"Color\":\"Blue\"}]}}";


    private BsonObject hat3 = new BsonObject().put("Price", 38).put("Color", "Blue");
    private BsonObject hat2 = new BsonObject().put("Price", 35).put("Color", "White");
    private BsonObject hat1 = new BsonObject().put("Price", 48).put("Color", "Red");
    private BsonArray hats = new BsonArray().add(hat1).add(hat2).add(hat3);
    private BsonObject edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38);
    private BsonArray sEditions3= new BsonArray().add(edition3);
    private BsonObject title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3);
    private BsonObject edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40);
    private BsonArray sEditions2 = new BsonArray().add(edition2);
    private BsonObject title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2);
    private BsonObject edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39);
    private BsonArray sEditions1 = new BsonArray().add(edition1);
    private BsonObject title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1);
    private BsonArray books = new BsonArray().add(title1).add(title2).add(title3);
    private BsonObject store = new BsonObject().put("Book", books).put("Hat", hats);
    private BsonObject bson = new BsonObject().put("Store", store);


    @Test
    public void jsonEqualBson() {

        String expression = "Store";

        System.out.println("WORK WITH JOSON\n");
        System.out.println("|-------- Perform Injection --------|\n");
        Joson joson = Joson.injector(expression,  (Map<String, Object> in) -> {
            in.put("WHAT", 10);
            return in;
        });
        CompletableFuture<String> midResult = joson.go(json);
        String result = midResult.join();
        System.out.println("|-------- Perform Extraction --------|\n");
        CompletableFuture<BsValue> future = new CompletableFuture<BsValue>();
        Joson joson1 = Joson.extractor(expression, (BsValue in) -> future.complete(in));
        joson1.go(result);
        Object json1 = future.join().getValue();
        System.out.println(json1);




        System.out.println("WORK WITH BOSON\n");
        System.out.println("|-------- Perform Injection --------|\n");
        Boson boson = Boson.injector(expression,  (Map<String, Object> in) -> {
            in.put("WHAT", 10);
            return in;
        });
        CompletableFuture<byte[]> midResult1 = boson.go(bson.encodeToBarray());
        byte[] result1 = midResult1.join();
        System.out.println("|-------- Perform Extraction --------|\n");
        CompletableFuture<BsValue> future1 = new CompletableFuture<BsValue>();
        Boson boson1 = Boson.extractor(expression, (BsValue in) -> future1.complete(in));
        boson1.go(result1);
        Object bson1 = future1.join().getValue();
        System.out.println(bson1);

        assertEquals(json1, bson1 );


    }


}
