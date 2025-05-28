package games.AimTrainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class AimTrainer {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Aim Trainer");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);

            AimPanel panel = new AimPanel();
            frame.add(panel);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}

class AimPanel extends JPanel {
    private int circleX, circleY, circleR = 60;
    private int score = 0;
    private long startTime;
    private long lastClickTime;
    private final Random rand = new Random();

    public AimPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        placeCircle();

        startTime = System.currentTimeMillis();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                int dx = mx - (circleX + circleR);
                int dy = my - (circleY + circleR);
                if (dx * dx + dy * dy <= circleR * circleR) {
                    score++;
                    lastClickTime = System.currentTimeMillis();
                    placeCircle();
                    repaint();
                }
            }
        });

        // ESC zum Schließen
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    SwingUtilities.getWindowAncestor(AimPanel.this).dispose();
                }
            }
        });

        // Timer für Repaint und Fokus
        Timer timer = new Timer(100, e -> {
            repaint();
            requestFocusInWindow();
        });
        timer.start();
    }

    private void placeCircle() {
        int w = getToolkit().getScreenSize().width;
        int h = getToolkit().getScreenSize().height;
        circleX = rand.nextInt(Math.max(1, w - 2 * circleR));
        circleY = rand.nextInt(Math.max(1, h - 2 * circleR - 150)) + 100;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Kreis
        g.setColor(Color.RED);
        g.fillOval(circleX, circleY, 2 * circleR, 2 * circleR);

        // Score und Zeit
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Treffer: " + score, 50, getHeight() - 80);

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        g.drawString("Zeit: " + elapsed + "s", 50, getHeight() - 30);
    }
}
