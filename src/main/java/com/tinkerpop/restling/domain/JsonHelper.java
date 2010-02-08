package com.tinkerpop.restling.domain;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonHelper {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) {
        return (Map<String, Object>) readJson( json );
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> jsonToListOfRelationshipRepresentations(String json) {
        return (List<Map<String, Object>>) readJson( json );
    }
    
    private static Object readJson( String json )
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, Object.class);
        } catch (IOException e) {
            throw new JsonParseRuntimeException( e );
        }
    }

    public static Object jsonToSingleValue(String json) {
        return PropertiesMap.assertSupportedPropertyValue( readJson( json ) );
    }

    public static String createJsonFrom(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new JsonParseRuntimeException( e );
        }
    }

    public static String createJsonFromList(List<RelationshipRepresentation> relreps) {
        StringBuilder result = new StringBuilder("[");
        String sep = "";
        for (RelationshipRepresentation relrep : relreps) {
            result.append(sep);
            result.append(createJsonFrom(relrep.serialize()));
            sep = ", ";
        }
        result.append("]");
        return result.toString();
    }
}
