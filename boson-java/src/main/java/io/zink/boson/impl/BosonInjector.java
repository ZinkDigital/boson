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
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.zink.boson.bson.bsonPath.Interpreter;
import scala.Some;
import scala.Tuple2;
import scala.collection.immutable.Nil;
import scala.collection.immutable.Nil$;
import scala.collection.immutable.Seq;
import scala.collection.immutable.Seq$;
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
            if (typeIsClass) {
                Function1<scala.collection.immutable.List<Tuple2<String, Object>>, T> convertFunction = new Function1<scala.collection.immutable.List<Tuple2<String, Object>>, T>() {
                    @Override
                    public T apply(scala.collection.immutable.List<Tuple2<String, Object>> v1) {
                        return InstantiateExtractedObject(v1);
                    }
                };
                this.interpreter = new Interpreter<T>(expression, Option.apply(this.anon), Option.empty(), Option.apply(typeCase),
                        Option.apply(convertFunction));
            } else {
                this.interpreter = new Interpreter<T>(expression, Option.apply(this.anon), Option.empty(), Option.apply(typeCase), Option.empty());
            }
        } else {
            typeIsClass = false;
            this.interpreter = new Interpreter<T>(expression, Option.apply(this.anon), Option.empty(), Option.empty(), Option.empty());
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

    //validates keys and value types of given class with extracted object
    private Boolean compareKeysAndTypes(ArrayList<Tuple2<String, Object>> list, List<String> _keyNames, List<Class<?>> _keyTypes) {
        Boolean flag = false;
        for (int i = 0; i < _keyNames.size(); i++) {
            Tuple2<String, Object> elem = list.get(i);
            int elemIndex = _keyNames.indexOf(elem._1);
            if (elemIndex != -1) {
                Class<?> keyType = _keyTypes.get(elemIndex);
                Object x = elem._2.getClass().getSimpleName();
                if (elem._2.getClass().equals(keyType)
                        || (keyType.getSimpleName().equals("int") && elem._2.getClass().getSimpleName().equals("Integer"))
                        || (keyType.getSimpleName().equalsIgnoreCase(elem._2.getClass().getSimpleName()))
                        ) {
                    flag = true;
                } else if (elem._2.getClass().equals(ArrayList.class)) {
                    List<String> kNames = new LinkedList<>();
                    List<Class<?>> kTypes = new LinkedList<>();
                    Constructor cons = keyType.getDeclaredConstructors()[0];
                    cons.setAccessible(true);
                    Field[] _fields = keyType.getDeclaredFields();
                    for (Field field : _fields) {
                        kNames.add(field.getName());
                        kTypes.add(field.getType());
                    }
                    flag = compareKeysAndTypes((ArrayList<Tuple2<String, Object>>) elem._2, kNames, kTypes);
                } else flag = false;
            } else flag = false;
        }
        return flag;
    }

    // Verifies if all keys and types are valid and triggers construction
    private Object triggerConstruction(Boolean allKeysAndTypesMatch, ArrayList<Tuple2<String, Object>> list, Class<?> outterClass, List<Class<?>> _keyTypes, List<String> _keyNames) {
        if (allKeysAndTypesMatch) {
            Object obj = instantiateClasses(list, outterClass, _keyTypes, _keyNames);
            return obj;
        }
        return null;
    }

    //triggers the process of construction
    private T InstantiateExtractedObject(scala.collection.immutable.List<Tuple2<String, Object>> _SeqOfTuplesList) {
        Object finalObj = null;
        scala.collection.mutable.ListBuffer<scala.collection.immutable.List<Tuple2<String, Object>>> wrapper = new scala.collection.mutable.ListBuffer<scala.collection.immutable.List<Tuple2<String, Object>>>();
        wrapper.$plus$eq(_SeqOfTuplesList);
        List<scala.collection.immutable.List<Tuple2<String, Object>>> seqOfTuplesList = scala.collection.JavaConverters.seqAsJavaList(wrapper);

//        ArrayList<List<scala.collection.immutable.List<Tuple2<String, Object>>>> listWrapper = new ArrayList<>();
        LinkedList<ArrayList<Tuple2<String, Object>>> javaList = new LinkedList<>();
        for (int i = 0; i < seqOfTuplesList.size(); i++) { // each iteration represents one extracted object
            javaList.add(convertLists(seqOfTuplesList.get(i)));
            Boolean allKeysAndTypesMatch = compareKeysAndTypes(javaList.get(i), keyNames, keyTypes);
            finalObj = triggerConstruction(allKeysAndTypesMatch, javaList.get(i), clazz, keyTypes, keyNames);
        }
        return (T) finalObj;
    }

    // verifies the existence of nested classes and starts to instantiate them from the inside to the outside
    private Object instantiateClasses(ArrayList<Tuple2<String, Object>> list, Class<?> typeClass, List<Class<?>> _keyTypes, List<String> _keyNames) {
        Object instance = null;
        Object innerInstance;
        List<Class<?>> kTypes = new LinkedList<>();
        List<String> kNames = new LinkedList<>();
        ArrayList<Object> values = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)._2 instanceof ArrayList) {
                int elemIndex = _keyNames.indexOf(list.get(i)._1);
                Field[] _fields = _keyTypes.get(elemIndex).getDeclaredFields();
                for (Field field : _fields) {
                    kTypes.add(field.getType());
                    kNames.add(field.getName());
                }
                innerInstance = instantiateClasses((ArrayList<Tuple2<String, Object>>) list.get(i)._2, _keyTypes.get(elemIndex), kTypes, kNames);
                values.add(innerInstance);
            } else {
                values.add(list.get(i)._2);
            }
        }
        Constructor cons = typeClass.getDeclaredConstructors()[0];
        cons.setAccessible(true);
        try {
            Class<?>[] parameterTypes = cons.getParameterTypes();
            ArrayList<Object> sortedValues = new ArrayList<>();
            for (Class<?> parameterType : parameterTypes) {
                for (Object value : values) {
                    if (value.getClass().equals(parameterType)
                            || parameterType.getSimpleName().equals("int") && value.getClass().getSimpleName().equals("Integer")
                            || parameterType.getSimpleName().equalsIgnoreCase(value.getClass().getSimpleName())) {
                        sortedValues.add(value);
                        values.remove(value);
                        break;
                    }
                }
            }
            instance = cons.newInstance(sortedValues.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Convert scala lists into java lists
     *
     * @param scalaList - scala list to be converted
     * @return Return the converted java list
     */
    private java.util.ArrayList<Tuple2<String, Object>> convertLists(scala.collection.immutable.List<Tuple2<String, Object>> scalaList) {
        List<Tuple2<String, Object>> globalList = scala.collection.JavaConverters.seqAsJavaList(scalaList);
        ArrayList<Tuple2<String, Object>> scndList = new ArrayList<>();
        for (int i = 0; i < globalList.size(); i++) {
            Tuple2<String, Object> elem = globalList.get(i);
            if (elem._2 instanceof scala.collection.immutable.List) {
                scndList.add(new Tuple2<>(elem._1, convertLists((scala.collection.immutable.List<Tuple2<String, Object>>) elem._2)));
            } else {
                scndList.add(elem);
            }
        }
        return scndList;
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
