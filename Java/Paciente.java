package Java;
import java.io.Serializable;

class Paciente implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contadorId = 1;
    private int id;
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private Cliente dueno;

    public Paciente(String nombre, String especie, String raza, int edad, Cliente dueno) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edad = edad;
        this.dueno = dueno;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public String getRaza() {
        return raza;
    }

    public int getEdad() {
        return edad;
    }

    public Cliente getDueno() {
        return dueno;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setDueno(Cliente dueno) {
        this.dueno = dueno;
    }

    public static void setContadorId(int contador) {
        contadorId = contador;
    }

    public static int getContadorId() {
        return contadorId;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Paciente: %s | Especie: %s | Raza: %s | Edad: %d años | Dueño: %s",
                id, nombre, especie, raza, edad, dueno.getNombre());
    }
}