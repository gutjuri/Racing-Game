import static org.junit.Assert.*;
import org.junit.Test;
import model.RoundTimer;

public class TestRoundTimer {
    RoundTimer tmr;

    private void setup() {
        tmr = new RoundTimer();
    }

    @Test
    public void testStartStop() {
        setup();
        assertFalse(tmr.isRunning());
        tmr.start();
        assertTrue(tmr.isRunning());
        tmr.start();
        assertTrue(tmr.isRunning());
        tmr.stop();
        assertFalse(tmr.isRunning());
        tmr.stop();
        assertFalse(tmr.isRunning());
    }

    @Test
    public void testFreezeUnfreeze() {
        setup();
        assertFalse(tmr.isRunning());
        tmr.unfreeze();
        assertTrue(tmr.isRunning());
        tmr.unfreeze();
        assertTrue(tmr.isRunning());
        tmr.freeze();
        assertFalse(tmr.isRunning());
    }

    @Test
    public void testToString() {
        setup();
        assertTrue(tmr.toString().length() == 5);
    }

    @Test
    public void testGetTime() {
        setup();
        assertEquals(0, tmr.getTimeDouble(), 0);
        tmr.start();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(tmr.getTimeDouble() > 0);
    }
}
