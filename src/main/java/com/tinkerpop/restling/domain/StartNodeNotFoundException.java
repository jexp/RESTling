package com.tinkerpop.restling.domain;

@SuppressWarnings("serial")
public class StartNodeNotFoundException extends NotFoundException {

    public StartNodeNotFoundException(Object nodeId) {
        super("start node not found " +nodeId);
    }
}
