package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.ggm.Edge;
import com.tinkerpop.gremlin.models.ggm.Element;
import com.tinkerpop.gremlin.models.ggm.Graph;
import com.tinkerpop.gremlin.models.ggm.Vertex;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.tinkerpop.restling.domain.DatabaseLocator.getGraphDatabase;
import static java.util.Arrays.asList;

public class StorageActions {

    private final URI baseUri;
    private final Graph graphdb;

    public StorageActions(final URI baseUri) {
        this.baseUri = baseUri;
        this.graphdb = DatabaseLocator.getGraphDatabase(baseUri);
    }

    public NodeRepresentation createNode(final PropertiesMap properties) {
        return inTransaction(new Callable<NodeRepresentation>() {

            public NodeRepresentation call() throws Exception {
                final Vertex node = graphdb.addVertex(null);
                properties.storeTo(node);
                return new NodeRepresentation(baseUri, node);
            }
        });
    }

    public void setNodeProperties(final Object nodeId, final PropertiesMap properties) {
        inTransaction(new Runnable() {

            public void run() {
                final Vertex node = getVertex(nodeId);
                deleteProperties(node);
                properties.storeTo(node);

            }
        });
    }

    private void deleteProperties(final Element container) {
        for (final String key : container.getPropertyKeys()) {
            container.removeProperty(key);
        }
    }

    public NodeRepresentation retrieveNode(final Object nodeId) {
        return inTransaction(new Callable<NodeRepresentation>() {

            public NodeRepresentation call() throws Exception {
                final Vertex node = getVertex(nodeId);
                return new NodeRepresentation(baseUri, node);
            }
        });
    }

    public PropertiesMap getNodeProperties(final Object nodeId) {
        return inTransaction(new Callable<PropertiesMap>() {
            public PropertiesMap call() throws Exception {
                return new PropertiesMap(getVertex(nodeId));
            }
        });
    }

    public void deleteNode(final Object nodeId) {
        inTransaction(new Runnable() {
            public void run() {
                final Vertex node = getVertex(nodeId);
                if (node.getOutEdges().iterator().hasNext()) throw new CascadingDeleteException(nodeId);
                if (node.getInEdges().iterator().hasNext()) throw new CascadingDeleteException(nodeId);
                graphdb.removeVertex(node);
            }
        });
    }

    public void setNodeProperty(final Object nodeId, final String key, final Object value) {
        inTransaction(new Runnable() {
            public void run() {
                final Vertex node = getVertex(nodeId);
                node.setProperty(key, value);
            }
        });
    }

    public Object getNodeProperty(final Object nodeId, final String key) {
        return inTransaction(new Callable<Object>() {
            public Object call() throws Exception {
                final Vertex node = getVertex(nodeId);
                return getProperty(node, key);
            }
        });
    }

    private Object getProperty(Element element, String key) {
        final Object result = element.getProperty(key);
        if (result==null) throw new PropertyValueException("property not found "+key+" for vertex "+ element);
        return result;
    }

    public RelationshipRepresentation createRelationship(final String type, final Object startNodeId, final Object endNodeId,
                                                         final PropertiesMap properties) throws StartNodeNotFoundException, EndNodeNotFoundException,
            StartNodeSameAsEndNodeException {
        if (endNodeId.equals(startNodeId)) throw new StartNodeSameAsEndNodeException(startNodeId, type);
        return inTransaction(new Callable<RelationshipRepresentation>() {
            public RelationshipRepresentation call() throws Exception {
                final Vertex startNode;
                try {
                    startNode = getVertex(startNodeId);
                } catch (NotFoundException e) {
                    throw new StartNodeNotFoundException(startNodeId);
                }
                Vertex endNode;
                try {
                    endNode = getVertex(endNodeId);
                } catch (NotFoundException e) {
                    throw new EndNodeNotFoundException(endNodeId);
                }
                final Edge relationship = graphdb.addEdge(null, startNode, endNode, type);
                properties.storeTo(relationship);
                final RelationshipRepresentation result = represent(relationship);
                return result;
            }
        });
    }

    public void removeNodeProperties(final Object nodeId) {
        inTransaction(new Runnable() {
            public void run() {
                final Vertex node = getVertex(nodeId);
                deleteProperties(node);
            }
        });
    }

    private Vertex getVertex(Object nodeId) {
        final Vertex vertex = graphdb.getVertex(nodeId);
        if (vertex==null) throw new NotFoundException("node not found "+nodeId);
        return vertex;
    }

    public boolean removeNodeProperty(final Object nodeId, final String key) {
        return inTransaction(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                final Vertex node = getVertex(nodeId);
                final boolean removed = node.removeProperty(key) != null;
                return removed;
            }
        });
    }

    public RelationshipRepresentation retrieveRelationship(final Object startNodeId, final String label) {
        return inTransaction(new Callable<RelationshipRepresentation>() {
            public RelationshipRepresentation call() throws Exception {
                final Edge relationship = getEdge(startNodeId,label);
                return represent(relationship);
            }
        });
    }

    public PropertiesMap getRelationshipProperties(final Object startNodeId, final String label) {
        return inTransaction(new Callable<PropertiesMap>() {
            public PropertiesMap call() throws Exception {
                return new PropertiesMap(getEdge(startNodeId,label));
            }
        });
    }

    public Object getRelationshipProperty(final Object startNodeId, final String label, final String key) {
        return inTransaction(new Callable<Object>() {
            public Object call() throws Exception {
                final Edge relationship = getEdge(startNodeId,label);
                return getProperty(relationship,key);
            }
        });
    }

    public void removeRelationship(final Object startNodeId, final String label) {
        inTransaction(new Runnable() {
            public void run() {
                final Edge relationship = getEdge(startNodeId,label);
                graphdb.removeEdge(relationship);

            }
        });
    }

    public List<RelationshipRepresentation> retrieveRelationships(final Object nodeId, final RelationshipDirection direction,
                                                                  final String...labels) {
        return retrieveRelationships(nodeId,direction,asList(labels));

    }
    public List<RelationshipRepresentation> retrieveRelationships(final Object nodeId, final RelationshipDirection direction,
                                                                  final List<String> labels) {
        return inTransaction(new Callable<List<RelationshipRepresentation>>() {
            public List<RelationshipRepresentation> call() throws Exception {
                final Vertex node = getVertex(nodeId);
                final List<RelationshipRepresentation> result = new LinkedList<RelationshipRepresentation>();
                if (direction == null || direction==RelationshipDirection.all || direction == RelationshipDirection.in)
                    for (final Edge edge : node.getInEdges()) {
                        if (labels.isEmpty() || labels.contains(edge.getLabel())) result.add(represent(edge));
                    }
                if (direction == null || direction==RelationshipDirection.all || direction == RelationshipDirection.out) {
                    for (final Edge edge : node.getOutEdges()) {
                        if (labels.isEmpty() || labels.contains(edge.getLabel())) result.add(represent(edge));
                    }
                }
                return result;
            }
        });
    }

    private RelationshipRepresentation represent(final Edge edge) {
        return new RelationshipRepresentation(baseUri, edge);
    }

    public void setRelationshipProperties(final Object startNodeId, final String label, final PropertiesMap properties) {
        inTransaction(new Runnable() {
            public void run() {
                final Edge relationship = getEdge(startNodeId,label);
                deleteProperties(relationship);
                properties.storeTo(relationship);
            }
        });
    }

    private Edge getEdge(final Object startNodeId, final String label) {
        final Vertex vertex = getVertex(startNodeId);
        if (vertex==null) throw new NotFoundException("Unknown node "+startNodeId);
        for (Edge edge : vertex.getOutEdges()) {
            if (edge.getLabel().equals(label)) return edge;
        }
        throw new NotFoundException("No relationship from " + startNodeId + " with label " + label);
    }

    public void setRelationshipProperty(final Object startNodeId, final String label, final String key, final Object value) {
        inTransaction(new Runnable() {
            public void run() {
                final Edge relationship = getEdge(startNodeId,label);
                relationship.setProperty(key, value);

            }
        });
    }

    public void removeRelationshipProperties(final Object startNodeId, final String label) {
        inTransaction(new Runnable() {
            public void run() {
                final Edge relationship = getEdge(startNodeId,label);
                deleteProperties(relationship);

            }
        });
    }

    public boolean removeRelationshipProperty(final Object startNodeId, final String label, final String propertyKey) {
        return inTransaction(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                final Edge relationship = getEdge(startNodeId,label);
                return relationship.removeProperty(propertyKey) != null;
            }
        });
    }


    public <T> T inTransaction(final Callable<T> call) {
        return DatabaseLocator.inTransaction(call);
    }

    public void inTransaction(final Runnable run) {
        inTransaction(new Callable<Void>() {

            public Void call() throws Exception {
                run.run();
                return null;
            }
        });
    }

    public Edge getRelationship(final Object startNodeId, final String label) {
        final Graph db = getGraphDatabase(null);
        return inTransaction(new Callable<Edge>() {
            public Edge call() throws Exception {
                final Vertex startNode = db.getVertex(startNodeId);
                for (Edge edge : startNode.getOutEdges()) {
                    if (edge.getLabel().equals(label)) return edge;
                }
                return null;
            }
        });
    }

}
