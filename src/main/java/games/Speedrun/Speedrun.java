package games.Speedrun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Speedrun {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Speedrun");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            frame.setSize(1280, 800);
            frame.setLocationRelativeTo(null); // Center the window
            frame.setResizable(true);

            SpeedrunPanel panel = new SpeedrunPanel();
            panel.setPreferredSize(new Dimension(1280, 800));
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            frame.pack();
            frame.setVisible(true);

            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        });
    }
}

class SpeedrunPanel extends JPanel implements KeyListener {
    private static final int FPS = 60;

    private GameState currentState;
    private GameLoop gameLoop;

    public SpeedrunPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        setState(new GameplayState(this));

        gameLoop = new GameLoop(() -> {
            if (currentState != null) currentState.update();
            repaint();
        }, FPS);
        gameLoop.start();
    }

    public void setState(GameState state) {
        this.currentState = state;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentState != null) {
            currentState.render(g, getWidth(), getHeight());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentState != null) currentState.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentState != null) currentState.keyReleased(e);
    }

    @Override public void keyTyped(KeyEvent e) {}
}
