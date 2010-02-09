package com.tinkerpop.restling;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.tinkerpop.restling.domain.DatabaseLocator;
import com.tinkerpop.restling.domain.GraphDbHelper;
import com.tinkerpop.restling.domain.RelationshipRepresentation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.tinkerpop.restling.FunctionalHelper.relationshipUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore
public class RemoveRelationshipPropertyFunctionalTest {

    @BeforeClass
    public static void startWebServer() throws Exception {
        WebServer.INSTANCE.startServer();
    }
    
    private String getPropertyUri( RelationshipRepresentation representation, String key )
    {
        return relationshipUri(representation.getStartNodeId(), representation.getType()) + "/properties/" + key;
    }
    private String getPropertyUri( Object nodeId, String label, String key )
    {
        return relationshipUri(nodeId, label) + "/properties/" + key;
    }

    @AfterClass
    public static void stopWebServer() throws Exception {
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(WebServer.BASE_URI));
    }

    @Ignore
    @Test
    public void shouldReturn200WhenRelationshipPropertyIsRemoved() {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("LOVES");
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setRelationshipProperties( representation, map );
        ClientResponse response = removeRelationshipPropertyOnServer(representation, "jim");
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldReturn404WhenRemovingNonExistentRelationshipProperty() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("KNOWS");
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setRelationshipProperties( relationshipId, map );
        ClientResponse response = removeRelationshipPropertyOnServer(relationshipId, "foo");
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void shouldReturnContentLengthZeroWhenPropertyIsRemoved() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("TEACHES-SHORTCUTS");
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("jim", "tobias");
        GraphDbHelper.setRelationshipProperties( relationshipId, map );
        ClientResponse response = removeRelationshipPropertyOnServer(relationshipId, "jim");
        String contentLength = response.getMetadata().get("Content-Length").get(0);
        assertNotNull(contentLength);
        assertEquals("0", contentLength);
    }
    
    @Test
    public void shouldReturn404WhenPropertyRemovedFromARelationshipWhichDoesNotExist() {
        ClientResponse response = Client.create().resource(getPropertyUri( 999999L,"test", "foo" )).delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    private ClientResponse removeRelationshipPropertyOnServer( RelationshipRepresentation representation, String key ) {
        return Client.create().resource(getPropertyUri( representation, key )).delete(ClientResponse.class);
    }
}
