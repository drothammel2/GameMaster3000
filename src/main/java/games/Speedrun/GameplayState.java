package games.Speedrun;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

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
    private boolean paused = false;
    private boolean wasdMode = true; // default to WASD

    // Smooth camera
    private float cameraX, cameraY;
    private static final float CAMERA_LERP = 0.15f; // 0..1, higher = faster camera

    private long lastUpdateTime = System.nanoTime();

    public GameplayState(SpeedrunPanel panel) {
        this.panel = panel;
        this.input = new InputManager();
        // Use 100x100 for random, but .txt will override for file maps
        gameMap = new GameMap(100, 100, TILE_SIZE);

        player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
        entities.add(player);

        // Camera starts centered on player
        cameraX = player.x;
        cameraY = player.y;

        startTime = System.currentTimeMillis();
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

        if (showMap) {
            gameMap.drawMapOverlay(g, width, height, player);
        }

        if (paused) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("PAUSED", width / 2 - 150, height / 2 - 20);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            // Updated menu with all options
            int menuY = height / 2 + 40;
            int menuX = width / 2 - 260;
            g.drawString("ESC: Resume   Q: Quit   C: Change controls", menuX, menuY);
            g.drawString("R: Reload random map   W: Worldbuilder map", menuX, menuY + 40);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (paused) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                paused = false;
            } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                System.exit(0);
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
                // Reload with worldbuilder.txt
                gameMap.reload(GameMap.MapSource.FILE, "games/Speedrun/resources/worldbuilder.txt");
                entities.clear();
                player = new Player(gameMap, input, gameMap.getCols() / 2f, gameMap.getRows() / 2f);
                entities.add(player);
                cameraX = player.x;
                cameraY = player.y;
                gameWon = false;
                startTime = System.currentTimeMillis();
                paused = false;
            }
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            paused = true;
            return;
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
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_M) {
            showMap = !showMap;
            return;
        }
        input.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!paused) {
            input.keyReleased(e.getKeyCode());
        }
    }
}