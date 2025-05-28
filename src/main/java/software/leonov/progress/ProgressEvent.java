package software.leonov.progress;

import java.util.OptionalLong;

/**
 * A {@code ProgressEvent} is used to notify {@link ProgressListener}s that the progress has changed.
 * 
 * @author Zhenya Leonov
 */
public interface ProgressEvent {

    /**
     * Returns the current progress.
     * 
     * @return the current progress
     */
    public long getProgress();

    /**
     * Returns the maximum value.
     * 
     * @return the maximum value
     */
    public OptionalLong getMaximum();

}
