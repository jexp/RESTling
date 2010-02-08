package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.ggm.Edge;
import com.tinkerpop.gremlin.models.ggm.Element;
import com.tinkerpop.gremlin.models.ggm.Graph;
import com.tinkerpop.gremlin.models.ggm.Vertex;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.NotFoundException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.tinkerpop.restling.domain.DatabaseLocator.inTransaction;
import static org.junit.Assert.*;

public class StorageActionsTest {

    private static final URI BASE_URI;

    static {
        try {
            BASE_URI = new URI("http://tinkerpop.org/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private StorageActions actions;

    @AfterClass
    public static void shutDownDatabase() {
        DatabaseLocator.shutdownGraphDatabase(BASE_URI);
    }

    private Object createNode(final Map<String, Object> properties) {
        final Graph graphdb = DatabaseLocator.getGraphDatabase(BASE_URI);
        return inTransaction(new Callable<Object>() {
            public Object call() throws Exception {
                Vertex node = graphdb.addVertex(null);
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    node.setProperty(entry.getKey(), entry.getValue());
                }
                return node.getId();
            }
        });
    }

    @Before
    public void doBefore() {
        this.actions = new StorageActions(BASE_URI);
    }

    @Test
    public void createdNodeShouldBeInDatabase() throws Exception {
        NodeRepresentation noderep = actions.createNode(new PropertiesMap(Collections.<String, Object>emptyMap()));
        Graph graphdb = DatabaseLocator.getGraphDatabase(BASE_URI);
        assertNotNull(GraphDbHelper.getNode(noderep.getId()));
    }

    @Test
    public void nodeInDatabaseShouldBeRetreivable() {
        Object nodeId = GraphDbHelper.createNode();
        assertNotNull(actions.retrieveNode(nodeId));
    }

    @Test
    public void shouldBeAbleToStorePropertiesInAnExistingNode() {
        Object nodeId = GraphDbHelper.createNode();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("baz", 17);

        actions.setNodeProperties(nodeId, new PropertiesMap(properties));

        Graph graphdb = DatabaseLocator.getGraphDatabase(BASE_URI);
        final Map<String, Object> props = GraphDbHelper.getNodeProperties(nodeId);
        assertSameProperties(properties, props);
    }

    @Test
    public void shouldOverwriteExistingProperties() {
        Graph graphdb = DatabaseLocator.getGraphDatabase(BASE_URI);
        final Object nodeId = createNode(Collections.<String, Object>singletonMap("remove me", "trash"));
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("baz", 17);
        actions.setNodeProperties(nodeId, new PropertiesMap(properties));
        final Map<String, Object> props = GraphDbHelper.getNodeProperties(nodeId);
        assertSameProperties(properties, props);
        assertFalse(props.containsKey("remove me"));
    }

    @Test
    public void shouldBeAbleToGetPropertiesOnNode() {
        Graph graphDb = DatabaseLocator.getGraphDatabase(BASE_URI);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("neo", "Thomas A. Anderson");
        properties.put("number", 15);
        Object nodeId = createNode(properties);
        Map<String, Object> readProperties = actions.getNodeProperties(nodeId).serialize();
        assertEquals(properties, readProperties);
    }

    @Test
    public void shouldRemoveNodeWithNoRelationsFromDBOnDelete() {
        final Graph graphdb = DatabaseLocator.getGraphDatabase(BASE_URI);
        Object nodeId = inTransaction(new Callable<Object>() {
            public Object call() throws Exception {
                Vertex node = graphdb.addVertex(null);
                return node.getId();
            }
        });
        int nodeCount = GraphDbHelper.getNumberOfNodes();
        actions.deleteNode(nodeId);
        assertEquals(nodeCount - 1, GraphDbHelper.getNumberOfNodes());
    }

    @Test
    public void shouldBeAbleToSetPropertyOnNode() {
        Object nodeId = createNode(Collections.<String, Object>emptyMap());
        String key = "foo";
        Object value = "bar";
        actions.setNodeProperty(nodeId, key, value);
        assertEquals(Collections.singletonMap(key, value), GraphDbHelper.getNodeProperties(nodeId));
    }

    @Test
    public void shouldBeAbleToGetPropertyOnNode() {
        String key = "foo";
        Object value = "bar";
        Object nodeId = createNode(Collections.singletonMap(key, (Object) value));
        assertEquals(value, actions.getNodeProperty(nodeId, key));
    }

    @Test
    public void shouldBeAbleToRemoveNodeProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        final Object nodeId = createNode(properties);
        actions.removeNodeProperties(nodeId);
        final Graph graphDb = DatabaseLocator.getGraphDatabase(BASE_URI);
        inTransaction(new Runnable() {
            public void run() {
                Vertex node = graphDb.getVertex(nodeId);
                assertEquals(false, node.getPropertyKeys().iterator().hasNext());
            }
        });
    }

    @Test
    public void shouldStoreRelationshipsBetweenTwoExistingNodes() throws Exception {
        int relationshipCount = GraphDbHelper.getNumberOfRelationships();
        actions.createRelationship("LOVES", GraphDbHelper.createNode(),
                GraphDbHelper.createNode(), new PropertiesMap(Collections.<String, Object>emptyMap()));
        assertEquals(relationshipCount + 1, GraphDbHelper.getNumberOfRelationships());
    }

    @Test
    public void shouldStoreSuppliedPropertiesWhenCreatingRelationship() throws Exception {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("string", "value");
        properties.put("integer", 17);
        final RelationshipRepresentation relId = actions.createRelationship("LOVES", GraphDbHelper.createNode(),
                GraphDbHelper.createNode(), new PropertiesMap(properties));
        final Graph graphdb = DatabaseLocator.getGraphDatabase(BASE_URI);
        inTransaction(new Runnable() {
            public void run() {
                Edge rel = GraphDbHelper.getRelationship(relId.getStartNodeId(), relId.getType());
                for (String key : rel.getPropertyKeys()) {
                    assertTrue("extra property stored", properties.containsKey(key));
                }
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    assertEquals(entry.getValue(), rel.getProperty(entry.getKey()));
                }
            }
        });
    }

    @Test(expected = EndNodeNotFoundException.class)
    public void shouldNotCreateRelationshipBetweenNonExistentNodes() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        PropertiesMap properties = new PropertiesMap(Collections.<String, Object>emptyMap());
        actions.createRelationship("Loves", nodeId, 1000, properties);
    }
    @Test(expected = StartNodeNotFoundException.class)
    public void shouldNotCreateRelationshipBetweenNonExistentNodes2() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        PropertiesMap properties = new PropertiesMap(Collections.<String, Object>emptyMap());
        actions.createRelationship("Loves", 1000, nodeId, properties);
    }

    @Test(expected = StartNodeSameAsEndNodeException.class)
    public void shouldNotCreateRelationshipWithSameStartAsEndNode() throws Exception {
        Object nodeId = GraphDbHelper.createNode();
        PropertiesMap properties = new PropertiesMap(Collections.<String, Object>emptyMap());
        actions.createRelationship("Loves", nodeId, nodeId, properties);
    }

    @Test
    public void shouldBeAbleToRemoveNodeProperty() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        final Object nodeId = createNode(properties);
        actions.removeNodeProperty(nodeId, "foo");
        final Graph graphDb = DatabaseLocator.getGraphDatabase(BASE_URI);
        inTransaction(new Runnable() {
            public void run() {
                Vertex node = graphDb.getVertex(nodeId);
                assertEquals(15, node.getProperty("number"));
                assertEquals(null, node.getProperty("foo"));
            }
        });
    }

    @Test
    public void shouldReturnTrueIfNodePropertyRemoved() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        Object nodeId = createNode(properties);
        assertEquals(true, actions.removeNodeProperty(nodeId, "foo"));
    }

    @Test
    public void shouldReturnFalseIfNodePropertyNotRemoved() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 15);
        Object nodeId = createNode(properties);
        assertEquals(false, actions.removeNodeProperty(nodeId, "baz"));
    }

    @Test
    public void shouldBeAbleToRetrieveARelationship() {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("ENJOYED");
        assertNotNull(actions.retrieveRelationship(representation.getStartNodeId(), representation.getType()));
    }

    @Test
    public void shouldBeAbleToGetPropertiesOnRelationship() {
        final Graph graphDb = DatabaseLocator.getGraphDatabase(BASE_URI);
        Object relationshipId;
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("neo", "Thomas A. Anderson");
        properties.put("number", 15);
        RelationshipRepresentation representation = inTransaction(new Callable<RelationshipRepresentation>() {
            public RelationshipRepresentation call() throws Exception {
                Vertex startNode = graphDb.addVertex(null);
                Vertex endNode = graphDb.addVertex(null);
                Edge relationship = graphDb.addEdge(null, startNode, endNode, "knows");
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    relationship.setProperty(entry.getKey(), entry.getValue());
                }
                return RelationshipRepresentation.represent(null, relationship);
            }
        });

        Map<String, Object> readProperties = actions.getRelationshipProperties(representation.getStartNodeId(), representation.getType())
                .serialize();
        assertEquals(properties, readProperties);
    }

    @Test
    public void shouldBeAbleToRetrieveASinglePropertyFromARelationship() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("neo", "Thomas A. Anderson");
        properties.put("number", 15);

        RelationshipRepresentation representation = GraphDbHelper.createRelationship("LOVES");
        GraphDbHelper.setRelationshipProperties(representation.getStartNodeId(), representation.getType(), properties);

        Object relationshipProperty = actions.getRelationshipProperty(representation.getStartNodeId(), representation.getType(), "foo");
        assertEquals("bar", relationshipProperty);
    }

    @Test(expected = RelationshipNotFoundException.class)
    public void shouldBeAbleToDeleteARelationship() {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("LOVES");

        actions.removeRelationship(representation.getStartNodeId(), representation.getType());
        GraphDbHelper.getRelationship(representation.getStartNodeId(), representation.getType());
    }

    @Test
    public void shouldBeAbleToRetrieveRelationshipsFromNode() {
        Object nodeId = GraphDbHelper.createNode();
        GraphDbHelper.createRelationship("LIKES", nodeId, GraphDbHelper.createNode());
        GraphDbHelper.createRelationship("LIKES", GraphDbHelper.createNode(), nodeId);
        GraphDbHelper.createRelationship("HATES", nodeId, GraphDbHelper.createNode());

        verifyRelReps(3, actions.retrieveRelationships(nodeId, RelationshipDirection.all));
        verifyRelReps(1, actions.retrieveRelationships(nodeId, RelationshipDirection.in));
        verifyRelReps(2, actions.retrieveRelationships(nodeId, RelationshipDirection.out));

        verifyRelReps(3, actions.retrieveRelationships(nodeId, RelationshipDirection.all, "LIKES", "HATES"));
        verifyRelReps(1, actions.retrieveRelationships(nodeId, RelationshipDirection.in, "LIKES", "HATES"));
        verifyRelReps(2, actions.retrieveRelationships(nodeId, RelationshipDirection.out, "LIKES", "HATES"));

        verifyRelReps(2, actions.retrieveRelationships(nodeId, RelationshipDirection.all, "LIKES"));
        verifyRelReps(1, actions.retrieveRelationships(nodeId, RelationshipDirection.in, "LIKES"));
        verifyRelReps(1, actions.retrieveRelationships(nodeId, RelationshipDirection.out, "LIKES"));

        verifyRelReps(1, actions.retrieveRelationships(nodeId, RelationshipDirection.all, "HATES"));
        verifyRelReps(0, actions.retrieveRelationships(nodeId, RelationshipDirection.in, "HATES"));
        verifyRelReps(1, actions.retrieveRelationships(nodeId, RelationshipDirection.out, "HATES"));
    }

    @Test
    public void shouldNotGetAnyRelationshipsWhenRetrievingFromNodeWithoutRelationships() {
        Object nodeId = GraphDbHelper.createNode();

        verifyRelReps(0, actions.retrieveRelationships(nodeId, RelationshipDirection.all));
        verifyRelReps(0, actions.retrieveRelationships(nodeId, RelationshipDirection.in));
        verifyRelReps(0, actions.retrieveRelationships(nodeId, RelationshipDirection.out));
    }

    @Test
    public void shouldBeAbleToSetRelationshipProperties() {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("KNOWS");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("foo", "bar");
        properties.put("number", 10);
        actions.setRelationshipProperties(representation.getStartNodeId(), representation.getType(), new PropertiesMap(properties));
        assertEquals(properties, GraphDbHelper.getRelationshipProperties(representation.getStartNodeId(), representation.getType()));
    }

    @Test
    public void shouldBeAbleToSetRelationshipProperty() {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("KNOWS");
        String key = "foo";
        Object value = "bar";
        actions.setRelationshipProperty(representation.getStartNodeId(), representation.getType(), key, value);
        assertEquals(Collections.singletonMap(key, value),
                GraphDbHelper.getRelationshipProperties(representation.getStartNodeId(), representation.getType()));
    }

    @Test
    public void shouldRemoveRelationProperties() {
        RelationshipRepresentation representation = GraphDbHelper.createRelationship("PAIR-PROGRAMS_WITH");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        map.put("baz", 22);
        GraphDbHelper.setRelationshipProperties(representation.getStartNodeId(), representation.getType(), map);

        actions.removeRelationshipProperties(representation.getStartNodeId(), representation.getType());

        assertTrue(GraphDbHelper.getRelationshipProperties(representation.getStartNodeId(), representation.getType()).isEmpty());
    }

    @Test
    public void shouldRemoveRelationshipProperty() {
        RelationshipRepresentation relId = GraphDbHelper.createRelationship("PAIR-PROGRAMS_WITH");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        map.put("baz", 22);
        GraphDbHelper.setRelationshipProperties(relId.getStartNodeId(), relId.getType(), map);

        assertTrue(actions.removeRelationshipProperty(relId.getStartNodeId(), relId.getType(), "foo"));
        assertEquals(1, GraphDbHelper.getRelationshipProperties(relId.getStartNodeId(), relId.getType()).size());
    }

    private void verifyRelReps(int expectedSize, List<RelationshipRepresentation> relreps) {
        assertEquals(expectedSize, relreps.size());
        for (RelationshipRepresentation relrep : relreps) {
            RelationshipRepresentationTest.verifySerialisation(relrep.serialize());
        }
    }

    private void assertHasProperties(Element container, Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            assertEquals(entry.getValue(), container.getProperty(entry.getKey()));
        }
    }

    private void assertSameProperties(Map<String, Object> expected, Map<String, Object> value) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), value.get(entry.getKey()));
        }
    }
}
