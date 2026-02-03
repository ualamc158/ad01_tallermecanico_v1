package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


//Enlace Mongo DB Atlas
//https://account.mongodb.com/account/login?n=https%3A%2F%2Fcloud.mongodb.com%2Fv2%2F6981d5abbcd3ea36dd4aa661&nextHash=%23clusters&signedOut=true

public class MongoDB {
    private static final String SERVIDOR = "@cluster0.j8dda1e.mongodb.net/?retryWrites=true&w=majority";
    private static final int PUERTO = 27017;
    private static final String BD = "tallerMecanico";
    private static final String USUARIO = "taller";
    private static final String CONTRASENA = "taller-2025";

    private static MongoClient conexion;

    private MongoDB(){

    }

    public static MongoDatabase getBD() {
        if (conexion == null) {
            establecerConexion();
        }
        return conexion.getDatabase(BD);
    }

    private static void establecerConexion(){
        try {
            if (conexion == null) {
                String uri = "mongodb+srv://" + USUARIO + ":" + CONTRASENA + SERVIDOR;
                conexion = MongoClients.create(uri);
                System.out.println("Conexión a MongoDB establecida.");
            }
        } catch (Exception e) {
            System.err.println("Error al conectar con MongoDB: " + e.getMessage());
        }
    }

    private static void cerrarConexion(){
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                System.out.println("Conexión a MongoDB cerrada.");
            } catch (Exception e) {
                System.err.println("Error al cerrar la conexión con MongoDB: " + e.getMessage());
            }
        }
    }
}
