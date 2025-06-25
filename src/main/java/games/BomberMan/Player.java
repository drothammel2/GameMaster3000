package games.BomberMan;

public class Player {
    private int x, y;
    private int maxBombs = 1;
    private int explosionRange = 2;
    private boolean speedBoost = false;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
        System.out.println("Spieler bewegt sich zu Position: (" + x + ", " + y + ")");
    }

    public void placeBomb() {
        System.out.println("Bombe gelegt bei: (" + x + ", " + y + ")");
        // Bombenlogik hier implementieren.
    }

    public void increaseMaxBombs() {
        maxBombs++;
        System.out.println("Maximale Bombenanzahl erhöht: " + maxBombs);
    }

    public void increaseExplosionRange() {
        explosionRange++;
        System.out.println("Explosionsreichweite erhöht: " + explosionRange);
    }

    public void activateSpeedBoost() {
        speedBoost = true;
        System.out.println("Geschwindigkeitsboost aktiviert!");
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public int getExplosionRange() {
        return explosionRange;
    }

    public boolean hasSpeedBoost() {
        return speedBoost;
    }
}
