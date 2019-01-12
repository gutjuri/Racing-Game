import static org.junit.Assert.*;
import java.util.Collection;
import org.junit.Test;
import model.Car;
import model.Obstacle;

public class TestObstacles {
    Collection<Obstacle> obs;

    private void setup(int amount, double spacing) {
        Car.initTrack();
        obs = Obstacle.generateObs(amount, spacing);
    }

    @Test
    public void testSpacing() {
        double spacing = 3;
        setup(50, spacing);
        obs.forEach(cObs -> {
            obs.forEach(aObs -> {
                if (cObs != aObs) {
                    assertTrue(cObs.spaceBetween(aObs) > spacing);
                }
            });
        });
    }
}
