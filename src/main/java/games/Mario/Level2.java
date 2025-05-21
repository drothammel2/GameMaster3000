package games.Mario;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Level2 extends JFrame {
    private Engine engine;
    private static final int[][] LEVEL2_PLATFORMS = {
        {  3,1,2 },{  7,2,2 },{ 11,1,1 },{ 14,3,2 },
        { 18,1,3 },{ 22,2,2 },{ 26,1,1 },{ 29,3,2 },
        { 33,1,2 },{ 37,2,3 },{ 41,1,1 },{ 44,3,2 },
        { 48,1,2 },{ 52,2,2 },{ 56,1,3 }
    };
    private static final int[][] LEVEL2_HOLES = {
        {  5,1 },{ 10,2 },{ 16,1 },{ 21,2 },
        { 27,1 },{ 31,2 },{ 36,1 },{ 40,2 },
        { 45,1 },{ 50,2 }
    };

    public Level2() {

        setTitle("Level 2 - Challenge");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setResizable(false);

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
                g.fillRect(0, 0, w, h);

                // Boden mit Löchern
                for (int bx = 0; bx < engine.getLEVEL_LENGTH()*bs; bx += bs) {
                    int bi = bx/bs;
                    boolean hole = false;
                    for (int[] ho : engine.getHoles()) {
                        if (bi >= ho[0] && bi < ho[0] + ho[1]) { hole = true; break; }
                    }
                    if (hole) continue;
                    int sx = bx - ox;
                    if (sx+bs < 0 || sx > w) continue;
                    g.setColor(new Color(139,69,19));
                    g.fillRect(sx, groundY, bs, bs);
                    g.setColor(new Color(34,139,34));
                    g.fillRect(sx, groundY, bs, bs/4);
                }

                // Plattformen
                for (int[] p : engine.getPlatforms()) {
                    int startX = p[0]*bs - ox;
                    int platY  = groundY - p[1]*bs;
                    for (int i = 0; i < p[2]; i++) {
                        int x = startX + i*bs;
                        if (x+bs < 0 || x > w) continue;
                        g.setColor(new Color(139,69,19));
                        g.fillRect(x, platY, bs, bs);
                        g.setColor(new Color(34,139,34));
                        g.fillRect(x, platY, bs, bs/4);
                    }
                }

                // Spieler
                engine.drawPlayer(g, getWidth(), getHeight());
            }
        };

        // Engine mit Level2‐Daten
        engine = new Engine(levelPanel, LEVEL2_PLATFORMS, LEVEL2_HOLES);
        add(levelPanel);
        SwingUtilities.invokeLater(() -> levelPanel.requestFocusInWindow());
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Level2 lvl = new Level2();
            lvl.setVisible(true);
        });
    }
}
