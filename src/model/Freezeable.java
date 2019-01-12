package model;

/**
 * The "Freezeable" interface is implemented by all classes whose instances can be frozen. A frozen
 * object will not change the state it possesed at the time it was frozen until it is unfrozen.
 * 
 * @author Juri Dispan
 *
 */
public interface Freezeable {
    /**
     * reject requests for change of state until the unfreeze() method is called.
     */
    public void freeze();

    /**
     * allow changes in state (again).
     */
    public void unfreeze();
}
