package com.tinkerpop.restling;

import static com.tinkerpop.restling.FunctionalHelper.relationshipUri;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import com.tinkerpop.restling.domain.RelationshipRepresentation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RemoveRelationshipFunctionalTest {

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
    public void shouldGet200WhenRemovingAValidRelationship() throws Exception {
        Object startNodeId = GraphDbHelper.createNode();
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("KNOWS", startNodeId, GraphDbHelper.createNode());

        ClientResponse response = sendDeleteRequest(new URI(relationshipUri(representation)));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGet404WhenRemovingAnInvalidRelationship() throws Exception {
        Object startNodeId = GraphDbHelper.createNode();
        Object relationshipId = GraphDbHelper.createRelationship("KNOWS", startNodeId, GraphDbHelper.createNode());

        ClientResponse response = sendDeleteRequest(new URI(relationshipUri(99999,"test")));

        assertEquals(404, response.getStatus());
    }

    private ClientResponse sendDeleteRequest(URI requestUri) {
        Client client = Client.create();
        WebResource resource = client.resource(requestUri);
        ClientResponse response = resource.delete(ClientResponse.class);
        return response;
    }
}
