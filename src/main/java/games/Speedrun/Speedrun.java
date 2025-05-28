package games.Speedrun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Speedrun {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Speedrun");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);

            SpeedrunPanel panel = new SpeedrunPanel();
            frame.add(panel);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}

class SpeedrunPanel extends JPanel implements KeyListener {
    // Grid settings
    private final int rows = 100;
    private final int cols = 100;
    private final int tileSize = 48;

    // Map and player
    private char[][] map = new char[rows][cols];
    private boolean[][] visited = new boolean[rows][cols];
    private int playerX, playerY;
    private int foundKeys = 0;
    private final int totalKeys = 4;
    private boolean gameWon = false;
    private long startTime, endTime;

    // Key positions
    private Point[] keyPositions = new Point[totalKeys];
    private boolean[] keyCollected = new boolean[totalKeys];

    // Treasure room
    private Point treasureDoor;
    private boolean treasureOpen = false;
    private Point houseTopLeft;
    private int houseW = 7, houseH = 7;

    // Sprites (Dummy, werden aus Ressourcen geladen)
    private Image grass, tree, stone, water, player, key, door, treasure;

    // Viewport
    private int viewCols, viewRows;

    // Map-Overlay
    private boolean showMap = false;

    public SpeedrunPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadSprites();
        generateMap();
        placePlayerAndHouse();
        placeKeys();
        startTime = System.currentTimeMillis();
    }

    private void loadSprites() {
        grass = loadImage("games/Speedrun/resources/grass.png");
        tree = loadImage("games/Speedrun/resources/tree.png");
        stone = loadImage("games/Speedrun/resources/stone.png");
        water = loadImage("games/Speedrun/resources/water.png");
        player = loadImage("games/Speedrun/resources/player.png");
        key = loadImage("games/Speedrun/resources/key.png");
        door = loadImage("games/Speedrun/resources/door.png");
        treasure = loadImage("games/Speedrun/resources/treasure.png");
    }

    private Image loadImage(String path) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) return null;
        return new ImageIcon(url).getImage();
    }

    private void generateMap() {
        // 1. Alles Grass
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++)
                map[y][x] = 'G';

        // 2. Wald (Bereich links oben)
        for (int y = 10; y < 40; y++) {
            for (int x = 5; x < 35; x++) {
                if (Math.random() < 0.7) map[y][x] = 'T';
            }
        }

        // 3. Ovaler Teich (Mitte rechts)
        int cx = 80, cy = 60, rx = 12, ry = 7;
        for (int y = cy - ry; y <= cy + ry; y++) {
            for (int x = cx - rx; x <= cx + rx; x++) {
                if (x >= 0 && x < cols && y >= 0 && y < rows) {
                    double dx = (x - cx) / (double)rx;
                    double dy = (y - cy) / (double)ry;
                    if (dx * dx + dy * dy <= 1.0) {
                        map[y][x] = 'W';
                    }
                }
            }
        }

        // 4. Einzelne Steine verstreut
        for (int i = 0; i < 200; i++) {
            int x = (int)(Math.random() * cols);
            int y = (int)(Math.random() * rows);
            if (map[y][x] == 'G') map[y][x] = 'S';
        }
    }

    private void placePlayerAndHouse() {
        // Spawn Spieler in der Nähe der Mitte
        playerX = cols / 2;
        playerY = rows / 2;

        // Haus direkt sichtbar beim Spawn (rechts vom Spawn)
        houseTopLeft = new Point(playerX + 3, playerY - houseH / 2);
        int hx = houseTopLeft.x;
        int hy = houseTopLeft.y;

        // Hauswände aus Stein
        for (int y = hy; y < hy + houseH; y++) {
            for (int x = hx; x < hx + houseW; x++) {
                if (y == hy || y == hy + houseH - 1 || x == hx || x == hx + houseW - 1) {
                    map[y][x] = 'S';
                } else {
                    map[y][x] = 'G';
                }
            }
        }
        // Tür (unten Mitte)
        int doorX = hx + houseW / 2;
        int doorY = hy + houseH - 1;
        map[doorY][doorX] = 'D'; // Spezielles Zeichen für Tür
        treasureDoor = new Point(doorX, doorY);
    }

    private void placeKeys() {
        Random rand = new Random();
        for (int i = 0; i < totalKeys; i++) {
            while (true) {
                int x = rand.nextInt(cols);
                int y = rand.nextInt(rows);
                // Nicht im Haus, nicht auf Tür, nicht auf Stein, nicht auf Wasser, nicht auf Spieler, nicht auf Baum
                boolean inHouse = houseTopLeft != null &&
                        x >= houseTopLeft.x && x < houseTopLeft.x + houseW &&
                        y >= houseTopLeft.y && y < houseTopLeft.y + houseH;
                if (map[y][x] == 'G' && !isOccupiedByKey(x, y) && !inHouse && (x != playerX || y != playerY)) {
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Viewport berechnen (zentriere Spieler)
        viewCols = Math.min(getWidth() / tileSize, cols);
        viewRows = Math.min(getHeight() / tileSize, rows);
        int vx = Math.max(0, Math.min(playerX - viewCols / 2, cols - viewCols));
        int vy = Math.max(0, Math.min(playerY - viewRows / 2, rows - viewRows));

        // Draw map (nur sichtbarer Bereich)
        for (int y = vy; y < vy + viewRows && y < rows; y++) {
            for (int x = vx; x < vx + viewCols && x < cols; x++) {
                int px = (x - vx) * tileSize;
                int py = (y - vy) * tileSize;
                switch (map[y][x]) {
                    case 'G':
                        g.drawImage(grass, px, py, tileSize, tileSize, this);
                        break;
                    case 'T':
                        g.drawImage(tree, px, py, tileSize, tileSize, this);
                        break;
                    case 'S':
                        g.drawImage(stone, px, py, tileSize, tileSize, this);
                        break;
                    case 'W':
                        g.drawImage(water, px, py, tileSize, tileSize, this);
                        break;
                    case 'D':
                        g.drawImage(grass, px, py, tileSize, tileSize, this);
                        if (!treasureOpen) g.drawImage(door, px, py, tileSize, tileSize, this);
                        break;
                }
            }
        }

        // Draw keys (nur wenn im Viewport)
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i]) {
                Point p = keyPositions[i];
                if (isInView(p.x, p.y, vx, vy)) {
                    int px = (p.x - vx) * tileSize;
                    int py = (p.y - vy) * tileSize;
                    g.drawImage(key, px, py, tileSize, tileSize, this);
                }
            }
        }

        // Draw treasure (immer im Haus, Mitte)
        int chestX = houseTopLeft.x + houseW / 2;
        int chestY = houseTopLeft.y + houseH / 2;
        if (isInView(chestX, chestY, vx, vy) && treasureOpen) {
            int px = (chestX - vx) * tileSize;
            int py = (chestY - vy) * tileSize;
            g.drawImage(treasure, px, py, tileSize, tileSize, this);
        }

        // Draw player
        int playerPx = (playerX - vx) * tileSize;
        int playerPy = (playerY - vy) * tileSize;
        g.drawImage(player, playerPx, playerPy, tileSize, tileSize, this);

        // Draw HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("Keys: " + foundKeys + "/" + totalKeys, 30, 40);

        if (gameWon) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("SPEEDRUN FINISHED!", 200, 200);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Zeit: " + ((endTime - startTime) / 1000.0) + " Sekunden", 200, 270);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Drücke ENTER für neuen Run", 200, 340);
        }

        // Map Overlay
        if (showMap) {
            drawMapOverlay(g);
        }
    }

    private void drawMapOverlay(Graphics g) {
        int mapW = Math.min(800, cols * 4);
        int mapH = Math.min(800, rows * 4);
        int ox = (getWidth() - mapW) / 2;
        int oy = (getHeight() - mapH) / 2;

        // Hintergrund
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(ox - 10, oy - 10, mapW + 20, mapH + 20);

        // Tiles
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int px = ox + x * mapW / cols;
                int py = oy + y * mapH / rows;
                int s = mapW / cols;
                if (!visited[y][x]) {
                    g.setColor(new Color(50, 50, 50, 120));
                    g.fillRect(px, py, s, s);
                } else {
                    switch (map[y][x]) {
                        case 'G': g.setColor(new Color(60, 180, 60)); break;
                        case 'T': g.setColor(new Color(40, 100, 40)); break;
                        case 'S': g.setColor(new Color(120, 120, 120)); break;
                        case 'W': g.setColor(new Color(60, 120, 200)); break;
                        case 'D': g.setColor(new Color(200, 180, 60)); break;
                        default: g.setColor(Color.GRAY);
                    }
                    g.fillRect(px, py, s, s);
                }
            }
        }
        // Keys
        g.setColor(Color.YELLOW);
        for (int i = 0; i < totalKeys; i++) {
            if (!keyCollected[i]) {
                int px = ox + keyPositions[i].x * mapW / cols;
                int py = oy + keyPositions[i].y * mapH / rows;
                int s = mapW / cols;
                g.fillOval(px + s/4, py + s/4, s/2, s/2);
            }
        }
        // Player
        g.setColor(Color.RED);
        int px = ox + playerX * mapW / cols;
        int py = oy + playerY * mapH / rows;
        int s = mapW / cols;
        g.fillOval(px + s/4, py + s/4, s/2, s/2);
        // Hausumriss
        g.setColor(Color.WHITE);
        int hx = ox + houseTopLeft.x * mapW / cols;
        int hy = oy + houseTopLeft.y * mapH / rows;
        int hw = houseW * mapW / cols;
        int hh = houseH * mapH / rows;
        g.drawRect(hx, hy, hw, hh);
    }

    private boolean isInView(int x, int y, int vx, int vy) {
        return x >= vx && x < vx + viewCols && y >= vy && y < vy + viewRows;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameWon) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                foundKeys = 0;
                treasureOpen = false;
                gameWon = false;
                generateMap();
                placePlayerAndHouse();
                placeKeys();
                startTime = System.currentTimeMillis();
                repaint();
            }
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_M) {
            showMap = !showMap;
            repaint();
            return;
        }
        int dx = 0, dy = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    dy = -1; break;
            case KeyEvent.VK_DOWN:  dy = 1; break;
            case KeyEvent.VK_LEFT:  dx = -1; break;
            case KeyEvent.VK_RIGHT: dx = 1; break;
        }
        int nx = playerX + dx;
        int ny = playerY + dy;
        if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
            char tile = map[ny][nx];
            // Kollision mit Tür, wenn nicht alle Keys
            if (tile == 'D' && foundKeys < totalKeys && !treasureOpen) {
                return;
            }
            // Kollision mit Stein, Wasser oder Baum
            if (tile == 'S' || tile == 'W' || tile == 'T') {
                return;
            }
            // Tür öffnen, wenn alle Keys
            if (tile == 'D' && foundKeys == totalKeys && !treasureOpen) {
                treasureOpen = true;
                repaint();
                return;
            }
            // Bewegung
            playerX = nx;
            playerY = ny;
            visited[playerY][playerX] = true;
            // Key einsammeln
            for (int i = 0; i < totalKeys; i++) {
                if (!keyCollected[i] && keyPositions[i].x == playerX && keyPositions[i].y == playerY) {
                    keyCollected[i] = true;
                    foundKeys++;
                }
            }
            // Schatz einsammeln und gewinnen
            int chestX = houseTopLeft.x + houseW / 2;
            int chestY = houseTopLeft.y + houseH / 2;
            if (playerX == chestX && playerY == chestY && treasureOpen && !gameWon) {
                endTime = System.currentTimeMillis();
                gameWon = true;
            }
        }
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
