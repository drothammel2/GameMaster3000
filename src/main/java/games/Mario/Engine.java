package games.Mario;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;      // neu: Import für Feuerblume
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import games.Mario.Items.Feuerblume;

public class Engine {
    private final int BLOCK_SIZE   = 50;
    public static final int LEVEL_LENGTH = 200;
    private final int MOVE_STEP    = 10;
    private final int PLAYER_WIDTH = 30;
    private final int PLAYER_HEIGHT= 50;
    private final int GRAVITY      = 1;

    private int offsetX        = 0;
    private int playerOffsetY  = 0; // 0 = Boden
    private int playerVelocityY= 0;
    private boolean onGround   = true;
    private boolean movingLeft, movingRight;
    private boolean fireMode = false;               // neu: Feuer-Modus aktiv?
    // neu: Fireball-Spam verhindern
    private long lastFireTime = 0;
    private static final int FIRE_COOLDOWN = 500; // Millisekunden
    private boolean facingLeft = false;  // neu: Blickrichtung

    private JDialog pauseDialog;
    private final JPanel panel;
    private final LevelBehavior level;
    private Image playerImage;
    private final List<Fireball> fireballs = new ArrayList<>();
    private final Set<Integer> usedBlackBlocks = new HashSet<>();  // verbrauchte Blocks
    private boolean levelCompleted = false;   // verhindert mehrfaches Anzeigen

    // Ein-Argument-Konstruktor für Standard-Level
    public Engine(JPanel panel) {
        this(panel, new Level1());
    }

    public Engine(JPanel panel, LevelBehavior level) {
        // Instanzvariablen setzen
        this.panel = panel;
        this.level = level;
        loadPlayerImage();

        panel.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e){ handleKey(e, true); }
            @Override public void keyReleased(KeyEvent e){ handleKey(e, false); }
        });
        panel.setFocusable(true);
        new Timer(16, e -> { update(panel.getWidth(), panel.getHeight()); panel.repaint(); })
            .start();
    }

    private void handleKey(KeyEvent e, boolean press) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                movingLeft  = press;
                if (press) facingLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
                movingRight = press;
                if (press) facingLeft = false;
                break;
            case KeyEvent.VK_SPACE:
                if(onGround && press){ playerVelocityY = -20; onGround = false; }
                break;
            case KeyEvent.VK_Q:
                if (fireMode && press) {
                    long now = System.currentTimeMillis();
                    if (now - lastFireTime >= FIRE_COOLDOWN) {
                        spawnFireball(panel.getWidth(), panel.getHeight());
                        lastFireTime = now;
                    }
                }
                break;
            case KeyEvent.VK_ESCAPE:           // neu: ESC für Pause‐Menü
                if (press) showPauseMenu();
                break;
        }
    }

    private void loadPlayerImage() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/games/Mario/Player.png"));
        playerImage = icon.getImage()
                          .getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_SMOOTH);
    }

    private void spawnFireball(int w, int h) {
        int screenPx = w/2 - PLAYER_WIDTH/2;
        int worldX = offsetX + screenPx;
        // Spieler-Welt-Y ermitteln (Boden minus Mario-Höhe plus Offset)
        int groundY = h - BLOCK_SIZE;
        int worldY = groundY - PLAYER_HEIGHT + playerOffsetY;
        // Schussrichtung je nach Blickrichtung
        int vx = facingLeft ? -MOVE_STEP : MOVE_STEP;
        int initialVy = -10;  // weniger steiler Start
        fireballs.add(new Fireball(worldX, worldY, vx, initialVy));
    }

    private void updateFireballs(int w, int h) {
        int groundY = h - BLOCK_SIZE;  // Y-Koordinate Oberkante Boden
        Iterator<Fireball> it = fireballs.iterator();
        while(it.hasNext()) {
            Fireball f = it.next();
            f.update();
            // Bodenkollision und Abprallen
            if (f.getY() > groundY - Fireball.SIZE) {
                f.bounce(groundY);
            }
            // Entfernen, wenn aus dem Bild oder Flugdistanz überschritten
            if (f.isOffscreen(offsetX, w) || f.exceededDistance()) {
                it.remove();
            }
        }
    }

    public void drawFireballs(Graphics g, int w, int h) {
        for (Fireball f : fireballs) {
            f.draw(g, offsetX);
        }
    }

    public void update(int w, int h) {
        int groundY = h - BLOCK_SIZE;
        // horizontal scrollen
        if(movingRight) offsetX += MOVE_STEP;
        if(movingLeft)  offsetX = Math.max(0, offsetX - MOVE_STEP);

        // ...neu: Unterstützung unter dem Spieler prüfen und bei keiner Unterstützung fallen lassen...
        if(onGround) {
            boolean support = false;
            int px = w/2 - PLAYER_WIDTH/2;
            int bi = (offsetX + px) / BLOCK_SIZE;
            if(playerOffsetY == 0) {
                // Bodenunterstützung (Löcher) prüfen
                boolean overHole = false;
                for(int[] hole : level.getHoles()) {
                    if(bi >= hole[0] && bi < hole[0] + hole[1]) { overHole = true; break; }
                }
                support = !overHole;
            } else {
                // Plattformunterstützung prüfen
                int playerY = groundY + playerOffsetY;
                for(int[] p : level.getPlatforms()) {
                    int platTop = groundY - p[1] * BLOCK_SIZE;
                    int sx = p[0] * BLOCK_SIZE - offsetX;
                    int ex = sx + p[2]*BLOCK_SIZE;
                    if(playerY == platTop && px+PLAYER_WIDTH>sx && px<ex) {
                        support = true; break;
                    }
                }
                // schwarz Block-Unterstützung prüfen (unter onGround)
                for(int[] b : level.getBlackBlocks()) {
                    int pHeight = 0;
                    for(int[] p : level.getPlatforms()) {
                       if (b[0] >= p[0] && b[0] < p[0] + p[2]) {
                           pHeight = p[1];
                           break;
                       }
                    }
                    int bx = b[0]*BLOCK_SIZE - offsetX;
                    int blockTop = groundY - (pHeight + 3)*BLOCK_SIZE;
                    if (playerY == blockTop && px + PLAYER_WIDTH > bx && px < bx + BLOCK_SIZE) {
                        support = true; break;
                    }
                }
            }
            if(!support) {
                onGround = false;
                playerVelocityY = 0;
            }
        }

        // fallen/springen
        if(!onGround) {
            playerVelocityY += GRAVITY;
            playerOffsetY  += playerVelocityY;
            // Kopf-Kollision nur in Fireball- oder Item-Absprungphase
            if (!onGround && playerVelocityY < 0) {
                int px      = w/2 - PLAYER_WIDTH/2;
                int prevTop = groundY + playerOffsetY - playerVelocityY;
                int newTop  = groundY + playerOffsetY;

                for (int[] b : level.getBlackBlocks()) {
                    int bx = b[0]*BLOCK_SIZE - offsetX;
                    // Plattform-Höhe
                    int pHeight = 0;
                    for (int[] p : level.getPlatforms()) {
                        if (b[0] >= p[0] && b[0] < p[0] + p[2]) {
                            pHeight = p[1]; break;
                        }
                    }
                    // Berechne Block-Top-Y
                    int bs       = BLOCK_SIZE;
                    int blockTop = groundY - (pHeight + 3)*bs;
                    if (px+PLAYER_WIDTH>bx && px<bx+BLOCK_SIZE
                       && prevTop>blockTop + bs && newTop<=blockTop + bs) {
                        playerVelocityY = -playerVelocityY/2;
                        playerOffsetY   = (blockTop + bs) - groundY;

                        // Item auf Block-Top platzieren (oberhalb)
                        int itemX = b[0]*BLOCK_SIZE + bs/2;
                        int itemY = blockTop - Feuerblume.SIZE;
                        // nur einmal Item spawnen
                        if (!usedBlackBlocks.contains(b[0])) {
                            level.spawnItemAt(itemX, itemY);
                            usedBlackBlocks.add(b[0]);
                        }
                        break;
                    }
                }
            }
        }
        // Boden-Kollision (außer Löcher)
        if(playerOffsetY > 0) {
            int px = w/2 - PLAYER_WIDTH/2;
            int bi = (offsetX + px)/BLOCK_SIZE;
            boolean overHole = false;
            for(int[] hole: level.getHoles()) {
                if(bi>=hole[0] && bi<hole[0]+hole[1]) { overHole = true; break; }
            }
            if(!overHole) { playerOffsetY=0; playerVelocityY=0; onGround=true; }
        }
        // Plattform-Kollision beim Fallen (Grasblöcke begehbar)
        if (playerVelocityY > 0) {
            int playerBottom = groundY + playerOffsetY;
            int prevBottom   = playerBottom - playerVelocityY;
            int px = w/2 - PLAYER_WIDTH/2;
            for (int[] p : level.getPlatforms()) {
                int platTop = groundY - p[1] * BLOCK_SIZE;
                int sx      = p[0] * BLOCK_SIZE - offsetX;
                int ex      = sx + p[2] * BLOCK_SIZE;
                if (px + PLAYER_WIDTH > sx && px < ex
                    && prevBottom < platTop && playerBottom >= platTop) {
                    onGround = true;
                    playerVelocityY = 0;
                    playerOffsetY   = platTop - groundY;
                    break;
                }
            }
        }
        // Kollisions-Handling für schwarze Blöcke nur beim Landen
        int px = w/2 - PLAYER_WIDTH/2;                    // Spieler-X im Fenster
        int py = groundY - PLAYER_HEIGHT + playerOffsetY;// Spieler-Y im Fenster
        for(int[] b : level.getBlackBlocks()) {
            int pHeight = 0;
            for(int[] p : level.getPlatforms()) {
                if(b[0]>=p[0]&&b[0]<p[0]+p[2]) { pHeight = p[1]; break; }
            }
            int bx = b[0]*BLOCK_SIZE - offsetX;   // Bildschirm-koord durch Abzug von offsetX
            int by = groundY - (pHeight + 3)*BLOCK_SIZE;
            Rectangle blockRect = new Rectangle(bx, by, BLOCK_SIZE, BLOCK_SIZE);
            Rectangle nextPlayer = new Rectangle(px, py + playerVelocityY, PLAYER_WIDTH, PLAYER_HEIGHT);
            if(playerVelocityY>0 && py+PLAYER_HEIGHT<=by && nextPlayer.intersects(blockRect)) {
                playerOffsetY = by - groundY;
                playerVelocityY = 0;
                onGround = true;
            }
        }
        // Item-Kollision prüfen und ggf. Power-Up anwenden
        level.updateItems(this, w, h);
        updateFireballs(w, h);
        respawnIfFallenBelow(w, h);
        checkGoalCollision(w, h);
    }

    private void respawnIfFallenBelow(int w, int h) {
        if ((h - BLOCK_SIZE) + playerOffsetY > h) {
            offsetX         = 0;
            playerOffsetY   = 0;
            playerVelocityY = 0;
            onGround        = true;
        }
    }

    private void checkGoalCollision(int w, int h) {
        if (levelCompleted) return;
        int groundY = h - BLOCK_SIZE;
        int px = w/2 - PLAYER_WIDTH/2;
        int py = groundY - PLAYER_HEIGHT + playerOffsetY;
        Rectangle playerRect = new Rectangle(px, py, PLAYER_WIDTH, PLAYER_HEIGHT);
        for(int[] g : level.getGoalBlocks()) {
            int bx = g[0]*BLOCK_SIZE - offsetX;
            // Hitbox als gelbe Wand über die gesamte Höhe
            Rectangle goalRect = new Rectangle(bx, 0, BLOCK_SIZE, h);
            if(playerRect.intersects(goalRect)) {
                levelCompleted = true;
                showLevelComplete();
                break;
            }
        }
    }

    private void showLevelComplete() {
        JFrame top = (JFrame) SwingUtilities.getWindowAncestor(panel);
        JDialog dlg = new JDialog(top, "Level geschafft!", true);
        dlg.setSize(300,120);
        dlg.setLocationRelativeTo(panel);
        JPanel p = new JPanel();
        JButton menu = new JButton("Levelauswahl");  // zur Levelauswahl
       // SPACE deaktivieren
       menu.getInputMap(JComponent.WHEN_FOCUSED)
           .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "none");
        menu.addActionListener(e -> {
            dlg.dispose();
            top.dispose();
            LevelSelection.start();           // hier ggf. Levelauswahl starten
        });
        p.add(menu);
        dlg.add(p);
        dlg.setVisible(true);
    }

    // Getter für Level‐Painting
    public int[][] getPlatforms() { return level.getPlatforms(); }
    public int[][] getHoles()     { return level.getHoles(); }
    public int   getBLOCK_SIZE()  { return BLOCK_SIZE; }
    public int   getLEVEL_LENGTH(){ return LEVEL_LENGTH; }
    public int   getPLAYER_WIDTH(){ return PLAYER_WIDTH; }
    public int   getPLAYER_HEIGHT(){ return PLAYER_HEIGHT; }
    public int   getOffsetX()     { return offsetX; }
    public int   getPlayerOffsetY(){ return playerOffsetY; }

    // neu: Pause‐Dialog erstellen und anzeigen
    private void showPauseMenu() {
        if (pauseDialog != null && pauseDialog.isShowing()) return;
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        pauseDialog = new JDialog(topFrame, "Pause", true);
        pauseDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pauseDialog.setSize(300, 150);
        pauseDialog.setLocationRelativeTo(panel);

        JPanel pnl = new JPanel();
        JButton resume = new JButton("Fortsetzen");
       // SPACE deaktivieren
       resume.getInputMap(JComponent.WHEN_FOCUSED)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "none");
        JButton quit   = new JButton("Spiel beenden");
       quit.getInputMap(JComponent.WHEN_FOCUSED)
           .put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "none");
        resume.addActionListener(a -> {
            pauseDialog.dispose();
            panel.requestFocusInWindow();
        });
        quit.addActionListener(a -> {
            pauseDialog.dispose();
            topFrame.dispose();
            Mario.start();
        });
        pnl.add(resume);
        pnl.add(quit);
        pauseDialog.add(pnl);
        pauseDialog.setVisible(true);
    }

    // neu: Player-Bild zeichnen
    public void drawPlayer(Graphics g, int w, int h) {
        int bs      = BLOCK_SIZE;
        int groundY = h - bs;
        int px      = w/2 - PLAYER_WIDTH/2;
        int py      = groundY - PLAYER_HEIGHT + playerOffsetY;
        if (facingLeft) {
            // gespiegelt zeichnen
            g.drawImage(playerImage,
                px + PLAYER_WIDTH, py,
                -PLAYER_WIDTH, PLAYER_HEIGHT,
                null);
        } else {
            g.drawImage(playerImage, px, py, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        }
    }

    // Items zeichnen
    public void drawItems(Graphics g) {
        // Items übernimmt jetzt das Level selbst
        level.drawItems(g);
    }

    // neu: Wechsel zu Fire-Mario
    public void applyFireMario() {
        fireMode = true;
        ImageIcon icon = new ImageIcon(getClass()
            .getResource("/games/Mario/Feuermario.png"));
        playerImage = icon.getImage()
                          .getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_SMOOTH);
        panel.repaint();
    }

    // neu: Zugriff auf verbrauchte Blocks für’s Drawing
    public Set<Integer> getUsedBlackBlocks() {
        return Collections.unmodifiableSet(usedBlackBlocks);
    }
}