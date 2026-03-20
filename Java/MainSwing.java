package Java;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * MainSwing — Punto de entrada para la interfaz gráfica Swing
 * 
 * Este archivo reemplaza el Main.java original que usaba la terminal.
 * Abre la ventana gráfica de VetManager Pro.
 */
public class MainSwing {
    public static void main(String[] args) {
        // Intentar usar el Look & Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, usar el Look & Feel por defecto de Java
            System.err.println("No se pudo aplicar el Look & Feel del sistema: " + e.getMessage());
        }
        
        // Iniciar la interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear el gestor (modelo de datos)
            GestorVeterinaria gestor = new GestorVeterinaria();
            
            // Cargar datos desde MySQL (o .dat si no está disponible)
            gestor.cargarDatos();
            
            // Crear y mostrar la ventana
            VentanaPrincipal ventana = new VentanaPrincipal(gestor);
            ventana.setVisible(true);
            
            System.out.println("✓ Interfaz gráfica iniciada correctamente");
        });
    }
}