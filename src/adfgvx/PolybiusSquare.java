package adfgvx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a useful representation of a Polybius square. This square is
 * used to fraction each character into two in the encryption and to defraction
 * each character in the decryption.
 * 
 * @author Ben Ruijl
 * 
 */
public class PolybiusSquare {
    /** Names of the keys (labels) of the rows and columns. */
    public static char[] keyName = { 'A', 'D', 'F', 'G', 'V', 'X' };

    /**
     * Helper class that defines an entry in the square.
     * 
     * @author Ben Ruijl
     * 
     */
    private class Entry {
        private final int col;
        private final int row;

        public Entry(final int col, final int row) {
            this.col = col;
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }
    }

    /** Values in the square. */
    private final char[][] square = new char[6][6];
    /**
     * The inverse of the square: a map from a character in the square to the
     * row and column index.
     */
    private final Map<Character, Entry> inverseSquare;

    /**
     * Maps a key name (label) to an index.
     * 
     * @param key
     *            Key in <code>keyName</code>
     * @return Index
     */
    static int keyNameToIndex(final char key) {
        switch (key) {
        case 'A':
            return 0;
        case 'D':
            return 1;
        case 'F':
            return 2;
        case 'G':
            return 3;
        case 'V':
            return 4;
        case 'X':
            return 5;
        default:
            return -1;
        }
    }

    /**
     * Creates a new Polybius square.
     * 
     * @param squareData
     *            Characters in the grid, specified row-wise.
     */
    public PolybiusSquare(final List<Character> squareData) {
        inverseSquare = new HashMap<Character, Entry>();

        for (int i = 0; i < squareData.size(); i++) {
            square[i / 6][i % 6] = squareData.get(i);
            inverseSquare.put(squareData.get(i), new Entry(i / 6, i % 6));
        }

    }

    /**
     * Unfractions the text by mapping the bigram of key names to the value in
     * the grid it represents. It finds the bigram by matching the correct row
     * and column key names.
     * 
     * @param row
     *            List of row key names
     * @param col
     *            List of column key names
     * @return Unfractioned text
     */
    public static String unFraction(final List<List<Character>> row,
            final List<List<Character>> col) {
        final StringBuffer buffer = new StringBuffer();

        /* Generate text from cols and rows */
        for (int j = 0; j < row.get(0).size(); j++) { // which row
            for (int i = 0; i < row.size(); i++) { // which col
                final int index = keyNameToIndex(row.get(i).get(j)) * 6
                        + keyNameToIndex(col.get(i).get(j));
                buffer.append(Encryption.plainAlphabet.charAt(index));
            }
        }

        return buffer.toString();
    }

    /**
     * Generates a random square, using the alphabet of
     * <code>plainAlphabet</code>
     * 
     * @return Random square
     * @see Encryption#plainAlphabet
     */
    public static PolybiusSquare generateRandomSquare() {
        final List<Character> squareData = new ArrayList<Character>();

        for (int i = 0; i < Encryption.plainAlphabet.length(); i++) {
            squareData.add(Encryption.plainAlphabet.charAt(i));
        }

        Collections.shuffle(squareData);

        return new PolybiusSquare(squareData);
    }

    /**
     * Fractions a text using the square.
     * 
     * @param input
     *            Input text
     * @return Fractioned text
     */
    public String fraction(final String input) {
        final StringBuffer result = new StringBuffer();

        for (int i = 0; i < input.length(); i++) {
            final Entry entry = inverseSquare.get(input.charAt(i));
            result.append(keyName[entry.getRow()]);
            result.append(keyName[entry.getCol()]);
        }

        return result.toString();
    }

    /**
     * Prints the square to the screen.
     */
    @Override
    public String toString() {
        final StringBuffer result = new StringBuffer("\n");

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                result.append(square[i][j] + " ");
            }

            result.append("\n");
        }

        return result.toString();
    }
}
