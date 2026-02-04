package org.iesalandalus.programacion.tallermecanico.modelo.negocio.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Updates.set;

public class Clientes implements IClientes {
    private static final String COLECCION = "clientes";
    private static final String NOMBRE = "nombre";
    private static final String DNI = "dni";
    private static final String TELEFONO = "telefono";
    private MongoCollection<Document> coleccionClientes;
    private static Clientes instancia;


    private Clientes(){

    }

    public static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {
        coleccionClientes = MongoDB.getBD().getCollection(COLECCION);
    }

    @Override
    public void terminar() {
        MongoDB.cerrarConexion();
    }

    private Cliente getCliente(Document documento){
        Objects.requireNonNull(documento, "El documento no puede ser nulo");
        return new Cliente(documento.getString(NOMBRE), documento.getString(DNI), documento.getString(TELEFONO));
    }

    private Document getDocument(Cliente cliente){
        Objects.requireNonNull(cliente, "El cliente no puede ser nulo");
        return new Document().append(NOMBRE, cliente.getNombre()).append(DNI, cliente.getDni()).append(TELEFONO, cliente.getTelefono());
    }

    @Override
    public List<Cliente> get() {
        List<Cliente> listaClientes = new ArrayList<>();
        for (Document doc : coleccionClientes.find().sort(Sorts.ascending(DNI))) {
            Cliente cliente = getCliente(doc);
            if (cliente != null) {
                listaClientes.add(cliente);
            }
        }
        return listaClientes;
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "El cliente a insertar no puede ser nulo");
        coleccionClientes.insertOne(getDocument(cliente));
        System.out.println(">> Cliente insertado correctamente.");
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        Objects.requireNonNull(cliente, "El cliente a modificar no puede ser nulo");
        coleccionClientes.updateOne(Filters.eq("dni", cliente.getDni()),Updates.combine(set(NOMBRE, nombre), set(TELEFONO, telefono)));
        return cliente;
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        if (cliente == null) {
            throw new NullPointerException("No se puede buscar un cliente nulo.");
        }
        Document doc = coleccionClientes.find(Filters.eq(DNI, cliente.getDni())).first();
        return getCliente(doc);
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede borrar un cliente nulo.");
        }

        if (buscar(cliente) == null) {
            throw new TallerMecanicoExcepcion("El cliente no existe.");
        }

        Document trabajoAsociado = MongoDB.getBD().getCollection("trabajos").find(Filters.eq("cliente.dni", cliente.getDni())).first();

        if (trabajoAsociado != null) {
            throw new TallerMecanicoExcepcion("No se puede borrar el cliente porque tiene trabajos asociados.");
        }

        coleccionClientes.deleteOne(Filters.eq(DNI, cliente.getDni()));
    }
}
