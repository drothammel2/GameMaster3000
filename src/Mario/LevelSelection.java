package Mario;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LevelSelection extends JFrame {

    public LevelSelection() {
        // Fenster-Einstellungen
        setTitle("Levelauswahl");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Fenster zentrieren
        setResizable(false);

        // Layout und Komponenten
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10)); // 4 Zeilen für Level-Buttons

        JLabel titleLabel = new JLabel("Wähle ein Level aus:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton level1Button = new JButton("Level 1");
        level1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(LevelSelection.this, "Level 1 wird gestartet...");
                // Hier könnte die Logik für Level 1 aufgerufen werden
            }
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
