package games.Speedrun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Speedrun {
    public static void start(Runnable onExitToMenu) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Speedrun");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            SpeedrunPanel panel = new SpeedrunPanel(onExitToMenu, frame);
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
            SwingUtilities.invokeLater(panel::requestFocusInWindow);
        });
    }
}

class SpeedrunPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final int FPS = 60;

    private GameState currentState;
    private GameLoop gameLoop;
    private final Runnable onExitToMenu;
    private final JFrame frame;

    public SpeedrunPanel(Runnable onExitToMenu, JFrame frame) {
        this.onExitToMenu = onExitToMenu;
        this.frame = frame;
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(this); // Add mouse listener
        addMouseMotionListener(this); // Add mouse motion listener

        setState(new GameplayState(this, onExitToMenu, frame));

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

    @Override
    public void mousePressed(MouseEvent e) {
        if (currentState != null) {
            try {
                java.lang.reflect.Method m = currentState.getClass().getMethod("mousePressed", MouseEvent.class);
                m.invoke(currentState, e);
            } catch (NoSuchMethodException ex) {
                // Ignore if not implemented
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentState != null) {
            try {
                java.lang.reflect.Method m = currentState.getClass().getMethod("mouseReleased", MouseEvent.class);
                m.invoke(currentState, e);
            } catch (NoSuchMethodException ex) {
                // Ignore if not implemented
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentState != null) {
            try {
                java.lang.reflect.Method m = currentState.getClass().getMethod("mouseDragged", MouseEvent.class);
                m.invoke(currentState, e);
            } catch (NoSuchMethodException ex) {
                // Ignore if not implemented
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
