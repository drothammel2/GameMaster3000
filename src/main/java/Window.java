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
        // MonkeyType zur Liste hinzufügen!
        startMainscreen(listener, Arrays.asList("Mario", "Snake", "Tetris", "Dummy1", "Dummy2", "MonkeyType", "AimTrainer", "Speedrun"));
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
            int totalButtons = numGames + 1; // +1 für Quit

            // Dynamische Spaltenzahl: maximal 4, mindestens 1
            int cols = Math.min(4, totalButtons);
            int rows = (int)Math.ceil((double)totalButtons / cols);

            JPanel buttonPanel = new JPanel(new GridLayout(rows, cols, 40, 40));
            buttonPanel.setOpaque(false);

            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            int buttonWidth = (screenSize.width - (cols + 1) * 40) / cols;
            int buttonHeight = (screenSize.height - 200 - (rows + 1) * 40) / rows;
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

            // Quit-Button hinzufügen
            JButton quitButton = new JButton("Quit");
            quitButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
            quitButton.setFont(new Font("Arial", Font.BOLD, fontSize));
            quitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    listener.onGameSelected("Quit");
                }
            });
            buttonPanel.add(quitButton);

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