package io.boson;

import bsonLib.BsonObject;
import bsonLib.BsonArray;
import io.boson.bson.bsonValue.*;
import io.boson.javaInterface.JavaInterface;
import io.boson.bson.bsonImpl.BosonImpl;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import scala.collection.Seq;
import scala.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


public class JavaInterfaceTest {

    private BsonArray br4 = new BsonArray().add("Insecticida");
    private BsonArray br1 = new BsonArray().add("Tarantula").add("Aracnídius").add(br4);
    private BsonObject obj1 = new BsonObject().put("José", br1);
    private BsonArray br2 = new BsonArray().add("Spider");
    private BsonObject obj2 = new BsonObject().put("José", br2);
    private BsonArray br3 = new BsonArray().add("Fly");
    private BsonObject obj3 = new BsonObject().put("José", br3);

    private BsonArray arr = new BsonArray().add(obj1).add(obj2).add(obj3).add(br4);
    private BsonObject bsonEvent = new BsonObject().put("StartUp", arr);

//
//    @Test
//    public void extractExceptionWithJavaInterface() {
//
//        JavaInterface jI = new JavaInterface();
//        String key = "José";
//        String expression = "[-8 to 5]";
//        BosonImpl boson = jI.createBoson(bsonEvent.encode().getBytes());
//
//        BsValue result = jI.parse(boson, key, expression);
//
//        assertEquals(BsException$.MODULE$.apply("Failure/Error parsing!"), result);
//    }
//
//    @Test
//    public void extractSetWithJavaInterface() {
//        Seq<Object> seq = null;
//        Boolean bool = null;
//        BigDecimal bD = null;
//
//        JavaInterface jI = new JavaInterface();
//        String key = "José";
//        String expression = "[0 until 4]";
//        BosonImpl boson = jI.createBoson(bsonEvent.encode().getBytes());
//
//        BsValue result = jI.parse(boson, key, expression);
//
//        if(result instanceof BsSeq){
//            BsSeq newResult = (BsSeq) result;
//            seq = newResult.getValue();
//        } else if (result instanceof BsBoolean) {
//            BsBoolean newResult = (BsBoolean) result;
//            bool = newResult.getValue();
//        } else if(result instanceof BsNumber) {
//            BsNumber newResult = (BsNumber) result;
//            bD = newResult.getValue();
//        }
//
//        ArrayList<Object> list = new ArrayList<>(); ArrayList<Object> list1 = new ArrayList<>(); ArrayList<Object> list2 = new ArrayList<>();
//        ArrayList<Object> list3 = new ArrayList<>(); ArrayList<Object> list4 = new ArrayList<>();
//        list1.add("Tarantula"); list1.add("Aracnídius"); list2.add("Insecticida"); list1.add(list2);
//        list.add(list1);
//        list3.add("Spider");
//        list.add(list3);
//        list4.add("Fly");
//        list.add(list4);
//
//        System.out.println("JavaList -> " + list);
//        List<Object> convertedList = jI.convert(seq);
//        System.out.println("List converted from Scala ->" + convertedList);
//
//        assertEquals(list, convertedList);
//    }
//
//    @Test
//    public void extractIntWithJavaInterface() {
//        BigDecimal bD = null;
//
//        JavaInterface jI = new JavaInterface();
//        String key = "";
//        String expression = "all size";
//        BosonImpl boson = jI.createBoson(arr.encode().getBytes());
//
//        BsValue result = jI.parse(boson, key, expression);
//
//        if(result instanceof BsNumber) {
//            BsNumber newResult = (BsNumber) result;
//            bD = newResult.getValue();
//        }
//        BigDecimal val = BigDecimal.binary(arr.size());
//
//        assertEquals(BsNumber$.MODULE$.apply(val).getValue(), bD);
//    }
//
//    @Test
//    public void extractBoolWithJavaInterface() {
//        Boolean bool = null;
//
//        JavaInterface jI = new JavaInterface();
//        String key = "";
//        String expression = "first isEmpty";
//        BosonImpl boson = jI.createBoson(arr.encode().getBytes());
//
//        BsValue result = jI.parse(boson, key, expression);
//
//        if (result instanceof BsBoolean) {
//            BsBoolean newResult = (BsBoolean) result;
//            bool = newResult.getValue();
//        }
//        assertEquals(false, bool);
//        System.out.println("passed");
//    }
}
