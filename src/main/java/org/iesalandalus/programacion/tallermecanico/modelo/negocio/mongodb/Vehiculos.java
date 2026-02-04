package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Vehiculo;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IVehiculos;

import java.util.ArrayList;
import java.util.List;

public class Vehiculos implements IVehiculos {
    private static final String COLECCION = "vehiculos";
    private static final String MARCA = "marca";
    private static final String MODELO = "modelo";
    private static final String MATRICULA = "matricula";

    private MongoCollection<Document> coleccionVehiculos;
    private static Vehiculos instancia;

    private Vehiculos() {
    }

    public static Vehiculos getInstancia() {
        if (instancia == null) {
            instancia = new Vehiculos();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        coleccionVehiculos = MongoDB.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        MongoDB.cerrarConexion();
    }

    @Override
    public List<Vehiculo> get() {
        List<Vehiculo> listaVehiculos = new ArrayList<>();
        for (Document documento : coleccionVehiculos.find().sort(Sorts.ascending(MATRICULA))) {
            Vehiculo vehiculo = getVehiculo(documento);
            if (vehiculo != null) {
                listaVehiculos.add(vehiculo);
            }
        }
        return listaVehiculos;
    }

    @Override
    public void insertar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede insertar un vehículo nulo.");
        }
        if (buscar(vehiculo) != null) {
            throw new TallerMecanicoExcepcion("El vehículo ya existe.");
        }
        coleccionVehiculos.insertOne(getDocumento(vehiculo));
    }

    @Override
    public Vehiculo buscar(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede buscar un vehículo nulo.");
        }
        Document documento = coleccionVehiculos.find(Filters.eq(MATRICULA, vehiculo.matricula())).first();
        return getVehiculo(documento);
    }

    @Override
    public void borrar(Vehiculo vehiculo) throws TallerMecanicoExcepcion {
        if (vehiculo == null) {
            throw new NullPointerException("No se puede borrar un vehículo nulo.");
        }
        if (buscar(vehiculo) == null) {
            throw new TallerMecanicoExcepcion("El vehículo no existe.");
        }

        Document trabajoAsociado = MongoDB.getBD().getCollection("trabajos").find(Filters.eq("vehiculo.matricula", vehiculo.matricula())).first();

        if (trabajoAsociado != null) {
            throw new TallerMecanicoExcepcion("No se puede borrar el vehículo porque tiene trabajos asociados.");
        }

        coleccionVehiculos.deleteOne(Filters.eq(MATRICULA, vehiculo.matricula()));
    }

    private Vehiculo getVehiculo(Document documento) {
        if (documento == null) {
            return null;
        }
        return new Vehiculo(documento.getString(MARCA), documento.getString(MODELO), documento.getString(MATRICULA));
    }

    private Document getDocumento(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }
        return new Document().append(MARCA, vehiculo.marca()).append(MODELO, vehiculo.modelo()).append(MATRICULA, vehiculo.matricula());
    }
}
