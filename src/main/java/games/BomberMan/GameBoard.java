package games.BomberMan;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.IOException;

public class GameBoard extends JPanel {
    // Neue Kartengröße für fast Vollbild
    private static final int TILE_SIZE = 48;
    private static final int ROWS = 15; // 1 weitere Zeile entfernt für Symmetrie
    private static final int COLS = 31; // 1 weniger horizontal
    private static final int BOMB_DELAY_MS = 2000;
    private static final int EXPLOSION_RANGE = 2;
    private static final int HUD_HEIGHT = 60;

    private int playerX = 1, playerY = 1;
    private char[][] board = new char[ROWS][COLS];
    private List<int[]> activeBombs = new ArrayList<>();
    private boolean bombCooldown = false;
    private Player player;
    private BufferedImage wallImage;
    private BufferedImage breakableBlockImage;
    private BufferedImage breakableBlockImageAnim1;
    private BufferedImage breakableBlockImageAnim2;
    private BufferedImage breakableBlockImageAnim3;
    private BufferedImage breakableBlockImageAnim4;
    private int[][] breakableBlockAnimState = new int[ROWS][COLS]; // 0=normal, 1-4=Animation, -1=weg
    private BufferedImage playerFrontIdle, playerFrontLeft, playerFrontRight;
    private BufferedImage playerBackIdle, playerBackLeft, playerBackRight;
    private String playerDirection = "front"; // "front" or "back"
    private String playerMovement = "idle"; // "idle", "left", "right"
    private BufferedImage bombState1, bombState2, bombState3;
    private int[][] bombAnimState = new int[ROWS][COLS]; // 0=none, 1-3=animation states
    private BufferedImage explosionState1, explosionState2, explosionState3, explosionState4;
    private int[][] explosionAnimState = new int[ROWS][COLS]; // 0=none, 1-4=animation states
    private boolean isGameOver = false;
    private boolean isStartMenu = true;
    private boolean isSettingsMenu = false; // Track if settings menu is active
    private boolean isPauseMenu = false; // Track if pause menu is active
    private int selectedOption = 0; // 0 = Start Game, 1 = Settings, 2 = Quit
    private int selectedSetting = 0; // Track selected setting option
    private int selectedGameOverOption = 0; // Track selected option in Game Over screen
    private final String[] options = {"Start Game", "Settings", "Quit"};
    private BufferedImage[] deathAnimationFrames;
    private int deathAnimationState = 0;
    private boolean isAnimatingDeath = false;
    private BufferedImage startScreenBackground;
    private BufferedImage settingsScreenBackground;
    private boolean useWASDControls = false; // Default to arrow keys
    private boolean isGameWon = false; // Track if the game is won
    private int goalX, goalY; // Coordinates of the goal
    private BufferedImage goalImage1;
    private ImageIcon winningGif; // Add a private field for the winning GIF
    private Image winImage; // Add a private field for the winning image
    private boolean movementCooldown = false;
    private List<Enemy> enemies = new ArrayList<>();
    private BufferedImage enemyImage;
    private java.util.Timer enemyMoveTimer;
    private BufferedImage[] enemyDeathFrames;
    private List<int[]> dyingEnemies = new ArrayList<>(); // Track enemies in death animation
    private int[][] enemyDeathState = new int[ROWS][COLS]; // 0=none, 1-3=animation states
    private String difficulty = "Normal"; // Default difficulty
    private int enemyCount = 6; // Default enemy count
    private int enemySpeed = 2; // Default enemy speed
    private Runnable quitAction;

    public GameBoard() {
        player = new Player(playerX, playerY);
        initialize();
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
                repaint();
            }
        });
        try {
            BufferedImage spriteSheet = ImageIO.read(new File("src/main/java/games/BomberMan/resources/spritesheet.png"));
            // Unzerstörbarer Block: x=13, y=254, w=35, h=35 (von 13 bis 48, 254 bis 289)
            wallImage = spriteSheet.getSubimage(13, 254, 35, 35);
            // Zerstörbarer Block (orange): x=57, y=254, w=35, h=35
            breakableBlockImage = spriteSheet.getSubimage(57, 254, 35, 35);
            breakableBlockImageAnim1 = spriteSheet.getSubimage(98, 254, 35, 35);
            breakableBlockImageAnim2 = spriteSheet.getSubimage(139, 254, 35, 35);
            breakableBlockImageAnim3 = spriteSheet.getSubimage(180, 254, 35, 35);
            breakableBlockImageAnim4 = spriteSheet.getSubimage(221, 254, 35, 35);
            playerFrontIdle = spriteSheet.getSubimage(10, 10, 26, 35);
            playerFrontLeft = spriteSheet.getSubimage(40, 10, 29, 35);
            playerFrontRight = spriteSheet.getSubimage(76, 10, 29, 35);
            playerBackIdle = spriteSheet.getSubimage(10, 51, 26, 35);
            playerBackLeft = spriteSheet.getSubimage(77, 51, 29, 35);
            playerBackRight = spriteSheet.getSubimage(42, 51, 29, 35);
            bombState1 = spriteSheet.getSubimage(10, 130, 29, 35);
            bombState2 = spriteSheet.getSubimage(42, 130, 29, 35);
            bombState3 = spriteSheet.getSubimage(77, 130, 35, 35);
            explosionState1 = spriteSheet.getSubimage(10, 171, 35, 35);
            explosionState2 = spriteSheet.getSubimage(50, 171, 35, 35);
            explosionState3 = spriteSheet.getSubimage(90, 171, 35, 35);
            explosionState4 = spriteSheet.getSubimage(130, 171, 35, 35);
            deathAnimationFrames = new BufferedImage[6];
            deathAnimationFrames[0] = spriteSheet.getSubimage(114, 91, 32, 35);
            deathAnimationFrames[1] = spriteSheet.getSubimage(151, 91, 32, 35);
            deathAnimationFrames[2] = spriteSheet.getSubimage(188, 91, 32, 35);
            deathAnimationFrames[3] = spriteSheet.getSubimage(225, 91, 32, 35);
            deathAnimationFrames[4] = spriteSheet.getSubimage(264, 91, 32, 35);
            deathAnimationFrames[5] = spriteSheet.getSubimage(303, 91, 32, 35);
            goalImage1 = spriteSheet.getSubimage(260, 209, 35, 35);
            enemyImage = spriteSheet.getSubimage(13, 474, 35, 34); // Use a suitable sprite
            enemyDeathFrames = new BufferedImage[3];
            enemyDeathFrames[0] = spriteSheet.getSubimage(178, 168, 29, 35);
            enemyDeathFrames[1] = spriteSheet.getSubimage(216, 171, 23, 26);
            enemyDeathFrames[2] = spriteSheet.getSubimage(272, 177, 17, 14);
        } catch (Exception e) {
            wallImage = null;
            breakableBlockImage = null;
            breakableBlockImageAnim1 = null;
            breakableBlockImageAnim2 = null;
            breakableBlockImageAnim3 = null;
            breakableBlockImageAnim4 = null;
            e.printStackTrace();
        }
        try {
            startScreenBackground = ImageIO.read(new File("src/main/java/games/BomberMan/resources/wallpaper.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            settingsScreenBackground = ImageIO.read(new File("src/main/java/games/BomberMan/resources/wallpaper2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            winningGif = new ImageIcon("resources/win.gif"); // Load the GIF from resources
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            winImage = ImageIO.read(new File("resources/wallpaper.png")); // Load wallpaper.png from resources
        } catch (IOException e) {
            e.printStackTrace();
        }
        spawnEnemies(4); // Spawn 4 enemies
        startEnemyMovement();
    }

    private void initialize() {
        Random random = new Random();

        // Clear the board
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = ' ';
            }
        }

        // Set outer walls
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                    board[row][col] = 'W';
                }
            }
        }

        // Set inner indestructible walls in a checkerboard pattern
        for (int row = 2; row < ROWS - 1; row += 2) {
            for (int col = 2; col < COLS - 1; col += 2) {
                board[row][col] = 'W';
            }
        }

        // Place destructible blocks randomly, ensuring symmetry
        for (int row = 1; row < ROWS - 1; row++) {
            for (int col = 1; col < COLS - 1; col++) {
                if (board[row][col] == ' ' && random.nextDouble() < 0.5) {
                    board[row][col] = 'B';
                }
            }
        }

        // Place the goal under a random destructible block
        do {
            goalX = random.nextInt(COLS - 2) + 1;
            goalY = random.nextInt(ROWS - 2) + 1;
        } while (board[goalY][goalX] != 'B');

        // Mark the goal position internally
        board[goalY][goalX] = 'G'; // 'G' represents the goal

        // Clear player spawn area (top-left corner)
        board[1][1] = ' ';
        board[1][2] = ' ';
        board[2][1] = ' ';

        // Clear symmetrical spawn area (bottom-right corner)
        board[ROWS - 2][COLS - 2] = ' ';
        board[ROWS - 2][COLS - 3] = ' ';
        board[ROWS - 3][COLS - 2] = ' ';

        // Place the player
        board[playerY][playerX] = 'P';

        // Platziere das Ziel so weit wie möglich vom Spieler-Spawn-Bereich entfernt
        // Ziel wird in der unteren rechten Ecke der Karte gesucht
        int maxDistance = 0;
        for (int row = ROWS - 2; row > 0; row--) {
            for (int col = COLS - 2; col > 0; col--) {
                if (board[row][col] == 'B') { // Nur unter zerstörbaren Blöcken
                    int distance = Math.abs(row - playerY) + Math.abs(col - playerX);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        goalX = col;
                        goalY = row;
                    }
                }
            }
        }

        // Markiere die Zielposition intern
        board[goalY][goalX] = 'G'; // 'G' repräsentiert das Ziel
    }

    private void applyDifficultySettings() {
        switch (difficulty) {
            case "Easy":
                enemyCount = 3;
                enemySpeed = 1;
                break;
            case "Normal":
                enemyCount = 6;
                enemySpeed = 3;
                break;
            case "Hard":
                enemyCount = 9;
                enemySpeed = 7;
                break;
            case "Extreme":
                enemyCount = 12;
                enemySpeed = 10;
                break;
            default:
                throw new IllegalStateException("Unexpected difficulty: " + difficulty);
        }

        // Reinitialize enemies based on the new settings
        initializeEnemies();
    }

    private void initializeEnemies() {
        // Clear existing enemies
        enemies.clear();

        // Spawn new enemies based on the current difficulty settings
        spawnEnemies(enemyCount);
    }

    private boolean isWall(int row, int col) {
        return row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1 || (row % 2 == 0 && col % 2 == 0);
    }

    public void handleKeyPress(int keyCode) {
        if (isPauseMenu) {
            switch (keyCode) {
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_P:
                    closePauseMenu(); // Resume the game
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    selectedOption = (selectedOption + 2) % 3; // Navigate up (wrap around)
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    selectedOption = (selectedOption + 1) % 3; // Navigate down (wrap around)
                    repaint();
                    break;
                case KeyEvent.VK_ENTER:
                    if (selectedOption == 0) { // Continue
                        closePauseMenu();
                    } else if (selectedOption == 1) { // Back to Home
                        isPauseMenu = false;
                        isStartMenu = true;
                        repaint();
                    } else if (selectedOption == 2) { // Settings
                        isPauseMenu = false;
                        openSettings();
                    }
                    break;
            }
            return;
        }

        if (isSettingsMenu) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                closeSettings(); // Return to the previous screen from settings
            } else {
                handleSettingsNavigation(keyCode); // Navigate within settings
            }
            return;
        }

        if (isStartMenu) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                System.exit(0); // Quit the game
            } else {
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        selectedOption = (selectedOption + options.length - 1) % options.length; // Navigate up
                        repaint();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        selectedOption = (selectedOption + 1) % options.length; // Navigate down
                        repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (selectedOption == 1) { // Settings option
                            openSettings();
                        } else if (selectedOption == 2) { // Quit option
                            if (quitAction != null) {
                                quitAction.run(); // Return to the game selection menu
                            }
                            return;
                        } else {
                            executeSelectedOption();
                        }
                        break;
                }
            }
            return;
        }

        if (isGameOver) {
            handleGameOverNavigation(keyCode);
            return;
        }

        if (isGameWon) {
            if (keyCode == KeyEvent.VK_ENTER) {
                resetGame(); // Restart the game
            }
            return;
        }

        if (!isStartMenu && !isPauseMenu && (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P)) {
            openPauseMenu(); // Open pause menu only during gameplay
            return;
        }

        if (isSettingsMenu && keyCode == KeyEvent.VK_ESCAPE) {
            closeSettings(); // Return to the previous screen from settings
            return;
        }

        if (movementCooldown) {
            return; // Prevent movement if cooldown is active
        }

        int dx = 0, dy = 0;
        if (useWASDControls) {
            switch (keyCode) {
                case KeyEvent.VK_W: dy = -1; playerDirection = "back"; playerMovement = "idle"; break;
                case KeyEvent.VK_S: dy = 1; playerDirection = "front"; playerMovement = "idle"; break;
                case KeyEvent.VK_A: dx = -1; playerMovement = "left"; break;
                case KeyEvent.VK_D: dx = 1; playerMovement = "right"; break;
                case KeyEvent.VK_SPACE: placeBomb(); return;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.VK_UP: dy = -1; playerDirection = "back"; playerMovement = "idle"; break;
                case KeyEvent.VK_DOWN: dy = 1; playerDirection = "front"; playerMovement = "idle"; break;
                case KeyEvent.VK_LEFT: dx = -1; playerMovement = "left"; break;
                case KeyEvent.VK_RIGHT: dx = 1; playerMovement = "right"; break;
                case KeyEvent.VK_SPACE: placeBomb(); return;
            }
        }

        int newX = playerX + dx;
        int newY = playerY + dy;
        if (isValidMove(newX, newY)) {
            playerX = newX;
            playerY = newY;
        }

        movementCooldown = true;
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                movementCooldown = false; // Reset cooldown after delay
            }
        }, 200); // Adjust delay as needed

        // Check if the player reaches the goal
        if (playerX == goalX && playerY == goalY) {
            isGameWon = true;
            repaint();
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < COLS && y >= 0 && y < ROWS && board[y][x] != 'W' && board[y][x] != 'B';
    }

    private void placeBomb() {
        if (!bombCooldown && board[playerY][playerX] != 'O') {
            int bombX = playerX;
            int bombY = playerY;
            activeBombs.add(new int[]{bombX, bombY});
            bombCooldown = true;
            board[bombY][bombX] = 'O';
            bombAnimState[bombY][bombX] = 1;
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    cycleBombAnimation(bombY, bombX);
                }
            }, 0, 500); // Cycle every 500ms
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    explodeBomb(bombX, bombY);
                    activeBombs.removeIf(b -> b[0] == bombX && b[1] == bombY);
                    bombCooldown = false;
                    repaint();
                }
            }, BOMB_DELAY_MS);
        }
    }

    private void cycleBombAnimation(int row, int col) {
        if (bombAnimState[row][col] > 0 && bombAnimState[row][col] < 3) {
            bombAnimState[row][col]++;
        } else {
            bombAnimState[row][col] = 1;
        }
        repaint();
    }

    private void explodeBomb(int x, int y) {
        board[y][x] = ' ';
        explosionAnimState[y][x] = 1;
        for (int i = 1; i <= EXPLOSION_RANGE; i++) {
            // Check right
            if (x + i < COLS) {
                if (board[y][x + i] == 'W' || board[y][x + i] == 'G') break; // Treat goal as indestructible
                explosionAnimState[y][x + i] = 1;
                board[y][x + i] = ' '; // Clear the field
            }
            // Check left
            if (x - i >= 0) {
                if (board[y][x - i] == 'W' || board[y][x - i] == 'G') break; // Treat goal as indestructible
                explosionAnimState[y][x - i] = 1;
                board[y][x - i] = ' '; // Clear the field
            }
        }
        for (int i = 1; i <= EXPLOSION_RANGE; i++) {
            // Check down
            if (y + i < ROWS) {
                if (board[y + i][x] == 'W' || board[y + i][x] == 'G') break; // Treat goal as indestructible
                explosionAnimState[y + i][x] = 1;
                board[y + i][x] = ' '; // Clear the field
            }
            // Check up
            if (y - i >= 0) {
                if (board[y - i][x] == 'W' || board[y - i][x] == 'G') break; // Treat goal as indestructible
                explosionAnimState[y - i][x] = 1;
                board[y - i][x] = ' '; // Clear the field
            }
        }
        java.util.Timer explosionTimer = new java.util.Timer();
        explosionTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean hasActiveExplosions = false;
                for (int row = 0; row < ROWS; row++) {
                    for (int col = 0; col < COLS; col++) {
                        if (explosionAnimState[row][col] > 0 && explosionAnimState[row][col] < 4) {
                            explosionAnimState[row][col]++;
                            hasActiveExplosions = true;
                        } else if (explosionAnimState[row][col] == 4) {
                            explosionAnimState[row][col] = 0; // End explosion
                        }
                    }
                }
                checkGameOver();
                repaint();
                if (!hasActiveExplosions) {
                    explosionTimer.cancel(); // Stop the timer if no active explosions
                }
            }
        }, 0, 120); // Cycle every 120ms
    }

    private void startBreakableBlockAnimation(int row, int col) {
        breakableBlockAnimState[row][col] = 1;
        final int[] phase = {1};
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (phase[0] <= 3) { // Ensure all 3 states are executed
                    breakableBlockAnimState[row][col] = phase[0]++;
                    repaint();
                } else {
                    board[row][col] = '0'; // Block disappears
                    breakableBlockAnimState[row][col] = -1;
                    repaint();
                    cancel(); // Stop the timer
                }
            }
        }, 0, 120); // 120ms per frame
    }

    private void movePlayer(int keyCode) {
        int newX = playerX, newY = playerY;
        switch (keyCode) {
            case KeyEvent.VK_UP: newY--; break;
            case KeyEvent.VK_DOWN: newY++; break;
            case KeyEvent.VK_LEFT: newX--; break;
            case KeyEvent.VK_RIGHT: newX++; break;
        }
        if (isMovable(newX, newY)) {
            board[playerY][playerX] = ' ';
            playerX = newX;
            playerY = newY;
            board[playerY][playerX] = 'P';
        }
    }

    private boolean isMovable(int x, int y) {
        return board[y][x] == ' ';
    }

    private boolean isValidMoveOld(int x, int y) {
        return x >= 0 && x < COLS && y >= 0 && y < ROWS && board[y][x] != 'W';
    }

    private void openSettings() {
        isSettingsMenu = true;
        repaint();
    }

    private void closeSettings() {
        isSettingsMenu = false;
        if (isPauseMenu) {
            isPauseMenu = true; // Return to pause menu if settings were opened from there
        } else {
            startEnemyMovement(); // Resume enemy movement if not paused
        }
        repaint();
    }

    private void openPauseMenu() {
        isPauseMenu = true;
        if (enemyMoveTimer != null) {
            enemyMoveTimer.cancel(); // Stop enemy movement when paused
        }
        repaint();
    }

    private void closePauseMenu() {
        isPauseMenu = false;
        startEnemyMovement(); // Always restart enemy movement when unpaused
        repaint();
    }

    private void handleSettingsNavigation(int keyCode) {
        String[] settingsOptions = {
            "Difficulty: " + difficulty, // Display current difficulty
            useWASDControls ? "Controls (W-A-S-D)" : "Controls (\u2191 \u2190 \u2193 \u2192)", // Arrow symbols
            "Back"
        };

        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                selectedSetting = (selectedSetting + settingsOptions.length - 1) % settingsOptions.length; // Navigate up (wrap around)
                repaint();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                selectedSetting = (selectedSetting + 1) % settingsOptions.length; // Navigate down (wrap around)
                repaint();
                break;
            case KeyEvent.VK_ENTER:
                if (selectedSetting == 2) { // "Back" option
                    closeSettings();
                } else {
                    toggleSetting(selectedSetting);
                }
                break;
        }
    }

    private void toggleSetting(int settingIndex) {
        if (settingIndex == 0) { // Difficulty option
            switch (difficulty) {
                case "Easy":
                    difficulty = "Normal";
                    break;
                case "Normal":
                    difficulty = "Hard";
                    break;
                case "Hard":
                    difficulty = "Extreme";
                    break;
                case "Extreme":
                    difficulty = "Easy";
                    break;
            }
            System.out.println("Difficulty set to: " + difficulty);
            applyDifficultySettings(); // Apply changes based on difficulty
            resetGame(); // Restart the game when difficulty changes
        } else if (settingIndex == 1) { // Controls option
            useWASDControls = !useWASDControls; // Toggle control scheme
            System.out.println("Controls set to: " + (useWASDControls ? "W-A-S-D" : "Arrow Keys"));
            updateControlScheme(); // Apply the control scheme change
        }
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        String[] settingsOptions = {"Difficulty: Normal", "Back"};
        int optionHeight = getFontMetrics(getFont()).getHeight();
        int spacing = 20;
        int totalHeight = settingsOptions.length * optionHeight + (settingsOptions.length - 1) * spacing;
        int startY = (getHeight() - totalHeight) / 2;

        for (int i = 0; i < settingsOptions.length; i++) {
            int optionY = startY + i * (optionHeight + spacing);
            if (mouseY >= optionY - optionHeight && mouseY <= optionY) {
                selectedSetting = i;
                if (i == 1) { // "Back" option
                    closeSettings();
                } else {
                    toggleSetting(selectedSetting);
                }
                repaint();
                break;
            }
        }
    }

    private void resetGame() {
        playerX = 1;
        playerY = 1;
        initialize(); // Reinitialize the game board, including the goal
        isGameOver = false;
        isGameWon = false; // Reset game won status
        applyDifficultySettings(); // Apply the current difficulty settings
        repaint();
    }

    private void handleGameOverNavigation(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                selectedGameOverOption = (selectedGameOverOption + 1) % 2; // Navigate up (wrap around)
                repaint();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                selectedGameOverOption = (selectedGameOverOption + 1) % 2; // Navigate down (wrap around)
                repaint();
                break;
            case KeyEvent.VK_ENTER:
                if (selectedGameOverOption == 0) { // Back to Home
                    isGameOver = false;
                    isStartMenu = true;
                    repaint();
                } else if (selectedGameOverOption == 1) { // Retry
                    resetGame(); // Use the new resetGame method
                }
                break;
        }
    }

    private void startDeathAnimation() {
        isAnimatingDeath = true;
        deathAnimationState = 0;
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if (deathAnimationState < deathAnimationFrames.length - 1) {
                    deathAnimationState++;
                    repaint();
                } else {
                    cancel(); // Stop the timer when the animation is complete
                }
            }
        }, 0, 200); // 200ms per frame
    }

    private void spawnEnemies(int count) {
        Random random = new Random();
        enemies.clear();
        int tries = 0;
        while (enemies.size() < count && tries < 100) {
            int x = random.nextInt(COLS - 2) + 1;
            int y = random.nextInt(ROWS - 2) + 1;
            // Avoid player spawn and goal
            if ((Math.abs(x - playerX) + Math.abs(y - playerY) > 4) && board[y][x] == ' ' && (x != goalX || y != goalY)) {
                enemies.add(new Enemy(x, y));
            }
            tries++;
        }
    }

    private void startEnemyMovement() {
        if (enemyMoveTimer != null) enemyMoveTimer.cancel();
        enemyMoveTimer = new java.util.Timer();
        enemyMoveTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                moveEnemies();
            }
        }, 0, 400); // Move every 400ms
    }

    private void moveEnemies() {
        Random random = new Random();
        for (Enemy enemy : enemies) {
            int[] dirs = {-1, 0, 1, 0, 0, -1, 0, 1}; // (dx,dy) pairs: up, down, left, right
            List<int[]> possibleMoves = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int nx = enemy.x + dirs[i * 2];
                int ny = enemy.y + dirs[i * 2 + 1];
                if (isValidMove(nx, ny) && !isEnemyAt(nx, ny)) {
                    possibleMoves.add(new int[]{nx, ny});
                }
            }
            if (!possibleMoves.isEmpty()) {
                int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
                enemy.x = move[0];
                enemy.y = move[1];
            }
        }
        checkEnemyPlayerCollision();
        removeDeadEnemies();
        repaint();
    }

    private boolean isEnemyAt(int x, int y) {
        for (Enemy e : enemies) {
            if (e.x == x && e.y == y) return true;
        }
        return false;
    }

    private void checkEnemyPlayerCollision() {
        for (Enemy e : enemies) {
            if (e.x == playerX && e.y == playerY && !isGameOver && !isGameWon) {
                isGameOver = true;
                startDeathAnimation();
                repaint();
                break;
            }
        }
    }

    private void removeDeadEnemies() {
        for (Enemy e : enemies) {
            if (explosionAnimState[e.y][e.x] > 0) {
                dyingEnemies.add(new int[]{e.x, e.y});
                enemyDeathState[e.y][e.x] = 1; // Start death animation
            }
        }
        enemies.removeIf(e -> explosionAnimState[e.y][e.x] > 0);
        startEnemyDeathAnimation();
    }

    private void startEnemyDeathAnimation() {
        java.util.Timer deathTimer = new java.util.Timer();
        deathTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean hasActiveDeaths = false;
                for (int[] pos : dyingEnemies) {
                    int x = pos[0];
                    int y = pos[1];
                    if (enemyDeathState[y][x] > 0 && enemyDeathState[y][x] < 3) {
                        enemyDeathState[y][x]++;
                        hasActiveDeaths = true;
                    } else if (enemyDeathState[y][x] == 3) {
                        enemyDeathState[y][x] = 0; // End death animation
                    }
                }
                dyingEnemies.removeIf(pos -> enemyDeathState[pos[1]][pos[0]] == 0);
                repaint();
                if (!hasActiveDeaths) {
                    deathTimer.cancel(); // Stop the timer if no active deaths
                }
            }
        }, 0, 200); // Cycle every 200ms
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isPauseMenu) {
            // Draw the background image for the pause menu
            if (settingsScreenBackground != null) {
                g.drawImage(settingsScreenBackground, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Fallback to a dark gray background if the image is not loaded
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // Draw pause menu title
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(48f));
            String title = "Pause Menu";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            int titleX = (getWidth() - titleWidth) / 2;
            int titleY = 100;
            g.drawString(title, titleX, titleY);

            // Draw pause menu options
            String[] pauseOptions = {"Continue", "Back to Home", "Settings"};
            int optionHeight = g.getFontMetrics().getHeight();
            int spacing = 20;
            int totalHeight = pauseOptions.length * optionHeight + (pauseOptions.length - 1) * spacing;
            int startY = (getHeight() - totalHeight) / 2;

            for (int i = 0; i < pauseOptions.length; i++) {
                String option = pauseOptions[i];
                int optionY = startY + i * (optionHeight + spacing);

                if (i == selectedOption) {
                    g.setColor(Color.YELLOW); // Highlight selected option
                    g.setFont(g.getFont().deriveFont(48f)); // Larger font for selected option
                } else {
                    g.setColor(Color.WHITE);
                    g.setFont(g.getFont().deriveFont(36f)); // Normal font for unselected options
                }

                // Recalculate the width of the text dynamically based on the current font
                int optionWidth = g.getFontMetrics().stringWidth(option);
                int optionX = (getWidth() - optionWidth) / 2; // Center horizontally

                g.drawString(option, optionX, optionY);
            }
            return;
        }

        if (isSettingsMenu) {
            // Draw the background image for the settings menu
            if (settingsScreenBackground != null) {
                g.drawImage(settingsScreenBackground, 0, 0, getWidth(), getHeight(), null);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // Draw settings menu title
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(48f));
            String title = "Settings";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            int titleX = (getWidth() - titleWidth) / 2;
            int titleY = 100;
            g.drawString(title, titleX, titleY);

            // Draw settings options
            String[] settingsOptions = {
                "Difficulty: " + difficulty, // Display current difficulty
                useWASDControls ? "Controls (W-A-S-D)" : "Controls (\u2191 \u2190 \u2193 \u2192)", // Arrow symbols
                "Back"
            };

            int optionHeight = g.getFontMetrics().getHeight();
            int spacing = 20;
            int totalHeight = settingsOptions.length * optionHeight + (settingsOptions.length - 1) * spacing;
            int startY = (getHeight() - totalHeight) / 2;

            for (int i = 0; i < settingsOptions.length; i++) {
                int optionY = startY + i * (optionHeight + spacing);
                if (i == selectedSetting) {
                    g.setColor(Color.YELLOW); // Highlight selected option
                } else {
                    g.setColor(Color.WHITE);
                }
                g.drawString(settingsOptions[i], (getWidth() - g.getFontMetrics().stringWidth(settingsOptions[i])) / 2, optionY);
            }
            return;
        }

        if (isStartMenu) {
            // Draw the background image for the start screen
            if (startScreenBackground != null) {
                g.drawImage(startScreenBackground, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Fallback to gradient background if the image is not loaded
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, Color.BLUE, getWidth(), getHeight(), Color.BLACK));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // Draw the title with a glowing effect
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(64f));
            String title = "BomberMan";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            int titleX = (getWidth() - titleWidth) / 2;
            int titleY = 100;
            g.drawString(title, titleX, titleY);

            // Define optionHeight and spacing for menu options
            int optionHeight = g.getFontMetrics().getHeight();
            int spacing = 20;
            int totalHeight = options.length * optionHeight + (options.length - 1) * spacing;

            // Draw menu options perfectly centered and symmetrically aligned
            int startY = (getHeight() - totalHeight) / 2;

            for (int i = 0; i < options.length; i++) {
                String option = options[i];
                int optionY = startY + i * (optionHeight + spacing);

                if (i == selectedOption) {
                    g.setFont(g.getFont().deriveFont(48f));
                    g.setColor(Color.YELLOW);
                } else {
                    g.setFont(g.getFont().deriveFont(36f));
                    g.setColor(Color.WHITE);
                }

                int optionWidth = g.getFontMetrics().stringWidth(option);
                int optionX = (getWidth() - optionWidth) / 2;
                g.drawString(option, optionX, optionY);
            }
            return;
        }
        // Draw the game normally
        if (isGameOver) {
            // Draw the game state in the background
            int offsetY = HUD_HEIGHT;
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int mapWidthPx = COLS * TILE_SIZE;
            int mapHeightPx = ROWS * TILE_SIZE;
            int offsetX = (panelWidth - mapWidthPx) / 2;
            int offsetMapY = offsetY + (panelHeight - offsetY - mapHeightPx) / 2;

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    int drawX = offsetX + col * TILE_SIZE;
                    int drawY = offsetMapY + row * TILE_SIZE;
                    if (board[row][col] == 'W') {
                        if (wallImage != null) {
                            g.drawImage(wallImage, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                        } else {
                            g.setColor(Color.GRAY);
                            g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        }
                    } else if (board[row][col] == 'B') {
                        int anim = breakableBlockAnimState[row][col];
                        if (anim == 0 && breakableBlockImage != null) {
                            g.drawImage(breakableBlockImage, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                        }
                    }
                }
            }

            if (isAnimatingDeath) {
                g.drawImage(deathAnimationFrames[deathAnimationState], offsetX + playerX * TILE_SIZE, offsetMapY + playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            } else {
                BufferedImage playerImage = null;
                if ("front".equals(playerDirection)) {
                    switch (playerMovement) {
                        case "left":
                            playerImage = playerFrontLeft;
                            break;
                        case "right":
                            playerImage = playerFrontRight;
                            break;
                        default:
                            playerImage = playerFrontIdle;
                    }
                } else if ("back".equals(playerDirection)) {
                    switch (playerMovement) {
                        case "left":
                            playerImage = playerBackLeft;
                            break;
                        case "right":
                            playerImage = playerBackRight;
                            break;
                        default:
                            playerImage = playerBackIdle;
                    }
                }
                if (playerImage != null) {
                    g.drawImage(playerImage, offsetX + playerX * TILE_SIZE, offsetMapY + playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }

            drawBombs(g, offsetX, offsetMapY);
            drawExplosions(g, offsetX, offsetMapY);

            // Create a gray overlay
            g.setColor(new Color(128, 128, 128, 150)); // Gray with 150 alpha for transparency
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw the Game Over text at the top center
            g.setColor(Color.RED);
            g.setFont(g.getFont().deriveFont(48f));
            String gameOverText = "GAME OVER";
            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
            int x = (getWidth() - textWidth) / 2;
            int y = 100;
            g.drawString(gameOverText, x, y);

            // Draw Game Over options
            String[] gameOverOptions = {"Back to Home", "Retry"};
            int optionHeight = g.getFontMetrics().getHeight();
            int spacing = 20;
            int totalHeight = gameOverOptions.length * optionHeight + (gameOverOptions.length - 1) * spacing;
            int startY = (getHeight() - totalHeight) / 2 + 50;

            for (int i = 0; i < gameOverOptions.length; i++) {
                String option = gameOverOptions[i];
                int optionY = startY + i * (optionHeight + spacing);

                if (i == selectedGameOverOption) {
                    g.setColor(Color.YELLOW); // Highlight selected option
                    g.setFont(g.getFont().deriveFont(36f)); // Larger font for selected option
                } else {
                    g.setColor(Color.WHITE);
                    g.setFont(g.getFont().deriveFont(24f)); // Normal font for unselected options
                }

                // Recalculate the width of the text dynamically based on the current font
                int optionWidth = g.getFontMetrics().stringWidth(option);
                int optionX = (getWidth() - optionWidth) / 2; // Center horizontally

                g.drawString(option, optionX, optionY);
            }
            return;
        }
        if (isGameWon) {
            // Draw the background image for the win screen
            if (startScreenBackground != null) {
                g.drawImage(startScreenBackground, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Fallback to a dark gray background if the image is not loaded
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // Draw the "Victory!" text
            g.setColor(Color.GREEN);
            g.setFont(g.getFont().deriveFont(72f)); // Set font size to 72
            String victoryText = "Victory!";
            int textWidth = g.getFontMetrics().stringWidth(victoryText);
            int textHeight = g.getFontMetrics().getHeight();
            int textX = (getWidth() - textWidth) / 2;
            int textY = (getHeight() + textHeight) / 2;
            g.drawString(victoryText, textX, textY);

            return;
        }
        int offsetY = HUD_HEIGHT;
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int mapWidthPx = COLS * TILE_SIZE;
        int mapHeightPx = ROWS * TILE_SIZE;
        int offsetX = (panelWidth - mapWidthPx) / 2;
        int offsetMapY = offsetY + (panelHeight - offsetY - mapHeightPx) / 2;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int drawX = offsetX + col * TILE_SIZE;
                int drawY = offsetMapY + row * TILE_SIZE;
                if (board[row][col] == 'W') {
                    if (wallImage != null) {
                        g.drawImage(wallImage, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g.setColor(Color.GRAY);
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    }
                } else if (board[row][col] == 'B') {
                    int anim = breakableBlockAnimState[row][col];
                    if (anim == 0 && breakableBlockImage != null) {
                        g.drawImage(breakableBlockImage, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    } else if (anim == 1 && breakableBlockImageAnim1 != null) {
                        g.drawImage(breakableBlockImageAnim1, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    } else if (anim == 2 && breakableBlockImageAnim2 != null) {
                        g.drawImage(breakableBlockImageAnim2, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    } else if (anim == 3 && breakableBlockImageAnim3 != null) {
                        g.drawImage(breakableBlockImageAnim3, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    } else if (anim == 4 && breakableBlockImageAnim4 != null) {
                        g.drawImage(breakableBlockImageAnim4, drawX, drawY, TILE_SIZE, TILE_SIZE, null);
                    } else if (anim == -1) {
                        // Block ist zerstört, nichts zeichnen
                    } else {
                        g.setColor(Color.ORANGE);
                        g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
        BufferedImage playerImage = null;
        if ("front".equals(playerDirection)) {
            switch (playerMovement) {
                case "left":
                    playerImage = playerFrontLeft;
                    break;
                case "right":
                    playerImage = playerFrontRight;
                    break;
                default:
                    playerImage = playerFrontIdle;
            }
        } else if ("back".equals(playerDirection)) {
            switch (playerMovement) {
                case "left":
                    playerImage = playerBackLeft;
                    break;
                case "right":
                    playerImage = playerBackRight;
                    break;
                default:
                    playerImage = playerBackIdle;
            }
        }
        if (playerImage != null) {
            g.drawImage(playerImage, offsetX + playerX * TILE_SIZE, offsetMapY + playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }
        drawBombs(g, offsetX, offsetMapY);
        drawExplosions(g, offsetX, offsetMapY);

        // Draw the goal if it is not yet reached
        if (!isGameWon && board[goalY][goalX] == 'G') {
            g.drawImage(goalImage1, offsetX + goalX * TILE_SIZE, offsetMapY + goalY * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }

        // Draw enemies
        if (!isGameOver && !isGameWon) {
            for (Enemy enemy : enemies) {
                if (enemyImage != null) {
                    g.drawImage(enemyImage, offsetX + enemy.x * TILE_SIZE, offsetMapY + enemy.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(Color.PINK);
                    g.fillOval(offsetX + enemy.x * TILE_SIZE, offsetMapY + enemy.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw enemy death animations
        for (int[] pos : dyingEnemies) {
            int x = pos[0];
            int y = pos[1];
            int anim = enemyDeathState[y][x];
            if (anim > 0 && anim <= 3 && enemyDeathFrames != null) {
                g.drawImage(enemyDeathFrames[anim - 1], offsetX + x * TILE_SIZE, offsetMapY + y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    private void drawBombs(Graphics g, int offsetX, int offsetY) {
        for (int[] bomb : activeBombs) {
            int bombX = bomb[0];
            int bombY = bomb[1];
            int anim = bombAnimState[bombY][bombX];
            BufferedImage bombImage = null;
            switch (anim) {
                case 1:
                    bombImage = bombState1;
                    break;
                case 2:
                    bombImage = bombState2;
                    break;
                case 3:
                    bombImage = bombState3;
                    break;
            }
            if (bombImage != null) {
                g.drawImage(bombImage, offsetX + bombX * TILE_SIZE, offsetY + bombY * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    private void drawExplosions(Graphics g, int offsetX, int offsetY) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int anim = explosionAnimState[row][col];
                BufferedImage explosionImage = null;
                switch (anim) {
                    case 1:
                        explosionImage = explosionState1;
                        break;
                    case 2:
                        explosionImage = explosionState2;
                        break;
                    case 3:
                        explosionImage = explosionState3;
                        break;
                    case 4:
                        explosionImage = explosionState4;
                        break;
                }
                if (explosionImage != null) {
                    g.drawImage(explosionImage, offsetX + col * TILE_SIZE, offsetY + row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }

    private void checkGameOver() {
        if (explosionAnimState[playerY][playerX] > 0) {
            isGameOver = true;
            startDeathAnimation();
            repaint();
        }
    }

    private void executeSelectedOption() {
        switch (selectedOption) {
            case 0:
                isStartMenu = false; // Start the game
                repaint();
                break;
            case 1:
                // Open settings (to be implemented)
                openSettings();
                break;
            case 2:
                System.exit(0); // Quit the game
                break;
        }
    }

    // Simple enemy class
    private static class Enemy {
        int x, y;
        Enemy(int x, int y) { this.x = x; this.y = y; }
    }

    private void updateControlScheme() {
        if (useWASDControls) {
            System.out.println("Using W-A-S-D controls");
            // Additional logic to apply W-A-S-D controls can be added here
        } else {
            System.out.println("Using Arrow Key controls");
            // Additional logic to apply arrow key controls can be added here
        }
    }

    public void setQuitAction(Runnable quitAction) {
        this.quitAction = quitAction;
    }

    private void handleQuit() {
        if (quitAction != null) {
            quitAction.run();
        }
    }
}
