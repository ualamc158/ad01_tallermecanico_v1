package org.iesalandalus.programacion.tallermecanico.modelo.negocio.json;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Vehiculos implements IVehiculos {
    private static final String FICHERO_VEHICULOS = "datos/ficheros/json/vehiculos.json";
    private static ObjectMapper mapper;
    private static Vehiculos instancia;

    static Vehiculos getInstancia() {
        if (instancia == null) {
            instancia = new Vehiculos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {

    }

    @Override
    public void terminar() {

    }

    public List<Vehiculo> leer() {
        File fichero = new File(FICHERO_VEHICULOS);
        List<Vehiculo> vehiculos = new ArrayList<>();

        // Si el fichero no existe, devolvemos el array vacío
        if (!fichero.exists()) {
            return vehiculos;
        }

        try {
            vehiculos = mapper.readValue(fichero, new TypeReference<List<Vehiculo>>() {
            });

        } catch (StreamReadException e) {
            System.out.println("Error leyendo el flujo de datos JSON: " + e.getMessage());
        } catch (DatabindException e) {
            System.out.println("Error mapeando el JSON a la clase Vehiculo: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida general: " + e.getMessage());
        }

        return vehiculos;
    }

    public void escribir(List<Vehiculo> vehiculos) {
        File fichero = new File(FICHERO_VEHICULOS);

        // Aseguramos que el directorio exista antes de escribir
        if (fichero.getParentFile() != null && !fichero.getParentFile().exists()) {
            fichero.getParentFile().mkdirs();
        }

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, vehiculos);

        } catch (StreamWriteException e) {
            System.out.println("Error al escribir el flujo de datos JSON: " + e.getMessage());
        } catch (DatabindException e) {
            System.out.println("Error al mapear la lista de vehiculos a JSON: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida general: " + e.getMessage());
        }
    }

    @Override
    public List<Vehiculo> get() {
        return leer();
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede insertar un vehículo nulo");
        }

        List<Vehiculo> vehiculos = leer();
        if (vehiculos.contains(vehiculo)) {
            throw new TallerMecanicoExcepcion("Ya existe un vehículo co esa matrícula.");
        }

        vehiculos.add(vehiculo);
        escribir(vehiculos);

    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede buscar un vehículo nulo");
        }

        List<Vehiculo> vehiculos = leer();

        int indice = vehiculos.indexOf(vehiculo);
        if (indice != -1) {
            return vehiculos.get(indice);
        }

        return null;
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede borrar un vehículo nulo");
        }

        List<Vehiculo> vehiculos = leer();

        if (!vehiculos.contains(vehiculo)){
            throw new TallerMecanicoExcepcion("El vehículo no existe");
        }

        vehiculos.remove(vehiculo);
        escribir(vehiculos);
    }
}
