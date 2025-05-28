package games.Mario.Items;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

import games.Mario.Engine;

public class Feuerblume implements Item {
    public static final int SIZE = 50;
    private final int x, y;
    private final Image image;

    public Feuerblume(int x, int y) {
        this.x = x;
        this.y = y;
        image = new ImageIcon(getClass().getResource("/games/Mario/Items/Feuerblume.png"))
                   .getImage()
                   .getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);
    }

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }

    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

    // Bild für relative Zeichnung
    public Image getImage() {
        return image;
    }

    @Override
    public void applyEffect(Engine engine) {
        engine.applyFireMario();
    }
}
