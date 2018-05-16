package io.zink.boson.impl;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonImpl.CustomException;
import io.zink.boson.bson.bsonPath.*;
import io.zink.boson.Boson;

import net.jodah.typetools.TypeResolver;
import scala.*;
import scala.collection.immutable.Seq;
import scala.runtime.BoxedUnit;
import scala.util.Left$;
import shapeless.TypeCase;
import shapeless.TypeCase$;
import shapeless.Typeable;
import shapeless.Typeable$;

import java.lang.Boolean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class BosonExtractor<T> implements Boson {

    private Consumer<T> extractFunction;
    private Interpreter<T> interpreter;
    private Class<T> clazz;
    private Constructor<?> constructor;
    private Field[] fields;
    private List<String> keyNames;
    private List<Class<?>> keyTypes;


    public BosonExtractor(String expression, Consumer<T> _extractFunction) {
        this.extractFunction = _extractFunction;
        Function1<T, BoxedUnit> anon = new Function1<T, BoxedUnit>() {
            @Override
            public BoxedUnit apply(T v1) {
                extractFunction.accept(v1);
                return BoxedUnit.UNIT;
            }
        };
        Class<T> retainedClass = retainConsumerType(_extractFunction);
        this.clazz = retainedClass;
        constructor = clazz.getDeclaredConstructors()[0];
        fields = clazz.getDeclaredFields();
        keyNames = new LinkedList<>();
        keyTypes = new LinkedList<>();
        for (Field field: fields) {
            keyNames.add(field.getName());
            //System.out.println("fieldName -> " + field.getName());
            keyTypes.add(field.getType());
            //System.out.println("fieldType -> " + field.getType());
        }
        Typeable<T> typeable = Typeable$.MODULE$.simpleTypeable(retainedClass);
        TypeCase<T> typeCase = TypeCase$.MODULE$.apply(typeable);
        BosonImpl boson = new BosonImpl(Option.empty(), Option.empty(), Option.empty());
        interpreter = new Interpreter<>(boson, expression, Option.empty(), Option.apply(anon), Option.apply(typeCase));
    }

    /**
     * Private method that retains the Type T from the Consumer
     * This method is used in order to create a TypeCase to prevent type erasure
     *
     * @param cons - The Consumer to retain the type T from
     * @param <T>  - The type to be retained
     * @return The retained T from the Consumer
     */
    private static <T> Class<T> retainConsumerType(Consumer<T> cons) {
        try {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(Function.class, cons.getClass());
            Class<T> retainedClass = (Class<T>) Class.forName(typeArgs[0].getName());
            return retainedClass;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Java API could not retain type T from Consumer<T>");
        }
    }

    private Object runInterpreter(byte[] bsonEncoded) {
        return interpreter.run(Left$.MODULE$.apply(bsonEncoded));
    }

    private Object InstantiateExtractedObject(Object extResult) {
        Object finalObj = null;
        Seq<scala.collection.immutable.List<Tuple2<String,Object>>> _SeqOfTuplesList = (Seq<scala.collection.immutable.List<Tuple2<String,Object>>>) extResult;
        List<scala.collection.immutable.List<Tuple2<String,Object>>> seqOfTuplesList = scala.collection.JavaConverters.seqAsJavaList(_SeqOfTuplesList);
        System.out.println("tuples in java: " + seqOfTuplesList);
        LinkedList<ArrayList<Tuple2<String,Object>>> javaList = new LinkedList<>();
        for (int i = 0; i<seqOfTuplesList.size();i++){
            //System.out.println("pos: " + i + " content: " + seqOfTuplesList.get(i));
            javaList.add(convert(seqOfTuplesList.get(i)));
            finalObj = compareKeysAndTypes(javaList.get(i));
        }
        //System.out.println("after conversion: " + javaList);

        return finalObj;
    }
    private Object compareKeysAndTypes(ArrayList<Tuple2<String,Object>> list) {
        Boolean flag = false;
        int counter = 0;
        for(int i = 0; i<keyNames.size();i++){
            Tuple2<String,Object> elem = list.get(i);
            if(elem._1.toLowerCase().equals(keyNames.get(i).toLowerCase())){
                if(elem._2.getClass().equals(keyTypes.get(i))){
                    flag = true;
                } else flag = false;
            } else flag = false;
            counter++;
        }
        if (flag){
            try {
                Constructor cons = clazz.getDeclaredConstructors()[0];
                cons.setAccessible(true);
                List<Object> newList = new ArrayList<>();
                for (int i = 0; i <counter;i++){
                    newList.add(list.get(i)._2);
                }
                Object obj = cons.newInstance(newList.toArray());
                //System.out.println("Object -> " + obj);
                return obj;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private java.util.ArrayList<Tuple2<String,Object>> convert(scala.collection.immutable.List<Tuple2<String,Object>> scalaList) {
        List<Tuple2<String,Object>> globalList = scala.collection.JavaConverters.seqAsJavaList(scalaList);
        ArrayList<Tuple2<String,Object>> scndList = new ArrayList<>();
        for (int i = 0; i < globalList.size(); i++) {
            Tuple2<String,Object> elem = globalList.get(i);
            if (elem._2 instanceof scala.collection.immutable.List) {
                scndList.add(new Tuple2<>(elem._1,convert((scala.collection.immutable.List<Tuple2<String,Object>>)elem._2)));
            } else {
                scndList.add(elem);
            }
        }
        return scndList;
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object res = runInterpreter(bsonByteEncoding);
                Object instance = InstantiateExtractedObject(res);
                System.out.println("instance -> " + instance);
                extractFunction.accept((T) instance);
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
