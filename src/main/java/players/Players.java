package players;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Players {
    private static String playersDir = "src/main/java/players";
    private static Player currentPlayer;

    public static class Player {
        public final String name;
        private final File file;
        private final Properties props = new Properties();
        private Map<String, String> highscores = new HashMap<>();

        public Player(String name) {
            this.name = name;
            this.file = new File(playersDir, name + ".txt");
            try {
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException ignored) {}
            load();
        }

        private void load() {
            highscores.clear();
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int idx = line.indexOf(":");
                        if (idx > 0) {
                            String game = line.substring(0, idx).trim().toLowerCase();
                            String value = line.substring(idx + 1).trim();
                            highscores.put(game, value);
                        }
                    }
                } catch (IOException ignored) {}
            }
        }

        public void save() {
            java.util.List<String> allGames = java.util.Arrays.asList("AimTrainer", "Mario", "MonkeyType", "Snake", "Speedrun", "Tetris");
            java.util.Collections.sort(allGames, String.CASE_INSENSITIVE_ORDER);
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                for (String game : allGames) {
                    String value = highscores.getOrDefault(game.toLowerCase(), "");
                    pw.println(game.toLowerCase() + ": " + value);
                }
            } catch (IOException ignored) {}
        }

        public String getHighscore(String game) {
            return highscores.getOrDefault(game.toLowerCase(), "");
        }

        public void setHighscore(String game, String value) {
            highscores.put(game.toLowerCase(), value);
            save();
        }

        public void rename(String newName) {
            File newFile = new File(playersDir, newName + ".txt");
            if (file.renameTo(newFile)) {
                props.setProperty("name", newName);
                save();
            }
        }

        public void delete() {
            file.delete();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Diese Methode muss exakt so vorhanden sein:
    public static java.util.List<String> getAllPlayerNames() {
        File dir = new File(playersDir);
        if (!dir.exists()) return new ArrayList<>();
        String[] files = dir.list((d, n) -> n.endsWith(".txt"));
        java.util.List<String> names = new ArrayList<>();
        if (files != null) {
            for (String f : files) {
                names.add(f.substring(0, f.length() - 4));
            }
        }
        return names;
    }

    public static void setCurrentPlayer(String name) {
        currentPlayer = new Player(name);
        currentPlayer.save();
    }

    public static Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Neue Methode zum Highscore-Schreiben für den aktuellen Spieler
    public static void writeHighscore(String game, int score) {
        Player player = getCurrentPlayer();
        if (player != null) {
            // Lade aktuelle Highscores aus Datei (für Robustheit)
            player.load();
            String old = player.getHighscore(game);
            int oldScore = 0;
            try {
                oldScore = Integer.parseInt(old.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            if (old.isEmpty() || oldScore < score) {
                player.setHighscore(game, score + "");
            }
        }
    }

    // --- GUI für Player-Management ---
    public static void showPlayerDialog(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Spieler-Einstellungen", true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        // Spieler-Auswahl
        JPanel selectPanel = new JPanel(new FlowLayout());
        selectPanel.add(new JLabel("Spieler:"));
        java.util.List<String> playerNames = getAllPlayerNames();
        JComboBox<String> playerBox = new JComboBox<>(playerNames.toArray(new String[0]));
        playerBox.setEditable(true);
        if (getCurrentPlayer() != null) {
            playerBox.setSelectedItem(getCurrentPlayer().name);
        }
        selectPanel.add(playerBox);

        // Highscore-Anzeige
        JTextArea highscoreArea = new JTextArea();
        highscoreArea.setEditable(false);
        highscoreArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        JScrollPane scroll = new JScrollPane(highscoreArea);
        dialog.add(scroll, BorderLayout.CENTER);

        // Methode zum Highscore-Anzeigen
        final Runnable updateHighscores = () -> {
            String name = (String) playerBox.getSelectedItem();
            if (name != null && !name.trim().isEmpty()) {
                Player p = new Player(name.trim());
                StringBuilder sb = new StringBuilder();
                sb.append("Highscores für ").append(p.name).append(":\n\n");
                sb.append("Speedrun:    ").append(p.getHighscore("Speedrun")).append("\n");
                sb.append("Snake:       ").append(p.getHighscore("Snake")).append("\n");
                sb.append("Mario:       ").append(p.getHighscore("Mario")).append("\n");
                sb.append("Tetris:      ").append(p.getHighscore("Tetris")).append("\n");
                sb.append("MonkeyType:  ").append(p.getHighscore("MonkeyType")).append("\n");
                sb.append("AimTrainer:  ").append(p.getHighscore("AimTrainer")).append("\n");
                highscoreArea.setText(sb.toString());
            } else {
                highscoreArea.setText("");
            }
        };

        JButton newBtn = new JButton("Neu");
        newBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "Neuen Spielernamen eingeben:");
            if (name != null && !name.trim().isEmpty()) {
                setCurrentPlayer(name.trim());
                playerBox.addItem(name.trim());
                playerBox.setSelectedItem(name.trim());
                updateHighscores.run();
            }
        });
        selectPanel.add(newBtn);

        JButton delBtn = new JButton("Löschen");
        delBtn.addActionListener(e -> {
            String name = (String) playerBox.getSelectedItem();
            if (name != null && !name.trim().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Spieler '" + name + "' wirklich löschen?", "Bestätigen", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Player p = new Player(name.trim());
                    p.delete();
                    playerBox.removeItem(name.trim());
                    if (getCurrentPlayer() != null && getCurrentPlayer().name.equals(name.trim())) {
                        setCurrentPlayer("");
                    }
                }
            }
        });
        selectPanel.add(delBtn);

        dialog.add(selectPanel, BorderLayout.NORTH);

        // Highscores sofort beim Öffnen anzeigen
        updateHighscores.run();

        // Highscores aktualisieren, wenn ein Spieler ausgewählt wird
        playerBox.addActionListener(e -> {
            String name = (String) playerBox.getSelectedItem();
            if (name != null && !name.trim().isEmpty()) {
                setCurrentPlayer(name.trim());
                updateHighscores.run();
            }
        });

        // --- Unteres Panel ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("Zurück");
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(closeBtn);
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(okBtn);
        // ENTER schließt das Fenster
        dialog.getRootPane().setDefaultButton(okBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}

// Die Datei wurde verschoben nach players/Players.java und ist jetzt im Package players.
