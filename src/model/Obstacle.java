package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import view.GameView;

/**
 * An obstacle a car can collide with.
 * 
 * @author Juri Dispan
 *
 */
public class Obstacle implements Drawable {

    /**
     * In metres.
     */
    public static final double MAX_DIAMETER = 1.8;
    private double x, y, radius, rot;
    private Image image;

    /**
     * Constructs an obstacle.
     * 
     * @param posX X position of obstacle in metres.
     * @param posY Y position of obstacle in metres.
     * @param radius Radius of obstacle in metres.
     * @param rot Rotation of obstacle in radians.
     */
    public Obstacle(double posX, double posY, double radius, double rot) {
        this.x = posX;
        this.y = posY;
        this.radius = radius;
        this.rot = rot;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (image == null) {
            initImage();
        }
        gc.save();
        Rotate tRot = new Rotate(rot * 180 / Math.PI, x * 10, y * 10);
        gc.setTransform(tRot.getMxx(), tRot.getMyx(), tRot.getMxy(), tRot.getMyy(), tRot.getTx(),
                        tRot.getTy());
        gc.drawImage(image, (x - radius) * 10, (y - radius) * 10, (2 * radius) * 10,
                        (2 * radius) * 10);
        gc.restore();
    }

    /**
     * Init image.
     */
    private void initImage() {
        this.image = new Image("obstacle.png");
    }

    /**
     * Distance between the centres of this and an other obstacle.
     * 
     * @param other
     * @return Distance between the centres of this and an other obstacle.
     */
    private double distanceTo(Obstacle other) {
        return Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
    }

    /**
     * 
     * @param other
     * @return The passable space between this obstacle and an other obstacle.
     */
    public double spaceBetween(Obstacle other) {
        return distanceTo(other) - getRadius() - other.getRadius();
    }

    /**
     * Generate the obstacles, so that all obstacles are on track and the car can still complete a
     * round only driving on track.
     * 
     * @param amount How many obstacles are to be generated
     * @param the width of the car
     */
    public static Collection<Obstacle> generateObs(int amount, double minSpacing) {
        List<Obstacle> obstacles = new ArrayList<>(amount);
        while (amount-- > 0) {
            // Generate an obstacle thats hopefully on track
            Obstacle cObs = new Obstacle(
                            randomBetween((GameView.WORLD_WIDTH / 2) - (GameView.TRACK_WIDTH / 2),
                                            (GameView.WORLD_WIDTH / 2)
                                                            + (GameView.TRACK_WIDTH / 2)),
                            randomBetween((GameView.WORLD_HEIGHT / 2) - (GameView.TRACK_HEIGHT / 2),
                                            (GameView.WORLD_HEIGHT / 2)
                                                            + (GameView.TRACK_HEIGHT / 2)),
                            randomBetween(Obstacle.MAX_DIAMETER * 0.4, Obstacle.MAX_DIAMETER),
                            randomBetween(0, 2 * Math.PI));

            // Dont add the generated obstacle if its not on track
            if (!Car.getTrackBitMap()[(int) (cObs.getX() * 10)][(int) (cObs.getY() * 10)]) {
                amount++;
                continue;
            }

            // Dont add generated obstacle if its close to the start/end line or
            // checkpoint
            if (cObs.getX() >= GameView.WORLD_WIDTH / 2 - minSpacing - 1.5
                            && cObs.getX() <= GameView.WORLD_WIDTH / 2 + minSpacing
                                            + Obstacle.MAX_DIAMETER + 1.0) {
                amount++;
                continue;
            }

            // check if obstacle blocks path, dont add if it does
            if (obstacles.stream()
                            .anyMatch(alreadyIn -> cObs.spaceBetween(alreadyIn) <= minSpacing)) {
                amount++;
                continue;
            }

            // Add obstacle if it is fine.
            obstacles.add(cObs);
        }
        return obstacles;
    }

    /**
     * returns a random double between a and b
     * 
     * @param a lower bound
     * @param b upper bound
     * @return random double between a and b
     */
    private static double randomBetween(double a, double b) {
        return Math.random() * (b - a) + a;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @return the diameter
     */
    public double getRadius() {
        return radius;
    }

}
