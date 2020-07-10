package software.leonov.common;
/**
 * A listener interface for handling progress changes when {@link ProgressMonitor monitoring} long running operations.
 * 
 * @author Zhenya Leonov
 */
@FunctionalInterface
public interface ProgressListener {

    /**
     * Invoked when the progress has changed.
     * 
     * @param progress the new {@link ProgressMonitor#setProgress(int) progress} count
     */
    public void progressChanged(final int progress);

}
