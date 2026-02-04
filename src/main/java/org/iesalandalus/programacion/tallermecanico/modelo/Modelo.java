package org.iesalandalus.programacion.tallermecanico.modelo;

import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.TipoTrabajo;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Trabajo;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface Modelo {
    void comenzar();

    void terminar();

    void insertar(Cliente cliente) throws TallerMecanicoExcepcion;

    void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion;

    void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion;

    Cliente buscar(Cliente cliente);

    Vehiculo buscar(Vehiculo vehiculo);

    Trabajo buscar(Trabajo trabajo) throws TallerMecanicoExcepcion;

    Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion;

    Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion;

    Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion;

    Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion;

    void borrar(Cliente cliente) throws TallerMecanicoExcepcion;

    void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion;

    void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion;

    List<Cliente> getClientes();

    List<Vehiculo> getVehiculos();

    List<Trabajo> getTrabajos() throws TallerMecanicoExcepcion;

    List<Trabajo> getTrabajos(Cliente cliente) throws TallerMecanicoExcepcion;

    List<Trabajo> getTrabajos(Vehiculo vehiculo) throws TallerMecanicoExcepcion;

    Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) throws TallerMecanicoExcepcion;
}
