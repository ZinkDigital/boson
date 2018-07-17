package io.zink.boson;

import bsonLib.Bson;
import bsonLib.BsonArray;
import bsonLib.BsonObject;
import org.junit.Test;

import static org.junit.Assert.*;

import java.time.Instant;

public class InjectorsTestJava {

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

    private final BsonObject nestedAuthorExpected = new BsonObject().put("firstName", "JOHN").put("lastName", "DOE").put("age", 41);
    private final BsonObject nestedBookExpected = new BsonObject().put("name", "SOME BOOK").put("pages", 200).put("author", nestedAuthorExpected);
    private final BsonObject nestedBsonExpected = new BsonObject().put("book", nestedBookExpected);


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
}

class BookAux {
    private String name;
    private int pages;

    public BookAux(String name, int pages) {
        this.name = name;
        this.pages = pages;
    }

    public String getName() {
        return name;
    }

    public int getPages() {
        return pages;
    }
}

class BookAux1 {
    private double pages;
    private long someLong;
    private boolean someBoolean;

    public BookAux1(double pages, long someLong, boolean someBoolean) {
        this.pages = pages;
        this.someLong = someLong;
        this.someBoolean = someBoolean;
    }

    public double getPages() {
        return pages;
    }

    public long getSomeLong() {
        return someLong;
    }

    public boolean isSomeBoolean() {
        return someBoolean;
    }
}

class NestedBook {
    private String name;
    private int pages;
    private Author author;

    public NestedBook(String name, int pages, Author author) {
        this.name = name;
        this.pages = pages;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public int getPages() {
        return pages;
    }

    public Author getAuthor() {
        return author;
    }
}

class Author {
    private String firstName;
    private String lastName;
    private int age;

    public Author(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }
}
