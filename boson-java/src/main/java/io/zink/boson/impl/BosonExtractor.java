package io.zink.boson.impl;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonImpl.CustomException;
import io.zink.boson.bson.bsonImpl.Dictionary;
import io.zink.boson.bson.bsonPath.*;
import io.zink.boson.Boson;
import io.zink.boson.bson.bsonImpl.Dictionary.*;

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
    private Boolean typeIsClass;


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
        constructor.setAccessible(true);
        fields = clazz.getDeclaredFields();
        keyNames = new LinkedList<>();
        keyTypes = new LinkedList<>();
        for (Field field: fields) {
            keyNames.add(field.getName());
            //System.out.println("fieldName -> " + field.getName());
            keyTypes.add(field.getType());
            //System.out.println("fieldType -> " + field.getType());
        }
        int counter = 0;
        for (int i = 0; i < Dictionary.TYPES_LIST().size();i++){
            if(!clazz.getSimpleName().equals(Dictionary.TYPES_LIST().get(i))) {
                counter++;
            }
        }
        if(counter < Dictionary.TYPES_LIST().size()) typeIsClass = false; else typeIsClass = true;
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
        //System.out.println("tuples in java: " + seqOfTuplesList);
        LinkedList<ArrayList<Tuple2<String,Object>>> javaList = new LinkedList<>();
        for (int i = 0; i<seqOfTuplesList.size();i++){
            //System.out.println("pos: " + i + " content: " + seqOfTuplesList.get(i));
            javaList.add(convert(seqOfTuplesList.get(i)));
            Boolean allKeysAndTypesMatch = compareKeysAndTypes(javaList.get(i),keyNames,keyTypes); // now is returning boolean
            ArrayList<Object> values = getValues(javaList.get(i),javaList.get(i).size());
            finalObj = doSomething(allKeysAndTypesMatch, values, javaList.get(i),clazz, keyTypes);
        }
        System.out.println("after conversion: " + javaList);

        return finalObj;
    }

    private Boolean compareKeysAndTypes(ArrayList<Tuple2<String,Object>> list, List<String> _keyNames, List<Class<?>> _keyTypes) {
        //System.out.println("KeyNames: " + _keyNames);
        //System.out.println("KeyTypes: " + _keyTypes);
        Boolean flag = false;
        int counter = 0;
        for(int i = 0; i<_keyNames.size();i++){
            Tuple2<String,Object> elem = list.get(i);
            //System.out.println("elem._1: "+elem._1 + ", keyNames: " + _keyNames.get(i));
            if(elem._1.toLowerCase().equals(_keyNames.get(i).toLowerCase())){
                //System.out.println("elem._2.getclass: "+elem._2.getClass() + ", keyTypes: " + _keyTypes.get(i));
                if(elem._2.getClass().equals(_keyTypes.get(i)) || (_keyTypes.get(i).getSimpleName().equals("int") && elem._2.getClass().getSimpleName().equals("Integer"))){
                    //System.out.println("matched names and types in: " + elem);
                    flag = true;
                } else if(elem._2.getClass().equals(ArrayList.class)){
                    //System.out.println("elem is instance of ArrayList, going down one level with elem: " + elem);
                    List<String> kNames = new LinkedList<>();
                    List<Class<?>> kTypes = new LinkedList<>();
                    Constructor cons = _keyTypes.get(i).getDeclaredConstructors()[0];
                    cons.setAccessible(true);
                    Field[] _fields = _keyTypes.get(i).getDeclaredFields();
                    for (Field field: _fields) {
                        kNames.add(field.getName());
                        //System.out.println("fieldName -> " + field.getName());
                        kTypes.add(field.getType());
                        //System.out.println("fieldType -> " + field.getType());
                    }
                    flag = compareKeysAndTypes((ArrayList<Tuple2<String,Object>>)elem._2, kNames,kTypes);
                } else flag = false;
            } else flag = false;
            counter++;
        }
        //System.out.println("flag "+flag);
        return flag;
    }

    private Object doSomething(Boolean allKeysAndTypesMatch, ArrayList<Object> values, ArrayList<Tuple2<String,Object>> list, Class<?> outterClass,List<Class<?>> _keyTypes) {
        if (allKeysAndTypesMatch){
                Object obj = instantiateInnerClasses(list,outterClass,_keyTypes);
                return obj;
                //System.out.println("Object -> " + obj);
                //return obj;

        }
        return null;
    }

    private ArrayList<Object> getValues(ArrayList<Tuple2<String,Object>> list, int counter){
        //System.out.println("getValues, globalList -> " + list);
        ArrayList<Object> newList = new ArrayList<>();
        for (int i = 0; i <counter;i++){
            //System.out.println("Element is ArrayList? " + (list.get(i)._2 instanceof ArrayList) + ", elem: " + list.get(i)._2);
            if(list.get(i)._2 instanceof ArrayList){    // probably call compareKeysAndTypes to make it recursive
                newList.add(getValues((ArrayList<Tuple2<String,Object>>) list.get(i)._2,((ArrayList<Tuple2<String,Object>>) list.get(i)._2).size()));
            } else newList.add(list.get(i)._2);
        }
        return newList;
    }

    private Object instantiateInnerClasses(ArrayList<Tuple2<String,Object>> list, Class<?> typeClass, List<Class<?>> _keyTypes){
        Object instance = null;
        Object innerInstance = null;
        List<Class<?>> kTypes = new LinkedList<>();
        ArrayList<Object> values = new ArrayList<>();
        System.out.println("List -> " + list);
        System.out.println("outterClass -> " + typeClass);
        System.out.println("keyTypes -> " + _keyTypes);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)._2 instanceof ArrayList) {
                Field[] _fields = _keyTypes.get(i).getDeclaredFields();
                for (Field field: _fields) {
                    kTypes.add(field.getType());
                    //System.out.println("fieldType -> " + field.getType());
                }
                innerInstance = instantiateInnerClasses((ArrayList<Tuple2<String,Object>>)list.get(i)._2,_keyTypes.get(i),kTypes);
                System.out.println("InnerInstance: " + innerInstance);
                values.add(innerInstance);
            }else {
                System.out.println("Element isn't an arraylist: " + list.get(i)._2);
                values.add(list.get(i)._2);
            }
        }
        System.out.println("Going to build instance");
        Constructor cons = typeClass.getDeclaredConstructors()[0];
        cons.setAccessible(true);
        System.out.println("Constructor: " + cons);
        try {
            instance = cons.newInstance(values.toArray());
            System.out.println("instance -> " + instance);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println("Going out of instantiateInnerClasses");
        return instance;
    }

    private java.util.ArrayList<Tuple2<String,Object>> convert(scala.collection.immutable.List<Tuple2<String,Object>> scalaList) {
        List<Tuple2<String,Object>> globalList = scala.collection.JavaConverters.seqAsJavaList(scalaList);
        //System.out.println("GlobalList -> " + globalList);
        ArrayList<Tuple2<String,Object>> scndList = new ArrayList<>();
        for (int i = 0; i < globalList.size(); i++) {
            Tuple2<String,Object> elem = globalList.get(i);
            //System.out.println("Element: " + elem + ", In position: " + i);
            if (elem._2 instanceof scala.collection.immutable.List) {
                //System.out.println("Element is instance of List");
                scndList.add(new Tuple2<>(elem._1,convert((scala.collection.immutable.List<Tuple2<String,Object>>)elem._2)));
            } else {
                //System.out.println("Regular tuple");
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
                System.out.println("typeIsClass? -> " + typeIsClass);
                if(typeIsClass){
                    Object instance = InstantiateExtractedObject(res);
                    System.out.println("instance -> " + instance);
                    extractFunction.accept((T) instance);
                }
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
