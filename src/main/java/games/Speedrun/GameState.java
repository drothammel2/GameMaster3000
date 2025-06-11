package games.Speedrun;

import java.awt.*;
import java.awt.event.KeyEvent;

public interface GameState {
    void update();
    void render(Graphics g, int width, int height);
    void keyPressed(KeyEvent e);
    void keyReleased(KeyEvent e);
}
