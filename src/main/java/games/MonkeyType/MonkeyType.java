package games.MonkeyType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class MonkeyType {
    public static void start(Runnable onExitToMenu) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MonkeyType");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);

            // Wörter laden
            final List<String> words = loadWords();
            final Random random = new Random();

            // Wort-Queue für aktuelle und nächste 5 Wörter
            final LinkedList<String> wordQueue = new LinkedList<>();
            for (int i = 0; i < 6; i++) {
                wordQueue.add(words.get(random.nextInt(words.size())));
            }

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.BLACK);

            // Wörter-Anzeige (aktuell + nächste 5)
            final JLabel wordsLabel = new JLabel("", SwingConstants.CENTER);
            wordsLabel.setFont(new Font("Consolas", Font.BOLD, 48));
            wordsLabel.setForeground(Color.ORANGE);
            updateWordsLabel(wordsLabel, wordQueue);
            mainPanel.add(wordsLabel, BorderLayout.NORTH);

            // Panel für das Textfeld (zentriert)
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);

            final JTextField textField = new JTextField(40);
            textField.setFont(new Font("Consolas", Font.BOLD, 48));
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBackground(Color.WHITE);
            textField.setForeground(Color.BLACK);
            textField.setCaretColor(Color.BLUE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            centerPanel.add(textField, gbc);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            // Counter-Anzeige
            final JLabel correctLabel = new JLabel("Richtige Wörter: 0");
            final JLabel wrongLabel = new JLabel("Falsche Buchstaben: 0");
            final JLabel speedLabel = new JLabel("Zeichen/Minute: 0");
            correctLabel.setFont(new Font("Arial", Font.BOLD, 32));
            wrongLabel.setFont(new Font("Arial", Font.BOLD, 32));
            speedLabel.setFont(new Font("Arial", Font.BOLD, 32));
            correctLabel.setForeground(Color.GREEN);
            wrongLabel.setForeground(Color.RED);
            speedLabel.setForeground(Color.CYAN);

            JPanel counterPanel = new JPanel(new GridLayout(1, 3));
            counterPanel.setOpaque(false);
            counterPanel.add(correctLabel);
            counterPanel.add(wrongLabel);
            counterPanel.add(speedLabel);

            mainPanel.add(counterPanel, BorderLayout.SOUTH);

            // Counter-Variablen
            final int[] correctWords = {0};
            final int[] wrongChars = {0};

            // Für Zeichen pro Minute
            final long[] startTime = {System.currentTimeMillis()};
            final int[] totalTypedChars = {0};
            final int[] lastTypedChars = {0};

            // Timer für Zeichen/Minute
            javax.swing.Timer speedTimer = new javax.swing.Timer(500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    long elapsed = System.currentTimeMillis() - startTime[0];
                    if (elapsed < 1000) {
                        speedLabel.setText("Zeichen/Minute: 0");
                        return;
                    }
                    int chars = totalTypedChars[0];
                    double minutes = elapsed / 60000.0;
                    int zpm = (int) Math.round(chars / minutes);
                    speedLabel.setText("Zeichen/Minute: " + zpm);
                }
            });
            speedTimer.start();

            // Pause-Status
            final boolean[] paused = {false};

            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    // ESC öffnet das Pause-Menü
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !paused[0]) {
                        paused[0] = true;
                        showPauseMenu(frame, paused, speedTimer, onExitToMenu);
                        return;
                    }
                    // SPACE wertet das Wort aus und zeigt das nächste
                    if (e.getKeyCode() == KeyEvent.VK_SPACE && !paused[0]) {
                        String typed = textField.getText().trim();
                        String currentWord = wordQueue.getFirst();
                        if (typed.equals(currentWord)) {
                            correctWords[0]++;
                            correctLabel.setText("Richtige Wörter: " + correctWords[0]);
                        } else {
                            // Falsche Buchstaben zählen (Vergleich Zeichen für Zeichen)
                            int minLen = Math.min(typed.length(), currentWord.length());
                            int wrong = 0;
                            for (int i = 0; i < minLen; i++) {
                                if (typed.charAt(i) != currentWord.charAt(i)) wrong++;
                            }
                            wrong += Math.abs(typed.length() - currentWord.length());
                            wrongChars[0] += wrong;
                            wrongLabel.setText("Falsche Buchstaben: " + wrongChars[0]);
                        }
                        // Wort-Queue updaten: erstes entfernen, neues anhängen
                        wordQueue.removeFirst();
                        wordQueue.add(words.get(random.nextInt(words.size())));
                        updateWordsLabel(wordsLabel, wordQueue);
                        textField.setText("");
                        e.consume();
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    // Zähle nur sichtbare Zeichen (keine Steuerzeichen)
                    char ch = e.getKeyChar();
                    if (!Character.isISOControl(ch) && ch != ' ' && !paused[0]) {
                        totalTypedChars[0]++;
                    }
                }
            });

            frame.add(mainPanel);
            frame.setVisible(true);

            // Fokus direkt auf das Textfeld setzen
            SwingUtilities.invokeLater(textField::requestFocusInWindow);
        });
    }

    private static void showPauseMenu(JFrame parent, boolean[] paused, javax.swing.Timer speedTimer, Runnable onExitToMenu) {
        JDialog pauseDialog = new JDialog(parent, "Pause", true);
        pauseDialog.setLayout(new BorderLayout());
        JLabel label = new JLabel("Pause - Drücke K für Hauptmenü, ESC für weiter", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        pauseDialog.add(label, BorderLayout.CENTER);
        pauseDialog.setSize(500, 200);
        pauseDialog.setLocationRelativeTo(parent);
        pauseDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_K) {
                    pauseDialog.dispose();
                    parent.dispose();
                    speedTimer.stop();
                    paused[0] = false;
                    if (onExitToMenu != null) onExitToMenu.run();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    pauseDialog.dispose();
                    paused[0] = false;
                }
            }
        });
        pauseDialog.setFocusable(true);
        pauseDialog.setVisible(true);
        pauseDialog.requestFocusInWindow();
    }

    private static void updateWordsLabel(JLabel label, LinkedList<String> wordQueue) {
        // Erstes Wort fett, die nächsten 5 normal
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<span style='color:orange'><b>").append(wordQueue.get(0)).append("</b></span>");
        for (int i = 1; i < wordQueue.size(); i++) {
            sb.append(" <span style='color:gray'>").append(wordQueue.get(i)).append("</span>");
        }
        sb.append("</html>");
        label.setText(sb.toString());
    }

    private static List<String> loadWords() {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        MonkeyType.class.getResourceAsStream("MonkeyTypeWords.txt"),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) words.add(trimmed);
            }
        } catch (Exception e) {
            // Fehler beim Laden, gib leere Liste zurück
        }
        return words.isEmpty() ? Arrays.asList("Fehler", "beim", "Laden", "der", "Wörter", "Test") : words;
    }
}
