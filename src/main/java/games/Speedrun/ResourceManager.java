package games.Speedrun;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static final ResourceManager instance = new ResourceManager();
    private final Map<String, Image> imageCache = new HashMap<>();

    private ResourceManager() {}

    public static ResourceManager get() {
        return instance;
    }

    public Image getImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) return null;
        Image img = new ImageIcon(url).getImage();
        imageCache.put(path, img);
        return img;
    }
}
