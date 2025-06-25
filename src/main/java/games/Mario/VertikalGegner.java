package games.Mario;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

public class VertikalGegner {
    private int y;
    private final int startY;
    private final int minY;
    private final int maxY;
    private int speed;
    private final Image image;

    public VertikalGegner(int startY, int speed) {
        this.startY = startY;
        this.minY = startY - 5 * 50; // 5 Blöcke nach oben
        this.maxY = startY + 5 * 50; // 5 Blöcke nach unten
        this.y = startY;
        this.speed = speed;
        this.image = new ImageIcon(getClass().getResource("/games/Mario/GUMBA.png")).getImage();
    }

    public void update() {
        y += speed;
        if (y >= maxY || y <= minY) {
            speed = -speed; // Richtung umkehren
        }
    }

    public void draw(Graphics g, int x, int offsetY) {
        g.drawImage(image, x, y - offsetY, 50, 50, null); // Zeichne GUMBA.png
    }

    public int getY() {
        return y;
    }
}
