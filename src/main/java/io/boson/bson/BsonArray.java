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
 * Derived from original file JsonArray.java from Vert.x
 */

package io.boson.bson;

import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * A representation of a <a href="http://json.org/">JSON</a> array in Java.
 * <p>
 * Unlike some other languages Java does not have a native understanding of JSON. To enable JSON to be used easily
 * in Vert.x code we use this class to encapsulate the notion of a JSON array.
 * <p>
 * The implementation adheres to the <a href="http://rfc-editor.org/rfc/rfc7493.txt">RFC-7493</a> to support Temporal
 * data types as well as binary data.
 * <p>
 * Please see the documentation for more information.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BsonArray implements Iterable<Object> {

    private List<Object> list;

    /**
     * Create an instance from a buffer
     *
     * @param bson the buffer containing the BSON
     */
    public BsonArray(Buffer bson) {
        fromBson(new ByteArrayInputStream(bson.getBytes()));
    }

    /**
     * Create an empty instance
     */
    public BsonArray() {
        list = new ArrayList<>();
    }

    /**
     * Create an instance from a List. The List is not copied.
     *
     * @param list
     */
    public BsonArray(List list) {
        this.list = list;
    }

    /**
     * Create an instance from a JsonArray
     *
     * @param jsonArray the JsonArray to create the BsonArray from
     */
    public BsonArray(JsonArray jsonArray) {
        this.list = jsonArray.getList();
    }

    /**
     * Get the String at position {@code pos} in the array,
     *
     * @param pos the position in the array
     * @return the String, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to String
     */
    public String getString(int pos) {
        CharSequence cs = (CharSequence)list.get(pos);
        return cs == null ? null : cs.toString();
    }

    /**
     * Get the Integer at position {@code pos} in the array,
     *
     * @param pos the position in the array
     * @return the Integer, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to Integer
     */
    public Integer getInteger(int pos) {
        return getNumber(pos, Integer.class, Number::intValue);
    }

    /**
     * Get the Long at position {@code pos} in the array,
     *
     * @param pos the position in the array
     * @return the Long, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to Long
     */
    public Long getLong(int pos) {
        return getNumber(pos, Long.class, Number::longValue);
    }

    /**
     * Get the Double at position {@code pos} in the array,
     *
     * @param pos the position in the array
     * @return the Double, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to Double
     */
    public Double getDouble(int pos) {
        return getNumber(pos, Double.class, Number::doubleValue);
    }

    /**
     * Get the Float at position {@code pos} in the array,
     *
     * @param pos the position in the array
     * @return the Float, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to Float
     */
    public Float getFloat(int pos) {
        return getNumber(pos, Float.class, Number::floatValue);
    }

    private <T> T getNumber(int pos, Class<T> clazz, Function<Number, T> conversion) {
        Object element = list.get(pos);
        if (element == null) {
            return null;
        }
        if (clazz.isInstance(element)) {
            return clazz.cast(element); // Avoids unnecessary object creation
        }
        return conversion.apply((Number)element);
    }

    /**
     * Get the Boolean at position {@code pos} in the array,
     *
     * @param pos the position in the array
     * @return the Boolean, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to Integer
     */
    public Boolean getBoolean(int pos) {
        return (Boolean)list.get(pos);
    }

    /**
     * Get the BsonObject at position {@code pos} in the array.
     *
     * @param pos the position in the array
     * @return the Integer, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to BsonObject
     */
    public BsonObject getBsonObject(int pos) {
        Object val = list.get(pos);
        if (val instanceof Map) {
            val = new BsonObject((Map)val);
        }
        return (BsonObject)val;
    }

    /**
     * Get the BsonArray at position {@code pos} in the array.
     *
     * @param pos the position in the array
     * @return the Integer, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to BsonArray
     */
    public BsonArray getBsonArray(int pos) {
        Object val = list.get(pos);
        if (val instanceof List) {
            val = new BsonArray((List)val);
        }
        return (BsonArray)val;
    }

    /**
     * Get the byte[] at position {@code pos} in the array.
     * <p>
     * JSON itself has no notion of a binary, so this method assumes there is a String value and
     * it contains a Base64 encoded binary, which it decodes if found and returns.
     * <p>
     * This method should be used in conjunction with {@link #add(byte[])}
     *
     * @param pos the position in the array
     * @return the byte[], or null if a null value present
     * @throws ClassCastException if the value cannot be converted to String
     */
    public byte[] getBinary(int pos) {
        String val = (String)list.get(pos);
        if (val == null) {
            return null;
        } else {
            return Base64.getDecoder().decode(val);
        }
    }

    /**
     * Get the Instant at position {@code pos} in the array.
     * <p>
     * JSON itself has no notion of a temporal types, so this method assumes there is a String value and
     * it contains a ISOString encoded date, which it decodes if found and returns.
     * <p>
     * This method should be used in conjunction with {@link #add(Instant)}
     *
     * @param pos the position in the array
     * @return the Instant, or null if a null value present
     * @throws ClassCastException if the value cannot be converted to String
     */
    public Instant getInstant(int pos) {
        String val = (String)list.get(pos);
        if (val == null) {
            return null;
        } else {
            return Instant.from(ISO_INSTANT.parse(val));
        }
    }

    /**
     * Get the Object value at position {@code pos} in the array.
     *
     * @param pos the position in the array
     * @return the Integer, or null if a null value present
     */
    public Object getValue(int pos) {
        Object val = list.get(pos);
        if (val instanceof Map) {
            val = new BsonObject((Map)val);
        } else if (val instanceof List) {
            val = new BsonArray((List)val);
        }
        return val;
    }

    /**
     * Is there a null value at position pos?
     *
     * @param pos the position in the array
     * @return true if null value present, false otherwise
     */
    public boolean hasNull(int pos) {
        return list.get(pos) == null;
    }

    /**
     * Add an enum to the JSON array.
     * <p>
     * JSON has no concept of encoding Enums, so the Enum will be converted to a String using the {@link Enum#name}
     * method and the value added as a String.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Enum value) {
        Objects.requireNonNull(value);
        list.add(value.name());
        return this;
    }

    /**
     * Add a CharSequence to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(CharSequence value) {
        Objects.requireNonNull(value);
        list.add(value.toString());
        return this;
    }

    /**
     * Add a String to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(String value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add an Integer to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Integer value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add a Long to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Long value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add a Double to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Double value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add a Float to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Float value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add a Boolean to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Boolean value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add a null value to the JSON array.
     *
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray addNull() {
        list.add(null);
        return this;
    }

    /**
     * Add a JSON object to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(BsonObject value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add another JSON array to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(BsonArray value) {
        Objects.requireNonNull(value);
        list.add(value);
        return this;
    }

    /**
     * Add a binary value to the JSON array.
     * <p>
     * JSON has no notion of binary so the binary will be base64 encoded to a String, and the String added.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(byte[] value) {
        Objects.requireNonNull(value);
        list.add(Base64.getEncoder().encodeToString(value));
        return this;
    }

    /**
     * Add a Instant value to the JSON array.
     * <p>
     * JSON has no notion of Temporal data so the Instant will be ISOString encoded, and the String added.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Instant value) {
        Objects.requireNonNull(value);
        list.add(ISO_INSTANT.format(value));
        return this;
    }

    /**
     * Add an Object to the JSON array.
     *
     * @param value the value
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray add(Object value) {
        Objects.requireNonNull(value);
        value = Bson.checkAndCopy(value, false);
        list.add(value);
        return this;
    }

    /**
     * Appends all of the elements in the specified array to the end of this JSON array.
     *
     * @param array the array
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray addAll(BsonArray array) {
        Objects.requireNonNull(array);
        list.addAll(array.list);
        return this;
    }

    /**
     * Does the JSON array contain the specified value? This method will scan the entire array until it finds a value
     * or reaches the end.
     *
     * @param value the value
     * @return true if it contains the value, false if not
     */
    public boolean contains(Object value) {
        return list.contains(value);
    }

    /**
     * Remove the specified value from the JSON array. This method will scan the entire array until it finds a value
     * or reaches the end.
     *
     * @param value the value to remove
     * @return true if it removed it, false if not found
     */
    public boolean remove(Object value) {
        return list.remove(value);
    }

    /**
     * Remove the value at the specified position in the JSON array.
     *
     * @param pos the position to remove the value at
     * @return the removed value if removed, null otherwise. If the value is a Map, a {@link BsonObject} is built from
     * this Map and returned. It the value is a List, a {@link BsonArray} is built form this List and returned.
     */
    public Object remove(int pos) {
        Object removed = list.remove(pos);
        if (removed instanceof Map) {
            return new BsonObject((Map)removed);
        } else if (removed instanceof ArrayList) {
            return new BsonArray((List)removed);
        }
        return removed;
    }

    /**
     * Get the number of values in this JSON array
     *
     * @return the number of items
     */
    public int size() {
        return list.size();
    }

    /**
     * Are there zero items in this JSON array?
     *
     * @return true if zero, false otherwise
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Get the underlying List
     *
     * @return the underlying List
     */
    public List getList() {
        return list;
    }

    /**
     * Remove all entries from the JSON array
     *
     * @return a reference to this, so the API can be used fluently
     */
    public BsonArray clear() {
        list.clear();
        return this;
    }

    /**
     * Get an Iterator over the values in the JSON array
     *
     * @return an iterator
     */
    @Override
    public Iterator<Object> iterator() {
        return new Iter(list.iterator());
    }

    /**
     * Encode the BSON object as a buffer
     *
     * @return the buffer
     */
    public Buffer encode() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Bson.encode(list, os);
            os.flush();
            return Buffer.buffer(os.toByteArray());
        } catch (IOException e) {
            throw new VertxException(e);
        }
    }

    /**
     * Encode this BSON object in an output stream
     *
     * @return the string encoding.
     */
    public void encode(OutputStream outputStream) {
        Bson.encode(list, outputStream);
    }

    /**
     * Encode this to a String
     *
     * @return the string form
     */
    public String encodeToString() {
        return toJsonArray().encode();
    }

    /**
     * Convert this into a JsonArray
     *
     * @return the equivalent JsonArray
     */
    public JsonArray toJsonArray() {
        List<Object> l = new ArrayList<>();
        for (Object o: list) {
            if (o instanceof BsonObject) {
                BsonObject bo = (BsonObject)o;
                l.add(bo.toJsonObject());
            } else if (o instanceof BsonArray) {
                BsonArray ba = (BsonArray)o;
                l.add(ba.toJsonArray());
            } else {
                l.add(o);
            }
        }
        return new JsonArray(l);
    }


    @Override
    public String toString() {
        return encodeToString();
    }


    /**
     * Make a copy of the JSON array
     *
     * @return a copy
     */
    public BsonArray copy() {
        List<Object> copiedList = new ArrayList<>(list.size());
        for (Object val : list) {
            val = Bson.checkAndCopy(val, true);
            copiedList.add(val);
        }
        return new BsonArray(copiedList);
    }

    /**
     * Get a Stream over the entries in the JSON array
     *
     * @return a Stream
     */
    public Stream<Object> stream() {
        return Bson.asStream(iterator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return arrayEquals(list, o);
    }

    static boolean arrayEquals(List<?> l1, Object o2) {
        List<?> l2;
        if (o2 instanceof BsonArray) {
            l2 = ((BsonArray)o2).list;
        } else if (o2 instanceof List<?>) {
            l2 = (List<?>)o2;
        } else {
            return false;
        }
        if (l1.size() != l2.size())
            return false;
        Iterator<?> iter = l2.iterator();
        for (Object entry : l1) {
            Object other = iter.next();
            if (entry == null) {
                if (other != null) {
                    return false;
                }
            } else if (!BsonObject.equals(entry, other)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    private void fromBson(InputStream bson) {
        list = Bson.decodeValue(bson, List.class);
    }

    private class Iter implements Iterator<Object> {

        final Iterator<Object> listIter;

        Iter(Iterator<Object> listIter) {
            this.listIter = listIter;
        }

        @Override
        public boolean hasNext() {
            return listIter.hasNext();
        }

        @Override
        public Object next() {
            Object val = listIter.next();
            if (val instanceof Map) {
                val = new BsonObject((Map)val);
            } else if (val instanceof List) {
                val = new BsonArray((List)val);
            }
            return val;
        }

        @Override
        public void remove() {
            listIter.remove();
        }
    }


}
