package games.Mario;

import java.net.URL;

import javax.swing.ImageIcon;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class ResourceTest {

    @Test
    public void testMarioImageResourceExists() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/mario.png");
        assertNotNull(url, "Die Ressource games/Mario/mario.png sollte im Klassenpfad liegen");
    }

    @Test
    public void testMarioImageIconLoads() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/mario.png");
        assertNotNull(url, "Resource darf nicht null sein");
        ImageIcon icon = new ImageIcon(url);
        assertNotNull(icon.getImage(), "Das geladene Bild darf nicht null sein");
    }

    @Test
    public void testPlayerImageResourceExists() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/Player.png");
        assertNotNull(url, "Die Ressource games/Mario/Player.png sollte im Klassenpfad liegen");
    }

    @Test
    public void testPlayerImageIconLoads() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/Player.png");
        assertNotNull(url, "Resource games/Mario/Player.png darf nicht null sein");
        ImageIcon icon = new ImageIcon(url);
        assertNotNull(icon.getImage(), "Das geladene Player-Bild darf nicht null sein");
    }

    @Test
    public void testFeuerblumeImageResourceExists() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/Items/Feuerblume.png");
        assertNotNull(url, "Die Ressource games/Mario/Items/Feuerblume.png sollte im Klassenpfad liegen");
    }

    @Test
    public void testFeuerblumeImageIconLoads() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/Items/Feuerblume.png");
        assertNotNull(url, "Resource games/Mario/Items/Feuerblume.png darf nicht null sein");
        ImageIcon icon = new ImageIcon(url);
        assertNotNull(icon.getImage(), "Das geladene Feuerblume-Bild darf nicht null sein");
    }

    @Test
    public void testFeuermarioImageResourceExists() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/Feuermario.png");
        assertNotNull(url, "Die Ressource games/Mario/Items/Feuermario.png sollte im Klassenpfad liegen");
    }

    @Test
    public void testFeuermarioImageIconLoads() {
        java.net.URL url = ResourceTest.class.getClassLoader()
                              .getResource("games/Mario/Feuermario.png");
        assertNotNull(url, "Resource games/Mario/Items/Feuermario.png darf nicht null sein");
        ImageIcon icon = new ImageIcon(url);
        assertNotNull(icon.getImage(), "Das geladene Feuermario-Bild darf nicht null sein");
    }

    @Test
    public void testFireballImageResourceExists() {
        URL url = ResourceTest.class.getClassLoader()
                   .getResource("games/Mario/fireball.png");
        assertNotNull(url, "Die Ressource games/Mario/fireball.png sollte im Klassenpfad liegen");
    }

    @Test
    public void testFireballImageIconLoads() {
        URL url = ResourceTest.class.getClassLoader()
                   .getResource("games/Mario/fireball.png");
        assertNotNull(url, "Resource games/Mario/fireball.png darf nicht null sein");
        ImageIcon icon = new ImageIcon(url);
        assertNotNull(icon.getImage(), "Das geladene Fireball-Bild darf nicht null sein");
    }
}
