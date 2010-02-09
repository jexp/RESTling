package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.pgm.Vertex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class NodeRepresentation {

    private final Object id;
    private final URI baseUri;
    private final PropertiesMap properties;

    public NodeRepresentation(URI baseUri, Vertex node) {
        this.baseUri = baseUri;
        this.id = node.getId();
        this.properties = new PropertiesMap(node);
    }

    public URI selfUri() {
        return uri("");
    }

    private URI uri(String path) {
        try {
            return new URI(link(path));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String link(String path) {
        return baseUri.toString() + getId() + path;
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("self", selfUri().toString());
        result.put("create relationship", relationshipCreationUri().toString());
        result.put("all relationships", allRelationshipsUri().toString());
        result.put("incoming relationships", incomingRelationshipsUri().toString());
        result.put("outgoing relationships", outgoingRelationshipsUri().toString());
        result.put("all typed relationships", allTypedRelationshipsUriTemplate());
        result.put("incoming typed relationships", incomingTypedRelationshipsUriTemplate());
        result.put("outgoing typed relationships", outgoingTypedRelationshipsUriTemplate());
        result.put("properties", propertiesUri().toString());
        result.put("property", propertyUriTemplate());
        result.put("data", properties.serialize());
        return result;
    }

    public Object getId() {
        return id;
    }

    public PropertiesMap getProperties() {
        return properties;
    }

    public URI allRelationshipsUri() {
        return uri("/relationships/all");
    }

    public URI incomingRelationshipsUri() {
        return uri("/relationships/in");
    }

    public URI outgoingRelationshipsUri() {
        return uri("/relationships/out");
    }

    public String allTypedRelationshipsUriTemplate() {
        return link("/relationships/all/{-list|&|types}");
    }

    public String incomingTypedRelationshipsUriTemplate() {
        return link("/relationships/in/{-list|&|types}");
    }

    public String outgoingTypedRelationshipsUriTemplate() {
        return link("/relationships/out/{-list|&|types}");
    }

    public URI relationshipCreationUri() {
        return uri("/relationships");
    }

    public URI propertiesUri() {
        return uri("/properties");
    }

    public String propertyUriTemplate() {
        return link("/properties/{key}");
    }

}
