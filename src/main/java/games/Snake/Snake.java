package games.Snake;
import players.Players;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class Snake {
    public static void start(Runnable onExitToMenu) {
        System.out.println("Aktueller Spieler: " + players.Players.getCurrentPlayer());

        JFrame frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true); // Kein Rahmen
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Vollbild
        frame.add(new GamePanel(onExitToMenu, frame));
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
        private boolean settingsMenu = false; // Einstellungen offen
        private boolean useWasd = false; // Steuerung: false = Pfeiltasten, true = WASD
        private int level = 1; // 1=Easy, 2=Medium, 3=Hard
        private boolean levelSelectMenu = true; // Level-Auswahl aktiv

        // Für dynamisches Delay
        private int DELAY_CURRENT = DELAY;

        // Berechnung für Spielfeld-Positionierung
        private int getFieldX() {
            return (getWidth() - WIDTH * TILE_SIZE) / 2;
        }
        private int getFieldY() {
            return (getHeight() - HEIGHT * TILE_SIZE) / 2;
        }

        // Level-abhängige Start-Delays
        private int getLevelDelay() {
            switch (level) {
                case 1: return 120; // Easy
                case 2: return 90;  // Medium
                case 3: return 60;  // Hard
                default: return 100;
            }
        }

        private final Runnable onExitToMenu;
        private final JFrame frame;

        public GamePanel(Runnable onExitToMenu, JFrame frame) {
            this.onExitToMenu = onExitToMenu;
            this.frame = frame;
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
            DELAY_CURRENT = getLevelDelay(); // Delay je nach Level
            timer = new Timer(DELAY_CURRENT, this);
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

            // Level-Auswahl-Overlay
            if (levelSelectMenu) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 44));
                String title = "Level auswählen";
                int titleWidth = g2d.getFontMetrics().stringWidth(title);
                g2d.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 100);

                g2d.setFont(new Font("Arial", Font.PLAIN, 32));
                String l1 = (level == 1 ? "> " : "  ") + "Level 1: Easy";
                String l2 = (level == 2 ? "> " : "  ") + "Level 2: Medium";
                String l3 = (level == 3 ? "> " : "  ") + "Level 3: Hard";
                g2d.drawString(l1, (getWidth() - g2d.getFontMetrics().stringWidth(l1)) / 2, getHeight() / 2 - 20);
                g2d.drawString(l2, (getWidth() - g2d.getFontMetrics().stringWidth(l2)) / 2, getHeight() / 2 + 30);
                g2d.drawString(l3, (getWidth() - g2d.getFontMetrics().stringWidth(l3)) / 2, getHeight() / 2 + 80);

                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                String info = "W/S oder Pfeil Hoch/Runter: Auswahl   ENTER: Bestätigen   ESC: Zurück";
                int infoWidth = g2d.getFontMetrics().stringWidth(info);
                g2d.drawString(info, (getWidth() - infoWidth) / 2, getHeight() / 2 + 140);

                g2d.dispose();
                return;
            }

            // Overlay für Start/Restart/Pause/Settings
            if (waitingForStart || !running || paused || settingsMenu) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(Color.WHITE);

                if (settingsMenu) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 44));
                    String settingsTitle = "Einstellungen";
                    int stw = g2d.getFontMetrics().stringWidth(settingsTitle);
                    g2d.drawString(settingsTitle, (getWidth() - stw) / 2, getHeight() / 2 - 80);

                    g2d.setFont(new Font("Arial", Font.PLAIN, 28));
                    String ctrl = "Steuerung: " + (useWasd ? "WASD" : "Pfeiltasten");
                    int ctrlw = g2d.getFontMetrics().stringWidth(ctrl);
                    g2d.drawString(ctrl, (getWidth() - ctrlw) / 2, getHeight() / 2 - 20);

                    g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                    String switchMsg = "S zum Umschalten";
                    int sw = g2d.getFontMetrics().stringWidth(switchMsg);
                    g2d.drawString(switchMsg, (getWidth() - sw) / 2, getHeight() / 2 + 30);

                    String escMsg = "ESC zurück";
                    int escw = g2d.getFontMetrics().stringWidth(escMsg);
                    g2d.drawString(escMsg, (getWidth() - escw) / 2, getHeight() / 2 + 70);

                    g2d.dispose();
                    return;
                }

                if (paused) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 48));
                    String pauseMsg = "PAUSE";
                    int pauseWidth = g2d.getFontMetrics().stringWidth(pauseMsg);
                    g2d.drawString(pauseMsg, (getWidth() - pauseWidth) / 2, getHeight() / 2 - 100);

                    g2d.setFont(new Font("Arial", Font.BOLD, 32));
                    String resumeMsg = "P zum Fortsetzen";
                    int resumeWidth = g2d.getFontMetrics().stringWidth(resumeMsg);
                    g2d.drawString(resumeMsg, (getWidth() - resumeWidth) / 2, getHeight() / 2 - 40);

                    String restartMsg = "R für Neustart";
                    int restartWidth = g2d.getFontMetrics().stringWidth(restartMsg);
                    g2d.drawString(restartMsg, (getWidth() - restartWidth) / 2, getHeight() / 2);

                    String settingsMsg = "E für Einstellungen";
                    int settingsWidth = g2d.getFontMetrics().stringWidth(settingsMsg);
                    g2d.drawString(settingsMsg, (getWidth() - settingsWidth) / 2, getHeight() / 2 + 40);

                    String levelMsg = "L für Level-Auswahl";
                    int levelWidth = g2d.getFontMetrics().stringWidth(levelMsg);
                    g2d.drawString(levelMsg, (getWidth() - levelWidth) / 2, getHeight() / 2 + 80);

                    g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                    String qMsg = "Q für Hauptmenü";
                    int qWidth = g2d.getFontMetrics().stringWidth(qMsg);
                    g2d.drawString(qMsg, (getWidth() - qWidth) / 2, getHeight() / 2 + 90);

                    g2d.dispose();
                    return;
                }

                // Game Over Overlay (wenn !waitingForStart && !running)
                if (!waitingForStart && !running) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 48));
                    String overMsg = "Game Over!";
                    int overWidth = g2d.getFontMetrics().stringWidth(overMsg);
                    g2d.drawString(overMsg, (getWidth() - overWidth) / 2, getHeight() / 2 - 100);

                    g2d.setFont(new Font("Arial", Font.BOLD, 32));
                    String restartMsg = "Leertaste für Neustart";
                    int restartWidth = g2d.getFontMetrics().stringWidth(restartMsg);
                    g2d.drawString(restartMsg, (getWidth() - restartWidth) / 2, getHeight() / 2 - 40);

                    String levelMsg = "L für Level-Auswahl";
                    int levelWidth = g2d.getFontMetrics().stringWidth(levelMsg);
                    g2d.drawString(levelMsg, (getWidth() - levelWidth) / 2, getHeight() / 2);

                    String escMsg = "ESC zum Schließen";
                    int escWidth = g2d.getFontMetrics().stringWidth(escMsg);
                    g2d.drawString(escMsg, (getWidth() - escWidth) / 2, getHeight() / 2 + 40);

                    g2d.setFont(new Font("Arial", Font.BOLD, 28));
                    String hs = "Highscore: " + highscore;
                    int hsWidth = g2d.getFontMetrics().stringWidth(hs);
                    g2d.drawString(hs, (getWidth() - hsWidth) / 2, getHeight() / 2 + 100);

                    g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                    String qMsg = "Q für Hauptmenü";
                    int qWidth = g2d.getFontMetrics().stringWidth(qMsg);
                    g2d.drawString(qMsg, (getWidth() - qWidth) / 2, getHeight() / 2 + 70);

                    g2d.dispose();
                    return;
                }

                // ...existing overlay code for Start...
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

                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                String qMsg = "Q für Hauptmenü";
                int qWidth = g2d.getFontMetrics().stringWidth(qMsg);
                g2d.drawString(qMsg, (getWidth() - qWidth) / 2, getHeight() / 2 + 90);

                g2d.setFont(new Font("Arial", Font.BOLD, 28));
                String hs = "Highscore: " + highscore;
                int hsWidth = g2d.getFontMetrics().stringWidth(hs);
                g2d.drawString(hs, (getWidth() - hsWidth) / 2, getHeight() / 2 + 120);

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
            if (levelSelectMenu) {
                repaint();
                return;
            }
            if (!waitingForStart && running && !paused && !settingsMenu) {
                move();
            }
            repaint();
        }

        private void updateSpeed() {
            // Verlangsamt minimal ab einer bestimmten Länge, aber nicht zu stark, Level-basiert
            int minDelay = 30; // nicht langsamer als 30ms
            int baseDelay = getLevelDelay();
            int newDelay = baseDelay + Math.min(60, snake.size() / 5); // alle 5 Felder +1ms
            DELAY_CURRENT = Math.min(newDelay, baseDelay + 60);
            if (timer != null) {
                timer.setDelay(Math.max(DELAY_CURRENT, minDelay));
            }
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
                Players.writeHighscore("Snake", score);
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
            updateSpeed(); // Geschwindigkeit ggf. anpassen
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            // Level-Auswahl-Menü
            if (levelSelectMenu) {
                if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
                    level = level > 1 ? level - 1 : 3;
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
                    level = level < 3 ? level + 1 : 1;
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                    levelSelectMenu = false;
                    waitingForStart = true;
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_ESCAPE) {
                    // Zurück ins Pause- oder GameOver-Menü
                    if (!running && !waitingForStart) {
                        // Game Over
                        levelSelectMenu = false;
                        repaint();
                        return;
                    }
                    if (paused) {
                        levelSelectMenu = false;
                        repaint();
                        return;
                    }
                }
                return;
            }

            // Einstellungen-Menü
            if (settingsMenu) {
                if (key == KeyEvent.VK_S) {
                    useWasd = !useWasd;
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_ESCAPE) {
                    settingsMenu = false;
                    repaint();
                    return;
                }
                return;
            }

            // Pause-Menü
            if (paused) {
                if (key == KeyEvent.VK_P) {
                    paused = false;
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_R) {
                    paused = false;
                    waitingForStart = false;
                    running = false;
                    initGame();
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_E) {
                    settingsMenu = true;
                    repaint();
                    return;
                }
                // Level-Auswahl im Pause-Menü
                if (key == KeyEvent.VK_L) {
                    levelSelectMenu = true;
                    repaint();
                    return;
                }
                return;
            }

            // Game Over: Level-Auswahl möglich
            if (!running && !waitingForStart) {
                if (key == KeyEvent.VK_L) {
                    levelSelectMenu = true;
                    repaint();
                    return;
                }
                if (key == KeyEvent.VK_Q) {
                    if (onExitToMenu != null) {
                        frame.dispose();
                        onExitToMenu.run();
                    }
                    return;
                }
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
            // Steuerung je nach Modus
            if (!waitingForStart && running && !paused) {
                if (useWasd) {
                    if (key == KeyEvent.VK_W && direction != 'D') direction = 'U';
                    if (key == KeyEvent.VK_S && direction != 'U') direction = 'D';
                    if (key == KeyEvent.VK_A && direction != 'R') direction = 'L';
                    if (key == KeyEvent.VK_D && direction != 'L') direction = 'R';
                } else {
                    if (key == KeyEvent.VK_UP && direction != 'D') direction = 'U';
                    if (key == KeyEvent.VK_DOWN && direction != 'U') direction = 'D';
                    if (key == KeyEvent.VK_LEFT && direction != 'R') direction = 'L';
                    if (key == KeyEvent.VK_RIGHT && direction != 'L') direction = 'R';
                }
            }
        }

        @Override public void keyReleased(KeyEvent e) {}
        @Override public void keyTyped(KeyEvent e) {}

    }
}
