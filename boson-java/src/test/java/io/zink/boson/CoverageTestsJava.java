package io.zink.boson;

import bsonLib.BsonArray;
import bsonLib.BsonObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoverageTestsJava {
    private final BsonObject johnDoeBson = new BsonObject().put("name", "John Doe").put("age", 21);
    private final BsonObject janeDoeBson = new BsonObject().put("name", "Jane Doe").put("age", 12);
    private final BsonArray personsArray = new BsonArray().add(johnDoeBson).add(janeDoeBson);
    private final BsonObject personsBson = new BsonObject().put("persons", personsArray);
    private final BsonObject clientBson = new BsonObject().put("client", personsBson);
    private final byte[] bsonEncoded = clientBson.encodeToBarray();

    private final BsonArray bsonHuman = new BsonArray().add("person1").add("person2").add("person3");
    private final BsonObject bsonObjArray = new BsonObject().put("person", bsonHuman);
    private final byte[] bsonObjArrayEncoded = bsonObjArray.encodeToBarray();

    private final BsonArray bsonAlien = new BsonArray().add("et").add("predator").add("alien");
    private final BsonObject bsonObjArray1 = new BsonObject().put("alien", bsonAlien);

    private final BsonObject bsonEvent = new BsonObject().put("person", bsonHuman).put("alien", bsonAlien);
    private final BsonObject bsonSpeciesObj = new BsonObject().put("species", bsonEvent);
    private final byte[] bsonSpeciesEncoded = bsonSpeciesObj.encodeToBarray();

    private BsonObject book = new BsonObject().put("name", "Title1").put("pages", 1);
    private BsonObject bsonBook = new BsonObject().put("book", book);

    private BsonObject book2 = new BsonObject().put("name", "Some book").put("pages", 123);
    private BsonObject bsonBook2 = new BsonObject().put("book", book2);

    private BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2);
    private BsonObject store = new BsonObject().put("books", books);
    private BsonObject storeBson = new BsonObject().put("store", store);


    private final BsonObject expected = new BsonObject().put("name", "Title1").put("pages", 101);
    private final BsonObject bsonBookExpected = new BsonObject().put("book", expected);

    private final BsonObject expected2 = new BsonObject().put("name", "Some book").put("pages", 223);
    private final BsonObject bsonBook2Expected = new BsonObject().put("book", expected2);

    private final BsonArray booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected);
    private final BsonObject storeExpected = new BsonObject().put("books", booksExpected);
    private final BsonObject storeBsonExpected = new BsonObject().put("store", storeExpected);


    private final BsonObject bookAux = new BsonObject().put("pages", 1.0).put("someLong", 1L).put("someBoolean", true);
    private final BsonObject bookAux2 = new BsonObject().put("pages", 23.10).put("someLong", 100000L).put("someBoolean", false);
    private final BsonObject bookAux3 = new BsonObject().put("pages", -3.0).put("someLong", 789456L).put("someBoolean", true);

    private final BsonObject bsonBookAux = new BsonObject().put("book", bookAux);
    private final BsonObject bsonBookAux2 = new BsonObject().put("book", bookAux2);
    private final BsonObject bsonBookAux3 = new BsonObject().put("book", bookAux3);

    private final BsonArray booksAux = new BsonArray().add(bsonBookAux).add(bsonBookAux2).add(bsonBookAux3);
    private final BsonObject storeAux = new BsonObject().put("books", booksAux);
    private final BsonObject storeBsonAux = new BsonObject().put("store", storeAux);

    private final BsonObject nestedAuthor = new BsonObject().put("firstName", "John").put("lastName", "Doe").put("age", 21);
    private final BsonObject nestedBook = new BsonObject().put("name", "Some Book").put("pages", 100).put("author", nestedAuthor);
    private final BsonObject nestedBson = new BsonObject().put("book", nestedBook);

    private final BsonObject nestedAuthor2 = new BsonObject().put("firstName", "Jane").put("lastName", "Doe").put("age", 12);
    private final BsonObject nestedBook2 = new BsonObject().put("name", "A Title").put("pages", 999).put("author", nestedAuthor2);
    private final BsonObject nestedBson2 = new BsonObject().put("book", nestedBook2);

    private final BsonObject nestedAuthorExpected = new BsonObject().put("firstName", "JOHN").put("lastName", "DOE").put("age", 41);
    private final BsonObject nestedBookExpected = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthorExpected);
    private final BsonObject nestedBsonExpected = new BsonObject().put("book", nestedBookExpected);

    private final BsonObject nestedAuthor2Expected = new BsonObject().put("firstName", "JANE").put("lastName", "DOE").put("age", 32);
    private final BsonObject nestedBook2Expected = new BsonObject().put("name", "A TITLE").put("pages", 1099).put("author", nestedAuthor2Expected);
    private final BsonObject nestedBson2Expected = new BsonObject().put("book", nestedBook2Expected);


    public String json = "{\"Store\":{\"Book\":[{\"Title\":\"Java\",\"SpecialEditions\":[{\"Title\":\"JavaMachine\",\"Price\":39}],\"Price\":15.5},{\"Title\":\"Scala\",\"Price\":21.5,\"SpecialEditions\":[{\"Title\":\"ScalaMachine\",\"Price\":40}]},{\"Title\":\"C++\",\"Price\":12.6,\"SpecialEditions\":[{\"Title\":\"C++Machine\",\"Price\":38}]}],\"Hat\":[{\"Price\":48,\"Color\":\"Red\"},{\"Price\":35,\"Color\":\"White\"},{\"Price\":38,\"Color\":\"Blue\"}]}}";


    private BsonObject hat3 = new BsonObject().put("Price", 38).put("Color", "Blue");
    private BsonObject hat2 = new BsonObject().put("Price", 35).put("Color", "White");
    private BsonObject hat1 = new BsonObject().put("Price", 48).put("Color", "Red");
    private BsonArray hats = new BsonArray().add(hat1).add(hat2).add(hat3);
    private BsonObject edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38);
    private BsonArray sEditions3 = new BsonArray().add(edition3);
    private BsonObject title3ApiTests = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3);
    private BsonObject edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40);
    private BsonArray sEditions2 = new BsonArray().add(edition2);
    private BsonObject title2ApiTests = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2);
    private BsonObject edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39);
    private BsonArray sEditions1 = new BsonArray().add(edition1);
    private BsonObject title1ApiTests = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1);
    private BsonArray booksApiTests = new BsonArray().add(title1ApiTests).add(title2ApiTests).add(title3ApiTests);
    private BsonObject storeApiTests = new BsonObject().put("Book", booksApiTests).put("Hat", hats);
    private BsonObject bsonApiTests = new BsonObject().put("Store", storeApiTests);

    /**
     * Private method to display all elements inside a byte array
     *
     * @param arr byte array to be displayed
     */
    private void printArray(byte[] arr) {
        for (byte b : arr) {
            System.out.print(b + " ");
        }
    }

    /**
     * Private method to simulate a contains slice for an integer
     *
     * @param arr           byte array in which to look for the slice
     * @param integerToFind the integer to look for, the start of the slice of interest
     * @return a boolean saying rather this byte array contains the slice of interest or not
     */
    private boolean containsInteger(byte[] arr, int integerToFind) {
        boolean found = false;
        int counter = 0;
        while (counter < arr.length && !found) {
            if (((int) arr[counter]) == integerToFind &&
                    ((int) arr[counter + 1]) == 0 &&
                    ((int) arr[counter + 2]) == 0 &&
                    ((int) arr[counter + 3]) == 0) {
                found = true;
            } else {
                counter++;
            }
        }
        return found;
    }

    @Test
    public void rootInjection() {
        BsonObject bson = new BsonObject().put("name", "John Doe");
        String ex = ".";
        Boson boson = Boson.injector(ex, (byte[] in) -> {
            return new String(in).toUpperCase().getBytes();
        });
        byte[] bsonEncoded = bson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (new String(resultValue).contains("JOHN DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void rootModification() {
        BsonObject bson = new BsonObject().put("name", "John Doe");
        String ex = ".";
        Boson boson = Boson.injector(ex, (byte[] in) -> {
            return new String(in).replace("John Doe", "Jane Doe").getBytes();
        });
        byte[] bsonEncoded = bson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (new String(resultValue).contains("Jane Doe") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void topLevelKeyInj() {
        String ex = ".name";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = johnDoeBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (new String(resultValue).contains("JOHN DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void nestedKeyInj() {
        BsonObject bson = new BsonObject().put("person", johnDoeBson);
        String ex = ".person.name";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (new String(resultValue).contains("JOHN DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void nestedMultiKeyInj() {
        BsonObject person = new BsonObject().put("person", johnDoeBson);
        BsonObject bson = new BsonObject().put("client", person);
        String ex = ".client.person.name";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (new String(resultValue).contains("JOHN DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void nestedMultiKeyInj_Integer() {
        BsonObject person = new BsonObject().put("person", johnDoeBson);
        BsonObject bson = new BsonObject().put("client", person);
        String ex = ".client.person.age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
            //  Subtracting 22 here to give us -1 so we can assert that we find this value in the resultValue bellow
            //  without it being from another source (like the size of an object)
        });
        byte[] bsonEncoded = bson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void halfNameInj() {
        String ex = ".n*";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = johnDoeBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (new String(resultValue).contains("JOHN DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void hasElemInj_String() {
        String ex = ".persons[@name]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = personsBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("JOHN DOE") && resultString.contains("JANE DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void hasElemInj_Integer() {
        String ex = ".persons[@age]";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        byte[] bsonEncoded = personsBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void hasElemInj_DoubleDot() {
        String ex = "..persons[@age]";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        byte[] bsonEncoded = personsBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void hasElemInjMultiKey_DoubleDot() {
        String ex = "..client..persons[@age]";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void keyWithArrExpr0to1() {
        String ex = ".person[0 to 1]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    @Test
    public void keyWithArrExpr0until1() {
        String ex = ".person[0 until 1]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("person2") && resultString.contains("person3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    @Test
    public void keyWithArrExpr1toEnd() {
        String ex = ".person[1 to end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("PERSON2") && resultString.contains("PERSON3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    @Test
    public void keyWithArrExpr1untilEnd() {
        String ex = ".person[1 until end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    @Test
    public void keyWithArrExprEnd() {
        String ex = ".person[end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("person2") && resultString.contains("PERSON3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    public void keyWithArrExprAll() {
        String ex = ".person[all]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("PERSON3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    public void keyWithArrExprfirst() {
        String ex = ".person[first]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("person2") && resultString.contains("person3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    public void keyWithArrExpr1() {
        String ex = ".person[1]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonObjArrayEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonObjArrayEncoded.length);
        }).join();
    }

    public void ArrExpr0to1() {
        String ex = ".[0 to 1]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void ArrExpr0toEnd() {
        String ex = ".[0 to end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("PERSON3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void ArrExpr0Until1() {
        String ex = ".[0 until 1]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("person2") && resultString.contains("person3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void ArrExpr0UntilEnd() {
        String ex = ".[0 until end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void ArrExprAll() {
        String ex = ".[all]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("PERSON3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void ArrExprFirst() {
        String ex = ".[first]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("person2") && resultString.contains("person3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void ArrExprEnd() {
        String ex = ".[end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = bsonHuman.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("person2") && resultString.contains("PERSON3") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    public void keyWithArrExpr_MultiKey_0to1() {
        String ex = ".species.person[0 to 1]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonSpeciesEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonSpeciesEncoded.length);
        }).join();
    }

    public void keyWithArrExpr_MultiKey_0until2() {
        String ex = ".species.person[0 until 2]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonSpeciesEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("PERSON2") && resultString.contains("person3") && resultValue.length == bsonSpeciesEncoded.length);
        }).join();
    }

    public void keyWithArrExpr_MultiKey_all() {
        String ex = ".species.alien[all]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonSpeciesEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("ET") && resultString.contains("PREDATOR") && resultString.contains("ALIEN") && resultValue.length == bsonSpeciesEncoded.length);
        }).join();
    }

    public void keyWithArrExpr_MultiKey_end() {
        String ex = ".species.alien[end]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonSpeciesEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("person1") && resultString.contains("et") && resultString.contains("predator") && resultString.contains("ALIEN") && resultValue.length == bsonSpeciesEncoded.length);
        }).join();
    }

    public void keyWithArrExpr_MultiKey_halfWord() {
        String ex = ".species.*[0]";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonSpeciesEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("PERSON1") && resultString.contains("person2") && resultString.contains("person3") && resultString.contains("ET") && resultString.contains("predator") && resultString.contains("alient") && resultValue.length == bsonSpeciesEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj() {
        String ex = "..name";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        byte[] bsonEncoded = clientBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("JOHN DOE") && resultString.contains("JANE DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_MultiKey() {
        String ex = ".client..name";
        Boson boson = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            String resultString = new String(resultValue);
            assert (resultString.contains("JOHN DOE") && resultString.contains("JANE DOE") && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_MultiDoubleDot() {
        String ex = "..persons..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArrExprFirst() {
        String ex = "..persons[first]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 12) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArrExprEnd() {
        String ex = "..persons[end]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 21) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArrExpr1ToEnd() {
        String ex = "..persons[1 to end]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 21) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArrAll() {
        String ex = "..persons[all]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArr0To1() {
        String ex = "..persons[0 to 1]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArr0To2() {
        BsonObject johnDoeBson = new BsonObject().put("name", "John Doe").put("age", 21);
        BsonObject janeDoeBson = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonObject doeJane = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(johnDoeBson).add(janeDoeBson).add(doeJane);
        BsonObject personsBson = new BsonObject().put("persons", personsArray);
        BsonObject clientBson = new BsonObject().put("client", personsBson);
        String ex = "..persons[0 to 2]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(clientBson.encodeToBarray()).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && containsInteger(resultValue, 30) && resultValue.length == clientBson.encodeToBarray().length);
        }).join();
    }

    @Test
    public void doubleDotInj_KeyWithArr0Until2() {
        BsonObject johnDoeBson = new BsonObject().put("name", "John Doe").put("age", 21);
        BsonObject janeDoeBson = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonObject doeJane = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(johnDoeBson).add(janeDoeBson).add(doeJane);
        BsonObject personsBson = new BsonObject().put("persons", personsArray);
        BsonObject clientBson = new BsonObject().put("client", personsBson);
        String ex = "..persons[0 until 2]..age";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(clientBson.encodeToBarray()).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && containsInteger(resultValue, 10) && resultValue.length == clientBson.encodeToBarray().length);
        }).join();
    }

    @Test
    public void doubleDotInj_HalfWord1() {
        String ex = "..per*[@age]";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_HalfWord2() {
        String ex = "..per*[@a*]";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void doubleDotInj_HalfWord3() {
        String ex = "..cl*..per*[@a*]";
        Boson boson = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void javaInstantTest() {
        String ex = "..instant";
        Instant ins = Instant.now();
        BsonObject person1 = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(1000));
        BsonObject client = new BsonObject().put("person", person1);
        BsonObject bson = new BsonObject().put("client", client);

        BsonObject expectedPerson = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(2000));
        BsonObject expectedClient = new BsonObject().put("person", expectedPerson);
        BsonObject expectedBson = new BsonObject().put("client", expectedClient);

        Boson boson = Boson.injector(ex, (Instant in) -> {
            return in.plusMillis(1000);
        });

        boson.go(bson.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedBson.encodeToBarray());
        }).join();
    }

    @Test
    public void javaInstantHasElemTest() {
        String ex = "..person[@instant]";
        Instant ins = Instant.now();
        BsonObject person1 = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(1000));
        BsonObject person2 = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject client = new BsonObject().put("person", persons);
        BsonObject bson = new BsonObject().put("client", client);

        BsonObject expectedPerson = new BsonObject().put("name", "John Doe").put("age", 21).put("instant", ins.plusMillis(2000));
        BsonArray expectedPersons = new BsonArray().add(expectedPerson).add(person2);
        BsonObject expectedClient = new BsonObject().put("person", expectedPersons);
        BsonObject expectedBson = new BsonObject().put("client", expectedClient);

        Boson boson = Boson.injector(ex, (Instant in) -> {
            return in.plusMillis(1000);
        });

        boson.go(bson.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedBson.encodeToBarray());
        }).join();
    }

    @Test
    public void classkeyInjection() {
        String ex = ".book";
        BsonObject book = new BsonObject().put("name", "Title1").put("pages", 1);
        BsonObject bsonBook = new BsonObject().put("book", book);
        BsonObject expectedBook = new BsonObject().put("name", "TITLE1").put("pages", 2);
        byte[] expectedBsonbook = new BsonObject().put("book", expectedBook).encodeToBarray();

        Boson boson = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName().toUpperCase(), in.getPages() * 2);
        });

        boson.go(bsonBook.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedBsonbook);
        }).join();
    }

    @Test
    public void MultipleKeyClassInjection() {
        BsonObject expectedBook = new BsonObject().put("name", "LOTR").put("pages", 320);
        BsonObject bsonBook = new BsonObject().put("book", expectedBook);

        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeBsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[0].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux("LOTR", 320);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_All() {
        String ex = ".store.books[all].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_0ToEnd() {
        String ex = ".store.books[0 to end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_0UntilEnd() {
        BsonArray books = new BsonArray().add(bsonBookExpected).add(bsonBook2);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeBsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[0 until end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_End() {
        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2Expected);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeBsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_1() {
        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2Expected);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeBsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_1() {
        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2Expected);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeBsonExpected = new BsonObject().put("store", store);

        String ex = "..books[1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_HasElem() {
        String ex = ".store.books[@book]";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_HasElem_DoubleDot() {
        String ex = "..books[@book]";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_DoubleDot() {
        String ex = "..book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_0_DoubleDot() {
        byte[] booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray();
        String ex = "..[0].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected);
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_1_DoubleDot() {
        byte[] booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToBarray();
        String ex = "..[1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected);
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_end_DoubleDot() {
        byte[] booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToBarray();
        String ex = "..[end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected);
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_first_DoubleDot() {
        byte[] booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray();
        String ex = "..[first].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected);
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_0ToEnd_DoubleDot() {
        String ex = "..[0 to end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_0UntilEnd_DoubleDot() {
        byte[] booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray();
        String ex = "..[0 until end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected);
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_0To1_DoubleDot() {
        String ex = "..[0 to 1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_0Until1_DoubleDot() {
        byte[] booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToBarray();
        String ex = "..[0 until 1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected);
        }).join();
    }

    @Test
    public void CaseClassInjection_ArrExpr_all_DoubleDot() {
        String ex = "..[all].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        byte[] bsonEncoded = books.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, booksExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void MultipleKeyCaseClassInjection_BookAux1() {
        BsonObject bookAux = new BsonObject().put("pages", 10.0).put("someLong", 10L).put("someBoolean", false);
        BsonObject bookAux2 = new BsonObject().put("pages", 23.10).put("someLong", 100000L).put("someBoolean", false);
        BsonObject bookAux3 = new BsonObject().put("pages", -3.0).put("someLong", 789456L).put("someBoolean", true);

        BsonObject bsonBookAux = new BsonObject().put("book", bookAux);
        BsonObject bsonBookAux2 = new BsonObject().put("book", bookAux2);
        BsonObject bsonBookAux3 = new BsonObject().put("book", bookAux3);

        BsonArray booksAux = new BsonArray().add(bsonBookAux).add(bsonBookAux2).add(bsonBookAux3);
        BsonObject storeAux = new BsonObject().put("books", booksAux);
        byte[] storeBsonAuxExpected = new BsonObject().put("store", storeAux).encodeToBarray();

        String ex = ".store.books[0].book";
        Boson bsonInj = Boson.injector(ex, (BookAux1 in) -> {
            return new BookAux1(in.getPages() + 9.0, in.getSomeLong() + 9L, !in.isSomeBoolean());
        });
        byte[] bsonEncoded = storeBsonAux.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonAuxExpected);
        }).join();
    }

    @Test
    public void DoubleDotKeyInjection_BookAux1() {
        BsonObject bookAux = new BsonObject().put("pages", 2.0).put("someLong", 2L).put("someBoolean", false);
        BsonObject bookAux2 = new BsonObject().put("pages", 46.20).put("someLong", 200000L).put("someBoolean", true);
        BsonObject bookAux3 = new BsonObject().put("pages", -6.0).put("someLong", 1578912L).put("someBoolean", false);

        BsonObject bsonBookAux = new BsonObject().put("book", bookAux);
        BsonObject bsonBookAux2 = new BsonObject().put("book", bookAux2);
        BsonObject bsonBookAux3 = new BsonObject().put("book", bookAux3);

        BsonArray booksAux = new BsonArray().add(bsonBookAux).add(bsonBookAux2).add(bsonBookAux3);
        BsonObject storeAux = new BsonObject().put("books", booksAux);
        byte[] storeBsonAuxExpected = new BsonObject().put("store", storeAux).encodeToBarray();

        String ex = "..book";
        Boson bsonInj = Boson.injector(ex, (BookAux1 in) -> {
            return new BookAux1(in.getPages() * 2, in.getSomeLong() * 2L, !in.isSomeBoolean());
        });
        byte[] bsonEncoded = storeBsonAux.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonAuxExpected);
        }).join();
    }

    @Test
    public void DoubleDotHasElem_BookAux1() {
        BsonObject bookAux = new BsonObject().put("pages", 2.0).put("someLong", 2L).put("someBoolean", false);
        BsonObject bookAux2 = new BsonObject().put("pages", 46.20).put("someLong", 200000L).put("someBoolean", true);
        BsonObject bookAux3 = new BsonObject().put("pages", -6.0).put("someLong", 1578912L).put("someBoolean", false);

        BsonObject bsonBookAux = new BsonObject().put("book", bookAux);
        BsonObject bsonBookAux2 = new BsonObject().put("book", bookAux2);
        BsonObject bsonBookAux3 = new BsonObject().put("book", bookAux3);

        BsonArray booksAux = new BsonArray().add(bsonBookAux).add(bsonBookAux2).add(bsonBookAux3);
        BsonObject storeAux = new BsonObject().put("books", booksAux);
        byte[] storeBsonAuxExpected = new BsonObject().put("store", storeAux).encodeToBarray();

        String ex = "..books[@book]";
        Boson bsonInj = Boson.injector(ex, (BookAux1 in) -> {
            return new BookAux1(in.getPages() * 2, in.getSomeLong() * 2L, !in.isSomeBoolean());
        });
        byte[] bsonEncoded = storeBsonAux.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonAuxExpected);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_0() {
        String expr = "..[0]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("person2").add("person3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_first() {
        String expr = "..[first]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("person2").add("person3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_all() {
        String expr = "..[all]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_end() {
        String expr = "..[end]";
        byte[] expectedEncoded = new BsonArray().add("person1").add("person2").add("PERSON3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_0To1() {
        String expr = "..[0 to 1]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("PERSON2").add("person3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_0Until1() {
        String expr = "..[0 until 1]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("person2").add("person3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_0ToEnd() {
        String expr = "..[0 to end]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void KeyWithArrayExp_DoubleDot_0UntilEnd() {
        String expr = "..[0 until end]";
        byte[] expectedEncoded = new BsonArray().add("PERSON1").add("PERSON2").add("person3").encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void NestedCaseClass_RootInj() {
        String expr = ".book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(nestedBson.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, nestedBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_DoubleDot() {
        String expr = "..book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(nestedBson.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, nestedBsonExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_KeyWithArrExpr_All() {
        BsonArray bsonArr = new BsonArray().add(nestedBson);
        BsonObject bsonObj = new BsonObject().put("books", bsonArr);

        BsonArray bsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject bsonObjExpected = new BsonObject().put("books", bsonArrExpected);

        String expr = "..books[all].book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(bsonObj.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, bsonObjExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_KeyWithArrExpr_0ToEnd() {
        BsonArray bsonArr = new BsonArray().add(nestedBson);
        BsonObject bsonObj = new BsonObject().put("books", bsonArr);

        BsonArray bsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject bsonObjExpected = new BsonObject().put("books", bsonArrExpected);

        String expr = "..books[0 to end].book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(bsonObj.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, bsonObjExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_KeyWithArrExpr_0() {
        BsonArray bsonArr = new BsonArray().add(nestedBson);
        BsonObject bsonObj = new BsonObject().put("books", bsonArr);

        BsonArray bsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject bsonObjExpected = new BsonObject().put("books", bsonArrExpected);

        String expr = "..books[0].book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(bsonObj.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, bsonObjExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_KeyWithArrExpr_first() {
        BsonArray bsonArr = new BsonArray().add(nestedBson);
        BsonObject bsonObj = new BsonObject().put("books", bsonArr);

        BsonArray bsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject bsonObjExpected = new BsonObject().put("books", bsonArrExpected);

        String expr = "..books[first].book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(bsonObj.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, bsonObjExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_KeyWithArrExpr_DooubleDot() {
        BsonArray bsonArr = new BsonArray().add(nestedBson);
        BsonObject bsonObj = new BsonObject().put("books", bsonArr);

        BsonArray bsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject bsonObjExpected = new BsonObject().put("books", bsonArrExpected);

        String expr = "..book";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(bsonObj.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, bsonObjExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void NestedCaseClass_KeyWithArrExpr_HasElem() {
        BsonArray bsonArr = new BsonArray().add(nestedBson);
        BsonObject bsonObj = new BsonObject().put("books", bsonArr);

        BsonArray bsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject bsonObjExpected = new BsonObject().put("books", bsonArrExpected);

        String expr = ".books[@book]";
        Boson bsonInj = Boson.injector(expr, (NestedBook in) -> {
            Author newAuthor = new Author(in.getAuthor().getFirstName().toUpperCase(), in.getAuthor().getLastName().toUpperCase(), in.getAuthor().getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        bsonInj.go(bsonObj.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, bsonObjExpected.encodeToBarray());
        }).join();
    }

    @Test
    public void Nested_KeyWithArrayExp_Single_First() {
        String expr = ".species.alien[first]";
        BsonArray expected = new BsonArray().add("ET").add("predator").add("alien");
        BsonObject expectedObj = new BsonObject().put("person", bsonHuman).put("alien", expected);
        byte[] expectedEncoded = new BsonObject().put("species", expectedObj).encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonSpeciesEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void Nested_ArrayExp_Double_Single_0UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[0 until end].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3);
        byte[] expectedEncoded = expected.encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void Nested_ArrayExp_Double_Double_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 until end]..age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        byte[] expectedEncoded = expected.encodeToBarray();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToBarray()).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, expectedEncoded);
        }).join();
    }

    @Test
    public void CodecJson_RootInjection() {
        BsonObject json = new BsonObject().put("name", "john doe");
        String ex = ".";
        Boson jsonInj = Boson.injector(ex, (String in) -> {
            return "{\"lastName\": \"Not Doe\"}";
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.contains("Not Doe"));
        }).join();
    }

    @Test
    public void CodecJson_TopLevelKeyMod() {
        BsonObject json = new BsonObject().put("name", "john doe");
        String ex = ".name";
        Boson jsonInj = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.contains("JOHN DOE") && resultValue.length() == json.encodeToString().length());
        }).join();
    }

    @Test
    public void CodecJson_NestedKey_TwoFields_SingleDots() {
        BsonObject obj = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person = new BsonObject().put("person", obj);
        BsonObject client = new BsonObject().put("client", person);
        BsonObject someObject = new BsonObject().put("SomeObject", client);
        BsonObject anotherObject = new BsonObject().put("AnotherObject", someObject);
        BsonObject json = new BsonObject().put("Wrapper", anotherObject);
        String ex = ".Wrapper.AnotherObject.SomeObject.client.person.age";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.contains("41") && resultValue.length() == json.encodeToString().length());
        }).join();
    }

    @Test
    public void CodecJson_NestedKey_TwoFields_3_SingleDots() {
        BsonObject obj = new BsonObject().put("name", "john doe").put("age", 21).put("gender", "male");
        BsonObject person = new BsonObject().put("person", obj);
        BsonObject client = new BsonObject().put("client", person);
        BsonObject someObject = new BsonObject().put("SomeObject", client);
        BsonObject anotherObject = new BsonObject().put("AnotherObject", someObject);
        BsonObject json = new BsonObject().put("Wrapper", anotherObject);
        String ex = ".Wrapper.AnotherObject.SomeObject.client.person.age";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.contains("41") && resultValue.length() == json.encodeToString().length());
        }).join();
    }

    @Test
    public void CodecJson_NestedKey_DoubleDots() {
        BsonObject obj = new BsonObject().put("name", "john doe").put("age", 21).put("gender", "male");
        BsonObject person = new BsonObject().put("person", obj);
        BsonObject client = new BsonObject().put("client", person);
        BsonObject someObject = new BsonObject().put("SomeObject", client);
        BsonObject anotherObject = new BsonObject().put("AnotherObject", someObject);
        BsonObject json = new BsonObject().put("Wrapper", anotherObject);
        String ex = "..age";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.contains("41") && resultValue.length() == json.encodeToString().length());
        }).join();
    }

    @Test
    public void CodecJson_NestedKey_2_DoubleDots() {
        BsonObject obj = new BsonObject().put("name", "john doe").put("age", 21).put("gender", "male");
        BsonObject person = new BsonObject().put("person", obj);
        BsonObject client = new BsonObject().put("client", person);
        BsonObject someObject = new BsonObject().put("SomeObject", client);
        BsonObject anotherObject = new BsonObject().put("AnotherObject", someObject);
        BsonObject json = new BsonObject().put("Wrapper", anotherObject);
        String ex = "..client..age";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.contains("41") && resultValue.length() == json.encodeToString().length());
        }).join();
    }

    @Test
    public void CodecJson_Java_instantInjection() {
        Instant ins = Instant.now();
        BsonObject obj = new BsonObject().put("name", "john doe").put("age", 21).put("instant", ins.plusMillis(1000));
        BsonObject person = new BsonObject().put("person", obj);
        BsonObject json = new BsonObject().put("client", person);

        BsonObject objExpected = new BsonObject().put("name", "john doe").put("age", 21).put("instant", ins.plusMillis(2000));
        BsonObject personExpected = new BsonObject().put("person", objExpected);
        BsonObject jsonExpected = new BsonObject().put("client", personExpected);

        String ex = "..instant";
        Boson jsonInj = Boson.injector(ex, (Instant in) -> {
            return in.plusMillis(1000);
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_1() {
        BsonObject person1 = new BsonObject().put("name", "John Doe");
        BsonObject person2 = new BsonObject().put("name", "Jane Doe");
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject json = new BsonObject().put("persons", persons);

        BsonObject person1Expected = new BsonObject().put("name", "JOHN DOE");
        BsonObject person2Expected = new BsonObject().put("name", "JANE DOE");
        BsonArray personsExpected = new BsonArray().add(person1Expected).add(person2Expected);
        BsonObject jsonExpected = new BsonObject().put("persons", personsExpected);

        String ex = ".persons[@name]";
        Boson jsonInj = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_2() {
        BsonObject person1 = new BsonObject().put("name", "John Doe");
        BsonObject person2 = new BsonObject().put("surname", "Doe");
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject json = new BsonObject().put("persons", persons);

        BsonObject person1Expected = new BsonObject().put("name", "JOHN DOE");
        BsonArray personsExpected = new BsonArray().add(person1Expected).add(person2);
        BsonObject jsonExpected = new BsonObject().put("persons", personsExpected);

        String ex = ".persons[@name]";
        Boson jsonInj = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_3() {
        BsonArray persons = new BsonArray().add("Not and Object").add("Something");
        BsonObject json = new BsonObject().put("persons", persons);

        BsonArray personsExpected = new BsonArray().add("Not and Object").add("Something");
        BsonObject jsonExpected = new BsonObject().put("persons", personsExpected);

        String ex = ".persons[@name]";
        Boson jsonInj = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_4() {
        BsonObject person1 = new BsonObject().put("name", "John Doe");
        BsonArray persons = new BsonArray().add(person1).add("Something");
        BsonObject json = new BsonObject().put("persons", persons);

        BsonObject person1Expected = new BsonObject().put("name", "JOHN DOE");
        BsonArray personsExpected = new BsonArray().add(person1Expected).add("Something");
        BsonObject jsonExpected = new BsonObject().put("persons", personsExpected);

        String ex = ".persons[@name]";
        Boson jsonInj = Boson.injector(ex, (String in) -> {
            return in.toUpperCase();
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_5() {
        BsonObject person1 = new BsonObject().put("name", "John Doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject json = new BsonObject().put("persons", persons);

        BsonObject person1Expected = new BsonObject().put("name", "John Doe").put("age", 41);
        BsonObject person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32);
        BsonArray personsExpected = new BsonArray().add(person1Expected).add(person2Expected);
        BsonObject jsonExpected = new BsonObject().put("persons", personsExpected);

        String ex = ".persons[@age]";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_6() {
        BsonObject person1 = new BsonObject().put("name", "John Doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject client = new BsonObject().put("persons", persons);
        BsonObject json = new BsonObject().put("client", client);


        BsonObject person1Expected = new BsonObject().put("name", "John Doe").put("age", 41);
        BsonObject person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32);
        BsonArray personsExpected = new BsonArray().add(person1Expected).add(person2Expected);
        BsonObject clientExpected = new BsonObject().put("persons", personsExpected);
        BsonObject jsonExpected = new BsonObject().put("client", clientExpected);

        String ex = ".client.persons[@age]";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_7() {
        BsonObject person1 = new BsonObject().put("name", "John Doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject client = new BsonObject().put("persons", persons);
        BsonObject json = new BsonObject().put("client", client);


        BsonObject person1Expected = new BsonObject().put("name", "John Doe").put("age", 41);
        BsonObject person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32);
        BsonArray personsExpected = new BsonArray().add(person1Expected).add(person2Expected);
        BsonObject clientExpected = new BsonObject().put("persons", personsExpected);
        BsonObject jsonExpected = new BsonObject().put("client", clientExpected);

        String ex = "..persons[@age]";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElem_8() {
        BsonObject person1 = new BsonObject().put("name", "John Doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "Jane Doe").put("age", 12);
        BsonArray persons = new BsonArray().add(person1).add(person2);
        BsonObject client = new BsonObject().put("persons", persons);
        BsonObject clientJson = new BsonObject().put("client", client);
        BsonObject obj = new BsonObject().put("obj", clientJson);
        BsonObject json = new BsonObject().put("wrapper", obj);


        BsonObject person1Expected = new BsonObject().put("name", "John Doe").put("age", 41);
        BsonObject person2Expected = new BsonObject().put("name", "Jane Doe").put("age", 32);
        BsonArray personsExpected = new BsonArray().add(person1Expected).add(person2Expected);
        BsonObject clientExpected = new BsonObject().put("persons", personsExpected);
        BsonObject clientJsonExpected = new BsonObject().put("client", clientExpected);
        BsonObject objExpected = new BsonObject().put("obj", clientJsonExpected);
        BsonObject jsonExpected = new BsonObject().put("wrapper", objExpected);


        String ex = "..obj..persons[@age]";
        Boson jsonInj = Boson.injector(ex, (Integer in) -> {
            return in + 20;
        });
        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_KeyCaseClassInjection() {
        BsonObject book = new BsonObject().put("name", "Title1").put("pages", 1);
        BsonObject json = new BsonObject().put("book", book);

        BsonObject bookExpected = new BsonObject().put("name", "LOTR").put("pages", 320);
        BsonObject jsonExpected = new BsonObject().put("book", bookExpected);
        String ex = ".book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux("LOTR", 320);
        });

        jsonInj.go(json.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElemCaseClassInjection() {
        String ex = ".store.books[@book]";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeBsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElemCaseClassInjection2() {
        String ex = "..books[@book]";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeBsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_HasElemCaseClassInjection3() {
        String ex = "..store..books[@book]";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeBsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_NestedCaseClass1() {
        BsonObject expectedBook = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthor);
        BsonObject expectedJson = new BsonObject().put("book", expectedBook);
        String ex = ".book";
        Boson jsonInj = Boson.injector(ex, (NestedBook in) -> {
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, in.getAuthor());
        });

        jsonInj.go(nestedBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedJson.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_NestedCaseClass2() {
        BsonObject expectedBook = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthor);
        BsonObject expectedJson = new BsonObject().put("book", expectedBook);
        String ex = "..book";
        Boson jsonInj = Boson.injector(ex, (NestedBook in) -> {
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, in.getAuthor());
        });

        jsonInj.go(nestedBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedJson.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_NestedCaseClass3() {
        BsonArray jsonArr = new BsonArray().add(nestedBson);
        BsonObject jsonObj = new BsonObject().put("books", jsonArr);

        BsonArray jsonArrExpected = new BsonArray().add(nestedBsonExpected);
        BsonObject jsonObjExpected = new BsonObject().put("books", jsonArrExpected);

        String ex = ".books[@book]";
        Boson jsonInj = Boson.injector(ex, (NestedBook in) -> {
            Author oldAuthor = in.getAuthor();
            Author newAuthor = new Author(oldAuthor.getFirstName().toUpperCase(), oldAuthor.getLastName().toUpperCase(), oldAuthor.getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        jsonInj.go(jsonObj.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonObjExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_NestedCaseClass4() {
        BsonArray jsonArr = new BsonArray().add(nestedBson).add(nestedBson2);
        BsonObject jsonObj = new BsonObject().put("books", jsonArr);

        BsonArray jsonArrExpected = new BsonArray().add(nestedBsonExpected).add(nestedBson2Expected);
        BsonObject jsonObjExpected = new BsonObject().put("books", jsonArrExpected);


        String ex = "..books[all].book";
        Boson jsonInj = Boson.injector(ex, (NestedBook in) -> {
            Author oldAuthor = in.getAuthor();
            Author newAuthor = new Author(oldAuthor.getFirstName().toUpperCase(), oldAuthor.getLastName().toUpperCase(), oldAuthor.getAge() + 20);
            return new NestedBook(in.getName().toUpperCase(), in.getPages() + 100, newAuthor);
        });

        jsonInj.go(jsonObj.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(jsonObjExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_1() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[1]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_0To1() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[0 to 1]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_0Until2() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[0 until 2]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_First() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("person2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[first]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_All() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[all]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_End() {
        BsonArray personExpected = new BsonArray().add("person1").add("person2").add("PERSON3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_1ToEnd() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[1 to end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_0UntilEnd() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = ".person[0 until end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_Double_1() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = "..person[1]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_Double_First() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("person2").add("person3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = "..person[first]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_Double_All() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = "..person[all]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_KeyArrayExp_Double_End() {
        BsonArray personExpected = new BsonArray().add("person1").add("person2").add("PERSON3");
        String expectedEncoded = new BsonObject().put("person", personExpected).encodeToString();
        String expr = "..person[end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonObjArray.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_1() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[1]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_First() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("person2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[first]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_All() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[all]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_End() {
        BsonArray personExpected = new BsonArray().add("person1").add("person2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_1To2() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[1 to 2]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_0Until2() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[0 until 2]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_1ToEnd() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[1 to end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_1UntilEnd() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = ".[1 until end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_1() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[1]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_First() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("person2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[first]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_All() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[all]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_End() {
        BsonArray personExpected = new BsonArray().add("person1").add("person2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_1To2() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[1 to 2]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_0Until2() {
        BsonArray personExpected = new BsonArray().add("PERSON1").add("PERSON2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[0 until 2]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_1ToEnd() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("PERSON3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[1 to end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_ArrayExp_Double_1UntilEnd() {
        BsonArray personExpected = new BsonArray().add("person1").add("PERSON2").add("person3");
        String expectedEncoded = personExpected.encodeToString();
        String expr = "..[1 until end]";

        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonHuman.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_1() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1].age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_First() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[first].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2).add(person3);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_All() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[all].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_End() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[end].age";

        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1).add(person2).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_1To2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 to 2].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_1Until2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 until 2].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_1ToEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 to end].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Single_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 until end].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_1() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[1]..age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_First() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[first]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2).add(person3);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_All() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[all]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_End() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[end]..age";

        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1).add(person2).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single__Double1To2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[1 to 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_1Until2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[1 until 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_1ToEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[1 to end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Single_Double_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = ".[1 until end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    /*********
     *
     */


    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_1() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1]..age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_First() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[first]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2).add(person3);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_All() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[all]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_End() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[end]..age";

        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1).add(person2).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_1To2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 to 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_1Until2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 until 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_1ToEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 to end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_ArrayExp_Double_Double_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray persons = new BsonArray().add(person1).add(person2).add(person3);

        String expr = "..[1 until end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = expected.encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_1() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1].age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_First() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[first].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2).add(person3);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_All() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[all].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_End() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[end].age";

        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1).add(person2).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_1To2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 to 2].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_1Until2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 until 2].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_1ToEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 to end].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Single_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 until end].age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_1() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[1]..age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_First() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[first]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2).add(person3);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_All() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[all]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_End() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[end]..age";

        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1).add(person2).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single__Double1To2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[1 to 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_1Until2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[1 until 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_1ToEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[1 to end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_Double_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = ".person[1 until end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_1() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1]..age";

        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonArray expected = new BsonArray().add(person1).add(person2Exp).add(person3);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_First() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[first]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2).add(person3);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_All() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[all]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 41);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_End() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[end]..age";

        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1).add(person2).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_1To2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 to 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_1Until2() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 until 2]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_1ToEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 to end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 30);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Double_Double_1UntilEnd() {
        BsonObject person1 = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2 = new BsonObject().put("name", "jane doe").put("age", 12);
        BsonObject person3 = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray personsArray = new BsonArray().add(person1).add(person2).add(person3);
        BsonObject persons = new BsonObject().put("person", personsArray);

        String expr = "..person[1 until end]..age";

        BsonObject person1Exp = new BsonObject().put("name", "john doe").put("age", 21);
        BsonObject person2Exp = new BsonObject().put("name", "jane doe").put("age", 32);
        BsonObject person3Exp = new BsonObject().put("name", "doe jane").put("age", 10);
        BsonArray expected = new BsonArray().add(person1Exp).add(person2Exp).add(person3Exp);
        String expectedEncoded = new BsonObject().put("person", expected).encodeToString();
        Boson bsonInj = Boson.injector(expr, (Integer in) -> {
            return in + 20;
        });
        bsonInj.go(persons.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj() {
        BsonObject book = new BsonObject().put("name", "LOTR").put("pages", 320);
        BsonObject bsonBook = new BsonObject().put("book", book);

        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeJsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[0].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux("LOTR", 320);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeJsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj_2() {
        BsonArray books = new BsonArray().add(bsonBookExpected).add(bsonBook2);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeJsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[first].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeJsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj_3() {
        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2Expected);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeJsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[1].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeJsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj_4() {
        String ex = ".store.books[all].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeBsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj_5() {
        String ex = ".store.books[0 to end].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeBsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj_6() {
        BsonArray books = new BsonArray().add(bsonBookExpected).add(bsonBook2);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeJsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[0 until end].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeJsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_MultipleKeyCaseClassInj_7() {
        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2Expected);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeJsonExpected = new BsonObject().put("store", store);

        String ex = ".store.books[end].book";
        Boson jsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });

        jsonInj.go(storeBson.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(storeJsonExpected.encodeToString()));
        }).join();
    }


    @Test
    public void CodecJson_Nested_KeyWithArrayExp_Single_First() {
        String expr = ".species.alien[first]";
        BsonArray expected = new BsonArray().add("ET").add("predator").add("alien");
        BsonObject expectedObj = new BsonObject().put("person", bsonHuman).put("alien", expected);
        String expectedEncoded = new BsonObject().put("species", expectedObj).encodeToString();
        Boson bsonInj = Boson.injector(expr, (String in) -> {
            return in.toUpperCase();
        });
        bsonInj.go(bsonSpeciesObj.encodeToString()).thenAccept(resultValue -> {
            assert (resultValue.equals(expectedEncoded));
        }).join();
    }

    @Test
    public void CodecJson_halfNameInj() {
        String ex1 = ".*ame";
        String ex2 = ".nam*";
        String ex3 = ".n*me";
        Boson boson1 = Boson.injector(ex1, (String in) -> {
            return in.toUpperCase();
        });
        Boson boson2 = Boson.injector(ex2, (String in) -> {
            return in.toLowerCase();
        });
        Boson boson3 = Boson.injector(ex3, (String in) -> {
            return in + " Hello";
        });

        String bsonEncoded = johnDoeBson.encodeToString();
        boson1.go(bsonEncoded).thenAccept((resultValue1) -> {
            Boolean b1 = (new String(resultValue1).contains("JOHN DOE") && resultValue1.length() == bsonEncoded.length());
            boson2.go(bsonEncoded).thenAccept((resultValue2) -> {
                Boolean b2 = (new String(resultValue2).contains("john doe") && resultValue2.length() == bsonEncoded.length());
                boson3.go(bsonEncoded).thenAccept((resultValue3) -> {
                    Boolean b3 = (new String(resultValue3).contains("John Doe Hello") && resultValue3.length() == (bsonEncoded.length()+6));
                    assert (b1 && b2 && b3);
                }).join();
            }).join();
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_1() {
        BsonArray books = new BsonArray().add(bsonBook).add(bsonBook2Expected);
        BsonObject store = new BsonObject().put("books", books);
        BsonObject storeBsonExpected = new BsonObject().put("store", store);

        String ex = "..books[1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = storeBson.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(storeBsonExpected.encodeToString()));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_0_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString();
        String ex = "..[0].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_first_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString();
        String ex = "..[first].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_1_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToString();
        String ex = "..[1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_End_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBook).add(bsonBook2Expected).encodeToString();
        String ex = "..[end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_0ToEnd_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected).encodeToString();
        String ex = "..[0 to end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_0UntilEnd_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString();
        String ex = "..[0 until end].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_0To1_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected).encodeToString();
        String ex = "..[0 to 1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_0Until1_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2).encodeToString();
        String ex = "..[0 until 1].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void CodecJson_CaseClassInjection_ArrExpr_All_DoubleDot() {
        String booksExpected = new BsonArray().add(bsonBookExpected).add(bsonBook2Expected).encodeToString();
        String ex = "..[all].book";
        Boson bsonInj = Boson.injector(ex, (BookAux in) -> {
            return new BookAux(in.getName(), in.getPages() + 100);
        });
        String bsonEncoded = books.encodeToString();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assert (resultValue.equals(booksExpected));
        }).join();
    }

    @Test
    public void ExtractFromArrayPos() {
        String expression = ".Store.Book[1 to 2]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(title2ApiTests.encodeToBarray());
            expected.add(title3ApiTests.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$.Store.Book[1:2] -> checked

    @Test
    public void ExtractFromArrayPosWithEnd() {  //TODO CHANGE
        String expression = ".Store.Book[1 until end]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(title2ApiTests.encodeToBarray());
            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$.Store.Book[0:2] -> checked

    @Test
    public void ExtractKeyFromArrayPosWithEnd() {
        String expression = ".Store.Book[1 until end].Price";
        ArrayList<Double> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Double out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
        }).join();

        assert (mutableBuffer.contains(21.5));
    }   //$.Store.Book[:].Price -> checked

    @Test
    public void ExtractFromArrayWithElem2Times() {
        String expression = ".Store.Book[@Price].SpecialEditions[@Title]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$.Store.Book[?(@.Price)].SpecialEditions[?(@.Title)] -> checked

    @Test
    public void ExtractFromArrayWithElem() {
        String expression = ".Store.Book[@SpecialEditions]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(title1ApiTests.encodeToBarray());
            expected.add(title2ApiTests.encodeToBarray());
            expected.add(title3ApiTests.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$.Store.Book[?(@.SpecialEditions)]

    @Test
    public void ExtractKeyFromArrayWithElem() {
        String expression = ".Store.Book[@Price].Title";
        ArrayList<String> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (String out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<String> expected = new ArrayList();
            expected.add("Java");
            expected.add("Scala");
            expected.add("C++");
            assert (mutableBuffer.containsAll(expected));
        }).join();
    } //$.Store.Book[?(@.Price)].Title -> checked

    @Test
    public void ExtractFromArrayWithElemAndArrayPos() {
        String expression = ".Store.Book[@SpecialEditions].SpecialEditions[0]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractFromArrayPosAndArrayWithElem() {
        String expression = ".Store.Book[0 until 1].SpecialEditions[@Price]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractEverythingFromRoot() {
        String expression = ".*";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(storeApiTests.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$.* -> checked

    @Test
    public void ExtractEntireArray() {
        String expression = ".Store.Book";
        byte[] expected = booksApiTests.encodeToBarray();
        List<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            assertTrue(Arrays.equals(mutableBuffer.get(0), expected));
        }).join();
    }   //$.Store.Book -> checked

    @Test
    public void ExtractAllPrices() {
        String expression = "Price";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<Number> expected = new ArrayList<>();
            expected.add(15.5);
            expected.add(39);
            expected.add(21.5);
            expected.add(40);
            expected.add(12.6);
            expected.add(38);
            expected.add(48);
            expected.add(35);
            expected.add(38);
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }   //$..Price -> checked

    @Test
    public void ExtractAllBookPrices() {
        String expression = "Book..Price";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<Number> expected = new ArrayList<>();
            expected.add(15.5);
            expected.add(39);
            expected.add(21.5);
            expected.add(40);
            expected.add(12.6);
            expected.add(38);
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }   //$.Book..Price -> checked

    @Test
    public void ExtractKeyEverywhereArrayWithElem() {
        String expression = "SpecialEditions[@Price].Title";
        ArrayList<String> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (String out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<String> expected = new ArrayList<>();
            expected.add("JavaMachine");
            expected.add("ScalaMachine");
            expected.add("C++Machine");
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }   //$..SpecialEditions[?(@.Price)].Title -> checked

    @Test
    public void ExtractEverywhereArrayWithElem() {
        String expression = "SpecialEditions[@Price]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$..SpecialEditions[?(@.Price)] -> checked

    @Test
    public void ExtractEverywhereArrayPos() {
        String expression = "SpecialEditions[0]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }   //$..SpecialEditions[0] -> checked

    @Test
    public void ExtractEverywhereHalfKeyV1() {
        String expression = "*tle";
        ArrayList<String> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (String out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<String> expected = new ArrayList<>();
            expected.add("Java");
            expected.add("JavaMachine");
            expected.add("Scala");
            expected.add("ScalaMachine");
            expected.add("C++");
            expected.add("C++Machine");
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }

    @Test
    public void ExtractEverywhereHalfKeyV3() {
        String expression = "Pri*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<Number> expected = new ArrayList<>();
            expected.add(15.5);
            expected.add(39);
            expected.add(21.5);
            expected.add(40);
            expected.add(12.6);
            expected.add(38);
            expected.add(48);
            expected.add(35);
            expected.add(38);
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }

    @Test
    public void ExtractHalfKeyArrayWithElem2Times() {
        String expression = "*k[@Price].SpecialEditions[@Price]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] byteArr) -> {
            mutableBuffer.add(byteArr);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractEverythingOfArrayWithElem() {
        String expression = "SpecialEditions[0 to end].*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("JavaMachine");
            expected.add(39);
            expected.add("ScalaMachine");
            expected.add(40);
            expected.add("C++Machine");
            expected.add(38);
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }

    @Test
    public void ExtractAllTitlesOfArray() {
        String expression = "Book.*..Title";
        ArrayList<String> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (String out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<String> expected = new ArrayList<>();
            expected.add("Java");
            expected.add("JavaMachine");
            expected.add("Scala");
            expected.add("ScalaMachine");
            expected.add("C++");
            expected.add("C++Machine");
            assert (mutableBuffer.containsAll(expected));
        }).join();
    }

    @Test
    public void ExtractArrayLimitFromBook() {
        String expression = "Book[0 to end].*..[0 to end]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(edition1.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractAllElemFromAllElemOfBook() {
        String expression = "Book.*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
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
            assert (mutableBuffer.size() == expected.size());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    private BsonArray arrEvent = new BsonArray().add("Shouldn't exist").add(bsonApiTests).add(false).add(new BsonObject().put("Store_1", storeApiTests));
    private byte[] encodedValidated = arrEvent.encodeToBarray();

    @Test
    public void ExtractPosFromEveryArrayInsideOtherArrayPosEnd() {
        String expression = ".[0 to 2]..[0 to end]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(encodedValidated).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(title1ApiTests.encodeToBarray());
            expected.add(edition1.encodeToBarray());
            expected.add(title2ApiTests.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(title3ApiTests.encodeToBarray());
            expected.add(edition3.encodeToBarray());
            expected.add(hat1.encodeToBarray());
            expected.add(hat2.encodeToBarray());
            expected.add(hat3.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++)
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
        }).join();
    }

    @Test
    public void ExtractPosFromEveryArrayInsideOtherArrayPosLimit() {
        String expression = ".[0 to 2]..[0 to 1]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(encodedValidated).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(title1ApiTests.encodeToBarray());
            expected.add(edition1.encodeToBarray());
            expected.add(title2ApiTests.encodeToBarray());
            expected.add(edition2.encodeToBarray());
            expected.add(edition3.encodeToBarray());
            expected.add(hat1.encodeToBarray());
            expected.add(hat2.encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++)
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
        }).join();
    }

    private BsonArray arr1 =
            new BsonArray()
                    .add("Hat")
                    .add(false)
                    .add(2.2)
                    .addNull()
                    .add(1000L)
                    .add(new BsonArray()
                            .addNull()
                            .add(new BsonArray()
                                    .add(100000L)))
                    .add(2)
                    .add(new BsonObject()
                            .put("Quantity", 500L)
                            .put("SomeObj", new BsonObject()
                                    .putNull("blah"))
                            .put("one", false)
                            .putNull("three"));

    private BsonObject bE = new BsonObject().put("Store", arr1);

    @Test
    public void ExtractArrayWithElemV1() {
        String expression = ".Store[@three]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());
            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractArrayWithElemV2() {
        String expression = ".Store[@one]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());
            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractArrayWithElemV3() {
        String expression = ".Store[@Quantity]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());
            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosLimitV1() {
        String expression = ".[0 to 5].[0 to end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Null");
            expected.add(new BsonArray().add(100000L).encodeToBarray());

            assert (mutableBuffer.size() == expected.size() && mutableBuffer.get(0).equals(expected.get(0)));
            assertArrayEquals((byte[]) mutableBuffer.get(1), (byte[]) expected.get(1));
        }).join();
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosLimitV2() {
        String expression = ".[6 to 7].[0 to end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosEndV1() {
        String expression = ".[0 to end].[0 to end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Null");
            expected.add(new BsonArray().add(100000L).encodeToBarray());

            assert (mutableBuffer.size() == expected.size() && mutableBuffer.get(0).equals(expected.get(0)));
            assertArrayEquals((byte[]) mutableBuffer.get(1), (byte[]) expected.get(1));
        }).join();
    }

    @Test
    public void ExtractPosFromArrayInsideOtherArrayPosEndV2() {
        String expression = ".[6 to end].[0 to end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractPosFromArrayInsideOtherInsideOtherArrayPosLimit() {
        String expression = ".[0 to 5].[0 to 40].[0]";
        ArrayList<Long> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Long out) -> {
            mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.contains(100000L));
        }).join();
    }

    @Test
    public void ExtractPosFromArrayInsideOtherInsideOtherArrayPosEnd() {
        String expression = ".[0 to end].[0 to end].[0 to end]";
        ArrayList<Long> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Long out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.contains(100000L));
        }).join();
    }

    @Test
    public void ExtractAllElemsOfArrayRootWithLimit() {
        String expression = ".[0 to 7].*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Null");
            expected.add(new BsonArray().add(100000L).encodeToBarray());
            expected.add(500L);
            expected.add(new BsonObject().putNull("blah").encodeToBarray());
            expected.add(false);
            expected.add("Null");

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                if (mutableBuffer.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                    assertTrue(Arrays.equals((byte[]) mutableBuffer.get(i), (byte[]) expected.get(i)));
                else if (mutableBuffer.get(i) instanceof Double && expected.get(i) instanceof Double)
                    assertTrue((double) mutableBuffer.get(i) == (double) expected.get(i));
                else {
                    assertTrue(mutableBuffer.get(i).equals(expected.get(i)));
                }
            }
        }).join();
    }

    @Test
    public void ExtractAllElemsOfArrayRootEnd() {
        String expression = ".[0 to end].*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Null");
            expected.add(new BsonArray().add(100000L).encodeToBarray());
            expected.add(500L);
            expected.add(new BsonObject().putNull("blah").encodeToBarray());
            expected.add(false);
            expected.add("Null");

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                if (mutableBuffer.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                    assertTrue(Arrays.equals((byte[]) mutableBuffer.get(i), (byte[]) expected.get(i)));
                else if (mutableBuffer.get(i) instanceof Double && expected.get(i) instanceof Double)
                    assertTrue((double) mutableBuffer.get(i) == (double) expected.get(i));
                else {
                    assertTrue(mutableBuffer.get(i).equals(expected.get(i)));
                }
            }
        }).join();
//        private BsonArray arr1 =
//                new BsonArray()
//                        .add("Hat")
//                        .add(false)
//                        .add(2.2)
//                        .addNull()
//                        .add(1000L)
//                        .add(new BsonArray().addNull().add(new BsonArray().add(100000L)))
//                        .add(2)
//                        .add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three"))
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootWithLimit() {
        String expression = ".[0 to 7].*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add(100000L);
            expected.add("Null");

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                if (mutableBuffer.get(i) instanceof byte[] && expected.get(i) instanceof byte[])
                    assertTrue(Arrays.equals((byte[]) mutableBuffer.get(i), (byte[]) expected.get(i)));
                else if (mutableBuffer.get(i) instanceof Double && expected.get(i) instanceof Double)
                    assertTrue((double) mutableBuffer.get(i) == (double) expected.get(i));
                else {
                    assertTrue(mutableBuffer.get(i).equals(expected.get(i)));
                }
            }
        }).join();
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootEnd() {
        String expression = ".[0 to end].*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object out) -> {
            if (out != null)
                mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.contains(100000L) && mutableBuffer.contains("Null"));
        }).join();
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootLastPosLimit() {
        String expression = ".[7 to 7].*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.get(0).equals("Null"));
        }).join();
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayRootLastPosEnd() {
        String expression = ".[7 to end].*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.get(0).equals("Null"));
        }).join();
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfAllElemsOfArrayRoot() {
        String expression = ".[0 to end].*.*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfAllElemsOfArrayRootWithOutput() {
        BsonArray _a = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L)
                .add(new BsonArray().addNull().add(new BsonArray().add(100000L).add(new BsonArray().add(true))));
        String expression = ".[0 to end].*.*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(_a.encodeToBarray()).thenRun(() -> {
            assert ((boolean) mutableBuffer.get(0));
        }).join();
    }

    @Test
    public void ExtractKeyFromArrayPosEndOfArrayRoot() {
        String expression = ".[0 to end]..Quantity";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.get(0).equals(500L));
        }).join();
    }

    @Test
    public void ExtractObjFromArrayPosLimitOfArrayRoot() {
        String expression = ".[0 to 7]..SomeObj";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().putNull("blah").encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractKeyArrayWithElem() {
        String expression = ".Store[@SomeObj]..SomeObj";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().putNull("blah").encodeToBarray());

            assert (mutableBuffer.size() == expected.size());
            for (int i = 0; i < mutableBuffer.size(); i++) {
                assertTrue(Arrays.equals(mutableBuffer.get(i), expected.get(i)));
            }
        }).join();
    }

    @Test
    public void ExtractAllElemsOfAllElemsOfArrayWithElem() {
        String expression = ".*.*.*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add(100000L);
            expected.add("Null");
            assert (mutableBuffer.size() == expected.size());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractKeyArrayWithElemOfArrayRootDontMatch() {
        String expression = ".Store[@Nothing]..SomeObj";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractBoolean() {
        String expression = "..one";
        ArrayList<Boolean> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Boolean obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (!mutableBuffer.get(0));
        }).join();
    }

    @Test
    public void ExtractNull() {
        String expression = "..three";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.get(0).equals("Null"));
        }).join();
    }

    @Test
    public void ExtractArrayPosToEndWithArrayRoot() {
        String expression = ".[0 to end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Hat");
            expected.add(false);
            expected.add(2.2);
            expected.add("Null");
            expected.add(1000L);
            expected.add(new BsonArray().addNull().add(new BsonArray().add(100000L)).encodeToBarray());
            expected.add(2);
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());

            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractArrayPosLimitWithArrayRoot() {
        String expression = ".[0 to 7]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Hat");
            expected.add(false);
            expected.add(2.2);
            expected.add("Null");
            expected.add(1000L);
            expected.add(new BsonArray().addNull().add(new BsonArray().add(100000L)).encodeToBarray());
            expected.add(2);
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractArrayLastPosLimitWithArrayRoot() {
        String expression = ".[7 to 7]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            byte[] expected = new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray();
            assertArrayEquals(mutableBuffer.get(0), expected);
        }).join();
    }

    @Test
    public void ExtractArrayLastPosEndWithArrayRoot() {
        String expression = ".[7 to end]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractAllElementsOfArrayRoot() {
        String expression = ".*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Hat");
            expected.add(false);
            expected.add(2.2);
            expected.add("Null");
            expected.add(1000L);
            expected.add(new BsonArray().addNull().add(new BsonArray().add(100000L)).encodeToBarray());
            expected.add(2);
            expected.add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three").encodeToBarray());

            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractArrayRoot() {
        String expression = ".";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            byte[] expected = arr1.encodeToBarray();
            assertArrayEquals(mutableBuffer.get(0), expected);
        }).join();
    }

    @Test
    public void ExtractObjRoot() {
        String expression = ".";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            byte[] expected = bsonApiTests.encodeToBarray();
            assertArrayEquals(mutableBuffer.get(0), expected);
        }).join();
    }

    @Test
    public void Extract_Coverage_1() {
        String expression = "[end]..[end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add(100000L);
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void Extract_Coverage_2() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull()))).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2)
                .add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three"));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[first]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())).encodeToBarray());
            expected.add("Null");
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void Extract_Coverage_3() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull()))).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2)
                .add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three"));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[first].*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Null");
            expected.add(new BsonObject().put("Store", new BsonArray().addNull()).encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void Extract_Coverage_4() {
        BsonArray arr1 = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull().add(new BsonArray().add(100000L))).add(2);
        String expression = "[end].[end].*";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void Extract_Coverage_5() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().add(33)).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2)
                .add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[end].*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Null");
            expected.add(new BsonObject().put("Store", new BsonArray().addNull()).encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void Extract_Coverage_6() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().add(33)).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2).add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[end].Level";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void Extract_Coverage_7() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().add(33)).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2).add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[end]..Store";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonArray().addNull().encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void Extract_Coverage_8() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().add(33)).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2).add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[end].*.*";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void Extract_Coverage_9() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().add(33)).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2).add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())));
        BsonObject bE = new BsonObject().put("Store", arr1);
        String expression = "Store[@elem]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] obj) -> {
            mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractFirstPosArray() {
        String expression = "[first]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add("Hat");
            expected.add("Null");
            expected.add(100000L);
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractLastPosDeep() {
        BsonArray arr1 = new BsonArray().add("Hat").add(false).add(2.2).addNull().add(1000L).add(new BsonArray().addNull().add(new BsonArray().add(100000L))).add(2)
                .add(new BsonObject().put("Quantity", 500L).put("SomeObj", new BsonObject().putNull("blah")).put("one", false).putNull("three"))
                .add(new BsonObject().put("Quantity", 200L).put("SomeObj", new BsonObject().putNull("blink")).put("one", true).putNull("four"));
        String expression = "[end].[end]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add(100000L);
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }


    //---------------------------------------------------------------------------------------
    //HorribleTests
    private ByteBuffer buffer = ByteBuffer.allocate(0);
    private byte[] byteArr = new byte[10];

    @Test
    public void ExtractWithWrongKeyV1() {
        String expression = ".Something";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractWithWrongKeyV2() {
        String expression = ".Something[0]";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bE.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractFromEmptyByteArray() {
        String expression = "Price";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(byteArr).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractArrayWhenDontMatch() {
        String expression = ".Book";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractArrayWithLimitWhenDontMatch() {
        String expression = ".Book[0]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractWhenKeyIsInsideKey_V1() {
        BsonObject obj2 = new BsonObject().put("Store", 1000L);
        BsonObject obj1 = new BsonObject().put("Store", obj2);
        String expression = "..Store";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(obj1.encodeToBarray()).thenRun(() -> {
            List<Object> expected = new ArrayList<>();
            expected.add(obj2.encodeToBarray());
            expected.add(1000L);
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractKeyOfAllElemOfArrayWithLimits() {
        String expression = "..Book[0].*.Title";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(bsonApiTests.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractKeyofAllElemsOfArrayRootWithLimitAndDontMatch() {
        String expression = ".[0 to 7].*.Nothing";
        ArrayList<Object> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (Object obj) -> {
            if (obj != null)
                mutableBuffer.add(obj);
        });
        boson.go(arr1.encodeToBarray()).thenRun(() -> {
            assertTrue(mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractWhenKeyIsInsideKey_V2() {
        BsonObject obj3 = new BsonObject().put("Store", new BsonArray());
        BsonArray arr2 = new BsonArray().add(obj3);
        BsonObject obj2 = new BsonObject().put("Store", arr2);
        BsonArray arr1 = new BsonArray().add(obj2);
        BsonObject obj1 = new BsonObject().put("Store", arr1);
        String expression = "..Store[@Store]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(obj1.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(obj2.encodeToBarray());
            expected.add(obj3.encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractWhenKeyIsInsideKey_V3() {
        BsonArray arr = new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("This", new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()))));
        BsonObject obj = new BsonObject().put("This", arr);
        String expression = "This[@some]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(obj.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().put("some", new BsonObject()).put("This", new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()))).encodeToBarray());
            expected.add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()).encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractWhenKeyIsInsideKey_V4() {
        BsonArray arr = new BsonArray().add(new BsonObject().put("Inside", new BsonObject()).put("This", new BsonArray().add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()))));
        BsonObject obj = new BsonObject().put("This", arr);
        String expression = "This[@some]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(obj.encodeToBarray()).thenRun(() -> {
            List<byte[]> expected = new ArrayList<>();
            expected.add(new BsonObject().put("some", new BsonObject()).put("thing", new BsonArray()).encodeToBarray());
            assertArrayEquals(mutableBuffer.toArray(), expected.toArray());
        }).join();
    }

    @Test
    public void ExtractWhenKeyIsInsideKey_V5() {
        BsonObject obj3 = new BsonObject().put("Store", new BsonArray());
        BsonArray arr2 = new BsonArray().add(obj3);
        BsonObject obj2 = new BsonObject().put("NotStore", arr2);
        BsonArray arr1 = new BsonArray().add(obj2);
        BsonObject obj1 = new BsonObject().put("Store", arr1);
        String expression = "..Store[@Store]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(obj1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void ExtractWhenKeyIsInsideKey_V6() {
        BsonObject obj3 = new BsonObject().put("Store", new BsonArray());
        BsonArray arr2 = new BsonArray().add(obj3);
        BsonObject obj2 = new BsonObject().put("NotStore", arr2);
        BsonArray arr1 = new BsonArray().add(obj2);
        BsonObject obj1 = new BsonObject().put("Store", arr1);
        String expression = ".Store[@Store]";
        ArrayList<byte[]> mutableBuffer = new ArrayList<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> {
            mutableBuffer.add(out);
        });
        boson.go(obj1.encodeToBarray()).thenRun(() -> {
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    private <T> void completeFuture(CompletableFuture future, T extractedValue) {
        future.complete(extractedValue);
    }

    @Test
    public void ExtractStringValue() {
        String expression = ".Store.Book[0].Title";
        CompletableFuture<String> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (String out) -> completeFuture(future1, out));
        boson.go(bsonApiTests.encodeToBarray());
        String result = future1.join();
        assertEquals(result, "Java");
    }

    @Test
    public void ExtractDoubleValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30.0);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Price";
        CompletableFuture<Double> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Double out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        Double result = future1.join();
        assertTrue(result == 30.0);
    }

    @Test
    public void ExtractIntegerValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Price";
        CompletableFuture<Integer> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Integer out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        int result = future1.join();
        assertTrue(result == 30);
    }

    @Test
    public void ExtractFloatValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30.0f);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Price";
        CompletableFuture<Float> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Float out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        float result = future1.join();
        assertTrue(result == 30.0f);
    }

    @Test
    public void ExtractBooleanValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30).put("Sale", true);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Sale";
        CompletableFuture<Boolean> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Boolean out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        boolean result = future1.join();
        assertTrue(result);
    }

    @Test
    public void ExtractLongValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30L);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Price";
        CompletableFuture<Long> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Long out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        long result = future1.join();
        assertTrue(result == 30L);
    }


    @Test
    public void ExtractInstantValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30L).put("Instant", Instant.EPOCH);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Instant";
        CompletableFuture<Instant> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Instant out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        Instant result = future1.join();
        assertEquals(result, Instant.EPOCH);
    }

    @Test
    public void ExtractByteArrayValue() {
        BsonObject book = new BsonObject().put("Title", "Java");
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book";
        CompletableFuture<byte[]> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (byte[] out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        byte[] result = future1.join();
        assertArrayEquals(new BsonObject().put("Title", "Java").encodeToBarray(), result);
    }

    @Test
    public void ExtractCharSequenceValue() {
        CharSequence charseq = "Java";
        BsonObject book = new BsonObject().put("Title", charseq);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Title";
        CompletableFuture<CharSequence> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (CharSequence out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        CharSequence result = future1.join();
        assertEquals(result, charseq);
    }

    @Test
    public void ExtractObjectValue() {
        BsonObject book = new BsonObject().put("Title", "Java").put("Price", 30L);
        BsonObject store = new BsonObject().put("Book", book);
        BsonObject bson = new BsonObject().put("Store", store);

        String expression = ".Store.Book.Price";
        CompletableFuture<Object> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Object out) -> completeFuture(future1, out));
        boson.go(bson.encodeToBarray());
        Object result = future1.join();
        assertEquals(result, 30L);
    }

    @Test
    public void TypeInferenceExample() {

        String expression = ".Store.Book[0].Title";
        CompletableFuture<String> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (String title) -> {
            future1.complete(title);
        });
        boson.go(bsonApiTests.encodeToBarray());
        assert (future1.join() instanceof String);
    }

    @Test
    public void Extract_Obj_As_Class() {
        BsonObject title1 = new BsonObject().put("Title", "Scala").put("Price", 15.6);
        BsonArray books = new BsonArray().add(title1);

        String expression = ".[0]";
        CompletableFuture<Book> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Book book) -> {
            future1.complete(book);
        });
        boson.go(books.encodeToBarray());
        Book result = future1.join();
        Book expected = new Book("Scala", 15.6);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));
    }

    @Test
    public void Extract_Obj_As_Class_With_Nested_Class() {
        BsonObject sEdition1 = new BsonObject().put("Title", "ScalaMachine").put("Price", 39);
        BsonObject title1 = new BsonObject().put("Title", "Scala").put("Price", 15.6).put("SpecialEditions", sEdition1);
        BsonArray books = new BsonArray().add(title1);

        String expression = ".[0]";
        CompletableFuture<Book1> future1 = new CompletableFuture<>();
        Boson boson = Boson.extractor(expression, (Book1 book) -> {
            future1.complete(book);
        });
        boson.go(books.encodeToBarray());

        Book1 result = future1.join();

        SpecialEditions sEdtn = new SpecialEditions("ScalaMachine", 39);
        Book1 expected = new Book1("Scala", 15.6, sEdtn);

        Assert.assertTrue(result.title.equals(expected.title));
        Assert.assertTrue(result.price.equals(expected.price));
        Assert.assertTrue(result.specialEditions.title.equals(expected.specialEditions.title));
        Assert.assertTrue(result.specialEditions.price == expected.specialEditions.price);
    }

    @Test
    public void VALUE_nestedMultiKeyInj_Integer() {
        BsonObject person = new BsonObject().put("person", johnDoeBson);
        BsonObject bson = new BsonObject().put("client", person);
        String ex = ".client.person.age";
        Boson boson = Boson.injector(ex, 20);
        byte[] bsonEncoded = bson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 20) && resultValue.length == bsonEncoded.length);
        }).join();
    }

    @Test
    public void VALUE_CaseClassInjection_All() {
        BsonArray booksExpectedValue = new BsonArray().add(bsonBookExpected).add(bsonBookExpected);
        BsonObject storeExpectedValue = new BsonObject().put("books", booksExpectedValue);
        BsonObject storeBsonExpectedValue = new BsonObject().put("store", storeExpectedValue);

        String ex = ".store.books[all].book";
        Boson bsonInj = Boson.injector(ex, new BookAux("Title1", 101));
        byte[] bsonEncoded = storeBson.encodeToBarray();
        bsonInj.go(bsonEncoded).thenAccept(resultValue -> {
            assertArrayEquals(resultValue, storeBsonExpectedValue.encodeToBarray());
        }).join();
    }
}
