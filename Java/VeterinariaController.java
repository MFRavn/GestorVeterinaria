package Java;
import java.util.Scanner;
import java.sql.Date;

public class VeterinariaController {
    private GestorVeterinaria modelo;
    private VeterinariaView vista;
    private Scanner sc;

    public VeterinariaController(GestorVeterinaria modelo, VeterinariaView vista) {
        this.modelo = modelo;
        this.vista  = vista;
        this.sc     = new Scanner(System.in);
    }

    public void iniciarApp() {
        modelo.cargarDatos();
        int opcion = -1;
        do {
            vista.mostrarHeader();
            vista.mostrarMenuPrincipal();
            try {
                opcion = Integer.parseInt(sc.nextLine());
                mapearAccion(opcion);
            } catch (NumberFormatException e) {
                vista.imprimirMensaje("Por favor, introduce un número válido.", false);
            }
        } while (opcion != 0);

        // Guardado final + cierre ordenado de conexión MySQL
        modelo.guardarDatos();
        Databasemanager.getInstance().cerrar();
    }

    private void mapearAccion(int opcion) {
        switch (opcion) {
            case 1 -> menuClientes();
            case 2 -> menuPacientes();
            case 3 -> menuCitas();
            case 4 -> {
                vista.imprimirMensaje("Generando reportes generales...", true);
                modelo.listarClientes();
            }
            case 5 -> {
                modelo.guardarDatos();
                vista.imprimirMensaje("Datos respaldados correctamente", true);
            }
            case 0 -> vista.imprimirMensaje("Saliendo del sistema...", true);
            default -> vista.imprimirMensaje("Opción no reconocida", false);
        }
    }

    // ── CLIENTES ──────────────────────────────────────────────────────────────

    private void menuClientes() {
        System.out.println("\n--- GESTIÓN DE CLIENTES ---");
        System.out.println("1. Listar   2. Agregar   3. Editar   4. Eliminar   5. Volver");
        String op = sc.nextLine();

        switch (op) {
            case "1" -> vista.mostrarTablaClientes(modelo.getClientes());
            case "2" -> {
                System.out.print("Nombre: ");    String nom  = sc.nextLine();
                System.out.print("Teléfono: ");  String tel  = sc.nextLine();
                System.out.print("Email: ");     String mail = sc.nextLine();
                System.out.print("Dirección: "); String dir  = sc.nextLine();
                modelo.agregarCliente(new Cliente(nom, tel, mail, dir));
            }
            case "3" -> {
                System.out.print("ID del cliente a editar: ");
                try { modelo.editarCliente(Integer.parseInt(sc.nextLine())); }
                catch (NumberFormatException e) { vista.imprimirMensaje("ID inválido", false); }
            }
            case "4" -> {
                System.out.print("ID del cliente a eliminar: ");
                try { modelo.eliminarCliente(Integer.parseInt(sc.nextLine())); }
                catch (NumberFormatException e) { vista.imprimirMensaje("ID inválido", false); }
            }
        }
    }

    // ── PACIENTES ─────────────────────────────────────────────────────────────

    private void menuPacientes() {
        System.out.println("\n--- GESTIÓN DE PACIENTES ---");
        System.out.println("1. Listar   2. Agregar   3. Editar   4. Eliminar   5. Volver");
        String op = sc.nextLine();

        switch (op) {
            case "1" -> modelo.listarPacientes();
            case "2" -> {
                // Mostrar clientes disponibles para elegir dueño
                vista.mostrarTablaClientes(modelo.getClientes());
                System.out.print("ID del dueño (cliente): ");
                try {
                    int idCliente = Integer.parseInt(sc.nextLine());
                    Cliente dueno = modelo.buscarCliente(idCliente);
                    if (dueno == null) { vista.imprimirMensaje("Cliente no encontrado", false); return; }

                    System.out.print("Nombre del paciente: "); String nom     = sc.nextLine();
                    System.out.print("Especie: ");             String especie = sc.nextLine();
                    System.out.print("Raza: ");                String raza    = sc.nextLine();
                    System.out.print("Edad (años): ");
                    int edad = Integer.parseInt(sc.nextLine());

                    modelo.agregarPaciente(new Paciente(nom, especie, raza, edad, dueno));
                } catch (NumberFormatException e) {
                    vista.imprimirMensaje("Valor numérico inválido", false);
                }
            }
            case "3" -> {
                System.out.print("ID del paciente a editar: ");
                try { modelo.editarPaciente(Integer.parseInt(sc.nextLine())); }
                catch (NumberFormatException e) { vista.imprimirMensaje("ID inválido", false); }
            }
            case "4" -> {
                System.out.print("ID del paciente a eliminar: ");
                try { modelo.eliminarPaciente(Integer.parseInt(sc.nextLine())); }
                catch (NumberFormatException e) { vista.imprimirMensaje("ID inválido", false); }
            }
        }
    }

    // ── CITAS ─────────────────────────────────────────────────────────────────

    private void menuCitas() {
        System.out.println("\n--- AGENDA DE CITAS ---");
        System.out.println("1. Ver Citas   2. Agendar   3. Editar   4. Eliminar   5. Volver");
        String op = sc.nextLine();

        switch (op) {
            case "1" -> modelo.listarCitas();
            case "2" -> {
                try {
                    modelo.listarPacientes();
                    System.out.print("ID del Paciente: ");
                    int idPac = Integer.parseInt(sc.nextLine());
                    Paciente p = modelo.buscarPaciente(idPac);

                    if (p == null) { vista.imprimirMensaje("Paciente no encontrado", false); return; }

                    System.out.print("Fecha (YYYY-MM-DD): ");
                    Date fecha = Date.valueOf(sc.nextLine());
                    System.out.print("Motivo: ");        String motivo = sc.nextLine();
                    System.out.print("Observaciones: "); String obs    = sc.nextLine();

                    modelo.agregarCita(new Cita(p, fecha, motivo, obs));
                } catch (Exception e) {
                    vista.imprimirMensaje("Error en los datos de la cita: " + e.getMessage(), false);
                }
            }
            case "3" -> {
                System.out.print("ID de la cita a editar: ");
                try { modelo.editarCita(Integer.parseInt(sc.nextLine())); }
                catch (NumberFormatException e) { vista.imprimirMensaje("ID inválido", false); }
            }
            case "4" -> {
                System.out.print("ID de la cita a eliminar: ");
                try { modelo.eliminarCita(Integer.parseInt(sc.nextLine())); }
                catch (NumberFormatException e) { vista.imprimirMensaje("ID inválido", false); }
            }
        }
    }
}