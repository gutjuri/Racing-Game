import static org.junit.Assert.*;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import model.Car;
import model.Obstacle;
import model.RoundTimer;

public class TestCar {

    Car car;


    public void setupCar() {
        car = new Car();
    }

    @Test
    public void testStartingState() {
        setupCar();
        assertEquals(68.1, car.getPosX(), 0.1);
        assertEquals(10.0, car.getPosY(), 0.1);
        assertEquals(0, car.getVel(), 0.0);
        assertEquals(0, car.getRotation(), 0.0);
        assertFalse(car.isMoving());
        assertFalse(car.isDestroyed());
        assertFalse(car.hasPassedEndLine());
        assertTrue(car.isFrozen());
    }

    @Test
    public void testTrack() {
        Car.initTrack();
        assertFalse(Car.getTrackBitMap()[0][0]);
        assertTrue(Car.getTrackBitMap()[681][100]);
    }

    @Test
    public void testAcceleration() {
        setupCar();
        car.unfreeze();
        car.stepForward(1, true, false, false, false, Collections.emptyList());
        assertTrue(car.isMoving());
        assertFalse(car.isFrozen());
    }

    @Test
    public void testFreeze() {
        setupCar();
        assertTrue(car.isFrozen());
        car.unfreeze();
        assertFalse(car.isFrozen());
        car.freeze();
        assertTrue(car.isFrozen());
        car.stepForward(1, true, false, false, false, Collections.emptyList());
        // car hasnt moved because it is frozen
        assertEquals(car.STARTING_POS_X, car.getPosX(), 0.0);
    }

    @Test
    public void testCheckLines() {
        setupCar();
        Car.initTrack();
        car.unfreeze();
        RoundTimer timer = new RoundTimer();
        assertFalse(car.checkLines(timer));
        double timeDelta = 0.2; // Calculated based on acceleration, mass and starting position of
                                // the car, this is the point in time when the car crosses the
                                // starting line
        car.stepForward(timeDelta, true, false, false, false, Collections.emptyList());
        assertTrue(car.checkLines(timer));
        car.stepForward(1, true, false, false, false, Collections.emptyList());
        assertFalse(car.checkLines(timer));
    }

    @Test
    public void checkCollectionWithBoundaryHighVel() {
        setupCar();
        Car.initTrack();
        car.unfreeze();
        car.stepForward(2, true, false, false, false, Collections.emptyList());
        assertTrue(car.isDestroyed());
    }

    @Test
    public void checkCollisions() {
        setupCar();
        Car.initTrack();
        car.unfreeze();
        Obstacle inFrontOfStartLine =
                        new Obstacle(car.STARTING_POS_X - 10, car.STARTING_POS_Y, 1, 0);
        List<Obstacle> obstacles = Collections.singletonList(inFrontOfStartLine);
        car.stepForward(0.5, true, false, false, false, obstacles);
        assertTrue(car.isDestroyed());
    }

    @Test
    public void testTurnLeft() {
        setupCar();
        car.unfreeze();
        car.stepForward(1, false, false, true, false, Collections.emptyList());
        assertEquals(0, car.getRotation(), 0.0);
        // speed up so that turning is possible
        car.stepForward(1, true, false, false, false, Collections.emptyList());
        car.stepForward(1, false, false, true, false, Collections.emptyList());
        assertTrue(car.getRotation() < 0);
    }

    @Test
    public void testTurnRight() {
        setupCar();
        car.unfreeze();
        car.stepForward(1, false, false, false, true, Collections.emptyList());
        assertEquals(0, car.getRotation(), 0.0);
        // speed up so that turning is possible
        car.stepForward(1, true, false, false, false, Collections.emptyList());
        car.stepForward(1, false, false, false, true, Collections.emptyList());
        assertTrue(car.getRotation() > 0);
    }

    @Test
    public void testReverse() {
        setupCar();
        car.unfreeze();
        car.stepForward(1, false, true, false, false, Collections.emptyList());
        assertTrue(car.getVel() < 0);
    }

}
