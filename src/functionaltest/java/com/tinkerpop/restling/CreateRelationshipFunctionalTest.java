package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
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

public class CreateRelationshipFunctionalTest {

    private static final String RELATIONSHIP_URI_PATTERN = WebServer.BASE_URI + "relationships/[0-9]+";

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
    public void shouldRespondWith201WhenSuccessfullyCreatedRelationshipWithProperties() throws Exception {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        String jsonString = "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : \"bar\"}}";
        ClientResponse response = Client.create().resource(WebServer.BASE_URI + startNode + "/relationships").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(jsonString).post(
                ClientResponse.class);
        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().matches(RELATIONSHIP_URI_PATTERN));
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        assertProperRelationshipRepresentation(JsonHelper.jsonToMap(response.getEntity(String.class)));
    }

    @Test
    public void shouldRespondWith201WhenSuccessfullyCreatedRelationship() throws Exception {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        String jsonString = "{\"to\" : \"" + WebServer.BASE_URI + endNode + "\", \"type\" : \"LOVES\"}";
        ClientResponse response = Client.create().resource(WebServer.BASE_URI + startNode + "/relationships").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(jsonString).post(
                ClientResponse.class);
        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().matches(RELATIONSHIP_URI_PATTERN));
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        assertProperRelationshipRepresentation(JsonHelper.jsonToMap(response.getEntity(String.class)));
    }

    @Test
    public void shouldRespondWith404WhenStartNodeDoesNotExist() {
        Object endNode = GraphDbHelper.createNode();
        String jsonString = "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : \"bar\"}}";
        ClientResponse response = Client.create().resource(WebServer.BASE_URI + "999999/relationships").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(jsonString).post(
                ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenEndNodeDoesNotExist() {
        Object startNode = GraphDbHelper.createNode();
        String jsonString = "{\"to\" : \"" + WebServer.BASE_URI
                + "999999\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : \"bar\"}}";
        ClientResponse response = Client.create().resource(WebServer.BASE_URI + startNode + "/relationships").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(jsonString).post(
                ClientResponse.class);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenBadJsonProvided() {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        String jsonString = "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : **BAD JSON HERE*** \"bar\"}}";
        ClientResponse response = Client.create().resource(WebServer.BASE_URI + startNode + "/relationships").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(jsonString).post(
                ClientResponse.class);

        assertEquals(400, response.getStatus());
    }

    private void assertProperRelationshipRepresentation(Map<String, Object> relrep) {
        RelationshipRepresentationTest.verifySerialisation(relrep);
    }
}
