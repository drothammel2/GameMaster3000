package games.AimTrainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AimTrainer {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AimTrainer - Trainingsmodi");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
            frame.setUndecorated(true); // Remove window borders
            frame.setLayout(new BorderLayout());

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(2, 2, 20, 20)); // Adjusted grid for 4 modes
            mainPanel.setBackground(Color.DARK_GRAY);
            frame.add(mainPanel, BorderLayout.CENTER);

            // Add training modes
            mainPanel.add(createModeButton("Flick", "Train your flick shots", "flick_icon.png", e -> startFlickMode(frame)));
            mainPanel.add(createModeButton("Tracking", "Follow moving targets", "tracking_icon.png", e -> startTrackingMode(frame)));
            mainPanel.add(createModeButton("Speed", "Improve your reaction time", "speed_icon.png", e -> startSpeedMode(frame)));
            mainPanel.add(createModeButton("Precision", "Focus on accuracy", "precision_icon.png", e -> startPrecisionMode(frame)));

            frame.setVisible(true);
        });
    }

    private static JButton createModeButton(String title, String description, String iconPath, java.awt.event.ActionListener action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.LIGHT_GRAY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Add icon
        JLabel iconLabel = new JLabel(new ImageIcon(iconPath), SwingConstants.CENTER);
        button.add(iconLabel, BorderLayout.CENTER);

        // Add title and description
        JLabel textLabel = new JLabel("<html><h1>" + title + "</h1><p>" + description + "</p></html>", SwingConstants.CENTER);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        button.add(textLabel, BorderLayout.SOUTH);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(Color.GRAY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Color.LIGHT_GRAY);
            }
        });

        button.addActionListener(action);
        return button;
    }

    private static void startFlickMode(JFrame frame) {
        frame.dispose(); // Close the menu

        SwingUtilities.invokeLater(() -> {
            JFrame flickFrame = new JFrame("Flick Mode");
            flickFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            flickFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
            flickFrame.setUndecorated(true); // Remove window borders
            flickFrame.setLayout(null); // Absolute positioning for random target placement

            JLabel scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
            scoreLabel.setBounds(10, 10, 200, 30);
            flickFrame.add(scoreLabel);

            JLabel timerLabel = new JLabel("Time: 30");
            timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
            timerLabel.setBounds(10, 50, 200, 30);
            flickFrame.add(timerLabel);

            Random random = new Random();
            JButton targetButton = new JButton();
            targetButton.setBackground(Color.RED);
            targetButton.setBounds(300, 200, 50, 50);
            flickFrame.add(targetButton);

            int[] score = {0};
            int[] totalClicks = {0}; // Total clicks, including misses
            long[] reactionStartTime = {System.currentTimeMillis()};
            long[] totalReactionTime = {0};
            int[] targetHits = {0};

            targetButton.addActionListener(e -> {
                score[0]++;
                targetHits[0]++;
                totalClicks[0]++; // Increment total clicks only when the target is hit
                scoreLabel.setText("Score: " + score[0]);
                totalReactionTime[0] += System.currentTimeMillis() - reactionStartTime[0];

                // Move target to a new random position
                int x = random.nextInt(flickFrame.getWidth() - targetButton.getWidth());
                int y = random.nextInt(flickFrame.getHeight() - targetButton.getHeight() - 50); // Adjust for title bar
                targetButton.setLocation(x, y);

                reactionStartTime[0] = System.currentTimeMillis(); // Reset reaction timer
            });

            // Count missed clicks
            flickFrame.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!targetButton.getBounds().contains(e.getPoint())) {
                        totalClicks[0]++; // Increment total clicks only if the click is outside the target
                    }
                }
            });

            Timer gameTimer = new Timer(1000, new AbstractAction() {
                int timeLeft = 30;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timeLeft > 0) {
                        timerLabel.setText("Time: " + (--timeLeft));
                    } else {
                        ((Timer) e.getSource()).stop();
                        flickFrame.dispose();
                        showFlickResults(score[0], targetHits[0], totalClicks[0], totalReactionTime[0]);
                    }
                }
            });
            gameTimer.start();

            // Start the first reaction timer
            reactionStartTime[0] = System.currentTimeMillis();

            flickFrame.setVisible(true);
        });
    }

    private static void showFlickResults(int score, int targetHits, int totalClicks, long totalReactionTime) {
        int missedTargets = totalClicks - targetHits; // Missed clicks
        double accuracy = totalClicks > 0 ? (targetHits / (double) totalClicks) * 100 : 0; // Avoid division by zero
        double avgReactionTime = targetHits > 0 ? totalReactionTime / (double) targetHits : 0;

        // Create a panel for the analysis
        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new GridLayout(6, 1, 10, 10));
        analysisPanel.add(new JLabel("Session Results:", SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Total Hits: %d", targetHits), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Total Clicks: %d", totalClicks), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Accuracy: %.2f%%", accuracy), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Average Reaction Time: %.2f ms", avgReactionTime), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Missed Targets: %d", missedTargets), SwingConstants.CENTER));

        // Add a save button
        int option = JOptionPane.showConfirmDialog(null, analysisPanel, "Flick Mode Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            saveFlickSessionResults(targetHits, totalClicks, accuracy, avgReactionTime, missedTargets, score / 30.0);
        }
    }

    private static void saveFlickSessionResults(int hits, int totalClicks, double accuracy, double avgReactionTime, int missedTargets, double dps) {
        // Save the session results (e.g., to a file or database)
        String results = String.format(
            "Hits: %d\nTotal Clicks: %d\nAccuracy: %.2f%%\nAverage Reaction Time: %.2f ms\nMissed Targets: %d\nDPS: %.2f",
            hits, totalClicks, accuracy, avgReactionTime, missedTargets, dps
        );

        // For simplicity, we display the results in a dialog (can be replaced with file saving logic)
        JOptionPane.showMessageDialog(null, "Session results saved:\n" + results, "Save Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void startTrackingMode(JFrame frame) {
        frame.dispose(); // Close the menu

        SwingUtilities.invokeLater(() -> {
            JFrame trackingFrame = new JFrame("Tracking Mode");
            trackingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            trackingFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
            trackingFrame.setUndecorated(true); // Remove window borders
            trackingFrame.setLayout(null); // Absolute positioning for target movement

            JLabel scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
            scoreLabel.setBounds(10, 10, 200, 30);
            trackingFrame.add(scoreLabel);

            JLabel timerLabel = new JLabel("Time: 30");
            timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
            timerLabel.setBounds(10, 50, 200, 30);
            trackingFrame.add(timerLabel);

            JPanel targetPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.RED);
                    g.fillOval(0, 0, getWidth(), getHeight());
                }
            };
            targetPanel.setBounds(300, 200, 50, 50); // Initial position and size
            trackingFrame.add(targetPanel);

            Random random = new Random();
            int[] velocity = {3, 2}; // Velocity in x and y directions
            int[] score = {0};
            long[] sessionStartTime = {System.currentTimeMillis()};

            targetPanel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    score[0]++;
                    scoreLabel.setText("Score: " + score[0]);
                }
            });

            Timer movementTimer = new Timer(16, e -> { // Smooth movement (60 FPS)
                int x = targetPanel.getX() + velocity[0];
                int y = targetPanel.getY() + velocity[1];

                // Bounce off walls
                if (x < 0 || x + targetPanel.getWidth() > trackingFrame.getWidth()) {
                    velocity[0] = -velocity[0];
                }
                if (y < 0 || y + targetPanel.getHeight() > trackingFrame.getHeight()) {
                    velocity[1] = -velocity[1];
                }

                targetPanel.setLocation(x, y);
            });
            movementTimer.start();

            Timer gameTimer = new Timer(1000, new AbstractAction() {
                int timeLeft = 30;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timeLeft > 0) {
                        timerLabel.setText("Time: " + (--timeLeft));
                    } else {
                        ((Timer) e.getSource()).stop();
                        movementTimer.stop();
                        trackingFrame.dispose();
                        showTrackingResults(score[0], System.currentTimeMillis() - sessionStartTime[0]);
                    }
                }
            });
            gameTimer.start();

            trackingFrame.setVisible(true);
        });
    }

    private static void showTrackingResults(int score, long sessionDuration) {
        double accuracy = score / (double) sessionDuration * 1000; // Hits per second
        int missedTargets = 0; // Tracking mode doesn't have missed targets in this implementation

        // Create a panel for the analysis
        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new GridLayout(5, 1, 10, 10));
        analysisPanel.add(new JLabel("Session Results:", SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Total Hits: %d", score), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Accuracy: %.2f hits/sec", accuracy), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Session Duration: %.2f seconds", sessionDuration / 1000.0), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Missed Targets: %d", missedTargets), SwingConstants.CENTER));

        // Add a save button
        int option = JOptionPane.showConfirmDialog(null, analysisPanel, "Tracking Mode Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            saveSessionResults(score, 0, accuracy, 0, missedTargets, accuracy);
        }
    }

    private static void saveSessionResults(int hits, int totalTargets, double accuracy, double avgReactionTime, int missedTargets, double dps) {
        // Save the session results (e.g., to a file or database)
        String results = String.format(
            "Hits: %d / %d\nAccuracy: %.2f%%\nAverage Reaction Time: %.2f ms\nMissed Targets: %d\nDPS: %.2f",
            hits, totalTargets, accuracy, avgReactionTime, missedTargets, dps
        );

        // For simplicity, we display the results in a dialog (can be replaced with file saving logic)
        JOptionPane.showMessageDialog(null, "Session results saved:\n" + results, "Save Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void startSpeedMode(JFrame frame) {
    frame.dispose(); // Close the menu

    SwingUtilities.invokeLater(() -> {
        JFrame speedFrame = new JFrame("Speed Mode");
        speedFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        speedFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        speedFrame.setUndecorated(true);
        speedFrame.setLayout(null);

        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setBounds(10, 10, 200, 30);
        speedFrame.add(scoreLabel);

        JLabel timerLabel = new JLabel("Time: 30");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setBounds(10, 50, 200, 30);
        speedFrame.add(timerLabel);

        JPanel targetPanel = new JPanel(null);
        targetPanel.setBounds(0, 0, 1920, 1080); // Fallback size
        targetPanel.setOpaque(false);
        speedFrame.add(targetPanel);

        speedFrame.setVisible(true); // <== Fenster wird sichtbar gemacht

        // Jetzt sicherstellen, dass Breite/Höhe korrekt sind
        SwingUtilities.invokeLater(() -> {
            Random random = new Random();
            int[] score = {0};
            List<JButton> activeTargets = new ArrayList<>();
            int[] timeLeft = {30};

            Timer spawnTimer = new Timer(500, e -> {
                if (timeLeft[0] > 0) {
                    JButton target = createSpeedTarget(random, targetPanel, score, activeTargets, scoreLabel);
                    activeTargets.add(target);
                    targetPanel.add(target);
                    targetPanel.revalidate();
                    targetPanel.repaint();
                }
            });

            Timer gameTimer = new Timer(1000, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timeLeft[0] > 0) {
                        timerLabel.setText("Time: " + (--timeLeft[0]));
                    } else {
                        ((Timer) e.getSource()).stop();
                        spawnTimer.stop();
                        speedFrame.dispose();
                        showSpeedResults(score[0], activeTargets.size(), 30 - timeLeft[0]);
                    }
                }
            });

            spawnTimer.start();
            gameTimer.start();
        });
    });
}

    private static JButton createSpeedTarget(Random random, JPanel targetPanel, int[] score, List<JButton> activeTargets, JLabel scoreLabel) {
        JButton targetButton = new JButton();
        targetButton.setBackground(Color.RED);
        targetButton.setBounds(
            random.nextInt(Math.max(1, targetPanel.getWidth() - 50)), // Ensure width is valid
            random.nextInt(Math.max(1, targetPanel.getHeight() - 50)), // Ensure height is valid
            50, 50
        );

        targetButton.addActionListener(e -> {
            score[0]++;
            scoreLabel.setText("Score: " + score[0]);
            targetPanel.remove(targetButton);
            activeTargets.remove(targetButton);
            targetPanel.revalidate();
            targetPanel.repaint();
        });

        return targetButton;
    }

    private static void showSpeedResults(int score, int remainingTargets, int sessionDuration) {
        double apm = (score / (double) sessionDuration) * 60; // Actions per minute

        // Create a panel for the analysis
        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new GridLayout(4, 1, 10, 10));
        analysisPanel.add(new JLabel("Session Results:", SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Total Hits: %d", score), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Remaining Targets: %d", remainingTargets), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("APM: %.2f", apm), SwingConstants.CENTER));

        // Add a save button
        int option = JOptionPane.showConfirmDialog(null, analysisPanel, "Speed Mode Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            saveSpeedSessionResults(score, remainingTargets, apm);
        }
    }

    private static void saveSpeedSessionResults(int score, int remainingTargets, double apm) {
        // Save the session results (e.g., to a file or database)
        String results = String.format(
            "Total Hits: %d\nRemaining Targets: %d\nAPM: %.2f",
            score, remainingTargets, apm
        );

        // For simplicity, we display the results in a dialog (can be replaced with file saving logic)
        JOptionPane.showMessageDialog(null, "Session results saved:\n" + results, "Save Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void startPrecisionMode(JFrame frame) {
    frame.dispose(); // Close the menu

    SwingUtilities.invokeLater(() -> {
        JFrame precisionFrame = new JFrame("Precision Mode");
        precisionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        precisionFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        precisionFrame.setUndecorated(true);
        precisionFrame.setLayout(null);

        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setBounds(10, 10, 200, 30);
        precisionFrame.add(scoreLabel);

        JLabel timerLabel = new JLabel("Time: 30");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setBounds(10, 50, 200, 30);
        precisionFrame.add(timerLabel);

        JPanel targetPanel = new JPanel(null);
        targetPanel.setOpaque(false);
        targetPanel.setBounds(0, 0, 1920, 1080); // fallback
        precisionFrame.add(targetPanel);

        precisionFrame.setVisible(true); // <- Jetzt wird das Fenster aufgebaut

        // 🔁 Jetzt wird alles gestartet, wenn die GUI aufgebaut ist
        SwingUtilities.invokeLater(() -> {
            Random random = new Random();
            int[] score = {0};
            int[] totalClicks = {0};
            int[] targetHits = {0};
            int[] missedClicks = {0};
            int[] timeLeft = {30};

            Timer spawnTimer = new Timer(1000, e -> {
                if (timeLeft[0] > 0) {
                    JButton target = createPrecisionTarget(random, targetPanel, score, targetHits, totalClicks, missedClicks, scoreLabel);
                    targetPanel.add(target);
                    targetPanel.revalidate();
                    targetPanel.repaint();
                }
            });

            Timer gameTimer = new Timer(1000, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timeLeft[0] > 0) {
                        timerLabel.setText("Time: " + (--timeLeft[0]));
                    } else {
                        ((Timer) e.getSource()).stop();
                        spawnTimer.stop();
                        precisionFrame.dispose();
                        showPrecisionResults(score[0], targetHits[0], totalClicks[0], missedClicks[0]);
                    }
                }
            });

            spawnTimer.start();
            gameTimer.start();
        });
    });
}


    private static JButton createPrecisionTarget(Random random, JPanel targetPanel, int[] score, int[] targetHits, int[] totalClicks, int[] missedClicks, JLabel scoreLabel) {
        JButton targetButton = new JButton();
        int targetSize = random.nextInt(11) + 5; // Target size between 5 and 15 pixels
        targetButton.setBackground(Color.BLUE);
        targetButton.setBounds(
            random.nextInt(Math.max(1, targetPanel.getWidth() - targetSize)),
            random.nextInt(Math.max(1, targetPanel.getHeight() - targetSize)),
            targetSize, targetSize
        );

        targetButton.addActionListener(e -> {
            score[0] += 10; // Add points for hitting the target
            targetHits[0]++;
            totalClicks[0]++; // Increment total clicks only for hits
            scoreLabel.setText("Score: " + score[0]);
            targetPanel.remove(targetButton);
            targetPanel.revalidate();
            targetPanel.repaint();
        });

    // Add a single MouseListener to the targetPanel to handle missed clicks
    if (targetPanel.getMouseListeners().length == 0) {
        targetPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                boolean hitTarget = false;

                // Check if the click was on any active target
                for (Component component : targetPanel.getComponents()) {
                    if (component.getBounds().contains(e.getPoint())) {
                        hitTarget = true;
                        break;
                    }
                }

                if (!hitTarget) {
                    if (score[0] >= 5) {
                        score[0] -= 5; // Deduct points only if score is sufficient
                    } else {
                        score[0] = 0; // Ensure score doesn't go below 0
                    }
                    missedClicks[0]++;
                    totalClicks[0]++; // Increment total clicks only for misses
                    scoreLabel.setText("Score: " + score[0]);
                }
            }
        });
    }

        return targetButton;
    }

    private static void showPrecisionResults(int score, int targetHits, int totalClicks, int missedClicks) {
        double accuracy = totalClicks > 0 ? (targetHits / (double) totalClicks) * 100 : 0; // Avoid division by zero
        String award = accuracy > 95 ? "🏆 Auszeichnung: Präzisionsmeister!" : "";

        // Create a panel for the analysis
        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new GridLayout(6, 1, 10, 10));
        analysisPanel.add(new JLabel("Session Results:", SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Total Hits: %d", targetHits), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Total Clicks: %d", totalClicks), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Accuracy: %.2f%%", accuracy), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(String.format("Missed Targets: %d", missedClicks), SwingConstants.CENTER));
        analysisPanel.add(new JLabel(award, SwingConstants.CENTER));

        // Add a save button
        int option = JOptionPane.showConfirmDialog(null, analysisPanel, "Precision Mode Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            savePrecisionSessionResults(score, targetHits, totalClicks, accuracy, missedClicks, award);
        }
    }

    private static void savePrecisionSessionResults(int score, int targetHits, int totalClicks, double accuracy, int missedClicks, String award) {
        // Save the session results (e.g., to a file or database)
        String results = String.format(
            "Score: %d\nTotal Hits: %d\nTotal Clicks: %d\nAccuracy: %.2f%%\nMissed Targets: %d\n%s",
            score, targetHits, totalClicks, accuracy, missedClicks, award
        );

        // For simplicity, we display the results in a dialog (can be replaced with file saving logic)
        JOptionPane.showMessageDialog(null, "Session results saved:\n" + results, "Save Results", JOptionPane.INFORMATION_MESSAGE);
    }
}
