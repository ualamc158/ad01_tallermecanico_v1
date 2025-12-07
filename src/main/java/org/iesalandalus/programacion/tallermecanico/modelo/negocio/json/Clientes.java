package org.iesalandalus.programacion.tallermecanico.modelo.negocio.json;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iesalandalus.programacion.tallermecanico.modelo.TallerMecanicoExcepcion;
import org.iesalandalus.programacion.tallermecanico.modelo.dominio.Cliente;
import org.iesalandalus.programacion.tallermecanico.modelo.negocio.IClientes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Clientes implements IClientes {

    private static final String FICHERO_CLIENTES = "datos/ficheros/json/clientes.json";
    private static ObjectMapper mapper;
    private static Clientes instancia;

    private Clientes (){
        ObjectMapper mapper = new ObjectMapper();

        File fichero = new File(FICHERO_CLIENTES);
        File carpeta = fichero.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
    }

    static Clientes getInstancia() {
        if (instancia == null) {
            instancia = new Clientes();
        }
        return instancia;
    }

    @Override
    public void comenzar() {

    }

    public List<Cliente> leer() {
        File fichero = new File(FICHERO_CLIENTES);
        List<Cliente> clientes = new ArrayList<>();

        // Si el fichero no existe, devolvemos el array vacío
        if (!fichero.exists()) {
            return clientes;
        }

        try {
            clientes = mapper.readValue(fichero, new TypeReference<List<Cliente>>() {});

        } catch (StreamReadException e) {
            System.out.println("Error leyendo el flujo de datos JSON: " + e.getMessage());
        } catch (DatabindException e) {
            System.out.println("Error mapeando el JSON a la clase Cliente: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida general: " + e.getMessage());
        }

        return clientes;
    }


    @Override
    public void terminar() {

    }

    public void escribir(List<Cliente> clientes) {
        File fichero = new File(FICHERO_CLIENTES);

        // Aseguramos que el directorio exista antes de escribir
        if (fichero.getParentFile() != null && !fichero.getParentFile().exists()) {
            fichero.getParentFile().mkdirs();
        }

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fichero, clientes);

        } catch (StreamWriteException e) {
            System.out.println("Error al escribir el flujo de datos JSON: " + e.getMessage());
        } catch (DatabindException e) {
            System.out.println("Error al mapear la lista de clientes a JSON: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de entrada/salida general: " + e.getMessage());
        }
    }

    @Override
    public List<Cliente> get() {
        return leer();
    }

    @Override
    public void insertar(Cliente cliente) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede insertar un cliente nulo.");
        }

        // Cargamos la lista actual desde el fichero
        List<Cliente> clientes = leer();

        // Comprobamos si el cliente ya existe
        if (clientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("Ya existe un cliente con ese DNI.");
        }

        // Añadimos el nuevo cliente a la lista
        clientes.add(cliente);

        // Guardamos la lista actualizada en el fichero
        escribir(clientes);
    }

    @Override
    public Cliente modificar(Cliente cliente, String nombre, String telefono) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede modificar un cliente nulo.");
        }

        List<Cliente> clientes = leer();

        // Buscamos el índice del cliente en la lista
        int indice = clientes.indexOf(cliente);

        if (indice == -1) {
            throw new TallerMecanicoExcepcion("No existe ningún cliente con ese DNI.");
        }

        // Recuperamos el objeto real de la lista para modificarlo
        Cliente clienteAModificar = clientes.get(indice);
        boolean modificado = false;

        // Verificamos y aplicamos cambios
        if (nombre != null && !nombre.isBlank()) {
            clienteAModificar.setNombre(nombre);
            modificado = true;
        }

        if (telefono != null && !telefono.isBlank()) {
            clienteAModificar.setTelefono(telefono);
            modificado = true;
        }

        // Si hubo cambios, guardamos la lista actualizada en el disco
        if (modificado) {
            escribir(clientes);
        }

        return clienteAModificar;
    }

    @Override
    public Cliente buscar(Cliente cliente) {
        if (cliente == null) {
            throw new NullPointerException("No se puede buscar un cliente nulo.");
        }

        List<Cliente> clientes = leer();
        int indice = clientes.indexOf(cliente);

        if (indice != -1) {
            return clientes.get(indice);
        }
        return null;
    }

    @Override
    public void borrar(Cliente cliente) throws TallerMecanicoExcepcion {
        if (cliente == null) {
            throw new NullPointerException("No se puede borrar un cliente nulo.");
        }

        List<Cliente> clientes = leer();

        if (!clientes.contains(cliente)) {
            throw new TallerMecanicoExcepcion("El cliente no existe.");
        }

        clientes.remove(cliente);
        escribir(clientes);
    }
}
