package com.tinkerpop.restling.domain;

@SuppressWarnings("serial")
public class EndNodeNotFoundException extends NotFoundException {

    public EndNodeNotFoundException(Object nodeId) {
        super("end node not found "+nodeId);
    }
}
