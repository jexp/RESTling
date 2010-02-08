package com.tinkerpop.restling;

import java.net.URI;

import com.tinkerpop.restling.domain.DatabaseLocator;


public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(String.format("Running server at [%s]", BASE_URI));
        WebServer.INSTANCE.startServer();
        System.out.println("Press a key to kill the server");
        System.in.read();
        WebServer.INSTANCE.stopServer();
        DatabaseLocator.shutdownGraphDatabase(new URI(BASE_URI));
        System.exit( 0 );
    }
    
    public static final String BASE_URI = "http://localhost:9999/";
}
