package games.Dummy2;

import javax.swing.*;

public class Dummy2 {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dummy2");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JLabel label = new JLabel("DIES IST NUR DER DUMMY2", SwingConstants.CENTER);
            label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
            frame.add(label);
            frame.setSize(600, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
