package games.Tetris;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Tetris {
    // Neue Signatur mit Callback
    public static void start(Runnable onExitToMenu) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setResizable(true);
            TetrisPanel panel = new TetrisPanel(onExitToMenu); // Callback übergeben
            frame.add(panel);
            frame.setMinimumSize(panel.getPreferredSize());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

class TetrisPanel extends JPanel implements ActionListener, KeyListener {
    private final int ROWS = 20;
    private final int COLS = 10;
    private final int TILE = 32;
    private final int SIDE_PANEL = 120;
    private final Color[] COLORS = {
        new Color(0,0,0,0), // 0: transparent/empty
        new Color(0, 255, 255),    // I - Cyan
        new Color(0, 0, 255),      // J - Blue
        new Color(255, 140, 0),    // L - Orange
        new Color(255, 255, 0),    // O - Yellow
        new Color(0, 255, 0),      // S - Green
        new Color(160, 0, 240),    // T - Purple
        new Color(255, 0, 0)       // Z - Red
    };

    private final Color[] SHINY_COLORS = {
        new Color(0,0,0,0),
        new Color(0, 255, 255, 220),
        new Color(0, 0, 255, 220),
        new Color(255, 140, 0, 220),
        new Color(255, 255, 0, 220),
        new Color(0, 255, 0, 220),
        new Color(160, 0, 240, 220),
        new Color(255, 0, 0, 220)
    };

    private Timer timer;
    private int[][] board = new int[ROWS][COLS];
    private Tetromino current;
    private Tetromino next;
    private int curRow, curCol;
    private boolean gameOver = false;
    private int score = 0;
    private int highscore = 0;
    private int linesCleared = 0;
    private int level = 1;
    private int dropDelay = 400;
    private boolean softDrop = false;
    private boolean hardDropAnim = false;
    private int hardDropY = -1;
    private int hardDropAnimFrames = 0;
    private Runnable onExitToMenu;  // Neuer Feld zur Speicherung des Callback

    // Neuer Konstruktor, der den Callback entgegennimmt
    public TetrisPanel(Runnable onExitToMenu) {
        this();
        this.onExitToMenu = onExitToMenu;
    }
    
    // Bestehender Standardkonstruktor wird beibehalten
    public TetrisPanel() {
        setPreferredSize(new Dimension(COLS * TILE + SIDE_PANEL, ROWS * TILE));
        setBackground(new Color(30, 30, 40));
        setFocusable(true);
        addKeyListener(this);
        loadHighscore();
        timer = new Timer(dropDelay, this);
        spawnTetromino();
        timer.start();
    }

    private void loadHighscore() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(System.getProperty("user.home"), ".tetris_highscore");
            if (java.nio.file.Files.exists(path)) {
                String s = java.nio.file.Files.readAllLines(path).get(0);
                highscore = Integer.parseInt(s.trim());
            }
        } catch (Exception ignored) {}
    }

    private void saveHighscore() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(System.getProperty("user.home"), ".tetris_highscore");
            java.nio.file.Files.write(path, String.valueOf(highscore).getBytes());
        } catch (Exception ignored) {}
    }

    private void spawnTetromino() {
        if (next == null) {
            current = Tetromino.randomTetromino();
            next = Tetromino.randomTetromino();
        } else {
            current = next;
            next = Tetromino.randomTetromino();
        }
        curRow = 0;
        curCol = COLS / 2 - 2;
        if (!canMove(current.shape, curRow, curCol)) {
            gameOver = true;
            timer.stop();
            if (score > highscore) {
                highscore = score;
                saveHighscore();
            }
        }
    }

    private boolean canMove(int[][] shape, int row, int col) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    int newRow = row + r;
                    int newCol = col + c;
                    if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol >= COLS)
                        return false;
                    if (board[newRow][newCol] != 0)
                        return false;
                }
            }
        }
        return true;
    }

    private void mergeTetromino() {
        for (int r = 0; r < current.shape.length; r++) {
            for (int c = 0; c < current.shape[0].length; c++) {
                if (current.shape[r][c] != 0) {
                    board[curRow + r][curCol + c] = current.color;
                }
            }
        }
    }

    private void clearLines() {
        int lines = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int row = r; row > 0; row--) {
                    System.arraycopy(board[row - 1], 0, board[row], 0, COLS);
                }
                for (int c = 0; c < COLS; c++) {
                    board[0][c] = 0;
                }
                score += 100 * level;
                lines++;
                linesCleared++;
                r++; // check same row again
            }
        }
        if (lines > 0) {
            level = 1 + linesCleared / 10;
            dropDelay = Math.max(60, 400 - (level - 1) * 30);
            timer.setDelay(dropDelay);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Fülle das ganze Fenster mit dem Spielfeld-Hintergrund
        g.setColor(new Color(40, 40, 60));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Berechne feste Dimensionen des Spielfelds
        int boardWidth = COLS * TILE;
        int boardHeight = ROWS * TILE;
        int totalWidth = boardWidth + SIDE_PANEL;
        int totalHeight = boardHeight;
        // Berechne Offsets, um das Spielfeld zentriert anzuzeigen
        int offsetX = (getWidth() - totalWidth) / 2;
        int offsetY = (getHeight() - totalHeight) / 2;

        // Zeichne Spielfeld-Hintergrund
        g.setColor(new Color(40, 40, 60));
        g.fillRoundRect(offsetX, offsetY, boardWidth, boardHeight, 16, 16);

        // Zeichne Spielfeld-Kacheln
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                drawTile(g, board[r][c], offsetX + c * TILE, offsetY + r * TILE, false, false);
            }
        }

        // Zeichne Geisterfigur
        if (!gameOver) {
            int ghostRow = curRow;
            while (canMove(current.shape, ghostRow + 1, curCol)) ghostRow++;
            for (int r = 0; r < current.shape.length; r++) {
                for (int c = 0; c < current.shape[0].length; c++) {
                    if (current.shape[r][c] != 0) {
                        drawTile(g, current.color, offsetX + (curCol + c) * TILE, offsetY + (ghostRow + r) * TILE, true, false);
                    }
                }
            }
        }

        // Zeichne aktuelles Tetromino
        if (!gameOver) {
            for (int r = 0; r < current.shape.length; r++) {
                for (int c = 0; c < current.shape[0].length; c++) {
                    if (current.shape[r][c] != 0) {
                        boolean anim = hardDropAnim && (curRow + r) == hardDropY;
                        drawTile(g, current.color, offsetX + (curCol + c) * TILE, offsetY + (curRow + r) * TILE, false, anim);
                    }
                }
            }
        }

        // Zeichne Gitter
        g.setColor(new Color(80, 80, 100));
        for (int r = 0; r <= ROWS; r++)
            g.drawLine(offsetX, offsetY + r * TILE, offsetX + boardWidth, offsetY + r * TILE);
        for (int c = 0; c <= COLS; c++)
            g.drawLine(offsetX + c * TILE, offsetY, offsetX + c * TILE, offsetY + boardHeight);

        // Zeichne Seitenpanel relativ zum Offset
        drawSidePanel(g, offsetX + boardWidth, offsetY);

        // Zeichne Game Over (unverändert)
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, getHeight() / 2 - 60, getWidth(), 120);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("GAME OVER", getWidth() / 2 - 110, getHeight() / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.setColor(Color.WHITE);
            g.drawString("Leertaste für Neustart", getWidth() / 2 - 90, getHeight() / 2 + 40);
            g.drawString("ESC zum Schließen", getWidth() / 2 - 80, getHeight() / 2 + 70);
        }
    }

    // Angepasste drawSidePanel()-Methode, verschiebt die Zeichnung um den Offset
    private void drawSidePanel(Graphics g, int panelX, int panelY) {
        int x = panelX + 10;
        int y = panelY + 20;
        g.setColor(new Color(60, 60, 80));
        g.fillRoundRect(panelX, panelY, SIDE_PANEL, ROWS * TILE, 16, 16);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score:", x, y + 10);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString(String.valueOf(score), x, y + 35);
        
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Highscore:", x, y + 65);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString(String.valueOf(highscore), x, y + 90);
        
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Level: " + level, x, y + 120);
        g.drawString("Lines: " + linesCleared, x, y + 145);
        
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Next:", x, y + 180);
        if (next != null) {
            int[][] origShape = next.shape;
            // Für den I-Block (color == 1) drehe das Shape, um 4 Kästchen vertikal darzustellen.
            int[][] shapeToDraw = (next.color == 1) ? next.rotate() : origShape;
            int color = next.color;
            // Definiere den Vorschau-Bereich
            int previewAreaX = x;
            int previewAreaY = y + 200;
            int previewAreaWidth = SIDE_PANEL - 20;
            int previewAreaHeight = 4 * TILE;
            // Berechne die Bounding Box des zu zeichnenden Shapes
            int minR = shapeToDraw.length, maxR = -1, minC = shapeToDraw[0].length, maxC = -1;
            for (int r = 0; r < shapeToDraw.length; r++) {
                for (int c = 0; c < shapeToDraw[0].length; c++) {
                    if (shapeToDraw[r][c] != 0) {
                        if (r < minR) minR = r;
                        if (r > maxR) maxR = r;
                        if (c < minC) minC = c;
                        if (c > maxC) maxC = c;
                    }
                }
            }
            int shapeRows = maxR - minR + 1;
            int shapeCols = maxC - minC + 1;
            // Zentriere das Tetromino im Vorschau-Bereich
            int dx = previewAreaX + (previewAreaWidth - shapeCols * TILE) / 2;
            int dy = previewAreaY + (previewAreaHeight - shapeRows * TILE) / 2;
            for (int r = 0; r < shapeToDraw.length; r++) {
                for (int c = 0; c < shapeToDraw[0].length; c++) {
                    if (shapeToDraw[r][c] != 0) {
                        drawTile(g, color, dx + (c - minC) * TILE, dy + (r - minR) * TILE, false, false);
                    }
                }
            }
        }
    }

    private void drawTile(Graphics g, int colorIdx, int x, int y, boolean ghost, boolean anim) {
        if (colorIdx == 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        Color base = SHINY_COLORS[colorIdx];
        if (ghost) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        }
        if (anim) {
            g2.setColor(base.brighter());
        } else {
            g2.setColor(base);
        }
        g2.fillRoundRect(x + 2, y + 2, TILE - 4, TILE - 4, 10, 10);

        // Add a highlight for a shiny effect
        g2.setColor(new Color(255, 255, 255, ghost ? 60 : 120));
        g2.fillRoundRect(x + 5, y + 5, TILE / 2, TILE / 4, 8, 8);

        // Add a border
        g2.setColor(base.darker().darker());
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x + 2, y + 2, TILE - 4, TILE - 4, 10, 10);

        g2.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (hardDropAnim) {
                hardDropAnimFrames--;
                if (hardDropAnimFrames <= 0) {
                    hardDropAnim = false;
                    mergeTetromino();
                    clearLines();
                    spawnTetromino();
                }
                repaint();
                return;
            }
            if (softDrop && canMove(current.shape, curRow + 1, curCol)) {
                curRow++;
                score += 1;
            } else if (canMove(current.shape, curRow + 1, curCol)) {
                curRow++;
            } else {
                mergeTetromino();
                clearLines();
                spawnTetromino();
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                board = new int[ROWS][COLS];
                score = 0;
                linesCleared = 0;
                level = 1;
                dropDelay = 400;
                timer.setDelay(dropDelay);
                gameOver = false;
                current = null;
                next = null;
                spawnTetromino();
                timer.start();
                repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.dispose(); // Spiel-Fenster schließen
                if(onExitToMenu != null) {
                    onExitToMenu.run(); // Callback aufrufen, um das Hauptmenü anzuzeigen
                }
            }
            return;
        }
        boolean repaintNeeded = false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (canMove(current.shape, curRow, curCol - 1)) {
                    curCol--;
                    repaintNeeded = true;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (canMove(current.shape, curRow, curCol + 1)) {
                    curCol++;
                    repaintNeeded = true;
                }
                break;
            case KeyEvent.VK_DOWN:
                softDrop = true;
                break;
            case KeyEvent.VK_UP:
                int[][] rotated = current.rotate();
                if (canMove(rotated, curRow, curCol)) {
                    current.shape = rotated;
                    repaintNeeded = true;
                }
                break;
            case KeyEvent.VK_SPACE:
                // Hard drop with animation
                int dropTo = curRow;
                while (canMove(current.shape, dropTo + 1, curCol)) dropTo++;
                if (dropTo != curRow) {
                    hardDropY = dropTo + current.shape.length - 1;
                    curRow = dropTo;
                    hardDropAnim = true;
                    hardDropAnimFrames = 4;
                    score += 2 * (dropTo - curRow + 1);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                frame.dispose();
                if(onExitToMenu != null) {
                    onExitToMenu.run();
                }
                break;
        }
        if (repaintNeeded) repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            softDrop = false;
        }
    }
    @Override public void keyTyped(KeyEvent e) {}
}

class Tetromino {
    public int[][] shape;
    public int color;
    private static final int[][][] SHAPES = {
        // I
        {
            {0,0,0,0},
            {1,1,1,1},
            {0,0,0,0},
            {0,0,0,0}
        },
        // J
        {
            {2,0,0},
            {2,2,2},
            {0,0,0}
        },
        // L
        {
            {0,0,3},
            {3,3,3},
            {0,0,0}
        },
        // O
        {
            {4,4},
            {4,4}
        },
        // S
        {
            {0,5,5},
            {5,5,0},
            {0,0,0}
        },
        // T
        {
            {0,6,0},
            {6,6,6},
            {0,0,0}
        },
        // Z
        {
            {7,7,0},
            {0,7,7},
            {0,0,0}
        }
    };

    public Tetromino(int[][] shape, int color) {
        this.shape = shape;
        this.color = color;
    }

    public static Tetromino randomTetromino() {
        Random rand = new Random();
        int idx = rand.nextInt(SHAPES.length);
        int[][] shape = deepCopy(SHAPES[idx]);
        return new Tetromino(shape, idx + 1);
    }

    public int[][] rotate() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                rotated[c][rows - 1 - r] = shape[r][c];
        return rotated;
    }

    private static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][];
        for (int i = 0; i < arr.length; i++)
            copy[i] = arr[i].clone();
        return copy;
    }
}
