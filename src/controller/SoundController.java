package controller;

import javafx.concurrent.Task;
import javafx.scene.media.AudioClip;
import model.Car;

/**
 * Task responsible for playing engine noises.
 * 
 * @author Juri Dispan
 *
 */
public class SoundController extends Task<Void> {
    private Car car;

    public SoundController(Car car) {
        this.car = car;
    }

    @Override
    protected Void call() throws Exception {
        AudioClip engineNoise = null;
        try {
            // "Professionally" recorded engine noise of my motorcycle
            engineNoise = new AudioClip(getClass().getResource("/engine.wav").toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Play sound while application is running and while the car is moving
        // and not frozen.
        while (!isCancelled()) {
            if (engineNoise.isPlaying()) {
                if (!car.isMoving() || car.isFrozen()) {
                    engineNoise.stop();
                }
            } else {
                if (car.isMoving() && !car.isFrozen()) {
                    engineNoise.play();
                }
            }
        }
        return null;
    }

}
