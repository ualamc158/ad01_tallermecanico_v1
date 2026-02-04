package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoClientConnectionExample {
    public static void main(String[] args) {
        // Si recibes errores SSL, asegúrate de que tu IP está en la Whitelist de MongoDB Atlas.
        // Network Access -> Add IP Address -> Add Current IP Address
        
        String connectionString = "mongodb+srv://taller:taller-2025@cluster0.j8dda1e.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
