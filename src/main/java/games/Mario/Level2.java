package games.Mario;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import games.Mario.Items.Feuerblume;

public class Level2 extends JFrame implements LevelBehavior {
    private Engine engine;
    private List<Feuerblume> items = new ArrayList<>();

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
    // Luft‐Blöcke: {BlockIndex, HöheInBlöcken über Boden}
    private static final int[][] BLACK_BLOCKS = {
        {  6,2 }, { 18,3 }, { 30,2 }
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
                if (engine == null) return;  // warte auf Engine-Initialisierung
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

                // Luft‐Blöcke
                g.setColor(Color.BLACK);
                int baseOffset = BLACK_BLOCKS[0][1];
                for (int[] b : BLACK_BLOCKS) {
                    int bx = b[0]*bs - ox;
                    int by = groundY - (baseOffset + 1)*bs;
                    g.fillRect(bx, by, bs, bs);
                }

                // Spieler
                engine.drawPlayer(g, w, h);
                engine.drawItems(g);
                engine.drawFireballs(g, w, h);
            }
        };

        // Engine mit Level2‐Daten
        engine = new Engine(levelPanel, this);
        add(levelPanel);

        SwingUtilities.invokeLater(() -> {
            levelPanel.requestFocusInWindow();
            // jetzt, da engine existiert und Panel Größe hat, Items spawnen
            spawnItems(levelPanel.getWidth(), levelPanel.getHeight());
        });
    }

    @Override public int[][] getPlatforms() { return LEVEL2_PLATFORMS; }
    @Override public int[][] getHoles()     { return LEVEL2_HOLES; }
    @Override
    public int[][] getBlackBlocks() {
        return Arrays.stream(BLACK_BLOCKS)
                     .filter(b -> b[1] >= 2)
                     .toArray(int[][]::new);
    }

    @Override
    public void spawnItems(int w, int h) {
        int bs      = engine.getBLOCK_SIZE();
        int groundY = h - bs;
        int[][] blacks = getBlackBlocks();
        items.clear();
        if (blacks.length == 0) return;
        int[] b = blacks[new Random().nextInt(blacks.length)];
        int pHeight = 0;
        for (int[] p : engine.getPlatforms()) {
            if (b[0] >= p[0] && b[0] < p[0] + p[2]) {
                pHeight = p[1];
                break;
            }
        }
        int blockTop = groundY - (pHeight + 3)*bs;
        int itemX    = b[0]*bs + bs/2;
        int itemY    = blockTop - Feuerblume.SIZE;
        items.add(new Feuerblume(itemX, itemY));
    }

    @Override
    public void updateItems(Engine engine, int w, int h) {
        int px = w/2 - engine.getPLAYER_WIDTH()/2;
        int py = h - engine.getBLOCK_SIZE() - engine.getPLAYER_HEIGHT() + engine.getPlayerOffsetY();
        Rectangle pr = new Rectangle(px, py, engine.getPLAYER_WIDTH(), engine.getPLAYER_HEIGHT());
        items.removeIf(f -> {
            int fx = f.getX() - engine.getOffsetX(), fy = f.getY();
            Rectangle ir = new Rectangle(fx, fy, Feuerblume.SIZE, Feuerblume.SIZE);
            if (pr.intersects(ir)) {
                f.applyEffect(engine);
                return true;
            }
            return false;
        });
    }

    @Override
    public void drawItems(Graphics g) {
        int ox = engine.getOffsetX();
        int groundY = getHeight() - engine.getBLOCK_SIZE();
        for (Feuerblume f : items) {
            int sx = f.getX() - ox;
            int sy = groundY - Feuerblume.SIZE;
            if (sx + Feuerblume.SIZE < 0 || sx > getWidth()) continue;
            g.drawImage(f.getImage(), sx, sy, null);
        }
    }

    @Override
    public void spawnItemAt(int worldX, int worldY) {
        items.add(new Feuerblume(worldX, worldY));
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Level2 lvl = new Level2();
            lvl.setVisible(true);
        });
    }
}
