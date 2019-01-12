package model;

import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import view.GameView;

/**
 * A Timer that counts from 3 to 0 and unfreezes a GameView upon reaching zero.
 * 
 * @author Juri Dispan
 *
 */
public class CountdownTimer implements Drawable {

    private long startTime;
    private long endTime;
    private boolean fired = false;
    /**
     * Position of the CountdownTimer on the canvas
     */
    private final double posX = GameView.WORLD_WIDTH / 2, posY = GameView.WORLD_HEIGHT / 2;

    /**
     * Constructs a CountdownTimer.
     * 
     * @param gv The GameView to unfreeze after reaching zero.
     */
    public CountdownTimer() {
        reset();
    }

    /**
     * Start the countdown and add this CountdownTimer the the objects to be drawn on the canvas.
     * 
     * @param toDraw The List which gets drawn on the canvas.
     */
    public void start(List<Drawable> toDraw) {
        toDraw.add(this);
        reset();
        start();
    }

    /**
     * Start the countdown.
     */
    public void start() {
        startTime = System.currentTimeMillis();
        endTime = startTime + 3_000;
    }

    /**
     * Pause the countdown.
     */
    public void stop() {
        fired = true;
        endTime = 0;
    }

    /**
     * Reset the countdown.
     */
    public void reset() {
        startTime = 0;
        endTime = 0;
        fired = false;
    }

    /**
     * 
     * @return The current system time in miliseconds.
     */
    public long getTimeLong() {
        return System.currentTimeMillis();
    }

    /**
     * unfreezes the given GameView when the countdown has reached zero and this timer has not yet
     * unfrozen a GameView after the last time reset() has been called.
     * 
     * @param gv
     */
    public void unfreezeOnZero(GameView gv) {
        if (getTimeLong() > endTime) {
            if (!fired) {
                gv.unfreeze();
                fired = true;
            }
            return;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        long ctime = getTimeLong();

        // Don't draw anything if the countdown reached zero.
        if (ctime > endTime) {
            return;
        }

        // Draw the countdown with an animation.
        gc.setFont(new Font("Microsoft Yi Baiti",
                        (1 - (((endTime - ctime) / 1_000.0) - ((endTime - ctime) / 1_000))) * 100));
        gc.fillText(String.format("%d", (endTime - ctime) / 1_000 + 1), posX * 10, posY * 10);
    }

    /**
     * 
     * @return false if and only if the contdown has reached zero.
     */
    public boolean isRunning() {
        return getTimeLong() < endTime;
    }

    /**
     * has this CountdownTimer unfrozen a GameView after the last time reset() has been called?
     * 
     * @return
     */
    public boolean hasFired() {
        return fired;
    }

}
