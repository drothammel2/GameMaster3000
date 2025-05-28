package games.Mario;

import java.awt.Graphics;

public interface LevelBehavior {
    int[][] getPlatforms();
    int[][] getHoles();
    void spawnItems(int panelWidth, int panelHeight);
    void drawItems(Graphics g);
    void updateItems(Engine engine, int panelWidth, int panelHeight);
}
