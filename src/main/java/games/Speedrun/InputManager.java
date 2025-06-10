package games.Speedrun;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private final Set<Integer> pressedKeys = new HashSet<>();

    public void keyPressed(int keyCode) {
        pressedKeys.add(keyCode);
    }

    public void keyReleased(int keyCode) {
        pressedKeys.remove(keyCode);
    }

    public boolean isKeyDown(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void clear() {
        pressedKeys.clear();
    }
}
