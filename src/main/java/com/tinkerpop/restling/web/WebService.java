package com.tinkerpop.restling.web;

import com.tinkerpop.restling.domain.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/")
public class WebService {

    private final StorageActions actions;

    public WebService(@Context UriInfo uriInfo) {
        this.actions = new StorageActions(uriInfo.getBaseUri());
    }

    /**
     * This is an awful hack. Because Jersey can't seem to understand that empty
     * POSTs should have no media type (nor can any client really), it assumes
     * form URL encoded.
     *
     * If we ever have to accept form URL encoded representations here, we'll
     * have to explicitly look in the content for them and *not* reject the
     * request just because the string parameter is not null or empty. Grrr.
     *
     * @param body
     *            should be empty, this exists just so we can 400 on a request
     *            that isn't empty. Null is a good value here if you're binding
     *            locally.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEmptyNode(String body) {
        if (!isNullOrEmpty(body)) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        NodeRepresentation noderep = actions.createNode(new PropertiesMap(Collections.<String, Object> emptyMap()));
        return addContentLengthHeader(Response.created(noderep.selfUri()).entity(JsonHelper.createJsonFrom(noderep.serialize()))).build();
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.equals("");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNode(String json) {
        PropertiesMap properties;
        try {
            properties = new PropertiesMap(JsonHelper.jsonToMap(json));
        } catch (PropertyValueException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        NodeRepresentation noderep = actions.createNode(properties);
        return addContentLengthHeader(
                Response.created(noderep.selfUri()).entity(JsonHelper.createJsonFrom(noderep.serialize())).header(HttpHeaders.CONTENT_LENGTH,
                        String.valueOf(JsonHelper.createJsonFrom(noderep.serialize()).length()))).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{nodeId}")
    public Response getNode(@PathParam("nodeId") Object nodeId) {
        NodeRepresentation noderep;
        try {
            noderep = actions.retrieveNode(nodeId);
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return addContentLengthHeader(Response.ok(noderep.selfUri()).entity(JsonHelper.createJsonFrom(noderep.serialize()))).build();
    }

    @PUT
    @Path("{nodeId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNodeProperties(@PathParam("nodeId") Object nodeId, String json) {
        PropertiesMap properties;
        try {
            properties = new PropertiesMap(JsonHelper.jsonToMap(json));
        } catch (PropertyValueException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        try {
            actions.setNodeProperties(nodeId, properties);
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{nodeId}/properties")
    public Response getNodeProperties(@PathParam("nodeId") Object nodeId) {
        try {
            PropertiesMap properties = actions.getNodeProperties(nodeId);
            if (properties.isEmpty()) {
                return Response.noContent().build();
            }
            return addContentLengthHeader(Response.ok(JsonHelper.createJsonFrom(properties.serialize()))).build();

        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{nodeId}")
    public Response deleteNode(@PathParam("nodeId") Object id) {
        try {
            actions.deleteNode(id);
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (CascadingDeleteException e) {
            return Response.status(Status.CONFLICT).build();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("{nodeId}/properties/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNodeProperty(@PathParam("nodeId") Object id, @PathParam("key") String key, String json) {
        try {
            actions.setNodeProperty(id, key, JsonHelper.jsonToSingleValue(json));
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (PropertyValueException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("{nodeId}/properties/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodeProperty(@PathParam("nodeId") Object nodeId, @PathParam("key") String key) {
        try {
            Object value = actions.getNodeProperty(nodeId, key);
            return Response.ok(JsonHelper.createJsonFrom(value)).build();
        } catch (PropertyValueException e) {
            return Response.status(Status.NO_CONTENT).build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{nodeId}/properties")
    public Response removeNodeProperties(@PathParam("nodeId") Object nodeId) {
        try {
            actions.removeNodeProperties(nodeId);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{nodeId}/relationships")
    public Response createRelationship(@PathParam("nodeId") Object startNodeId, String json) {
        Long endNodeId;
        String type;
        PropertiesMap properties;
        try {
            Map<String, Object> payload = JsonHelper.jsonToMap(json);
            endNodeId = getNodeIdFromUri((String) payload.get("to"));
            type = ((String) payload.get("type")).toString();
            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) payload.get("properties");
            if (props != null) {
                properties = new PropertiesMap(props);
            } else {
                properties = new PropertiesMap(Collections.<String, Object> emptyMap());
            }
        } catch (PropertyValueException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        RelationshipRepresentation relationship;
        try {
            relationship = actions.createRelationship(type, startNodeId, endNodeId, properties);
        } catch (StartNodeNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (EndNodeNotFoundException e) {
            return Response.status(Status.BAD_REQUEST).build();
        } catch (StartNodeSameAsEndNodeException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        return addContentLengthHeader(Response.created(relationship.selfUri()).entity(JsonHelper.createJsonFrom(relationship.serialize()))).build();
    }

    @DELETE
    @Path("{nodeId}/properties/{key}")
    public Response removeNodeProperty(@PathParam("nodeId") Object nodeId, @PathParam("key") String key) {
        try {
            boolean removed = actions.removeNodeProperty(nodeId, key);
            return removed ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{nodeId}/relationships/{label}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationship(@PathParam("nodeId") Long nodeId,@PathParam("label") String label) {
        RelationshipRepresentation relrep;
        try {
            relrep = actions.retrieveRelationship(nodeId,label);
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return addContentLengthHeader(Response.ok().entity(JsonHelper.createJsonFrom(relrep.serialize()))).build();
    }

    @GET
    @Path("{nodeId}/relationships/{label}/properties")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationshipProperties(@PathParam("nodeId") Long nodeId,@PathParam("label") String label) {
        try {
            PropertiesMap properties = actions.getRelationshipProperties(nodeId,label);
            if (properties.isEmpty()) {
                return Response.noContent().build();
            }
            return addContentLengthHeader(Response.ok(JsonHelper.createJsonFrom(properties.serialize()))).build();

        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{nodeId}/relationships/{label}/properties/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationshipProperty(@PathParam("nodeId") Long nodeId,@PathParam("label") String label, @PathParam("key") String key) {
        try {
            Object value = actions.getRelationshipProperty(nodeId,label, key);
            return addContentLengthHeader(Response.ok().entity(JsonHelper.createJsonFrom(value))).build();
        } catch (PropertyValueException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    private Long getNodeIdFromUri(String uri) {
        return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
    }

    private ResponseBuilder addContentLengthHeader(ResponseBuilder responseBuilder) {
        return responseBuilder.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(responseBuilder.clone().build().getEntity().toString().length()));
    }

    @DELETE
    @Path("{nodeId}/relationships/{label}")
    public Response removeRelationship(@PathParam("nodeId") Long nodeId,@PathParam("label") String label) {
        try {
            actions.removeRelationship(nodeId,label);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{nodeId}/relationships/{dir}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationships(@PathParam("nodeId") Long nodeId, @PathParam("dir") RelationshipDirection direction) {
        return getRelationships(nodeId, direction, new AmpersandSeparatedList());
    }

    @GET
    @Path("{nodeId}/relationships/{dir}/{types}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationships(@PathParam("nodeId") Long nodeId, @PathParam("dir") RelationshipDirection direction,
            @PathParam("types") AmpersandSeparatedList types) {
        List<RelationshipRepresentation> relreps;
        try {
            relreps = actions.retrieveRelationships(nodeId, direction, types);
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String json = JsonHelper.createJsonFromList(relreps);
        return addContentLengthHeader(Response.ok(json, MediaType.APPLICATION_JSON)).build();
    }

    @PUT
    @Path("{nodeId}/relationships/{label}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRelationshipProperties(@PathParam("nodeId") Long nodeId,@PathParam("label") String label, String json) {
        PropertiesMap properties = null;
        try {
            properties = new PropertiesMap(JsonHelper.jsonToMap(json));
        } catch (PropertyValueException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            actions.setRelationshipProperties(nodeId,label, properties);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("{nodeId}/relationships/{label}/properties/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRelationshipProperty(@PathParam("nodeId") Long nodeId,@PathParam("label") String label, @PathParam("key") String key, String json) {
        try {
            actions.setRelationshipProperty(nodeId,label, key, JsonHelper.jsonToSingleValue(json));
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (PropertyValueException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("{nodeId}/relationships/{label}/properties")
    public Response removeRelationshipProperties(@PathParam("nodeId") Long nodeId,@PathParam("label") String label) {
        try {
            actions.removeRelationshipProperties(nodeId,label);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{nodeId}/relationships/{label}/properties/{key}")
    public Response removeRelationshipProperty(@PathParam("nodeId") Long nodeId,@PathParam("label") String label, @PathParam("key") String propertyKey) {
        try {
            if (actions.removeRelationshipProperty(nodeId,label, propertyKey)) {
                return Response.ok().build();
            } else {
                return Response.status(Status.NOT_FOUND).build();
            }
        } catch (NotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
