package Java;

public class Main {
    public static void main(String[] args) {
        // Inicializamos los componentes del MVC
        GestorVeterinaria modelo = new GestorVeterinaria();
        VeterinariaView vista = new VeterinariaView();
        
        // El controlador toma el mando
        VeterinariaController controlador = new VeterinariaController(modelo, vista);
        controlador.iniciarApp();
    }
}