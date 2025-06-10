package games.Speedrun;

import javax.swing.*;

public class GameLoop {
    private final Timer timer;

    public GameLoop(Runnable updateAndRender, int fps) {
        timer = new Timer(1000 / fps, e -> updateAndRender.run());
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
