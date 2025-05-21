import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

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
    void testMarioHasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Mario.Mario").getMethod("start"));
    }

    @Test
    void testSnakeHasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Snake.Snake").getMethod("start"));
    }

    @Test
    void testTetrisHasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Tetris.Tetris").getMethod("start"));
    }

    @Test
    void testDummy1HasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Dummy1.Dummy1").getMethod("start"));
    }

    @Test
    void testDummy2HasStartMethod() throws Exception {
        assertNotNull(Class.forName("games.Dummy2.Dummy2").getMethod("start"));
    }

    @Test
    void testWindowGameSelectionListenerExists() {
        assertDoesNotThrow(() -> Class.forName("Window$GameSelectionListener"));
    }
}
