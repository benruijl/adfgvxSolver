package adfgvx;

import java.util.Arrays;

public class IntArrayWrapper {
    private int[] data;

    public IntArrayWrapper(int[] data) {
	super();
	this.data = data;
    }

    public int[] getData() {
	return data;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null || obj.getClass() != this.getClass())
	    return false;

	return Arrays.equals(data, ((IntArrayWrapper) obj).data);
    }

    @Override
    public int hashCode() {
	return Arrays.hashCode(data);
    }
}
