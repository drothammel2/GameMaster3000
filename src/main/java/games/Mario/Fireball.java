package games.Mario;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

public class Fireball {
    private int x, y, vx, vy;               // neu: vertikale Geschwindigkeit
    private final Image image;
    public static final int SIZE = 20;
    private final int startX;
    private static final int MAX_DISTANCE = 500;
    private static final int GRAVITY = 1;   // neu: Gravitation

    public Fireball(int x, int y, int vx, int vy) { // neu: Parameter vy
        this.startX = x;
        this.x = x; this.y = y; 
        this.vx = vx; this.vy = vy;
        image = new ImageIcon(getClass()
            .getResource("/games/Mario/fireball.png"))
            .getImage()
            .getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);
    }

    public boolean exceededDistance() {
        return Math.abs(x - startX) > MAX_DISTANCE;
    }

    // angepasst: Bewegung mit Gravitation
    public void update() {
        x += vx;
        vy += GRAVITY;
        y += vy;
    }

    public void draw(Graphics g, int offsetX) {
        g.drawImage(image, x - offsetX, y, null);
    }

    public boolean isOffscreen(int offsetX, int panelWidth) {
        int sx = x - offsetX;
        return sx < -SIZE || sx > panelWidth + SIZE;
    }

    // Getter für Y-Koordinate
    public int getY() {
        return y;
    }

    // neu: Abprallen an Boden (mit Dämpfung)
    public void bounce(int groundY) {
        y = groundY - SIZE;    // am Boden positionieren
        vy = -vy / 2;          // Abprallkraft halbieren
    }
}
