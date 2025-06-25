package games.Mario;

import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EngineTest {
    private Engine engine;
    private JPanel panel;

    @BeforeEach
    public void setUp() {
        panel = new JPanel();
        engine = new Engine(panel);
    }

    @Test
    public void testMovementResetOnRestart() {
        // Simuliere Bewegung nach rechts
        engine.handleKey(new java.awt.event.KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, ' '), true);
        assertTrue(engine.isMovingRight(), "Mario sollte sich nach rechts bewegen.");

        // Simuliere Neustart
        engine.restartGame(); // Neustart direkt aufrufen
        assertFalse(engine.isMovingRight(), "Mario sollte nach dem Neustart nicht mehr automatisch nach rechts laufen.");
        assertFalse(engine.isMovingLeft(), "Mario sollte nach dem Neustart nicht mehr automatisch nach links laufen.");
    }

    @Test
    public void testFireModeActivation() {
        // Aktiviere Fire-Modus
        engine.applyFireMario();
        assertTrue(engine.isFireMode(), "Fire-Modus sollte aktiviert sein.");
    }

    @Test
    public void testPlayerJump() {
        // Simuliere Sprung
        engine.handleKey(new java.awt.event.KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' '), true);
        assertFalse(engine.onGround, "Mario sollte nicht mehr auf dem Boden sein.");
        assertTrue(engine.playerVelocityY < 0, "Mario sollte nach oben springen.");
    }

    @Test
    public void testRestartResetsFireballs() {
        // Simuliere das Spawnen eines Fireballs
        engine.applyFireMario();
        engine.handleKey(new java.awt.event.KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_Q, ' '), true);
        assertEquals(1, engine.getFireballs().size(), "Es sollte ein Fireball existieren.");

        // Simuliere Neustart
        engine.restartGame();
        assertEquals(0, engine.getFireballs().size(), "Fireballs sollten nach dem Neustart entfernt werden.");
    }

    @Test
    public void testFacingDirection() {
        // Simuliere Bewegung nach links
        engine.handleKey(new java.awt.event.KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, ' '), true);
        assertTrue(engine.facingLeft, "Mario sollte nach links schauen.");

        // Simuliere Bewegung nach rechts
        engine.handleKey(new java.awt.event.KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, ' '), true);
        assertFalse(engine.facingLeft, "Mario sollte nach rechts schauen.");
    }
}
