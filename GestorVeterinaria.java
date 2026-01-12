import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class GestorVeterinaria {
    private List<Cliente> clientes = new ArrayList<>();
    private List<Paciente> pacientes = new ArrayList<>();
    private List<Cita> citas = new ArrayList<>();
    private static final String ARCHIVO_DATOS = "veterinaria_datos.dat";

    // Métodos para Clientes
    public void agregarCliente(Cliente cliente) {
        clientes.add(cliente);
        System.out.println("✓ Cliente agregado exitosamente");
    }

    public void listarClientes() {
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados");
            return;
        }
        System.out.println("\n=== LISTA DE CLIENTES ===");
        for (Cliente c : clientes) {
            System.out.println(c);
        }
    }

    public Cliente buscarCliente(int id) {
        return clientes.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Cliente> buscarClientesPorNombre(String nombre) {
        return clientes.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();
    }

    public void editarCliente(int id) {
        Cliente cliente = buscarCliente(id);
        if (cliente == null) {
            System.out.println("Cliente no encontrado");
            return;
        }

        System.out.println("\nCliente actual: " + cliente);
        System.out.println("\n--- EDITAR CLIENTE (Enter para mantener valor actual) ---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Nombre [" + cliente.getNombre() + "]: ");
        String nombre = sc.nextLine();
        if (!nombre.trim().isEmpty())
            cliente.setNombre(nombre);

        System.out.print("Teléfono [" + cliente.getTelefono() + "]: ");
        String telefono = sc.nextLine();
        if (!telefono.trim().isEmpty())
            cliente.setTelefono(telefono);

        System.out.print("Email [" + cliente.getEmail() + "]: ");
        String email = sc.nextLine();
        if (!email.trim().isEmpty())
            cliente.setEmail(email);

        System.out.print("Dirección [" + cliente.getDireccion() + "]: ");
        String direccion = sc.nextLine();
        if (!direccion.trim().isEmpty())
            cliente.setDireccion(direccion);

        System.out.println("✓ Cliente actualizado exitosamente");
    }

    public void eliminarCliente(int id) {
        Cliente cliente = buscarCliente(id);
        if (cliente == null) {
            System.out.println("Cliente no encontrado");
            return;
        }

        // Verificar si tiene pacientes asociados
        long pacientesAsociados = pacientes.stream()
                .filter(p -> p.getDueno().getId() == id)
                .count();

        if (pacientesAsociados > 0) {
            System.out.println(
                    "⚠ No se puede eliminar. El cliente tiene " + pacientesAsociados + " paciente(s) asociado(s)");
            return;
        }

        clientes.remove(cliente);
        System.out.println("✓ Cliente eliminado exitosamente");
    }

    // Métodos para Pacientes
    public void agregarPaciente(Paciente paciente) {
        pacientes.add(paciente);
        System.out.println("✓ Paciente agregado exitosamente");
    }

    public void listarPacientes() {
        if (pacientes.isEmpty()) {
            System.out.println("No hay pacientes registrados");
            return;
        }
        System.out.println("\n=== LISTA DE PACIENTES ===");
        for (Paciente p : pacientes) {
            System.out.println(p);
        }
    }

    public Paciente buscarPaciente(int id) {
        return pacientes.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Paciente> buscarPacientesPorNombre(String nombre) {
        return pacientes.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();
    }

    public void editarPaciente(int id) {
        Paciente paciente = buscarPaciente(id);
        if (paciente == null) {
            System.out.println("Paciente no encontrado");
            return;
        }

        System.out.println("\nPaciente actual: " + paciente);
        System.out.println("\n--- EDITAR PACIENTE (Enter para mantener valor actual) ---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Nombre [" + paciente.getNombre() + "]: ");
        String nombre = sc.nextLine();
        if (!nombre.trim().isEmpty())
            paciente.setNombre(nombre);

        System.out.print("Especie [" + paciente.getEspecie() + "]: ");
        String especie = sc.nextLine();
        if (!especie.trim().isEmpty())
            paciente.setEspecie(especie);

        System.out.print("Raza [" + paciente.getRaza() + "]: ");
        String raza = sc.nextLine();
        if (!raza.trim().isEmpty())
            paciente.setRaza(raza);

        System.out.print("Edad [" + paciente.getEdad() + "]: ");
        String edadStr = sc.nextLine();
        if (!edadStr.trim().isEmpty()) {
            try {
                int edad = Integer.parseInt(edadStr);
                paciente.setEdad(edad);
            } catch (NumberFormatException e) {
                System.out.println("Edad inválida, se mantiene el valor anterior");
            }
        }

        System.out.println("✓ Paciente actualizado exitosamente");
    }

    public void eliminarPaciente(int id) {
        Paciente paciente = buscarPaciente(id);
        if (paciente == null) {
            System.out.println("Paciente no encontrado");
            return;
        }

        // Eliminar citas asociadas
        citas.removeIf(c -> c.getPaciente().getId() == id);

        pacientes.remove(paciente);
        System.out.println("✓ Paciente y sus citas eliminados exitosamente");
    }

    // Métodos para Citas
    public void agregarCita(Cita cita) {
        citas.add(cita);
        System.out.println("✓ Cita agendada exitosamente");
    }

    public void listarCitas() {
        if (citas.isEmpty()) {
            System.out.println("No hay citas registradas");
            return;
        }
        System.out.println("\n=== LISTA DE CITAS ===");
        for (Cita c : citas) {
            System.out.println(c);
        }
    }

    public Cita buscarCita(int id) {
        return citas.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void editarCita(int id) {
        Cita cita = buscarCita(id);
        if (cita == null) {
            System.out.println("Cita no encontrada");
            return;
        }

        System.out.println("\nCita actual: " + cita);
        System.out.println("\n--- EDITAR CITA (Enter para mantener valor actual) ---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Motivo [" + cita.getMotivo() + "]: ");
        String motivo = sc.nextLine();
        if (!motivo.trim().isEmpty())
            cita.setMotivo(motivo);

        System.out.print("Observaciones [" + cita.getObservaciones() + "]: ");
        String obs = sc.nextLine();
        if (!obs.trim().isEmpty())
            cita.setObservaciones(obs);

        System.out.println("✓ Cita actualizada exitosamente");
    }

    public void eliminarCita(int id) {
        Cita cita = buscarCita(id);
        if (cita == null) {
            System.out.println("Cita no encontrada");
            return;
        }

        citas.remove(cita);
        System.out.println("✓ Cita eliminada exitosamente");
    }

    // Generación de Informes
    public void generarInformeCliente(int idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        if (cliente == null) {
            System.out.println("Cliente no encontrado");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("           INFORME DE CLIENTE");
        System.out.println("=".repeat(60));
        System.out.println("ID Cliente: " + cliente.getId());
        System.out.println("Nombre: " + cliente.getNombre());
        System.out.println("Teléfono: " + cliente.getTelefono());
        System.out.println("Email: " + cliente.getEmail());
        System.out.println("Dirección: " + cliente.getDireccion());
        System.out.println("\nPACIENTES ASOCIADOS:");
        System.out.println("-".repeat(60));

        pacientes.stream()
                .filter(p -> p.getDueno().getId() == idCliente)
                .forEach(p -> System.out.println("  • " + p.getNombre() + " (" + p.getEspecie() + ")"));

        System.out.println("=".repeat(60) + "\n");
    }

    public void generarInformePaciente(int idPaciente) {
        Paciente paciente = buscarPaciente(idPaciente);
        if (paciente == null) {
            System.out.println("Paciente no encontrado");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("           INFORME DE PACIENTE");
        System.out.println("=".repeat(60));
        System.out.println("ID Paciente: " + paciente.getId());
        System.out.println("Nombre: " + paciente.getNombre());
        System.out.println("Especie: " + paciente.getEspecie());
        System.out.println("Raza: " + paciente.getRaza());
        System.out.println("Edad: " + paciente.getEdad() + " años");
        System.out.println("\nDUEÑO:");
        System.out.println("-".repeat(60));
        System.out.println("  Nombre: " + paciente.getDueno().getNombre());
        System.out.println("  Teléfono: " + paciente.getDueno().getTelefono());

        System.out.println("\nHISTORIAL DE CITAS:");
        System.out.println("-".repeat(60));
        citas.stream()
                .filter(c -> c.getPaciente().getId() == idPaciente)
                .forEach(c -> System.out.println("  • " + c.getFecha() + " - " + c.getMotivo()));

        System.out.println("=".repeat(60) + "\n");
    }

    // Persistencia de Datos
    public void guardarDatos() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            oos.writeObject(clientes);
            oos.writeObject(pacientes);
            oos.writeObject(citas);
            oos.writeInt(Cliente.getContadorId());
            oos.writeInt(Paciente.getContadorId());
            oos.writeInt(Cita.getContadorId());
            System.out.println("✓ Datos guardados exitosamente en " + ARCHIVO_DATOS);
        } catch (IOException e) {
            System.err.println("✗ Error al guardar datos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void cargarDatos() {
        File archivo = new File(ARCHIVO_DATOS);
        if (!archivo.exists()) {
            System.out.println("No hay datos previos para cargar");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO_DATOS))) {
            clientes = (List<Cliente>) ois.readObject();
            pacientes = (List<Paciente>) ois.readObject();
            citas = (List<Cita>) ois.readObject();
            Cliente.setContadorId(ois.readInt());
            Paciente.setContadorId(ois.readInt());
            Cita.setContadorId(ois.readInt());
            System.out.println("✓ Datos cargados exitosamente");
            System.out.println("  - Clientes: " + clientes.size());
            System.out.println("  - Pacientes: " + pacientes.size());
            System.out.println("  - Citas: " + citas.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ Error al cargar datos: " + e.getMessage());
        }
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public List<Cita> getCitas() {
        return citas;
    }
}