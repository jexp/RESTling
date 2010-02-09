package com.tinkerpop.restling;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.RelationshipRepresentation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.tinkerpop.restling.FunctionalHelper.relationshipUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RemoveRelationshipPropertiesFunctionalTest {

    @BeforeClass
    public static void startWebServer() throws Exception {
        WebServer.INSTANCE.startServer();
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }

    @Test
    public void shouldReturn200WhenPropertiesAreRemovedFromRelationship() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("LOVES");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setRelationshipProperties(relationshipId, map);
        ClientResponse response = removeRelationshipPropertiesOnServer(relationshipId);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldReturnContentLengthZeroWhenPropertiesAreRemovedFromRelationship() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("LOVES");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setRelationshipProperties(relationshipId, map);
        ClientResponse response = removeRelationshipPropertiesOnServer(relationshipId);
        String contentLength = response.getMetadata().get("Content-Length").get(0);
        assertNotNull(contentLength);
        assertEquals("0", contentLength);
    }

    @Test
    public void shouldReturn404WhenPropertiesRemovedFromRelationshipWhichDoesNotExist() {
        ClientResponse response = Client.create().resource(relationshipUri(999999, "test") + "/properties").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    private ClientResponse removeRelationshipPropertiesOnServer(RelationshipRepresentation relationshipId) {
        return Client.create().resource(relationshipUri(relationshipId) + "/properties").delete(ClientResponse.class);
    }
}
