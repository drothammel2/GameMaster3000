import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WindowTest {
    @Test
    public void testGameSelectionListenerInterfaceExists() {
        assertNotNull(Window.GameSelectionListener.class);
    }
}
