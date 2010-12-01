package adfgvx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {
    public static <A, B> Map<A, B> invert(Map<B, A> map) {
	Map<A, B> result = new HashMap<A, B>();

	for (Entry<B, A> entry : map.entrySet()) {
	    result.put(entry.getValue(), entry.getKey());
	}

	return result;
    }
}
