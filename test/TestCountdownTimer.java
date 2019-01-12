import static org.junit.Assert.*;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import model.CountdownTimer;
import model.Drawable;

public class TestCountdownTimer {
    CountdownTimer cnt;

    private void setup() {
        this.cnt = new CountdownTimer();
    }

    @Test
    public void testOnCreation() {
        setup();
        assertFalse(cnt.isRunning());
        assertFalse(cnt.hasFired());
    }

    @Test
    public void testStart() {
        List<Drawable> toDraw = new LinkedList<>();
        setup();
        cnt.start(toDraw);
        assertTrue(cnt.isRunning());
        cnt.stop();
        assertFalse(cnt.isRunning());
        assertTrue(toDraw.contains(cnt));
    }
}
