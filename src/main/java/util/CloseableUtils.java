package util;

import java.io.Closeable;
import java.io.IOException;

/**
 * {@link java.io.Closeable}工具.
 *
 * @author skywalker
 */
public class CloseableUtils {

    private CloseableUtils() {
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignore) {
        }
    }

}
