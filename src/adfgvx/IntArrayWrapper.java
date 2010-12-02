package adfgvx;

import java.util.Arrays;

public class IntArrayWrapper {
    private final int[] data;

    public IntArrayWrapper(final int[] data) {
        super();
        this.data = data;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        return Arrays.equals(data, ((IntArrayWrapper) obj).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
