package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.JsonHelper;
import com.tinkerpop.restling.domain.NodeRepresentationTest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class CreateNodeFunctionalTest {
    private static final String NODE_URI_PATTERN = WebServer.BASE_URI + "[0-9]+";

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
    public void shouldGet201WhenCreatingNode() throws Exception {
        ClientResponse response = sendCreateRequestToServer();
        assertEquals(201, response.getStatus());
        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().matches(NODE_URI_PATTERN));
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        assertProperNodeRepresentation(JsonHelper.jsonToMap(response.getEntity(String.class)));
    }

    @Test
    public void shouldGet201WhenCreatingNodeWithProperties() throws Exception {
        ClientResponse response = sendCreateRequestToServer("{\"foo\" : \"bar\"}");
        assertEquals(201, response.getStatus());
        assertNotNull(response.getMetadata().get("Content-Length"));
        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().matches(NODE_URI_PATTERN));
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
        assertProperNodeRepresentation(JsonHelper.jsonToMap(response.getEntity(String.class)));
    }
    
    @Test
    public void should415IfNotASupportedMediaType() {
        Client client = Client.create();
        WebResource resource = client.resource(WebServer.BASE_URI);
        ClientResponse response = resource.type("junk/media-type").accept(MediaType.APPLICATION_JSON).entity("{\"foo\" : \"bar\"}").post(ClientResponse.class);
        assertEquals(415, response.getStatus());
    }
    
    @Test
    public void should400IfEntityBodyProvidedWhenCreatingAnEmptyNode() {
        Client client = Client.create();
        WebResource resource = client.resource(WebServer.BASE_URI);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).entity("{\"foo\" : \"bar\"}").post(ClientResponse.class);
        assertEquals(400, response.getStatus());

    }

    @Test
    public void shouldGet400WhenCreatingNodeMalformedProperties() throws Exception {
        ClientResponse response = sendCreateRequestToServer("this:::isNot::JSON}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldGet400WhenCreatingNodeUnsupportedPropertyValues() throws Exception {
        ClientResponse response = sendCreateRequestToServer("{\"foo\" : {\"bar\" : \"baz\"}}");
        assertEquals(400, response.getStatus());
    }

    private ClientResponse sendCreateRequestToServer(String json) {
        Client client = Client.create();
        WebResource resource = client.resource(WebServer.BASE_URI);
        ClientResponse response = resource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(
                json).post(ClientResponse.class);
        return response;
    }

    private ClientResponse sendCreateRequestToServer() {
        Client client = Client.create();
        WebResource resource = client.resource(WebServer.BASE_URI);
        ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(
                MediaType.APPLICATION_JSON).post(ClientResponse.class);
        return response;
    }

    @Test
    public void shouldGetValidLocationHeaderWhenCreatingNode() throws Exception {
        ClientResponse response = sendCreateRequestToServer();
        assertNotNull(response.getLocation());
        assertTrue(response.getLocation().toString().startsWith(WebServer.BASE_URI));
    }

    @Test
    public void shouldGetAContentLengthHeaderWhenCreatingANode() {
        ClientResponse response = sendCreateRequestToServer();
        assertNotNull(response.getMetadata().get("Content-Length"));
    }

    @Test
    public void shouldBeJSONContentTypeOnResponse() {
        ClientResponse response = sendCreateRequestToServer();
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
    }

    @Test
    public void shouldGetValidNodeRepresentationWhenCreatingNode() throws Exception {
        ClientResponse response = sendCreateRequestToServer();
        String entity = response.getEntity(String.class);

        Map<String, Object> map = JsonHelper.jsonToMap(entity);

        assertNotNull(map);
        assertTrue(map.containsKey("self"));

    }

    private void assertProperNodeRepresentation(Map<String, Object> noderep) {
        NodeRepresentationTest.verifySerialisation(noderep);
    }
}
