package games.Speedrun;

import java.awt.*;

public abstract class Entity {
    protected float x, y;

    public Entity(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(float deltaTime);
    public abstract void render(Graphics g, int offsetX, int offsetY, int tileSize);

    public int getX() { return Math.round(x); }
    public int getY() { return Math.round(y); }
}
