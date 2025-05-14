package games.Tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Tetris {
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 800);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.add(new TetrisPanel());
            frame.setVisible(true);
        });
    }
}

class TetrisPanel extends JPanel implements ActionListener, KeyListener {
    private final int ROWS = 20;
    private final int COLS = 10;
    private final int TILE = 30;
    private final Color[] COLORS = {
        Color.BLACK, Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED
    };

    private Timer timer;
    private int[][] board = new int[ROWS][COLS];
    private Tetromino current;
    private int curRow, curCol;
    private boolean gameOver = false;
    private int score = 0;

    public TetrisPanel() {
        setPreferredSize(new Dimension(COLS * TILE, ROWS * TILE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(400, this);
        spawnTetromino();
        timer.start();
    }

    private void spawnTetromino() {
        current = Tetromino.randomTetromino();
        curRow = 0;
        curCol = COLS / 2 - 2;
        if (!canMove(current.shape, curRow, curCol)) {
            gameOver = true;
            timer.stop();
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
                score += 100;
                r++; // check same row again
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw board
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                drawTile(g, board[r][c], c * TILE, r * TILE);
            }
        }

        // Draw current tetromino
        if (!gameOver) {
            for (int r = 0; r < current.shape.length; r++) {
                for (int c = 0; c < current.shape[0].length; c++) {
                    if (current.shape[r][c] != 0) {
                        drawTile(g, current.color, (curCol + c) * TILE, (curRow + r) * TILE);
                    }
                }
            }
        }

        // Draw grid
        g.setColor(Color.DARK_GRAY);
        for (int r = 0; r <= ROWS; r++)
            g.drawLine(0, r * TILE, COLS * TILE, r * TILE);
        for (int c = 0; c <= COLS; c++)
            g.drawLine(c * TILE, 0, c * TILE, ROWS * TILE);

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 25);

        // Draw Game Over
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

    private void drawTile(Graphics g, int colorIdx, int x, int y) {
        if (colorIdx == 0) return;
        g.setColor(COLORS[colorIdx]);
        g.fillRect(x, y, TILE, TILE);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, TILE, TILE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (canMove(current.shape, curRow + 1, curCol)) {
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
                gameOver = false;
                spawnTetromino();
                timer.start();
                repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                SwingUtilities.getWindowAncestor(this).dispose();
            }
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (canMove(current.shape, curRow, curCol - 1)) curCol--;
                break;
            case KeyEvent.VK_RIGHT:
                if (canMove(current.shape, curRow, curCol + 1)) curCol++;
                break;
            case KeyEvent.VK_DOWN:
                if (canMove(current.shape, curRow + 1, curCol)) curRow++;
                break;
            case KeyEvent.VK_UP:
                int[][] rotated = current.rotate();
                if (canMove(rotated, curRow, curCol)) current.shape = rotated;
                break;
            case KeyEvent.VK_SPACE:
                while (canMove(current.shape, curRow + 1, curCol)) curRow++;
                break;
            case KeyEvent.VK_ESCAPE:
                SwingUtilities.getWindowAncestor(this).dispose();
                break;
        }
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
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
