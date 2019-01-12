package view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import controller.GameController;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Car;
import model.CountdownTimer;
import model.Drawable;
import model.Freezeable;
import model.Obstacle;
import model.RoundTimer;

/**
 * Contains the view a player has while actually playing the game.
 * 
 * @author Juri Dispan
 *
 */
public class GameView implements Freezeable {

    public static final double WORLD_WIDTH = 130, WORLD_HEIGHT = 80;
    public static final double TRACK_WIDTH = 110, TRACK_HEIGHT = 70;
    public static final int OBSTACLES_AMOUNT = 10;

    // Three Tracks available, try them out!
    // track_grass, track_dirt, and track_gravel will work!!
    public static Image TRACK;
    protected boolean hasReset;

    protected Scene scene;

    protected StackPane rootPane;
    protected ImageView lostImage;
    protected ImageView wonImage;
    protected ImageView pauseImage;
    protected ImageView menuImage;
    protected Text roundTime;

    protected Canvas canvas;

    protected Car car;
    protected GameController contr;
    protected RoundTimer timer;
    protected CountdownTimer cntdwn;

    /**
     * Obstacles on the track.
     */
    protected Collection<Obstacle> obstacles;

    /**
     * List of all elements that need to be drawn on the canvas (excluding the background image).
     */
    protected List<Drawable> toDraw;

    /**
     * A GameView is the view a player sees while playing the game.
     *
     * @param stage the primary stage
     */
    public GameView(Stage stage) {
        car = new Car();
        timer = new RoundTimer();
        cntdwn = new CountdownTimer();
        hasReset = false;
        obstacles = Obstacle.generateObs(OBSTACLES_AMOUNT, car.getWidth());
        rootPane = new StackPane();
        scene = new Scene(rootPane);
        canvas = new Canvas(WORLD_WIDTH * 10, WORLD_HEIGHT * 10);
        rootPane.getChildren().add(canvas);
        lostImage = new ImageView(new Image("game_over.png"));
        wonImage = new ImageView(new Image("round_finished.png"));
        pauseImage = new ImageView(new Image("pause.png"));
        menuImage = new ImageView(new Image("menu.png"));
        roundTime = new Text();
        roundTime.setFont(Font.font("Microsoft Yi Baiti", 38));
        TRACK = new Image("track_gravel.png");
        stage.setScene(scene);
        stage.setTitle("Rennspiel");
        stage.setResizable(false);
        stage.sizeToScene();
    }

    /**
     * Reset the game world and put it in foreground in the application.
     */
    public void toForegroundAndReset() {
        car.reset();
        timer.reset();
        obstacles = Obstacle.generateObs(OBSTACLES_AMOUNT, car.getWidth());
        hasReset = true;
        rootPane.getChildren().removeAll(lostImage, wonImage, roundTime, menuImage);
    }

    /**
     * Show the initial help dialog.
     */
    public void showMenu() {
        freeze();
        cntdwn.stop();
        rootPane.getChildren().add(menuImage);
    }



    /**
     * Add all elements that need to be drawn to the toDraw list. A element with a lower index in
     * the list will be drawn below all elements with a higher index.
     */
    public void refreshToDraw() {
        if (hasReset()) {
            // System.out.println("ref");
            toDraw.clear();
            toDraw.add(getCar());
            toDraw.addAll(getObstacles());
            toDraw.add(getRoundTimer());
            cntdwn.start(toDraw);
        }
    }

    /**
     * Pauses the gameworld if b is true, unpauses it if b is false. Responsible for displaying and
     * dismissing the pause dialog.
     */
    public void setFreeze(boolean b) {
        if (b) {
            if (cntdwn.isRunning()) {
                return;
            }
            freeze();
            if (!rootPane.getChildren().contains(pauseImage)
                            && !rootPane.getChildren().contains(wonImage)
                            && !rootPane.getChildren().contains(lostImage)) {
                rootPane.getChildren().add(pauseImage);
            }
        } else {
            if (cntdwn.isRunning()) {
                return;
            }
            rootPane.getChildren().remove(pauseImage);
            timer.unfreeze();
            unfreeze();
        }
    }

    /**
     * This gets called when the car is destroyed. It displays the game over dialog.
     */
    public void showLostOverlay() {
        if (!rootPane.getChildren().contains(lostImage)) {
            rootPane.getChildren().add(lostImage);
        }

    }

    /**
     * This gets called when a round has been finished successfully. It displays the won dialog.
     */
    public void showFinishOverlay() {
        if (!rootPane.getChildren().contains(wonImage)) {
            rootPane.getChildren().add(wonImage);
            roundTime.setText(timer.toString());

            rootPane.getChildren().add(roundTime);
        }

    }

    @Override
    public void freeze() {
        car.freeze();
        timer.freeze();
        contr.notInGame();
    }

    @Override
    public void unfreeze() {
        car.unfreeze();
        contr.inGame();
    }

    // Getters and setters after this

    /**
     * 
     * @return true if and only if the GameView has been reset in the last frame.
     */
    public boolean hasReset() {
        if (hasReset) {
            hasReset = false;
            return true;
        }
        return false;
    }

    /**
     * 
     * @return List with all elements that want to be drawn on canvas.
     */
    public List<Drawable> getToDraw() {
        if (toDraw == null) {
            toDraw = new ArrayList<>();
            hasReset = true;
            refreshToDraw();
        }
        return toDraw;
    }

    /**
     * 
     * @return The Timer used to measure roundtimes in this game.
     */

    public RoundTimer getRoundTimer() {
        return timer;
    }

    /**
     * 
     * @return Obstacles on the track.
     */
    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * @return the racecar.
     */
    public Car getCar() {
        return car;
    }

    /**
     * 
     * @return The scene used.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * 
     * @return the GraphicsContext needed to draw on the GameView's canvas.
     */
    public GraphicsContext getGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    /**
     * Set the GameController
     * 
     * @param contr The GameController to be used.
     */
    public void setController(GameController contr) {
        this.contr = contr;
    }

    public CountdownTimer getCountDownTimer() {
        return cntdwn;
    }

}
