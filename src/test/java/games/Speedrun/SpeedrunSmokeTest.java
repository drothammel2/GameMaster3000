/* 
package games.Speedrun;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class SpeedrunSmokeTest {

    @Test
    public void testPanelConstructionAndPainting() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            SpeedrunPanel panel = new SpeedrunPanel();
            frame.add(panel);
            frame.setSize(800, 600);
            frame.setVisible(true);

            // Try to paint the panel (should not throw)
            try {
                panel.paint(panel.getGraphics());
            } catch (Exception e) {
                fail("Panel paint threw exception: " + e.getMessage());
            }

            frame.dispose();
        });
    }

    

    @Test
    public void testGameMapAndPlayerCreation() {
        try {
            // Use a larger map to avoid out-of-bounds in house placement
            int size = 40;
            GameMap map = new GameMap(size, size, 32);
            InputManager input = new InputManager();
            int px = map.getCols() / 2;
            int py = map.getRows() / 2;
            Player player = new Player(map, input, px, py);
            assertNotNull(map, "GameMap should not be null");
            assertNotNull(player, "Player should not be null");
            // Check player is on a walkable tile
            assertTrue(map.canMoveTo(player.getX(), player.getY(), Player.Direction.DOWN), "Player should be on a walkable tile");
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception during GameMap/Player creation: " + t.getMessage());
        }
    }
}

*/