package software.leonov.common;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to monitor the progress of a long running operation. This class is thread safe.
 * 
 * @author Zhenya Leonov
 */
public final class ProgressMonitor {

    public final static int DEFAULT_MIN_STEP_SIZE = 10;
    public final static int DEFAULT_MAX_STEP_SIZE = 1000;

    private final int minStepSize;
    private final int maxStepSize;

    private final List<ProgressListener> listeners = new LinkedList<>();

    private int progress = 0;
    private int step = 0;
    private Integer maximum = null;
    private boolean completed = false;

    private ProgressMonitor(final int minStepSize, final int maxStepSize) {
        if (minStepSize <= 0)
            throw new IllegalArgumentException("minStepSize <= 0");
        if (maxStepSize <= 0)
            throw new IllegalArgumentException("maxStepSize <= 0");
        if (maxStepSize < minStepSize)
            throw new IllegalArgumentException("maxStepSize < minStepSize");

        this.minStepSize = minStepSize;
        this.maxStepSize = maxStepSize;
        this.step = minStepSize;
    }

    /**
     * Returns a new {@code ProgressMonitor} with the specified minimum and maximum step size.
     * 
     * @param minStepSize the minimum step size
     * @param maxStepSize the maximum step size
     * @return a new {@code ProgressMonitor} with the specified minimum and maximum step size
     */
    public static ProgressMonitor withMinMaxSize(final int minStepSize, final int maxStepSize) {
        return new ProgressMonitor(minStepSize, maxStepSize);
    }

    /**
     * Returns a new {@code ProgressMonitor} with the specified {@code stepSize}.
     * 
     * @param stepSize the specified step size
     * @return a new {@code ProgressMonitor} with the specified {@code stepSize}
     */
    public static ProgressMonitor withConstantStepSize(final int stepSize) {
        return new ProgressMonitor(stepSize, stepSize);
    }

    /**
     * Returns a new {@code ProgressMonitor} with the minimum and maximum step size set to {@link #DEFAULT_MIN_STEP_SIZE}
     * and {@link #DEFAULT_MAX_STEP_SIZE} respectively.
     * 
     * @return a new {@code ProgressMonitor} with the minimum and maximum step size set to {@link #DEFAULT_MIN_STEP_SIZE}
     *         and {@link #DEFAULT_MAX_STEP_SIZE} respectively
     */
    public static ProgressMonitor create() {
        return new ProgressMonitor(DEFAULT_MIN_STEP_SIZE, DEFAULT_MAX_STEP_SIZE);
    }

    /**
     * Resets this {@code ProgressMonitor} to the beginning state. The {@link #setProgress(int) progress} is set to zero,
     * the step size is set to the minimum step size specified at creation, and the {@link #setMaximum(int) maximum bound}
     * is removed.
     * <p>
     * All {@link #addProgressListener(ProgressListener) registered} {@code ProgressListener}s are kept.
     * 
     * @return this {@code ProgressMonitor} instance
     */
    public synchronized ProgressMonitor reset() {
        progress = 0;
        this.step = minStepSize;
        maximum = null;
        completed = false;
        return this;
    }

    /**
     * Adds the specified progress listener to handle progress events.
     * 
     * @param listener the progress listener to add
     * @return this {@code ProgressMonitor} instance
     */
    public synchronized ProgressMonitor addProgressListener(final ProgressListener listener) {
        if (listener == null)
            throw new NullPointerException("listener == null");
        listeners.add(listener);
        return this;
    }

    /**
     * Sets the maximum bound.
     * 
     * @param maximum the maximum bound
     * @return this {@code ProgressMonitor} instance
     */
    public synchronized ProgressMonitor setMaximum(final int maximum) {
        if (completed)
            throw new IllegalStateException("completed() has already been called: must invoke reset() to continue");
        if (maximum <= 0)
            throw new IllegalArgumentException("maximum <= 0");
        if (maximum <= getProgress())
            throw new IllegalArgumentException("maximum <= getProgress()");

        this.maximum = maximum;
        step = maximum > maxStepSize * 5 ? maxStepSize : (maximum / 5 <= minStepSize ? minStepSize : maximum / 5);

        return this;
    }

    /**
     * Returns the maximum bound.
     * 
     * @return the maximum bound
     */
    public synchronized Integer getMaximum() {
        return maximum;
    }

    /**
     * Increments the progress count by 1, invoking the {@link #addProgressListener(ProgressListener) registered}
     * {@code ProgressListener}s if necessary.
     * 
     * @return the new progress count
     */
    public synchronized int increment() {
        if (completed)
            throw new IllegalStateException("completed() has already been called: must invoke reset() to continue");
        setProgress(getProgress() + 1);
        return getProgress();
    }

    /**
     * Sets the progress count, invoking the {@link #addProgressListener(ProgressListener) registered}
     * {@code ProgressListener}s if necessary.
     * 
     * @param progress the new progress count
     * @return this {@code ProgressMonitor} instance
     */
    public synchronized ProgressMonitor setProgress(final int progress) {
        if (completed)
            throw new IllegalStateException("completed() has already been called: must invoke reset() to continue");
        if (progress < getProgress())
            throw new IllegalArgumentException("progress < getProgress()");

        final int prev = this.progress;
        this.progress = progress;

        if (progress % step == 0 || progress - prev > step) {
            notifyListeners();

            if (step < maxStepSize)
                step = progress > maxStepSize * 5 ? maxStepSize : (progress / 5 <= minStepSize ? minStepSize : progress / 5);
        }

        return this;
    }

    /**
     * Returns the progress count.
     * 
     * @return the progress count
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * Invokes the {@link #addProgressListener(ProgressListener) registered} {@code ProgressListener}s for the final time if
     * necessary.
     * <p>
     * This {@code ProgressMonitor} will be left unusable after this call returns until {@link #reset()} is invoked.
     * 
     * @return the progress count
     */
    public synchronized int completed() {
        if (completed)
            throw new IllegalStateException("completed() has already been called: must invoke reset() to continue");

        if (progress == 0 || progress % step != 0)
            notify();

        completed = true;
        return progress;
    }

    private void notifyListeners() {
        for (final ProgressListener listener : listeners)
            listener.progressChanged(progress);
    }

    /**
     * Removes all previous {@link #addProgressListener(ProgressListener) registered} {@code ProgressListener}s.
     * 
     * @return this {@code ProgressMonitor} instance
     */
    public synchronized ProgressMonitor removeListeners() {
        this.listeners.clear();
        return this;
    }

}
