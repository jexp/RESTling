package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.ggm.Edge;
import com.tinkerpop.gremlin.models.ggm.Element;
import com.tinkerpop.gremlin.models.ggm.Graph;
import com.tinkerpop.gremlin.models.ggm.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.tinkerpop.restling.domain.DatabaseLocator.getGraphDatabase;
import static com.tinkerpop.restling.domain.DatabaseLocator.inTransaction;
import static com.tinkerpop.restling.domain.RelationshipRepresentation.represent;

public class GraphDbHelper {

    public static int getNumberOfNodes() {
        return numberOfEntitiesFor(Node.class);
    }

    public static int getNumberOfRelationships() {
        return numberOfEntitiesFor(Relationship.class);
    }

    private static int numberOfEntitiesFor(Class<?> type) {
        return (int) DatabaseLocator.getNeo().getConfig().getNeoModule().getNodeManager().getNumberOfIdsInUse(type);
    }

    public static Map<String, Object> getNodeProperties(final Object nodeId) {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<Map<String, Object>>() {
            public Map<String, Object> call() throws Exception {
                Vertex node = db.getVertex(nodeId);
                Map<String, Object> allProperties = new HashMap<String, Object>();
                for (String propertyKey : node.getPropertyKeys()) {
                    allProperties.put(propertyKey, node.getProperty(propertyKey));
                }
                return allProperties;
            }
        });
    }

    public static void setNodeProperties(final Object nodeId, final Map<String, Object> properties) {
        final Graph db = getGraphDatabase(null);
        inTransaction(new Runnable() {
            public void run() {
                Vertex node = db.getVertex(nodeId);
                for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
                    node.setProperty(propertyEntry.getKey(), propertyEntry.getValue());
                }
            }
        });
    }

    public static Long createNode() {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return longValue(db.addVertex(null).getId());
            }
        });
    }

    public static Long longValue(Object value) {
        if (value==null) return 0L;
        if (value instanceof Number) return ((Number)value).longValue();
        throw new IllegalArgumentException(value+" is not a Number");
    }

    public static RelationshipRepresentation createRelationship( final String type, final Object startNodeId, final Object endNodeId ) {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<RelationshipRepresentation>() {
            public RelationshipRepresentation call() throws Exception {
                Vertex startNode = db.getVertex( startNodeId );
                Vertex endNode = db.getVertex( endNodeId );
                Edge relationship = db.addEdge(null,startNode,endNode,type);
                return represent(null,relationship);
            }
        });
    }
    
    public static RelationshipRepresentation createRelationship( final String type )
    {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<RelationshipRepresentation>(){
            public RelationshipRepresentation call() throws Exception {
                Vertex startNode = db.addVertex(null);
                Vertex endNode = db.addVertex(null);
                Edge relationship = db.addEdge(null,startNode,endNode,type);
                return represent(null,relationship);
            }
        });
    }

    public static void setRelationshipProperties(final RelationshipRepresentation representation, final Map<String, Object> properties) {
        setRelationshipProperties(representation.getStartNodeId(),representation.getType(),properties);
    }
    public static void setRelationshipProperties(final Object startNodeId, final String label, final Map<String, Object> properties) {
        final Graph db = getGraphDatabase(null);
        inTransaction(new Runnable() {
            public void run() {
                final Edge edge = getRelationship(startNodeId, label);
                if (edge == null) return;
                for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
                    edge.setProperty(propertyEntry.getKey(), propertyEntry.getValue());
                }
            }
        });
    }
    
    public static Map<String, Object> getRelationshipProperties(final Object startNodeId, final String label) {
        Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<Map<String, Object>>() {
            public void run() {
            }

            @Override
            public Map<String, Object> call() throws Exception {
                final Edge edge = getRelationship(startNodeId, label);
                if (edge == null) return null;
                Map<String, Object> allProperties = new HashMap<String, Object>();
                for (String propertyKey : edge.getPropertyKeys()) {
                    allProperties.put(propertyKey, edge.getProperty(propertyKey));
                }
                return allProperties;
            }
        });
    }

    public static Edge getRelationship(final Object startNodeId, final String label) {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<Edge>() {
            public Edge call() throws Exception {
                final Vertex startNode = db.getVertex(startNodeId);
                for (Edge edge : startNode.getOutEdges()) {
                    if (edge.getLabel().equals(label)) return edge;
                }
                throw new RelationshipNotFoundException(startNodeId,label);
            }
        });
    }

    public static Vertex getNode(final Object id) {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<Vertex>() {
            public Vertex call() throws Exception {
                return db.getVertex(id);
            }
        });
    }
}
