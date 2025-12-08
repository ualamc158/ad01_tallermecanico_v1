package org.iesalandalus.programacion.tallermecanico.modelo.negocio.json;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class Trabajos implements ITrabajos {
    private static final String FICHERO_TRABAJOS = "datos/ficheros/json/trabajos.json";
    private static ObjectMapper mapper;
    private static final TypeReference<List<Trabajo>> TYPE_LIST_TRABAJO = new TypeReference<>() {};
    private static Trabajos instancia;

    private Trabajos(){
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        File fichero = new File(FICHERO_TRABAJOS);
        File carpeta = fichero.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
    }


    static Trabajos getInstancia() {
        if (instancia == null) {
            instancia = new Trabajos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {

    }

    @Override
    public void terminar() {

    }

    public List<Trabajo> leer(){
        File fichero = new File(FICHERO_TRABAJOS);
        List<Trabajo> trabajos = new ArrayList<>();

        if (!fichero.exists()) {
            return trabajos;
        }

        try {
            trabajos = mapper.readValue(fichero, TYPE_LIST_TRABAJO);

        } catch (StreamReadException e) {
            System.out.println("Error leyendo el flujo de datos JSON: " + e.getMessage());
        } catch (DatabindException e) {
            System.out.println("Error mapeando el JSON a la clase Trabajo: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida general: " + e.getMessage());
        }

        return trabajos;
    }

    public void escribir(List<Trabajo> trabajos) {
        Objects.requireNonNull(trabajos, "La lista de trabajos a escribir no puede ser nula.");
        File fichero = new File(FICHERO_TRABAJOS);

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, trabajos);
        } catch (StreamWriteException e) {
            System.out.println("Error al escribir el flujo de datos JSON: " + e.getMessage());
        } catch (DatabindException e) {
            System.out.println("Error al mapear la lista de trabajos a JSON: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida general: " + e.getMessage());
        }
    }

    @Override
    public List<Trabajo> get() {
        return leer();
    }

    @Override
    public List<Trabajo> get(Cliente cliente) {
        List<Trabajo> trabajos = leer();
        List<Trabajo> trabajosCliente = new ArrayList<>();
        for (Trabajo trabajo : trabajos) {
            if (trabajo.getCliente().equals(cliente)) {
                trabajosCliente.add(trabajo);
            }
        }
        return trabajosCliente;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) {
        List<Trabajo> trabajos = leer();
        List<Trabajo> trabajosVehiculo = new ArrayList<>();
        for (Trabajo trabajo : trabajos) {
            if (trabajo.getVehiculo().equals(vehiculo)) {
                trabajosVehiculo.add(trabajo);
            }
        }
        return trabajosVehiculo;
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) {
        Objects.requireNonNull(mes, "El mes no puede ser nulo.");

        List<Trabajo> trabajos = leer();
        Map<TipoTrabajo, Integer> estadisticas = inicializarEstadisticas();

        for (Trabajo trabajo : trabajos) {
            LocalDate fecha = trabajo.getFechaInicio();
            if (fecha.getMonthValue() == mes.getMonthValue() && fecha.getYear() == mes.getYear()) {
                TipoTrabajo tipoTrabajo = TipoTrabajo.get(trabajo);
                estadisticas.put(tipoTrabajo, estadisticas.get(tipoTrabajo) + 1);
            }
        }
        return estadisticas;
    }

    private Map<TipoTrabajo, Integer> inicializarEstadisticas(){
        Map<TipoTrabajo, Integer> estadisticas = new EnumMap<>(TipoTrabajo.class);
        for (TipoTrabajo tipoTrabajo : TipoTrabajo.values()) {
            estadisticas.put(tipoTrabajo, 0);
        }
        return estadisticas;
    }


    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede insertar un trabajo nulo.");
        List<Trabajo> trabajos = leer();

        for (Trabajo t : trabajos) {
            if (!t.estaCerrado()) {
                if (t.getCliente().equals(trabajo.getCliente())) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo en curso.");
                } else if (t.getVehiculo().equals(trabajo.getVehiculo())) {
                    throw new TallerMecanicoExcepcion("El vehículo está actualmente en el taller.");
                }
            } else {
                if (t.getCliente().equals(trabajo.getCliente()) && !trabajo.getFechaInicio().isAfter(t.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El cliente tiene otro trabajo posterior.");
                } else if (t.getVehiculo().equals(trabajo.getVehiculo()) && !trabajo.getFechaInicio().isAfter(t.getFechaFin())) {
                    throw new TallerMecanicoExcepcion("El vehículo tiene otro trabajo posterior.");
                }
            }
        }

        trabajos.add(trabajo);
        escribir(trabajos);
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir horas a un trabajo nulo.");

        List<Trabajo> trabajos = leer();

        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);

        trabajoEncontrado.anadirHoras(horas);
        escribir(trabajos);

        return trabajoEncontrado;
    }

    private Trabajo getTrabajoAbierto(Vehiculo vehiculo, List<Trabajo> trabajos) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(vehiculo, "No puedo operar sobre un vehículo nulo.");

        for (Trabajo trabajo : trabajos) {
            if (trabajo.getVehiculo().equals(vehiculo) && !trabajo.estaCerrado()) {
                return trabajo;
            }
        }
        throw new TallerMecanicoExcepcion("No existe ningún trabajo abierto para dicho vehículo.");
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo añadir precio del material a un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);

        if (trabajoEncontrado instanceof Mecanico mecanico) {
            mecanico.anadirPrecioMaterial(precioMaterial);
            escribir(trabajos);
            return trabajoEncontrado;
        } else {
            throw new TallerMecanicoExcepcion("No se puede añadir precio al material para este tipo de trabajos.");
        }
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No puedo cerrar un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        Trabajo trabajoEncontrado = getTrabajoAbierto(trabajo.getVehiculo(), trabajos);

        trabajoEncontrado.cerrar(fechaFin);
        escribir(trabajos);

        return trabajoEncontrado;
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) {
        Objects.requireNonNull(trabajo, "No se puede buscar un trabajo nulo.");

        List<Trabajo> trabajos = leer();
        int indice = trabajos.indexOf(trabajo);

        return (indice != -1) ? trabajos.get(indice) : null;
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(trabajo, "No se puede borrar un trabajo nulo.");

        List<Trabajo> trabajos = leer();

        if (!trabajos.contains(trabajo)) {
            throw new TallerMecanicoExcepcion("No existe ningún trabajo igual.");
        }

        trabajos.remove(trabajo);
        escribir(trabajos);
    }
}
