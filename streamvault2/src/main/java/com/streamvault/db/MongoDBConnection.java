package com.streamvault.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDBConnection — provides access to the MongoDB streamvault database.
 * Used ONLY by AdminAnalyticsServlet for Phase 3 aggregation pipelines.
 */
public class MongoDBConnection {

    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DB_NAME   = "streamvault";   // same logical name

    private static MongoClient client;

    public static synchronized MongoDatabase getDatabase() {
        if (client == null) {
            client = MongoClients.create(MONGO_URI);
        }
        return client.getDatabase(DB_NAME);
    }

    public static void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
