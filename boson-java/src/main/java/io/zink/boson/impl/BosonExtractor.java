package io.zink.boson.impl;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.*;
//import io.zink.boson.bson.bsonValue.BsException$;
//import io.zink.boson.bson.bsonValue.BsObject$;
//import io.zink.boson.bson.bsonValue.BsValue;
//import io.zink.boson.bson.bsonValue.Writes$;
import io.zink.boson.Boson;

import org.parboiled2.ParserInput;
import scala.Function1;
import scala.Option;
import scala.Unit;
import scala.runtime.BoxedUnit;
import scala.util.Left$;
import scala.util.Try;
import scala.util.parsing.combinator.Parsers;
import shapeless.TypeCase;
import shapeless.TypeCase$;
import shapeless.Typeable;
import shapeless.Typeable$;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonExtractor<T> implements Boson {

    private Consumer<T> extractFunction;
    private Interpreter<T> interpreter;


    public BosonExtractor(String expression, Consumer<T> _extractFunction) {
        this.extractFunction = _extractFunction;
        Function1<T,BoxedUnit> anon = new Function1<T, BoxedUnit>(){
            @Override
            public BoxedUnit apply(T v1) {
                extractFunction.accept(v1);
                return BoxedUnit.UNIT;
            }
        };
        Typeable<Object> typeable = Typeable$.MODULE$.doubleTypeable(); //Typeable$.MODULE$.dfltTypeable();
        TypeCase<Object> typeCase = TypeCase$.MODULE$.apply(typeable);
        BosonImpl boson = new BosonImpl(Option.empty(), Option.empty(), Option.empty());
        interpreter = new Interpreter<T>(boson, expression, Option.empty(), Option.apply(anon), Option.empty());
        //private Typeable<T> t;  //Typeable$.MODULE$.dfltTypeable()
        //private Option<TypeCase<T>> typeCase = Option.apply(TypeCase$.MODULE$.apply(t));
    }

    private void runInterpreter(byte[] bsonEncoded) {
        interpreter.run(Left$.MODULE$.apply(bsonEncoded));
    }



//    private void callParse(BosonImpl boson, String expression){
//        DSLParser parser = new DSLParser(expression);
//        try{
//            Try<ProgStatement> pr = parser.Parse();
//         if(pr.isSuccess()){
//             Interpreter interpreter = new Interpreter<>(boson, pr.get(), Option.empty(), Option.apply(anon));
//             interpreter.run();
//         }else{
//             throw new RuntimeException("Failure/Error parsing!");
//         }
//        }catch (RuntimeException e){
//            throw new RuntimeException(e.getMessage());
//        }
//    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                runInterpreter(bsonByteEncoding);
                return bsonByteEncoding;
            } catch (Exception ex) {
                extractFunction.accept(null);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            try {
            runInterpreter(bsonByteBufferEncoding.array());
            return bsonByteBufferEncoding;
            } catch (Exception ex) {
                extractFunction.accept(null);
                return null;
            }
        });
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this, boson);
    }
}
