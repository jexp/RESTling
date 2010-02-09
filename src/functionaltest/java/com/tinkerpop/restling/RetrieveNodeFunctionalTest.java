package com.tinkerpop.restling;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.JsonHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Map;

import static org.junit.Assert.*;

public class RetrieveNodeFunctionalTest {

    private static URI nodeUri;
    
    @BeforeClass
    public static void startWebServer() throws Exception {
        nodeUri = new URI(FunctionalHelper.nodeUri(GraphDbHelper.createNode()));
        WebServer.INSTANCE.startServer();
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }
    
    @Test
    public void shouldGet200WhenRetrievingNode() throws Exception {
        ClientResponse response = retrieveNodeFromService(nodeUri.toString());
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldGetContentLengthHeaderWhenRetrievingNode() throws Exception {
        ClientResponse response = retrieveNodeFromService(nodeUri.toString());
        assertNotNull(response.getMetadata().get("Content-Length"));
    }

    @Test
    public void shouldHaveJsonMediaTypeOnResponse() {
        ClientResponse response = retrieveNodeFromService(nodeUri.toString());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getType());
    }
    
    @Test
    public void shouldHaveJsonDataInResponse() throws Exception {
        ClientResponse response = retrieveNodeFromService(nodeUri.toString());
        Map<String, Object> map = JsonHelper.jsonToMap(response.getEntity(String.class));
        assertTrue(map.containsKey("self"));
    }
    
    @Test
    public void shouldGet404WhenRetrievingNonExistentNode() throws Exception {
        ClientResponse response = retrieveNodeFromService(FunctionalHelper.badUri());
        assertEquals(404, response.getStatus());
    }

    private ClientResponse retrieveNodeFromService(String uri) {
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        return response;
    }
}
