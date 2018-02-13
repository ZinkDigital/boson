package io.boson;
import bsonLib.BsonArray;
import bsonLib.BsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.boson.bson.Boson;
import io.boson.bson.bsonValue.BsValue;
import io.boson.json.Joson;
import org.junit.Test;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import scala.collection.JavaConverters;
import scala.collection.immutable.Vector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
    public void ExtractFromArrayPos() {
        String expression = ".Store.Book[1 to 2]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(title2.encodeToBarray());
        expected.add(title3.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }   //$.Store.Book[1:2] -> checked

    @Test
    public void ExtractFromArrayPosWithEnd() {
        String expression = ".Store.Book[1 until end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(title2.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }   //$.Store.Book[?(@.Price)].SpecialEditions[?(@.Title)] -> checked

    @Test
    public void ExtractFromArrayWithElem() {
        String expression = ".Store.Book[@SpecialEditions]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(title1.encodeToBarray());
        expected.add(title2.encodeToBarray());
        expected.add(title3.encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractFromArrayPosAndArrayWithElem() {
        String expression = ".Store.Book[0 until 1].SpecialEditions[@Price]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractEverythingFromRoot() {
        String expression = ".*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(store.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }   //$.* -> checked

    @Test
    public void ExtractEntireArray() {
        String expression = ".Store.Book";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(books.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());

        System.out.println(result);

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }   //$..SpecialEditions[?(@.Price)] -> checked

    @Test
    public void ExtractEverywhereArrayPos() {
        String expression = "SpecialEditions[0]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(books.encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractAllElemFromAllElemOfBook() {
        String expression = "Book.*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bson.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Java");
        expected.add(15.5);
        expected.add(sEditions1.encodeToBarray());
        expected.add("Scala");
        expected.add(21.5);
        expected.add(sEditions2.encodeToBarray());
        expected.add("C++");
        expected.add(12.6);
        expected.add(sEditions3.encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));

//        assertEquals(
//                "Vector(Java, 15.5, List(Map(Title -> JavaMachine, Price -> 39)), Scala, 21.5, List(Map(Title -> ScalaMachine, Price -> 40)), C++, 12.6, List(Map(Title -> C++Machine, Price -> 38)))",
//                result.toString());
        }
    }

    private BsonArray arrEvent = new BsonArray().add("Shouldn't exist").add(bson).add(false).add(new BsonObject().put("Store_1", store));
    private byte[] encodedValidated = arrEvent.encodeToBarray();

    @Test
    public void ExtractPosFromEveryArrayInsideOtherArrayPosEnd() {
        System.out.println(arrEvent);
        String expression = ".[0 to 2]..[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(encodedValidated);

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(title1.encodeToBarray());
        expected.add(title2.encodeToBarray());
        expected.add(title3.encodeToBarray());
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(edition3.encodeToBarray());
        expected.add(hat1.encodeToBarray());
        expected.add(hat2.encodeToBarray());
        expected.add(hat3.encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractPosFromEveryArrayInsideOtherArrayPosLimit() {
        String expression = ".[0 to 2]..[0 to 1]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(encodedValidated);

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(title1.encodeToBarray());
        expected.add(title2.encodeToBarray());
        expected.add(edition1.encodeToBarray());
        expected.add(edition2.encodeToBarray());
        expected.add(hat1.encodeToBarray());
        expected.add(hat2.encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());


        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractArrayWithElemV2() {
        String expression = ".Store[@one]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());


        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractArrayWithElemV3() {
        String expression = ".Store[@Quantity]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());


        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosLimitV1() {
        String expression = ".[0 to 5].[0 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Null");
        expected.add(new BsonArray().add(100000L).encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Null");
        expected.add(new BsonArray().add(100000L).encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Null");
        expected.add(new BsonArray().add(100000L).encodeToBarray());
        expected.add(500L);
        expected.add(new BsonObject().putNull("blah").encodeToBarray());
        expected.add(false);
        expected.add("Null");

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractAllElemsOfArrayRootEnd(){
        String expression = ".[0 to end].*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Null");
        expected.add(new BsonArray().add(100000L).encodeToBarray());
        expected.add(500L);
        expected.add(new BsonObject().putNull("blah").encodeToBarray());
        expected.add(false);
        expected.add("Null");

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootWithLimit(){
        String expression = ".[0 to 7].*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(100000L);
        expected.add("Null");

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().putNull("blah").encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractKeyArrayWithElem(){
        String expression = ".Store[@SomeObj]..SomeObj";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().putNull("blah").encodeToBarray());

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else if(result.get(i)==null && expected.get(i)==null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayWithElem(){
        String expression = ".*.*.*.*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(bE.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(100000L);
        expected.add("Null");

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Hat");
        expected.add(false);
        expected.add(2.2);
        expected.add("Null");
        expected.add(1000L);
        expected.add(new BsonArray().addNull().add(new BsonArray().add(100000L)).encodeToBarray());
        expected.add(2);
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractArrayPosLimitWithArrayRoot(){
        String expression = ".[0 to 7]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Hat");
        expected.add(false);
        expected.add(2.2);
        expected.add("Null");
        expected.add(1000L);
        expected.add(new BsonArray().addNull().add(new BsonArray().add(100000L)).encodeToBarray());
        expected.add(2);
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractArrayLastPosLimitWithArrayRoot(){
        String expression = ".[7 to 7]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractArrayLastPosEndWithArrayRoot(){
        String expression = ".[7 to end]";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
    }

    @Test
    public void ExtractAllElementsOfArrayRoot(){
        String expression = ".*";
        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, future1::complete);
        boson.go(arr1.encodeToBarray());

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>) future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add("Hat");
        expected.add(false);
        expected.add(2.2);
        expected.add("Null");
        expected.add(1000L);
        expected.add(new BsonArray().addNull().add(new BsonArray().add(100000L)).encodeToBarray());
        expected.add(2);
        expected.add(new BsonObject().put("Quantity",500L).put("SomeObj",new BsonObject().putNull("blah")).put("one",false).putNull("three").encodeToBarray());

        assert (result.size() == expected.size());
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i), (byte[]) expected.get(i)));
            else if (result.get(i) == null && expected.get(i) == null) assertTrue(true);
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
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

        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
        List<Object> result = scala.collection.JavaConverters.seqAsJavaList(res);
        List<Object> expected = new ArrayList<>();
        expected.add(obj2.encodeToBarray());
        expected.add(1000L);

        assert(result.size() == expected.size());
        for(int i =0;i<result.size();i++) {
            if(result.get(i) instanceof Double) assertTrue(result.get(i) == expected.get(i));
            else if(result.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                assertTrue(Arrays.equals((byte[]) result.get(i),(byte[])expected.get(i)));
            else assertTrue(result.get(i).equals(expected.get(i)));
        }
        //assertEquals("Vector(Map(Store -> 1000), 1000)", result.toString());
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

// INJECTOR TESTS

    private BsonObject spirit = new BsonObject().put("name", "SpiritualDrink");
    private BsonObject wine = new BsonObject().put("name", "Wine");
    private BsonObject beer = new BsonObject().put("name", "Beer");
    private BsonArray alcoholic = new BsonArray().add(beer).add(wine).add(spirit);
    private BsonObject water = new BsonObject().put("name", "Water");
    private BsonObject sumol = new BsonObject().put("name", "Sumol");
    private BsonObject coca = new BsonObject().put("name", "Coca-Cola");
    private BsonArray nonAlcoholic = new BsonArray().add(coca).add(sumol).add(water);
    private BsonObject drinks = new BsonObject().put("Non-Alcoholic", nonAlcoholic).put("Alcoholic", alcoholic);
    private BsonObject menu3 = new BsonObject().put("name", "Menu3");
    private BsonObject menu2 = new BsonObject().put("name", "Menu2");
    private BsonObject menu1 = new BsonObject().put("name", "Menu1");
    private BsonArray menus = new BsonArray().add(menu1).add(menu2).add(menu3);
    private BsonObject natura = new BsonObject().put("name", "Natura");
    private BsonObject specialHD = new BsonObject().put("name", "Special");
    private BsonObject normal = new BsonObject().put("name", "Normal");
    private BsonArray hotdogs = new BsonArray().add(normal).add(specialHD).add(natura);
    private BsonObject chicken = new BsonObject().put("name", "Chicken");
    private BsonObject specialS = new BsonObject().put("name", "Special");
    private BsonObject mix = new BsonObject().put("name", "Mix");
    private BsonArray sandwichs = new BsonArray().add(mix).add(specialS).add(chicken);
    private BsonObject food = new BsonObject().put("Sandwichs", sandwichs ).put("HotDogs", hotdogs).put("Menus", menus);
    private BsonObject coffee = new BsonObject().put("Food", food).put("Drinks", drinks).put("TakeAway?", true);
    private BsonObject pinball = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 0.5f);
    private BsonObject kingkong = new BsonObject().put("#players", 2).put("TeamGame?", false).put("cost", 0.5f);
    private BsonObject soccer = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 0.5f);
    private BsonObject arcadeGames = new BsonObject().put("Pinball", pinball).put("KingKong", kingkong).put("Soccer", soccer);
    private BsonObject peixinho = new BsonObject().put("#players", 8).put("TeamGame?", false).put("cost", 2.0f);
    private BsonObject italiana = new BsonObject().put("#players", 5).put("TeamGame?", true).put("cost", 2.0f);
    private BsonObject sueca = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 2.0f);
    private BsonObject cardGames = new BsonObject().put("Sueca", sueca).put("Italiana", italiana).put("Peixinho", peixinho);
    private BsonObject trivialPursuit = new BsonObject().put("#players", 4).put("TeamGame?", false).put("cost", 3.0f);
    private BsonObject mysterium = new BsonObject().put("#players", 8).put("TeamGame?", true).put("cost", 3.0f);
    private BsonObject monopoly = new BsonObject().put("#players", 6).put("TeamGame?", false).put("cost", 3.0f);
    private BsonObject boardGames = new BsonObject().put("Monopoly", monopoly).put("Mysterium", mysterium).put("TrivialPursuit", trivialPursuit);
    private BsonObject pingpong = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 2.5f);
    private BsonObject slippers = new BsonObject().put("#players", 2).put("TeamGame?", false).put("cost", 2.5f);
    private BsonObject snooker = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 2.5f);
    private BsonObject tableGames = new BsonObject().put("Snooker", snooker).put("Slippers", slippers).put("PingPong", pingpong);
    private BsonObject gameRoom = new BsonObject().put("TableGame", tableGames).put("BoardGames", boardGames).put("ArcadeGames", arcadeGames).put("CardGames", cardGames);
    private BsonObject magazine3 = new BsonObject().put("Title", "C++Magazine").put("Price", 9.99).put("InStock", 15L);
    private BsonObject magazine2 = new BsonObject().put("Title", "JavaMagazine").put("Price", 4.99).put("InStock", 10L);
    private BsonObject magazine1 = new BsonObject().put("Title", "ScalaMagazine").put("Price", 1.99).put("InStock", 5L);
    private BsonArray magazines = new BsonArray().add(magazine1).add(magazine2).add(magazine3);
    private BsonObject article3 = new BsonObject().put("Title", "C++Article").put("Price", 29.99).put("available", true );
    private BsonObject article2 = new BsonObject().put("Title", "JavaArticle").put("Price", 24.99).put("available", true );
    private  BsonObject article1 = new BsonObject().put("Title", "ScalaArticle").put("Price", 19.99).put("available", true );
    private  BsonArray articles = new BsonArray().add(article1).add(article2).add(article3);
    private  BsonObject book3 = new BsonObject().put("Title", "C++").put("Price", 29.99).put("InStock", 15);
    private  BsonObject book2 = new BsonObject().put("Title", "Java").put("Price", 24.99).put("InStock", 10);
    private BsonObject book1 = new BsonObject().put("Title", "Scala").put("Price", 19.99).put("InStock", 5);
    private  BsonArray books1 = new BsonArray().add(book1).add(book2).add(book3);
    private  BsonObject library = new BsonObject().put("Books", books1).put("Articles", articles).put("Magazines", magazines);
    private  BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
    private  BsonObject root = new BsonObject().put("Store", services);

/*
    * KEY
    */

    @Test
    public void KEY_test_V1() {
        BsonArray services = new BsonArray();
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = ".Store";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bl) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V2() {
        BsonArray services = new BsonArray();
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = "..Store";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bl) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V3() {
        BsonObject root = new BsonObject().put("Store", 15);
        BsonObject rootx = new BsonObject().put("Store", 16);
        String expression = "..Store";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+1);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V4() {
        BsonObject library = new BsonObject();
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = ".Store.[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bm) -> new BsonObject().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V5() {
        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = ".Store.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V6() {
        BsonObject library = new BsonObject();
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = "..Store.[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bm) -> new BsonObject().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V7() {
        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = "..Store.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V8() {
        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = ".Stre.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V9() {
        BsonObject rootx = new BsonObject().put("Store", 11L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = ".Store";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V10() {
        BsonObject rootx = new BsonObject().put("Store", 11.0f);
        BsonObject root =  new BsonObject().put("Store", 10.0f);
        String expression = ".Store";
        Boson bosonInjector = Boson.injector(expression,  (Float value) -> value+1.0f);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V11() {
        BsonObject rootx = new BsonObject().put("Store", "array!!");
        BsonObject root =  new BsonObject().put("Store", "array");
        String expression = ".Store";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> {
            String str = new String(value);
            String newStr = str.concat("!!");
            return newStr.getBytes();
        });
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V12() {
        Instant ins = Instant.now();
        BsonObject rootx = new BsonObject().put("Store", ins.plusMillis(1000));
        BsonObject root =  new BsonObject().put("Store", ins);
        String expression = ".Store";
        Boson bosonInjector = Boson.injector(expression,  (Instant value) -> value.plusMillis(1000));
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V13() {
        BsonObject rootx = new BsonObject().put("Store", 1000).putNull("null");
        BsonObject root =  new BsonObject().put("Store",  1000).putNull("null");
        String expression = ".null";
        Boson bosonInjector = Boson.injector(expression,  (Instant value) -> value.plusMillis(1000));
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * HALFNAME
    */
    @Test
    public void HALFNAME_test_V1() {
        BsonArray services = new BsonArray();
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = ".Sto*";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bl) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V2() {
        BsonArray services = new BsonArray();
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = "..Sto*";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bl) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V3() {
        BsonObject root = new BsonObject().put("Store", 15);
        BsonObject rootx = new BsonObject().put("Store", 16);
        String expression = "..S*e";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+1);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V4() {
        BsonObject library = new BsonObject();
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = ".Sto*.[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bm) -> new BsonObject().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V5() {
        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = ".*tore.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V6() {
        BsonObject library = new BsonObject();
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", services);
        String expression = "..S*ore.[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bm) -> new BsonObject().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V7() {
        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = "..St*re.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V8() {
        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = ".Str*.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V9() {
        BsonObject rootx = new BsonObject().put("LisbonGarden", 11L);
        BsonObject root =  new BsonObject().put("LisbonGarden", 10L);
        String expression = ".L*sb*nG*den";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V10() {
        BsonObject rootx = new BsonObject().put("LisbonGarden", 10L);
        BsonObject root =  new BsonObject().put("LisbonGarden", 10L);
        String expression = ".4*b*nG*den";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * ROOT
    */

    @Test
    public void ROOT_test_V1() {
        BsonObject rootx = new BsonObject();
        BsonObject root =  new BsonObject().put("Store", 10L);
        String expression = ".";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * HASELEM
    */

    @Test
    public void HASELEM_test_V1() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V2() {
        BsonObject servicesx = new BsonObject().put("Empty",new BsonObject()).put("Games",gameRoom).put("Coffee", coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonObject root = new BsonObject().put("Store", servicesx);
        String expression = ".Store[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V3() {
        BsonObject servicesx = new BsonObject().put("Empty",new BsonObject()).put("Games",gameRoom).put("Coffee", coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonObject root = new BsonObject().put("Store", servicesx);
        String expression = "..Store[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V4() {
        BsonObject servicesx = new BsonObject().put("Empty",new BsonObject()).put("Games",gameRoom).put("Coffee", coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonObject root = new BsonObject().put("Store", servicesx);
        String expression = ".Stoe[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V5() {

        BsonObject rootx = new BsonObject().put("Store", 10L);
        BsonObject root = new BsonObject().put("Store", 10L);
        String expression = "..Stoe[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V6() {
        BsonObject books = new BsonObject().put("#1", 10L).put("#2", true).putNull("#3").put("#4", 10.2f).put("#5", 10).put("#6", "six");
        BsonArray servicesx = new BsonArray().add(books).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonObject root = new BsonObject().put("Store", servicesx);
        String expression = "..Store[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V7() {
        BsonObject library = new BsonObject();
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = "..Store[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V8() {
        BsonObject library = new BsonObject().put("Books", new BsonArray()).put("Articles", articles).put("Magazines", magazines);
        BsonArray servicesx = new BsonArray().add(10).add(10.0f).add("10").add(true).add(library).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonArray services = new BsonArray().add(10).add(10.0f).add("10").add(true).add(library).add(coffee);
        BsonObject root = new BsonObject().put("Store", services);
        String expression = "..Store[@Books].Books";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonArray().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HASELEM_test_V9() {
        BsonObject library = new BsonObject().put("Books", new BsonArray()).put("Articles", articles).put("Magazines", magazines);
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store[@Books].Books";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonArray().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * ARR
    */

    @Test
    public void ARR_test_V1() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V2() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V3() {
        BsonArray books = new BsonArray();
        BsonObject library = new BsonObject().put("Books", books).put("Articles", articles).put("Magazines", magazines);
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[0 to end].Books";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonArray().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V4() {
        BsonArray books = new BsonArray();
        BsonObject library = new BsonObject().put("Books", books).put("Articles", articles).put("Magazines", magazines);
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[0 to end].Books";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonArray().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V5() {
        BsonObject tableGames = new BsonObject();
        BsonObject gameRoom = new BsonObject().put("TableGame", tableGames).put("BoardGames", boardGames).put("ArcadeGames", arcadeGames).put("CardGames", cardGames);
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[1 to end].TableGame";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V6() {
        BsonObject tableGames = new BsonObject();
        BsonObject gameRoom = new BsonObject().put("TableGame", tableGames).put("BoardGames", boardGames).put("ArcadeGames", arcadeGames).put("CardGames", cardGames);
        BsonArray servicesx = new BsonArray().add(10).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonArray services = new BsonArray().add(10).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store", services);
        String expression = ".Store.[1 to end].TableGame";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V7() {
        BsonObject tableGames = new BsonObject();
        BsonObject gameRoom = new BsonObject().put("TableGame", tableGames).put("BoardGames", boardGames).put("ArcadeGames", arcadeGames).put("CardGames", cardGames);
        BsonArray servicesx = new BsonArray().add(10).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        BsonArray services = new BsonArray().add(10).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store", services);
        String expression = ".Store.[0 to end].TableGame";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V8() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[0 until end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V9() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[0 to 2]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V10() {
        BsonArray servicesx = new BsonArray().add(library).add(new BsonObject()).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[1]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V11() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[0 to 2]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V12() {
        BsonArray alcoholic = new BsonArray().add(beer).add(10).add(spirit);
        BsonArray nonAlcoholic = new BsonArray().add(coca).add(10).add(water);
        BsonObject drinks = new BsonObject().put("Non-Alcoholic", nonAlcoholic).put("Alcoholic", alcoholic);
        BsonArray menus = new BsonArray().add(menu1).add(10).add(menu3);
        BsonArray hotdogs = new BsonArray().add(normal).add(10).add(natura);
        BsonArray sandwichs = new BsonArray().add(mix).add(10).add(chicken);
        BsonObject food = new BsonObject().put("Sandwichs", sandwichs ).put("HotDogs", hotdogs).put("Menus", menus);
        BsonObject coffee = new BsonObject().put("Food", food).put("Drinks", drinks).put("TakeAway?", true);
        BsonObject magazine3 = new BsonObject().put("Title", "C++Magazine").put("Price", 9.99).put("InStock", 15L);
        BsonObject magazine1 = new BsonObject().put("Title", "ScalaMagazine").put("Price", 1.99).put("InStock", 5L);
        BsonArray magazines = new BsonArray().add(magazine1).add(10).add(magazine3);
        BsonObject article3 = new BsonObject().put("Title", "C++Article").put("Price", 29.99).put("available", true );
        BsonObject article1 = new BsonObject().put("Title", "ScalaArticle").put("Price", 19.99).put("available", true );
        BsonArray articles = new BsonArray().add(article1).add(10).add(article3);
        BsonObject book3 = new BsonObject().put("Title", "C++").put("Price", 29.99).put("InStock", 15);
        BsonObject book1 = new BsonObject().put("Title", "Scala").put("Price", 19.99).put("InStock", 5);
        BsonArray books = new BsonArray().add(book1).add(10).add(book3);
        BsonObject library = new BsonObject().put("Books", books).put("Articles", articles).put("Magazines", magazines);
        BsonArray services = new BsonArray().add(library).add(10).add(coffee);
        BsonObject root = new BsonObject().put("Store", services);

        BsonArray alcoholicx = new BsonArray().add(beer).add(11).add(spirit);
        BsonArray nonAlcoholicx = new BsonArray().add(coca).add(11).add(water);
        BsonObject drinksx = new BsonObject().put("Non-Alcoholic", nonAlcoholicx).put("Alcoholic", alcoholicx);
        BsonArray menusx = new BsonArray().add(menu1).add(11).add(menu3);
        BsonArray hotdogsx = new BsonArray().add(normal).add(11).add(natura);
        BsonArray sandwichsx = new BsonArray().add(mix).add(11).add(chicken);
        BsonObject foodx = new BsonObject().put("Sandwichs", sandwichsx ).put("HotDogs", hotdogsx).put("Menus", menusx);
        BsonObject coffeex = new BsonObject().put("Food", foodx).put("Drinks", drinksx).put("TakeAway?", true);
        BsonObject magazine3x = new BsonObject().put("Title", "C++Magazine").put("Price", 9.99).put("InStock", 15L);
        BsonObject magazine1x = new BsonObject().put("Title", "ScalaMagazine").put("Price", 1.99).put("InStock", 5L);
        BsonArray magazinesx = new BsonArray().add(magazine1x).add(11).add(magazine3x);
        BsonObject article3x = new BsonObject().put("Title", "C++Article").put("Price", 29.99).put("available", true );
        BsonObject article1x = new BsonObject().put("Title", "ScalaArticle").put("Price", 19.99).put("available", true );
        BsonArray articlesx = new BsonArray().add(article1x).add(11).add(article3x);
        BsonObject book3x = new BsonObject().put("Title", "C++").put("Price", 29.99).put("InStock", 15);
        BsonObject book1x = new BsonObject().put("Title", "Scala").put("Price", 19.99).put("InStock", 5);
        BsonArray booksx = new BsonArray().add(book1x).add(11).add(book3x);
        BsonObject libraryx = new BsonObject().put("Books", booksx).put("Articles", articlesx).put("Magazines", magazinesx);
        BsonArray servicesx = new BsonArray().add(libraryx).add(11).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store", servicesx);

        String expression = ".Store..[1]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+1 );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V13() {
        BsonArray servicesx = new BsonArray().add(library).add(new BsonObject()).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[1]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V14() {
        BsonArray services = new BsonArray().add(10).add(10).add(10);
        BsonObject root = new BsonObject().put("Store", services);
        BsonArray servicesx = new BsonArray().add(11).add(11).add(11);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+1  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V15() {
        BsonArray services = new BsonArray().add(10).add(10).add(10);
        BsonObject root = new BsonObject().put("Store", services);
        BsonArray servicesx = new BsonArray().add(10).add(10).add(10);
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[1 to end].track";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+1  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V16() {
        BsonArray services = new BsonArray().add(10).add(10).add(new BsonObject().put("track", false));
        BsonObject root = new BsonObject().put("Store", services);
        BsonArray servicesx = new BsonArray().add(10).add(10).add(new BsonObject().put("track", true));
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store..[1 to 2].track";
        Boson bosonInjector = Boson.injector(expression,  (Boolean value) -> !value  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V17() {
        BsonArray services = new BsonArray().add(10).add(10).add(new BsonObject().put("track", false));
        BsonObject root = new BsonObject().put("Store", services);
        BsonArray servicesx = new BsonArray().add(10).add(10).add(new BsonObject().put("track", true));
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[1 to 2].track";
        Boson bosonInjector = Boson.injector(expression,  (Boolean value) -> !value  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V18() {

        BsonObject root = new BsonObject().put("Store", new BsonObject().put("track", true));

        BsonObject rootx = new BsonObject().put("Store", new BsonObject().put("track", true));
        String expression = ".Store.[1 to 2].track";
        Boson bosonInjector = Boson.injector(expression,  (Boolean value) -> !value  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V19() {
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V20() {
        BsonObject lib = new BsonObject().put("Lib","Closed");
        BsonArray services = new BsonArray().add(lib).add(lib).add(lib);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject libx = new BsonObject().put("Lib","Closed");
        BsonArray servicesx = new BsonArray().add(libx).add(libx).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V21() {
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(100).add(1000);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[end]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value*10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V22() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(100).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject telex = new BsonObject().put("product", "radio");
        BsonArray servicesx = new BsonArray().add(100).add(100).add(telex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[end].product";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "radio"  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V23() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(100).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject telex = new BsonObject().put("product", "radio");
        BsonArray servicesx = new BsonArray().add(100).add(100).add(telex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[end].product";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "radio"  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V24() {
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(1000).add(1000).add(1000);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[all]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value*10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V25_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject telex = new BsonObject().put("product", "radio");
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[end]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V26_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject telex = new BsonObject().put("product", "radio");
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[end]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V27_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject telex = new BsonObject().put("product", "radio");
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[end]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V28_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject telex = new BsonObject().put("product", "radio");
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[end]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V29_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V30_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V31_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V32_exception() {
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V33_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 to 2]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V34_exception() {
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 to 2]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V35_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to 2]";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V36_exception() {
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to 2]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V37_exception() {
        BsonObject tele = new BsonObject().put("product", "television");
        BsonArray services = new BsonArray().add(100).add(tele).add(tele).add(tele);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(tele).add(tele).add(tele);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 until 3].product";
        Boson bosonInjector = Boson.injector(expression,  (Integer value) -> value+10  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V38_exception() {
        BsonArray services = new BsonArray().add(100).add(100).add(100);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(100).add(100).add(100);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[1 to 2].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V39_exception() {
        BsonObject menu1 = new BsonObject().put("product", "10");
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu);
        BsonObject game1 = new BsonObject().put("product", 10);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games);
        BsonObject book1 = new BsonObject().put("product", 10);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books);
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", "10");
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux);
        BsonObject game1x = new BsonObject().put("product", 10);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx);
        BsonObject book1x = new BsonObject().put("product", 10);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx);
        BsonArray servicesx = new BsonArray().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[end].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V40_exception() {
        BsonObject menu1 = new BsonObject().put("product", "10");
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "10");
        BsonObject game1 = new BsonObject().put("product", 10);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games);
        BsonObject book1 = new BsonObject().put("product", 10);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books);
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", "10");
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "10");
        BsonObject game1x = new BsonObject().put("product", 10);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx);
        BsonObject book1x = new BsonObject().put("product", 10);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx);
        BsonArray servicesx = new BsonArray().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[end].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V41_exception() {
        BsonObject menu1 = new BsonObject().put("product", 10.0);
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "aaaa");
        BsonObject game1 = new BsonObject().put("product", 10.0);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games).put("product", "aaaa");
        BsonObject book1 = new BsonObject().put("product", 10.0);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books).put("product", "aaaa");
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", 10.0);
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "aaaa");
        BsonObject game1x = new BsonObject().put("product", 10.0);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx).put("product", "aaaa");
        BsonObject book1x = new BsonObject().put("product", 10.0);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx).put("product", "aaaa");
        BsonArray servicesx = new BsonArray().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to end].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V42_exception() {
        BsonObject menu1 = new BsonObject().put("product", 10.0);
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "10");
        BsonObject game1 = new BsonObject().put("product", 10.0);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games).put("product", "10");
        BsonObject book1 = new BsonObject().put("product", 10.0);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books).put("product", "10");
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", 10.0);
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "10");
        BsonObject game1x = new BsonObject().put("product", 10.0);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx).put("product", "10");
        BsonObject book1x = new BsonObject().put("product", 10.0);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx).put("product", "10");
        BsonArray servicesx = new BsonArray().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 to end].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V43_exception() {
        BsonObject menu1 = new BsonObject().put("product", 10.0);
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "aaaa");
        BsonObject game1 = new BsonObject().put("product", 10.0);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games).put("product", "aaaa");
        BsonObject book1 = new BsonObject().put("product", 10.0);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books).put("product", "aaaa");
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", 10.0);
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "aaaa");
        BsonObject game1x = new BsonObject().put("product", 10.0);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx).put("product", "aaaa");
        BsonObject book1x = new BsonObject().put("product", 10.0);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx).put("product", "aaaa");
        BsonArray servicesx = new BsonArray().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to 2].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V44_exception() {
        BsonObject menu1 = new BsonObject().put("product", 10.0);
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "10");
        BsonObject game1 = new BsonObject().put("product", 10.0);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games).put("product", "10");
        BsonObject book1 = new BsonObject().put("product", 10.0);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books).put("product", "10");
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", 10.0);
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "10");
        BsonObject game1x = new BsonObject().put("product", 10.0);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx).put("product", "10");
        BsonObject book1x = new BsonObject().put("product", 10.0);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx).put("product", "10");
        BsonArray servicesx = new BsonArray().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 to 2].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V45_exception() {
        BsonObject menu1 = new BsonObject().put("product", 10.0);
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "10");
        BsonObject game1 = new BsonObject().put("product", 10.0);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games).put("product", "10");
        BsonObject book1 = new BsonObject().put("product", 10.0);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books).put("product", "10");
        BsonArray services = new BsonArray().add(10).add(12.0f).add(true).add("string").add(10L).addNull().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", 10.0);
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "10");
        BsonObject game1x = new BsonObject().put("product", 10.0);
        BsonArray gamesx = new BsonArray().add(game1x);
        BsonObject gameRoomx = new BsonObject().put("games", gamesx).put("product", "10");
        BsonObject book1x = new BsonObject().put("product", 10.0);
        BsonArray booksx = new BsonArray().add(book1x);
        BsonObject libraryx = new BsonObject().put("books", booksx).put("product", "10");
        BsonArray servicesx = new BsonArray().add(10).add(12.0f).add(true).add("string").add(10L).addNull().add(libraryx).add(gameRoomx).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store..[0 to 8].product";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+10.0  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void ARR_test_V46() {
        BsonObject menu1 = new BsonObject().put("product", 10.0);
        BsonArray menu = new BsonArray().add(menu1);
        BsonObject coffee = new BsonObject().put("Coffee", menu).put("product", "10");
        BsonObject game1 = new BsonObject().put("product", 10.0);
        BsonArray games = new BsonArray().add(game1);
        BsonObject gameRoom = new BsonObject().put("games", games).put("product", "10");
        BsonObject book1 = new BsonObject().put("product", 10.0);
        BsonArray books = new BsonArray().add(book1);
        BsonObject library = new BsonObject().put("books", books).put("product", "10");
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store",services);

        BsonObject menu1x = new BsonObject().put("product", 10.0);
        BsonArray menux = new BsonArray().add(menu1x);
        BsonObject coffeex = new BsonObject().put("Coffee", menux).put("product", "10");
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(coffeex);
        BsonObject rootx = new BsonObject().put("Store",servicesx);

        String expression = ".Store.[0 until 2]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * KEYWITHARR
    */

    @Test
    public void KEYWARR_test_V1() {
        BsonObject coffee = new BsonObject();
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V2() {
        BsonObject library = new BsonObject();
        BsonArray servicesx = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[first]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V3() {
        BsonObject coffee = new BsonObject();
        BsonArray servicesx = new BsonArray().add(coffee).add(coffee).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[all]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V4() {
        BsonObject coffeex = new BsonObject();
        BsonArray servicesx = new BsonArray().add(coffeex).add(coffeex).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[0 until 2]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V5() {
        BsonObject coffeex = new BsonObject();
        BsonArray servicesx = new BsonArray().add(coffeex).add(coffeex).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[0 until end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V6() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V7() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Store[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V8() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Store[0 until 1]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V9() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add(library).add(library).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Store[end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonObject().encodeToBarray()  );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V10() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "none").put("Articles", "alot").put("Magazines", "none");
        BsonArray servicesx = new BsonArray().add(library).add(library).add(libraryx);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Store[end].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V11() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "none").put("Articles", "alot").put("Magazines", "none");
        BsonArray servicesx = new BsonArray().add(library).add(library).add(libraryx);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Store[2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V12() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "none").put("Articles", "alot").put("Magazines", "none");
        BsonArray servicesx = new BsonArray().add(libraryx).add(libraryx).add(library);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Store[0 until 2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V13() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "none").put("Articles", "alot").put("Magazines", "none");
        BsonArray servicesx = new BsonArray().add(library).add(library).add(libraryx);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V14() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "none").put("Articles", "alot").put("Magazines", "none");
        BsonArray servicesx = new BsonArray().add(library).add(library).add(libraryx);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[end].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V15() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray services = new BsonArray().add(library).add(library).add(library);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "none").put("Articles", "alot").put("Magazines", "none");
        BsonArray servicesx = new BsonArray().add(libraryx).add(libraryx).add(library);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[0 until 2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V16() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonObject root = new BsonObject().put("Store",library);
        BsonObject libraryx = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonObject rootx = new BsonObject().put("Store",libraryx);
        String expression = ".Store[0 until 2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V17() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonObject root = new BsonObject().put("Store",library);
        BsonObject libraryx = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonObject rootx = new BsonObject().put("Store",libraryx);
        String expression = "..Store[0 until 2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V18() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonObject root = new BsonObject().put("Store",library);
        BsonObject libraryx = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonObject rootx = new BsonObject().put("Store",libraryx);
        String expression = ".Stre[0 until 2].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V19() {
        BsonObject library = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray shelf = new BsonArray().add(library);
        BsonArray services = new BsonArray().add(shelf).add(shelf).add(shelf);
        BsonObject root = new BsonObject().put("Store",services);
        BsonObject libraryx = new BsonObject().put("Books", "alot").put("Articles", "alot").put("Magazines", "none");
        BsonArray shelfx = new BsonArray().add(libraryx);
        BsonArray servicesx = new BsonArray().add(shelfx).add(shelfx).add(shelfx);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = "..Stor[end].Books";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V20() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("none").add(true).add(10L).add(10.0f);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[0]";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "none");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V21() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx =new BsonArray().add("none").add(true).add(10L).add(10.0f);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[0]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> "none".getBytes());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V22() {
        Instant ins = Instant.now();
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(ins);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(10L).add(ins.plusMillis(1000));
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[3]";
        Boson bosonInjector = Boson.injector(expression,  (Instant value) -> value.plusMillis(1000));
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V23() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(10L).add(11.0f);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[3]";
        Boson bosonInjector = Boson.injector(expression,  (Float value) -> value+1.0f);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V24() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(10L).add(11.0);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[3]";
        Boson bosonInjector = Boson.injector(expression,  (Double value) -> value+1.0);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V25() {
        BsonArray array = new BsonArray().add("smth");
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(array);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(10L).add(new BsonArray());
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[3]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] value) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V26() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(false).add(10L).add(10.0f);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[1]";
        Boson bosonInjector = Boson.injector(expression,  (Boolean value) -> !value);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V27() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f);
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(11L).add(10.0f);
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[2]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V28() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f).addNull();
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(10L).add(10.0f).addNull();
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[4]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEYWARR_test_V29() {
        BsonArray services = new BsonArray().add("str").add(true).add(10L).add(10.0f).addNull();
        BsonObject root = new BsonObject().put("Store",services);
        BsonArray servicesx = new BsonArray().add("str").add(true).add(10L).add(10.0f).addNull();
        BsonObject rootx = new BsonObject().put("Store",servicesx);
        String expression = ".Store[3]";
        Boson bosonInjector = Boson.injector(expression,  (String value) -> "error");
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
}
