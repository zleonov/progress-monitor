package software.leonov.progress;

import static java.util.Objects.requireNonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A skeletal implementation of the {@code ProgressListener} interface which calculates the
 * {@link ProgressMonitor#getProgress() progress} value as a percentage of the {@link ProgressMonitor#getMaximum()
 * maximum} value.
 * <p>
 * Extending class need only implement the {@link #progressChanged(String)} method, which receives the formatted
 * representation of the percent value. The format pattern can be specified using
 * {@link #PercentListener(DecimalFormat)} or {@link #PercentListener(Locale, String)} methods. By default, the this
 * class formats the value to the nearest whole percent without decimal places. Users of this class are guaranteed never
 * to receive duplicate percent values and will never encounter zero as an input.
 * <p>
 * Using this listener when the maximum value is not {@link ProgressMonitor#setMaximum(long) specified} will lead to an
 * {@code IllegalStateException}.
 * <p>
 * For example: <pre><code class="line-numbers match-braces language-java">
 *     ...
 *     final ProgressMonitor progress = new ProgressMonitor().addProgressListener(new PercentListener() {
 *
 *         {@literal @}Override
 *         public void progressChanged(final String pct) {
 *             System.out.println("Processed " + pct + "%");
 *         }
 *     });
 *     ...
 * </code></pre>
 *
 * @author Zhenya Leonov
 */
public abstract class PercentListener implements ProgressListener {

    private final DecimalFormat format;
    private String              last;

    /**
     * Creates a new {@link PercentListener} which formats the precent value to the nearest whole percent without decimal
     * places.
     */
    public PercentListener() {
        format = new DecimalFormat("#");
    }

    /**
     * Creates a new {@link PercentListener} which uses the specified {@code DecimalFormat} to format the percent value.
     * 
     * @param format the format to use
     */
    public PercentListener(final DecimalFormat format) {
        requireNonNull("format", "format == null");
        this.format = format;
    }

    /**
     * Creates a new {@link PercentListener} which uses the specified {@code Locale} and
     * {@link DecimalFormat#applyPattern(String) pattern} to format the percent value.
     * 
     * @param locale  the {@code Locale} to use when formatting the percent value
     * @param pattern the {@link DecimalFormat#applyPattern(String) pattern} to apply
     */
    public PercentListener(final Locale locale, final String pattern) {
        requireNonNull("locale", "locale == null");
        requireNonNull("pattern", "pattern == null");
        this.format = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.format.applyPattern(pattern);
    }

    /**
     * {@inheritDoc} Then {@link DecimalFormat#format(double) formats} the value as a percentage of the
     * {@link ProgressMonitor#getMaximum() maximum} value and invokes {@link #progressChanged(String)}.
     * <p>
     * <b>Note:</b> This method is marked final and cannot be overridden by extending classes.
     */
    @Override
    public final void progressChanged(final ProgressEvent event) {
        final long   max        = event.getMaximum().orElseThrow(() -> new IllegalStateException("maximum value undefined"));
        final double percentage = (double) event.getProgress() / max * 100;
        final String pct        = format.format(percentage);

        try {
            if (!pct.equals(last) && format.parse(pct).doubleValue() > 0D) {
                last = pct;
                progressChanged(pct);
            }
        } catch (final ParseException e) {
            // cannot happen
        }
    }

    /**
     * Receives the formatted percent value as a calculation of the {@link ProgressMonitor#getProgress() progress} against
     * the {@link ProgressMonitor#getMaximum() maximum} value. Users are guaranteed never to receive duplicate percent
     * values and will never encounter zero as an input.
     * 
     * @param pct the formatted percent value
     * @throws IllegalStateException if the maximum value is not {@link ProgressMonitor#setMaximum(long) specified}
     */
    public abstract void progressChanged(final String pct);

}
