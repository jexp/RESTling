package com.tinkerpop.restling;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import com.tinkerpop.restling.domain.RelationshipRepresentation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.JsonHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class SetRelationshipPropertyFunctionalTest {

    private static URI propertiesUri;
    private static URI badUri;
    
    @BeforeClass
    public static void startWebServer() throws Exception {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship( "KNOWS" );
        propertiesUri = new URI(WebServer.BASE_URI + representation.getStartNodeId()+"/relationships/" + representation.getType() + "/properties");
        badUri = new URI(WebServer.BASE_URI +representation.getStartNodeId()+ "/relationships/UNKNOWN/properties");
        WebServer.INSTANCE.startServer();
    }
    
    private static URI getPropertyUri( String key ) throws Exception
    {
        return new URI( propertiesUri.toString() + "/" + key );
    }

    @AfterClass
    public static void stopWebServer() {
        WebServer.INSTANCE.stopServer();
    }
    
    @Test
    public void shouldReturn200WhenPropertyIsSet() throws Exception {
        ClientResponse response = setPropertyOnServer("foo", "bar");
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldReturn400WhenSendinIncompatibleJsonProperty() throws Exception {
        ClientResponse response = setPropertyOnServer("jim", new HashMap<String, Object>());
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void shouldReturn400WhenSendingCorruptJsonProperty() throws Exception {
        ClientResponse response = Client.create().resource(getPropertyUri( "foo" )).type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(
                        "this:::Is::notJSON}").put(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void shouldReturn404WhenPropertySentToANodeWhichDoesNotExist() throws Exception {
        ClientResponse response = Client.create().resource(badUri.toString() + "/foo").type(
                MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).entity(
                        JsonHelper.createJsonFrom("bar")).put(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    private ClientResponse setPropertyOnServer(String key, Object value) throws Exception
    {
        return Client.create().resource(getPropertyUri( key )).type(MediaType.APPLICATION_JSON).accept(
                MediaType.APPLICATION_JSON).entity(JsonHelper.createJsonFrom(value)).put(ClientResponse.class);
    }
}
