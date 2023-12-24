package software.leonov.progress;

import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A {@code ProgressMonitor} can be used to track the progress of a long running operation.
 * <p>
 * This class can be instantiated by calling the {@link #create()}, {@link #withConstantStepSize(long)}, or
 * {@link #withMinMaxStepSize(long, long)} methods, followed by {@link #addProgressListener(ProgressListener) adding}
 * one or more {@link ProgressListener}s.
 * <p>
 * <b>Basic usage:</b>
 *
 * <pre><code class="line-numbers match-braces language-java">
 * final ProgressMonitor progress = ProgressMonitor.create().addProgressListener(event -> ...);
 *
 * while (...) {
 *     ...
 *     progress.increment();
 * }
 *
 * progress.completed();
 * </code></pre>
 * <p>
 * <b>Maximum value:</b>
 * <p>
 * Specifying the {@link #setMaximum(long) maximum} value is not mandatory. However, if specified, it's important to
 * note that the progress count cannot surpass the maximum value. If the initial value proves to be underestimated, it
 * should be adjusted to prevent an {@link IllegalArgumentException} when the progress count surpasses it.
 * <p>
 * <b>{@code ProgressEvent}s and step size:</b>
 * <p>
 * {@link ProgressEvent}s are {@link ProgressListener#progressChanged(ProgressEvent) published} commensurate with the
 * step size specified at creation. Duplicate events will never be published. Unless the step size is
 * {@link #withConstantStepSize(long) constant}, it will progressively increase from the minimum to the maximum
 * specified size. Note that setting or adjusting the maximum value may recalculate the step size.
 * <p>
 * <b>Progress completion:</b>
 * <p>
 * In typical scenarios {@link #completed()} should be called when the operation has concluded. In cases where the
 * operation might raise an exception, it may be appropriate to call {@code completed} within the finally clause of a
 * try statement. Calling {@code completed} multiple times is explicitly permitted and will have no subsequent effect.
 * Attempts to modify the progress count after calling {@code completed} will result in an {@code
 * IllegalStateException} until it is {@link #reset() reset}.
 * <p>
 * <b>Thread safety:</b>
 * <p>
 * This class is not thread safe. In a concurrent environment access to this class must be synchronized externally.
 * Typically only the {@link #increment()} or {@link #setProgress(long)} method are called while the operation is in
 * progress. A simple synchronized block is usually sufficient to ensure thread safety for most use cases:
 *
 * <pre><code class="line-numbers match-braces language-java">
 *     ...
 *     synchronized(...) {
 *         progress.increment();
 *     }
 *     ...
 * </code></pre>
 *
 * @author Zhenya Leonov
 */
public class ProgressMonitor {

    /**
     * The default minimum step size.
     */
    public final static long DEFAULT_MIN_STEP_SIZE = 10;

    /**
     * The default maximum step size.
     */
    public final static long DEFAULT_MAX_STEP_SIZE = 1000;

    private final long minStepSize;
    private final long maxStepSize;

    private long           progress = 0;
    private long           step     = 0;
    private Optional<Long> maximum  = Optional.empty();
    private boolean        done     = false;

    private final List<ProgressListener> listeners = new LinkedList<>();

    private ProgressMonitor(final long minStepSize, final long maxStepSize) {
        if (minStepSize <= 0)
            throw new IllegalArgumentException("minStepSize <= 0");
        if (maxStepSize <= 0)
            throw new IllegalArgumentException("maxStepSize <= 0");
        if (maxStepSize < minStepSize)
            throw new IllegalArgumentException("maxStepSize < minStepSize");

        this.minStepSize = minStepSize;
        this.maxStepSize = maxStepSize;
        this.step        = minStepSize;
    }

    /**
     * Returns a new {@code ProgressMonitor} with the specified minimum and maximum step size.
     * 
     * @param minStepSize the minimum step size
     * @param maxStepSize the maximum step size
     * @throws IllegalArgumentException if {@code minStepSize} <= 0, {@code maxStepSize} <= 0, or {@code maxStepSize} <
     *                                  {@code minMaxSize}
     * @return a new {@code ProgressMonitor} with the specified minimum and maximum step size
     */
    public static ProgressMonitor withMinMaxStepSize(final long minStepSize, final long maxStepSize) {
        return new ProgressMonitor(minStepSize, maxStepSize);
    }

    /**
     * Returns a new {@code ProgressMonitor} with the specified {@code stepSize}.
     * 
     * @param stepSize the specified step size
     * @return a new {@code ProgressMonitor} with the specified {@code stepSize}
     */
    public static ProgressMonitor withConstantStepSize(final long stepSize) {
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
     * Adds the specified listener to handle {@link ProgressEvent}s.
     * 
     * @param listener the listener to add
     * @return this {@code ProgressMonitor} instance
     */
    public ProgressMonitor addProgressListener(final ProgressListener listener) {
        requireNonNull(listener, "listener == null");
        listeners.add(listener);
        return this;
    }

    /**
     * Sets the maximum value. This method may be called more than once to reset the maximum value if this
     * {@code ProgressMonitor} has not {@link #completed() completed}.
     * 
     * @param maximum the maximum value
     * @throws IllegalStateException    if this {@link ProgressMonitor} has {@link #completed() completed}
     * @throws IllegalArgumentException if {@code maximum} < 1 or {@code maximum} < {@link #getProgress() progress}
     * @return this {@code ProgressMonitor} instance
     */
    public ProgressMonitor setMaximum(final long maximum) {
        if (done)
            throw new IllegalStateException("operation has completed");
        if (maximum < 1)
            throw new IllegalArgumentException("maximum < 1");
        if (maximum < progress)
            throw new IllegalArgumentException("maximum (" + maximum + ") < progress (" + progress + ")");

        this.maximum = Optional.of(maximum);
        step         = maximum > maxStepSize * 5 ? maxStepSize : (maximum / 5 <= minStepSize ? minStepSize : maximum / 5);

        return this;
    }

    /**
     * Returns the maximum value.
     * 
     * @return the maximum value
     */
    public Optional<Long> getMaximum() {
        return maximum;
    }

    /**
     * Increments the progress count by 1, {@link ProgressListener#progressChanged(ProgressEvent) publishing} a
     * {@link ProgressEvent} if necessary.
     * 
     * @throws IllegalStateException    if this {@link ProgressMonitor} has {@link #completed() completed}
     * @throws IllegalArgumentException if the new count > {@link #getMaximum() maximum}
     * @return the new progress count
     */
    public long increment() {
        setProgress(progress + 1);
        return getProgress();
    }

    /**
     * Sets the progress count, {@link ProgressListener#progressChanged(ProgressEvent) publishing} a {@link ProgressEvent}
     * if necessary.
     * 
     * @param count the new progress count
     * @throws IllegalStateException    if this {@link ProgressMonitor} has {@link #completed() completed}
     * @throws IllegalArgumentException if {@code count} < {@link #getProgress() progress} or {@code count} >
     *                                  {@link #getMaximum() maximum}
     * @return this {@code ProgressMonitor} instance
     */
    public ProgressMonitor setProgress(final long count) {
        if (done)
            throw new IllegalStateException("operation has completed");
        if (count < progress)
            throw new IllegalArgumentException("count (" + count + ") < progress (" + progress + ")");

        maximum.ifPresent(max -> {
            if (count > max)
                throw new IllegalArgumentException("count (" + count + ") > maximum (" + max + ")");
        });

        if (count > progress) {
            final long prev = this.progress;
            this.progress = count;

            if (count % step == 0 || count - prev > step) {
                publish();

                if (step < maxStepSize)
                    step = count > maxStepSize * 5 ? maxStepSize : (count / 5 <= minStepSize ? minStepSize : count / 5);
            }
        }

        return this;
    }

    /**
     * Returns the progress count.
     * 
     * @return the progress count
     */
    public long getProgress() {
        return progress;
    }

    /**
     * Marks this {@code ProgressMonitor} as completed, {@link ProgressListener#progressChanged(ProgressEvent) publishing}
     * the final {@link ProgressEvent} if necessary. Logically this signifies the end of the operation.
     * <p>
     * Calling this method multiple times is always permitted and will have no subsequent effect. Any further attempts to
     * modify the state of this {@code ProgressMonitor} will result in an {@code IllegalStateException} until it is
     * {@link #reset() reset}.
     */
    public void completed() {
        if (done)
            return;

        done = true;

        if (progress == 0 || progress % step != 0)
            publish();
    }

    /**
     * Returns whether or not this {@code ProgressMonitor} has {@link #completed() completed}.
     * 
     * @return whether or not this {@code ProgressMonitor} has {@link #completed() completed}
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Resets this {@code ProgressMonitor} its initial state, retaining all added {@link ProgressListener}s and the
     * {@link #getMaximum() maximum} value. After this call returns {@link #isDone()} will return {@code false} and
     * {@link #getProgress()} will return {@code 0}.
     * 
     * @return this {@code ProgressMonitor} instance
     */
    public ProgressMonitor reset() {
        progress  = 0;
        this.step = minStepSize;
        done      = false;
        return this;
    }

    long getCurrentStepSize() {
        return step;
    }

    private void publish() {
        final ProgressEvent event = new Event(progress, maximum);

        for (final ProgressListener listener : listeners)
            listener.progressChanged(event);
    }

    private static class Event implements ProgressEvent {
        private final long           progress;
        private final Optional<Long> maximum;

        Event(final long progress, final Optional<Long> maximum) {
            this.progress = progress;
            this.maximum  = maximum;
        }

        @Override
        public long getProgress() {
            return progress;
        }

        @Override
        public Optional<Long> getMaximum() {
            return maximum;
        }
    }

}