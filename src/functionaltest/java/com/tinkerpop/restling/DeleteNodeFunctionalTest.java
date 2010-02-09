package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class DeleteNodeFunctionalTest {
    private static final Object NON_EXISTENT_NODE_ID = 999999;

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
    public void shouldRespondWith200WhenNodeDeleted() throws Exception {
        ClientResponse response = sendDeleteRequestToServer(GraphDbHelper.createNode());
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldRespondWith404WhenNodeToBeDeletedCannotBeFound() throws Exception {
        ClientResponse response = sendDeleteRequestToServer(NON_EXISTENT_NODE_ID);
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void shouldRespondWith409WhenNodeCannotBeDeleted() throws Exception {
        Object id = GraphDbHelper.createNode();
        GraphDbHelper.createRelationship("LOVES", id, GraphDbHelper.createNode());
        ClientResponse response = sendDeleteRequestToServer(id);
        assertEquals(409, response.getStatus());
    }

    private ClientResponse sendDeleteRequestToServer(Object id) throws Exception {
        return Client.create().resource(new URI(WebServer.BASE_URI + id)).delete(ClientResponse.class);
    }
}
