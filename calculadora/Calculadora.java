package calculadora;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;

public class Calculadora extends JFrame implements ActionListener, KeyListener {
    JTextField display;
    boolean nuevaOperacion = true;
    boolean error = false;
    File bitacora = new File("bitacoraCalculadora.txt");

    public Calculadora() {
        setTitle("Calculadora JAVA");
        setSize(350, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        display = new JTextField("0");
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        add(display, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(5, 4, 5, 5));
        String[] botones = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "√", "^", "%", "C"
        };
        for (String texto : botones) {
            JButton btn = new JButton(texto);
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.addActionListener(this);
            panelBotones.add(btn);
        }

        add(panelBotones, BorderLayout.CENTER);
        crearMenu();

        display.addKeyListener(this);
        setVisible(true);
    }

    private void crearMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuOpciones = new JMenu("Opciones");
        JMenuItem nuevo = new JMenuItem("Nuevo");
        nuevo.addActionListener(e -> reiniciarCalculadora());

        JMenuItem historial = new JMenuItem("Historial");
        historial.addActionListener(e -> mostrarHistorial());

        menuOpciones.add(nuevo);
        menuOpciones.add(historial);

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem ayuda = new JMenuItem("Manual de Usuario");
        ayuda.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(Calculadora.this, """
                                                                        Manual de Usuario:
                                                                        - Utiliza los botones o teclado.
                                                                        - Presiona '=' para calcular.
                                                                        - Usa el men\u00fa para ver historial o reiniciar.""");
        });

        menuAyuda.add(ayuda);

        menuBar.add(menuOpciones);
        menuBar.add(menuAyuda);
        setJMenuBar(menuBar);
    }

    private void reiniciarCalculadora() {
        display.setText("0");
        nuevaOperacion = true;
        error = false;
        guardarEnBitacora("Nuevo");
    }

    private void mostrarHistorial() {
        JTextArea area = new JTextArea(20, 30);
        area.setEditable(false);
        try (BufferedReader br = new BufferedReader(new FileReader(bitacora))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                area.append(linea + "\n");
            }
        } catch (IOException ex) {
            area.setText("No hay historial disponible.");
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Historial de Operaciones", JOptionPane.INFORMATION_MESSAGE);
    }

    private void guardarEnBitacora(String texto) {
        try (FileWriter fw = new FileWriter(bitacora, true)) {
            fw.write(texto + "\n");
        } catch (IOException e) {
            System.err.println("Error al guardar en bitácora.");
        }
    }

    private boolean esNumeroValido(String texto) {
        try {
            return !texto.equals(".");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (error) return; // evita operaciones si hay error

        String cmd = e.getActionCommand();
        Toolkit.getDefaultToolkit().beep();

        if (cmd.matches("[0-9]") || cmd.equals(".")) {
            if (nuevaOperacion || display.getText().equals("0")) {
                display.setText(cmd);
                nuevaOperacion = false;
            } else {
                display.setText(display.getText() + cmd);
            }
        } else if (cmd.matches("[\\+\\-\\*/%\\^]")) {
            String textoActual = display.getText();

            // Si ya hay operador al final, reemplazarlo
            if (textoActual.matches(".*[\\+\\-\\*/%\\^]$")) {
                display.setText(textoActual.substring(0, textoActual.length() - 1) + cmd);
            } else {
                display.setText(textoActual + cmd);
            }
            nuevaOperacion = false;
        } else if (cmd.equals("√")) {
            String texto = display.getText();
            // Solo aplica raíz si es un número válido (sin operador)
            if (texto.matches(".*[\\+\\-\\*/%\\^].*")) {
                mostrarError("Syntax Error");
                return;
            }
            if (!esNumeroValido(texto)) {
                mostrarError("Syntax Error");
                return;
            }
            double valor = Double.parseDouble(texto);
            if (valor < 0) {
                mostrarError("Math Error");
                return;
            }
            double res = Math.sqrt(valor);
            display.setText(format(res));
            guardarEnBitacora("√" + texto + " = " + format(res));
            nuevaOperacion = true;
        } else if (cmd.equals("=")) {
            String expr = display.getText();

            // Buscar operador
            String operadorEncontrado = "";
            int posOperador = -1;

            for (int i = 0; i < expr.length(); i++) {
                char ch = expr.charAt(i);
                if ("+-*/%^".indexOf(ch) >= 0) {
                    operadorEncontrado = String.valueOf(ch);
                    posOperador = i;
                    break;
                }
            }

            if (operadorEncontrado.isEmpty() || posOperador == -1) {
                mostrarError("Syntax Error");
                return;
            }

            try {
                double op1 = Double.parseDouble(expr.substring(0, posOperador));
                double op2 = Double.parseDouble(expr.substring(posOperador + 1));
                double res = 0;

                switch (operadorEncontrado) {
                    case "+":
                        res = op1 + op2;
                        break;
                    case "-":
                        res = op1 - op2;
                        break;
                    case "*":
                        res = op1 * op2;
                        break;
                    case "/":
                        if (op2 == 0) {
                            mostrarError("Math Error");
                            return;
                        }
                        res = op1 / op2;
                        break;
                    case "%":
                        res = op1 * op2 / 100;
                        break;
                    case "^":
                        res = Math.pow(op1, op2);
                        break;
                }

                display.setText(format(res));
                guardarEnBitacora(expr + " = " + format(res));
                nuevaOperacion = true;
            } catch (NumberFormatException ex) {
                mostrarError("Syntax Error");
            }
        } else if (cmd.equals("C")) {
            reiniciarCalculadora();
        }
    }

    private void mostrarError(String mensaje) {
        display.setText(mensaje);
        nuevaOperacion = true;
        error = true;
        guardarEnBitacora(mensaje);
    }

    private String format(double num) {
        DecimalFormat df = new DecimalFormat("0.######");
        return df.format(num);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No usado
    }

    @Override
    public void keyPressed(KeyEvent e) {
        char c = e.getKeyChar();
        if (Character.isDigit(c) || "+-*/.=^%√C".contains(String.valueOf(c))) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No usado
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculadora());
    }
}
