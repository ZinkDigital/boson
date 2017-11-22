package io.boson.javaInterface;


import io.boson.bsonPath.Interpreter;
import io.boson.bsonPath.Program;
import io.boson.bsonPath.TinyLanguage;
import io.boson.nettyboson.Boson;
import io.netty.buffer.ByteBuf;
import scala.Function1;
import scala.Option;
import scala.util.parsing.combinator.Parsers;
import java.nio.ByteBuffer;
import io.boson.bsonValue.*;


public class JavaInterface {

    public Boson createNettyBson(byte[] byteArray) {
        Option<io.netty.buffer.ByteBuf> op = Option.apply(null);
        Option<java.nio.ByteBuffer> op1 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new Boson(Option.apply(byteArray), op, op1, op2, op3);
    }

    public Boson createNettyBson(ByteBuf byteBuf) {
        Option<byte[]> op = Option.apply(null);
        Option<java.nio.ByteBuffer> op1 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new Boson(op, Option.apply(byteBuf), op1, op2, op3);
    }

    public Boson createNettyBson(ByteBuffer byteBuffer) {
        Option<byte[]> op = Option.apply(null);
        Option<io.netty.buffer.ByteBuf> op1 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new Boson(op, op1, Option.apply(byteBuffer), op2, op3);
    }

    public Boson createNettyBson(io.vertx.core.buffer.Buffer vertxBuffer) {
        Option<byte[]> op = Option.apply(null);
        Option<io.netty.buffer.ByteBuf> op1 = Option.apply(null);
        Option<java.nio.ByteBuffer> op2 = Option.apply(null);
        Option<scala.collection.mutable.ArrayBuffer<java.lang.Object>> op3 = Option.apply(null);
        return new Boson(op, op1, op2, Option.apply(vertxBuffer), op3);
    }

    public Boson createNettyBson(scala.collection.mutable.ArrayBuffer<java.lang.Object> arrayBuffer) {
        Option<byte[]> op = Option.apply(null);
        Option<io.netty.buffer.ByteBuf> op1 = Option.apply(null);
        Option<java.nio.ByteBuffer> op2 = Option.apply(null);
        Option<io.vertx.core.buffer.Buffer> op3 = Option.apply(null);
        return new Boson(op, op1, op2, op3, Option.apply(arrayBuffer));
    }

    private Function1<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    public BsValue parse(Boson netty, String key, String expression) {
        TinyLanguage parser = new TinyLanguage();
        try {
            Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
            if (pr.successful()) {
                Interpreter interpreter = new Interpreter(netty, key, (Program) pr.get());
                return interpreter.run();
            } else {
                return BsObject$.MODULE$.toBson("Failure/Error parsing!", Writes$.MODULE$.apply(writer));
            }
        } catch (RuntimeException e) {
            return BsObject$.MODULE$.toBson("Error inside interpreter.run() ", Writes$.MODULE$.apply(writer));
        }
    }


    public java.util.List<Object> convert(scala.collection.Seq<Object> seq) {
        return scala.collection.JavaConverters.seqAsJavaList(seq);
    }
}

