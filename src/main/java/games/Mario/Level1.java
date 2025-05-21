package games.Mario;

import java.awt.*;
import java.awt.event.*;            // KeyAdapter, KeyEvent
import javax.swing.*;

public class Level1 extends JFrame {
    private JDialog pauseDialog;
    private Engine engine;  // neu

    public Level1() {
        setTitle("Level 1 - Overworld");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setResizable(false);

        System.out.println("Level1 wird im Windowed Fullscreen gestartet.");

        JPanel levelPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth(), h = getHeight();
                int bs = engine.getBLOCK_SIZE();
                int ox = engine.getOffsetX();
                int groundY = h - bs;

                // Himmel
                g.setColor(new Color(135,206,235));
                g.fillRect(0,0,w,h);

                // Boden mit Löchern
                for (int bx = 0; bx < engine.getLEVEL_LENGTH()*bs; bx += bs) {
                    int bi = bx/bs;
                    boolean hole = false;
                    for (int[] ho : engine.getHoles()) {
                        if (bi >= ho[0] && bi < ho[0] + ho[1]) { hole = true; break; }
                    }
                    if (hole) continue;
                    int sx = bx - ox;
                    if (sx+bs<0||sx>w) continue;
                    g.setColor(new Color(139,69,19));
                    g.fillRect(sx, groundY, bs, bs);
                    g.setColor(new Color(34,139,34));
                    g.fillRect(sx, groundY, bs, bs/4);
                }

                // Plattformen
                for (int[] p : engine.getPlatforms()) {
                    int startX = p[0]*bs - ox;
                    int platY   = groundY - p[1]*bs;
                    for (int i = 0; i < p[2]; i++) {
                        int x = startX + i*bs;
                        if (x+bs<0||x>w) continue;
                        g.setColor(new Color(139,69,19));
                        g.fillRect(x, platY, bs, bs);
                        g.setColor(new Color(34,139,34));
                        g.fillRect(x, platY, bs, bs/4);
                    }
                }

                // Spieler
                int px = w/2 - engine.getPLAYER_WIDTH()/2;
                int py = groundY - engine.getPLAYER_HEIGHT() + engine.getPlayerOffsetY();
                g.setColor(Color.RED);
                g.fillRect(px, py, engine.getPLAYER_WIDTH(), engine.getPLAYER_HEIGHT());
            }
        };

        engine = new Engine(levelPanel);  // statt eigener Bewegung und Timer
        add(levelPanel);
        setupPauseListener();
    }

    private void setupPauseListener() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showPauseMenu();
                }
            }
        });
    }

    private void showPauseMenu() {
        if (pauseDialog != null && pauseDialog.isShowing()) return;

        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        pauseDialog = new JDialog(topFrame, "Pause", true);
        pauseDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pauseDialog.setSize(300, 150);
        pauseDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        JButton resumeButton = new JButton("Fortsetzen");
        JButton quitButton = new JButton("Spiel beenden");

        resumeButton.addActionListener(e -> {
            pauseDialog.dispose();
            requestFocusInWindow();
        });

        quitButton.addActionListener(e -> {
            pauseDialog.dispose();
            // Fenster explizit schließen
            Level1.this.dispose();
            games.Mario.Mario.start(); // Zeige das Mario-Hauptmenü
        });

        panel.add(resumeButton);
        panel.add(quitButton);
        pauseDialog.add(panel);
        pauseDialog.setVisible(true);
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Level1 level1 = new Level1();
            level1.setVisible(true);
            ((JPanel)level1.getContentPane().getComponent(0)).requestFocusInWindow();
        });
    }
}
