package io.zink.boson.impl;

import io.zink.boson.Boson;
import io.zink.boson.bson.bsonImpl.CustomException;
import io.zink.boson.bson.bsonImpl.Dictionary;
import net.jodah.typetools.TypeResolver;
import scala.Function1;
import scala.Option;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.zink.boson.bson.bsonPath.Interpreter;
import scala.Some;
import scala.util.Left$;
import scala.util.Right$;
import shapeless.TypeCase;
import shapeless.TypeCase$;
import shapeless.Typeable;
import shapeless.Typeable$;


public class BosonInjector<T> implements Boson {
    private String expression;
    private Function1<T, T> anon;
    private Interpreter<T> interpreter;
    private Class<T> clazz;
    private Boolean typeIsClass;
    private Constructor<?> constructor;
    private Field[] fields;
    private List<String> keyNames;
    private List<Class<?>> keyTypes;

    public BosonInjector(String expression, Function<T, T> injectFunction) {
        this.expression = expression;
        this.anon = new Function1<T, T>() {
            @Override
            public T apply(T v1) {
                return injectFunction.apply(v1);
            }
        };
        Option<Class<T>> retainedClassOpt = retainConsumerType(injectFunction);
        if (retainedClassOpt.isDefined()) {
            this.clazz = retainedClassOpt.get();
            int counter = 0;
            for (int i = 0; i < Dictionary.TYPES_LIST().size(); i++) {
                if (!clazz.getSimpleName().equals(Dictionary.TYPES_LIST().get(i))) {
                    counter++;
                }
            }
            typeIsClass = counter >= Dictionary.TYPES_LIST().size();
            if (typeIsClass) {
                constructor = clazz.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                fields = clazz.getDeclaredFields();
                keyNames = new LinkedList<>();
                keyTypes = new LinkedList<>();
                for (Field field : fields) {
                    keyNames.add(field.getName());
                    keyTypes.add(field.getType());
                }
            }
            Typeable<T> typeable = Typeable$.MODULE$.simpleTypeable(retainedClassOpt.get());
            TypeCase<T> typeCase = TypeCase$.MODULE$.apply(typeable);
            this.interpreter = new Interpreter<T>(expression, Option.apply(this.anon), Option.empty(), Option.apply(typeCase));
        } else {
            typeIsClass = false;
            this.interpreter = new Interpreter<T>(expression, Option.apply(this.anon), Option.empty(), Option.empty());
        }
    }

    /**
     * Method that delegates the injection process to Interpreter passing to it the data structure to be used (either a byte array or a String)
     *
     * @param bsonEncoded - Data structure to be used in the injection process
     */
    private byte[] runInterpreter(byte[] bsonEncoded) {
        scala.util.Left<byte[], String> castedResult = (scala.util.Left<byte[], String>) interpreter.run(Left$.MODULE$.apply(bsonEncoded));
        return castedResult.value();
    }

    /**
     * Method that delegates the injection process to Interpreter passing to it the data structure to be used (either a byte array or a String)
     *
     * @param bsonEncoded - Data structure to be used in the injection process
     */
    private String runInterpreter(String bsonEncoded) {
        scala.util.Right<byte[], String> castedResult = (scala.util.Right<byte[], String>) interpreter.run(Right$.MODULE$.apply(bsonEncoded));
        return castedResult.value();
    }

    /**
     * Private method that retains the Type T from the Consumer
     * This method is used in order to create a TypeCase to prevent type erasure
     *
     * @param cons - The Consumer to retain the type T from
     * @param <T>  - The type to be retained
     * @return The retained T from the Consumer
     */
    private static <T> Option<Class<T>> retainConsumerType(Function<T, T> cons) {
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

    /**
     * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
     * the future with the resulting byte array. In the case of an Extractor this will result in
     * the immutable byte array being returned unmodified.
     *
     * @param bsonByteEncoding bson encoded into a byte array
     * @return CompletableFuture with original or a modified byte array.
     */
    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            return runInterpreter(bsonByteEncoding);
        });
    }

    /**
     * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
     * the future with the resulting byte array. In the case of an Extractor tis will result in
     * the immutable byte array being returned unmodified.
     *
     * @param bsonByteEncoding bson encoded into a String
     * @return CompletableFuture with original or a modified String.
     */
    @Override
    public CompletableFuture<String> go(String bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            return runInterpreter(bsonByteEncoding);
        });
    }

    /**
     * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
     * the future with the resulting byte array. In the case of an Extractor tis will result in
     * the immutable byte array being returned unmodified.
     *
     * @param bsonByteBufferEncoding byte array encoded wrapped in a ByteBuffer.
     * @return CompletableFuture with original or a modified ByteBuffer.
     */
    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            return bsonByteBufferEncoding;
        });
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this, boson);
    }
}
