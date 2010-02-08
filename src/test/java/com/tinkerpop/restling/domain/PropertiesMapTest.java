package com.tinkerpop.restling.domain;

import org.junit.Test;
import com.tinkerpop.gremlin.models.ggm.Element;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesMapTest {

    @Test
    public void shouldContainAddedPropertiesWhenCreatedFromPropertyContainer() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("foo", "bar");
        PropertiesMap properties = new PropertiesMap(container(values));
        assertEquals("bar", properties.getValue("foo"));
    }

    @Test
    public void shouldContainAddedPropertiesWhenCreatedFromMap() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("foo", "bar");
        PropertiesMap properties = new PropertiesMap(values);
        assertEquals("bar", properties.getValue("foo"));
    }

    @Test
    public void shouldSerializeToMapWithSamePropertiesWhenCreatedFromPropertyContainer() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("foo", "bar");
        PropertiesMap properties = new PropertiesMap(container(values));
        Map<String, Object> map = properties.serialize();
        assertEquals(values, map);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertToNeo4jValueTypesWhenCreatingFromMap() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "value");
        values.put("int", 5);
        values.put("Object", 17L);
        values.put("double", 3.14);
        values.put("float", 42.0f);
        values.put("string list", Arrays.asList("one", "two"));
        values.put("Object list", Arrays.asList(5, 17L));
        values.put("double list", Arrays.asList(3.14, 42.0f));

        PropertiesMap properties = new PropertiesMap(values);

        assertEquals("value", properties.getValue("string"));
        assertEquals(5, ((Integer) properties.getValue("int")).intValue());
        assertEquals(17L, ((Long) properties.getValue("Object")).longValue());
        assertEquals(3.14, ((Double) properties.getValue("double")).doubleValue(), 0);
        assertEquals(42.0f, ((Float) properties.getValue("float")).floatValue(), 0);
        assertArrayEquals(new String[] { "one", "two" }, (String[]) properties.getValue("string list"));
        assertArrayEquals(new Long[] { 5L, 17L }, (Long[]) properties.getValue("Object list"));
        assertArrayEquals(new Double[] { 3.14, 42.0 }, (Double[]) properties.getValue("double list"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSerializeToMap() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "value");
        values.put("int", 5);
        values.put("Object", 17L);
        values.put("double", 3.14);
        values.put("float", 42.0f);
        values.put("string array", new String[] { "one", "two" });
        values.put("Object array", new Object[] { 5L, 17L });
        values.put("double array", new double[] { 3.14, 42.0 });

        PropertiesMap properties = new PropertiesMap(container(values));
        Map<String, Object> map = properties.serialize();

        assertEquals("value", map.get("string"));
        assertEquals(5, map.get("int"));
        assertEquals(17L, map.get("Object"));
        assertEquals(3.14, map.get("double"));
        assertEquals(42.0f, map.get("float"));
        assertEqualContent(Arrays.asList("one", "two"), (List) map.get("string array"));
        assertEqualContent(Arrays.asList(5L, 17L), (List) map.get("Object array"));
        assertEqualContent(Arrays.asList(3.14, 42.0), (List) map.get("double array"));
    }

    @Test
    public void shouldBeAbleToSignalEmptiness() {
        Map<String, Object> values = new HashMap<String, Object>();
        PropertiesMap properties = new PropertiesMap(values);
        values.put("key", "value");
        assertTrue(properties.isEmpty());
        properties = new PropertiesMap(values);
        assertFalse(properties.isEmpty());
    }

    private void assertEqualContent(List<?> expected, List<?> actual) {
        assertEquals(expected.size(), actual.size());
        for (Iterator<?> ex = expected.iterator(), ac = actual.iterator(); ex.hasNext() && ac.hasNext();) {
            assertEquals(ex.next(), ac.next());
        }
    }

    private Element container(Map<String, Object> values) {
        Element container = mock(Element.class);
        when(container.getPropertyKeys()).thenReturn(values.keySet());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            when(container.getProperty(entry.getKey())).thenReturn(entry.getValue());
        }
        return container;
    }

}
