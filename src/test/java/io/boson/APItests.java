package io.boson;
import bsonLib.BsonArray;
import bsonLib.BsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.boson.bson.Boson;
import io.boson.bson.bsonValue.BsSeq;
import io.boson.bson.bsonValue.BsValue;
import io.boson.json.Joson;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;


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
    public void test() throws IOException {

    }

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
        CompletableFuture<BsValue> future = new CompletableFuture<>();
        Joson joson1 = Joson.extractor(expression, future::complete);
        joson1.go(result);
        Object json1 = future.join().getValue();
        System.out.println(json1);




        System.out.println("WORK WITH BOSON\n");
        System.out.println("|-------- Perform Injection --------|\n");
        Boson boson = Boson.injector(expression, (Map<String, Object> in) -> {
            in.put("WHAT", 10);
            return in;
        });
        CompletableFuture<byte[]> midResult1 = boson.go(bson.encodeToBarray());
        byte[] result1 = midResult1.join();

        System.out.println("|-------- Perform Extraction --------|\n");
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson1 = Boson.extractor(expression, future1::complete);
        boson1.go(result1);
        Object bson1 = future1.join().getValue();
        System.out.println(bson1);

        assertEquals(json1, bson1 );
    }

    @Test
    public void ExtractFromArrayPos() {
        String expression = ".Store.Book[1 to 2]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(List(Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40))), Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38)))))",
                result.toString());
    }   //$.Store.Book[1:2] -> checked

    @Test
    public void ExtractFromArrayPosWithEnd() {
        String expression = ".Store.Book[1 until end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(List(Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40)))))",
                result.toString());
    }   //$.Store.Book[0:2] -> checked

    @Test
    public void ExtractKeyFromArrayPosWithEnd() {
        String expression = ".Store.Book[1 until end].Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(21.5)",
                result.toString());
    }   //$.Store.Book[:].Price -> checked

    @Test
    public void ExtractFromArrayWithElem2Times() {
        String expression = ".Store.Book[@Price].SpecialEditions[@Title]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Title -> JavaMachine, Price -> 39)," +
                        " Map(Title -> ScalaMachine, Price -> 40)," +
                        " Map(Title -> C++Machine, Price -> 38))",
                result.toString());
    }   //$.Store.Book[?(@.Price)].SpecialEditions[?(@.Title)] -> checked

    @Test
    public void ExtractFromArrayWithElem() {
        String expression = ".Store.Book[@SpecialEditions]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39))), Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40))), Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38))))",
                result.toString());
    }   //$.Store.Book[?(@.SpecialEditions)]

    @Test
    public void ExtractKeyFromArrayWithElem() {
        String expression = ".Store.Book[@Price].Title";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Java, Scala, C++)",
                result.toString());
    } //$.Store.Book[?(@.Price)].Title -> checked

    @Test
    public void ExtractFromArrayWithElemAndArrayPos() {
        String expression = ".Store.Book[@SpecialEditions].SpecialEditions[0]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(List(Map(Title -> JavaMachine, Price -> 39)), List(Map(Title -> ScalaMachine, Price -> 40)), List(Map(Title -> C++Machine, Price -> 38)))",
                result.toString());
    }

    @Test
    public void ExtractFromArrayPosAndArrayWithElem() {
        String expression = ".Store.Book[0 until 1].SpecialEditions[@Price]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Title -> JavaMachine, Price -> 39))",
                result.toString());
    }

    @Test
    public void ExtractEverythingFromRoot() {
        String expression = ".*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Book -> List(Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39))), Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40))), Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38))))," +
                        " Hat -> List(Map(Price -> 48, Color -> Red), Map(Price -> 35, Color -> White), Map(Price -> 38, Color -> Blue))))",
                result.toString());
    }   //$.* -> checked

    @Test
    public void ExtractEntireArray() {
        String expression = ".Store.Book";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(List(Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39)))," +
                        " Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40)))," +
                        " Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38)))))",
                result.toString());
    }   //$.Store.Book -> checked

    @Test
    public void ExtractAllPrices() {
        String expression = "Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(15.5, 39, 21.5, 40, 12.6, 38, 48, 35, 38)",
                result.toString());
    }   //$..Price -> checked

    @Test
    public void ExtractAllBookPrices() {
        String expression = "Book..Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(15.5, 39, 21.5, 40, 12.6, 38)",
                result.toString());
    }   //$.Book..Price -> checked

    @Test
    public void ExtractTitlesOfBooks() {
        String expression = "Book.Title";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Java, Scala, C++)",
                result.toString());
    }   //$..Book[:].Title -> checked

    @Test
    public void ExtractKeyEverywhereArrayWithElem() {
        String expression = "SpecialEditions[@Price].Title";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(JavaMachine, ScalaMachine, C++Machine)",
                    result.toString());
        }   //$..SpecialEditions[?(@.Price)].Title -> checked

    @Test
    public void ExtractEverywhereArrayWithElem() {
        String expression = "SpecialEditions[@Price]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Title -> JavaMachine, Price -> 39), Map(Title -> ScalaMachine, Price -> 40), Map(Title -> C++Machine, Price -> 38))",
                result.toString());
    }

}
