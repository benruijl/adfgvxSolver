package adfgvx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {
    public static double exp(double val) {
	final long tmp = (long) (1512775 * val + (1072693248 - 60801));
	return Double.longBitsToDouble(tmp << 32);
    }

    public static <A, B> Map<A, B> invert(Map<B, A> map) {
	Map<A, B> result = new HashMap<A, B>();

	for (Entry<B, A> entry : map.entrySet()) {
	    result.put(entry.getValue(), entry.getKey());
	}

	return result;
    }
}
