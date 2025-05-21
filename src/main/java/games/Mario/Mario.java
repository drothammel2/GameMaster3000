package games.Mario;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
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
        JLabel imageLabel = new JLabel();
        ImageIcon marioIcon = new ImageIcon(getClass().getResource("/games/Mario/mario.png")); // Korrigierter relativer Pfad
        Image scaledImage = marioIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH); // Bild auf 50x50 skalieren
        imageLabel.setIcon(new ImageIcon(scaledImage));
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

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Mario mainWindow = new Mario();
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
