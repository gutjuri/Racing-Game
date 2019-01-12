import static org.junit.Assert.*;
import org.junit.Test;
import application.RGMain;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

public class TestGUI {

    /**
     * Test, wheather the GUI gets drawn correctly. Exception -> test fails.
     */
    @Test
    public void doesGuiShow() {
        new Thread(() -> {
            new JFXPanel();
            Platform.runLater(() -> {
                Platform.runLater(() -> {
                    try {
                        new RGMain().start(new Stage());
                    } catch (Exception e) {
                        fail("Exception starting GUI");
                    }
                });
            });
        }).start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
