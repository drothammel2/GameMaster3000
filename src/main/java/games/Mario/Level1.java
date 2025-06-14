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

public class Level1 extends JFrame implements LevelBehavior {
    private Engine engine;
    private List<Feuerblume> items = new ArrayList<>();

    // Plattformen: {StartBlockX, HöheInBlöcken, LängeInBlöcken}
    private static final int[][] PLATFORMS = {
        {  5,1,4},{ 12,2,3},{ 20,1,5},{ 30,3,2},
        { 45,1,3},{ 55,2,4},{ 65,1,6},{ 75,2,3},
        { 85,1,5},{ 95,3,4},{105,1,8},{115,2,5},
        {125,1,4},{135,3,5},{145,2,4},{155,1,6},
        {165,2,3},{175,1,5},{185,3,4},{195,2,2}
    };
    private static final int[][] HOLES = {
        {10,2},{50,3},{120,1}
    };
    // Luft‐Blöcke: {BlockIndex, HöheInBlöcken über Boden}
    private static final int[][] BLACK_BLOCKS = {
        {  8,3 }, { 14,4 }, { 24,2 }
    };

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
                if (engine == null) return;  // warte, bis Engine gesetzt ist
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

                // Zeichne Luft‐Blöcke mit dynamischem Abstand
                g.setColor(Color.BLACK);
                for (int[] b : BLACK_BLOCKS) {
                    int bx = b[0]*bs - ox;
                    // Bestimme Plattform-Höhe unter diesem Block
                    int pHeight = 0;
                    for (int[] p : engine.getPlatforms()) {
                        if (b[0] >= p[0] && b[0] < p[0] + p[2]) {
                            pHeight = p[1];
                            break;
                        }
                    }
                    // drei Blöcke über Plattform oder Boden
                    int by = groundY - (pHeight + 3)*bs;
                    g.fillRect(bx, by, bs, bs);
                }

                // Spieler
                engine.drawItems(g);
                engine.drawFireballs(g, getWidth(), getHeight());
                engine.drawPlayer(g, getWidth(), getHeight());
            }
        };

        engine = new Engine(levelPanel, this);
        add(levelPanel);

        SwingUtilities.invokeLater(() -> {
            levelPanel.requestFocusInWindow();
            // jetzt, da engine existiert und Panel bekannt, Items spawnen
            spawnItems(levelPanel.getWidth(), levelPanel.getHeight());
            levelPanel.repaint();
        });
    }

    @Override public int[][] getPlatforms()    { return PLATFORMS; }
    @Override public int[][] getHoles()        { return HOLES; }
    @Override
    public int[][] getBlackBlocks() {
        return Arrays.stream(BLACK_BLOCKS)
                     .filter(b -> b[1] >= 2)    // nur Blöcke mind. 2 Höhen über Boden
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
        // Bestimme Plattform-Höhe unter diesem schwarzen Block
        int pHeight = 0;
        for (int[] p : engine.getPlatforms()) {
            if (b[0] >= p[0] && b[0] < p[0] + p[2]) {
                pHeight = p[1];
                break;
            }
        }
        // Oberkante des schwarzen Blocks in der Paint-Logik:
        int blockTop = groundY - (pHeight + 3)*bs;
        // Blume zentriert auf dem schwarzen Block
        int itemX = b[0]*bs + (bs - Feuerblume.SIZE)/2;
        int itemY = blockTop - Feuerblume.SIZE;
        items.add(new Feuerblume(itemX, itemY));
    }

    @Override
    public void drawItems(Graphics g) {
        int ox = engine.getOffsetX();
        int bs = engine.getBLOCK_SIZE();
        // Feuerblume jeweils eine Blockhöhe über dem Boden
        for (Feuerblume f : items) {
            int screenX = f.getX() - ox;
            // zeichne an der in spawnItems gesetzten Y‐Position
            int screenY = f.getY();
            // nur zeichnen, wenn im sichtbaren Bereich
            if (screenX + Feuerblume.SIZE < 0 || screenX > getWidth()) continue;
            g.drawImage(f.getImage(), screenX, screenY, null);
        }
    }

    @Override
    public void updateItems(Engine engine, int w, int h) {
        int sx = w/2 - engine.getPLAYER_WIDTH()/2;
        int sy = h - engine.getBLOCK_SIZE() - engine.getPLAYER_HEIGHT() + engine.getPlayerOffsetY();
        Rectangle playerRect = new Rectangle(sx, sy, engine.getPLAYER_WIDTH(), engine.getPLAYER_HEIGHT());
        items.removeIf(f -> {
            int fx = f.getX() - engine.getOffsetX();
            int fy = f.getY();
            Rectangle itemRect = new Rectangle(fx, fy, Feuerblume.SIZE, Feuerblume.SIZE);
            if (playerRect.intersects(itemRect)) {
                f.applyEffect(engine);
                return true;
            }
            return false;
        });
    }

    @Override
    public void spawnItemAt(int worldX, int worldY) {
        items.add(new Feuerblume(worldX, worldY));
    }

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Level1 lvl = new Level1();
            lvl.setVisible(true);
        });
    }
}
