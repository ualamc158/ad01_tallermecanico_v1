package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;

public class Vehiculos implements IVehiculos {

    private static Vehiculos instancia;
    private Connection conexion;

    // Cambia la línea de la URL por esta:
    private static final String URL = "jdbc:mysql://3.235.161.27:3306/tallermecanico";
    private static final String USER = "root";
    private static final String PASS = "amcciclista";

    private Vehiculos() {}

    public static Vehiculos getInstancia() {
        if (instancia == null) instancia = new Vehiculos();
        return instancia;
    }

    @Override
    public void comenzar() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Conexión MySQL (Vehículos) establecida.");
            }
        } catch (SQLException e) {
            System.err.println("Error conexión MySQL Vehículos: " + e.getMessage());
        }
    }

    @Override
    public void terminar() {
        try {
            if (conexion != null && !conexion.isClosed()) conexion.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Vehiculo> get() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT marca, modelo, matricula FROM vehiculos";
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                vehiculos.add(new Vehiculo(rs.getString("marca"), rs.getString("modelo"), rs.getString("matricula")));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener vehículos: " + e.getMessage());
        }
        return vehiculos;
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) throw new NullPointerException("No se puede insertar un vehículo nulo.");

        String sql = "INSERT INTO vehiculos (marca, modelo, matricula) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            // CORRECCIÓN: Uso de accesores de record (sin get)
            stmt.setString(1, vehiculo.marca());
            stmt.setString(2, vehiculo.modelo());
            stmt.setString(3, vehiculo.matricula());
            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new TallerMecanicoExcepcion("El vehículo ya existe.");
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al insertar vehículo: " + e.getMessage());
        }
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        if (vehiculo == null) return null;

        String sql = "SELECT marca, modelo, matricula FROM vehiculos WHERE matricula = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            // CORRECCIÓN: Uso de accesor de record (sin get)
            stmt.setString(1, vehiculo.matricula());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return new Vehiculo(rs.getString("marca"), rs.getString("modelo"), rs.getString("matricula"));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) throw new NullPointerException("No se puede borrar un vehículo nulo.");

        String sqlCheck = "SELECT count(*) FROM trabajos WHERE vehiculo_matricula = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlCheck)) {
            // CORRECCIÓN: Uso de accesor de record (sin get)
            stmt.setString(1, vehiculo.matricula());
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new TallerMecanicoExcepcion("No se puede borrar el vehículo porque tiene trabajos asociados.");
            }
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al verificar trabajos del vehículo.");
        }

        String sql = "DELETE FROM vehiculos WHERE matricula = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            // CORRECCIÓN: Uso de accesor de record (sin get)
            stmt.setString(1, vehiculo.matricula());
            if (stmt.executeUpdate() == 0) throw new TallerMecanicoExcepcion("El vehículo a borrar no existe.");
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al borrar vehículo: " + e.getMessage());
        }
    }
}