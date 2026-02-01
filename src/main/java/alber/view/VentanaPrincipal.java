package alber.view;

import alber.repository.Repository;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final Repository repo;

    private JButton btnOp1;
    private JButton btnOp2;
    private JButton btnOp3;
    private JButton btnSalir;

    /*Recibimos por el constructor de la ventana el repo, cremaos ventana, e inicializamos UI y eventos*/
    public VentanaPrincipal(Repository repo) {
        this.repo = repo;

        setTitle("Centros educativos JPA");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        initEventos();
    }

    private void initUI() {
        /*Border layout porque si, colocamos el titulo al norte y los botones al centro, le damos unos bordes chulos*/
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Menú de operaciones", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 10, 10));

        btnOp1 = new JButton("1. Asignaturas y profesores");
        btnOp2 = new JButton("2. Centros");
        btnOp3 = new JButton("3. Insertar asignatura en profesor");
        btnSalir = new JButton("4. Salir");

        panelBotones.add(btnOp1);
        panelBotones.add(btnOp2);
        panelBotones.add(btnOp3);
        panelBotones.add(btnSalir);

        panel.add(panelBotones, BorderLayout.CENTER);


        setContentPane(panel);
    }

    /*El inicializador de eventos, en cada operacion cogemos el texto que nos devuelva cada metodo del repo
    * y usando el metodo auxiliar de mostrar texto mostramos el resultado*/
    private void initEventos() {
        btnOp1.addActionListener(e -> {
            try {
                String resultado = repo.listarProf_Asig();
                mostrarTexto("Operación 1", resultado);
            } catch (Exception ex) {
                mostrarError("Error en Operación 1", ex);
            }
        });

        btnOp2.addActionListener(e -> {
            try {
                String resultado = repo.listarCentros();
                mostrarTexto("Operación 2", resultado);
            } catch (Exception ex) {
                mostrarError("Error en Operación 2", ex);
            }
        });

        /*En este igual salvo que pedimos primero los parámetros, y luego lanzamos una ventana modal normal ya que aqui
        * no necesitamos tanto texto*/
        btnOp3.addActionListener(e -> {
            try {
                Long idAsig = pedirLong("Introduce el ID de la asignatura:");
                if (idAsig == null) return;

                Long idProf = pedirLong("Introduce el ID del profesor:");
                if (idProf == null) return;

                String mensaje = repo.insertarAsignaturaEnProfesor(idAsig, idProf);
                JOptionPane.showMessageDialog(this, mensaje, "Operación 3", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                mostrarError("Error en Operación 3", ex);
            }
        });

        btnSalir.addActionListener(e -> dispose());

    }

    /****MÉTODOS AUXILIARES****/
    /*Metodo para pedir los ids, les quitamos los espacios para ver que no esta vacio y comprobamos si es null*/
    private Long pedirLong(String mensaje) {
        String input = JOptionPane.showInputDialog(this, mensaje);
        if (input == null) return null;

        input = input.trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No has introducido nada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        //si ha metido algo lo parseamos a Long y ya lo devolvemos
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Debe ser un número (Long).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    /*Para mostrar en ventanas modales el texto de las querys, añadiré un area de texto scrolleable a la ventana modal,
    * y lo guardo en un metodo que reciba el titulo de la ventana, el texto a introducir, y que me valga para las dos operaciones
    * de la misma forma sin tener que crear una ventana completamente*/
    private void mostrarTexto(String titulo, String texto) {
        /*Primero hacemos el area de texto con el texto de la query, hacemos que no sea editable, y que si hubiera
        * una linea muy larga se corte y salte de linea, y que esto lo haga al acabar una palabra y no en el medio de una*/
        JTextArea area = new JTextArea(texto, 22, 70);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        //una vez hecho el textArea, lo convertimos en una panel scrolleable, y se lo pasamos a la ventana modal
        JScrollPane scroll = new JScrollPane(area);

        /*Le puedo pasar un mensaje de texto normal y un scrollPane ya que el constructor pide un objeto, no solo mensajes*/
        JOptionPane.showMessageDialog(this, scroll, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /*Cada vez que haya un error al hacer una operacion pasamos esto en vez de hacer 3 veces JOptionPane*/
    private void mostrarError(String titulo, Exception ex) {
        String mensaje = ex.getMessage();
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
    /****MÉTODOS AUXILIARES****/
}

