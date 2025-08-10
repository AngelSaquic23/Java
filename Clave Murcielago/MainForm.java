package textanalyzer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;

public class MainForm extends JFrame {
    private JTextArea textArea, outputArea;
    private JLabel lblChars, lblWords, lblLines, lblVowels, lblConsonants, lblSentences, lblMostFreq, lblAvgLen;
    private JLabel lblFirstLetter, lblLastLetter, lblMiddleLetter;
    private JLabel lblFirstWord, lblMiddleWord, lblLastWord;
    private JLabel lblVowelA, lblVowelE, lblVowelI, lblVowelO, lblVowelU;
    private JLabel lblEvenWords, lblOddWords;
    private File currentFile;

    private static final Map<Character, Character> MURCIELAGO_MAP = createMurcielagoMap();

    public MainForm() {
        setTitle("Text Analyzer - Murciélago Cipher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        initUI();
    }

    private static Map<Character, Character> createMurcielagoMap() {
        Map<Character, Character> map = new HashMap<>();
        String keys = "murcielago";
        String values = "0123456789";
        for (int i = 0; i < keys.length(); i++) {
            map.put(keys.charAt(i), values.charAt(i));
        }
        return Collections.unmodifiableMap(map);
    }

    private void initUI() {
        setJMenuBar(createMenuBar());

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Área de texto
        textArea = createTextArea(true);
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY, 1), "Área de texto"));
        mainPanel.add(textScroll, BorderLayout.NORTH);

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton btnProcess = new JButton("Procesar");
        JButton btnClear = new JButton("Limpiar");
        btnPanel.add(btnProcess);
        btnPanel.add(btnClear);
        centerPanel.add(btnPanel, BorderLayout.NORTH);

        btnProcess.addActionListener(e -> processText());
        btnClear.addActionListener(e -> clearAll());

        // Estadísticas
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel leftStats = new JPanel(new GridLayout(6, 2, 4, 4));
        lblChars = new JLabel("Caracteres: 0");
        lblWords = new JLabel("Palabras: 0");
        lblLines = new JLabel("Líneas: 0");
        lblVowels = new JLabel("Vocales: 0");
        lblConsonants = new JLabel("Consonantes: 0");
        lblSentences = new JLabel("Oraciones: 0");
        lblMostFreq = new JLabel("Más frecuente: -");
        lblAvgLen = new JLabel("Promedio palabra: 0.0");

        leftStats.add(lblChars);
        leftStats.add(lblWords);
        leftStats.add(lblLines);
        leftStats.add(lblVowels);
        leftStats.add(lblConsonants);
        leftStats.add(lblSentences);
        leftStats.add(lblMostFreq);
        leftStats.add(lblAvgLen);

        // Letras y palabras específicas
        lblFirstLetter = new JLabel("Primera letra: -");
        lblMiddleLetter = new JLabel("Letra central: -");
        lblLastLetter = new JLabel("Última letra: -");
        lblFirstWord = new JLabel("Primera palabra: -");
        lblMiddleWord = new JLabel("Palabra central: -");
        lblLastWord = new JLabel("Última palabra: -");

        leftStats.add(lblFirstLetter);
        leftStats.add(lblMiddleLetter);
        leftStats.add(lblLastLetter);
        leftStats.add(lblFirstWord);
        leftStats.add(lblMiddleWord);
        leftStats.add(lblLastWord);

        JPanel rightStats = new JPanel(new GridLayout(7, 1, 4, 4));
        lblVowelA = new JLabel("Repeticiones de A/a/á: 0");
        lblVowelE = new JLabel("Repeticiones de E/e/é: 0");
        lblVowelI = new JLabel("Repeticiones de I/i/í: 0");
        lblVowelO = new JLabel("Repeticiones de O/o/ó: 0");
        lblVowelU = new JLabel("Repeticiones de U/u/ú: 0");
        lblEvenWords = new JLabel("Palabras con caracteres pares: 0");
        lblOddWords = new JLabel("Palabras con caracteres impares: 0");

        rightStats.add(lblVowelA);
        rightStats.add(lblVowelE);
        rightStats.add(lblVowelI);
        rightStats.add(lblVowelO);
        rightStats.add(lblVowelU);
        rightStats.add(lblEvenWords);
        rightStats.add(lblOddWords);

        statsPanel.add(leftStats);
        statsPanel.add(rightStats);

        centerPanel.add(statsPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Área de salida
        outputArea = createTextArea(false);
        outputArea.setBackground(new Color(245, 245, 245));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY, 1), "Traducción (Murciélago)"));
        mainPanel.add(outputScroll, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Archivo");
        fileMenu.add(createMenuItem("Abrir", KeyEvent.VK_O, e -> openFile()));
        fileMenu.add(createMenuItem("Guardar", KeyEvent.VK_S, e -> saveFile(false)));
        fileMenu.add(createMenuItem("Guardar como", 0, e -> saveFile(true)));

        JMenu editMenu = new JMenu("Editar");
        editMenu.add(createMenuItem("Copiar", KeyEvent.VK_C, e -> textArea.copy()));
        editMenu.add(createMenuItem("Cortar", KeyEvent.VK_X, e -> textArea.cut()));
        editMenu.add(createMenuItem("Pegar", KeyEvent.VK_V, e -> textArea.paste()));
        editMenu.addSeparator();
        editMenu.add(createMenuItem("Buscar", KeyEvent.VK_F, e -> findText()));
        editMenu.add(createMenuItem("Reemplazar", KeyEvent.VK_H, e -> replaceText()));

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        return menuBar;
    }

    private JMenuItem createMenuItem(String text, int keyEvent, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        if (keyEvent != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, InputEvent.CTRL_DOWN_MASK));
        }
        item.addActionListener(listener);
        return item;
    }

    private JTextArea createTextArea(boolean editable) {
        JTextArea area = new JTextArea(10, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(editable);
        area.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return area;
    }

    private void clearAll() {
        textArea.setText("");
        outputArea.setText("");
        resetStats();
        currentFile = null;
        setTitle("Text Analyzer - Murciélago Cipher");
    }

    private void resetStats() {
        lblChars.setText("Caracteres: 0");
        lblWords.setText("Palabras: 0");
        lblLines.setText("Líneas: 0");
        lblVowels.setText("Vocales: 0");
        lblConsonants.setText("Consonantes: 0");
        lblSentences.setText("Oraciones: 0");
        lblMostFreq.setText("Más frecuente: -");
        lblAvgLen.setText("Promedio palabra: 0.0");
        lblFirstLetter.setText("Primera letra: -");
        lblMiddleLetter.setText("Letra central: -");
        lblLastLetter.setText("Última letra: -");
        lblFirstWord.setText("Primera palabra: -");
        lblMiddleWord.setText("Palabra central: -");
        lblLastWord.setText("Última palabra: -");
        lblVowelA.setText("Repeticiones de A/a/á: 0");
        lblVowelE.setText("Repeticiones de E/e/é: 0");
        lblVowelI.setText("Repeticiones de I/i/í: 0");
        lblVowelO.setText("Repeticiones de O/o/ó: 0");
        lblVowelU.setText("Repeticiones de U/u/ú: 0");
        lblEvenWords.setText("Palabras con caracteres pares: 0");
        lblOddWords.setText("Palabras con caracteres impares: 0");
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                String content = Files.readString(currentFile.toPath(), StandardCharsets.UTF_8);
                textArea.setText(content);
                setTitle("Text Analyzer - " + currentFile.getName());
            } catch (IOException ex) {
                showError("Error al abrir el archivo", ex);
            }
        }
    }

    private void saveFile(boolean saveAs) {
        if (!saveAs && currentFile != null) {
            writeFile(currentFile);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            writeFile(currentFile);
        }
    }

    private void writeFile(File file) {
        try {
            Files.writeString(file.toPath(), textArea.getText(), StandardCharsets.UTF_8);
            setTitle("Text Analyzer - " + file.getName());
            JOptionPane.showMessageDialog(this, "Archivo guardado: " + file.getAbsolutePath());
        } catch (IOException ex) {
            showError("Error al guardar el archivo", ex);
        }
    }

    private void findText() {
        String term = JOptionPane.showInputDialog(this, "Ingrese texto a buscar:");
        if (term == null || term.isEmpty()) return;
        highlightOccurrences(term);
    }

    private void highlightOccurrences(String term) {
        try {
            Highlighter highlighter = textArea.getHighlighter();
            highlighter.removeAllHighlights();
            String content = textArea.getText().toLowerCase();
            term = term.toLowerCase();
            int index = 0;
            while ((index = content.indexOf(term, index)) >= 0) {
                highlighter.addHighlight(index, index + term.length(),
                        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                index += term.length();
            }
        } catch (BadLocationException ignored) {}
    }

    private void replaceText() {
        JTextField find = new JTextField();
        JTextField replace = new JTextField();
        Object[] message = {"Buscar:", find, "Reemplazar por:", replace};
        if (JOptionPane.showConfirmDialog(this, message, "Reemplazar", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            textArea.setText(textArea.getText().replace(find.getText(), replace.getText()));
        }
    }

    private void processText() {
        String content = textArea.getText();
        lblChars.setText("Caracteres: " + content.length());

        String[] linesArr = content.split("\\R");
        lblLines.setText("Líneas: " + (content.isBlank() ? 0 : linesArr.length));

        String[] wordsArr = content.trim().split("\\s+");
        List<String> wordsList = Arrays.asList(wordsArr);
        lblWords.setText("Palabras: " + (content.isBlank() ? 0 : wordsArr.length));

        lblVowels.setText("Vocales: " + countMatches(content, "[aeiouáéíóúüAEIOUÁÉÍÓÚÜ]"));
        lblConsonants.setText("Consonantes: " + countMatches(content, "[bcdfghjklmnpqrstvwxyzñBCDFGHJKLMNPQRSTVWXYZÑ]"));
        lblSentences.setText("Oraciones: " + countMatches(content, "[.!?]"));

        String mostFreq = wordsList.stream()
                .map(w -> w.replaceAll("\\W", "").toLowerCase())
                .filter(w -> !w.isEmpty())
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
        lblMostFreq.setText("Más frecuente: " + mostFreq);

        double avgLen = wordsList.stream()
                .map(w -> w.replaceAll("\\W", ""))
                .filter(w -> !w.isEmpty())
                .mapToInt(String::length)
                .average().orElse(0.0);
        lblAvgLen.setText(String.format("Promedio palabra: %.2f", avgLen));

        // Letras y palabras específicas
        if (!content.isBlank()) {
            lblFirstLetter.setText("Primera letra: " + content.charAt(0));
            lblLastLetter.setText("Última letra: " + content.charAt(content.length() - 1));
            lblMiddleLetter.setText("Letra central: " + content.charAt(content.length() / 2));

            lblFirstWord.setText("Primera palabra: " + wordsArr[0]);
            lblLastWord.setText("Última palabra: " + wordsArr[wordsArr.length - 1]);
            lblMiddleWord.setText("Palabra central: " + wordsArr[wordsArr.length / 2]);
        }

        // Repeticiones de vocales
        lblVowelA.setText("Repeticiones de A/a/á: " + countMatches(content, "[aáAÁ]"));
        lblVowelE.setText("Repeticiones de E/e/é: " + countMatches(content, "[eéEÉ]"));
        lblVowelI.setText("Repeticiones de I/i/í: " + countMatches(content, "[iíIÍ]"));
        lblVowelO.setText("Repeticiones de O/o/ó: " + countMatches(content, "[oóOÓ]"));
        lblVowelU.setText("Repeticiones de U/u/ú: " + countMatches(content, "[uúUÚ]"));

        // Palabras pares e impares
        long even = wordsList.stream().filter(w -> w.length() % 2 == 0).count();
        long odd = wordsList.size() - even;
        lblEvenWords.setText("Palabras con caracteres pares: " + even);
        lblOddWords.setText("Palabras con caracteres impares: " + odd);

        outputArea.setText(murcielagoTranslate(content));
    }

    private int countMatches(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    private String murcielagoTranslate(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            char low = Character.toLowerCase(ch);
            sb.append(MURCIELAGO_MAP.getOrDefault(low, ch));
        }
        return sb.toString();
    }

    private void showError(String msg, Exception ex) {
        JOptionPane.showMessageDialog(this, msg + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
        SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
    }
}
