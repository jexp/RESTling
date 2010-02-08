package com.tinkerpop.restling.web;

import com.tinkerpop.restling.WebServer;
import com.tinkerpop.restling.domain.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tinkerpop.restling.domain.GraphDbHelper.longValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebServiceTest {
    private WebService service;
    private static final Long UNKNOWN_NODE = 99999999L;

    @Before
    public void doBefore()
    {
        service = new WebService( uriInfo() );
    }
    
    @AfterClass
    public static void shutdownDatabase() {
        DatabaseLocator.shutdownGraphDatabase(null);
    }

    @Test
    public void shouldRespondWith201LocationHeaderAndNodeRepresentationInJSONWhenEmptyNodeCreated() throws Exception {
        Response response = service.createEmptyNode(null);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getMetadata().get("Location").get(0));
        String json = (String) response.getEntity();

        Map<String, Object> map = JsonHelper.jsonToMap(json);

        assertNotNull(map);

        assertTrue(map.containsKey("self"));
    }

    @Test
    public void shouldRespondWith201LocationHeaderAndNodeRepresentationInJSONWhenPopulatedNodeCreated()
            throws Exception {
        Response response = service.createNode("{\"foo\" : \"bar\"}");

        assertEquals(201, response.getStatus());
        assertNotNull(response.getMetadata().get("Location").get(0));
        String json = (String) response.getEntity();

        Map<String, Object> map = JsonHelper.jsonToMap(json);

        assertNotNull(map);

        assertTrue(map.containsKey("self"));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) map.get("data");

        assertEquals("bar", data.get("foo"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRespondWith201LocationHeaderAndNodeRepresentationInJSONWhenPopulatedNodeCreatedWithArrays()
            throws Exception {
        Response response = service.createNode("{\"foo\" : [\"bar\", \"baz\"] }");

        assertEquals(201, response.getStatus());
        assertNotNull(response.getMetadata().get("Location").get(0));
        String json = (String) response.getEntity();

        Map<String, Object> map = JsonHelper.jsonToMap(json);

        assertNotNull(map);

        Map<String, Object> data = (Map<String, Object>) map.get("data");

        List<String> foo = (List<String>) data.get("foo");
        assertNotNull(foo);
        assertEquals(2, foo.size());
    }

    @Test
    public void shouldRespondWith400WhenNodeCreatedWithUnsupportedPropertyData() {
        Response response = service.createNode("{\"foo\" : {\"bar\" : \"baz\"}}");

        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenNodeCreatedWithInvalidJSON() {
        Response response = service.createNode("this:::isNot::JSON}");

        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith200AndNodeRepresentationInJSONWhenNodeRequested() throws Exception {
        Response response = service.getNode(GraphDbHelper.createNode());
        assertEquals(200, response.getStatus());
        String json = (String) response.getEntity();
        Map<String, Object> map = JsonHelper.jsonToMap(json);
        assertNotNull(map);
        assertTrue(map.containsKey("self"));
    }

    @Test
    public void shouldRespondWith404WhenRequestedNodeDoesNotExist() throws Exception {
        Response response = service.getNode(9000000000000L);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith200AfterSettingPropertiesOnExistingNode() throws Exception {
        Response response = service.setNodeProperties(GraphDbHelper.createNode(),
                "{\"foo\" : \"bar\", \"a-boolean\": true, \"boolean-array\": [true, false, false]}");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldRespondWith404WhenSettingPropertiesOnNodeThatDoesNotExist() throws Exception {
        Response response = service.setNodeProperties(9000000000000L, "{\"foo\" : \"bar\"}");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenTransferringCorruptJsonPayload() throws Exception {
        Response response = service.setNodeProperties(GraphDbHelper.createNode(), "{\"foo\" : bad-json-here \"bar\"}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenTransferringIncompatibleJsonPayload() throws Exception {
        Response response = service.setNodeProperties(GraphDbHelper.createNode(), "{\"foo\" : {\"bar\" : \"baz\"}}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith200ForGetNodeProperties() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        GraphDbHelper.setNodeProperties(nodeId, properties);
        Response response = service.getNodeProperties(nodeId);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldRespondWith204ForGetNoNodeProperties() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        Response response = service.getNodeProperties(nodeId);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void shouldGetPropertiesForGetNodeProperties() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        properties.put("double", 15.7);
        GraphDbHelper.setNodeProperties(nodeId, properties);
        Response response = service.getNodeProperties(nodeId);
        String jsonBody = response.getEntity().toString();
        Map<String, Object> readProperties = JsonHelper.jsonToMap(jsonBody);
        assertEquals(properties, readProperties);
    }

    @Test
    public void shouldRespondWith200OnSuccessfulDelete() {
        Object id = GraphDbHelper.createNode();

        Response response = service.deleteNode(id);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldRespondWith409IfNodeCannotBeDeleted() {
        Object id = GraphDbHelper.createNode();
        GraphDbHelper.createRelationship("LOVES", id, GraphDbHelper.createNode());

        Response response = service.deleteNode(id);

        assertEquals(409, response.getStatus());
    }

    @Test
    public void shouldRespondWith404IfNodeToBeDeletedDoesNotExist() {
        Object nonExistentId = 999999;
        Response response = service.deleteNode(nonExistentId);

        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith200ForSetNodeProperty() {
        Object nodeId = GraphDbHelper.createNode();
        String key = "foo";
        String json = "\"bar\"";
        Response response = service.setNodeProperty(nodeId, key, json);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldSetRightValueForSetNodeProperty() {
        Object nodeId = GraphDbHelper.createNode();
        String key = "foo";
        String value = "bar";
        String json = "\"" + value + "\"";
        service.setNodeProperty(nodeId, key, json);
        Map<String, Object> readProperties = GraphDbHelper.getNodeProperties(nodeId);
        assertEquals(Collections.singletonMap(key, value), readProperties);
    }

    @Test
    public void shouldRespondWith404ForSetNodePropertyOnNonExistingNode() {
        String key = "foo";
        String json = "\"bar\"";
        Response response = service.setNodeProperty(999999, key, json);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith400ForSetNodePropertyWithInvalidJson() {
        String key = "foo";
        String json = "{invalid json";
        Response response = service.setNodeProperty(999999, key, json);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith404ForGetNonExistentNodeProperty() {
        Object nodeId = GraphDbHelper.createNode();
        Response response = service.getNodeProperty(nodeId, "foo");
        assertEquals(204, response.getStatus());
    }

    @Test
    public void shouldRespondWith404ForGetNodePropertyOnNonExistentNode() {
        Object nodeId = 999999;
        Response response = service.getNodeProperty(nodeId, "foo");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith200ForGetNodeProperty() {
        Object nodeId = GraphDbHelper.createNode();
        String key = "foo";
        Object value = "bar";
        GraphDbHelper.setNodeProperties(nodeId, Collections.singletonMap(key, value));
        Response response = service.getNodeProperty(nodeId, "foo");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldReturnCorrectValueForGetNodeProperty() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        String key = "foo";
        Object value = "bar";
        GraphDbHelper.setNodeProperties(nodeId, Collections.singletonMap(key, value));
        Response response = service.getNodeProperty(nodeId, "foo");
        assertEquals(JsonHelper.createJsonFrom(value), response.getEntity());
    }

    @Test
    public void shouldRespondWith201AndLocationWhenRelationshipIsCreatedWithoutProperties() {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        Response response = service.createRelationship(startNode, "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\"}");
        assertEquals(201, response.getStatus());
        assertNotNull(response.getMetadata().get("Location").get(0));
    }

    @Test
    public void shouldRespondWith201AndLocationWhenRelationshipIsCreatedWithProperties() {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        Response response = service.createRelationship(startNode, "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : \"bar\"}}");
        assertEquals(201, response.getStatus());
        assertNotNull(response.getMetadata().get("Location").get(0));
    }

    @Test
    public void shouldReturnRelationshipRepresentationWhenCreatingRelationship() throws Exception {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        Response response = service.createRelationship(startNode, "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : \"bar\"}}");
        Map<String, Object> map = JsonHelper.jsonToMap((String) response.getEntity());

        assertNotNull(map);

        assertTrue(map.containsKey("self"));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) map.get("data");

        assertEquals("bar", data.get("foo"));
    }

    @Test
    public void shouldRespondWith404WhenTryingToCreateRelationshipFromNonExistentNode() {
        Object nodeId = GraphDbHelper.createNode();
        Response response = service.createRelationship(1000, "{\"to\" : \"" + WebServer.BASE_URI + nodeId
                + "\", \"type\" : \"LOVES\"}");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenTryingToCreateRelationshipToNonExistentNode() {
        Object nodeId = GraphDbHelper.createNode();
        Response response = service.createRelationship(nodeId, "{\"to\" : \"" + WebServer.BASE_URI + (1000)
                + "\", \"type\" : \"LOVES\"}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenTryingToCreateRelationshipToStartNode() {
        Object nodeId = GraphDbHelper.createNode();
        Response response = service.createRelationship(nodeId, "{\"to\" : \"" + WebServer.BASE_URI + nodeId
                + "\", \"type\" : \"LOVES\"}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenTryingToCreateRelationshipWithBadJson() {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        Response response = service.createRelationship(startNode, "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" ***and junk*** : \"LOVES\"}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith400WhenTryingToCreateRelationshipWithUnsupportedProperties() {
        Object startNode = GraphDbHelper.createNode();
        Object endNode = GraphDbHelper.createNode();
        Response response = service.createRelationship(startNode, "{\"to\" : \"" + WebServer.BASE_URI + endNode
                + "\", \"type\" : \"LOVES\", \"properties\" : {\"foo\" : {\"bar\" : \"baz\"}}}");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldRespondWith200ForRemoveNodeProperties() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        GraphDbHelper.setNodeProperties(nodeId, properties);
        Response response = service.removeNodeProperties(nodeId);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldBeAbleToRemoveNodeProperties() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        GraphDbHelper.setNodeProperties(nodeId, properties);
        service.removeNodeProperties(nodeId);
        assertEquals(true, GraphDbHelper.getNodeProperties(nodeId).isEmpty());
    }

    @Test
    public void shouldRespondWith404ForRemoveNodePropertiesForNonExistingNode() {
        Object nodeId = 999999;
        Response response = service.removeNodeProperties(nodeId);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldBeAbleToRemoveNodeProperty() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        GraphDbHelper.setNodeProperties(nodeId, properties);
        service.removeNodeProperty(nodeId, "foo");
        assertEquals(Collections.singletonMap("number", (Object) new Integer(15)), GraphDbHelper
                .getNodeProperties(nodeId));
    }

    @Test
    public void shouldGet404WhenRemovingNonExistingProperty() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        GraphDbHelper.setNodeProperties(nodeId, properties);
        Response response = service.removeNodeProperty(nodeId, "baz");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldGet404WhenRemovingPropertyFromNonExistingNode() {
        Object nodeId = 999999;
        Response response = service.removeNodeProperty(nodeId, "foo");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldGet200WhenRetrievingARelationshipFromANode() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("BEATS");
        Response response = service.getRelationship(longValue(relationshipId.getStartNodeId()),relationshipId.getType());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGet404WhenRetrievingRelationshipThatDoesNotExist() {
        Response response = service.getRelationship(UNKNOWN_NODE,"BEATS");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith200AndDataForGetRelationshipProperties() throws Exception {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("knows");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        GraphDbHelper.setRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), properties);
        Response response = service.getRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType());
        assertEquals(200, response.getStatus());
        Map<String, Object> readProperties = JsonHelper.jsonToMap(response.getEntity().toString());
        assertEquals(properties, readProperties);
    }

    @Test
    public void shouldRespondWith204ForGetNoRelationshipProperties() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("knows");
        Response response = service.getRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType());
        assertEquals(204, response.getStatus());
    }

    @Test
    public void shouldGet200WhenSuccessfullyRetrievedPropertyOnRelationship() {

        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("knows");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("some-key", "some-value");
        GraphDbHelper.setRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), properties);

        Response response = service.getRelationshipProperty(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), "some-key");

        assertEquals(200, response.getStatus());
        assertEquals("some-value", JsonHelper.jsonToSingleValue((String) response.getEntity()));
    }

    @Test
    public void shouldGet404WhenCannotResolveAPropertyOnRelationship() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("knows");
        Response response = service.getRelationshipProperty(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), "some-key");
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldGet200WhenRemovingARelationship() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("KNOWS");

        Response response = service.removeRelationship(longValue(relationshipId.getStartNodeId()),relationshipId.getType());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGet404WhenRemovingNonExistentRelationship() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship("KNOWS");

        Response response = service.removeRelationship(UNKNOWN_NODE,relationshipId.getType());
        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldRespondWith200AndListOfRelationshipRepresentationsWhenGettingRelationshipsForANode() {
        Long nodeId = GraphDbHelper.createNode();
        GraphDbHelper.createRelationship("LIKES", nodeId, GraphDbHelper.createNode());
        GraphDbHelper.createRelationship("LIKES", GraphDbHelper.createNode(), nodeId);
        GraphDbHelper.createRelationship("HATES", nodeId, GraphDbHelper.createNode());

        Response response = service.getRelationships(nodeId, RelationshipDirection.all, new AmpersandSeparatedList());
        assertEquals(200, response.getStatus());
        verifyRelReps(3, (String) response.getEntity());

        response = service.getRelationships(nodeId, RelationshipDirection.in, new AmpersandSeparatedList());
        assertEquals(200, response.getStatus());
        verifyRelReps(1, (String) response.getEntity());

        response = service.getRelationships(nodeId, RelationshipDirection.out, new AmpersandSeparatedList());
        assertEquals(200, response.getStatus());
        verifyRelReps(2, (String) response.getEntity());

        response = service.getRelationships(nodeId, RelationshipDirection.out, new AmpersandSeparatedList("LIKES&HATES"));
        assertEquals(200, response.getStatus());
        verifyRelReps(2, (String) response.getEntity());

        response = service.getRelationships(nodeId, RelationshipDirection.all, new AmpersandSeparatedList("LIKES"));
        assertEquals(200, response.getStatus());
        verifyRelReps(2, (String) response.getEntity());
    }

    private void verifyRelReps(int expectedSize, String entity) {
        List<Map<String, Object>> relreps = JsonHelper.jsonToListOfRelationshipRepresentations(entity);
        assertEquals(expectedSize, relreps.size());
        for (Map<String, Object> relrep : relreps) {
            RelationshipRepresentationTest.verifySerialisation(relrep);
        }
    }

    @Test
    public void shouldRespondWith200AndEmptyListOfRelationshipRepresentationsWhenGettingRelationshipsForANodeWithoutRelationships() {
        Long nodeId = GraphDbHelper.createNode();

        Response response = service.getRelationships(nodeId, RelationshipDirection.all, new AmpersandSeparatedList());
        assertEquals(200, response.getStatus());
        verifyRelReps(0, (String) response.getEntity());
    }

    @Test
    public void shouldRespondWith404WhenGettingIncomingRelationshipsForNonExistingNode() {
        Response response = service.getRelationships(UNKNOWN_NODE, RelationshipDirection.all, new AmpersandSeparatedList());
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void shouldRespondWith200AndSetCorrectDataWhenSettingRelationshipProperties()
    {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        String json = "{\"name\": \"Mattias\", \"age\": 30}";
        Response response = service.setRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), json );
        assertEquals( 200, response.getStatus() );
        Map<String, Object> setProperties = new HashMap<String, Object>();
        setProperties.put( "name", "Mattias" );
        setProperties.put( "age", 30 );
        assertEquals( setProperties, GraphDbHelper.getRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType()) );
    }

    @Test
    public void shouldRespondWith400WhenSettingRelationshipPropertiesWithBadJson()
    {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        String json = "{\"name: \"Mattias\", \"age\": 30}";
        Response response = service.setRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), json );
        assertEquals( 400, response.getStatus() );
    }
    
    @Test
    public void shouldRespondWith404WhenSettingRelationshipPropertiesOnNonExistingRelationship()
    {
        String json = "{\"name\": \"Mattias\", \"age\": 30}";
        Response response = service.setRelationshipProperties( UNKNOWN_NODE,"knows", json );
        assertEquals( 404, response.getStatus() );
    }
    
    @Test
    public void shouldRespondWith200AndSetCorrectDataWhenSettingRelationshipProperty()
    {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        String key = "name";
        Object value = "Mattias";
        String json = "\"" + value + "\"";
        Response response = service.setRelationshipProperty( longValue(relationshipId.getStartNodeId()),relationshipId.getType(), key, json );
        assertEquals( 200, response.getStatus() );
        assertEquals( value, GraphDbHelper.getRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType()).get( "name" ) );
    }

    @Test
    public void shouldRespondWith400WhenSettingRelationshipPropertyWithBadJson()
    {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        String json = "}Mattias";
        Response response = service.setRelationshipProperty( longValue(relationshipId.getStartNodeId()),relationshipId.getType(), "name", json );
        assertEquals( 400, response.getStatus() );
    }
    
    @Test
    public void shouldRespondWith404WhenSettingRelationshipPropertyOnNonExistingRelationship()
    {
        String json = "\"Mattias\"";
        Response response = service.setRelationshipProperty(UNKNOWN_NODE,"knows", "name", json );
        assertEquals( 404, response.getStatus() );
    }
    
    @Test
    public void shouldRespondWith200WhenSuccessfullyRemovedRelationshipProperties() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        GraphDbHelper.setRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), Collections.singletonMap("foo", (Object)"bar"));
        
        Response response = service.removeRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType());
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldRespondWith200WhenSuccessfullyRemovedRelationshipPropertiesWhichAreEmpty() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        
        Response response = service.removeRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType());
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void shouldRespondWith404WhenNoRelationshipFromWhichToRemoveProperties() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        
        Response response = service.removeRelationshipProperties(UNKNOWN_NODE,relationshipId.getType());
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void shouldRespondWith200WhenRemovingRelationshipProperty() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        GraphDbHelper.setRelationshipProperties(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), Collections.singletonMap("foo", (Object)"bar"));
        
        Response response = service.removeRelationshipProperty(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), "foo");
        
        assertEquals(200, response.getStatus());
        
    }
    
    
    @Test
    public void shouldRespondWith404WhenRemovingRelationshipPropertyWhichDoesNotExist() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        Response response = service.removeRelationshipProperty(longValue(relationshipId.getStartNodeId()),relationshipId.getType(), "foo");
        assertEquals(404, response.getStatus());

    }
    
    @Test
    public void shouldRespondWith404WhenNoRelationshipFromWhichToRemoveProperty() {
        RelationshipRepresentation relationshipId = GraphDbHelper.createRelationship( "KNOWS" );
        
        Response response = service.removeRelationshipProperty(UNKNOWN_NODE,relationshipId.getType(), "some-key");
        assertEquals(404, response.getStatus());
    }
    
    private UriInfo uriInfo() {
        UriInfo mockUriInfo = mock(UriInfo.class);
        try {
            when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://tinkerpop.org/"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return mockUriInfo;
    }
}
