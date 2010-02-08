package com.tinkerpop.restling.domain;

import org.neo4j.graphdb.Direction;

public enum RelationshipDirection {
    all(Direction.BOTH), in(Direction.INCOMING), out(Direction.OUTGOING);
    final Direction internal;

    private RelationshipDirection(Direction internal) {
        this.internal = internal;

    }
}
