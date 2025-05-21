package games.Mario;

import java.awt.event.*;           // KeyAdapter, KeyEvent, ActionListener
import javax.swing.*;              // JPanel, Timer, JDialog, JButton, JFrame, SwingUtilities

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

    // Plattformen: {StartBlockX, HöheInBlöcken, LängeInBlöcken}
    private final int[][] platforms = {
        {  5,1,4},{ 12,2,3},{ 20,1,5},{ 30,3,2},
        { 45,1,3},{ 55,2,4},{ 65,1,6},{ 75,2,3},
        { 85,1,5},{ 95,3,4},{105,1,8},{115,2,5},
        {125,1,4},{135,3,5},{145,2,4},{155,1,6},
        {165,2,3},{175,1,5},{185,3,4},{195,2,2}
    };
    // Boden-Löcher: {StartBlock, Länge}
    private final int[][] holes = {
        {10,2},{50,3},{120,1}
    };

    private final JPanel panel;      // neu: Panel‐Referenz
    private JDialog pauseDialog;     // neu: Pause‐Dialog

    public Engine(JPanel panel) {
        this.panel = panel;          // Panel speichern
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
                for(int[] hole : holes) {
                    if(bi >= hole[0] && bi < hole[0] + hole[1]) { overHole = true; break; }
                }
                support = !overHole;
            } else {
                // Plattformunterstützung prüfen
                int playerY = groundY + playerOffsetY;
                for(int[] p : platforms) {
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
            for(int[] hole: holes) {
                if(bi>=hole[0] && bi<hole[0]+hole[1]) { overHole = true; break; }
            }
            if(!overHole) { playerOffsetY=0; playerVelocityY=0; onGround=true; }
        }
        // Plattform-Kollision beim Fallen
        if(playerVelocityY>0) {
            int playerBottom = groundY + playerOffsetY;
            int prevBottom   = playerBottom - playerVelocityY;
            int px = w/2 - PLAYER_WIDTH/2;
            for(int[] p: platforms) {
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
    }

    // Getter für Level1.paintComponent()
    public int getOffsetX() { return offsetX; }
    public int getPlayerOffsetY() { return playerOffsetY; }
    public int getBLOCK_SIZE() { return BLOCK_SIZE; }
    public int getLEVEL_LENGTH() { return LEVEL_LENGTH; }
    public int[][] getPlatforms() { return platforms; }
    public int[][] getHoles() { return holes; }
    public int getPLAYER_WIDTH() { return PLAYER_WIDTH; }
    public int getPLAYER_HEIGHT(){ return PLAYER_HEIGHT; }

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
}
