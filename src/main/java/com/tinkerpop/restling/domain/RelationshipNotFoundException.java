package com.tinkerpop.restling.domain;

@SuppressWarnings("serial")
public class RelationshipNotFoundException extends NotFoundException {

    public RelationshipNotFoundException(Object startNodeId, String label) {
        super("relationship from "+startNodeId+" with label "+label+" not found");
    }
}