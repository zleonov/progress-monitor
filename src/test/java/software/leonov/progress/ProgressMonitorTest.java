package software.leonov.progress;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgressMonitorTest {

    static List<Long>      actual = new ArrayList<>();
    static ProgressMonitor progress;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        actual.clear();
        progress = new ProgressMonitor();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void increment_1_to_10000_completed() {
        progress.addProgressListener(event -> actual.add(event.getProgress()));

        for (int i = 0; i < 10000; i++)
            progress.increment();

        progress.complete();

        final List<Long> expected = Arrays.asList(10L, 20L, 30L, 40L, 50L, 60L, 72L, 84L, 96L, 114L, 132L, 156L, 186L, 222L, 264L, 312L, 372L, 444L, 528L, 630L, 756L, 906L, 1086L, 1302L, 1560L, 1872L, 2244L, 2688L, 3222L, 3864L, 4632L,
                5556L, 6000L, 7000L, 8000L, 9000L, 10000L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void setProgress_10_100_1000_10000_completed() {
        progress.addProgressListener(event -> actual.add(event.getProgress()));

        LongStream.of(10, 100, 1000, 10000).forEach(progress::setProgress);

        progress.complete();

        final List<Long> expected = Arrays.asList(10L, 100L, 1000L, 10000L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void setProgress_10_10_10_completed() {
        progress.addProgressListener(event -> actual.add(event.getProgress()));

        LongStream.of(10, 10, 10).forEach(progress::setProgress);

        progress.complete();

        final List<Long> expected = Arrays.asList(10L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void setProgress_10_11_12() {
        progress.addProgressListener(event -> actual.add(event.getProgress()));

        LongStream.of(10, 11, 12).forEach(progress::setProgress);

        final List<Long> expected = Arrays.asList(10L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void setProgress_1_2_3() {
        progress.addProgressListener(event -> actual.add(event.getProgress()));

        LongStream.of(1, 2, 3).forEach(progress::setProgress);

        assertThat(actual).isEqualTo(Collections.emptyList());
    }

    @Test
    void create_minStepSize_0() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> new ProgressMonitor().setDynamicStepSize(0L, 1L));
        assertThat(e.getMessage()).isEqualTo("minStepSize <= 0");
    }

    @Test
    void create_maxStepSize_0() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> new ProgressMonitor().setDynamicStepSize(1L, 0L));
        assertThat(e.getMessage()).isEqualTo("maxStepSize <= 0");
    }

    @Test
    void create_minStepSize_10_maxStepSize_1() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> new ProgressMonitor().setDynamicStepSize(10L, 1L));
        assertThat(e.getMessage()).isEqualTo("maxStepSize < minStepSize");
    }

    @Test
    void create_setProgress_completed_increment() {
        progress.setProgress(100).complete();

        final Exception e = assertThrows(IllegalStateException.class, () -> progress.increment());
        assertThat(e.getMessage()).isEqualTo("operation has completed");
    }

    @Test
    void create_setProgress_completed_setProgress() {
        progress.setProgress(100).complete();

        final Exception e = assertThrows(IllegalStateException.class, () -> progress.setProgress(200));
        assertThat(e.getMessage()).isEqualTo("operation has completed");
    }

    @Test
    void create_setProgress_100_setProgress_10() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> progress.setProgress(100).setProgress(10));
        assertThat(e.getMessage()).isEqualTo("count (10) < progress (100)");
    }

    @Test
    void create_setMaximum_100_setProgress_1000() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> progress.setMaximum(100).setProgress(1000));
        assertThat(e.getMessage()).isEqualTo("count (1000) > maximum (100)");
    }

    @Test
    void create_setMaximum_100_setProgress_100_increment() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> progress.setMaximum(100).setProgress(100).increment());
        assertThat(e.getMessage()).isEqualTo("count (101) > maximum (100)");
    }

    @Test
    void setMaximum_0() {
        final Exception e = assertThrows(IllegalArgumentException.class, () -> progress.setMaximum(0));
        assertThat(e.getMessage()).isEqualTo("maximum < 1");
    }

    @Test
    void setMaximum_getMaximum() {
        assertThat(progress.setMaximum(100).getMaximum()).hasValue(100L);
    }

    @Test
    void getMaximum_empty() {
        assertThat(progress.getMaximum()).isEmpty();
    }

    @Test
    void setStepSize_100_setProgress_50_setStepSize_55_setProgress_75() {
        progress.addProgressListener(event -> actual.add(event.getProgress()));

        progress.setStepSize(100).setProgress(50).setStepSize(55).setProgress(75);

        final List<Long> expected = Arrays.asList(75L);

        assertThat(actual).isEqualTo(expected);
    }

}
