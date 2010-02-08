package com.tinkerpop.restling.domain;

@SuppressWarnings("serial")
public class CascadingDeleteException extends RuntimeException {

    public CascadingDeleteException(Object nodeId) {
        super("Node "+nodeId+" has still dependencies");
    }
}