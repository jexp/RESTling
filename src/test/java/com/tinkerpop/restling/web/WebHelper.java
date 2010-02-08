package com.tinkerpop.restling.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.tinkerpop.restling.domain.GraphDbHelper;

public class WebHelper {
    private final URI baseUri;

    public WebHelper(URI baseUri) {
        this.baseUri = baseUri;
        
    }
    
    public URI createNode() {
        Object nodeId = GraphDbHelper.createNode();
        try {
            return new URI(baseUri.toString() + "/" + nodeId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public URI createNodeWithProperties(Map<String, Object> props) {
        URI nodeUri = createNode();
        setNodeProperties(nodeUri, props);
        return nodeUri;
    }
    
    private void setNodeProperties(URI nodeUri, Map<String, Object> props) {
        GraphDbHelper.setNodeProperties(extractNodeId(nodeUri), props);
    }
    
    private Object extractNodeId(URI nodeUri) {
        String path = nodeUri.getPath();
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        
        return Long.parseLong(path);
    }
}
