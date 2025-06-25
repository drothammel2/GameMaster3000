package games.Speedrun;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

public class GameplayState implements GameState {
    private static final int TILE_SIZE = 48;

    private final SpeedrunPanel panel;
    private final InputManager input;
    private GameMap gameMap;
    private List<Entity> entities = new ArrayList<>();
    private Player player;
    private boolean gameWon = false;
    private long startTime, endTime;
    private boolean showMap = false;
    private boolean showMiniMap = false; // minimap is hidden by default
    private boolean paused = false;
    private boolean wasdMode = true; // default to WASD
    private boolean showInventory = false;

    // Smooth camera
    private float cameraX, cameraY;
    private static final float CAMERA_LERP = 0.15f; // 0..1, higher = faster camera

    private long lastUpdateTime = System.nanoTime();
    private final Runnable onExitToMenu;
    private final JFrame frame;

    public GameplayState(SpeedrunPanel panel, Runnable onExitToMenu, JFrame frame) {
        this.panel = panel;
        this.input = new InputManager();
        this.onExitToMenu = onExitToMenu;
        this.frame = frame;
        // Use 100x100 for random, but .txt will override for file maps
        gameMap = new GameMap(100, 100, TILE_SIZE);
        player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
        entities.add(player);
        cameraX = player.x;
        cameraY = player.y;
        lastUpdateTime = System.nanoTime();
    }

    public GameplayState(SpeedrunPanel panel, Runnable onExitToMenu, JFrame frame, boolean randomMap) {
        this.panel = panel;
        this.input = new InputManager();
        this.onExitToMenu = onExitToMenu;
        this.frame = frame;
        if (randomMap) {
            gameMap = new GameMap(100, 100, TILE_SIZE); // or use your random logic
        } else {
            gameMap = new GameMap(100, 100, TILE_SIZE); // or load normal map
        }
        player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
        entities.add(player);
        cameraX = player.x;
        cameraY = player.y;
        lastUpdateTime = System.nanoTime();
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        float deltaTime = (now - lastUpdateTime) / 1_000_000_000f;
        lastUpdateTime = now;

        if (!gameWon) {
            for (Entity e : entities) {
                e.update(deltaTime);
            }
            // Smoothly move camera towards player
            cameraX += (player.x - cameraX) * CAMERA_LERP;
            cameraY += (player.y - cameraY) * CAMERA_LERP;

            if (gameMap.checkWin(player.getX(), player.getY())) {
                endTime = System.currentTimeMillis();
                gameWon = true;
            }
        }
    }

    @Override
    public void render(Graphics g, int width, int height) {
        int viewCols = Math.min(width / TILE_SIZE, gameMap.getCols());
        int viewRows = Math.min(height / TILE_SIZE, gameMap.getRows());

        // Camera offset in tiles (float, so we can scroll smoothly)
        float camOffsetX = cameraX - viewCols / 2f + 0.5f;
        float camOffsetY = cameraY - viewRows / 2f + 0.5f;

        // Clamp camera to map bounds (ensure no black bars)
        camOffsetX = Math.max(0, Math.min(camOffsetX, Math.max(0, gameMap.getCols() - viewCols)));
        camOffsetY = Math.max(0, Math.min(camOffsetY, Math.max(0, gameMap.getRows() - viewRows)));

        // Draw map and entities with smooth offset
        // Use viewCols+2, viewRows+2 to cover edge cases for partial tiles
        gameMap.drawSmooth(g, camOffsetX, camOffsetY, viewCols + 2, viewRows + 2, TILE_SIZE, entities);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Keys: " + gameMap.getFoundKeys() + "/" + gameMap.getTotalKeys(), 30, 40);

        if (gameWon) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("SPEEDRUN FINISHED!", 200, 200);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Zeit: " + ((endTime - startTime) / 1000.0) + " Sekunden", 200, 270);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Drücke ENTER für neuen Run", 200, 340);
        }

        if (showMiniMap) {
            gameMap.drawMiniMap(g, width, height, player);
        }
        if (showMap) {
            gameMap.drawMapOverlay(g, width, height, player);
        }
        if (showInventory) {
            drawInventory(g, width, height);
        }
        if (paused) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("PAUSED", width / 2 - 150, height / 2 - 20);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            int menuY = height / 2 + 40;
            int menuX = width / 2 - 260;
            g.drawString("ESC: Resume   Q: Quit   C: Change controls   K: Main Menu", menuX, menuY);
            g.drawString("R: Reload random map   W: Worldbuilder map", menuX, menuY + 40);
        }
    }

    private void drawInventory(Graphics g, int width, int height) {
        // Größeres Inventar-Overlay oben links
        int boxW = 320, boxH = 180;
        int x = 30, y = 60;
        g.setColor(new Color(30, 30, 30, 230));
        g.fillRoundRect(x, y, boxW, boxH, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("Inventar", x + 16, y + 36);
        int iconY = y + 60;
        int iconX = x + 30;
        int iconSize = 48;
        int spacingY = 54;
        // Keys (immer sichtbar, zeigt Anzahl)
        g.drawImage(gameMap.getKeyIcon(), iconX, iconY, iconSize, iconSize, null);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("x " + gameMap.getFoundKeys() + " / " + gameMap.getTotalKeys(), iconX + 60, iconY + 32);
        // Axe (nur wenn eingesammelt)
        iconY += spacingY;
        if (gameMap.hasAxe()) {
            g.drawImage(gameMap.getAxeIcon(), iconX, iconY, iconSize, iconSize, null);
            g.setColor(Color.GREEN);
            g.drawString("Axt", iconX + 60, iconY + 32);
        }
        // Pickaxe (nur wenn eingesammelt)
        iconY += spacingY;
        if (gameMap.hasPickaxe()) {
            g.drawImage(gameMap.getPickaxeIcon(), iconX, iconY, iconSize, iconSize, null);
            g.setColor(Color.GREEN);
            g.drawString("Spitzhacke", iconX + 60, iconY + 32);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (paused) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = false;
            } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                System.exit(0);
            } else if (e.getKeyCode() == KeyEvent.VK_K) {
                if (onExitToMenu != null) {
                    frame.dispose();
                    onExitToMenu.run();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_C) {
                wasdMode = !wasdMode;
                player.setUseWASD(wasdMode);
            } else if (e.getKeyCode() == KeyEvent.VK_R) {
                // Reload with random map
                gameMap.reload(GameMap.MapSource.RANDOM, null);
                entities.clear();
                player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
                entities.add(player);
                cameraX = player.x;
                cameraY = player.y;
                gameWon = false;
                startTime = System.currentTimeMillis();
                paused = false;
            } else if (e.getKeyCode() == KeyEvent.VK_W) {
                // Switch to worldbuilder mode
                panel.setState(new WorldbuilderState(panel, onExitToMenu, frame));
            }
        } else {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = true;
            }
            if (gameWon) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gameMap.reset();
                    entities.clear();
                    player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
                    entities.add(player);
                    gameWon = false;
                    startTime = System.currentTimeMillis();
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_M) {
                showMiniMap = !showMiniMap;
            }
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                showMap = !showMap;
            }
            if (e.getKeyCode() == KeyEvent.VK_I) {
                showInventory = !showInventory;
            }
            input.keyPressed(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!paused) {
            input.keyReleased(e.getKeyCode());
        }
    }

    // --- Maus-Handling für Baumfällen ---
    public void mousePressed(java.awt.event.MouseEvent e) {
        if (gameWon || paused) return;
        if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            int width = panel.getWidth();
            int height = panel.getHeight();
            int viewCols = Math.min(width / TILE_SIZE, gameMap.getCols());
            int viewRows = Math.min(height / TILE_SIZE, gameMap.getRows());
            float camOffsetX = cameraX - viewCols / 2f + 0.5f;
            float camOffsetY = cameraY - viewRows / 2f + 0.5f;
            int mx = (int)((e.getX() / (float)TILE_SIZE) + camOffsetX);
            int my = (int)((e.getY() / (float)TILE_SIZE) + camOffsetY);
            int px = player.getX();
            int py = player.getY();
            if (gameMap.hasAxe() && isNeighbor(mx, my, px, py)) {
                gameMap.fellTree(mx, my);
            }
            // Spitzhacke: Erde (017) abbauen
            if (gameMap.hasPickaxe() && isNeighbor(mx, my, px, py) && gameMap.getTile(mx, my) == 17) {
                gameMap.mineBlock(mx, my);
            }
            // Tür öffnen: Nur wenn alle Schlüssel, vor Tür und Mausklick auf Tür
            if (gameMap.getFoundKeys() == gameMap.getTotalKeys()
                && isNeighbor(mx, my, px, py)
                && gameMap.getTile(mx, my) == 0) { // 0 = grass00 = Tür
                gameMap.openAllDoors();
            }
        }
    }
    private boolean isNeighbor(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx + dy == 1); // Nur direkt angrenzend
    }
}