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
}
