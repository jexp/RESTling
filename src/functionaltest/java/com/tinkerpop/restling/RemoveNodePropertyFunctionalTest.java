package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class RemoveNodePropertyFunctionalTest {

    @BeforeClass
    public static void startWebServer() throws Exception {
        WebServer.INSTANCE.startServer();
    }
    
    private String getPropertyUri( Object nodeId, String key )
    {
        return WebServer.BASE_URI + nodeId + "/properties/" + key;
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }
    
    @Test
    public void shouldReturn200WhenPropertyIsRemoved() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setNodeProperties( nodeId, map );
        ClientResponse response = removeNodePropertyOnServer(nodeId, "jim");
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldReturn404WhenRemovingNonExistingNodeProperty() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setNodeProperties( nodeId, map );
        ClientResponse response = removeNodePropertyOnServer(nodeId, "foo");
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void shouldReturnContentLengthZeroWhenPropertyAreRemoved() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setNodeProperties( nodeId, map );
        ClientResponse response = removeNodePropertyOnServer(nodeId, "jim");
        String contentLength = response.getMetadata().get("Content-Length").get(0);
        assertNotNull(contentLength);
        assertEquals("0", contentLength);
    }
    
    @Test
    public void shouldReturn404WhenPropertySentToANodeWhichDoesNotExist() {
        ClientResponse response = Client.create().resource(getPropertyUri( 999999, "foo" )).type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    private ClientResponse removeNodePropertyOnServer( Object nodeId, String key ) {
        return Client.create().resource(getPropertyUri( nodeId, key )).delete(ClientResponse.class);
    }
}
