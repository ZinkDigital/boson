package io.zink.boson;

import bsonLib.BsonArray;
import bsonLib.BsonObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

public class InjectorsTestJava {
    private final BsonObject johnDoeBson = new BsonObject().put("name", "John Doe").put("age", 21);
    private final BsonObject janeDoeBson = new BsonObject().put("name", "Jane Doe").put("age", 12);
    private final BsonArray personsArray = new BsonArray().add(johnDoeBson).add(janeDoeBson);
    private final BsonObject personsBson = new BsonObject().put("persons", personsArray);
    private final BsonObject clientBson = new BsonObject().put("client", personsBson);

    private final BsonArray bsonHuman = new BsonArray().add("person1").add("person2").add("person3");
    private final BsonObject bsonObjArray = new BsonObject().put("person", bsonHuman);
    private final byte[] bsonObjArrayEncoded = bsonObjArray.encodeToBarray();

    private final BsonArray bsonAlien = new BsonArray().add("et").add("predator").add("alien");
    private final BsonObject bsonObjArray1 = new BsonObject().put("alien", bsonAlien);

    private final BsonObject bsonEvent = new BsonObject().put("person", bsonHuman).put("alien", bsonAlien);
    private final BsonObject bsonSpeciesObj = new BsonObject().put("species", bsonEvent);
    private final byte[] bsonSpeciesEncoded = bsonSpeciesObj.encodeToBarray();


    /**
     * Private method to display all elements inside a byte array
     *
     * @param arr byte array to be displayed
     */
    private void printArray(byte[] arr) {
        for (byte b : arr) {
            System.out.println(b + " ");
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
        byte[] bsonEncoded = clientBson.encodeToBarray();
        boson.go(bsonEncoded).thenAccept((resultValue) -> {
            assert (containsInteger(resultValue, 41) && containsInteger(resultValue, 32) && resultValue.length == bsonEncoded.length);
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
}
