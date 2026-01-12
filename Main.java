import java.sql.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner sc = new Scanner(System.in);
    private static GestorVeterinaria gestor = new GestorVeterinaria();

    public static void main(String[] args) {
        gestor.cargarDatos();

        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");
            procesarOpcion(opcion);
        } while (opcion != 0);

        gestor.guardarDatos();
        System.out.println("¡Hasta luego!");
        sc.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   SISTEMA DE GESTIÓN VETERINARIA      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("  CLIENTES:");
        System.out.println("    1. Agregar Cliente");
        System.out.println("    2. Listar Clientes");
        System.out.println("    3. Buscar Cliente por Nombre");
        System.out.println("    4. Editar Cliente");
        System.out.println("    5. Eliminar Cliente");
        System.out.println("\n  PACIENTES:");
        System.out.println("    6. Agregar Paciente");
        System.out.println("    7. Listar Pacientes");
        System.out.println("    8. Buscar Paciente por Nombre");
        System.out.println("    9. Editar Paciente");
        System.out.println("   10. Eliminar Paciente");
        System.out.println("\n  CITAS:");
        System.out.println("   11. Agendar Cita");
        System.out.println("   12. Listar Citas");
        System.out.println("   13. Editar Cita");
        System.out.println("   14. Eliminar Cita");
        System.out.println("\n  INFORMES:");
        System.out.println("   15. Generar Informe de Cliente");
        System.out.println("   16. Generar Informe de Paciente");
        System.out.println("\n  SISTEMA:");
        System.out.println("   17. Guardar Datos Manualmente");
        System.out.println("    0. Salir");
        System.out.println("─────────────────────────────────────────");
    }

    private static void procesarOpcion(int opcion) {
        switch (opcion) {
            case 1:
                agregarCliente();
                break;
            case 2:
                gestor.listarClientes();
                break;
            case 3:
                buscarClientePorNombre();
                break;
            case 4:
                editarCliente();
                break;
            case 5:
                eliminarCliente();
                break;
            case 6:
                agregarPaciente();
                break;
            case 7:
                gestor.listarPacientes();
                break;
            case 8:
                buscarPacientePorNombre();
                break;
            case 9:
                editarPaciente();
                break;
            case 10:
                eliminarPaciente();
                break;
            case 11:
                agendarCita();
                break;
            case 12:
                gestor.listarCitas();
                break;
            case 13:
                editarCita();
                break;
            case 14:
                eliminarCita();
                break;
            case 15:
                generarInformeCliente();
                break;
            case 16:
                generarInformePaciente();
                break;
            case 17:
                gestor.guardarDatos();
                break;
            case 0:
                break;
            default:
                System.out.println("Opción inválida");
        }
    }

    private static void agregarCliente() {
        System.out.println("\n--- AGREGAR CLIENTE ---");
        String nombre = leerTexto("Nombre: ");
        String telefono = leerTexto("Teléfono: ");
        String email = leerTexto("Email: ");
        String direccion = leerTexto("Dirección: ");

        Cliente cliente = new Cliente(nombre, telefono, email, direccion);
        gestor.agregarCliente(cliente);
    }

    private static void buscarClientePorNombre() {
        String nombre = leerTexto("\nIngrese el nombre a buscar: ");
        List<Cliente> resultados = gestor.buscarClientesPorNombre(nombre);

        if (resultados.isEmpty()) {
            System.out.println("No se encontraron clientes con ese nombre");
        } else {
            System.out.println("\n=== RESULTADOS DE BÚSQUEDA ===");
            resultados.forEach(System.out::println);
        }
    }

    private static void editarCliente() {
        gestor.listarClientes();
        int id = leerEntero("\nID del cliente a editar: ");
        gestor.editarCliente(id);
    }

    private static void eliminarCliente() {
        gestor.listarClientes();
        int id = leerEntero("\nID del cliente a eliminar: ");
        String confirmacion = leerTexto("¿Está seguro? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S")) {
            gestor.eliminarCliente(id);
        } else {
            System.out.println("Operación cancelada");
        }
    }

    private static void agregarPaciente() {
        if (gestor.getClientes().isEmpty()) {
            System.out.println("Primero debe registrar un cliente");
            return;
        }

        System.out.println("\n--- AGREGAR PACIENTE ---");
        gestor.listarClientes();
        int idCliente = leerEntero("\nID del dueño: ");

        Cliente dueno = gestor.buscarCliente(idCliente);
        if (dueno == null) {
            System.out.println("Cliente no encontrado");
            return;
        }

        String nombre = leerTexto("Nombre del paciente: ");
        String especie = leerTexto("Especie: ");
        String raza = leerTexto("Raza: ");
        int edad = leerEntero("Edad (años): ");

        Paciente paciente = new Paciente(nombre, especie, raza, edad, dueno);
        gestor.agregarPaciente(paciente);
    }

    private static void buscarPacientePorNombre() {
        String nombre = leerTexto("\nIngrese el nombre a buscar: ");
        List<Paciente> resultados = gestor.buscarPacientesPorNombre(nombre);

        if (resultados.isEmpty()) {
            System.out.println("No se encontraron pacientes con ese nombre");
        } else {
            System.out.println("\n=== RESULTADOS DE BÚSQUEDA ===");
            resultados.forEach(System.out::println);
        }
    }

    private static void editarPaciente() {
        gestor.listarPacientes();
        int id = leerEntero("\nID del paciente a editar: ");
        gestor.editarPaciente(id);
    }

    private static void eliminarPaciente() {
        gestor.listarPacientes();
        int id = leerEntero("\nID del paciente a eliminar: ");
        String confirmacion = leerTexto("¿Está seguro? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S")) {
            gestor.eliminarPaciente(id);
        } else {
            System.out.println("Operación cancelada");
        }
    }

    private static void agendarCita() {
        if (gestor.getPacientes().isEmpty()) {
            System.out.println("Primero debe registrar un paciente");
            return;
        }

        System.out.println("\n--- AGENDAR CITA ---");
        gestor.listarPacientes();
        int idPaciente = leerEntero("\nID del paciente: ");

        Paciente paciente = gestor.buscarPaciente(idPaciente);
        if (paciente == null) {
            System.out.println("Paciente no encontrado");
            return;
        }

        Date fecha = new Date(idPaciente, idPaciente, idPaciente);
        String motivo = leerTexto("Motivo de la cita: ");
        String observaciones = leerTexto("Observaciones: ");

        Cita cita = new Cita(paciente, fecha, motivo, observaciones);
        gestor.agregarCita(cita);
    }

    private static void editarCita() {
        gestor.listarCitas();
        int id = leerEntero("\nID de la cita a editar: ");
        gestor.editarCita(id);
    }

    private static void eliminarCita() {
        gestor.listarCitas();
        int id = leerEntero("\nID de la cita a eliminar: ");
        String confirmacion = leerTexto("¿Está seguro? (S/N): ");
        if (confirmacion.equalsIgnoreCase("S")) {
            gestor.eliminarCita(id);
        } else {
            System.out.println("Operación cancelada");
        }
    }

    private static void generarInformeCliente() {
        gestor.listarClientes();
        int id = leerEntero("\nID del cliente para el informe: ");
        gestor.generarInformeCliente(id);
    }

    private static void generarInformePaciente() {
        gestor.listarPacientes();
        int id = leerEntero("\nID del paciente para el informe: ");
        gestor.generarInformePaciente(id);
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine();
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (!sc.hasNextInt()) {
            System.out.print("Por favor ingrese un número válido: ");
            sc.next();
        }
        int valor = sc.nextInt();
        sc.nextLine();
        return valor;
    }
}