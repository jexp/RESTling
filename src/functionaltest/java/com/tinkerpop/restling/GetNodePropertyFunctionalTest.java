package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.JsonHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GetNodePropertyFunctionalTest {
    @BeforeClass
    public static void startWebServer() {
        WebServer.INSTANCE.startServer();
    }

    @AfterClass
    public static void stopWebServer() {
        WebServer.INSTANCE.stopServer();
    }

    private String getPropertyUri(String baseUri, String key) {
        return baseUri.toString() + "/properties/" + key;
    }

    @Test
    public void shouldGet404ForNoProperty() {
        Client client = Client.create();
        WebResource createResource = client.resource(WebServer.BASE_URI);
        ClientResponse createResponse = createResource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);
        WebResource resource = client.resource(getPropertyUri(createResponse.getLocation().toString(), "foo"));
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldGet200ForProperties() {
        Client client = Client.create();
        WebResource createResource = client.resource(WebServer.BASE_URI);
        String entity = JsonHelper.createJsonFrom(Collections.singletonMap("foo", "bar"));
        ClientResponse createResponse = createResource.type(MediaType.APPLICATION_JSON).entity(entity).accept(MediaType.APPLICATION_JSON).post(
                ClientResponse.class);
        WebResource resource = client.resource(getPropertyUri(createResponse.getLocation().toString(), "foo"));
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGet404ForNonExistingNode() {
        Client client = Client.create();
        WebResource resource = client.resource(getPropertyUri(WebServer.BASE_URI + "999999", "foo"));
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
        WebResource resource = client.resource(getPropertyUri(createResponse.getLocation().toString(), "foo"));
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
    }
}
