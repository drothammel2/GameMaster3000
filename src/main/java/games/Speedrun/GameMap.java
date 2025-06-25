package games.Speedrun;

import java.awt.*;
import java.util.Random;
import java.util.List;
import java.io.*;

public class GameMap {
    private int rows;
    private int cols;
    private final int tileSize;

    private int[][] map; // now stores tile indices (0..37)
    private boolean[][] visited;
    private int playerX, playerY;
    private int foundKeys = 0;
    private final int totalKeys = 4; // Jetzt 4 Keys: 2 spawnen, 1 durch Bäume, 1 durch Mining

    private Point[] keyPositions;
    private boolean[] keyCollected;

    private Point treasureDoor;
    private boolean treasureOpen = false;
    private Point houseTopLeft;
    private int houseW = 7, houseH = 7;

    // Sprites: all 38 tile textures (000.png to 037.png)
    private Image[] tileImages = new Image[38];
    private Image key, door, chest, axe, pickaxe;

    // Named indices for main tiles
    private final int grass00 = 0;   // 000.png
    private final int road03 = 3;    // 003.png
    private final int earth17 = 17;  // 017.png
    private final int water00 = 18;  // 018.png
    private final int water01 = 19;  // 019.png
    private final int tree16 = 16;   // 016.png
    private final int stone32 = 32;  // 032.png
    private final int hut33 = 33;    // 033.png

    // IDs für spezielle Tiles (20-27 außer 26)
    private final int tile20 = 20;
    private final int tile21 = 21;
    private final int tile22 = 22;
    private final int tile23 = 23;
    private final int tile24 = 24;
    private final int tile25 = 25;
    // tile26 bleibt begehbar
    private final int tile27 = 27;

    public enum MapSource {
        FILE, RANDOM
    }

    private MapSource mapSource;
    private String mapFilePath;

    // Track if the door is open (for all doors on grass00)
    private boolean doorsOpen = false;

    // Add fields for axe and pickaxe positions
    private Point axePosition;
    private Point pickaxePosition;

    // --- Backup-Mechanismus für Map ---
    private static final String BACKUP_MAP_PATH = "src/main/java/games/Speedrun/resources/world_backup.txt";
    private static final String ACTIVE_MAP_PATH = "src/main/java/games/Speedrun/resources/world.txt";

    public GameMap(int rows, int cols, int tileSize) {
        this(rows, cols, tileSize, MapSource.FILE, BACKUP_MAP_PATH);
    }

    public GameMap(int rows, int cols, int tileSize, MapSource source, String filePath) {
        this.tileSize = tileSize;
        this.mapSource = source;
        this.mapFilePath = filePath;
        this.rows = rows;
        this.cols = cols;
        loadSprites();
        // Immer aus world_backup.txt laden
        loadMapFromFile(BACKUP_MAP_PATH);
        this.keyPositions = new Point[totalKeys];
        this.keyCollected = new boolean[totalKeys];
        visit(playerX, playerY);
        spawnCollectables();
    }

    private void loadMapFromFile(String filePath) {
        try {
            BufferedReader br;
            File file = new File(filePath);
            if (file.exists()) {
                br = new BufferedReader(new FileReader(file));
            } else {
                InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
                if (in == null) throw new FileNotFoundException("Resource not found: " + filePath);
                br = new BufferedReader(new InputStreamReader(in));
            }
            java.util.List<String[]> lines = new java.util.ArrayList<>();
            String line;
            int maxCols = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                lines.add(tokens);
                if (tokens.length > maxCols) maxCols = tokens.length;
            }
            br.close();
            this.rows = lines.size();
            this.cols = maxCols;
            this.map = new int[rows][cols];
            this.visited = new boolean[rows][cols];
            for (int y = 0; y < rows; y++) {
                String[] tokens = lines.get(y);
                for (int x = 0; x < cols; x++) {
                    int idx = (x < tokens.length) ? parseTileIndex(tokens[x]) : grass00;
                    map[y][x] = idx;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load map from file: " + filePath, e);
        }
    }

    private void saveMapToFile(String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(filePath)))) {
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    pw.print(map[y][x]);
                    if (x < cols - 1) pw.print(" ");
                }
                pw.println();
            }
        } catch (Exception e) {
            System.err.println("Failed to save generated map: " + e.getMessage());
        }
    }

    // Parse tile index from string (1-based in file, 0-based in code)
    private int parseTileIndex(String token) {
        try {
            int idx = Integer.parseInt(token);
            if (idx >= 0 && idx < tileImages.length) return idx;
            if (idx >= 1 && idx <= tileImages.length) return idx - 1;
        } catch (Exception ignored) {}
        return grass00;
    }

    private void loadSprites() {
        for (int i = 0; i < tileImages.length; i++) {
            String num = String.format("%03d", i);
            String relPath = "src/main/java/games/Speedrun/resources/tiles-numbers/" + num + ".png";
            tileImages[i] = Toolkit.getDefaultToolkit().createImage(relPath);
            try {
                MediaTracker tracker = new MediaTracker(new Canvas());
                tracker.addImage(tileImages[i], 0);
                tracker.waitForID(0);
                if (tileImages[i].getWidth(null) <= 0 || tileImages[i].getHeight(null) <= 0) {
                    System.err.println("Tile image failed to load (bad file?): " + relPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading tile image: " + relPath);
            }
        }
        key = Toolkit.getDefaultToolkit().createImage("src/main/java/games/Speedrun/resources/objects/key.png");
        door = Toolkit.getDefaultToolkit().createImage("src/main/java/games/Speedrun/resources/objects/door.png");
        chest = Toolkit.getDefaultToolkit().createImage("src/main/java/games/Speedrun/resources/objects/chest.png");
        axe = Toolkit.getDefaultToolkit().createImage("src/main/java/games/Speedrun/resources/objects/axe.png");
        pickaxe = Toolkit.getDefaultToolkit().createImage("src/main/java/games/Speedrun/resources/objects/pickaxe.png");
    }

    private void generateMap() {
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                map[y][x] = grass00;
        for (int y = 0; y < rows; y++) {
            map[y][0] = water00;
            map[y][1] = water00;
            map[y][cols - 1] = water00;
            map[y][cols - 2] = water00;
        }
        for (int x = 0; x < cols; x++) {
            map[0][x] = water00;
            map[1][x] = water00;
            map[rows - 1][x] = water00;
            map[rows - 2][x] = water00;
        }
        for (int i = 0; i < 10; i++) {
            int cx = 10 + (int)(Math.random() * Math.max(1, cols - 20));
            int cy = 10 + (int)(Math.random() * Math.max(1, rows - 20));
            int r = 3 + (int)(Math.random() * 4);
            for (int y = cy - r; y <= cy + r; y++)
                for (int x = cx - r; x <= cx + r; x++)
                    if (x > 2 && x < cols - 3 && y > 2 && y < rows - 3)
                        if (Math.hypot(x - cx, y - cy) < r)
                            map[y][x] = earth17;
        }
        int roadY = rows / 2;
        for (int x = 2; x < cols - 2; x++)
            map[roadY][x] = road03;
        int roadX = cols / 2;
        for (int y = 2; y < rows - 2; y++)
            map[y][roadX] = road03;
        for (int i = 0; i < 6; i++) {
            int tries = 0;
            while (tries++ < 100) {
                int hx = 3 + (int)(Math.random() * Math.max(1, cols - 6));
                int hy = 3 + (int)(Math.random() * Math.max(1, rows - 6));
                boolean canPlace = true;
                for (int dy = 0; dy < 3; dy++)
                    for (int dx = 0; dx < 3; dx++)
                        if (hy + dy >= rows || hx + dx >= cols || map[hy + dy][hx + dx] != grass00)
                            canPlace = false;
                if (canPlace) {
                    for (int dy = 0; dy < 3; dy++)
                        for (int dx = 0; dx < 3; dx++)
                            map[hy + dy][hx + dx] = hut33;
                    break;
                }
            }
        }
        int wx = cols / 4;
        int wy = rows / 4;
        int wrx = 5, wry = 3;
        for (int y = wy - wry; y <= wy + wry; y++)
            for (int x = wx - wrx; x <= wx + wrx; x++)
                if (x > 2 && x < cols - 3 && y > 2 && y < rows - 3) {
                    double dx = (x - wx) / (double)wrx;
                    double dy = (y - wy) / (double)wry;
                    if (dx * dx + dy * dy <= 1.0) map[y][x] = water01;
                }
        for (int i = 0; i < Math.min(300, rows * cols / 2); i++) {
            int x = 2 + (int)(Math.random() * Math.max(1, cols - 4));
            int y = 2 + (int)(Math.random() * Math.max(1, rows - 4));
            if (map[y][x] == grass00) map[y][x] = tree16;
        }
        for (int i = 0; i < Math.min(200, rows * cols / 3); i++) {
            int x = 2 + (int)(Math.random() * Math.max(1, cols - 4));
            int y = 2 + (int)(Math.random() * Math.max(1, rows - 4));
            if (map[y][x] == grass00) map[y][x] = stone32;
        }
    }

    private void placePlayerAndHouse() {
        // entfernt: kein Haus mehr generieren
    }

    private void placeKeys() {
        // entfernt: keine Keys mehr generieren
    }

    // Konfigurierbare Spawn-Tile-IDs für Collectables
    private int keySpawnTile = 1;      // 001.png
    private int axeSpawnTile = 1;      // aktuell auch 001.png
    private int pickaxeSpawnTile = 1;  // aktuell auch 001.png

    // Spawn alle Keys, Axe, Pickaxe auf konfigurierbaren Tiles (aktuell 001.png)
    public void spawnCollectables() {
        System.out.println("hallo fatih");
        // Debug: clear all key positions and collected state
        for (int i = 0; i < totalKeys; i++) {
            keyPositions[i] = null;
            keyCollected[i] = false;
        }
        axePosition = null;
        pickaxePosition = null;
        // Sammle alle möglichen Spawn-Positionen für jedes Objekt
        java.util.List<Point> keyTiles = new java.util.ArrayList<>();
        java.util.List<Point> axeTiles = new java.util.ArrayList<>();
        java.util.List<Point> pickaxeTiles = new java.util.ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (map[y][x] == keySpawnTile) keyTiles.add(new Point(x, y));
                if (map[y][x] == axeSpawnTile) axeTiles.add(new Point(x, y));
                if (map[y][x] == pickaxeSpawnTile) pickaxeTiles.add(new Point(x, y));
            }
        }
        java.util.Collections.shuffle(keyTiles);
        java.util.Collections.shuffle(axeTiles);
        java.util.Collections.shuffle(pickaxeTiles);
        // Nur 2 Keys spawnen auf der Map (Index 0 und 1), die anderen 2 (2 und 3) werden durch Aktionen vergeben
        for (int i = 0; i < 2; i++) {
            if (!keyTiles.isEmpty()) {
                keyPositions[i] = keyTiles.remove(0);
                System.out.println("Key " + i + " spawned at: " + keyPositions[i].x + "," + keyPositions[i].y);
            } else {
                keyPositions[i] = null;
                System.out.println("No spawn tile for key " + i);
            }
        }
        // Die anderen beiden Keys werden nicht auf der Map platziert
        for (int i = 2; i < totalKeys; i++) {
            keyPositions[i] = null;
        }
        // Axe
        if (!axeTiles.isEmpty()) {
            axePosition = axeTiles.remove(0);
            System.out.println("Axe spawned at: " + axePosition.x + "," + axePosition.y);
        } else {
            axePosition = null;
            System.out.println("No spawn tile for axe");
        }
        // Pickaxe
        if (!pickaxeTiles.isEmpty()) {
            pickaxePosition = pickaxeTiles.remove(0);
            System.out.println("Pickaxe spawned at: " + pickaxePosition.x + "," + pickaxePosition.y);
        } else {
            pickaxePosition = null;
            System.out.println("No spawn tile for pickaxe");
        }
    }

    // Draws all objects (door, key, chest, etc.) on top of the tile background
    private void drawObjects(Graphics g, int x, int y, int px, int py, int tileSize) {
        // Draw door if tile is grass00 (000.png)
        if (map[y][x] == grass00) {
            g.drawImage(door, px, py, tileSize, tileSize, null);
        }
        // Draw key if present
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i] && keyPositions[i] != null && keyPositions[i].x == x && keyPositions[i].y == y) {
                g.drawImage(key, px, py, tileSize, tileSize, null);
            }
        }
        // Draw axe
        if (axePosition != null && axePosition.x == x && axePosition.y == y) {
            g.drawImage(axe, px, py, tileSize, tileSize, null);
        }
        // Draw pickaxe
        if (pickaxePosition != null && pickaxePosition.x == x && pickaxePosition.y == y) {
            g.drawImage(pickaxe, px, py, tileSize, tileSize, null);
        }
    }

    public void draw(Graphics g, int vx, int vy, int viewCols, int viewRows, int tileSize, Player player) {
        for (int y = vy; y < vy + viewRows && y < rows; y++) {
            for (int x = vx; x < vx + viewCols && x < cols; x++) {
                int px = (x - vx) * tileSize;
                int py = (y - vy) * tileSize;
                int tileIdx = map[y][x];
                if (tileIdx >= 0 && tileIdx < tileImages.length && tileImages[tileIdx] != null) {
                    g.drawImage(tileImages[tileIdx], px, py, tileSize, tileSize, null);
                } else {
                    g.setColor(Color.MAGENTA);
                    g.fillRect(px, py, tileSize, tileSize);
                }
                drawObjects(g, x, y, px, py, tileSize);
            }
        }
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i]) {
                Point p = keyPositions[i];
                if (p != null && isInView(p.x, p.y, vx, vy, viewCols, viewRows)) {
                    int px = (p.x - vx) * tileSize;
                    int py = (p.y - vy) * tileSize;
                    g.drawImage(key, px, py, tileSize, tileSize, null);
                }
            }
        }
        int chestX = houseTopLeft != null ? houseTopLeft.x + houseW / 2 : -1;
        int chestY = houseTopLeft != null ? houseTopLeft.y + houseH / 2 : -1;
        if (isInView(chestX, chestY, vx, vy, viewCols, viewRows) && treasureOpen && houseTopLeft != null) {
            int px = (chestX - vx) * tileSize;
            int py = (chestY - vy) * tileSize;
            g.drawImage(chest, px, py, tileSize, tileSize, null);
        }
        int playerPx = (player.getX() - vx) * tileSize;
        int playerPy = (player.getY() - vy) * tileSize;
        player.render(g, playerPx, playerPy, tileSize);
    }

    public void drawSmooth(Graphics g, float camOffsetX, float camOffsetY, int viewCols, int viewRows, int tileSize, List<Entity> entities) {
        int startCol = (int)Math.floor(camOffsetX);
        int startRow = (int)Math.floor(camOffsetY);
        float offsetX = (camOffsetX - startCol) * tileSize;
        float offsetY = (camOffsetY - startRow) * tileSize;

        for (int y = 0; y < viewRows && (startRow + y) < rows; y++) {
            for (int x = 0; x < viewCols && (startCol + x) < cols; x++) {
                int px = Math.round(x * tileSize - offsetX);
                int py = Math.round(y * tileSize - offsetY);
                int mapX = startCol + x;
                int mapY = startRow + y;
                if (mapX < 0 || mapY < 0 || mapX >= cols || mapY >= rows) continue;
                int tileIdx = map[mapY][mapX];
                if (tileIdx >= 0 && tileIdx < tileImages.length && tileImages[tileIdx] != null) {
                    g.drawImage(tileImages[tileIdx], px, py, tileSize, tileSize, null);
                } else {
                    g.setColor(Color.MAGENTA);
                    g.fillRect(px, py, tileSize, tileSize);
                }
                drawObjects(g, mapX, mapY, px, py, tileSize);
            }
        }
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i]) {
                Point p = keyPositions[i];
                if (p != null) {
                    float px = (p.x - camOffsetX) * tileSize;
                    float py = (p.y - camOffsetY) * tileSize;
                    g.drawImage(key, Math.round(px), Math.round(py), tileSize, tileSize, null);
                }
            }
        }
        int chestX = houseTopLeft != null ? houseTopLeft.x + houseW / 2 : -1;
        int chestY = houseTopLeft != null ? houseTopLeft.y + houseH / 2 : -1;
        if (treasureOpen && houseTopLeft != null) {
            float px = (chestX - camOffsetX) * tileSize;
            float py = (chestY - camOffsetY) * tileSize;
            g.drawImage(chest, Math.round(px), Math.round(py), tileSize, tileSize, null);
        }
        for (Entity e : entities) {
            float px = (e.x - camOffsetX) * tileSize;
            float py = (e.y - camOffsetY) * tileSize;
            e.render(g, Math.round(px), Math.round(py), tileSize);
        }
    }

    public void drawMapOverlay(Graphics g, int width, int height, Player player) {
        int mapW = Math.min(800, cols * 4);
        int mapH = Math.min(800, rows * 4);
        int ox = (width - mapW) / 2;
        int oy = (height - mapH) / 2;
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(ox - 10, oy - 10, mapW + 20, mapH + 20);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int px = ox + x * mapW / cols;
                int py = oy + y * mapH / rows;
                int s = mapW / cols;
                int tileIdx = map[y][x];
                if (!visited[y][x]) {
                    g.setColor(new Color(50, 50, 50, 120));
                    g.fillRect(px, py, s, s);
                } else if (tileIdx >= 0 && tileIdx < tileImages.length) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(px, py, s, s);
                } else {
                    g.setColor(Color.GRAY);
                    g.fillRect(px, py, s, s);
                }
            }
        }
        g.setColor(Color.YELLOW);
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i] && keyPositions[i] != null) {
                int px = ox + keyPositions[i].x * mapW / cols;
                int py = oy + keyPositions[i].y * mapH / rows;
                int s = mapW / cols;
                g.fillOval(px + s/4, py + s/4, s/2, s/2);
            }
        }
        g.setColor(Color.RED);
        int px = ox + player.getX() * mapW / cols;
        int py = oy + player.getY() * mapH / rows;
        int s = mapW / cols;
        g.fillOval(px + s/4, py + s/4, s/2, s/2);
        g.setColor(Color.WHITE);
        if (houseTopLeft != null) {
            int hx = ox + houseTopLeft.x * mapW / cols;
            int hy = oy + houseTopLeft.y * mapH / rows;
            int hw = houseW * mapW / cols;
            int hh = houseH * mapH / rows;
            g.drawRect(hx, hy, hw, hh);
        }
    }

    // Neue Methode: Minimap oben rechts mit echten Texturen
    public void drawMiniMap(Graphics g, int panelWidth, int panelHeight, Player player) {
        int miniTile = 4; // Kleinere Mini-Tiles (vorher 8)
        int margin = 20;
        int mapW = cols * miniTile;
        int mapH = rows * miniTile;
        int ox = panelWidth - mapW - margin;
        int oy = margin;
        // Hintergrund
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(ox - 6, oy - 6, mapW + 12, mapH + 12, 12, 12);
        // Tiles + objects
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int tileIdx = map[y][x];
                int px = ox + x * miniTile;
                int py = oy + y * miniTile;
                if (tileIdx >= 0 && tileIdx < tileImages.length && tileImages[tileIdx] != null) {
                    g.drawImage(tileImages[tileIdx], px, py, miniTile, miniTile, null);
                } else {
                    g.setColor(Color.MAGENTA);
                    g.fillRect(px, py, miniTile, miniTile);
                }
                // Draw door overlay if grass00
                if (tileIdx == grass00) {
                    g.drawImage(door, px, py, miniTile, miniTile, null);
                }
                // Draw key if present
                for (int i = 0; i < totalKeys; i++) {
                    if (!keyCollected[i] && keyPositions[i] != null && keyPositions[i].x == x && keyPositions[i].y == y) {
                        g.drawImage(key, px, py, miniTile, miniTile, null);
                    }
                }
                // Draw axe
                if (axePosition != null && axePosition.x == x && axePosition.y == y) {
                    g.drawImage(axe, px, py, miniTile, miniTile, null);
                }
                // Draw pickaxe
                if (pickaxePosition != null && pickaxePosition.x == x && pickaxePosition.y == y) {
                    g.drawImage(pickaxe, px, py, miniTile, miniTile, null);
                }
            }
        }
        // Truhe
        int chestX = houseTopLeft != null ? houseTopLeft.x + houseW / 2 : -1;
        int chestY = houseTopLeft != null ? houseTopLeft.y + houseH / 2 : -1;
        if (treasureOpen && houseTopLeft != null) {
            int px = ox + chestX * miniTile;
            int py = oy + chestY * miniTile;
            g.drawImage(chest, px, py, miniTile, miniTile, null);
        }
        // Spieler
        int px = ox + player.getX() * miniTile;
        int py = oy + player.getY() * miniTile;
        g.setColor(Color.RED);
        g.fillOval(px + 1, py + 1, miniTile - 2, miniTile - 2);
    }

    private boolean isInView(int x, int y, int vx, int vy, int viewCols, int viewRows) {
        return x >= vx && x < vx + viewCols && y >= vy && y < vy + viewRows;
    }

    // Call this to open all doors (e.g. after collecting all keys or by event)
    public void openAllDoors() {
        doorsOpen = true;
        // Ersetze alle Türen (grass00) mit earth17 (017)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (map[y][x] == grass00) {
                    map[y][x] = earth17;
                }
            }
        }
        saveActiveMap();
    }

    // Call this to close all doors (reset)
    public void closeAllDoors() {
        doorsOpen = false;
    }

    // Nach jeder Map-Änderung speichern wir in world.txt
    public void saveActiveMap() {
        saveMapToFile(ACTIVE_MAP_PATH);
    }

    public void reset() {
        this.map = new int[rows][cols];
        this.visited = new boolean[rows][cols];
        this.foundKeys = 0;
        this.treasureOpen = false;
        this.doorsOpen = false;
        this.axePosition = null;
        this.pickaxePosition = null;
        // Immer aus world_backup.txt laden
        loadMapFromFile(BACKUP_MAP_PATH);
        visit(playerX, playerY);
        spawnCollectables();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getFoundKeys() { return foundKeys; }
    public int getTotalKeys() { return totalKeys; }

    public boolean checkWin(int playerX, int playerY) {
        // Avoid NullPointerException if houseTopLeft is null (e.g. map too small for house)
        if (houseTopLeft == null) return false;
        int chestX = houseTopLeft.x + houseW / 2;
        int chestY = houseTopLeft.y + houseH / 2;
        return playerX == chestX && playerY == chestY && treasureOpen;
    }

    public boolean canMoveTo(int nx, int ny, Player.Direction dir) {
        if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) return false;
        int tile = map[ny][nx];
        // Block movement if door is present and not open
        if (tile == grass00 && !doorsOpen) return false;
        if (tile == -1 && foundKeys < totalKeys && !treasureOpen) return false;
        if (tile == stone32 || tile == water01 || tile == tree16 || tile == water00
            || tile == tile20 || tile == tile21 || tile == tile22 || tile == tile23
            || tile == tile24 || tile == tile25 || tile == tile27) return false;
        if (tile == -1 && foundKeys == totalKeys && !treasureOpen) {
            treasureOpen = true;
            return true;
        }
        return true;
    }

    public void visit(int x, int y) {
        int radius = 2;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    visited[ny][nx] = true;
                }
            }
        }
    }

    public void checkCollectables(int x, int y) {
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i] && keyPositions[i] != null && keyPositions[i].x == x && keyPositions[i].y == y) {
                keyCollected[i] = true;
                foundKeys++;
                // Open doors if all keys collected
                if (foundKeys == totalKeys) openAllDoors();
            }
        }
        if (axePosition != null && axePosition.x == x && axePosition.y == y) {
            axePosition = null; // collected
        }
        if (pickaxePosition != null && pickaxePosition.x == x && pickaxePosition.y == y) {
            pickaxePosition = null; // collected
        }
    }

    public void reload(MapSource source, String filePath) {
        this.mapSource = source;
        this.mapFilePath = filePath;
        this.map = new int[rows][cols];
        this.visited = new boolean[rows][cols];
        this.foundKeys = 0;
        this.treasureOpen = false;
        if (source == MapSource.FILE) {
            loadMapFromFile(filePath);
        } else {
            generateMap();
            saveMapToFile("src/main/java/games/Speedrun/resources/rdm_gen_world.txt");
        }
        // Entferne Platzieren von Haus und Keys beim Neuladen
        // placePlayerAndHouse();
        // placeKeys();
        visit(playerX, playerY);
    }

    /**
     * Returns the tile index at the given coordinates, or -1 if out of bounds.
     */
    public int getTile(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) return -1;
        return map[y][x];
    }

    // Getter für Inventar-Anzeige
    public Image getKeyIcon() { return key; }
    public Image getAxeIcon() { return axe; }
    public Image getPickaxeIcon() { return pickaxe; }
    public boolean hasAxe() { return axePosition == null; }
    public boolean hasPickaxe() { return pickaxePosition == null; }

    // --- Baumfäll-Logik ---
    private int treesFelled = 0;
    private final int treesForKey = 2;
    // --- Mining-Logik ---
    private int blocksMined = 0;
    private final int blocksForKey = 2;

    public boolean fellTree(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) return false;
        if (map[y][x] == tree16) {
            map[y][x] = 1; // 001.png
            treesFelled++;
            saveActiveMap();
            // Key-Belohnung
            if (treesFelled >= treesForKey) {
                for (int i = 0; i < keyCollected.length; i++) {
                    if (!keyCollected[i]) {
                        keyCollected[i] = true;
                        foundKeys++;
                        break;
                    }
                }
                treesFelled = 0;
            }
            // Türen öffnen, falls alle Schlüssel
            if (foundKeys == totalKeys) openAllDoors();
            return true;
        }
        return false;
    }

    // --- Mining-Logik: Erde (017) mit Spitzhacke abbauen, nach 2 Blöcken Key geben ---
    public boolean mineBlock(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) return false;
        if (map[y][x] == earth17) {
            map[y][x] = road03; // 003.png
            blocksMined++;
            saveActiveMap();
            // Key-Belohnung
            if (blocksMined >= blocksForKey) {
                for (int i = 0; i < keyCollected.length; i++) {
                    if (!keyCollected[i]) {
                        keyCollected[i] = true;
                        foundKeys++;
                        break;
                    }
                }
                blocksMined = 0;
            }
            // Türen öffnen, falls alle Schlüssel
            if (foundKeys == totalKeys) openAllDoors();
            return true;
        }
        return false;
    }
    public int getBlocksMined() { return blocksMined; }
}
