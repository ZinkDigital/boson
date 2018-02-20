/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 *
 * Derived from original file Json.java from Vert.x
 */

package bsonLib;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.undercouch.bson4jackson.BsonFactory;
import bsonLib.BsonObject;
import bsonLib.BsonArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

public class Bson {

    public static ObjectMapper mapper = new ObjectMapper(new BsonFactory());


    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(BsonObject.class, new BsonObjectSerializer());
        module.addSerializer(BsonArray.class, new BsonArraySerializer());
        mapper.registerModule(module);
    }

    public static void encode(Object obj, OutputStream outputStream) {
        try {
            mapper.writeValue(outputStream, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T decodeValue(InputStream inputStream, Class<T> clazz) {
        try {
            return mapper.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static Object checkAndCopy(Object val, boolean copy) {
        if (val == null) {
            // OK
        } else if (val instanceof Number && !(val instanceof BigDecimal)) {
            // OK
        } else if (val instanceof Boolean) {
            // OK
        } else if (val instanceof String) {
            // OK
        } else if (val instanceof Character) {
            // OK
        } else if (val instanceof CharSequence) {
            val = val.toString();
        } else if (val instanceof BsonObject) {
            if (copy) {
                val = ((BsonObject)val).copy();
            }
        } else if (val instanceof BsonArray) {
            if (copy) {
                val = ((BsonArray)val).copy();
            }
        } else if (val instanceof Map) {
            if (copy) {
                val = (new BsonObject((Map)val)).copy();
            } else {
                val = new BsonObject((Map)val);
            }
        } else if (val instanceof List) {
            if (copy) {
                val = (new BsonArray((List)val)).copy();
            } else {
                val = new BsonArray((List)val);
            }
        } else if (val instanceof byte[]) {
            val = Base64.getEncoder().encodeToString((byte[])val);
        } else if (val instanceof Instant) {
            val = ISO_INSTANT.format((Instant)val);
        } else {
            throw new IllegalStateException("Illegal type in BsonObject: " + val.getClass());
        }
        return val;
    }

    static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private static class BsonObjectSerializer extends JsonSerializer<BsonObject> {
        @Override
        public void serialize(BsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getMap());
        }
    }

    private static class BsonArraySerializer extends JsonSerializer<BsonArray> {
        @Override
        public void serialize(BsonArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getList());
        }
    }
}
