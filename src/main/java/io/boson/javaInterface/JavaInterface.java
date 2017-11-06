package io.boson.javaInterface;

import io.boson.bson.BsonArray;
import io.boson.bson.BsonObject;
import io.boson.bsonPath.Interpreter;
import io.boson.bsonPath.Program;
import io.boson.bsonPath.TinyLanguage;
import io.boson.nettybson.NettyBson;
import io.netty.buffer.ByteBuf;
import scala.Array;
import scala.Option;
import scala.collection.mutable.ListBuffer;
import scala.util.parsing.combinator.Parsers;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import scala.collection.JavaConverters.*;

/**
 * Created by Tiago Filipe on 03/11/2017.
 */
public class JavaInterface {


    public static void main(String [] args)
    {
        JavaInterface JI = new JavaInterface();
        Object result = JI.parse(JI.createNettyBson(new BsonObject().encode()), "ok", "first");


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



    public Object parse(NettyBson netty, String key, String expression){
        TinyLanguage parser = new TinyLanguage();
        Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
        Object result = null;
        try {
            if (pr.successful()) {
                System.out.println("Success");
                Interpreter interpreter = new Interpreter(netty, key, (Program) pr.get());
                //System.out.println(interpreter.run());
                result = interpreter.run();
            } else {
                System.out.println("Failure or Error");
                result = pr.get();
                System.out.println(pr.get());
            }
        }catch(RuntimeException e){
            System.out.println(e.getMessage());
        }

        return result;
    }

}
