package adfgvx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class Grid {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Grid.class);

    private final int dimension;
    private List<List<Character>> grid;

    public Grid(int n) {
        dimension = n;

        grid = new ArrayList<List<Character>>(n);

        for (int i = 0; i < n; i++) {
            grid.add(new ArrayList<Character>());
        }
    }

    public void clear() {
        for (int i = 0; i < dimension; i++) {
            grid.clear();
        }
    }

    public void add(String data) {
        for (int i = 0; i < data.length(); i++) {
            grid.get(i % dimension).add(data.charAt(i));
        }
    }

    public void transpose(List<Integer> key) {
        List<List<Character>> transposedGrid = new ArrayList<List<Character>>(
                dimension);

        for (int i = 0; i < key.size(); i++) {
            transposedGrid.add(grid.get(key.get(i)));
        }

        grid = transposedGrid;
    }

    public static List<Integer> generateRandomKey(int length) {
        List<Integer> key = new ArrayList<Integer>(length);

        for (int i = 0; i < length; i++) {
            key.add(i);
        }

        Collections.shuffle(key);

        return key;
    }

    public List<Character> getColumn(int index) {
        return grid.get(index);
    }

    public List<List<Character>> getGrid() {
        return grid;
    }

    public String encode() {
        StringBuffer result = new StringBuffer();

        int size = grid.get(0).size();

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < dimension; i++) {
                if (size != grid.get(i).size()) {
                    LOG.fatal("Grid data is not rectangular.");
                    break;
                }

                result.append(grid.get(i).get(j));
            }
        }

        return result.toString();
    }
}
