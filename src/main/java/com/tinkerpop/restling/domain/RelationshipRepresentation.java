package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.ggm.Edge;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class RelationshipRepresentation {

    private final Object id;
    private final URI baseUri;
    private final PropertiesMap properties;
    private final Object startNodeId;
    private final Object endNodeId;
    private final String type;

    public RelationshipRepresentation(URI baseUri, Edge relationship) {
        this.baseUri = baseUri;
        this.id = relationship.getId();
        this.startNodeId = relationship.getOutVertex().getId();
        this.endNodeId = relationship.getInVertex().getId();
        this.properties = new PropertiesMap(relationship);
        this.type = relationship.getLabel();
    }

    public URI selfUri() {
        return uri(link(""));
    }

    private URI uri(String link) {
        try {
            return new URI(link);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String link(String path) {
        return baseUri + "relationships/" + getId() + path;
    }

    private String nodeLink(Object nodeId) {
        return baseUri.toString() + nodeId;
    }

    public Object getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Object getStartNodeId() {
        return startNodeId;
    }

    public Object getEndNodeId() {
        return endNodeId;
    }

    public URI startNodeUri() {
        return uri(nodeLink(startNodeId));
    }

    public URI endNodeUri() {
        return uri(nodeLink(endNodeId));
    }

    public URI propertiesUri() {
        return uri(link("/properties"));
    }

    public String propertyUriTemplate() {
        return link("/properties/{key}");
    }

    public PropertiesMap getProperties() {
        return properties;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("self", selfUri().toString());
        result.put("start", startNodeUri().toString());
        result.put("end", endNodeUri().toString());
        result.put("type", getType());
        result.put("properties", propertiesUri().toString());
        result.put("property", propertyUriTemplate());
        result.put("data", properties.serialize());
        return result;
    }
    public static RelationshipRepresentation represent(URI baseUrl, Edge edge) {
        return new RelationshipRepresentation(baseUrl,edge);
    }
}
