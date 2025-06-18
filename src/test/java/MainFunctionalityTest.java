import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class MainFunctionalityTest {

    @Test
    void testMarioClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.Mario.Mario"));
    }

    @Test
    void testSnakeClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.Snake.Snake"));
    }

    @Test
    void testTetrisClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.Tetris.Tetris"));
    }

    @Test
    void testDummy1ClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.Dummy1.Dummy1"));
    }

    @Test
    void testDummy2ClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.Dummy2.Dummy2"));
    }

    @Test
    void testMonkeyTypeClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.MonkeyType.MonkeyType"));
    }

    
    @Test
    void testSpeedrunClassExists() {
        assertDoesNotThrow(() -> Class.forName("games.Speedrun.Speedrun"));
    }

    @Test
    void testMarioHasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Mario.Mario").getMethod("start"));
    }

    @Test
    void testSpeedrunHasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Speedrun.Speedrun").getMethod("start"));
    }

    @Test
    void testWindowGameSelectionListenerExists() {
        assertDoesNotThrow(() -> Class.forName("Window$GameSelectionListener"));
    }
}
