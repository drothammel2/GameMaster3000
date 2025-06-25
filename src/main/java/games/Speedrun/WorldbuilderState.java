package games.Speedrun;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

public class WorldbuilderState implements GameState {
    private final SpeedrunPanel panel;
    private final Runnable onExitToMenu;
    private final JFrame frame;
    private GameMap gameMap;
    private boolean paused = false;
    private boolean wasdMode = true;
    private float cameraX, cameraY;
    private Player player;
    private long startTime;
    private java.util.List<Entity> entities = new java.util.ArrayList<>();
    private InputManager input = new InputManager();
    private boolean showMinimap = false;
    private boolean tileMenuOpen = false;
    private String tileInputBuffer = "";
    private int selectedTileIndex = 0;
    private int tileCount = 38; // Number of tile images
    private static final int TILE_PICKER_ROW = 5; // Number of tiles in row

    private static final int TILE_SIZE = 48;
    private static final float CAMERA_LERP = 0.15f;
    private long lastUpdateTime = System.nanoTime();
    private boolean mouseDown = false;

    public WorldbuilderState(SpeedrunPanel panel, Runnable onExitToMenu, JFrame frame) {
        this.panel = panel;
        this.onExitToMenu = onExitToMenu;
        this.frame = frame;
        // Load worldbuilder map (previous behavior: load from file)
        gameMap = new GameMap(100, 100, 48);
        gameMap.reload(GameMap.MapSource.FILE, "games/Speedrun/resources/worldbuilder.txt");
        input = new InputManager();
        player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
        player.setPhaseMode(true);
        cameraX = player.x;
        cameraY = player.y;
        startTime = System.currentTimeMillis();
        entities.add(player);
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        float deltaTime = (now - lastUpdateTime) / 1_000_000_000f;
        lastUpdateTime = now;
        // Allow player movement in worldbuilder mode
        player.update(deltaTime);
        // Smooth camera movement
        cameraX += (player.x - cameraX) * CAMERA_LERP;
        cameraY += (player.y - cameraY) * CAMERA_LERP;
    }

    @Override
    public void render(Graphics g, int width, int height) {
        int viewCols = Math.min(width / 48, gameMap.getCols());
        int viewRows = Math.min(height / 48, gameMap.getRows());
        float camOffsetX = cameraX - viewCols / 2f + 0.5f;
        float camOffsetY = cameraY - viewRows / 2f + 0.5f;
        camOffsetX = Math.max(0, Math.min(camOffsetX, Math.max(0, gameMap.getCols() - viewCols)));
        camOffsetY = Math.max(0, Math.min(camOffsetY, Math.max(0, gameMap.getRows() - viewRows)));
        gameMap.drawSmooth(g, camOffsetX, camOffsetY, viewCols + 2, viewRows + 2, 48, entities);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Worldbuilder Mode", 30, 40);
        if (showMinimap) {
            drawMinimap(g, width, height);
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
            g.drawString("ESC: Resume   Q: Quit   C: Change controls   R: Random map   N: Normal mode   K: Main Menu", menuX, menuY);
        }
        if (tileMenuOpen) {
            drawTileMenu(g, width, height);
        }
    }

    private void drawMinimap(Graphics g, int width, int height) {
        int mapCols = gameMap.getCols();
        int mapRows = gameMap.getRows();
        int minimapSize = Math.min(width, height) / 3;
        int tileSize = Math.max(1, minimapSize / Math.max(mapCols, mapRows));
        int minimapX = width - minimapSize - 20;
        int minimapY = 20;
        // Draw minimap background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(minimapX - 4, minimapY - 4, minimapSize + 8, minimapSize + 8);
        // Draw map tiles as images
        Image[] tileImages = null;
        try {
            java.lang.reflect.Field tileImagesField = gameMap.getClass().getDeclaredField("tileImages");
            tileImagesField.setAccessible(true);
            tileImages = (Image[]) tileImagesField.get(gameMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (int y = 0; y < mapRows; y++) {
            for (int x = 0; x < mapCols; x++) {
                int tile = gameMap.getTile(x, y);
                int drawX = minimapX + x * tileSize;
                int drawY = minimapY + y * tileSize;
                if (tileImages != null && tile >= 0 && tile < tileImages.length && tileImages[tile] != null) {
                    g.drawImage(tileImages[tile], drawX, drawY, tileSize, tileSize, null);
                } else {
                    g.setColor(tile == 0 ? Color.DARK_GRAY : Color.LIGHT_GRAY);
                    g.fillRect(drawX, drawY, tileSize, tileSize);
                }
            }
        }
        // Draw player position
        g.setColor(Color.RED);
        int px = (int) (minimapX + player.x * tileSize);
        int py = (int) (minimapY + player.y * tileSize);
        g.fillOval(px, py, tileSize, tileSize);
        g.setColor(Color.WHITE);
        g.drawRect(minimapX, minimapY, mapCols * tileSize, mapRows * tileSize);
    }

    private void drawTileMenu(Graphics g, int width, int height) {
        int menuW = 520, menuH = 320;
        int x = width / 2 - menuW / 2;
        int y = height / 2 - menuH / 2;
        g.setColor(new Color(0, 0, 0, 230));
        g.fillRect(x, y, menuW, menuH);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Tile Selection", x + 20, y + 40);
        // Draw current tile number
        g.setFont(new Font("Arial", Font.BOLD, 32));
        String tileNumStr = "Tile: " + selectedTileIndex;
        int numW = g.getFontMetrics().stringWidth(tileNumStr);
        g.drawString(tileNumStr, x + menuW / 2 - numW / 2, y + 90);
        // Draw tile picker row
        int tileImgSize = 80;
        int rowY = y + 120;
        int rowW = TILE_PICKER_ROW * tileImgSize + (TILE_PICKER_ROW - 1) * 16;
        int rowX = x + menuW / 2 - rowW / 2;
        Image[] tileImages = null;
        try {
            java.lang.reflect.Field tileImagesField = gameMap.getClass().getDeclaredField("tileImages");
            tileImagesField.setAccessible(true);
            tileImages = (Image[]) tileImagesField.get(gameMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        int half = TILE_PICKER_ROW / 2;
        for (int i = -half; i <= half; i++) {
            int idx = (selectedTileIndex + i + tileCount) % tileCount;
            int tx = rowX + (i + half) * (tileImgSize + 16);
            int ty = rowY;
            if (tileImages != null && idx >= 0 && idx < tileImages.length) {
                g.drawImage(tileImages[idx], tx, ty, tileImgSize, tileImgSize, null);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(tx, ty, tileImgSize, tileImgSize);
            }
            // Highlight selected
            if (i == 0) {
                g.setColor(Color.YELLOW);
                g.drawRect(tx - 3, ty - 3, tileImgSize + 6, tileImgSize + 6);
                g.setColor(Color.WHITE);
            }
            // Draw tile number below
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String numStr = String.valueOf(idx);
            int numStrW = g.getFontMetrics().stringWidth(numStr);
            g.drawString(numStr, tx + tileImgSize / 2 - numStrW / 2, ty + tileImgSize + 22);
        }
        // Draw left/right arrows
        int arrowY = rowY + tileImgSize / 2;
        int arrowSize = 32;
        int leftArrowX = rowX - 50;
        int rightArrowX = rowX + rowW + 18;
        g.setColor(Color.WHITE);
        // Left arrow
        int[] lx = {leftArrowX + arrowSize, leftArrowX, leftArrowX + arrowSize};
        int[] ly = {arrowY - arrowSize / 2, arrowY, arrowY + arrowSize / 2};
        g.fillPolygon(lx, ly, 3);
        // Right arrow
        int[] rx = {rightArrowX, rightArrowX + arrowSize, rightArrowX};
        int[] ry = {arrowY - arrowSize / 2, arrowY, arrowY + arrowSize / 2};
        g.fillPolygon(rx, ry, 3);
        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Left/Right: Change tile   Enter: OK   Esc: Cancel", x + 20, y + menuH - 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (tileMenuOpen) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                tileMenuOpen = false;
                tileInputBuffer = "";
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                tileMenuOpen = false;
                tileInputBuffer = "";
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                selectedTileIndex = (selectedTileIndex - 1 + tileCount) % tileCount;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                selectedTileIndex = (selectedTileIndex + 1) % tileCount;
            } else if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) {
                // Allow direct number entry (optional, keep for power users)
                if (tileInputBuffer.length() < 3) {
                    tileInputBuffer += (char) e.getKeyChar();
                    try {
                        int idx = Integer.parseInt(tileInputBuffer);
                        if (idx >= 0 && idx < tileCount) selectedTileIndex = idx;
                    } catch (Exception ex) {}
                }
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && tileInputBuffer.length() > 0) {
                tileInputBuffer = tileInputBuffer.substring(0, tileInputBuffer.length() - 1);
            }
            return;
        }
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
            } else if (e.getKeyCode() == KeyEvent.VK_R) {
                // Switch to random map gameplay
                panel.setState(new GameplayState(panel, onExitToMenu, frame, true));
            } else if (e.getKeyCode() == KeyEvent.VK_N) {
                // Switch to normal gameplay
                panel.setState(new GameplayState(panel, onExitToMenu, frame, false));
            }
        } else {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = true;
            } else if (e.getKeyCode() == KeyEvent.VK_M) {
                showMinimap = !showMinimap;
            } else if (e.getKeyCode() == KeyEvent.VK_F) { // changed from P to F
                tileMenuOpen = true;
                tileInputBuffer = "";
                return;
            } else if (e.getKeyCode() == KeyEvent.VK_V) { // changed from O to V
                int px = Math.round(player.x);
                int py = Math.round(player.y);
                if (px >= 0 && px < gameMap.getCols() && py >= 0 && py < gameMap.getRows()) {
                    // Set tile in memory
                    setTileAndSave(px, py, selectedTileIndex);
                }
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

    // Add mouse support for placing tiles
    public void mousePressed(java.awt.event.MouseEvent e) {
        if (!paused && !tileMenuOpen && e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            mouseDown = true;
            placeTileAtMouse(e);
        }
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
        if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            mouseDown = false;
        }
    }

    public void mouseDragged(java.awt.event.MouseEvent e) {
        if (mouseDown && !paused && !tileMenuOpen) {
            placeTileAtMouse(e);
        }
    }

    private void placeTileAtMouse(java.awt.event.MouseEvent e) {
        int width = panel.getWidth();
        int height = panel.getHeight();
        int viewCols = Math.min(width / 48, gameMap.getCols());
        int viewRows = Math.min(height / 48, gameMap.getRows());
        float camOffsetX = cameraX - viewCols / 2f + 0.5f;
        float camOffsetY = cameraY - viewRows / 2f + 0.5f;
        int tileX = (int)(camOffsetX + (e.getX() / 48f));
        int tileY = (int)(camOffsetY + (e.getY() / 48f));
        if (tileX >= 0 && tileX < gameMap.getCols() && tileY >= 0 && tileY < gameMap.getRows()) {
            setTileAndSave(tileX, tileY, selectedTileIndex);
        }
    }

    private void setTileAndSave(int x, int y, int tileIdx) {
        // Set in memory
        try {
            java.lang.reflect.Field mapField = gameMap.getClass().getDeclaredField("map");
            mapField.setAccessible(true);
            int[][] mapArr = (int[][]) mapField.get(gameMap);
            mapArr[y][x] = tileIdx;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Save to file
        saveMapToFile();
    }

    private void saveMapToFile() {
        try {
            java.lang.reflect.Field mapField = gameMap.getClass().getDeclaredField("map");
            mapField.setAccessible(true);
            int[][] mapArr = (int[][]) mapField.get(gameMap);
            int rows = gameMap.getRows();
            int cols = gameMap.getCols();
            java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("src/main/java/games/Speedrun/resources/worldbuilder.txt"));
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    pw.print(mapArr[y][x]);
                    if (x < cols - 1) pw.print(" ");
                }
                pw.println();
            }
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
