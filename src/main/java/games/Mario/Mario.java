package games.Mario;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Mario extends JFrame {
    private static final float NORMAL_SCALE = 1.0f;
    private static final float FIRE_SCALE   = NORMAL_SCALE * 2.0f;

    private boolean hasFireFlower;

    // neu: Felder für Bild, Basissize und Hitbox-Größe
    private JLabel imageLabel;
    private ImageIcon marioIcon;
    private int baseSize     = 200;
    private int spriteWidth  = baseSize;
    private int spriteHeight = baseSize;
    private int x, y;                          // Position im Spiel

    public Mario() {
        // Fenster-Einstellungen
        setTitle("MARIO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Toolkit.getDefaultToolkit().getScreenSize()); // Fenstergröße auf Bildschirmgröße setzen
        setLocation(0, 0); // Fensterposition auf (0, 0) setzen
        setResizable(false); // Fenstergröße nicht veränderbar

        // Layout und Komponenten
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10)); // Abstand zwischen Komponenten

        // Bild hinzufügen
        imageLabel = new JLabel();
        marioIcon  = new ImageIcon(getClass().getResource("/games/Mario/mario.png"));
        updateImage();                           // setzt initiales Bild und Hitbox
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20)); // Abstand zur oberen Grenze
        imagePanel.add(imageLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10)); // Abstand zwischen Buttons

        JButton startButton = new JButton("Spiel starten");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Schließt das aktuelle Fenster
                LevelSelection.start(); // Ruft die LevelSelection-GUI auf
            }
        });

        JButton exitButton = new JButton("Beenden");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Buttons hinzufügen
        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);

        // Komponenten hinzufügen
        panel.add(imagePanel, BorderLayout.NORTH); // Bild oben
        panel.add(buttonPanel, BorderLayout.CENTER); // Buttons darunter

        add(panel);
    }

    // neu: aktualisiert Bildgröße und Hitbox
    private void updateImage() {
        float scale = hasFireFlower ? FIRE_SCALE : NORMAL_SCALE;
        int size    = (int)(baseSize * scale);
        spriteWidth = spriteHeight = size;
        Image img   = marioIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(img));
    }
    public void collectFireFlower() {
        hasFireFlower = true;
        updateImage();
    }

    // neu: liefert aktuelle Hitbox (für Kollision)
    public Rectangle getHitBox() {
        return new Rectangle(x, y, spriteWidth, spriteHeight);
    }

    // ersetze die bisherige start()-Methode
    public static void start() {
        start(false);
    }

    // neu: startet Mario im Normal- oder Feuerblumen-Modus
    public static void start(boolean fireFlowerMode) {
        SwingUtilities.invokeLater(() -> {
            Mario mainWindow = new Mario();
            if (fireFlowerMode) {
                mainWindow.collectFireFlower();
            }
            mainWindow.setVisible(true);
        });
    }

    public static void showLevelSelection() {
        // Starte das Mario-Hauptmenü oder die Levelauswahl neu
        Mario.start();
    }

    public static void main(String[] args) {
        start();
    }
}
