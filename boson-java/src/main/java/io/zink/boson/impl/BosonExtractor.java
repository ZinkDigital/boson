package io.zink.boson.impl;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonImpl.CustomException;
import io.zink.boson.bson.bsonPath.*;
//import io.zink.boson.bson.bsonValue.BsException$;
//import io.zink.boson.bson.bsonValue.BsObject$;
//import io.zink.boson.bson.bsonValue.BsValue;
//import io.zink.boson.bson.bsonValue.Writes$;
import io.zink.boson.Boson;

import net.jodah.typetools.TypeResolver;
import scala.*;
import scala.runtime.BoxedUnit;
import scala.util.Left$;
import shapeless.TypeCase;
import shapeless.TypeCase$;
import shapeless.Typeable;
import shapeless.Typeable$;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonExtractor<T> implements Boson {

    private Consumer<T> extractFunction;
    private Interpreter<T> interpreter;

    public BosonExtractor(String expression, Consumer<T> _extractFunction) {
        this.extractFunction = _extractFunction;
        Function1<T, BoxedUnit> anon = new Function1<T, BoxedUnit>() {
            @Override
            public BoxedUnit apply(T v1) {
                extractFunction.accept(v1);
                return BoxedUnit.UNIT;
            }
        };
        Option<Class<T>> retainedClassOpt = retainConsumerType(_extractFunction);
        BosonImpl boson = new BosonImpl(Option.empty(), Option.empty(), Option.empty());
        if (retainedClassOpt.isDefined()) {
            Typeable<T> typeable = Typeable$.MODULE$.simpleTypeable(retainedClassOpt.get());
            TypeCase<T> typeCase = TypeCase$.MODULE$.apply(typeable);
            interpreter = new Interpreter<>(boson, expression, Option.empty(), Option.apply(anon), Option.apply(typeCase));
        } else {
            interpreter = new Interpreter<>(boson, expression, Option.empty(), Option.apply(anon), Option.empty());
        }
    }

    /**
     * Private method that retains the Type T from the Consumer
     * This method is used in order to create a TypeCase to prevent type erasure
     *
     * @param cons - The Consumer to retain the type T from
     * @param <T>  - The type to be retained
     * @return The retained T from the Consumer
     */
    private static <T> Option<Class<T>> retainConsumerType(Consumer<T> cons) {
        try {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, cons.getClass());
            if (typeArgs[0].getName().equals("[B")) {   //in case T type is a byte[] return a None
                return Option.empty();
            } else {    //Otherwise return the class of T wrapped in a Some
                Class<T> retainedClass = (Class<T>) Class.forName(typeArgs[0].getName());
                return Some.apply(retainedClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Java API could not retain type T from Consumer<T>");
        }
    }

    private void runInterpreter(byte[] bsonEncoded) {
        interpreter.run(Left$.MODULE$.apply(bsonEncoded));
    }

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
