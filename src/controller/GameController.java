package controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import model.Car;
import view.GameView;

/**
 * This class is responsible for coordinating calculations and handling user input.
 * 
 * @author Juri Dispan
 *
 */
public class GameController {

    private GameView gameView;
    private Scene scene;
    private Car car;
    private boolean carAcc, carPlsBreak, carTurnLeft, carTurnRight, freeze;
    private boolean hold = false;
    private boolean inGame = false;
    private SoundController audioController;

    /**
     * A GameController is responsible for coordinating calculations and handling user input.
     * 
     * @param gameView
     * @param audioController
     */
    public GameController(GameView gameView, SoundController audioController) {
        this.gameView = gameView;
        this.scene = gameView.getScene();
        this.car = gameView.getCar();
        this.audioController = audioController;
        freeze = false;
        setUpInputHandler();
    }

    /**
     * Update all dependencies and check for the end of the game.
     *
     * @param timeDelta the time passed since last frame
     */
    public void updateContinuously(double timeDelta) {
        if (inGame) {
            car.stepForward(timeDelta, carAcc, carPlsBreak, carTurnLeft, carTurnRight,
                            gameView.getObstacles());
            checkEndConditions(gameView);
        } else {
            gameView.getCountDownTimer().unfreezeOnZero(gameView);
        }


    }

    public void checkEndConditions(GameView gameView) {
        // game ends if car is destroyed
        if (car.isDestroyed()) {
            gameView.showLostOverlay();
            gameView.freeze();
            return;
        }

        // game ends if end line and checkpoint have been passed
        car.checkLines(gameView.getRoundTimer());
        if (car.hasPassedEndLine()) {
            gameView.showFinishOverlay();
            gameView.freeze();
        }
    }

    /**
     * Handle input differently depending on weather a key has been pressed or released.
     */
    private void setUpInputHandler() {
        scene.setOnKeyPressed(e -> handleKeyCommands(e, true));
        scene.setOnKeyReleased(e -> handleKeyCommands(e, false));
    }

    /**
     * Handle all user input over keyboard.
     */
    private void handleKeyCommands(KeyEvent e, boolean pressed) {
        switch (e.getCode()) {
            case W:
            case UP:
                carAcc = pressed;
                break;
            case A:
            case LEFT:
                carTurnLeft = pressed;
                break;
            case S:
            case DOWN:
                carPlsBreak = pressed;
                break;
            case D:
            case RIGHT:
                carTurnRight = pressed;
                break;
            case P:
                // hold avoids that the game toggles between paused and not paused
                // while the key is pressed
                if (pressed && !hold) {
                    hold = true;
                    freeze = !freeze;
                    gameView.setFreeze(freeze);
                    if (!freeze) {
                        gameView.getRoundTimer().unfreeze();
                    }
                }
                if (!pressed) {
                    hold = false;
                }
                break;
            case R:
                if (pressed && !hold) {
                    gameView.toForegroundAndReset();
                    freeze = true;
                    hold = true;
                }
                if (!pressed) {
                    hold = false;
                }

                break;
            case ESCAPE:
                audioController.cancel();
                Platform.exit();
                break;
            default:
                break;
        }
    }

    /**
     * called when the game is currently being played.
     */
    public void inGame() {
        this.inGame = true;
    }

    /**
     * called in a situation where input concerning the racecar should be dismissed.
     */
    public void notInGame() {
        this.inGame = false;
    }

    public boolean isInGame() {
        return inGame;
    }
}
