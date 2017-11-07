package io.boson;

import io.boson.bson.BsonArray;
import io.boson.bson.BsonObject;
import io.boson.javaInterface.JavaInterface;
import io.boson.nettybson.NettyBson;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tiago Filipe on 03/11/2017.
 */
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


    @Test
    public void extractWithJavaInterface() {

        JavaInterface jI = new JavaInterface();
        String key = "José";
        String expression = "all";
        NettyBson netty = jI.createNettyBson(bsonEvent.encode().getBytes());

        Object result = jI.parse(netty, key, expression);
        List list = jI.convert((scala.collection.Seq<Object>)result);



        ArrayList<Object> myList =  new ArrayList<>();
        myList.add(br1);
        myList.add(br2);
        myList.add(br3);

        assertEquals(myList, list);
    }
}
