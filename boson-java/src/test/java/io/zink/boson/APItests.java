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

import static org.junit.Assert.*;


public class APItests {

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
            System.out.println(mutableBuffer);
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
            System.out.println(mutableBuffer);
            assert (mutableBuffer.isEmpty());
        }).join();
    }

    @Test
    public void Extract_Coverage_5() {
        BsonArray arr1 = new BsonArray().add(new BsonArray().add(33)).add(false).add(2.2).addNull().add(1000L).add("Hat").add(2)
                .add(new BsonArray().addNull().add(new BsonObject().put("Store", new BsonArray().addNull())));
        BsonObject bE = new BsonObject().put("Store", arr1);
        System.out.println(bE);
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
        System.out.println("APPLIED");
        System.out.println("Extracted type: " + extractedValue.getClass());
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
        System.out.println(result);
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
            System.out.println("title: " + title);
            System.out.println("Class of title: " + title.getClass());
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
}

class Book {
    private String title;
    private Double price;

    public Book(String _title, Double _price) {
        this.title = _title;
        this.price = _price;
    }
}

class Book1 {
    public String title;
    public Double price;
    public SpecialEditions specialEditions;

    public Book1(String _title, Double _price, SpecialEditions _sEditions) {
        this.title = _title;
        this.price = _price;
        this.specialEditions = _sEditions;
    }


}

class SpecialEditions {
    public String title;
    public Integer price;

    public SpecialEditions(String _title, Integer _price) {
        this.title = _title;
        this.price = _price;
    }
}

