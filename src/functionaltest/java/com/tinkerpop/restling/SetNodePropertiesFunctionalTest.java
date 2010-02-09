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
import com.tinkerpop.restling.domain.JsonHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class SetNodePropertiesFunctionalTest {

    private static URI propertiesUri;
    private static URI badUri;
    
    @BeforeClass
    public static void startWebServer() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        propertiesUri = new URI(WebServer.BASE_URI + nodeId + "/properties");
        badUri = new URI(FunctionalHelper.badUri() +"/properties");
        WebServer.INSTANCE.startServer();
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }
    
    @Test
    public void shouldReturn200WhenPropertiesAreUpdated() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        ClientResponse response = updateNodePropertiesOnServer(map);
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldReturnContentLengthZeroWhenPropertiesAreUpdated() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        ClientResponse response = updateNodePropertiesOnServer(map);
        String contentLength = response.getMetadata().get("Content-Length").get(0);
        assertNotNull(contentLength);
        assertEquals("0", contentLength);
    }
    
    
    
    @Test
    public void shouldReturn400WhenSendinIncompatibleJsonProperties() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", new HashMap<String, Object>());
        ClientResponse response = updateNodePropertiesOnServer(map);
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void shouldReturn400WhenSendingCorruptJsonProperties() {
        ClientResponse response = Client.create().resource(propertiesUri).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity("this:::Is::notJSON}").put(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void shouldReturn404WhenPropertiesSentToANodeWhichDoesNotExist() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        ClientResponse response = Client.create().resource(badUri).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(JsonHelper.createJsonFrom(map)).put(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    private ClientResponse updateNodePropertiesOnServer(Map<String, Object> map) {
        return Client.create().resource(propertiesUri).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(JsonHelper.createJsonFrom(map)).put(ClientResponse.class);
    }
}
