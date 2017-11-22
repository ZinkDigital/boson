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
        return new Boson(
                Option.apply(byteArray),
                Option.apply(null),
                Option.apply(null)
        );
    }

    public Boson createNettyBson(ByteBuffer byteBuffer) {
        return new Boson(
                Option.apply(null),
                Option.apply(byteBuffer),
                Option.apply(null)
        );
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

