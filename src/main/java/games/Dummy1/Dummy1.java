package games.Dummy1;

import javax.swing.*;

public class Dummy1 {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dummy1");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JLabel label = new JLabel("DIES IST NUR DER DUMMY1", SwingConstants.CENTER);
            label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
            frame.add(label);
            frame.setSize(600, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
