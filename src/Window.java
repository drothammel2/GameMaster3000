import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Arrays;

public class Window {
    public interface GameSelectionListener {
        void onGameSelected(String gameName);
    }

    public static void startMainscreen(GameSelectionListener listener) {
        // Standardspiele, kann später erweitert werden
        startMainscreen(listener, Arrays.asList("Mario", "Snake"));
    }

    public static void startMainscreen(GameSelectionListener listener, List<String> gameNames) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GameMaster3000");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Vollbild
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JLabel label = new JLabel("Wähle ein Spiel:", JLabel.CENTER);

            JPanel buttonPanel = new JPanel();
            for (String gameName : gameNames) {
                JButton button = new JButton(gameName);
                button.setPreferredSize(new java.awt.Dimension(300, 120));
                button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 36));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        frame.dispose();
                        listener.onGameSelected(gameName);
                    }
                });
                buttonPanel.add(button);
            }

            frame.setLayout(new BorderLayout());
            frame.add(label, BorderLayout.NORTH);
            frame.add(buttonPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
