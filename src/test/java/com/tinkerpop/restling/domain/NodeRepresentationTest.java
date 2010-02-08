package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.ggm.Vertex;
import org.junit.Test;
import com.tinkerpop.gremlin.models.ggm.Vertex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeRepresentationTest {
    private static final URI BASE_URI;
    static {
        try {
            BASE_URI = new URI("http://tinkerpop.org/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldHaveSelfLink() {
        assertUriMatches(uriPattern(""), noderep(1234).selfUri());
    }

    @Test
    public void shouldHaveAllRelationshipsLink() {
        assertUriMatches(uriPattern("/relationships/all"), noderep(1234).allRelationshipsUri());
    }

    @Test
    public void shouldHaveIncomingRelationshipsLink() {
        assertUriMatches(uriPattern("/relationships/in"), noderep(1234).incomingRelationshipsUri());
    }

    @Test
    public void shouldHaveOutgoingRelationshipsLink() {
        assertUriMatches(uriPattern("/relationships/out"), noderep(1234).outgoingRelationshipsUri());
    }

    @Test
    public void shouldHaveAllTypedRelationshipsLinkTemplate() {
        assertUriMatches(uriPattern("/relationships/all/\\{-list\\|&\\|types\\}"), noderep(1234)
                .allTypedRelationshipsUriTemplate());
    }

    @Test
    public void shouldHaveIncomingTypedRelationshipsLinkTemplate() {
        assertUriMatches(uriPattern("/relationships/in/\\{-list\\|&\\|types\\}"), noderep(1234)
                .incomingTypedRelationshipsUriTemplate());
    }

    @Test
    public void shouldHaveOutgoingTypedRelationshipsLinkTemplate() {
        assertUriMatches(uriPattern("/relationships/out/\\{-list\\|&\\|types\\}"), noderep(1234)
                .outgoingTypedRelationshipsUriTemplate());
    }

    @Test
    public void shouldHaveRelationshipCreationLink() {
        assertUriMatches(uriPattern("/relationships"), noderep(1234).relationshipCreationUri());
    }

    @Test
    public void shouldHavePropertiesLink() {
        assertUriMatches(uriPattern("/properties"), noderep(1234).propertiesUri());
    }

    @Test
    public void shouldHavePropertyLinkTemplate() {
        assertUriMatches(uriPattern("/properties/\\{key\\}"), noderep(1234).propertyUriTemplate());
    }

    @Test
    public void shouldHavePropertiesData() {
        PropertiesMap data = noderep(1234).getProperties();
        assertNotNull(data);
    }

    @Test
    public void shouldSerialiseToMap() {
        Map<String, Object> repr = noderep(1234).serialize();
        assertNotNull(repr);
        verifySerialisation(repr);
    }

    private static void assertUriMatches(String expectedRegex, URI actualUri) {
        assertUriMatches(expectedRegex, actualUri.toString());
    }

    private static void assertUriMatches(String expectedRegex, String actualUri) {
        assertTrue("expected <" + expectedRegex + "> got <" + actualUri + ">", actualUri.matches(expectedRegex));
    }

    private static String uriPattern(String subPath) {
        return "http://.*/[0-9]+" + subPath;
    }

    private NodeRepresentation noderep(Object id) {
        return new NodeRepresentation(BASE_URI, node(id));
    }

    private Vertex node(Object id) {
        Vertex node = mock(Vertex.class);
        when(node.getId()).thenReturn(id);
        when(node.getPropertyKeys()).thenReturn(Collections.<String> emptySet());
        return node;
    }

    
    @SuppressWarnings("unchecked")
    public static void verifySerialisation(Map<String, Object> noderep) {
        assertUriMatches(uriPattern(""), (String) noderep.get("self"));
        assertUriMatches(uriPattern("/relationships"), (String) noderep.get("create relationship"));
        assertUriMatches(uriPattern("/relationships/all"), (String) noderep.get("all relationships"));
        assertUriMatches(uriPattern("/relationships/in"), (String) noderep.get("incoming relationships"));
        assertUriMatches(uriPattern("/relationships/out"), (String) noderep.get("outgoing relationships"));
        assertUriMatches(uriPattern("/relationships/all/\\{-list\\|&\\|types\\}"), (String) noderep
                .get("all typed relationships"));
        assertUriMatches(uriPattern("/relationships/in/\\{-list\\|&\\|types\\}"), (String) noderep
                .get("incoming typed relationships"));
        assertUriMatches(uriPattern("/relationships/out/\\{-list\\|&\\|types\\}"), (String) noderep
                .get("outgoing typed relationships"));
        assertUriMatches(uriPattern("/properties"), (String) noderep.get("properties"));
        assertUriMatches(uriPattern("/properties/\\{key\\}"), (String) noderep.get("property"));
        assertNotNull((Map<String, Object>) noderep.get("data"));
    }
}
