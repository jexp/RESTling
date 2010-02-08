package com.tinkerpop.restling;

import com.tinkerpop.restling.WebServer;
import com.tinkerpop.restling.domain.RelationshipRepresentation;

/**
 * @author Michael Hunger
 * @since 08.02.2010
 */
public class FunctionalHelper {
    public static String relationshipUri(RelationshipRepresentation representation) {
        final Object nodeId = representation.getStartNodeId();
        return nodeUri(nodeId) +"/relationships/" + representation.getType();
    }

    public static String nodeUri(Object nodeId) {
        return WebServer.BASE_URI + nodeId;
    }
}
