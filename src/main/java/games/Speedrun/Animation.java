package games.Speedrun;

import java.awt.Image;

public class Animation {
    private final Image[] frames;
    private final float frameDuration; // seconds per frame
    private float timer = 0f;
    private int currentFrame = 0;

    public Animation(Image[] frames, float frameDuration) {
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    public void update(float deltaTime) {
        timer += deltaTime;
        if (timer >= frameDuration) {
            timer -= frameDuration;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    public Image getCurrentFrame() {
        return frames[currentFrame];
    }

    public void reset() {
        timer = 0f;
        currentFrame = 0;
    }
}
