package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;

public class Clientes implements IClientes {

    private static Clientes instancia;
    private Connection conexion;

    private Clientes() {}

    public static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        this.conexion = MySQL.establecerConexion();
    }

    @Override
    public void terminar() {
        MySQL.cerrarConexion();
    }

    @Override
    public List<Cliente> get() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT nombre, dni, telefono FROM clientes";

        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clientes.add(new Cliente(rs.getString("nombre"), rs.getString("dni"), rs.getString("telefono")));
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al recuperar clientes: " + e.getMessage());
        }
        return clientes;
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede insertar un cliente nulo.");
        }

        String sql = "INSERT INTO clientes (nombre, dni, telefono) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getDni());
            stmt.setString(3, cliente.getTelefono());
            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new TallerMecanicoExcepcion("El cliente ya existe.");
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al insertar cliente: " + e.getMessage());
        }
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede modificar un cliente nulo.");
        }

        if (buscar(cliente) == null) {
            throw new TallerMecanicoExcepcion("El cliente no existe.");
        }

        String sql = "UPDATE clientes SET nombre = ?, telefono = ? WHERE dni = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            String nuevoNombre = (nombre != null) ? nombre : cliente.getNombre();
            String nuevoTelefono = (telefono != null) ? telefono : cliente.getTelefono();

            stmt.setString(1, nuevoNombre);
            stmt.setString(2, nuevoTelefono);
            stmt.setString(3, cliente.getDni());
            stmt.executeUpdate();

            return new Cliente(nuevoNombre, cliente.getDni(), nuevoTelefono);
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al modificar cliente: " + e.getMessage());
        }
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        if (cliente == null) return null;

        String sql = "SELECT nombre, dni, telefono FROM clientes WHERE dni = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, cliente.getDni());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(rs.getString("nombre"), rs.getString("dni"), rs.getString("telefono"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede borrar un cliente nulo.");
        }

        String sqlCheck = "SELECT count(*) FROM trabajos WHERE cliente_dni = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlCheck)) {
            stmt.setString(1, cliente.getDni());
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new TallerMecanicoExcepcion("No se puede borrar el cliente porque tiene trabajos asociados.");
            }
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al verificar trabajos del cliente.");
        }

        String sql = "DELETE FROM clientes WHERE dni = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, cliente.getDni());
            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new TallerMecanicoExcepcion("El cliente a borrar no existe.");
            }
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al borrar cliente: " + e.getMessage());
        }
    }
}