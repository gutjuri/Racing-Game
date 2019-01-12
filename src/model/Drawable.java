package model;

import javafx.scene.canvas.GraphicsContext;

/**
 * The interface implemented by all classes which can draw a representation of themselves on a
 * canvas.
 * 
 * @author Juri Dispan
 *
 */
public interface Drawable {
    /**
     * Draw a representation of the Drawable on the graphics context
     * 
     * @param gc GraphicsContext of the canvas to be drawn on
     */
    public void draw(GraphicsContext gc);
}
