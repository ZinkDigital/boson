//package io.zink.joson;
//
//import bsonLib.BsonArray;
//import bsonLib.BsonObject;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import de.undercouch.bson4jackson.BsonFactory;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//
//import io.zink.boson.bson.bsonImpl.BosonImpl;
//
//import io.zink.boson.Boson;
//import mapper.Mapper;
//import org.junit.Test;
//import scala.Option;
//import scala.collection.JavaConverters;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//import static org.junit.Assert.assertArrayEquals;
//
//
//public class APITestsJ {
//
//  private BsonObject hat3 = new BsonObject().put("Price", 38).put("Color", "Blue");
//  private BsonObject hat2 = new BsonObject().put("Price", 35).put("Color", "White");
//  private BsonObject hat1 = new BsonObject().put("Price", 48).put("Color", "Red");
//  private BsonArray hats = new BsonArray().add(hat1).add(hat2).add(hat3);
//  private BsonObject edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38);
//  private BsonArray sEditions3 = new BsonArray().add(edition3);
//  private BsonObject title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3);
//  private BsonObject edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40);
//  private BsonArray sEditions2 = new BsonArray().add(edition2);
//  private BsonObject title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2);
//  private BsonObject edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39);
//  private BsonArray sEditions1 = new BsonArray().add(edition1);
//  private BsonObject title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1);
//  private BsonArray books = new BsonArray().add(title1).add(title2).add(title3);
//  private BsonObject store = new BsonObject().put("Book", books).put("Hat", hats);
//  private BsonObject bson  = new BsonObject().put("Store", store);
//
//    private String json = "{\"Store\":{\"Book\":[{\"Title\":\"Java\",\"Price\":15.5,\"SpecialEditions\":[{\"Title\":\"JavaMachine\",\"Price\":39}]},{\"Title\":\"Scala\",\"Price\":21.5,\"SpecialEditions\":[{\"Title\":\"ScalaMachine\",\"Price\":40}]},{\"Title\":\"C++\",\"Price\":12.6,\"SpecialEditions\":[{\"Title\":\"C++Machine\",\"Price\":38}]}],\"Hat\":[{\"Price\":48,\"Color\":\"Red\"},{\"Price\":35,\"Color\":\"White\"},{\"Price\":38,\"Color\":\"Blue\"}]}}";
//
//    @Test
//    public void JSON_test_V1() {
//        //String => Json Object
//        JsonObject a = new JsonObject(json);
//        //Set the Mapper with JsonObject and JsonArray Serializer
//        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(JsonObject.class,new Joson.JsonObjectSerializer());
//        module.addSerializer(JsonArray.class, new Joson.JsonArraySerializer());
//        mapper.registerModule(module);
//        //convert JsonObject
//        try {
//            mapper.writeValue(os, a);
//            //convert from byte[] to JsonNode
//            JsonNode s = mapper.readTree(os.toByteArray());
//            os.flush();
//            //read and print json received and original json
//            System.out.println(s.toString());
//            System.out.println(json);
//        }catch (IOException e){
//            System.out.println(e.getMessage());
//        }
//    }
//    @Test
//    public void JOSON_test_V2(){ // joson == boson
//
//        String expression = "Store";
//        System.out.println("WORK WITH JOSON\n");
//        System.out.println("|-------- Perform Injection --------|\n");
//        Joson joson = Joson.injector(expression, (byte[] x ) -> {
//            BosonImpl b = new BosonImpl(Option.apply(x), Option.empty(), Option.empty());
//            Map<String,Object> m = JavaConverters.mapAsJavaMap(Mapper.decodeBsonObject(b.getByteBuf()));
//            m.put("newField!", 100);
//
//            ByteBuf res0 = Mapper.encode(m);
//                if(res0.hasArray())
//                    return res0.array();
//                else {
//                    ByteBuf buf  = Unpooled.buffer(res0.capacity()).writeBytes(res0);
//                    byte[] array = buf.array();
//                    buf.release();
//                    return array;
//                }
//
//            });
//        CompletableFuture<String> midResult  = joson.go(json);
//        String result = midResult.join();
//        System.out.println("|-------- Perform Extraction --------|\n");
//        CompletableFuture<BsValue> future = new CompletableFuture<>();
//        Joson joson1 = Joson.extractor(expression, future::complete);
//        joson1.go(result);
//
//
//        scala.collection.immutable.Vector<Object> res = (scala.collection.immutable.Vector<Object>)future.join().getValue();
//
//        //byte[] json1 =
//        List<Object> l = scala.collection.JavaConverters.seqAsJavaList(res);
//
//                //.get(0); //.asInstanceOf<Vector<byte[]>>.head
//        System.out.println(l.get(0));
//
//        System.out.println("WORK WITH BOSON\n");
//        System.out.println("|-------- Perform Injection --------|\n");
//        byte[] validBsonArray = bson.encodeToBarray();
//        Boson boson = Boson.injector(expression,(byte[] x) -> {
//            BosonImpl b = new BosonImpl(Option.apply(x), Option.empty(), Option.empty());
//            Map<String,Object> m = JavaConverters.mapAsJavaMap(Mapper.decodeBsonObject(b.getByteBuf()));
//            m.put("newField!", 100);
//            ByteBuf res0 = Mapper.encode(m);
//                if(res0.hasArray())
//                    return res0.array();
//                else {
//                    ByteBuf buf = Unpooled.buffer(res0.capacity()).writeBytes(res0);
//                    byte[] array = buf.array();
//                    buf.release();
//                    return array;
//                }
//            });
//        CompletableFuture<byte[]> midResult1 = boson.go(validBsonArray);
//        byte[] result1 = midResult1.join();
//        System.out.println("|-------- Perform Extraction --------|\n");
//        CompletableFuture<BsValue> future1 = new CompletableFuture<>();
//        Boson boson1 = Boson.extractor(expression, future1::complete);
//        boson1.go(result1);
//        scala.collection.immutable.Vector<Object> res1 = (scala.collection.immutable.Vector<Object>)future1.join().getValue();
//
//        //byte[] json1 =
//        List<Object> l1 = scala.collection.JavaConverters.seqAsJavaList(res1);
//
//        //.get(0); //.asInstanceOf<Vector<byte[]>>.head
//        System.out.println(l1.get(0));
//
//        System.out.println("|-------- Perform Assertion --------|\n\n");
//        System.out.println(new String((byte[])l.get(0)));
//        System.out.println(new String((byte[])l1.get(0)));
//        assertArrayEquals((byte[])l.get(0),(byte[]) l1.get(0));
//
//
//    }
//}
//
//
//
//
