package application;

import controller.GameController;
import controller.SoundController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.Car;
import view.GameView;

/**
 * Main class of the racing game. Sets up the GUI and logic elements.
 * 
 * @author Juri Dispan
 *
 */
public class RGMain extends Application {

    private long oldTime;
    private GameView gameView;
    private SoundController audioController;

    public GameView getGameView() {
        return this.gameView;
    }

    public SoundController getAudioController() {
        return this.audioController;
    }


    @Override
    public void start(Stage stage) throws Exception {
        // Create the instances for the game
        Car.initTrack();
        gameView = new GameView(stage);
        audioController = new SoundController(gameView.getCar());
        new Thread(audioController).start();
        stage.setOnCloseRequest(e -> audioController.cancel());
        GameController gameController = new GameController(gameView, audioController);
        GraphicsContext gc = gameView.getGraphicsContext();

        gameView.setController(gameController);
        gameView.toForegroundAndReset();
        // init toDraw List
        gameView.getToDraw();
        // freeze the game and show initial info screen
        gameView.showMenu();

        // For determining the current framerate.
        // Start the application with argument "-fps" for framerate output.
        // Framerate will be displayed in the GUI and in stdout.
        // I had about 140 fps on average.
        long[] benchmark = new long[3];
        // benchmark[0] = time since last fps update has been printed
        // benchmark[1] = how many frames have been rendered since the last fps
        // printing
        // benchmark[2] = value of last fps printing

        boolean displayFPS;
        if (getParameters() != null) {
            displayFPS = getParameters().getRaw().contains("--fps");
        } else {
            displayFPS = true;
        }


        /*
         * Start the gameloop. It is executed every frame, the long now is the current timestamp
         */
        AnimationTimer anim = new AnimationTimer() {
            @Override
            public void handle(long now) {

                /*
                 * timeDelta calculates the time between 2 frames. It compares the last time with
                 * the current time (now) and is divided by 1000000000.0 to get the time in seconds
                 */
                double timeDelta = (now - oldTime) / 1_000_000_000.0;

                // if 1 sec has passed, update fps display
                if ((now - benchmark[0]) / 1_000_000_000.0 >= 1) {
                    if (displayFPS) {
                        System.out.println("Current framerate: " + benchmark[1] + " fps.");
                    }
                    benchmark[0] = now;
                    benchmark[2] = benchmark[1];
                    benchmark[1] = 0;

                } else {
                    benchmark[1]++;
                }

                /*
                 * Sets the oldTime to now, so the next loop can take the difference
                 */
                oldTime = now;

                // Update Canvas
                // First, draw the track image in order to reset the canvas
                gc.drawImage(GameView.TRACK, 0, 0);
                // refresh "toDraw" list in case the obstacles were randomized
                gameView.refreshToDraw();
                // draw every Drawable
                gameView.getToDraw().forEach(drawable -> drawable.draw(gc));
                if (displayFPS) {
                    gc.setFont(Font.font("monospace", 20));
                    gc.fillText(benchmark[2] + " fps", 10, 25);
                }

                /*
                 * Use the controller to update all dependencies
                 */
                gameController.updateContinuously(timeDelta);

            }
        };
        anim.start();

        stage.show();
    }

    /**
     * Launches the Application (calls start overriden start method)
     * 
     * @param args "--fps" if you want framerate output
     */
    public static void main(String[] args) {
        launch(args);
    }
}
