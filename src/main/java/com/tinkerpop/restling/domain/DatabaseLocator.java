package com.tinkerpop.restling.domain;

import com.tinkerpop.gremlin.models.pgm.Graph;
import com.tinkerpop.gremlin.models.pgm.impls.neo4j.Neo4jGraph;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.net.URI;
import java.util.concurrent.Callable;

public class DatabaseLocator {

    private static Neo4jGraph db;
    private static EmbeddedGraphDatabase embeddedGraphDatabase;

    public static Graph getGraphDatabase(URI baseUri) {
        // TODO: this is just a KISS implementation
        if (db == null) {
            embeddedGraphDatabase = new EmbeddedGraphDatabase("target/neodb");
            db = new Neo4jGraph(embeddedGraphDatabase);
        }
        return db;
    }

    public static void shutdownGraphDatabase(URI baseUri) {
        // TODO: this is just a KISS implementation
        if (db != null) {
            db.shutdown();
            db = null;
        }
    }

    public static <T> T inTransaction(Callable<T> call) {
        db.startTransaction();
        try {
            final T result = call.call();
            db.stopTransaction(true);
            return result;
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            db.stopTransaction(false);
            throw e;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            db.stopTransaction(false);
            throw new RuntimeException(e);
        }
    }

    public static void inTransaction(final Runnable run) {
        inTransaction(new Callable<Void>() {
            public Void call() throws Exception {
                run.run();
                return null;
            }
        });
    }

    public static EmbeddedGraphDatabase getNeo() {
        return embeddedGraphDatabase;
    }
}
