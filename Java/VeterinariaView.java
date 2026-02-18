package Java;
import java.util.List;

public class VeterinariaView {
    // Colores ANSI para la terminal
    public static final String RESET = "\u001B[0m";
    public static final String BLUE = "\u001B[34m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";

    public void mostrarHeader() {
        System.out.println(BLUE + "╔══════════════════════════════════════════════════════╗");
        System.out.println("║                VET-MANAGER PRO v1.0                  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝" + RESET);
    }

    public void mostrarMenuPrincipal() {
        System.out.println(CYAN + "\n  [1] 👤 Gestión Clientes    [2] 🐾 Gestión Pacientes");
        System.out.println("  [3] 📅 Agenda de Citas     [4] 📊 Informes");
        System.out.println("  [5] 💾 Guardar Datos       [0] ❌ Salir" + RESET);
        System.out.print("\n> Seleccione una opción: ");
    }

    public void imprimirMensaje(String msg, boolean exito) {
        String color = exito ? GREEN : "\u001B[31m";
        System.out.println(color + (exito ? "✔ " : "✘ ") + msg + RESET);
    }

    public void mostrarTablaClientes(List<Cliente> clientes) {
        System.out.println("\n" + YELLOW + "ID | NOMBRE               | TELÉFONO     | EMAIL");
        System.out.println("-------------------------------------------------------");
        for (Cliente c : clientes) {
            System.out.printf("%-2d | %-20s | %-12s | %-15s\n", 
                c.getId(), c.getNombre(), c.getTelefono(), c.getEmail());
        }
        System.out.println("-------------------------------------------------------" + RESET);
    }
}
