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
    private final int totalKeys = 4;

    private Point[] keyPositions;
    private boolean[] keyCollected;

    private Point treasureDoor;
    private boolean treasureOpen = false;
    private Point houseTopLeft;
    private int houseW = 7, houseH = 7;

    // Sprites: all 38 tile textures (000.png to 037.png)
    private Image[] tileImages = new Image[38];
    private Image key, door, chest;

    // Named indices for main tiles
    private final int grass00 = 0;   // 000.png
    private final int road03 = 3;    // 003.png
    private final int earth17 = 17;  // 017.png
    private final int water00 = 18;  // 018.png
    private final int water01 = 19;  // 019.png
    private final int tree16 = 16;   // 016.png
    private final int stone32 = 32;  // 032.png
    private final int hut33 = 33;    // 033.png

    public enum MapSource {
        FILE, RANDOM
    }

    private MapSource mapSource;
    private String mapFilePath;

    public GameMap(int rows, int cols, int tileSize) {
        this(rows, cols, tileSize, MapSource.FILE, "games/Speedrun/resources/world.txt");
    }

    public GameMap(int rows, int cols, int tileSize, MapSource source, String filePath) {
        this.tileSize = tileSize;
        this.mapSource = source;
        this.mapFilePath = filePath;
        // Default size for random maps
        this.rows = rows;
        this.cols = cols;
        loadSprites();
        if (source == MapSource.FILE) {
            loadMapFromFile(filePath);
        } else {
            this.map = new int[rows][cols];
            this.visited = new boolean[rows][cols];
            generateMap();
            saveMapToFile("src/main/java/games/Speedrun/resources/rdm_gen_world.txt");
        }
        this.keyPositions = new Point[totalKeys];
        this.keyCollected = new boolean[totalKeys];
        placePlayerAndHouse();
        placeKeys();
        visit(playerX, playerY);
    }

    private void loadMapFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filePath)))) {
            java.util.List<String[]> lines = new java.util.ArrayList<>();
            String line;
            int maxCols = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                lines.add(tokens);
                if (tokens.length > maxCols) maxCols = tokens.length;
            }
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
        // Always spawn player in the middle of the map, regardless of walkability
        playerX = cols / 2;
        playerY = rows / 2;

        // Place house only if it fits
        int hx = playerX + 3;
        int hy = playerY - houseH / 2;
        boolean fits = hx >= 0 && hy >= 0 && hx + houseW <= cols && hy + houseH <= rows;
        if (fits) {
            houseTopLeft = new Point(hx, hy);
            for (int yy = hy; yy < hy + houseH; yy++)
                for (int xx = hx; xx < hx + houseW; xx++)
                    if (yy == hy || yy == hy + houseH - 1 || xx == hx || xx == hx + houseW - 1)
                        map[yy][xx] = stone32;
                    else
                        map[yy][xx] = grass00;
            int doorX = hx + houseW / 2;
            int doorY = hy + houseH - 1;
            map[doorY][doorX] = -1; // special value for door
            treasureDoor = new Point(doorX, doorY);
        } else {
            houseTopLeft = null;
            treasureDoor = null;
        }
    }

    private void placeKeys() {
        Random rand = new Random();
        for (int i = 0; i < totalKeys; i++) {
            int tries = 0;
            while (tries++ < 1000) {
                int x = rand.nextInt(cols);
                int y = rand.nextInt(rows);
                boolean inHouse = houseTopLeft != null &&
                        x >= houseTopLeft.x && x < houseTopLeft.x + houseW &&
                        y >= houseTopLeft.y && y < houseTopLeft.y + houseH;
                if (map[y][x] == grass00 && !isOccupiedByKey(x, y) && !inHouse && (x != playerX || y != playerY)) {
                    keyPositions[i] = new Point(x, y);
                    keyCollected[i] = false;
                    break;
                }
            }
        }
    }

    private boolean isOccupiedByKey(int x, int y) {
        for (Point p : keyPositions) {
            if (p != null && p.x == x && p.y == y) return true;
        }
        return false;
    }

    public void draw(Graphics g, int vx, int vy, int viewCols, int viewRows, int tileSize, Player player) {
        for (int y = vy; y < vy + viewRows && y < rows; y++) {
            for (int x = vx; x < vx + viewCols && x < cols; x++) {
                int px = (x - vx) * tileSize;
                int py = (y - vy) * tileSize;
                int tileIdx = map[y][x];
                if (tileIdx == -1) {
                    g.drawImage(tileImages[grass00], px, py, tileSize, tileSize, null);
                    if (!treasureOpen) g.drawImage(door, px, py, tileSize, tileSize, null);
                } else if (tileIdx >= 0 && tileIdx < tileImages.length && tileImages[tileIdx] != null) {
                    g.drawImage(tileImages[tileIdx], px, py, tileSize, tileSize, null);
                } else {
                    g.setColor(Color.MAGENTA);
                    g.fillRect(px, py, tileSize, tileSize);
                }
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
                if (tileIdx == -1) {
                    g.drawImage(tileImages[grass00], px, py, tileSize, tileSize, null);
                    g.drawImage(door, px, py, tileSize, tileSize, null);
                } else if (tileIdx >= 0 && tileIdx < tileImages.length && tileImages[tileIdx] != null) {
                    g.drawImage(tileImages[tileIdx], px, py, tileSize, tileSize, null);
                } else {
                    g.setColor(Color.MAGENTA);
                    g.fillRect(px, py, tileSize, tileSize);
                }
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

    private boolean isInView(int x, int y, int vx, int vy, int viewCols, int viewRows) {
        return x >= vx && x < vx + viewCols && y >= vy && y < vy + viewRows;
    }

    public void reset() {
        this.map = new int[rows][cols];
        this.visited = new boolean[rows][cols];
        this.foundKeys = 0;
        this.treasureOpen = false;
        if (mapSource == MapSource.FILE) {
            loadMapFromFile(mapFilePath);
        } else {
            generateMap();
            saveMapToFile("src/main/java/games/Speedrun/resources/rdm_gen_world.txt");
        }
        placePlayerAndHouse();
        placeKeys();
        visit(playerX, playerY);
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
        // Make both water00 and water01 not walkable
        if (tile == -1 && foundKeys < totalKeys && !treasureOpen) return false;
        if (tile == stone32 || tile == water01 || tile == tree16 || tile == water00) return false;
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
            }
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
        placePlayerAndHouse();
        placeKeys();
        visit(playerX, playerY);
    }
}
