package games.Mario;

import java.awt.*;
import javax.swing.*;

public class Level1 extends JFrame {

    public Level1() {
        // Fenster-Einstellungen
        setTitle("Level 1 - Overworld");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Windowed Fullscreen
        setUndecorated(true); // Entfernt Fensterrahmen
        setResizable(false);

        System.out.println("Level1 wird im Windowed Fullscreen gestartet."); // Debug-Ausgabe

        // Panel für das Level
        JPanel levelPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Hintergrundbild
                 ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/games/Mario/background.jpg")); // Hintergrundbild laden
                if (backgroundIcon.getImage() != null) {
                    Image backgroundImage = backgroundIcon.getImage();
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null); // Hintergrund zuerst zeichnen
                } else {
                    System.err.println("Fehler: Bild 'background.jpg' konnte nicht geladen werden.");
                }

                // Boden mit Blöcken
                ImageIcon blockIcon = new ImageIcon(getClass().getResource("/games/Mario/overworldblock.png")); // Block-Bild laden
                if (blockIcon.getImage() != null) {
                    Image blockImage = blockIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Block skalieren

                    // Zeichne die Bodenblöcke in einer Reihe am unteren Rand
                    int blockHeight = 50; // Höhe des Blocks
                    int blockY = getHeight() - blockHeight; // Y-Position der Bodenreihe
                    for (int x = 0; x < getWidth(); x += 50) {
                        g.drawImage(blockImage, x, blockY, null); // Blöcke über dem Hintergrund zeichnen
                    }
                } else {
                    System.err.println("Fehler: Bild 'overworldblock.png' konnte nicht geladen werden.");
                }
            }
        };

        add(levelPanel); // Panel zum Fenster hinzufügen
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Level1 level1 = new Level1();
            level1.setVisible(true);
        });
    }
}
