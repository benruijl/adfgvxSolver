package utils;

public class IntPair<T> {
    private final T first;
    private final int second;

    public IntPair(T first, int second) {
        super();
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }
}
