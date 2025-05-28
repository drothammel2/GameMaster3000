package games.Mario.Items;

import java.awt.Graphics;

import games.Mario.Engine;

public interface Item {
    int getX();
    int getY();
    void draw(Graphics g);
    void applyEffect(Engine engine);
}
