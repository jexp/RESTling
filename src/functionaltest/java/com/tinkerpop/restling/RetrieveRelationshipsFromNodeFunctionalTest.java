package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.JsonHelper;
import com.tinkerpop.restling.domain.RelationshipRepresentationTest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RetrieveRelationshipsFromNodeFunctionalTest {

    private static Long nodeWithRelationships;
    private static Long nodeWithoutRelationships;
    private static Long nonExistingNode;

    @BeforeClass
    public static void startWebServer() {
        WebServer.INSTANCE.startServer();
        nodeWithRelationships = GraphDbHelper.createNode();
        GraphDbHelper.createRelationship("LIKES", nodeWithRelationships, GraphDbHelper.createNode());
        GraphDbHelper.createRelationship("LIKES", GraphDbHelper.createNode(), nodeWithRelationships);
        GraphDbHelper.createRelationship("HATES", nodeWithRelationships, GraphDbHelper.createNode());
        nodeWithoutRelationships = GraphDbHelper.createNode();
        nonExistingNode = nodeWithoutRelationships * 100;
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }

    private ClientResponse sendRetrieveRequestToServer(Object nodeId, String path) {
        WebResource resource = Client.create().resource(WebServer.BASE_URI + nodeId + "/relationships" + path);
        return resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    }

    private void verifyRelReps(int expectedSize, String json) {
        List<Map<String, Object>> relreps = JsonHelper.jsonToListOfRelationshipRepresentations(json);
        assertEquals(expectedSize, relreps.size());
        for (Map<String, Object> relrep : relreps) {
            RelationshipRepresentationTest.verifySerialisation(relrep);
        }
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingAllRelationshipsForANode() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithRelationships, "/all");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(3, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingIncomingRelationshipsForANode() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithRelationships, "/in");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(1, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingOutgoingRelationshipsForANode() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithRelationships, "/out");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(2, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingAllTypedRelationshipsForANode() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithRelationships, "/all/LIKES&HATES");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(3, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingIncomingTypedRelationshipsForANode() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithRelationships, "/in/LIKES");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(1, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingOutgoingTypedRelationshipsForANode() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithRelationships, "/out/HATES");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(1, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndEmptyListOfRelationshipRepresentationsWhenGettingAllRelationshipsForANodeWithoutRelationships() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithoutRelationships, "/all");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(0, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndEmptyListOfRelationshipRepresentationsWhenGettingIncomingRelationshipsForANodeWithoutRelationships() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithoutRelationships, "/in");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(0, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith200AndEmptyListOfRelationshipRepresentationsWhenGettingOutgoingRelationshipsForANodeWithoutRelationships() {
        ClientResponse response = sendRetrieveRequestToServer(nodeWithoutRelationships, "/out");
        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        verifyRelReps(0, response.getEntity(String.class));
    }

    @Test
    public void shouldRespondWith404WhenGettingAllRelationshipsForNonExistingNode() {
        ClientResponse response = sendRetrieveRequestToServer(nonExistingNode, "/all");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith404WhenGettingIncomingRelationshipsForNonExistingNode() {
        ClientResponse response = sendRetrieveRequestToServer(nonExistingNode, "/in");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith404WhenGettingOutgoingRelationshipsForNonExistingNode() {
        ClientResponse response = sendRetrieveRequestToServer(nonExistingNode, "/out");
        assertEquals(404, response.getStatus());
    }
}
