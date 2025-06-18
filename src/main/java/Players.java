import java.io.*;
import java.util.*;

public class Players {
    private static String playersDir = "src/main/java/players";
    private static Player currentPlayer;

    public static class Player {
        public final String name;
        private final File file;
        private final Properties props = new Properties();

        public Player(String name) {
            this.name = name;
            this.file = new File(playersDir, name + ".txt");
            // Datei direkt beim Erstellen anlegen, falls sie nicht existiert
            try {
                file.getParentFile().mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException ignored) {}
            load();
        }

        private void load() {
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                } catch (IOException ignored) {}
            }
        }

        public void save() {
            try {
                file.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    props.store(fos, "Player data for " + name);
                }
            } catch (IOException ignored) {}
        }

        public String getHighscore(String game) {
            return props.getProperty("highscore." + game, "");
        }

        public void setHighscore(String game, String value) {
            props.setProperty("highscore." + game, value);
            save();
        }
    }

    public static List<String> getAllPlayerNames() {
        File dir = new File(playersDir);
        if (!dir.exists()) return new ArrayList<>();
        String[] files = dir.list((d, n) -> n.endsWith(".txt"));
        List<String> names = new ArrayList<>();
        if (files != null) {
            for (String f : files) {
                names.add(f.substring(0, f.length() - 4));
            }
        }
        return names;
    }

    public static void setCurrentPlayer(String name) {
        currentPlayer = new Player(name);
        // Datei direkt anlegen, falls sie noch nicht existiert
        currentPlayer.save();
    }

    public static Player getCurrentPlayer() {
        return currentPlayer;
    }
}
