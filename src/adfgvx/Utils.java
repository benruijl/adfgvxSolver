package adfgvx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class.
 * 
 * @author Ben Ruijl
 * 
 */
public class Utils {
    /**
     * A fast exp approximation. The validity range is from -700 to 700.
     * 
     * @param x
     *            Value
     * @return Return e^x
     */
    public static double exp(final double x) {
        final long tmp = (long) (1512775 * x + (1072693248 - 60801));
        return Double.longBitsToDouble(tmp << 32);
    }

    /**
     * Inverts a map.
     * 
     * @param <A>
     *            Type of the value
     * @param <B>
     *            Type of the key
     * @param map
     *            Map to invert
     * @return Inverted map
     */
    public static <A, B> Map<A, B> invert(final Map<B, A> map) {
        final Map<A, B> result = new HashMap<A, B>();

        for (final Entry<B, A> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }

        return result;
    }
}
