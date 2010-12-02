package adfgvx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class represents the transposition grid.
 * 
 * @author Ben Ruijl
 * 
 */
public class Grid {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Grid.class);

    /** The amount of columns in the grid. */
    private final int dimension;
    /** Grid characters. */
    private List<List<Character>> grid;

    /**
     * Creates a new grid.
     * 
     * @param columnCount
     *            Amount of columns
     */
    public Grid(final int columnCount) {
        dimension = columnCount;

        grid = new ArrayList<List<Character>>(columnCount);

        for (int i = 0; i < columnCount; i++) {
            grid.add(new ArrayList<Character>());
        }
    }

    /**
     * Clears the grid.
     */
    public void clear() {
        for (int i = 0; i < dimension; i++) {
            grid.clear();
        }
    }

    /**
     * Add text to the grid.
     * 
     * @param data
     *            Text
     */
    public void add(final String data) {
        for (int i = 0; i < data.length(); i++) {
            grid.get(i % dimension).add(data.charAt(i));
        }
    }

    /**
     * Switches columns in the grid according to the key.
     * 
     * @param key
     *            Transposition key
     */
    public void switchColumns(final List<Integer> key) {
        final List<List<Character>> transposedGrid = new ArrayList<List<Character>>(
                dimension);

        for (int i = 0; i < key.size(); i++) {
            transposedGrid.add(grid.get(key.get(i)));
        }

        grid = transposedGrid;
    }

    /**
     * Generates a random key.
     * 
     * @param length
     *            Key length
     * @return Random key
     */
    public static List<Integer> generateRandomKey(final int length) {
        final List<Integer> key = new ArrayList<Integer>(length);

        for (int i = 0; i < length; i++) {
            key.add(i);
        }

        Collections.shuffle(key);

        return key;
    }

    /**
     * Gets a column.
     * 
     * @param index
     *            Index of column
     * @return List of characters in the column
     */
    public List<Character> getColumn(final int index) {
        return grid.get(index);
    }

    /**
     * Gets the entire grid.
     * 
     * @return Grid
     */
    public List<List<Character>> getGrid() {
        return grid;
    }

    /**
     * Encodes the grid by doing a vertical readout.
     * 
     * @return Encoded text
     */
    public String encode() {
        final StringBuffer result = new StringBuffer();

        final int size = grid.get(0).size();

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
