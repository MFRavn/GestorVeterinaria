package Java;

import java.io.*;

class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contadorId = 1;
    private int id;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;

    public Cliente(String nombre, String telefono, String email, String direccion) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public static void setContadorId(int contador) {
        contadorId = contador;
    }

    public static int getContadorId() {
        return contadorId;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Cliente: %s | Tel: %s | Email: %s",
                id, nombre, telefono, email);
    }
}
