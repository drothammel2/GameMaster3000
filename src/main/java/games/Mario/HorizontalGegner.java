package games.Mario;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

public class HorizontalGegner {
    private int x;
    private final int startX;
    private final int minX;
    private final int maxX;
    private int speed;
    private final Image image;

    public HorizontalGegner(int startX, int speed) {
        this.startX = startX;
        this.minX = startX - 5 * 50; // 5 Blöcke nach links
        this.maxX = startX + 5 * 50; // 5 Blöcke nach rechts
        this.x = startX;
        this.speed = speed;
        this.image = new ImageIcon(getClass().getResource("/games/Mario/GUMBA.png")).getImage();
    }

    public void update() {
        x += speed;
        if (x >= maxX || x <= minX) {
            speed = -speed; // Richtung umkehren
        }
    }

    public void draw(Graphics g, int offsetX, int y) {
        g.drawImage(image, x - offsetX, y, 50, 50, null); // Zeichne GUMBA.png
    }

    public int getX() {
        return x;
    }
}
