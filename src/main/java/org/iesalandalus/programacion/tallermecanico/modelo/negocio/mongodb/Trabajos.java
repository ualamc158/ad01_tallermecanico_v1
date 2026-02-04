package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.*;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.ITrabajos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.iesalandalus.programacion.tallermecanico.modelo.dominio.Trabajo.FORMATO_FECHA;

public class Trabajos implements ITrabajos {
    private static final String COLECCION = "trabajos";
    private static final String CLIENTE = "cliente";
    private static final String VEHICULO = "vehiculo";
    private static final String FECHA_INICIO = "fechaInicio";
    private static final String FECHA_FIN = "fechaFin";
    private static final String TIPO = "tipo";
    private static final String REVISION = "revision";
    private static final String MECANICO = "mecanico";
    private static final String HORAS = "horas";
    private static final String PRECIO_MATERIAL = "precioMaterial";
    private static final String DNI_CLIENTE = "dni";
    private static final String MATRICULA_VEHICULO = "matricula";

    private MongoCollection<Document> coleccionTrabajos;
    private static Trabajos instancia;

    private Trabajos() {
    }

    public static Trabajos getInstancia() {
        if (instancia == null) {
            instancia = new Trabajos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        coleccionTrabajos = MongoDB.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        MongoDB.cerrarConexion();
    }

    private Trabajo getTrabajo(Document doc) throws TallerMecanicoExcepcion {
        if (doc == null) return null;
        Document docCliente = (Document) doc.get(CLIENTE);
        Document docVehiculo = (Document) doc.get(VEHICULO);

        Cliente cliente = new Cliente(docCliente.getString("nombre"), docCliente.getString(DNI_CLIENTE), docCliente.getString("telefono"));
        Vehiculo vehiculo = new Vehiculo(docVehiculo.getString("marca"), docVehiculo.getString("modelo"), docVehiculo.getString(MATRICULA_VEHICULO));

        LocalDate fechaInicio = LocalDate.parse(doc.getString(FECHA_INICIO), FORMATO_FECHA);
        String tipoTrabajo = doc.getString(TIPO);

        Trabajo trabajo;

        if (REVISION.equals(tipoTrabajo)) {
            trabajo = new Revision(cliente, vehiculo, fechaInicio);
        } else {
            trabajo = new Mecanico(cliente, vehiculo, fechaInicio);

            if (doc.containsKey(HORAS)) {
                ((Mecanico) trabajo).anadirHoras(doc.getInteger(HORAS));
            }
            if (doc.containsKey(PRECIO_MATERIAL)) {
                ((Mecanico) trabajo).anadirPrecioMaterial(doc.getDouble(PRECIO_MATERIAL).floatValue());
            }
        }

        if (doc.containsKey(FECHA_FIN)) {
            trabajo.cerrar(LocalDate.parse(doc.getString(FECHA_FIN), FORMATO_FECHA));
        }

        return trabajo;
    }

    private Document getDocumento(Trabajo trabajo) {
        if (trabajo == null) return null;

        Document doc = new Document();

        doc.append(CLIENTE, new Document("nombre", trabajo.getCliente().getNombre()).append(DNI_CLIENTE, trabajo.getCliente().getDni()).append("telefono", trabajo.getCliente().getTelefono()));
        doc.append(VEHICULO, new Document("marca", trabajo.getVehiculo().marca()).append("modelo", trabajo.getVehiculo().modelo()).append(MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()));
        doc.append(FECHA_INICIO, trabajo.getFechaInicio().format(FORMATO_FECHA));

        if (trabajo.getFechaFin() != null) {
            doc.append(FECHA_FIN, trabajo.getFechaFin().format(FORMATO_FECHA));
        }

        if (trabajo instanceof Revision) {
            doc.append(TIPO, REVISION);
        } else if (trabajo instanceof Mecanico) {
            doc.append(TIPO, MECANICO);
            Mecanico trabMec = (Mecanico) trabajo;
            doc.append(HORAS, trabMec.getHoras());
            doc.append(PRECIO_MATERIAL, (double) trabMec.getPrecioMaterial());
        }

        return doc;
    }

    @Override
    public List<Trabajo> get() throws TallerMecanicoExcepcion {
        List<Trabajo> trabajos = new ArrayList<>();
        for (Document doc : coleccionTrabajos.find()) {
            Trabajo trabajo = getTrabajo(doc);
            if (trabajo != null) {
                trabajos.add(trabajo);
            }
        }
        return trabajos;
    }

    @Override
    public List<Trabajo> get(Cliente cliente) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede buscar trabajos de un cliente nulo.");
        }
        List<Trabajo> trabajos = new ArrayList<>();
        Bson filtro = Filters.eq(CLIENTE + "." + DNI_CLIENTE, cliente.getDni());
        for (Document doc : coleccionTrabajos.find(filtro)) {
            trabajos.add(getTrabajo(doc));
        }
        return trabajos;
    }

    @Override
    public List<Trabajo> get(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede buscar trabajos de un vehículo nulo.");
        }
        List<Trabajo> trabajos = new ArrayList<>();
        Bson filtro = Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, vehiculo.matricula());
        for (Document doc : coleccionTrabajos.find(filtro)) {
            trabajos.add(getTrabajo(doc));
        }
        return trabajos;
    }

    @Override
    public Map<TipoTrabajo, Integer> getEstadisticasMensuales(LocalDate mes) throws TallerMecanicoExcepcion {
        if (mes == null) {
            throw new NullPointerException("El mes no puede ser nulo.");
        }

        Map<TipoTrabajo, Integer> estadisticas = inicializarEstadisticas();

        List<Trabajo> trabajos = get();
        for (Trabajo trabajo : trabajos) {
            if (trabajo.getFechaInicio().getMonth().equals(mes.getMonth()) && trabajo.getFechaInicio().getYear() == mes.getYear()) {
                TipoTrabajo tipo = TipoTrabajo.get(trabajo);
                estadisticas.put(tipo, estadisticas.get(tipo) + 1);
            }
        }
        return estadisticas;
    }

    private Map<TipoTrabajo, Integer> inicializarEstadisticas() {
        Map<TipoTrabajo, Integer> estadisticas = new HashMap<>();
        for (TipoTrabajo tipo : TipoTrabajo.values()) {
            estadisticas.put(tipo, 0);
        }
        return estadisticas;
    }

    @Override
    public void insertar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        if (trabajo == null) {
            throw new NullPointerException("No se puede insertar un trabajo nulo.");
        }
        if (buscar(trabajo) != null) {
            throw new TallerMecanicoExcepcion("El trabajo ya existe.");
        }

        Bson filtroAbierto = Filters.and(Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()), Filters.exists(FECHA_FIN, false));

        if (coleccionTrabajos.find(filtroAbierto).first() != null) {
            throw new TallerMecanicoExcepcion("El vehículo ya tiene un trabajo abierto.");
        }

        coleccionTrabajos.insertOne(getDocumento(trabajo));
    }

    @Override
    public Trabajo anadirHoras(Trabajo trabajo, int horas) throws TallerMecanicoExcepcion {
        if (trabajo == null) {
            throw new NullPointerException("No se puede añadir horas a un trabajo nulo.");
        }
        if (buscar(trabajo) == null) {
            throw new TallerMecanicoExcepcion("El trabajo no existe.");
        }
        if (trabajo instanceof Revision) {
            throw new TallerMecanicoExcepcion("No se pueden añadir horas a una revisión.");
        }

        Bson filtro = Filters.and(Filters.eq(CLIENTE + "." + DNI_CLIENTE, trabajo.getCliente().getDni()), Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()), Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().format(FORMATO_FECHA)));

        coleccionTrabajos.updateOne(filtro, Updates.set(HORAS, horas));

        ((Mecanico) trabajo).anadirHoras(horas);
        return trabajo;
    }

    @Override
    public Trabajo anadirPrecioMaterial(Trabajo trabajo, float precioMaterial) throws TallerMecanicoExcepcion {
        if (trabajo == null) {
            throw new NullPointerException("No se puede añadir precio de material a un trabajo nulo.");
        }
        if (buscar(trabajo) == null) {
            throw new TallerMecanicoExcepcion("El trabajo no existe.");
        }
        if (trabajo instanceof Revision) {
            throw new TallerMecanicoExcepcion("No se puede añadir precio material a una revisión.");
        }

        Bson filtro = Filters.and(Filters.eq(CLIENTE + "." + DNI_CLIENTE, trabajo.getCliente().getDni()), Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()), Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().format(FORMATO_FECHA)));

        coleccionTrabajos.updateOne(filtro, Updates.set(PRECIO_MATERIAL, (double) precioMaterial));

        ((Mecanico) trabajo).anadirPrecioMaterial(precioMaterial);
        return trabajo;
    }

    @Override
    public Trabajo cerrar(Trabajo trabajo, LocalDate fechaFin) throws TallerMecanicoExcepcion {
        if (trabajo == null) {
            throw new NullPointerException("No se puede cerrar un trabajo nulo.");
        }
        if (fechaFin == null) {
            throw new NullPointerException("La fecha de fin no puede ser nula.");
        }
        if (buscar(trabajo) == null) {
            throw new TallerMecanicoExcepcion("El trabajo no existe.");
        }
        if (fechaFin.isBefore(trabajo.getFechaInicio())) {
            throw new TallerMecanicoExcepcion("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Bson filtro = Filters.and(Filters.eq(CLIENTE + "." + DNI_CLIENTE, trabajo.getCliente().getDni()), Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()), Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().format(FORMATO_FECHA)));

        coleccionTrabajos.updateOne(filtro, Updates.set(FECHA_FIN, fechaFin.format(FORMATO_FECHA)));

        trabajo.cerrar(fechaFin);
        return trabajo;
    }

    @Override
    public Trabajo buscar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        if (trabajo == null) {
            throw new NullPointerException("No se puede buscar un trabajo nulo.");
        }

        Bson filtro = Filters.and(Filters.eq(CLIENTE + "." + DNI_CLIENTE, trabajo.getCliente().getDni()), Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()), Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().format(FORMATO_FECHA)));

        Document doc = coleccionTrabajos.find(filtro).first();
        return getTrabajo(doc);
    }

    @Override
    public void borrar(Trabajo trabajo) throws TallerMecanicoExcepcion {
        if (trabajo == null) {
            throw new NullPointerException("No se puede borrar un trabajo nulo.");
        }
        if (buscar(trabajo) == null) {
            throw new TallerMecanicoExcepcion("El trabajo no existe.");
        }

        Bson filtro = Filters.and(Filters.eq(CLIENTE + "." + DNI_CLIENTE, trabajo.getCliente().getDni()), Filters.eq(VEHICULO + "." + MATRICULA_VEHICULO, trabajo.getVehiculo().matricula()), Filters.eq(FECHA_INICIO, trabajo.getFechaInicio().format(FORMATO_FECHA))
        );

        coleccionTrabajos.deleteOne(filtro);
    }
}