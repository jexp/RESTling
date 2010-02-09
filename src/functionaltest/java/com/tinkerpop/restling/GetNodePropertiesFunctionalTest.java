package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.JsonHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GetNodePropertiesFunctionalTest {
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
    public void shouldGet204ForNoProperties() {
        Client client = Client.create();
        WebResource createResource = client.resource(WebServer.BASE_URI);
        ClientResponse createResponse = createResource.accept(MediaType.APPLICATION_JSON).entity("").post(ClientResponse.class);
        WebResource resource = client.resource(createResponse.getLocation().toString() + "/properties");
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void shouldGet200ForProperties() {
        Client client = Client.create();
        WebResource createResource = client.resource(WebServer.BASE_URI);
        String entity = JsonHelper.createJsonFrom(Collections.singletonMap("foo", "bar"));
        ClientResponse createResponse = createResource.type(MediaType.APPLICATION_JSON).entity(entity).accept(MediaType.APPLICATION_JSON).post(
                ClientResponse.class);
        WebResource resource = client.resource(createResponse.getLocation().toString() + "/properties");
        ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGetContentLengthHeaderForRetrievingProperties() {
        Client client = Client.create();
        WebResource createResource = client.resource(WebServer.BASE_URI);
        String entity = JsonHelper.createJsonFrom(Collections.singletonMap("foo", "bar"));
        ClientResponse createResponse = createResource.type(MediaType.APPLICATION_JSON).entity(entity).accept(MediaType.APPLICATION_JSON).post(
                ClientResponse.class);
        WebResource resource = client.resource(createResponse.getLocation().toString() + "/properties");
        ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertNotNull(response.getMetadata().get("Content-Length"));
    }

    @Test
    public void shouldGet404ForNonExistingNode() {
        Client client = Client.create();
        WebResource resource = client.resource(WebServer.BASE_URI + "999999/properties");
        ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldBeJSONContentTypeOnResponse() {
        Client client = Client.create();
        WebResource createResource = client.resource(WebServer.BASE_URI);
        String entity = JsonHelper.createJsonFrom(Collections.singletonMap("foo", "bar"));
        ClientResponse createResponse = createResource.type(MediaType.APPLICATION_JSON).entity(entity).accept(MediaType.APPLICATION_JSON).post(
                ClientResponse.class);
        WebResource resource = client.resource(createResponse.getLocation().toString() + "/properties");
        ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
    }
}
