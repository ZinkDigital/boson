package io.boson;

/**
 * Created by Ricardo Martins on 09/02/2018.
 */
import bsonLib.Bson;
import bsonLib.BsonArray;
import bsonLib.BsonObject;
import io.boson.bson.Boson;
import io.boson.bson.bsonImpl.BosonImpl;
import io.boson.bson.bsonValue.BsValue;
import io.boson.json.Joson;
import io.netty.buffer.ByteBuf;
import mapper.Mapper;
import org.junit.Test;
import scala.Option;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import scala.collection.JavaConverters.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InjectorAPITestsJ {

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
    private  BsonArray books = new BsonArray().add(book1).add(book2).add(book3);
    private  BsonObject library = new BsonObject().put("Books", books).put("Articles", articles).put("Magazines", magazines);
    private  BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
    private  BsonObject root = new BsonObject().put("Store", services);

    /*
    * KEY
    * */
    @Test
    public void KEY_test_V1() {
        BsonArray services = new BsonArray();
        BsonObject rootx = new BsonObject().put("Store", services);

        String expression = ".Store";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bl) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V5() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", 10L);

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = ".Store.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V7() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", 10L);

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = "..Store.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void KEY_test_V8() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", 10L);

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = ".Stre.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * HALFNAME
    * */
    @Test
    public void HALFNAME_test_V1() {
        BsonArray services = new BsonArray();
        BsonObject rootx = new BsonObject().put("Store", services);

        String expression = ".Sto*";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bl) -> new BsonArray().encodeToBarray());
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V5() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", 10L);

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = ".*tore.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V7() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", 10L);

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = "..St*re.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    @Test
    public void HALFNAME_test_V8() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", 10L);

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = ".Str*.[0]";
        Boson bosonInjector = Boson.injector(expression,  (Long value) -> value+1L);
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * ROOT
    * */
    @Test
    public void ROOT_test_V1() {
        //BsonArray servicesx = new BsonArray().add(11L).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject();

        //BsonArray services = new BsonArray().add(10L).add(gameRoom).add(coffee);
        BsonObject root =  new BsonObject().put("Store", 10L);

        String expression = ".";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * HASELEM
    * */
    @Test
    public void HASELEM_test_V1() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(gameRoom).add(coffee);
        BsonObject rootx = new BsonObject().put("Store", servicesx);

        String expression = ".Store[@Books]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) -> new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }
    /*
    * ARR
    * */
    @Test
    public void ARR_test_V1() {
        BsonArray servicesx = new BsonArray().add(new BsonObject()).add(new BsonObject()).add(new BsonObject());
        BsonObject rootx = new BsonObject().put("Store", servicesx);
        String expression = ".Store.[0 to end]";
        Boson bosonInjector = Boson.injector(expression,  (byte[] bo) ->new BsonObject().encodeToBarray() );
        CompletableFuture<byte[]> midResult1 = bosonInjector.go(root.encodeToBarray());
        byte[] result = midResult1.join();
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
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
        System.out.println(Arrays.toString(result));
        System.out.println(Arrays.toString(rootx.encodeToBarray()));
        assertArrayEquals(rootx.encodeToBarray(), result );
    }

}
/*
        BsonObject spirit = new BsonObject().put("name", "SpiritualDrink");
        BsonObject wine = new BsonObject().put("name", "Wine");
        BsonObject beer = new BsonObject().put("name", "Beer");
        BsonArray alcoholic = new BsonArray().add(beer).add(wine).add(spirit);
        BsonObject water = new BsonObject().put("name", "Water");
        BsonObject sumol = new BsonObject().put("name", "Sumol");
        BsonObject coca = new BsonObject().put("name", "Coca-Cola");
        BsonArray nonAlcoholic = new BsonArray().add(coca).add(sumol).add(water);
        BsonObject drinks = new BsonObject().put("Non-Alcoholic", nonAlcoholic).put("Alcoholic", alcoholic);
        BsonObject menu3 = new BsonObject().put("name", "Menu3");
        BsonObject menu2 = new BsonObject().put("name", "Menu2");
        BsonObject menu1 = new BsonObject().put("name", "Menu1");
        BsonArray menus = new BsonArray().add(menu1).add(menu2).add(menu3);
        BsonObject natura = new BsonObject().put("name", "Natura");
        BsonObject specialHD = new BsonObject().put("name", "Special");
        BsonObject normal = new BsonObject().put("name", "Normal");
        BsonArray hotdogs = new BsonArray().add(normal).add(specialHD).add(natura);
        BsonObject chicken = new BsonObject().put("name", "Chicken");
        BsonObject specialS = new BsonObject().put("name", "Special");
        BsonObject mix = new BsonObject().put("name", "Mix");
        BsonArray sandwichs = new BsonArray().add(mix).add(specialS).add(chicken);
        BsonObject food = new BsonObject().put("Sandwichs", sandwichs ).put("HotDogs", hotdogs).put("Menus", menus);
        BsonObject coffee = new BsonObject().put("Food", food).put("Drinks", drinks).put("TakeAway?", true);
        BsonObject pinball = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 0.5f);
        BsonObject kingkong = new BsonObject().put("#players", 2).put("TeamGame?", false).put("cost", 0.5f);
        BsonObject soccer = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 0.5f);
        BsonObject arcadeGames = new BsonObject().put("Pinball", pinball).put("KingKong", kingkong).put("Soccer", soccer);
        BsonObject peixinho = new BsonObject().put("#players", 8).put("TeamGame?", false).put("cost", 2.0f);
        BsonObject italiana = new BsonObject().put("#players", 5).put("TeamGame?", true).put("cost", 2.0f);
        BsonObject sueca = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 2.0f);
        BsonObject cardGames = new BsonObject().put("Sueca", sueca).put("Italiana", italiana).put("Peixinho", peixinho);
        BsonObject trivialPursuit = new BsonObject().put("#players", 4).put("TeamGame?", false).put("cost", 3.0f);
        BsonObject mysterium = new BsonObject().put("#players", 8).put("TeamGame?", true).put("cost", 3.0f);
        BsonObject monopoly = new BsonObject().put("#players", 6).put("TeamGame?", false).put("cost", 3.0f);
        BsonObject boardGames = new BsonObject().put("Monopoly", monopoly).put("Mysterium", mysterium).put("TrivialPursuit", trivialPursuit);
        BsonObject pingpong = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 2.5f);
        BsonObject slippers = new BsonObject().put("#players", 2).put("TeamGame?", false).put("cost", 2.5f);
        BsonObject snooker = new BsonObject().put("#players", 4).put("TeamGame?", true).put("cost", 2.5f);
        BsonObject tableGames = new BsonObject().put("Snooker", snooker).put("Slippers", slippers).put("PingPong", pingpong);
        BsonObject gameRoom = new BsonObject().put("TableGame", tableGames).put("BoardGames", boardGames).put("ArcadeGames", arcadeGames).put("CardGames", cardGames);
        BsonObject magazine3 = new BsonObject().put("Title", "C++Magazine").put("Price", 9.99).put("InStock", 15L);
        BsonObject magazine2 = new BsonObject().put("Title", "JavaMagazine").put("Price", 4.99).put("InStock", 10L);
        BsonObject magazine1 = new BsonObject().put("Title", "ScalaMagazine").put("Price", 1.99).put("InStock", 5L);
        BsonArray magazines = new BsonArray().add(magazine1).add(magazine2).add(magazine3);
        BsonObject article3 = new BsonObject().put("Title", "C++Article").put("Price", 29.99).put("available", true );
        BsonObject article2 = new BsonObject().put("Title", "JavaArticle").put("Price", 24.99).put("available", true );
        BsonObject article1 = new BsonObject().put("Title", "ScalaArticle").put("Price", 19.99).put("available", true );
        BsonArray articles = new BsonArray().add(article1).add(article2).add(article3);
        BsonObject book3 = new BsonObject().put("Title", "C++").put("Price", 29.99).put("InStock", 15);
        BsonObject book2 = new BsonObject().put("Title", "Java").put("Price", 24.99).put("InStock", 10);
        BsonObject book1 = new BsonObject().put("Title", "Scala").put("Price", 19.99).put("InStock", 5);
        BsonArray books = new BsonArray().add(book1).add(book2).add(book3);
        BsonObject library = new BsonObject().put("Books", books).put("Articles", articles).put("Magazines", magazines);
        BsonArray services = new BsonArray().add(library).add(gameRoom).add(coffee);
        BsonObject root = new BsonObject().put("Store", services);


*/