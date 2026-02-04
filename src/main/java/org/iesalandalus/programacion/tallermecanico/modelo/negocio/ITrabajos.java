package org.iesalandalus.programacion.tallermecanico.modelo.negocio;

import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ITrabajos {
    void comenzar();

    void terminar();

    List<Trabajo> get() throws TallerMecanicoExcepcion;

    List<Trabajo> get(Cliente cliente) throws TallerMecanicoExcepcion;

    List<Trabajo> get(Vehiculo vehiculo) throws TallerMecanicoExcepcion;

    Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) throws TallerMecanicoExcepcion;

    void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion;

    Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion;

    Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion;

    Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion;

    Trabajo buscar(Trabajo trabajo) throws TallerMecanicoExcepcion;

    void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion;
}
