package com.tinkerpop.restling;

import com.tinkerpop.restling.domain.RelationshipRepresentation;

/**
 * @author Michael Hunger
 * @since 08.02.2010
 */
public class FunctionalHelper {
    public static final Long UNKNOWN_NODE = 9999999L;
    public static String relationshipUri(final RelationshipRepresentation representation) {
        return relationshipUri(representation.getStartNodeId(),representation.getType());
    }
    public static String relationshipUri(final Object nodeId, final String type) {
        return nodeUri(nodeId) +"/relationships/" + type;
    }

    public static String nodeUri(Object nodeId) {
        return WebServer.BASE_URI + nodeId;
    }

    public static String badUri() {
        return nodeUri(UNKNOWN_NODE);
    }
}
