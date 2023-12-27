package software.leonov.progress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Example {

    public static void main(String[] args) throws IOException {
        final URL  from = new URL("https://repo1.maven.org/maven2/com/google/guava/guava/32.1.3-jre/guava-32.1.3-jre.jar");
        final Path to   = Paths.get("d:/guava-32.1.3-jre.jar");

        System.out.println("Downloading " + from + " to " + to);

        final ProgressMonitor progress = new ProgressMonitor().addProgressListener(new Percentage() {

            @Override
            public void onUpdate(final String pct) {
                System.out.println("Downloaded " + pct + "%");
            }
        });

        download(from, to, progress);
    }

    private static void download(final URL from, final Path fout, final ProgressMonitor progress) throws IOException {

        final URLConnection conn   = from.openConnection();
        final long          length = conn.getContentLengthLong();

        progress.setMaximum(length).setStepSize(length / 100);
        
        final byte[] buffer = new byte[1024];

        try (final InputStream in = conn.getInputStream(); final OutputStream out = Files.newOutputStream(fout)) {
            int count = 0;

            while ((count = in.read(buffer, 0, 1024)) != -1) {
                out.write(buffer, 0, count);
                progress.setProgress(progress.getProgress() + count);
            }
        }

        progress.completed();
    }

}