package com.hawolt.rtmp.amf;

import com.hawolt.rtmp.amf.decoder.ObjectTerminateAMF0;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Helper object also representing an AMF TypedObject
 *
 * @author Hawolt
 */

public class TypedObject extends LinkedHashMap<String, Object> {
    private String type;

    public TypedObject() {
        this(null);
    }

    public TypedObject(String type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getInteger(String key) {
        Object o = get(key);
        if (o == null) return null;
        else return ((Double) get(key)).intValue();
    }

    public Long getLong(String key) {
        return containsKey(key) ? ((Double) get(key)).longValue() : null;
    }

    public String getString(String key) {
        return containsKey(key) ? get(key).toString() : null;
    }

    public TypedObject getTypedObject(String key) {
        return containsKey(key) ? (TypedObject) get(key) : null;
    }

    @Override
    public String toString() {
        return tidy(this).toString();
    }

    public static TypedObject createArrayCollection(Object[] arr) {
        TypedObject typedObject = new TypedObject("flex.messaging.io.ArrayCollection");
        typedObject.put("array", arr);
        return typedObject;
    }

    public static TypedObject fromJson(Object o) {
        JSONObject json = new JSONObject(o.toString());
        TypedObject typedObject = new TypedObject();
        Set<String> keySet = json.keySet();
        if (keySet.size() == 0) return typedObject;
        for (String key : keySet) {
            typedObject.put(key, convert(json.get(key)));
        }
        return typedObject;
    }

    private static Object convert(Object o) {
        if (o instanceof JSONObject) return fromJson(o.toString());
        if (o instanceof JSONArray) {
            JSONArray array = (JSONArray) o;
            Object[] objects = new Object[array.length()];
            for (int i = 0; i < array.length(); i++) {
                objects[i] = array.get(i);
            }
            return objects;
        }
        return o;
    }


    public static JSONObject tidy(TypedObject o) {
        JSONObject object = new JSONObject();
        for (String key : o.keySet()) {
            Object tmp = o.get(key);
            if (tmp == null) {
                object.put(key, JSONObject.NULL);
            } else if (tmp instanceof Object[]) {
                Object[] arr = (Object[]) tmp;
                object.put(key, tidy(arr));
            } else {
                if (tmp instanceof TypedObject) {
                    object.put(key, tidy((TypedObject) tmp));
                } else if (tmp instanceof Double) {
                    object.put(key, ((Double) o.get(key)).longValue());
                } else {
                    if (tmp instanceof ObjectTerminateAMF0) continue;
                    object.put(key, tidy(tmp));
                }
            }
        }
        return object;
    }

    private static Object tidy(Object value) {
        if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            JSONArray array = new JSONArray();
            for (Object o : arr) {
                array.put(tidy(o));
            }
            return array;
        } else if (value instanceof TypedObject) {
            return tidy((TypedObject) value);
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        } else {
            return value;
        }
    }
}
