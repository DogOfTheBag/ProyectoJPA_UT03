package alber.view;

import alber.repository.Repository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaPrincipal extends JFrame {

    private final Repository repo;

    private JButton btnOp1;
    private JButton btnOp2;
    private JButton btnOp3;
    private JButton btnSalir;

    public VentanaPrincipal(Repository repo) {
        this.repo = repo;

        setTitle("Centros educativos JPA");
        setSize(650, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        initEventos();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Menú de operaciones", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));

        btnOp1 = new JButton("1) Asignaturas y profesores");
        btnOp2 = new JButton("2) Centros");
        btnOp3 = new JButton("3) Insertar asignatura en profesor");
        btnSalir = new JButton("4) Salir");

        panelBotones.add(btnOp1);
        panelBotones.add(btnOp2);
        panelBotones.add(btnOp3);
        panelBotones.add(btnSalir);

        panel.add(panelBotones, BorderLayout.CENTER);

        JLabel nota = new JLabel("Los resultados se muestran en ventanas emergentes (JOptionPane).");
        nota.setHorizontalAlignment(SwingConstants.CENTER);
        nota.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(nota, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void initEventos() {
       /* btnOp1.addActionListener(e -> {
            try {
                String resultado = repo.op1_listadoAsignaturasYProfesores();
                mostrarTexto("Operación 1", resultado);
            } catch (Exception ex) {
                mostrarError("Error en Operación 1", ex);
            }
        });

        btnOp2.addActionListener(e -> {
            try {
                String resultado = repo.op2_listadoCentros();
                mostrarTexto("Operación 2", resultado);
            } catch (Exception ex) {
                mostrarError("Error en Operación 2", ex);
            }
        });

        btnOp3.addActionListener(e -> {
            try {
                Long idAsig = pedirLong("Introduce el ID de la asignatura:");
                if (idAsig == null) return;

                Long idProf = pedirLong("Introduce el ID del profesor:");
                if (idProf == null) return;

                String msg = repo.op3_insertarAsignaturaEnProfesor(idAsig, idProf);
                JOptionPane.showMessageDialog(this, msg, "Operación 3", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                mostrarError("Error en Operación 3", ex);
            }
        });

        btnSalir.addActionListener(e -> dispose());

        // Si quieres hacer algo al cerrar (opcional, normalmente lo hace el main)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Aquí NO cierres el EntityManagerFactory.
                // El cierre recomendado se hace en el main.
            }
        });
    }

    private Long pedirLong(String mensaje) {
        String input = JOptionPane.showInputDialog(this, mensaje);
        if (input == null) return null; // canceló

        input = input.trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No has introducido nada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        try {
            return Long.parseLong(input);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Debe ser un número (Long).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void mostrarTexto(String titulo, String texto) {
        JTextArea area = new JTextArea(texto, 22, 70);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(area);

        JOptionPane.showMessageDialog(this, scroll, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, Exception ex) {
        String msg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        JOptionPane.showMessageDialog(this, msg, titulo, JOptionPane.ERROR_MESSAGE);
    }*/
    }
}
