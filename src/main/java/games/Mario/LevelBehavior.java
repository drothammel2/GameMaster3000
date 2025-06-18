package games.Mario;

import java.awt.Graphics;
import java.util.List;

public interface LevelBehavior {
    int[][] getPlatforms();
    int[][] getHoles();
    int[][] getBlackBlocks();
    int[][] getGoalBlocks(); // Neu: Luftblöcke für Kopf‐Interaktion
    void spawnItems(int panelWidth, int panelHeight);
    void drawItems(Graphics g);
    void updateItems(Engine engine, int panelWidth, int panelHeight);
    void updateGegner(); // Methode zum Aktualisieren der Gegner
    void drawGegner(Graphics g, int offsetX, int groundY); // Methode zum Zeichnen der Gegner
    default void spawnItemAt(int worldX, int worldY) { /* default: nichts */ }
    List<HorizontalGegner> getHorizontalGegner(); // Methode zum Abrufen der horizontalen Gegner
    List<VertikalGegner> getVertikalGegner();     // Methode zum Abrufen der vertikalen Gegner
}
