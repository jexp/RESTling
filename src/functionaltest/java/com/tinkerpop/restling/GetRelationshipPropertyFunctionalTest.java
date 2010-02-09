package com.tinkerpop.restling;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.JsonHelper;
import com.tinkerpop.restling.domain.RelationshipRepresentation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetRelationshipPropertyFunctionalTest {
    private static String baseUri;

    @BeforeClass
    public static void startWebServer() {
        WebServer.INSTANCE.startServer();
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("LIKES");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        GraphDbHelper.setRelationshipProperties(representation, map);
        baseUri = FunctionalHelper.relationshipUri(representation) + "/properties/";
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(baseUri));
    }

    private String getPropertyUri(String key) {
        return baseUri + key;
    }

    @Test
    public void shouldGet404ForNoProperty() {
        WebResource resource = Client.create().resource(getPropertyUri("baz"));
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldGet200ForProperty() {
        String propertyUri = getPropertyUri("foo");
        WebResource resource = Client.create().resource(propertyUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGet404ForNonExistingRelationship() {
        String uri = FunctionalHelper.relationshipUri(FunctionalHelper.UNKNOWN_NODE,"LOVES")+"/properties/foo";
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldBeValidJSONOnResponse() {
        WebResource resource = Client.create().resource(getPropertyUri("foo"));
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        assertNotNull(JsonHelper.createJsonFrom(response.getEntity(String.class)));
        assertEquals(200, response.getStatus());
    }
}
