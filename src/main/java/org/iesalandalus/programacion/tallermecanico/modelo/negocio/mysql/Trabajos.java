package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mysql;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;

public class Trabajos implements ITrabajos {

    private static Trabajos instancia;
    private Connection conexion;

    private Trabajos() {}

    public static Trabajos getInstancia() {
        if (instancia == null) instancia = new Trabajos();
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
    public List<Trabajo> get() {
        List<Trabajo> trabajos = new ArrayList<>();
        String sql = "SELECT * FROM trabajos";
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                trabajos.add(mapearTrabajo(rs));
            }
        } catch (Exception e) {
            System.err.println("Error al recuperar trabajos: " + e.getMessage());
        }
        return trabajos;
    }

    private Trabajo mapearTrabajo(ResultSet rs) throws SQLException {
        String dni = rs.getString("cliente_dni");
        String matricula = rs.getString("vehiculo_matricula");

        Cliente c = Clientes.getInstancia().buscar(new Cliente("Dummy", dni, "000000000"));
        Vehiculo v = Vehiculos.getInstancia().buscar(new Vehiculo("Marca", "Modelo", matricula));

        LocalDate fechaInicio = rs.getDate("fecha_inicio").toLocalDate();
        Date fechaFinSql = rs.getDate("fecha_fin");
        LocalDate fechaFin = (fechaFinSql != null) ? fechaFinSql.toLocalDate() : null;
        int horas = rs.getInt("horas");
        float precioMaterial = rs.getFloat("precio_material");
        String tipo = rs.getString("tipo");

        Trabajo t = null;
        if ("Mecanico".equalsIgnoreCase(tipo)) {
            Mecanico m = new Mecanico(c, v, fechaInicio);
            try {
                if (precioMaterial > 0) m.anadirPrecioMaterial(precioMaterial);
                if (horas > 0) m.anadirHoras(horas);
                if (fechaFin != null) m.cerrar(fechaFin);
            } catch (TallerMecanicoExcepcion e) { /* Ignorar */ }
            t = m;
        } else {
            Revision r = new Revision(c, v, fechaInicio);
            try {
                if (horas > 0) r.anadirHoras(horas);
                if (fechaFin != null) r.cerrar(fechaFin);
            } catch (TallerMecanicoExcepcion e) { /* Ignorar */ }
            t = r;
        }
        return t;
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        List<Trabajo> trabajos = new ArrayList<>();
        String sql = "SELECT * FROM trabajos WHERE cliente_dni = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, cliente.getDni());
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) trabajos.add(mapearTrabajo(rs));
        } catch (Exception e) { System.err.println(e.getMessage()); }
        return trabajos;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        List<Trabajo> trabajos = new ArrayList<>();
        String sql = "SELECT * FROM trabajos WHERE vehiculo_matricula = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, vehiculo.matricula());
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) trabajos.add(mapearTrabajo(rs));
        } catch (Exception e) { System.err.println(e.getMessage()); }
        return trabajos;
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        if (trabajo == null) throw new NullPointerException("Trabajo nulo.");

        String sql = "INSERT INTO trabajos (cliente_dni, vehiculo_matricula, fecha_inicio, tipo, descripcion, horas, precio_material, fecha_fin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, trabajo.getCliente().getDni());
            stmt.setString(2, trabajo.getVehiculo().matricula());
            stmt.setDate(3, Date.valueOf(trabajo.getFechaInicio()));

            String tipo = (trabajo instanceof Mecanico) ? "Mecanico" : "Revision";
            stmt.setString(4, tipo);
            stmt.setString(5, "");
            stmt.setInt(6, trabajo.getHoras());

            if (trabajo instanceof Mecanico) {
                stmt.setFloat(7, ((Mecanico) trabajo).getPrecioMaterial());
            } else {
                stmt.setFloat(7, 0);
            }

            stmt.setDate(8, trabajo.getFechaFin() != null ? Date.valueOf(trabajo.getFechaFin()) : null);

            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new TallerMecanicoExcepcion("El trabajo ya existe.");
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al insertar trabajo: " + e.getMessage());
        }
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        if (trabajo == null) return null;
        String sql = "SELECT * FROM trabajos WHERE cliente_dni=? AND vehiculo_matricula=? AND fecha_inicio=?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, trabajo.getCliente().getDni());
            stmt.setString(2, trabajo.getVehiculo().matricula());
            stmt.setDate(3, Date.valueOf(trabajo.getFechaInicio()));
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearTrabajo(rs);
            }
        } catch (Exception e) { System.err.println(e.getMessage()); }
        return null;
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        if (trabajo == null) throw new NullPointerException("Trabajo nulo.");
        String sql = "DELETE FROM trabajos WHERE cliente_dni=? AND vehiculo_matricula=? AND fecha_inicio=?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, trabajo.getCliente().getDni());
            stmt.setString(2, trabajo.getVehiculo().matricula());
            stmt.setDate(3, Date.valueOf(trabajo.getFechaInicio()));
            if (stmt.executeUpdate() == 0) throw new TallerMecanicoExcepcion("El trabajo no existe.");
        } catch (SQLException e) { throw new TallerMecanicoExcepcion(e.getMessage()); }
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        trabajo.anadirHoras(horas);
        actualizarTrabajo(trabajo);
        return trabajo;
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precio) throws TallerMecanicoExcepcion {
        if (trabajo instanceof Mecanico) {
            ((Mecanico) trabajo).anadirPrecioMaterial(precio);
            actualizarTrabajo(trabajo);
        }
        return trabajo;
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        trabajo.cerrar(fechaFin);
        actualizarTrabajo(trabajo);
        return trabajo;
    }

    private void actualizarTrabajo(Trabajo trabajo) throws TallerMecanicoExcepcion {
        String sql = "UPDATE trabajos SET horas=?, precio_material=?, fecha_fin=? WHERE cliente_dni=? AND vehiculo_matricula=? AND fecha_inicio=?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, trabajo.getHoras());
            stmt.setFloat(2, (trabajo instanceof Mecanico) ? ((Mecanico) trabajo).getPrecioMaterial() : 0);
            stmt.setDate(3, trabajo.getFechaFin() != null ? Date.valueOf(trabajo.getFechaFin()) : null);
            stmt.setString(4, trabajo.getCliente().getDni());
            stmt.setString(5, trabajo.getVehiculo().matricula());
            stmt.setDate(6, Date.valueOf(trabajo.getFechaInicio()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new TallerMecanicoExcepcion("Error al actualizar trabajo: " + e.getMessage());
        }
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        Map<TipoTrabajo, Integer> estadisticas = new HashMap<>();
        estadisticas.put(TipoTrabajo.MECANICO, 0);
        estadisticas.put(TipoTrabajo.REVISION, 0);

        String sql = "SELECT tipo, COUNT(*) as cantidad FROM trabajos WHERE MONTH(fecha_inicio) = ? AND YEAR(fecha_inicio) = ? GROUP BY tipo";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, mes.getMonthValue());
            stmt.setInt(2, mes.getYear());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tipoStr = rs.getString("tipo");
                if ("Mecanico".equalsIgnoreCase(tipoStr)) {
                    estadisticas.put(TipoTrabajo.MECANICO, rs.getInt("cantidad"));
                } else if ("Revision".equalsIgnoreCase(tipoStr)) {
                    estadisticas.put(TipoTrabajo.REVISION, rs.getInt("cantidad"));
                }
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        return estadisticas;
    }
}