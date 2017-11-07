package io.boson.bson;

/**
 * Created by tim on 04/11/16.
 */
@Deprecated
public class BsonPath {

    public static BsonObject set(BsonObject doc, Object value, Object... path) {
        BsonObject root = getRoot(doc, path);
        String lastElem = (String)path[path.length - 1];
        root.put(lastElem, value);
        return doc;
    }

    public static BsonObject add(BsonObject doc, int value, Object... path) {
        BsonObject root = getRoot(doc, path);
        String lastElem = (String)path[path.length - 1];
        Object prevVal = root.getValue(lastElem);
        Object newVal;
        if (prevVal != null) {
            if (prevVal instanceof Integer) {
                newVal = ((Integer)prevVal) + value;
            } else if (prevVal instanceof Long) {
                newVal = ((Long)prevVal) + value;
            } else if (prevVal instanceof Short) {
                newVal = ((Short)prevVal) + value;
            } else if (prevVal instanceof Byte) {
                newVal = ((Byte)prevVal) + value;
            } else {
                throw new IllegalArgumentException("Cannot increment " + prevVal);
            }
        } else {
            newVal = value;
        }
        root.put(lastElem, newVal);
        return doc;
    }

    public static BsonObject inc(BsonObject doc, Object... path) {
        return add(doc, 1, path);
    }

    public static BsonObject inc(BsonObject doc, String path) {
        return null;
    }

    // TODO support BsonArray
    private static BsonObject getRoot(BsonObject doc, Object... path) {
        BsonObject rootObj = doc;
        for (int i = 0; i < path.length - 1; i++) {
            Object o = path[i];
            if (o instanceof String) {
                String key = (String)o;
                BsonObject child = rootObj.getBsonObject(key);
                if (child == null) {
                    child = new BsonObject();
                    rootObj.put(key, child);
                }
                rootObj = child;
            } else {
                throw new IllegalArgumentException("Invalid path element " + o);
            }
        }
        return rootObj;
    }

}
