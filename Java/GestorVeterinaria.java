package Java;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * GestorVeterinaria — Capa de lógica de negocio + acceso a datos.
 *
 * Estrategia de persistencia:
 *   · PRINCIPAL  → MySQL (via DatabaseManager / JDBC)
 *   · RESPALDO   → Serialización binaria (.dat)  ← se usa si MySQL no está disponible
 *                  y también como copia de seguridad tras cada guardado SQL.
 */
class GestorVeterinaria {

    // Listas en memoria (caché de trabajo)
    private List<Cliente>  clientes  = new ArrayList<>();
    private List<Paciente> pacientes = new ArrayList<>();
    private List<Cita>     citas     = new ArrayList<>();

    private static final String ARCHIVO_DATOS = "veterinaria_datos.dat";

    // ══════════════════════════════════════════════════════════════════════════
    //  CARGA Y GUARDADO DE DATOS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Intenta cargar desde MySQL. Si la BD no está disponible, carga el .dat.
     * Al final sincroniza los contadores de ID.
     */
    public void cargarDatos() {
        if (Databasemanager.getInstance().isConectado()) {
            System.out.println("ℹ Cargando datos desde MySQL...");
            boolean ok = cargarDesdeSQL();
            if (ok) {
                System.out.println("  - Clientes: "  + clientes.size());
                System.out.println("  - Pacientes: " + pacientes.size());
                System.out.println("  - Citas: "     + citas.size());
                sincronizarContadores();
                return;
            }
        }
        // Fallback
        System.out.println("⚠ MySQL no disponible — cargando respaldo .dat...");
        cargarDesdeDat();
    }

    /**
     * Guarda en MySQL Y genera un .dat de respaldo.
     */
    public void guardarDatos() {
        if (Databasemanager.getInstance().isConectado()) {
            guardarEnSQL();
        } else {
            System.out.println("⚠ MySQL no disponible — solo se guardará el respaldo .dat");
        }
        guardarEnDat();   // siempre genera el respaldo
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PERSISTENCIA MySQL
    // ══════════════════════════════════════════════════════════════════════════

    private boolean cargarDesdeSQL() {
        try {
            clientes.clear();
            pacientes.clear();
            citas.clear();

            Connection con = Databasemanager.getInstance().getConnection();

            // 1. Clientes
            String sqlClientes = "SELECT id, nombre, telefono, email, direccion FROM clientes ORDER BY id";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlClientes)) {
                while (rs.next()) {
                    Cliente c = new Cliente(
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion")
                    );
                    // Forzamos el ID que viene de la BD (el constructor ya asignó uno temporal)
                    setIdCliente(c, rs.getInt("id"));
                    clientes.add(c);
                }
            }

            // 2. Pacientes
            String sqlPacientes = "SELECT id, nombre, especie, raza, edad, cliente_id FROM pacientes ORDER BY id";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlPacientes)) {
                while (rs.next()) {
                    Cliente dueno = buscarCliente(rs.getInt("cliente_id"));
                    if (dueno == null) continue;  // integridad: skip huérfanos
                    Paciente p = new Paciente(
                        rs.getString("nombre"),
                        rs.getString("especie"),
                        rs.getString("raza"),
                        rs.getInt("edad"),
                        dueno
                    );
                    setIdPaciente(p, rs.getInt("id"));
                    pacientes.add(p);
                }
            }

            // 3. Citas
            String sqlCitas = "SELECT id, paciente_id, fecha, motivo, observaciones FROM citas ORDER BY id";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlCitas)) {
                while (rs.next()) {
                    Paciente pac = buscarPaciente(rs.getInt("paciente_id"));
                    if (pac == null) continue;
                    Cita cita = new Cita(
                        pac,
                        rs.getDate("fecha"),
                        rs.getString("motivo"),
                        rs.getString("observaciones")
                    );
                    setIdCita(cita, rs.getInt("id"));
                    citas.add(cita);
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("✘ Error al cargar desde MySQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sincroniza la BD completa con las listas en memoria (INSERT OR UPDATE).
     * Usa REPLACE INTO para simplificar la lógica upsert en MySQL.
     */
    private void guardarEnSQL() {
        Connection con = Databasemanager.getInstance().getConnection();
        try {
            con.setAutoCommit(false);   // transacción

            // ── Clientes ──────────────────────────────────────────────────────
            String sqlC = "REPLACE INTO clientes (id, nombre, telefono, email, direccion) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlC)) {
                for (Cliente c : clientes) {
                    ps.setInt(1,    c.getId());
                    ps.setString(2, c.getNombre());
                    ps.setString(3, c.getTelefono());
                    ps.setString(4, c.getEmail());
                    ps.setString(5, c.getDireccion());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // ── Pacientes ─────────────────────────────────────────────────────
            String sqlP = "REPLACE INTO pacientes (id, nombre, especie, raza, edad, cliente_id) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlP)) {
                for (Paciente p : pacientes) {
                    ps.setInt(1,    p.getId());
                    ps.setString(2, p.getNombre());
                    ps.setString(3, p.getEspecie());
                    ps.setString(4, p.getRaza());
                    ps.setInt(5,    p.getEdad());
                    ps.setInt(6,    p.getDueno().getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // ── Citas ─────────────────────────────────────────────────────────
            String sqlCita = "REPLACE INTO citas (id, paciente_id, fecha, motivo, observaciones) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlCita)) {
                for (Cita c : citas) {
                    ps.setInt(1,    c.getId());
                    ps.setInt(2,    c.getPaciente().getId());
                    ps.setDate(3,   c.getFecha());
                    ps.setString(4, c.getMotivo());
                    ps.setString(5, c.getObservaciones());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            System.out.println("✔ Datos guardados en MySQL correctamente.");

        } catch (SQLException e) {
            System.err.println("✘ Error al guardar en MySQL: " + e.getMessage());
            try { con.rollback(); } catch (SQLException ex) { /* ignore */ }
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  OPERACIONES CRUD — escribe en memoria Y en MySQL en tiempo real
    // ══════════════════════════════════════════════════════════════════════════

    // ── CLIENTES ──────────────────────────────────────────────────────────────

    public void agregarCliente(Cliente cliente) {
        // Insertar en BD y recuperar el ID generado por AUTO_INCREMENT
        if (Databasemanager.getInstance().isConectado()) {
            String sql = "INSERT INTO clientes (nombre, telefono, email, direccion) VALUES (?,?,?,?)";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection()
                        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getTelefono());
                ps.setString(3, cliente.getEmail());
                ps.setString(4, cliente.getDireccion());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) setIdCliente(cliente, rs.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("✘ Error al insertar cliente en MySQL: " + e.getMessage());
            }
        }
        clientes.add(cliente);
        System.out.println("✔ Cliente agregado exitosamente (ID: " + cliente.getId() + ")");
    }

    public void listarClientes() {
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados");
            return;
        }
        System.out.println("\n=== LISTA DE CLIENTES ===");
        for (Cliente c : clientes) System.out.println(c);
    }

    public Cliente buscarCliente(int id) {
        return clientes.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public List<Cliente> buscarClientesPorNombre(String nombre) {
        return clientes.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();
    }

    public void editarCliente(int id) {
        Cliente cliente = buscarCliente(id);
        if (cliente == null) { System.out.println("Cliente no encontrado"); return; }

        System.out.println("\nCliente actual: " + cliente);
        System.out.println("\n--- EDITAR CLIENTE (Enter para mantener valor actual) ---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Nombre ["      + cliente.getNombre()    + "]: "); String nombre    = sc.nextLine();
        System.out.print("Teléfono ["    + cliente.getTelefono()  + "]: "); String telefono  = sc.nextLine();
        System.out.print("Email ["       + cliente.getEmail()     + "]: "); String email     = sc.nextLine();
        System.out.print("Dirección ["   + cliente.getDireccion() + "]: "); String direccion = sc.nextLine();

        if (!nombre.trim().isEmpty())    cliente.setNombre(nombre);
        if (!telefono.trim().isEmpty())  cliente.setTelefono(telefono);
        if (!email.trim().isEmpty())     cliente.setEmail(email);
        if (!direccion.trim().isEmpty()) cliente.setDireccion(direccion);

        // Actualizar en BD
        if (Databasemanager.getInstance().isConectado()) {
            String sql = "UPDATE clientes SET nombre=?, telefono=?, email=?, direccion=? WHERE id=?";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getTelefono());
                ps.setString(3, cliente.getEmail());
                ps.setString(4, cliente.getDireccion());
                ps.setInt(5,    cliente.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("✘ Error al actualizar cliente en MySQL: " + e.getMessage());
            }
        }
        System.out.println("✔ Cliente actualizado exitosamente");
        sc.close();
    }

    public void eliminarCliente(int id) {
        Cliente cliente = buscarCliente(id);
        if (cliente == null) { System.out.println("Cliente no encontrado"); return; }

        long asoc = pacientes.stream().filter(p -> p.getDueno().getId() == id).count();
        if (asoc > 0) {
            System.out.println("⚠ No se puede eliminar. El cliente tiene " + asoc + " paciente(s) asociado(s)");
            return;
        }

        if (Databasemanager.getInstance().isConectado()) {
            String sql = "DELETE FROM clientes WHERE id=?";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("✘ Error al eliminar cliente en MySQL: " + e.getMessage());
            }
        }
        clientes.remove(cliente);
        System.out.println("✔ Cliente eliminado exitosamente");
    }

    // ── PACIENTES ─────────────────────────────────────────────────────────────

    public void agregarPaciente(Paciente paciente) {
        if (Databasemanager.getInstance().isConectado()) {
            String sql = "INSERT INTO pacientes (nombre, especie, raza, edad, cliente_id) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection()
                        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, paciente.getNombre());
                ps.setString(2, paciente.getEspecie());
                ps.setString(3, paciente.getRaza());
                ps.setInt(4,    paciente.getEdad());
                ps.setInt(5,    paciente.getDueno().getId());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) setIdPaciente(paciente, rs.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("✘ Error al insertar paciente en MySQL: " + e.getMessage());
            }
        }
        pacientes.add(paciente);
        System.out.println("✔ Paciente agregado exitosamente (ID: " + paciente.getId() + ")");
    }

    public void listarPacientes() {
        if (pacientes.isEmpty()) { System.out.println("No hay pacientes registrados"); return; }
        System.out.println("\n=== LISTA DE PACIENTES ===");
        for (Paciente p : pacientes) System.out.println(p);
    }

    public Paciente buscarPaciente(int id) {
        return pacientes.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public List<Paciente> buscarPacientesPorNombre(String nombre) {
        return pacientes.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .toList();
    }

    public void editarPaciente(int id) {
        Paciente paciente = buscarPaciente(id);
        if (paciente == null) { System.out.println("Paciente no encontrado"); return; }

        System.out.println("\nPaciente actual: " + paciente);
        System.out.println("\n--- EDITAR PACIENTE (Enter para mantener valor actual) ---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Nombre ["  + paciente.getNombre()  + "]: "); String nombre   = sc.nextLine();
        System.out.print("Especie [" + paciente.getEspecie() + "]: "); String especie  = sc.nextLine();
        System.out.print("Raza ["    + paciente.getRaza()    + "]: "); String raza     = sc.nextLine();
        System.out.print("Edad ["    + paciente.getEdad()    + "]: "); String edadStr  = sc.nextLine();

        if (!nombre.trim().isEmpty())  paciente.setNombre(nombre);
        if (!especie.trim().isEmpty()) paciente.setEspecie(especie);
        if (!raza.trim().isEmpty())    paciente.setRaza(raza);
        if (!edadStr.trim().isEmpty()) {
            try { paciente.setEdad(Integer.parseInt(edadStr)); }
            catch (NumberFormatException e) { System.out.println("Edad inválida, se mantiene el valor anterior"); }
        }

        if (Databasemanager.getInstance().isConectado()) {
            String sql = "UPDATE pacientes SET nombre=?, especie=?, raza=?, edad=? WHERE id=?";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setString(1, paciente.getNombre());
                ps.setString(2, paciente.getEspecie());
                ps.setString(3, paciente.getRaza());
                ps.setInt(4,    paciente.getEdad());
                ps.setInt(5,    paciente.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("✘ Error al actualizar paciente en MySQL: " + e.getMessage());
            }
        }
        System.out.println("✔ Paciente actualizado exitosamente");
        sc.close();
    }

    public void eliminarPaciente(int id) {
        Paciente paciente = buscarPaciente(id);
        if (paciente == null) { System.out.println("Paciente no encontrado"); return; }

        // La FK con ON DELETE CASCADE elimina las citas automáticamente en BD
        if (Databasemanager.getInstance().isConectado()) {
            String sql = "DELETE FROM pacientes WHERE id=?";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("✘ Error al eliminar paciente en MySQL: " + e.getMessage());
            }
        }
        citas.removeIf(c -> c.getPaciente().getId() == id);
        pacientes.remove(paciente);
        System.out.println("✔ Paciente y sus citas eliminados exitosamente");
    }

    // ── CITAS ─────────────────────────────────────────────────────────────────

    public void agregarCita(Cita cita) {
        if (Databasemanager.getInstance().isConectado()) {
            String sql = "INSERT INTO citas (paciente_id, fecha, motivo, observaciones) VALUES (?,?,?,?)";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection()
                        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1,    cita.getPaciente().getId());
                ps.setDate(2,   cita.getFecha());
                ps.setString(3, cita.getMotivo());
                ps.setString(4, cita.getObservaciones());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) setIdCita(cita, rs.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("✘ Error al insertar cita en MySQL: " + e.getMessage());
            }
        }
        citas.add(cita);
        System.out.println("✔ Cita agendada exitosamente (ID: " + cita.getId() + ")");
    }

    public void listarCitas() {
        if (citas.isEmpty()) { System.out.println("No hay citas registradas"); return; }
        System.out.println("\n=== LISTA DE CITAS ===");
        for (Cita c : citas) System.out.println(c);
    }

    public Cita buscarCita(int id) {
        return citas.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    public void editarCita(int id) {
        Cita cita = buscarCita(id);
        if (cita == null) { System.out.println("Cita no encontrada"); return; }

        System.out.println("\nCita actual: " + cita);
        System.out.println("\n--- EDITAR CITA (Enter para mantener valor actual) ---");
        Scanner sc = new Scanner(System.in);

        System.out.print("Motivo ["        + cita.getMotivo()        + "]: "); String motivo = sc.nextLine();
        System.out.print("Observaciones [" + cita.getObservaciones() + "]: "); String obs    = sc.nextLine();

        if (!motivo.trim().isEmpty()) cita.setMotivo(motivo);
        if (!obs.trim().isEmpty())    cita.setObservaciones(obs);

        if (Databasemanager.getInstance().isConectado()) {
            String sql = "UPDATE citas SET motivo=?, observaciones=? WHERE id=?";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setString(1, cita.getMotivo());
                ps.setString(2, cita.getObservaciones());
                ps.setInt(3,    cita.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("✘ Error al actualizar cita en MySQL: " + e.getMessage());
            }
        }
        System.out.println("✔ Cita actualizada exitosamente");
        sc.close();
    }

    public void eliminarCita(int id) {
        Cita cita = buscarCita(id);
        if (cita == null) { System.out.println("Cita no encontrada"); return; }

        if (Databasemanager.getInstance().isConectado()) {
            String sql = "DELETE FROM citas WHERE id=?";
            try (PreparedStatement ps = Databasemanager.getInstance().getConnection().prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("✘ Error al eliminar cita en MySQL: " + e.getMessage());
            }
        }
        citas.remove(cita);
        System.out.println("✔ Cita eliminada exitosamente");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INFORMES
    // ══════════════════════════════════════════════════════════════════════════

    public void generarInformeCliente(int idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        if (cliente == null) { System.out.println("Cliente no encontrado"); return; }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("           INFORME DE CLIENTE");
        System.out.println("=".repeat(60));
        System.out.println("ID Cliente: " + cliente.getId());
        System.out.println("Nombre: "     + cliente.getNombre());
        System.out.println("Teléfono: "   + cliente.getTelefono());
        System.out.println("Email: "      + cliente.getEmail());
        System.out.println("Dirección: "  + cliente.getDireccion());
        System.out.println("\nPACIENTES ASOCIADOS:");
        System.out.println("-".repeat(60));
        pacientes.stream()
                .filter(p -> p.getDueno().getId() == idCliente)
                .forEach(p -> System.out.println("  • " + p.getNombre() + " (" + p.getEspecie() + ")"));
        System.out.println("=".repeat(60) + "\n");
    }

    public void generarInformePaciente(int idPaciente) {
        Paciente paciente = buscarPaciente(idPaciente);
        if (paciente == null) { System.out.println("Paciente no encontrado"); return; }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("           INFORME DE PACIENTE");
        System.out.println("=".repeat(60));
        System.out.println("ID Paciente: " + paciente.getId());
        System.out.println("Nombre: "      + paciente.getNombre());
        System.out.println("Especie: "     + paciente.getEspecie());
        System.out.println("Raza: "        + paciente.getRaza());
        System.out.println("Edad: "        + paciente.getEdad() + " años");
        System.out.println("\nDUEÑO:");
        System.out.println("-".repeat(60));
        System.out.println("  Nombre: "   + paciente.getDueno().getNombre());
        System.out.println("  Teléfono: " + paciente.getDueno().getTelefono());
        System.out.println("\nHISTORIAL DE CITAS:");
        System.out.println("-".repeat(60));
        citas.stream()
                .filter(c -> c.getPaciente().getId() == idPaciente)
                .forEach(c -> System.out.println("  • " + c.getFecha() + " - " + c.getMotivo()));
        System.out.println("=".repeat(60) + "\n");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RESPALDO — Serialización binaria (.dat)
    // ══════════════════════════════════════════════════════════════════════════

    private void guardarEnDat() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            oos.writeObject(clientes);
            oos.writeObject(pacientes);
            oos.writeObject(citas);
            oos.writeInt(Cliente.getContadorId());
            oos.writeInt(Paciente.getContadorId());
            oos.writeInt(Cita.getContadorId());
            System.out.println("✔ Respaldo .dat guardado en " + ARCHIVO_DATOS);
        } catch (IOException e) {
            System.err.println("✘ Error al guardar respaldo .dat: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void cargarDesdeDat() {
        File archivo = new File(ARCHIVO_DATOS);
        if (!archivo.exists()) { System.out.println("No hay respaldo .dat disponible"); return; }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO_DATOS))) {
            clientes  = (List<Cliente>)  ois.readObject();
            pacientes = (List<Paciente>) ois.readObject();
            citas     = (List<Cita>)     ois.readObject();
            Cliente.setContadorId(ois.readInt());
            Paciente.setContadorId(ois.readInt());
            Cita.setContadorId(ois.readInt());
            System.out.println("✔ Datos cargados desde respaldo .dat");
            System.out.println("  - Clientes: "  + clientes.size());
            System.out.println("  - Pacientes: " + pacientes.size());
            System.out.println("  - Citas: "     + citas.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✘ Error al cargar respaldo .dat: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILIDADES INTERNAS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Ajusta los contadores estáticos de ID para que la próxima entidad nueva
     * use un ID mayor que el máximo ya existente (evita colisiones).
     */
    private void sincronizarContadores() {
        clientes.stream().mapToInt(Cliente::getId).max()
                .ifPresent(max -> Cliente.setContadorId(max + 1));
        pacientes.stream().mapToInt(Paciente::getId).max()
                .ifPresent(max -> Paciente.setContadorId(max + 1));
        citas.stream().mapToInt(Cita::getId).max()
                .ifPresent(max -> Cita.setContadorId(max + 1));
    }

    // Reflection-free helpers para forzar el ID tras leer de BD.
    // Funcionan porque el constructor ya incrementó el contador; aquí
    // simplemente sobreescribimos el campo usando el setter estático de contador
    // y recreando el objeto... pero es más limpio añadir un setter de id.
    // NOTA: añade setId() a cada entidad si prefieres; aquí usamos el truco
    // de ajustar el contador ANTES de construir el objeto.
    //
    // En realidad el objeto YA está creado cuando llegamos aquí, así que
    // necesitamos reflection o un package-private setter.  Optamos por
    // añadir un método protegido en cada clase (ver nota en README).
    //
    // Alternativa temporal: usamos reflection solo internamente.
    private void setIdCliente(Cliente c, int id) {
        try {
            java.lang.reflect.Field f = Cliente.class.getDeclaredField("id");
            f.setAccessible(true);
            f.setInt(c, id);
        } catch (Exception e) { /* si falla, se usa el id del constructor */ }
    }

    private void setIdPaciente(Paciente p, int id) {
        try {
            java.lang.reflect.Field f = Paciente.class.getDeclaredField("id");
            f.setAccessible(true);
            f.setInt(p, id);
        } catch (Exception e) { /* si falla, se usa el id del constructor */ }
    }

    private void setIdCita(Cita c, int id) {
        try {
            java.lang.reflect.Field f = Cita.class.getDeclaredField("id");
            f.setAccessible(true);
            f.setInt(c, id);
        } catch (Exception e) { /* si falla, se usa el id del constructor */ }
    }

    // ── Getters de listas ─────────────────────────────────────────────────────

    public List<Cliente>  getClientes()  { return clientes;  }
    public List<Paciente> getPacientes() { return pacientes; }
    public List<Cita>     getCitas()     { return citas;     }
}