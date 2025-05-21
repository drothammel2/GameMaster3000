package games.Mario;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LevelSelection extends JFrame {

    public LevelSelection() {
        // Fenster-Einstellungen
        setTitle("Levelauswahl");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Windowed Fullscreen
        setUndecorated(true); // Entfernt Fensterrahmen
        setResizable(false);

        // Layout und Komponenten
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10)); // 4 Zeilen für Level-Buttons

        JLabel titleLabel = new JLabel("Wähle ein Level aus:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton level1Button = new JButton("Level 1");
        level1Button.addActionListener(e -> {
            dispose(); // Schließt das Levelauswahl-Fenster
            Level1.start(); // Startet die Overworld
        });

        JButton level2Button = new JButton("Level 2");
        level2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(LevelSelection.this, "Level 2 wird gestartet...");
                // Hier könnte die Logik für Level 2 aufgerufen werden
            }
        });

        JButton backButton = new JButton("Zurück");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Schließt das Levelauswahl-Fenster
                Mario.start(); // Zurück zur Haupt-GUI
            }
        });

        // Komponenten hinzufügen
        panel.add(titleLabel);
        panel.add(level1Button);
        panel.add(level2Button);
        panel.add(backButton);

        add(panel);
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            LevelSelection levelSelection = new LevelSelection();
            levelSelection.setVisible(true);
        });
    }
}
