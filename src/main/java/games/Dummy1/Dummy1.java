package games.Dummy1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

public class Dummy1 {
    private static int score = 0;
    private static boolean isPaused = false; // Track pause state
    private static Timer gameTimer; // Timer for game activity
    private static JLabel countdownLabel; // Label for countdown display
    private static JLabel pauseOverlay; // Pause overlay label

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AimTrainer - Mode Selection");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set fullscreen mode
            frame.setUndecorated(true); // Remove window borders for fullscreen
            frame.setLayout(new BorderLayout());

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.setBackground(Color.DARK_GRAY);
            frame.add(mainPanel, BorderLayout.CENTER);

            JLabel titleLabel = new JLabel("Choose Your Training Mode");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(20, 0, 40, 0);
            mainPanel.add(titleLabel, gbc);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 3, 20, 0));
            buttonPanel.setOpaque(false);

            JButton flickButton = createStyledButton("Flick");
            JButton trackingButton = createStyledButton("Tracking");
            JButton targetSwitchButton = createStyledButton("Target Switch");

            buttonPanel.add(flickButton);
            buttonPanel.add(trackingButton);
            buttonPanel.add(targetSwitchButton);

            gbc.gridy = 1;
            mainPanel.add(buttonPanel, gbc);

            flickButton.addActionListener(e -> {
                frame.dispose(); // Close mode selection screen
                startFlickMode(); // Start the Flick mode
            });

            trackingButton.addActionListener(e -> {
                frame.dispose(); // Close mode selection screen
                startTrackingMode(); // Start the Tracking mode
            });

            targetSwitchButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Target Switch mode is not implemented yet."));

            frame.setVisible(true);
        });
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }

    private static void startFlickMode() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AimTrainer - Flick Mode");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set fullscreen mode
            frame.setUndecorated(true); // Remove window borders for fullscreen
            frame.setLayout(null); // Use absolute positioning

            setupKeyListener(frame); // Add consistent key handling

            countdownLabel = createCountdownLabel(frame);
            frame.add(countdownLabel);

            JLabel scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
            scoreLabel.setBounds(10, 10, 200, 30);
            frame.add(scoreLabel);

            JButton targetButton = new JButton();
            targetButton.setBackground(Color.BLUE);
            targetButton.setBounds(300, 200, 50, 50);
            targetButton.setVisible(false); // Initially hidden
            frame.add(targetButton);

            Random random = new Random();

            gameTimer = new Timer(1500, e -> { // Slower interval (1.5 seconds)
                if (!isPaused) { // Only move the target if not paused
                    int x = random.nextInt(frame.getWidth() - targetButton.getWidth());
                    int y = random.nextInt(frame.getHeight() - targetButton.getHeight() - 50); // Adjust for title bar
                    targetButton.setLocation(x, y);
                    targetButton.setVisible(true); // Make the target visible
                }
            });
            gameTimer.start();

            targetButton.addActionListener(e -> {
                if (!isPaused) { // Only increment score if not paused
                    score++;
                    scoreLabel.setText("Score: " + score);
                    targetButton.setVisible(false); // Hide the target after a successful click
                }
            });

            frame.setVisible(true);
            frame.requestFocus(); // Ensure the frame has focus for key events
        });
    }

    private static void startTrackingMode() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AimTrainer - Tracking Mode");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set fullscreen mode
            frame.setUndecorated(true); // Remove window borders for fullscreen
            frame.setLayout(null); // Use absolute positioning

            setupKeyListener(frame); // Add consistent key handling

            countdownLabel = createCountdownLabel(frame);
            frame.add(countdownLabel);

            JLabel scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
            scoreLabel.setBounds(10, 10, 200, 30);
            frame.add(scoreLabel);

            JPanel targetCircle = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.RED);
                    g.fillOval(0, 0, getWidth(), getHeight());
                }
            };
            targetCircle.setBounds(300, 200, 50, 50); // Initial position and size
            frame.add(targetCircle);

            int[] velocity = {3, 2}; // Velocity in x and y directions

            gameTimer = new Timer(16, e -> { // Smooth movement (60 FPS)
                if (!isPaused) { // Only move the target if not paused
                    int x = targetCircle.getX() + velocity[0];
                    int y = targetCircle.getY() + velocity[1];

                    // Bounce off walls
                    if (x < 0 || x + targetCircle.getWidth() > frame.getWidth()) {
                        velocity[0] = -velocity[0];
                    }
                    if (y < 0 || y + targetCircle.getHeight() > frame.getHeight()) {
                        velocity[1] = -velocity[1];
                    }

                    targetCircle.setLocation(x, y);
                }
            });
            gameTimer.start();

            targetCircle.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (!isPaused) { // Only increment score if not paused
                        score++;
                        scoreLabel.setText("Score: " + score);
                    }
                }
            });

            frame.setVisible(true);
            frame.requestFocus(); // Ensure the frame has focus for key events
        });
    }

    private static void setupKeyListener(JFrame frame) {
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    frame.dispose(); // Close the current mode
                    start(); // Return to mode selection
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause(frame); // Pause menu
                }
            }
        });
        frame.setFocusable(true);
        frame.requestFocusInWindow(); // Ensure the frame has focus for key events
    }

    private static JLabel createCountdownLabel(JFrame frame) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 120));
        label.setForeground(Color.WHITE);
        label.setBounds(0, frame.getHeight() / 2 - 60, frame.getWidth(), 120);
        label.setVisible(false);
        return label;
    }

    private static void togglePause(JFrame frame) {
        isPaused = !isPaused;
        if (isPaused) {
            // Display pause overlay
            pauseOverlay = new JLabel("Paused", SwingConstants.CENTER);
            pauseOverlay.setFont(new Font("Arial", Font.BOLD, 48));
            pauseOverlay.setForeground(Color.WHITE);
            pauseOverlay.setOpaque(true);
            pauseOverlay.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent background
            pauseOverlay.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            frame.add(pauseOverlay);
            frame.repaint();
        } else {
            frame.remove(pauseOverlay); // Remove pause overlay
            startCountdown(frame); // Start countdown
            frame.repaint();
        }
    }

    private static void startCountdown(JFrame frame) {
        countdownLabel.setVisible(true);
        countdownLabel.setText(""); // Clear any previous text
        Timer countdownTimer = new Timer(1000, new AbstractAction() {
            int countdown = 3;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (countdown > 0) {
                    countdownLabel.setText(String.valueOf(countdown--));
                    countdownLabel.setForeground(new Color(255, 255, 255, 255)); // Fully visible
                } else {
                    ((Timer) e.getSource()).stop();
                    countdownLabel.setVisible(false);
                    isPaused = false; // Resume the game
                }
            }
        });
        countdownTimer.start();
    }
}
