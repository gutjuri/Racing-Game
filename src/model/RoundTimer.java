package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The timer used to determine the time a player needs to complete a round. It can be read while
 * running.
 * 
 * @author Juri Dispan
 *
 */
public class RoundTimer implements Drawable, Freezeable {
    private long tbuf;
    private long startTime;
    private long endTime;
    private boolean running;

    private final double posX = 10, posY = 790;

    /**
     * Constructs a not running timer.
     */
    public RoundTimer() {
        reset();
        running = false;
    }

    /**
     * Start the timer.
     */
    public void start() {
        if (running) {
            return;
        }
        startTime = System.currentTimeMillis();
        running = true;
    }

    /**
     * Pause the timer.
     */
    public void stop() {
        if (!running) {
            return;
        }
        endTime = System.currentTimeMillis();
        running = false;
        tbuf += endTime - startTime;
    }

    /**
     * Set timer to zero.
     */
    public void reset() {
        tbuf = 0;
        startTime = 0;
        endTime = 0;
        running = false;
    }

    /**
     * 
     * @return The measured time in milliseconds.
     */
    public long getTimeLong() {
        if (running) {
            return tbuf + System.currentTimeMillis() - startTime;
        }
        return tbuf;
    }

    /**
     * 
     * @return The measured time in seconds.
     */
    public double getTimeDouble() {
        return getTimeLong() / 1_000.0;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFont(new Font("Microsoft Yi Baiti", 42));
        gc.setFill(Color.BLACK);
        gc.fillText("Round Time: " + toString(), posX, posY);
    }

    @Override
    public void freeze() {
        stop();
    }

    @Override
    public void unfreeze() {
        start();
    }

    /**
     * @return The measured time in the correct format.
     */
    @Override
    public String toString() {
        long ctime = (long) getTimeDouble();
        return String.format("%02d:%02d", ctime / 60, ctime % 60);
    }

    public boolean isRunning() {
        return running;
    }

}
