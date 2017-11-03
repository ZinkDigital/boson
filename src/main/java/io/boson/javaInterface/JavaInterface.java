package io.boson.javaInterface;

import io.boson.bson.BsonArray;
import io.boson.bson.BsonObject;
import io.boson.bsonPath.Interpreter;
import io.boson.bsonPath.Program;
import io.boson.bsonPath.TinyLanguage;
import io.boson.nettybson.NettyBson;
import io.netty.buffer.ByteBuf;
import scala.Option;
import scala.util.parsing.combinator.Parsers;
import java.nio.ByteBuffer;

/**
 * Created by Tiago Filipe on 03/11/2017.
 */
public class JavaInterface {

    public static void main(String [] args) {
        BsonArray br4 = new BsonArray().add("Insecticida");
        BsonArray br1 = new BsonArray().add("Tarantula").add("Aracnídius").add(br4);
        BsonObject obj1 = new BsonObject().put("José", br1);
        BsonArray br2 = new BsonArray().add("Spider");
        BsonObject obj2 = new BsonObject().put("José", br2);
        BsonArray br3 = new BsonArray().add("Fly");
        BsonObject obj3 = new BsonObject().put("José", br3);

        BsonArray arr = new BsonArray().add(obj1).add(obj2).add(obj3).add(br4);
        BsonObject bsonEvent = new BsonObject().put("StartUp", arr);

        BsonArray arrTest = new BsonArray().add(2.2).add(2.4).add(2.6);

        JavaInterface jI =  new JavaInterface();
        NettyBson netty = jI.createNettyBson(bsonEvent.encode());
        String language = "first";
         Object result = jI.parse(netty, "José", language);

         System.out.println(result);

    }

    public NettyBson createNettyBson(byte[] byteArray){
        Option<io.netty.buffer.ByteBuf> op = Option.apply(null);
        Option<java.nio.ByteBuffer> op1 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new NettyBson( Option.apply(byteArray), op,op1, op2, op3);
    }

    public NettyBson createNettyBson(ByteBuf byteBuf){
        Option<byte[]> op = Option.apply(null);
        Option<java.nio.ByteBuffer> op1 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new NettyBson( op, Option.apply(byteBuf),op1, op2, op3);
    }

    public NettyBson createNettyBson(ByteBuffer byteBuffer){
        Option<byte[]> op = Option.apply(null);
        Option<io.netty.buffer.ByteBuf> op1 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new NettyBson( op, op1, Option.apply(byteBuffer), op2, op3);
    }

    public NettyBson createNettyBson(io.vertx.core.buffer.Buffer vertxBuffer){
        Option<byte[]> op = Option.apply(null);
        Option<io.netty.buffer.ByteBuf> op1 = Option.apply(null);
        Option<java.nio.ByteBuffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new NettyBson( op, op1, op2, Option.apply(vertxBuffer) , op3);
    }

    public NettyBson createNettyBson(scala.collection.mutable.ArrayBuffer<java.lang.Object> arrayBuffer){
        Option<byte[]> op = Option.apply(null);
        Option<io.netty.buffer.ByteBuf> op1 = Option.apply(null);
        Option<java.nio.ByteBuffer> op2 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op3 = Option.apply(null);
        return new NettyBson( op, op1, op2, op3 ,Option.apply(arrayBuffer));
    }


//    String key = "José";
//    String language = "all isEmpty";

    public Object parse(NettyBson netty, String key, String expression){
        TinyLanguage parser = new TinyLanguage();
        Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
        Object result = null;
        try {
            if (pr.successful()) {
                System.out.println("Success");
                Interpreter interpreter = new Interpreter(netty, key, (Program) pr.get());
                result = interpreter.run();
                System.out.println(result);

            } else {
                System.out.println("Failure or Error");
                result = pr.get();
                System.out.println(result);
            }
        }catch(RuntimeException e){
            System.out.println(e.getMessage());
        }

        return result;
    }
/*
    val parser = new TinyLanguage
    parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
            val interpreter = new Interpreter(netty, key, r.asInstanceOf[Program])
            try {
                interpreter.run()
            } catch {
            case e: RuntimeException => println("Error inside run() " + e.getMessage)
        }
        case parser.Error(msg, _) => throw new RuntimeException("Error parsing: " + msg)
        case parser.Failure(msg, _) => throw new RuntimeException("Failure parsing: " + msg)
    }*/
}
