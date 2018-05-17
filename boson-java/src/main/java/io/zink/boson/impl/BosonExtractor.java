package io.zink.boson.impl;


import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonImpl.CustomException;
import io.zink.boson.bson.bsonImpl.Dictionary;
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
        Option<Class<T>> retainedClassOpt = retainConsumerType(_extractFunction);

        BosonImpl boson = new BosonImpl(Option.empty(), Option.empty(), Option.empty());
        if (retainedClassOpt.isDefined()) {
            this.clazz = retainedClassOpt.get();
            int counter = 0;
            for (int i = 0; i < Dictionary.TYPES_LIST().size();i++){
                if(!clazz.getSimpleName().equals(Dictionary.TYPES_LIST().get(i))) {
                    counter++;
                }
            }
            if(counter < Dictionary.TYPES_LIST().size()) typeIsClass = false; else typeIsClass = true;
            if(typeIsClass){
                constructor = clazz.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                fields = clazz.getDeclaredFields();
                keyNames = new LinkedList<>();
                keyTypes = new LinkedList<>();
                for (Field field: fields) {
                    keyNames.add(field.getName());
                    keyTypes.add(field.getType());
                }
            }
            Typeable<T> typeable = Typeable$.MODULE$.simpleTypeable(retainedClassOpt.get());
            TypeCase<T> typeCase = TypeCase$.MODULE$.apply(typeable);
            interpreter = new Interpreter<>(boson, expression, Option.empty(), Option.apply(anon), Option.apply(typeCase));
        } else {
            typeIsClass = false;
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

    private Object runInterpreter(byte[] bsonEncoded) {
        return interpreter.run(Left$.MODULE$.apply(bsonEncoded));
    }

    //triggers the process of construction
    private Object InstantiateExtractedObject(Object extResult) {
        Object finalObj = null;
        Seq<scala.collection.immutable.List<Tuple2<String,Object>>> _SeqOfTuplesList = (Seq<scala.collection.immutable.List<Tuple2<String,Object>>>) extResult;
        List<scala.collection.immutable.List<Tuple2<String,Object>>> seqOfTuplesList = scala.collection.JavaConverters.seqAsJavaList(_SeqOfTuplesList);
        LinkedList<ArrayList<Tuple2<String,Object>>> javaList = new LinkedList<>();
        for (int i = 0; i<seqOfTuplesList.size();i++){ // each iteration represents one extracted object
            javaList.add(convert(seqOfTuplesList.get(i)));
            Boolean allKeysAndTypesMatch = compareKeysAndTypes(javaList.get(i),keyNames,keyTypes);
            finalObj = triggerConstruction(allKeysAndTypesMatch, javaList.get(i),clazz, keyTypes);
        }
        return finalObj;
    }

    //validates keys and value types of given class with extracted object
    private Boolean compareKeysAndTypes(ArrayList<Tuple2<String,Object>> list, List<String> _keyNames, List<Class<?>> _keyTypes) {
        Boolean flag = false;
        for(int i = 0; i<_keyNames.size();i++){
            Tuple2<String,Object> elem = list.get(i);
            if(elem._1.toLowerCase().equals(_keyNames.get(i).toLowerCase())){
                if(elem._2.getClass().equals(_keyTypes.get(i)) || (_keyTypes.get(i).getSimpleName().equals("int") && elem._2.getClass().getSimpleName().equals("Integer"))){
                    flag = true;
                } else if(elem._2.getClass().equals(ArrayList.class)){
                    List<String> kNames = new LinkedList<>();
                    List<Class<?>> kTypes = new LinkedList<>();
                    Constructor cons = _keyTypes.get(i).getDeclaredConstructors()[0];
                    cons.setAccessible(true);
                    Field[] _fields = _keyTypes.get(i).getDeclaredFields();
                    for (Field field: _fields) {
                        kNames.add(field.getName());
                        kTypes.add(field.getType());
                    }
                    flag = compareKeysAndTypes((ArrayList<Tuple2<String,Object>>)elem._2, kNames,kTypes);
                } else flag = false;
            } else flag = false;
        }
        return flag;
    }

    // Verifies if all keys and types are valid and triggers construction
    private Object triggerConstruction(Boolean allKeysAndTypesMatch, ArrayList<Tuple2<String,Object>> list, Class<?> outterClass,List<Class<?>> _keyTypes) {
        if (allKeysAndTypesMatch){
                Object obj = instantiateClasses(list,outterClass,_keyTypes);
                return obj;
        }
        return null;
    }

    // verifies the existence of nested classes and starts to instantiate them from the inside to the outside
    private Object instantiateClasses(ArrayList<Tuple2<String,Object>> list, Class<?> typeClass, List<Class<?>> _keyTypes){
        Object instance = null;
        Object innerInstance;
        List<Class<?>> kTypes = new LinkedList<>();
        ArrayList<Object> values = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)._2 instanceof ArrayList) {
                Field[] _fields = _keyTypes.get(i).getDeclaredFields();
                for (Field field: _fields) {
                    kTypes.add(field.getType());
                }
                innerInstance = instantiateClasses((ArrayList<Tuple2<String,Object>>)list.get(i)._2,_keyTypes.get(i),kTypes);
                values.add(innerInstance);
            }else {
                values.add(list.get(i)._2);
            }
        }
        Constructor cons = typeClass.getDeclaredConstructors()[0];
        cons.setAccessible(true);
        try {
            instance = cons.newInstance(values.toArray());
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

    //converts scala.Lists to java.ArrayLists
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
                if(typeIsClass){
                    Object instance = InstantiateExtractedObject(res);
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
