package games.Mario;

import java.awt.Graphics;

public interface LevelBehavior {
    int[][] getPlatforms();
    int[][] getHoles();
    int[][] getBlackBlocks(); // Neu: Luftblöcke für Kopf‐Interaktion
    void spawnItems(int panelWidth, int panelHeight);
    void drawItems(Graphics g);
    void updateItems(Engine engine, int panelWidth, int panelHeight);
    default void spawnItemAt(int worldX, int worldY) { /* default: nichts */ }
}
