package com.tinkerpop.restling.domain;

@SuppressWarnings("serial")
public class StartNodeSameAsEndNodeException extends Exception {

    public StartNodeSameAsEndNodeException(Object startNodeId, String label) {
        super("Start node "+startNodeId+" same as end node of relationship "+label);
    }
}
