package games.Mario;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import games.Mario.Mario;

public class Engine {
    private final int BLOCK_SIZE   = 50;
    private final int LEVEL_LENGTH = 200;
    private final int MOVE_STEP    = 10;
    private final int PLAYER_WIDTH = 30;
    private final int PLAYER_HEIGHT= 50;
    private final int GRAVITY      = 1;

    private int offsetX        = 0;
    private int playerOffsetY  = 0; // 0 = Boden
    private int playerVelocityY= 0;
    private boolean onGround   = true;
    private boolean movingLeft, movingRight;

    private JDialog pauseDialog;
    private final JPanel panel;
    private final LevelBehavior level;
    private Image playerImage;

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
            case KeyEvent.VK_RIGHT: movingRight = press; break;
            case KeyEvent.VK_LEFT:  movingLeft  = press; break;
            case KeyEvent.VK_SPACE:
                if(onGround && press){ playerVelocityY = -20; onGround = false; }
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
                    int ex = sx + p[2] * BLOCK_SIZE;
                    if(playerY == platTop && px + PLAYER_WIDTH > sx && px < ex) {
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
        // Plattform-Kollision beim Fallen
        if(playerVelocityY>0) {
            int playerBottom = groundY + playerOffsetY;
            int prevBottom   = playerBottom - playerVelocityY;
            int px = w/2 - PLAYER_WIDTH/2;
            for(int[] p: level.getPlatforms()) {
                int platTop = groundY - p[1]*BLOCK_SIZE;
                int sx      = p[0]*BLOCK_SIZE - offsetX;
                int ex      = sx + p[2]*BLOCK_SIZE;
                if(px+PLAYER_WIDTH>sx && px<ex
                   && prevBottom<platTop && playerBottom>=platTop) {
                    onGround=true; playerVelocityY=0;
                    playerOffsetY = platTop - groundY;
                    break;
                }
            }
        }
        // Item-Kollision prüfen und ggf. Power-Up anwenden
        level.updateItems(this, w, h);
        respawnIfFallenBelow(w, h);
    }

    private void respawnIfFallenBelow(int w, int h) {
        if ((h - BLOCK_SIZE) + playerOffsetY > h) {
            offsetX         = 0;
            playerOffsetY   = 0;
            playerVelocityY = 0;
            onGround        = true;
        }
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
        JButton quit   = new JButton("Spiel beenden");
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
        g.drawImage(playerImage, px, py, PLAYER_WIDTH, PLAYER_HEIGHT, null);
    }

    // Items zeichnen
    public void drawItems(Graphics g) {
        // Items übernimmt jetzt das Level selbst
        level.drawItems(g);
    }

    // neu: Wechsel zu Fire-Mario
    public void applyFireMario() {
        System.out.println("Power-Up: Fire Mario aktiviert");
        ImageIcon icon = new ImageIcon(getClass().getResource("/games/Mario/Feuermario.png"));
        playerImage = icon.getImage()
                          .getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_SMOOTH);
        panel.repaint(); // sofort neu zeichnen
    }
}
