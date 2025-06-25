package games.Speedrun;

import java.awt.*;

public class Player extends Entity {
    public enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction dir = Direction.DOWN;
    private Animation[] walkAnimations = new Animation[4]; // [direction]
    private GameMap map;
    private InputManager input;

    private float moveCooldown = 0f;
    private static final float MOVE_DELAY = 0.08f; // seconds between moves

    // Control scheme: true = WASD, false = Arrow keys
    private boolean useWASD = true;
    private boolean phaseMode = false;

    public Player(GameMap map, InputManager input, float x, float y) {
        super(x, y);
        this.map = map;
        this.input = input;
        loadAnimations();
    }

    private void loadAnimations() {
        walkAnimations[0] = new Animation(new Image[]{
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_up_1.png"),
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_up_2.png")
        }, 0.12f);
        walkAnimations[1] = new Animation(new Image[]{
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_down_1.png"),
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_down_2.png")
        }, 0.12f);
        walkAnimations[2] = new Animation(new Image[]{
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_left_1.png"),
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_left_2.png")
        }, 0.12f);
        walkAnimations[3] = new Animation(new Image[]{
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_right_1.png"),
            ResourceManager.get().getImage("games/Speedrun/resources/player-mov/boy_right_2.png")
        }, 0.12f);
    }

    public void setUseWASD(boolean useWASD) {
        this.useWASD = useWASD;
    }

    public void setPhaseMode(boolean phase) { this.phaseMode = phase; }

    @Override
    public void update(float deltaTime) {
        moveCooldown -= deltaTime;
        int dx = 0, dy = 0;
        Direction newDir = dir;

        int up = useWASD ? java.awt.event.KeyEvent.VK_W : java.awt.event.KeyEvent.VK_UP;
        int down = useWASD ? java.awt.event.KeyEvent.VK_S : java.awt.event.KeyEvent.VK_DOWN;
        int left = useWASD ? java.awt.event.KeyEvent.VK_A : java.awt.event.KeyEvent.VK_LEFT;
        int right = useWASD ? java.awt.event.KeyEvent.VK_D : java.awt.event.KeyEvent.VK_RIGHT;

        boolean upPressed = input.isKeyDown(up);
        boolean downPressed = input.isKeyDown(down);
        boolean leftPressed = input.isKeyDown(left);
        boolean rightPressed = input.isKeyDown(right);

        // Diagonal and straight movement
        if (upPressed) dy -= 1;
        if (downPressed) dy += 1;
        if (leftPressed) dx -= 1;
        if (rightPressed) dx += 1;

        // Determine direction for animation (prioritize last pressed, or horizontal for diagonal)
        if (dx != 0 && dy != 0) {
            // Diagonal: use left/right sprite for animation
            newDir = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (dx != 0) {
            newDir = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (dy != 0) {
            newDir = (dy > 0) ? Direction.DOWN : Direction.UP;
        }

        walkAnimations[newDir.ordinal()].update(deltaTime);

        // Normalize diagonal speed so it's not faster
        double moveLen = Math.sqrt(dx * dx + dy * dy);
        int ndx = 0, ndy = 0;
        if (moveLen > 0) {
            ndx = (int)Math.round(dx / moveLen);
            ndy = (int)Math.round(dy / moveLen);
        }

        if ((dx != 0 || dy != 0) && moveCooldown <= 0f) {
            int nx = Math.round(x) + ndx;
            int ny = Math.round(y) + ndy;
            if (phaseMode || map.canMoveTo(nx, ny, newDir)) {
                x = nx;
                y = ny;
                dir = newDir;
                map.visit(getX(), getY());
                map.checkCollectables(getX(), getY());
            } else {
                dir = newDir;
            }
            moveCooldown = MOVE_DELAY;
        }
    }

    @Override
    public void render(Graphics g, int offsetX, int offsetY, int tileSize) {
        Animation anim = walkAnimations[dir.ordinal()];
        g.drawImage(anim.getCurrentFrame(), offsetX, offsetY, tileSize, tileSize, null);
    }
}
