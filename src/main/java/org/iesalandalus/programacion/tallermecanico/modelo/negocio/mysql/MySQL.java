package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

    private static final String URL = "jdbc:mysql://44.202.190.169:3306/tallermecanico";
    private static final String USER = "root";
    private static final String PASS = "amcciclista";

    private static Connection conexion;

    private MySQL() {}

    public static Connection establecerConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Conexión a MySQL establecida correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al establecer la conexión con MySQL: " + e.getMessage());
        }
        return conexion;
    }

    public static Connection getConexion() {
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión a MySQL cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión con MySQL: " + e.getMessage());
        }
    }
}