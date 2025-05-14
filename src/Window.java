import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Arrays;

public class Window {
    public interface GameSelectionListener {
        void onGameSelected(String gameName);
    }

    public static void startMainscreen(GameSelectionListener listener) {
        startMainscreen(listener, Arrays.asList("Mario", "Snake", "Tetris", "Dummy1", "Dummy2"));
    }

    public static void startMainscreen(GameSelectionListener listener, List<String> gameNames) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GameMaster3000");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Vollbild
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JLabel label = new JLabel("Wähle ein Spiel:", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 48));

            int numGames = gameNames.size();
            // Dynamische Berechnung der Grid-Größe (quadratisch oder rechteckig)
            int cols = (int)Math.ceil(Math.sqrt(numGames));
            int rows = (int)Math.ceil((double)numGames / cols);

            JPanel buttonPanel = new JPanel(new GridLayout(rows, cols, 40, 40));
            buttonPanel.setOpaque(false);

            // Button-Größe dynamisch anpassen
            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            int buttonWidth = (screenSize.width - (cols + 1) * 40) / cols;
            int buttonHeight = (screenSize.height - 200 - (rows + 1) * 40) / rows; // 200 für Label und Rand

            int fontSize = Math.min(buttonWidth, buttonHeight) / 8 + 18;

            for (String gameName : gameNames) {
                JButton button = new JButton(gameName);
                button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
                button.setFont(new Font("Arial", Font.BOLD, fontSize));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        frame.dispose();
                        listener.onGameSelected(gameName);
                    }
                });
                buttonPanel.add(button);
            }

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.add(buttonPanel, BorderLayout.CENTER);

            frame.setLayout(new BorderLayout(0, 40));
            frame.add(label, BorderLayout.NORTH);
            frame.add(centerPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
