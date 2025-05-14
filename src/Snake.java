import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class Snake {
    public void start() {
        JFrame frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true); // Kein Rahmen
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Vollbild
        frame.add(new GamePanel());
        frame.setVisible(true);
    }

    // Die Methode start() ist bereits vorhanden und kann wie folgt verwendet werden:
    // new SnakeGame().start();

    // Inneres Panel für das Spiel
    static class GamePanel extends JPanel implements ActionListener, KeyListener {
        private final int TILE_SIZE = 20;
        private final int WIDTH = 20;
        private final int HEIGHT = 20;
        private final int DELAY = 100;

        private LinkedList<Point> snake;
        private Point food;
        private char direction = 'R';
        private boolean running = false;
        private Timer timer;
        private Random rand = new Random();
        private boolean waitingForStart = true;
        private int score = 0;
        private int highscore = 0;
        private boolean paused = false; // Pause-Status

        // Berechnung für Spielfeld-Positionierung
        private int getFieldX() {
            return (getWidth() - WIDTH * TILE_SIZE) / 2;
        }
        private int getFieldY() {
            return (getHeight() - HEIGHT * TILE_SIZE) / 2;
        }

        public GamePanel() {
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);
            // initGame() wird erst nach Tastendruck aufgerufen!
        }

        private void initGame() {
            snake = new LinkedList<>();
            snake.add(new Point(WIDTH / 2, HEIGHT / 2));
            direction = 'R';
            placeFood();
            running = true;
            score = 0;
            timer = new Timer(DELAY, this);
            timer.start();
        }

        private void placeFood() {
            while (true) {
                int x = rand.nextInt(WIDTH);
                int y = rand.nextInt(HEIGHT);
                Point p = new Point(x, y);
                if (!snake.contains(p)) {
                    food = p;
                    break;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int fieldX = getFieldX();
            int fieldY = getFieldY();

            // Spielfeld-Rahmen zeichnen
            g.setColor(Color.WHITE);
            g.drawRect(fieldX - 2, fieldY - 2, WIDTH * TILE_SIZE + 3, HEIGHT * TILE_SIZE + 3);

            // Score und Highscore über dem Spielfeld
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String scoreText = "Score: " + score + "    Highscore: " + highscore;
            int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
            g.drawString(scoreText, fieldX + (WIDTH * TILE_SIZE - scoreWidth) / 2, fieldY - 20);

            // Overlay für Start/Restart/Pause
            if (waitingForStart || !running || paused) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(Color.WHITE);

                if (paused) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 48));
                    String pauseMsg = "PAUSE";
                    int pauseWidth = g2d.getFontMetrics().stringWidth(pauseMsg);
                    g2d.drawString(pauseMsg, (getWidth() - pauseWidth) / 2, getHeight() / 2 - 40);

                    g2d.setFont(new Font("Arial", Font.BOLD, 32));
                    String resumeMsg = "P zum Fortsetzen";
                    int resumeWidth = g2d.getFontMetrics().stringWidth(resumeMsg);
                    g2d.drawString(resumeMsg, (getWidth() - resumeWidth) / 2, getHeight() / 2 + 20);

                    g2d.dispose();
                    return;
                }

                // ...existing overlay code for Start/Restart...
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                String title = "Snake Spiel";
                int titleWidth = g2d.getFontMetrics().stringWidth(title);
                g2d.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 80);

                g2d.setFont(new Font("Arial", Font.PLAIN, 28));
                String info = "Mit den Pfeiltasten steuern";
                int infoWidth = g2d.getFontMetrics().stringWidth(info);
                g2d.drawString(info, (getWidth() - infoWidth) / 2, getHeight() / 2 - 30);

                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                String startMsg = waitingForStart ? "Leertaste zum Starten" : "Leertaste für Neustart";
                int startMsgWidth = g2d.getFontMetrics().stringWidth(startMsg);
                g2d.drawString(startMsg, (getWidth() - startMsgWidth) / 2, getHeight() / 2 + 30);

                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                String pauseHint = "P zum Pausieren";
                int pauseHintWidth = g2d.getFontMetrics().stringWidth(pauseHint);
                g2d.drawString(pauseHint, (getWidth() - pauseHintWidth) / 2, getHeight() / 2 + 60);

                if (!waitingForStart && !running) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                    String escMsg = "ESC zum Schließen";
                    int escMsgWidth = g2d.getFontMetrics().stringWidth(escMsg);
                    g2d.drawString(escMsg, (getWidth() - escMsgWidth) / 2, getHeight() / 2 + 90);
                }

                g2d.setFont(new Font("Arial", Font.BOLD, 28));
                String hs = "Highscore: " + highscore;
                int hsWidth = g2d.getFontMetrics().stringWidth(hs);
                g2d.drawString(hs, (getWidth() - hsWidth) / 2, getHeight() / 2 + 140);

                g2d.dispose();
                return;
            }

            // Snake zeichnen
            g.setColor(Color.GREEN);
            for (Point p : snake) {
                g.fillRect(fieldX + p.x * TILE_SIZE, fieldY + p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
            // Food zeichnen
            g.setColor(Color.RED);
            g.fillRect(fieldX + food.x * TILE_SIZE, fieldY + food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!waitingForStart && running && !paused) {
                move();
            }
            repaint();
        }

        private void move() {
            Point head = new Point(snake.getFirst());
            switch (direction) {
                case 'U': head.y--; break;
                case 'D': head.y++; break;
                case 'L': head.x--; break;
                case 'R': head.x++; break;
            }
            if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT || snake.contains(head)) {
                running = false;
                timer.stop();
                if (score > highscore) {
                    highscore = score;
                }
                return;
            }
            snake.addFirst(head);
            if (head.equals(food)) {
                placeFood();
                score++;
            } else {
                snake.removeLast();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (paused && key == KeyEvent.VK_P) {
                paused = false;
                repaint();
                return;
            }
            if (!paused && key == KeyEvent.VK_P && !waitingForStart && running) {
                paused = true;
                repaint();
                return;
            }
            if (waitingForStart && key == KeyEvent.VK_SPACE) {
                waitingForStart = false;
                initGame();
                repaint();
                return;
            }
            if (!running && key == KeyEvent.VK_ESCAPE) {
                SwingUtilities.getWindowAncestor(this).dispose();
            }
            if (!running && !waitingForStart && key == KeyEvent.VK_SPACE) {
                initGame();
                repaint();
                return;
            }
            if (!waitingForStart && running && !paused) {
                if (key == KeyEvent.VK_UP && direction != 'D') direction = 'U';
                if (key == KeyEvent.VK_DOWN && direction != 'U') direction = 'D';
                if (key == KeyEvent.VK_LEFT && direction != 'R') direction = 'L';
                if (key == KeyEvent.VK_RIGHT && direction != 'L') direction = 'R';
            }
        }

        @Override public void keyReleased(KeyEvent e) {}
        @Override public void keyTyped(KeyEvent e) {}
    }
}
