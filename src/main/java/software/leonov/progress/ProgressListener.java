package software.leonov.progress;

/**
 * A listener interface for handling progress change events.
 * 
 * @author Zhenya Leonov
 */
@FunctionalInterface
public interface ProgressListener {

    /**
     * Receives a progress changed event.
     * 
     * @param event the progress change event
     */
    public void progressChanged(final ProgressEvent event);

}
