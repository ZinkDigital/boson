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
 *
 * Derived from original file JsonObject.java from Vert.x
 */

package io.zink.boson.bson.bsonImpl.bsonLib;


import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * A representation of a <a href="http://json.org/">JSON</a> object in Java.
 * <p>
 * Unlike some other languages Java does not have a native understanding of JSON. To enable JSON to be used easily
 * in Vert.x code we use this class to encapsulate the notion of a JSON object.
 * <p>
 * The implementation adheres to the <a href="http://rfc-editor.org/rfc/rfc7493.txt">RFC-7493</a> to support Temporal
 * data types as well as binary data.
 * <p>
 * Please see the documentation for more information.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

public class BsonObject implements Iterable<Map.Entry<String, Object>> {

    private Map<String, Object> map;

    /**
     * Create an instance from a Buffer
     *
     * @param buffer the buffer containing the BSON
     */
    public BsonObject(Buffer buffer) {
        fromBson(new ByteArrayInputStream(buffer.getBytes()));
        fromBson(new ByteArrayInputStream(buffer.getBytes()));
    }

    /**
     * Create a new, empty instance
     */
    public BsonObject() {
        map = new LinkedHashMap<>();
    }

    /**
     * Create an instance from a Map. The Map is not copied.
     *
     * @param map the map to create the instance from.
     */
    public BsonObject(Map map) {
        this.map = map;
    }

    /**
     * Create an instance from a JsonObject
     *
     * @param jsonObject the JsonObject to create the BsonObject from
     */
    public BsonObject(JsonObject jsonObject) {
        this.map = jsonObject.getMap();
    }

    /**
     * Get the string value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a String
     */
    public String getString(String key) {
        Objects.requireNonNull(key);
        CharSequence cs = (CharSequence)map.get(key);
        return cs == null ? null : cs.toString();
    }

    /**
     * Get the Integer value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not an Integer
     */
    public Integer getInteger(String key) {
        Objects.requireNonNull(key);
        Number number = (Number)map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Integer) {
            return (Integer)number;  // Avoids unnecessary unbox/box
        } else {
            return number.intValue();
        }
    }

    /**
     * Get the Long value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a Long
     */
    public Long getLong(String key) {
        Objects.requireNonNull(key);
        Number number = (Number)map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Long) {
            return (Long)number;  // Avoids unnecessary unbox/box
        } else {
            return number.longValue();
        }
    }

    /**
     * Get the Double value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a Double
     */
    public Double getDouble(String key) {
        Objects.requireNonNull(key);
        Number number = (Number)map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Double) {
            return (Double)number;  // Avoids unnecessary unbox/box
        } else {
            return number.doubleValue();
        }
    }

    /**
     * Get the Float value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a Float
     */
    public Float getFloat(String key) {
        Objects.requireNonNull(key);
        Number number = (Number)map.get(key);
        if (number == null) {
            return null;
        } else if (number instanceof Float) {
            return (Float)number;  // Avoids unnecessary unbox/box
        } else {
            return number.floatValue();
        }
    }

    /**
     * Get the Boolean value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a Boolean
     */
    public Boolean getBoolean(String key) {
        Objects.requireNonNull(key);
        return (Boolean)map.get(key);
    }

    /**
     * Get the BsonObject value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a BsonObject
     */
    public BsonObject getBsonObject(String key) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        if (val instanceof Map) {
            val = new BsonObject((Map)val);
        }
        return (BsonObject)val;
    }

    /**
     * Get the BsonArray value with the specified key
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException if the value is not a BsonArray
     */
    public BsonArray getBsonArray(String key) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        if (val instanceof List) {
            val = new BsonArray((List)val);
        }
        return (BsonArray)val;
    }

    /**
     * Get the binary value with the specified key.
     * <p>
     * JSON itself has no notion of a binary, this extension complies to the RFC-7493, so this method assumes there is a
     * String value with the key and it contains a Base64 encoded binary, which it decodes if found and returns.
     * <p>
     * This method should be used in conjunction with {@link #put(String, byte[])}
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException       if the value is not a String
     * @throws IllegalArgumentException if the String value is not a legal Base64 encoded value
     */
    public byte[] getBinary(String key) {
        Objects.requireNonNull(key);
        String encoded = (String)map.get(key);
        return encoded == null ? null : Base64.getDecoder().decode(encoded);
    }

    /**
     * Get the instant value with the specified key.
     * <p>
     * JSON itself has no notion of a date, this extension complies to the RFC-7493, so this method assumes there is a
     * String value with the key and it contains a ISODATE encoded date, which it decodes if found and returns.
     * <p>
     * This method should be used in conjunction with {@link #put(String, Instant)}
     *
     * @param key the key to return the value for
     * @return the value or null if no value for that key
     * @throws ClassCastException       if the value is not a String
     * @throws IllegalArgumentException if the String value is not a legal Base64 encoded value
     */
    public Instant getInstant(String key) {
        Objects.requireNonNull(key);
        String encoded = (String)map.get(key);
        return encoded == null ? null : Instant.from(ISO_INSTANT.parse(encoded));
    }

    /**
     * Get the value with the specified key, as an Object
     *
     * @param key the key to lookup
     * @return the value
     */
    public Object getValue(String key) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        if (val instanceof Map) {
            val = new BsonObject((Map)val);
        } else if (val instanceof List) {
            val = new BsonArray((List)val);
        }
        return val;
    }

    /**
     * Like {@link #getString(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public String getString(String key, String def) {
        Objects.requireNonNull(key);
        CharSequence cs = (CharSequence)map.get(key);
        return cs != null || map.containsKey(key) ? cs == null ? null : cs.toString() : def;
    }

    /**
     * Like {@link #getInteger(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Integer getInteger(String key, Integer def) {
        Objects.requireNonNull(key);
        Number val = (Number)map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Integer) {
            return (Integer)val;  // Avoids unnecessary unbox/box
        } else {
            return val.intValue();
        }
    }

    /**
     * Like {@link #getLong(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Long getLong(String key, Long def) {
        Objects.requireNonNull(key);
        Number val = (Number)map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Long) {
            return (Long)val;  // Avoids unnecessary unbox/box
        } else {
            return val.longValue();
        }
    }

    /**
     * Like {@link #getDouble(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Double getDouble(String key, Double def) {
        Objects.requireNonNull(key);
        Number val = (Number)map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Double) {
            return (Double)val;  // Avoids unnecessary unbox/box
        } else {
            return val.doubleValue();
        }
    }

    /**
     * Like {@link #getFloat(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Float getFloat(String key, Float def) {
        Objects.requireNonNull(key);
        Number val = (Number)map.get(key);
        if (val == null) {
            if (map.containsKey(key)) {
                return null;
            } else {
                return def;
            }
        } else if (val instanceof Float) {
            return (Float)val;  // Avoids unnecessary unbox/box
        } else {
            return val.floatValue();
        }
    }

    /**
     * Like {@link #getBoolean(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Boolean getBoolean(String key, Boolean def) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        return val != null || map.containsKey(key) ? (Boolean)val : def;
    }

    /**
     * Like {@link #getBsonObject(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public BsonObject getBsonObject(String key, BsonObject def) {
        BsonObject val = getBsonObject(key);
        return val != null || map.containsKey(key) ? val : def;
    }

    /**
     * Like {@link #getBsonArray(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public BsonArray getBsonArray(String key, BsonArray def) {
        BsonArray val = getBsonArray(key);
        return val != null || map.containsKey(key) ? val : def;
    }

    /**
     * Like {@link #getBinary(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public byte[] getBinary(String key, byte[] def) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        return val != null || map.containsKey(key) ? (val == null ? null : Base64.getDecoder().decode((String)val)) : def;
    }

    /**
     * Like {@link #getInstant(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Instant getInstant(String key, Instant def) {
        Objects.requireNonNull(key);
        Object val = map.get(key);
        return val != null || map.containsKey(key) ?
                (val == null ? null : Instant.from(ISO_INSTANT.parse((String)val))) : def;
    }

    /**
     * Like {@link #getValue(String)} but specifying a default value to return if there is no entry.
     *
     * @param key the key to lookup
     * @param def the default value to use if the entry is not present
     * @return the value or {@code def} if no entry present
     */
    public Object getValue(String key, Object def) {
        Objects.requireNonNull(key);
        Object val = getValue(key);
        return val != null || map.containsKey(key) ? val : def;
    }

    /**
     * Does the JSON object contain the specified key?
     *
     * @param key the key
     * @return true if it contains the key, false if not.
     */
    public boolean containsKey(String key) {
        Objects.requireNonNull(key);
        return map.containsKey(key);
    }

    /**
     * Return the set of field names in the JSON objects
     *
     * @return the set of field names
     */
    public Set<String> fieldNames() {
        return map.keySet();
    }

    /**
     * Put an Enum into the JSON object with the specified key.
     * <p>
     * JSON has no concept of encoding Enums, so the Enum will be converted to a String using the {@link Enum#name}
     * method and the value put as a String.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Enum value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : value.name());
        return this;
    }

    /**
     * Put an CharSequence into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, CharSequence value) {

        Objects.requireNonNull(key);
        map.put(key, value == null ? null : value.toString());
        return this;
    }

    /**
     * Put a String into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, String value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put an Integer into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Integer value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a Long into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Long value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a Double into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Double value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a Float into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Float value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a Boolean into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Boolean value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a null value into the JSON object with the specified key.
     *
     * @param key the key
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject putNull(String key) {
        Objects.requireNonNull(key);
        map.put(key, null);
        return this;
    }

    /**
     * Put another JSON object into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, BsonObject value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a JSON array into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, BsonArray value) {
        Objects.requireNonNull(key);
        map.put(key, value);
        return this;
    }

    /**
     * Put a byte[] into the JSON object with the specified key.
     * <p>
     * JSON extension RFC7493, binary will first be Base64 encoded before being put as a String.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, byte[] value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : Base64.getEncoder().encodeToString(value));
        return this;
    }

    /**
     * Put a Instant into the JSON object with the specified key.
     * <p>
     * JSON extension RFC7493, instant will first be encoded to ISODATE String.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Instant value) {
        Objects.requireNonNull(key);
        map.put(key, value == null ? null : ISO_INSTANT.format(value));
        return this;
    }

    /**
     * Put an Object into the JSON object with the specified key.
     *
     * @param key   the key
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject put(String key, Object value) {
        Objects.requireNonNull(key);
        value = Bson.checkAndCopy(value, false);
        map.put(key, value);
        return this;
    }

    /**
     * Remove an entry from this object.
     *
     * @param key the key
     * @return the value that was removed, or null if none
     */
    public Object remove(String key) {
        return map.remove(key);
    }

    /**
     * Merge in another JSON object.
     * <p>
     * This is the equivalent of putting all the entries of the other JSON object into this object.
     *
     * @param other the other JSON object
     * @return a reference to this, so the API can be used fluently
     */
    public BsonObject mergeIn(BsonObject other) {
        map.putAll(other.map);
        return this;
    }

    /**
     * Encode this JSON object as a string.
     *
     * @return the string encoding.
     */
    public void encode(OutputStream outputStream) {
        Bson.encode(map, outputStream);
    }

    public Buffer encode() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Bson.encode(map, os);
            os.flush();
            return Buffer.buffer(os.toByteArray());
        } catch (IOException e) {
            throw new VertxException(e);
        }
    }

    /**
     * Encode the BSON object as a Byte Array
     *
     * @return the Byte Array
     */
    public byte[] encodeToBarray(){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Bson.encode(map, os);
            os.flush();
            return os.toByteArray();
        } catch (IOException e) {
            throw new VertxException(e);
        }
    }


    /**
     * Copy the JSON object
     *
     * @return a copy of the object
     */
    public BsonObject copy() {
        Map<String, Object> copiedMap = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            val = Bson.checkAndCopy(val, true);
            copiedMap.put(entry.getKey(), val);
        }
        return new BsonObject(copiedMap);
    }

    /**
     * Get the underlying Map.
     *
     * @return the underlying Map.
     */
    public Map<String, Object> getMap() {
        return map;
    }

    /**
     * Get a stream of the entries in the JSON object.
     *
     * @return a stream of the entries.
     */
    public Stream<Map.Entry<String, Object>> stream() {
        return Bson.asStream(iterator());
    }

    /**
     * Get an Iterator of the entries in the JSON object.
     *
     * @return an Iterator of the entries
     */
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return new Iter(map.entrySet().iterator());
    }

    /**
     * Get the number of entries in the JSON object
     *
     * @return the number of entries
     */
    public int size() {
        return map.size();
    }

    /**
     * Remove all the entries in this JSON object
     */
    public BsonObject clear() {
        map.clear();
        return this;
    }

    /**
     * Encode this to a String
     *
     * @return the string form
     */
    public String encodeToString() {
        return toJsonObject().encode();
    }

    /**
     * Convert this to a JsonObject
     *
     * @return the equivalent JsonObject
     */
    public JsonObject toJsonObject() {
        Map<String, Object> m = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            Object o = entry.getValue();
            if (o instanceof BsonObject) {
                BsonObject bo = (BsonObject)o;
                m.put(entry.getKey(), bo.toJsonObject());
            } else if (o instanceof BsonArray) {
                BsonArray ba = (BsonArray)o;
                m.put(entry.getKey(), ba.toJsonArray());
            } else {
                m.put(entry.getKey(), o);
            }
        }
        return new JsonObject(m);
    }

    public JsonObject asJson(){
        return this.toJsonObject();
    }

    public BsonObject asBson(JsonObject jsonObj) {
        return new BsonObject(jsonObj);
    }

    @Override
    public String toString() {
        return encodeToString();
    }

    /**
     * Is this object entry?
     *
     * @return true if it has zero entries, false if not.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return objectEquals(map, o);
    }

    static boolean objectEquals(Map<?, ?> m1, Object o2) {
        Map<?, ?> m2;
        if (o2 instanceof BsonObject) {
            m2 = ((BsonObject)o2).map;
        } else if (o2 instanceof Map<?, ?>) {
            m2 = (Map<?, ?>)o2;
        } else {
            return false;
        }
        if (m1.size() != m2.size())
            return false;
        for (Map.Entry<?, ?> entry : m1.entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                if (m2.get(entry.getKey()) != null) {
                    return false;
                }
            } else {
                if (!equals(entry.getValue(), m2.get(entry.getKey()))) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 instanceof BsonObject) {
            return objectEquals(((BsonObject)o1).map, o2);
        }
        if (o1 instanceof Map<?, ?>) {
            return objectEquals((Map<?, ?>)o1, o2);
        }
        if (o1 instanceof BsonArray) {
            return BsonArray.arrayEquals(((BsonArray)o1).getList(), o2);
        }
        if (o1 instanceof List<?>) {
            return BsonArray.arrayEquals((List<?>)o1, o2);
        }
        if (o1 instanceof Number && o2 instanceof Number && o1.getClass() != o2.getClass()) {
            Number n1 = (Number)o1;
            Number n2 = (Number)o2;
            if (o1 instanceof Float || o1 instanceof Double || o2 instanceof Float || o2 instanceof Double) {
                return n1.doubleValue() == n2.doubleValue();
            } else {
                return n1.longValue() == n2.longValue();
            }
        }
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    private void fromBson(InputStream inputStream) {
        map = Bson.decodeValue(inputStream, Map.class);
    }

    private class Iter implements Iterator<Map.Entry<String, Object>> {

        final Iterator<Map.Entry<String, Object>> mapIter;

        Iter(Iterator<Map.Entry<String, Object>> mapIter) {
            this.mapIter = mapIter;
        }

        @Override
        public boolean hasNext() {
            return mapIter.hasNext();
        }

        @Override
        public Map.Entry<String, Object> next() {
            Map.Entry<String, Object> entry = mapIter.next();
            if (entry.getValue() instanceof Map) {
                return new Entry(entry.getKey(), new BsonObject((Map)entry.getValue()));
            } else if (entry.getValue() instanceof List) {
                return new Entry(entry.getKey(), new BsonArray((List)entry.getValue()));
            }
            return entry;
        }

        @Override
        public void remove() {
            mapIter.remove();
        }
    }

    private static final class Entry implements Map.Entry<String, Object> {
        final String key;
        final Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }
}
