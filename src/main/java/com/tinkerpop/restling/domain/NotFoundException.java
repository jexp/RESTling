package com.tinkerpop.restling.domain;

@SuppressWarnings("serial")
public class NotFoundException extends RuntimeException {

    public NotFoundException(String s) {
        super(s);
    }
}