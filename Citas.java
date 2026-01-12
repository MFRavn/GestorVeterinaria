import java.io.Serializable;
import java.sql.Date;

class Cita implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contadorId = 1;
    private int id;
    private Paciente paciente;
    private Date fecha;
    private String motivo;
    private String observaciones;

    public Cita(Paciente paciente, Date fecha, String motivo, String observaciones) {
        this.id = contadorId++;
        this.paciente = paciente;
        this.fecha = fecha;
        this.motivo = motivo;
        this.observaciones = observaciones;
    }

    public int getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public static void setContadorId(int contador) {
        contadorId = contador;
    }

    public static int getContadorId() {
        return contadorId;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Fecha: %s | Paciente: %s | Motivo: %s",
                id, fecha, paciente.getNombre(), motivo);
    }
}
