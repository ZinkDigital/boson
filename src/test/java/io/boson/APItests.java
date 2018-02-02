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
import java.nio.Buffer;
import java.nio.ByteBuffer;
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
    }   //$..SpecialEditions[?(@.Price)] -> checked

    @Test
    public void ExtractEverywhereArrayPos() {
        String expression = "SpecialEditions[0]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(List(Map(Title -> JavaMachine, Price -> 39)), List(Map(Title -> ScalaMachine, Price -> 40)), List(Map(Title -> C++Machine, Price -> 38)))",
                result.toString());
    }   //$..SpecialEditions[0] -> checked

    @Test
    public void ExtractEverywhereHalfKeyV1() {
        String expression = "*tle";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Java, JavaMachine, Scala, ScalaMachine, C++, C++Machine)",
                result.toString());
    }

    @Test
    public void ExtractEverywhereHalfKeyV2() {
        String expression = "B*k";
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
    }

    @Test
    public void ExtractEverywhereHalfKeyV3() {
        String expression = "Pri*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(15.5, 39, 21.5, 40, 12.6, 38, 48, 35, 38)",
                result.toString());
    }

    @Test
    public void ExtractHalfKeyArrayWithElem2Times() {
        String expression = "*k[@Price].SpecialEditions[@Price]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Title -> JavaMachine, Price -> 39), Map(Title -> ScalaMachine, Price -> 40), Map(Title -> C++Machine, Price -> 38))",
                result.toString());
    }

    @Test
    public void ExtractEverythingOfArrayWithElem() {
        String expression = "SpecialEditions[0 to end].*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(JavaMachine, 39, ScalaMachine, 40, C++Machine, 38)",
                result.toString());
    }

    @Test
    public void ExtractAllTitlesOfArray() {
        String expression = "Book.*..Title";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Java, JavaMachine, Scala, ScalaMachine, C++, C++Machine)",
                result.toString());
    }

    @Test
    public void ExtractArrayLimitFromBook() {
        String expression = "Book[0 to end].*..[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Title -> JavaMachine, Price -> 39), Map(Title -> ScalaMachine, Price -> 40), Map(Title -> C++Machine, Price -> 38))",
                result.toString());
    }

    @Test
    public void ExtractAllElemFromAllElemOfBook() {
        String expression = "Book.*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Java, 15.5, List(Map(Title -> JavaMachine, Price -> 39)), Scala, 21.5, List(Map(Title -> ScalaMachine, Price -> 40)), C++, 12.6, List(Map(Title -> C++Machine, Price -> 38)))",
                result.toString());
    }

    private BsonArray arrEvent = new BsonArray().add("Shouldn't exist").add(bson).add(false).add(new BsonObject().put("Store_1", store));
    private byte[] encodedValidated = arrEvent.encodeToBarray();

    @Test
    public void ExtractPosFromEveryArrayInsideOtherArrayPosEnd() {
        String expression = ".[0 to 2]..[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(encodedValidated);
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(" +
                        "Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39)))," +
                        " Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40)))," +
                        " Map(Title -> C++, Price -> 12.6, SpecialEditions -> List(Map(Title -> C++Machine, Price -> 38)))," +
                        " Map(Title -> JavaMachine, Price -> 39)," +
                        " Map(Title -> ScalaMachine, Price -> 40)," +
                        " Map(Title -> C++Machine, Price -> 38)," +
                        " Map(Price -> 48, Color -> Red)," +
                        " Map(Price -> 35, Color -> White)," +
                        " Map(Price -> 38, Color -> Blue))",
                result.toString());
    }

    @Test
    public void ExtractPosFromEveryArrayInsideOtherArrayPosLimit() {
        System.out.println(arrEvent);
        String expression = ".[0 to 2]..[0 to 1]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(encodedValidated);
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(" +
                        "Map(Title -> Java, Price -> 15.5, SpecialEditions -> List(Map(Title -> JavaMachine, Price -> 39)))," +
                        " Map(Title -> Scala, Price -> 21.5, SpecialEditions -> List(Map(Title -> ScalaMachine, Price -> 40)))," +
                        " Map(Title -> JavaMachine, Price -> 39)," +
                        " Map(Title -> ScalaMachine, Price -> 40)," +
                        " Map(Price -> 48, Color -> Red)," +
                        " Map(Price -> 35, Color -> White))",
                result.toString());
    }   //TODO: ..[#] ain't searching outside limits deeper, it should return the Map(Title -> C++Machine, ...)

    private ByteBuffer validatedByteBuffer = ByteBuffer.allocate(bson.encodeToBarray().length);
    private ByteBuffer b = validatedByteBuffer.put(bson.encodeToBarray());
    private Buffer b1 = validatedByteBuffer.flip();

    @Test
    public void ExtractFromByteBuffer(){
        String expression = "Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(validatedByteBuffer);
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals("Vector(15.5, 39, 21.5, 40, 12.6, 38, 48, 35, 38)", result.toString());
    }

    private BsonArray arr1 = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull().add(new BsonArray().add(100000L))).add(2)
    .add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three"));
    private BsonObject bE = new BsonObject().put("Store",arr1);

    @Test
    public void ExtractArrayWithElemV1() {
        String expression = ".Store[@three]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null))",
                result.toString());
    }

    @Test
    public void ExtractArrayWithElemV2() {
        String expression = ".Store[@one]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null))",
                result.toString());
    }

    @Test
    public void ExtractArrayWithElemV3() {
        String expression = ".Store[@Quantity]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null))",
                result.toString());
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosLimitV1() {
        String expression = ".[0 to 5].[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(null, List(100000))",
                result.toString());
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosLimitV2() {
        String expression = ".[6 to 7].[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector()",
                result.toString());
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosEndV1() {
        String expression = ".[0 to end].[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(null, List(100000))",
                result.toString());
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosEndV2() {
        String expression = ".[6 to end].[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector()",
                result.toString());
    }

    @Test
    public void ExtractPosFromArrayInsideOtherInsideOtherArrayPosLimit() {
        String expression = ".[0 to 5].[0 to 40].[0]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(100000)",
                result.toString());
    }
    @Test
    public void ExtractPosFromArrayInsideOtherInsideOtherArrayPosEnd() {
        String expression = ".[0 to end].[0 to end].[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector(100000)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfArrayRootWithLimit(){
        String expression = ".[0 to 7].*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(null, List(100000), 500, Map(blah -> null), false, Null)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfArrayRootEnd(){
        String expression = ".[0 to end].*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(null, List(100000), 500, Map(blah -> null), false, Null)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootWithLimit(){
        String expression = ".[0 to 7].*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(100000, Null)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootEnd(){
        String expression = ".[0 to end].*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(100000, Null)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootLastPosLimit(){
        System.out.println(arr1);
        String expression = ".[7 to 7].*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(Null)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootLastPosEnd(){
        String expression = ".[7 to end].*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(Null)",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfAllElemsOfArrayRoot(){
        String expression = ".[0 to end].*.*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector()",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfAllElemsOfArrayRootWithOutput(){
        BsonArray _a = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L)
                .add(new BsonArray().addNull().add(new BsonArray().add(100000L).add(new BsonArray().add(true))));
        String expression = ".[0 to end].*.*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(_a.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(true)",
                result.toString());
    }

    @Test
    public void ExtractKeyFromArrayPosEndOfArrayRoot(){
        String expression = ".[0 to end]..Quantity";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(500)",
                result.toString());
    }

    @Test
    public void ExtractObjFromArrayPosLimitOfArrayRoot(){
        String expression = ".[0 to 7]..SomeObj";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(Map(blah -> null))",
                result.toString());
    }

    @Test
    public void ExtractKeyArrayWithElem(){
        String expression = ".Store[@SomeObj]..SomeObj";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(Map(blah -> null))",
                result.toString());
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayWithElem(){
        String expression = ".*.*.*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(100000, Null)",
                result.toString());
    }

    @Test
    public void ExtractKeyArrayWithElemOfArrayRootDontMatch(){
        String expression = ".Store[@Nothing]..SomeObj";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector()",
                result.toString());
    }

    @Test
    public void ExtractBoolean(){
        String expression = "..one";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals("Vector(false)", result.toString());
    }

    @Test
    public void ExtractNull(){
        String expression = "..three";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals("Vector(Null)", result.toString());
    }

    @Test
    public void ExtractArrayPosToEndWithArrayRoot(){
        String expression = ".[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(List(Hat, false, 2.2, null, 1000, List(null, List(100000)), 2, Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null)))",
                result.toString());
    }

    @Test
    public void ExtractArrayPosLimitWithArrayRoot(){
        String expression = ".[0 to 7]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(List(Hat, false, 2.2, null, 1000, List(null, List(100000)), 2, Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null)))",
                result.toString());
    }

    @Test
    public void ExtractArrayLastPosLimitWithArrayRoot(){
        String expression = ".[7 to 7]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(List(Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null)))",
                result.toString());
    }

    @Test
    public void ExtractArrayLastPosEndWithArrayRoot(){
        String expression = ".[7 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(List(Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null)))",
                result.toString());
    }

    @Test
    public void ExtractAllElementsOfArrayRoot(){
        String expression = ".*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector(Hat, false, 2.2, null, 1000, List(null, List(100000)), 2, Map(Quantity -> 500, SomeObj -> Map(blah -> null), one -> false, three -> null))",
                result.toString());
    }






    //---------------------------------------------------------------------------------------//
    //HorribleTests
    private ByteBuffer buffer = ByteBuffer.allocate(0);
    private byte[] byteArr = new byte[10];

    @Test
    public void ExtractWithWrongKeyV1() {
        String expression = ".Something";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector()",
                result.toString());
    }

    @Test
    public void ExtractWithWrongKeyV2() {
        String expression = ".Something[0]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals(
                "Vector()",
                result.toString());
    }

    @Test
    public void ExtractFromEmptyByteBufferZeroAllocate() throws Exception{
        String expression = "Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(buffer);
        Object result = future1.join().getValue();
    }

    @Test
    public void ExtractFromByteBufferSomeAllocate() throws Exception{
        ByteBuffer buf = ByteBuffer.allocate(10);
        buf.put("hi".getBytes());
        String expression = "Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(buf);
        Object result = future1.join().getValue();
    }

    @Test
    public void ExtractFromEmptyByteArray(){
        String expression = "Price";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(byteArr);
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals("Vector()", result.toString());
    }

    @Test
    public void ExtractArrayWhenDontMatch(){
        String expression = ".Book";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals("Vector()", result.toString());
    }

    @Test
    public void ExtractArrayWithLimitWhenDontMatch(){
        String expression = ".Book[0]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals("Vector()", result.toString());
    }

    @Test
    public void ExtractWhenKeyIsInsideKey() {
        BsonObject obj2 = new BsonObject().put("Store", 1000L);
        BsonObject obj1 = new BsonObject().put("Store", obj2);
        String expression = "..Store";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(obj1.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals("Vector(Map(Store -> 1000), 1000)", result.toString());
    }

    @Test
    public void ExtractKeyOfAllElemOfArrayWithLimits() {
        String expression = "..Book[0].*.Title";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());
        Object result = future1.join().getValue();
        System.out.println(result);

        assertEquals("Vector()", result.toString());
    }

    @Test
    public void ExtractKeyofAllElemsOfArrayRootWithLimitAndDontMatch(){
        String expression = ".[0 to 7].*.Nothing";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());
        Object result = future1.join().getValue();

        assertEquals(
                "Vector()",
                result.toString());
    }


}
