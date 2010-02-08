package com.tinkerpop.restling.domain;

import org.junit.Test;
import com.tinkerpop.gremlin.models.ggm.Vertex;
import com.tinkerpop.gremlin.models.ggm.Edge;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelationshipRepresentationTest {
    private static final URI BASE_URI;
    static {
        try {
            BASE_URI = new URI("http://tinkerpop.org/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String NODE_URI_PATTERN = "http://.*/[0-9]+";
    private static final String RELATIONSHIP_URI_PATTERN = "http://.*/relationships/[0-9]+";

    @Test
    public void shouldHaveSelfLink() {
        assertUriMatches(RELATIONSHIP_URI_PATTERN, relrep(1234).selfUri());
    }

    @Test
    public void shouldHaveType() {
        assertNotNull(relrep(1234).getType());
    }

    @Test
    public void shouldHaveStartNodeLink() {
        assertUriMatches(NODE_URI_PATTERN, relrep(1234).startNodeUri());
    }

    @Test
    public void shouldHaveEndNodeLink() {
        assertUriMatches(NODE_URI_PATTERN, relrep(1234).endNodeUri());
    }

    @Test
    public void shouldHavePropertiesLink() {
        assertUriMatches(RELATIONSHIP_URI_PATTERN + "/properties", relrep(1234).propertiesUri());
    }

    @Test
    public void shouldHavePropertyLinkTemplate() {
        assertUriMatches(RELATIONSHIP_URI_PATTERN + "/properties/\\{key\\}", relrep(1234).propertyUriTemplate());
    }

    @Test
    public void shouldHavePropertiesData() {
        RelationshipRepresentation relrep = new RelationshipRepresentation(BASE_URI, relationship(1234));
        PropertiesMap data = relrep.getProperties();
        assertNotNull(data);
    }

    @Test
    public void shouldSerialiseToMap() {
        RelationshipRepresentation relrep = new RelationshipRepresentation(BASE_URI, relationship(1234));
        Map<String, Object> repr = relrep.serialize();
        assertNotNull(repr);
        verifySerialisation(repr);
    }

    private static void assertUriMatches(String expectedRegex, URI actualUri) {
        assertUriMatches(expectedRegex, actualUri.toString());
    }

    private static void assertUriMatches(String expectedRegex, String actualUri) {
        assertTrue("expected <" + expectedRegex + "> got <" + actualUri + ">", actualUri.matches(expectedRegex));
    }

    private RelationshipRepresentation relrep(Object id) {
        return new RelationshipRepresentation(BASE_URI, relationship(id));
    }

    private Edge relationship(Object id) {
        Vertex startNode = mock(Vertex.class);
        when(startNode.getId()).thenReturn(0L);

        Vertex endNode = mock(Vertex.class);
        when(endNode.getId()).thenReturn(1L);

        Edge relationship = mock(Edge.class);
        when(relationship.getId()).thenReturn(id);
        when(relationship.getPropertyKeys()).thenReturn(Collections.<String> emptySet());
        when(relationship.getInVertex()).thenReturn(startNode);
        when(relationship.getOutVertex()).thenReturn(endNode);
        when(relationship.getLabel()).thenReturn("LOVES");

        return relationship;
    }

    @SuppressWarnings("unchecked")
    public static void verifySerialisation(Map<String, Object> relrep) {
        assertUriMatches(RELATIONSHIP_URI_PATTERN, (String) relrep.get("self"));
        assertUriMatches(NODE_URI_PATTERN, (String) relrep.get("start"));
        assertUriMatches(NODE_URI_PATTERN, (String) relrep.get("end"));
        assertNotNull((String) relrep.get("type"));
        assertUriMatches(RELATIONSHIP_URI_PATTERN + "/properties", (String) relrep.get("properties"));
        assertUriMatches(RELATIONSHIP_URI_PATTERN + "/properties/\\{key\\}", (String) relrep.get("property"));
        assertNotNull((Map<String, Object>) relrep.get("data"));
    }
}
