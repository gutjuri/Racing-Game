package model;

import static java.lang.Math.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.imageio.ImageIO;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import view.GameView;

/**
 * Represents the Racecar. Most calculations concerning the physics etc. of the racecar take place
 * here.
 * 
 * @author Juri Dispan
 *
 */
public strictfp class Car implements Drawable, Freezeable {
    private Image imageNotDestroyed, imageDestroyed;
    private double posX, posY;
    private double vel;
    private double rot;
    private double width, height;
    private boolean destroyed;
    private boolean frozen;
    private boolean passedEndLine;
    private boolean passedCheckpoint;

    /*
     * Car-specific values:
     */
    public final double CAR_MASS = 1000 /* kg */;
    private final double C_W = 0.28, A = 2.19 /* m^2 */, RHO = 1.2041 /* kg * m^-3 */;

    public final double enginePower = 315_000.0 /* W */ , breakingPower = 500_000.0 /* W */ ,
                    turningPower = 1 /* no unit */;

    /*
     * Types of resistance
     */
    private final double RES_DIRT = 0.05, RES_TRACK = 0.015, RES_AIR = C_W * A * RHO / 2;
    /*
     * Even though all physical constants are realistic, without the "magic constant" gameplay feels
     * weird. I don't know why. You can try setting it to 1.0 (for a perfect simulation), but you
     * won't have much fun.
     */
    private final double MAGIC_CONSTANT = 30.0;

    /*
     * Starting position of car
     */
    public final double STARTING_POS_X = GameView.WORLD_WIDTH / 2 + 3.1, STARTING_POS_Y = 10.0;

    private static final double ɛ = 0.01; // very small value
    private static boolean[][] trackBitMap;

    private Rectangle2D finishLine;
    private Rectangle2D checkpoint;

    /**
     * CollisionType represents the severity of collisions between the car and an obstacle.
     * 
     * @author Juri Dispan
     *
     */
    private enum CollisionType {
        NO_COLLISION, MINIMAL_DAMAGE, CRITICAL_DAMAGE;

        // Crashes with a velocity above this will result in the destruction of
        // the car.
        private static final double CRIT_COLLISION_THRESHOLD = 50.0 /* km/h */ / 3.6;

        /**
         * Determine if a crash leads to the destruction of a vehicle or not.
         * 
         * @param vel
         * @return MINIMAL_DAMAGE if |vel| is smaller than the configured threshold, otherwise
         *         CRITICAL_DAMAGE
         */
        static CollisionType getCollisionSeverity(double vel) {
            return abs(vel) < CRIT_COLLISION_THRESHOLD ? CollisionType.MINIMAL_DAMAGE
                            : CollisionType.CRITICAL_DAMAGE;
        }
    }

    /**
     * Construct car on gameView.
     * 
     * @param gameView
     */
    public Car() {
        width = 4.2;
        height = 2.0;
        passedEndLine = false;
        passedCheckpoint = false;
        finishLine = new Rectangle2D(STARTING_POS_X - (width / 2) - 1, STARTING_POS_Y - 5.0, 1,
                        10.0);
        checkpoint = new Rectangle2D(STARTING_POS_X - (width / 2) - 1,
                        STARTING_POS_Y + GameView.TRACK_HEIGHT - 15.0, 1, 10.0);
        reset();
    }



    /**
     * Update the position of the car.
     * 
     * @param timeDelta The time since the last update.
     */
    private void updatePosition(double timeDelta) {
        posX -= cos(rot) * vel * timeDelta;
        posY -= sin(rot) * vel * timeDelta;
    }

    /**
     * Does Car collide with given obstacle?
     * 
     * @param obs Obstacle which is tested on collision
     * @param cps Collision Points of the car.
     * @return yes if and only if the model of the car interects the model of the obstacle
     */
    private boolean collidesWith(Obstacle obs, Collection<Point2D> cps) {
        return cps.stream().anyMatch(
                        cpoint -> cpoint.distance(obs.getX(), obs.getY()) <= obs.getRadius());
    }

    /**
     * Does Car collide with one of the obstacles or the map boundary, and if yes is it destroyed
     * upon impact?
     * 
     * @param obstacles
     * @return The severity of the collision (or NO_COLLISION if no collision takes place).
     */
    private CollisionType checkCollision(Collection<Obstacle> obstacles) {
        if (obstacles == null) {
            obstacles = Collections.emptyList();
        }
        Collection<Point2D> cps = getCollisionPoints();

        if (obstacles.stream().anyMatch(currObstacle -> collidesWith(currObstacle, cps))
                        || cps.stream().anyMatch(hitpoint -> outOfBounds(hitpoint))) {
            return CollisionType.getCollisionSeverity(vel);
        }
        return CollisionType.NO_COLLISION;
    }

    /**
     * Is hitpoint outside the level?
     * 
     * @param hitpoint
     * @return true if and only if hitpoint is outsite the visible level.
     */
    private boolean outOfBounds(Point2D hitpoint) {
        return (hitpoint.getX() < 0) || (hitpoint.getX() > GameView.WORLD_WIDTH)
                        || (hitpoint.getY() < 0) || (hitpoint.getY() > GameView.WORLD_HEIGHT);

    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        Rotate tRot = new Rotate(rot * 180 / Math.PI, posX * 10, posY * 10);
        gc.setTransform(tRot.getMxx(), tRot.getMyx(), tRot.getMxy(), tRot.getMyy(), tRot.getTx(),
                        tRot.getTy());
        gc.drawImage(getImage(), (posX - (width / 2)) * 10, (posY - (height / 2)) * 10);
        gc.restore();

        // DEBUG: Show car hitpoints
        // getCollisionPoints().stream().forEach(p -> gc.fillOval(p.getX() * 10, p.getY() * 10, 3,
        // 3));
    }

    /**
     * Restore the car to its initial state.
     */
    public void reset() {
        posX = STARTING_POS_X;
        posY = STARTING_POS_Y;
        vel = 0;
        destroyed = false;
        frozen = true;
        passedCheckpoint = false;
        passedEndLine = false;
        rot = 0;
    }

    public void stepForward(double timeDelta, boolean carAcc, boolean carPlsBreak, boolean turnLeft,
                    boolean turnRight, Collection<Obstacle> obstacles) {
        if (frozen) {
            return;
        }
        applyResistance(timeDelta);
        acceptInput(carAcc, carPlsBreak, turnLeft, turnRight, timeDelta);
        updatePosAndState(timeDelta, obstacles);
    }

    /**
     * Update the position and the state of the car.
     * 
     * @param timeDelta Time since last update.
     */
    private void updatePosAndState(double timeDelta, Collection<Obstacle> obstacles) {
        updatePosition(timeDelta);
        switch (checkCollision(obstacles)) {
            case CRITICAL_DAMAGE:
                destroyed = true;
            case MINIMAL_DAMAGE:
                updatePosition(-timeDelta); // dont glitch in with obstacles
                vel = 0;
            default:
                break;
        }
    }

    /**
     * Handle the commands of the player.
     * 
     * @param carAcc
     * @param carPlsBreak
     * @param carTurnLeft
     * @param carTurnRight
     * @param timeDelta
     */
    private void acceptInput(boolean carAcc, boolean carPlsBreak, boolean carTurnLeft,
                    boolean carTurnRight, double timeDelta) {
        // Turn
        double signOfTurn = signum(vel);
        if (carTurnLeft) {
            rot -= signOfTurn * sqrt(sqrt(abs(vel))) * turningPower * timeDelta;
        }
        if (carTurnRight) {
            rot += signOfTurn * sqrt(sqrt(abs(vel))) * turningPower * timeDelta;
        }


        double eVel = 0.5 * CAR_MASS * vel * vel;
        double dir = 1;
        if (vel > 0) {
            if (carAcc) {
                eVel += enginePower * timeDelta;
            }
            if (carPlsBreak) {
                eVel -= breakingPower * timeDelta;
                if (eVel < 0) {
                    eVel = 0;
                }
            }
        } else if (vel < 0) {
            dir = -1;
            if (carAcc) {
                eVel -= breakingPower * timeDelta;

            }
            if (carPlsBreak) {
                eVel += enginePower * timeDelta;

            }
            if (eVel < 0) {
                eVel = 0;
            }
        } else {
            if (carAcc) {
                eVel += enginePower * timeDelta;
            }
            if (carPlsBreak) {
                eVel -= enginePower * timeDelta;
            }
            if (eVel < 0) {
                eVel = -eVel;
                dir = -1;
            }
        }
        vel = sqrt(2 * abs(eVel) / CAR_MASS) * dir;
    }

    /**
     * Apply air resistance and rolling resistance.
     * 
     * @param timeDelta Time since the last update.
     */
    private void applyResistance(double timeDelta) {

        double dir = signum(vel);
        double eVel = 0.5 * CAR_MASS * vel * vel;

        // air resistance
        double fAir = RES_AIR * vel * vel;

        // rolling resistance
        // look in bitmap weather the car is on track or on dirt
        double groundResistance =
                        trackBitMap[(int) (posX * 10)][(int) (posY * 10)] ? RES_TRACK : RES_DIRT;
        double fRolling = groundResistance * CAR_MASS * abs(vel) * 9.81;

        if ((fAir + fRolling) * timeDelta * MAGIC_CONSTANT < eVel) {
            eVel -= (fAir + fRolling) * timeDelta * MAGIC_CONSTANT;
        } else {
            eVel = 0;
        }

        // System.out.println((fAir + fRolling) * timeDelta * MAGIC_CONSTANT);

        // avoid infinite rolling with infinitly small velocity

        if (eVel < ɛ) {
            vel = 0;
        } else {
            vel = sqrt(2 * abs(eVel) / CAR_MASS) * dir;
        }

    }

    /**
     * Check if the car is destroyed or the player passed the checkpoint and the start/finishline.
     */


    /**
     * Check if the racecar passed the start/finish line or the checkpoint. If the car passed the
     * start/finish line, start the timer if the checkpoint has not been passed yet.
     * 
     * @return true if the center of the car is on a line.
     */
    public boolean checkLines(RoundTimer timer) {
        // start/finishline passed once -> game starts
        // start/finishline passed after checkpoint has been passed -> game ends
        if (finishLine.contains(posX, posY)) {
            if (passedCheckpoint) {
                passedEndLine = true;
            } else {
                timer.unfreeze();
            }
        } else if (checkpoint.contains(posX, posY)) {
            passedCheckpoint = true;
        } else {
            return false;
        }
        return true;

    }

    @Override
    public void freeze() {
        frozen = true;
    }

    @Override
    public void unfreeze() {
        frozen = false;
    }

    /**
     * Read in bitmap that the car uses to determine on which kind of ground it currently is.
     */
    public static void initTrack() {
        BufferedImage mask = null;
        try {
            mask = ImageIO.read(new File("res/track_mask.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        trackBitMap = new boolean[mask.getWidth()][mask.getHeight()];
        for (int i = 0; i < mask.getWidth(); i++) {
            for (int j = 0; j < mask.getHeight(); j++) {
                if (mask.getRGB(i, j) != -1) {
                    trackBitMap[i][j] = true;
                }
            }
        }
    }

    // Getters and Setters after here

    /**
     * 
     * @return The length of the car.
     */
    public double getWidth() {
        return width;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getVel() {
        return vel;
    }

    /**
     * @return The image that represents the state of the car
     */
    private Image getImage() {
        if (imageDestroyed == null || imageNotDestroyed == null) {
            imageNotDestroyed = new Image("car_intact.png");
            imageDestroyed = new Image("car_destroyed.png");
        }
        return destroyed ? imageDestroyed : imageNotDestroyed;
    }

    /**
     * 
     * @return A collection containing the points where the car is checked for a collision.
     */
    private Collection<Point2D> getCollisionPoints() {
        Collection<Point2D> corners = new ArrayList<>();

        // bottom left
        corners.add(new Point2D(posX - (width / 2 * cos(rot)) - (height / 2 * sin(rot)),
                        posY - (width / 2 * sin(rot)) + (height / 2 * cos(rot))));

        // top left
        corners.add(new Point2D(posX - (width / 2 * cos(rot)) + (height / 2 * sin(rot)),
                        posY - (width / 2 * sin(rot)) - (height / 2 * cos(rot))));

        // middle left
        corners.add(new Point2D(posX - (width / 2 * cos(rot)), posY - (width / 2 * sin(rot))));

        // bottom right
        corners.add(new Point2D(posX + (width / 2 * cos(rot)) - (height / 2 * sin(rot)),
                        posY + (width / 2 * sin(rot)) + (height / 2 * cos(rot))));

        // top right
        corners.add(new Point2D(posX + (width / 2 * cos(rot)) + (height / 2 * sin(rot)),
                        posY + (width / 2 * sin(rot)) - (height / 2 * cos(rot))));

        // middle right

        corners.add(new Point2D(posX + (width / 2 * cos(rot)), posY + (width / 2 * sin(rot))));
        return corners;
    }

    /**
     * @return The trackBitMap
     */
    public static boolean[][] getTrackBitMap() {
        return trackBitMap;
    }

    public double getRotation() {
        return rot;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    /**
     * 
     * @return true if and only if the velocity of the car is not equal to zero
     */
    public boolean isMoving() {
        return vel != 0;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean hasPassedEndLine() {
        return passedEndLine;
    }

}
