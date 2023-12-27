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
 * Extending class need only to implement the {@link #onUpdate(String)} method, which receives the formatted
 * representation of the percent value. The format pattern can be specified using {@link #Percentage(DecimalFormat)} or
 * {@link #Percentage(Locale, String)} methods. By default, the this class formats the value to the nearest whole
 * percent without decimal places. Users of this class are guaranteed never to receive duplicate percent values and will
 * never encounter zero as an input.
 * <p>
 * Using this listener when the maximum value is not {@link ProgressMonitor#setMaximum(long) specified} will lead to an
 * {@code IllegalStateException}.
 * <p>
 * For example: <pre><code class="line-numbers match-braces language-java">
 *     ...
 *     final ProgressMonitor progress = new ProgressMonitor().addProgressListener(new Percentage() {
 *
 *         {@literal @}Override
 *         public void onUpdate(final String pct) {
 *             System.out.println("Processed " + pct + "%");
 *         }
 *     });
 *     ...
 * </code></pre>
 *
 * @author Zhenya Leonov
 */
public abstract class Percentage implements ProgressListener {

    private final DecimalFormat format;
    private String              last;

    /**
     * Creates a new {@link Percentage} {@code ProgressListener}.
     */
    public Percentage() {
        format = new DecimalFormat("#");
    }

    /**
     * Creates a new {@link Percentage} {@code ProgressListener} which uses the specified {@code DecimalFormat} to format
     * the percent value.
     * 
     * @param format the format to use
     */
    public Percentage(final DecimalFormat format) {
        requireNonNull("format", "format == null");
        this.format = format;
    }

    /**
     * Creates a new {@link Percentage} {@code ProgressListener} which uses the specified {@code Locale} and {@code pattern}
     * to format the percent value.
     * 
     * @param locale  the {@code Locale} to use when formatting the percent value
     * @param pattern the pattern to apply
     */
    public Percentage(final Locale locale, final String pattern) {
        requireNonNull("locale", "locale == null");
        requireNonNull("pattern", "pattern == null");
        this.format = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.format.applyPattern(pattern);
    }

    /**
     * {@inheritDoc} Then {@link DecimalFormat#format(double) formats} the value as a percentage of the
     * {@link ProgressMonitor#getMaximum() maximum} value and invokes {@link #onUpdate(String)}.
     */
    @Override
    public final void progressChanged(final ProgressEvent event) {
        final double percentage = (double) event.getProgress() / event.getMaximum().orElseThrow(() -> new IllegalStateException("maximum value undefined")) * 100;
        final String pct        = format.format(percentage);

        try {
            if (!pct.equals(last) && format.parse(pct).doubleValue() != 0) {
                last = pct;
                onUpdate(pct);
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
    public abstract void onUpdate(final String pct);

}
