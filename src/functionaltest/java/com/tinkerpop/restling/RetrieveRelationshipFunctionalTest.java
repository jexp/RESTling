package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.JsonHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class RetrieveRelationshipFunctionalTest {

    @BeforeClass
    public static void startWebServer() {
        WebServer.INSTANCE.startServer();
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }

    @Test
    public void shouldGet200WhenRetrievingValidRelationship() {
        Object startNodeId = GraphDbHelper.createNode();
        Object relationshipId = GraphDbHelper.createRelationship("LIKES", startNodeId, GraphDbHelper.createNode());

        ClientResponse response = Client.create().resource(WebServer.BASE_URI + "relationships/" + relationshipId).get(
                ClientResponse.class);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGetARelationshipRepresentationInJsonWhenRetrievingValidRelationship() throws Exception {
        Object startNodeId = GraphDbHelper.createNode();
        Object relationshipId = GraphDbHelper.createRelationship("LIKES", startNodeId, GraphDbHelper.createNode());

        ClientResponse response = Client.create().resource(WebServer.BASE_URI + "relationships/" + relationshipId).get(
                ClientResponse.class);

        String entity = response.getEntity(String.class);
        assertNotNull(entity);
        isLegalJson(entity);
    }

    private void isLegalJson(String entity) throws IOException {
        JsonHelper.jsonToMap(entity);
    }
}
