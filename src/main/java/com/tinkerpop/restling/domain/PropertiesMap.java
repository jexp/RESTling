package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.pgm.Element;

import java.lang.reflect.Array;
import java.util.*;


public class PropertiesMap {

    private final Map<String, Object> values = new HashMap<String, Object>();

    public PropertiesMap(Element container) {
        for (String key : container.getPropertyKeys()) {
            values.put(key, container.getProperty(key));
        }
    }

    public PropertiesMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            values.put(entry.getKey(), toInternalType(entry.getValue()));
        }
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.put(entry.getKey(), toSerializedType(entry.getValue()));
        }
        return result;
    }

    void storeTo(Element container) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            container.setProperty(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static Object toInternalType(Object value) {
        if (value instanceof List) {
            List list = (List) value;
            if (list.isEmpty()) {
                return new byte[0];
            } else {
                Object first = list.get(0);
                if (first instanceof String) {
                    return stringArray(list);
                } else if (first instanceof Number) {
                    return numberArray(list);
                } else if (first instanceof Boolean) {
                    return booleanArray(list);
                } else {
                    throw new PropertyValueException("Unsupported array type " + first.getClass());
                }
            }
        } else {
            return assertSupportedPropertyValue( value );
        }
    }
    
    public static Object assertSupportedPropertyValue( Object value )
    {
        if (value instanceof String) {
        } else if (value instanceof Number) {
        } else if (value instanceof Boolean) {
        } else {
            throw new PropertyValueException("Unsupported value type " + value.getClass());
        }
        return value;
    }

    private static Boolean[] booleanArray( List<Boolean> list )
    {
        return list.toArray( new Boolean[list.size()] );
    }

    private static Number[] numberArray(List<Number> numbers) {
        Number[] internal = new Number[numbers.size()];
        for (int i = 0; i < internal.length; i++) {
            Number number = numbers.get(i);
            if (number instanceof Float || number instanceof Double) {
                number = number.doubleValue();
            } else {
                number = number.longValue();
            }
            internal[i] = number;
        }
        if (internal[0] instanceof Double) {
            return Arrays.copyOf(internal, internal.length, Double[].class);
        } else {
            return Arrays.copyOf(internal, internal.length, Long[].class);
        }
    }

    private static String[] stringArray(List<String> strings) {
        return strings.toArray(new String[strings.size()]);
    }

    private Object toSerializedType(Object value) {
        if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().isPrimitive()) {
                int size = Array.getLength(value);
                List<Object> result = new ArrayList<Object>();
                for (int i = 0; i < size; i++) {
                    result.add(Array.get(value, i));
                }
                return result;
            } else {
                return Arrays.asList((Object[]) value);
            }
        } else {
            return value;
        }
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
