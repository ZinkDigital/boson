package io.boson.javaInterface;


import io.boson.bson.bsonPath.Interpreter;
import io.boson.bson.bsonPath.Program;
import io.boson.bson.bsonPath.TinyLanguage;
import io.boson.bson.bsonImpl.BosonImpl;
import scala.Function1;
import scala.None;
import scala.Option;
import scala.Some;
import scala.util.parsing.combinator.Parsers;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import io.boson.bson.bsonValue.*;

@Deprecated
public class JavaInterface {

    public BosonImpl createBoson(byte[] byteArray) {
        return new BosonImpl(
                Option.apply(byteArray),
                Option.apply(null),
                Option.apply(null)
        );
    }

    public BosonImpl createBoson(ByteBuffer byteBuffer) {
        return new BosonImpl(
                Option.apply(null),
                Option.apply(byteBuffer),
                Option.apply(null)
        );
    }

    private Function1<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    public BsValue parse(BosonImpl boson, String key, String expression) {
        TinyLanguage parser = new TinyLanguage();
        try {
            Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
            if (pr.successful()) {
                Interpreter interpreter = new Interpreter(boson, (Program) pr.get(), null);
                return interpreter.run();
            } else {
                return BsObject$.MODULE$.toBson("Failure/Error parsing!", Writes$.MODULE$.apply(writer));
            }
        } catch (RuntimeException e) {
            return BsObject$.MODULE$.toBson("Error inside interpreter.run() ", Writes$.MODULE$.apply(writer));
        }
    }

    public java.util.List<Object> convert(scala.collection.Seq<Object> seq) {
        List<Object> globalList = scala.collection.JavaConverters.seqAsJavaList(seq);
        List<Object> scndList = new ArrayList<>();
        for (int i = 0; i < globalList.size(); i++) {
            Object elem = globalList.get(i);
            if (elem instanceof scala.collection.Seq) {
                scndList.add(convert((scala.collection.Seq<Object>) elem));
            } else {
                scndList.add(elem);
            }
        }
        return scndList;
    }
}

