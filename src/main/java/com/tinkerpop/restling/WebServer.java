package com.tinkerpop.restling;

import java.util.HashMap;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

public enum WebServer {
	INSTANCE;

    public static final String BASE_URI = "http://localhost:9999/";

    private SelectorThread threadSelector;

    public void startServer() {
        final HashMap<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "com.tinkerpop.restling.web");

        try {
			threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    public void stopServer() {
        threadSelector.stopEndpoint();
    }
}
