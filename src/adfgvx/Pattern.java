package adfgvx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * This class provides functions to find the optimal arrangement of columns in
 * the transposition grid. Hopefully, this will solve the columnar
 * transposition. Before using these functions, the columns that map to
 * columns/rows in the Polybius square have to be identified,
 * 
 * To find the optimal arrangement, hillclimbing is performed. The fitness
 * function is a comparison of log pattern tetagram frequencies of the cipher
 * text to that of a large reference text.
 * 
 * 
 * @author Ben Ruijl
 * 
 */
public class Pattern {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Pattern.class);

    /** Pattern table of a large reference text. */
    private final Map<IntArrayWrapper, Double> patternFreq;

    /**
     * Reads a pattern table from a reference file.
     * 
     * @param filename
     *            Filename of the reference table
     * @throws IOException
     *             Exception when problems occur with reading the reference file
     */
    public Pattern(final String filename) throws IOException {
        patternFreq = new HashMap<IntArrayWrapper, Double>();
        readPatternTetagrams(filename);
    }

    /**
     * Calculates the frequency table of tetagrams in a given text.
     * 
     * @param text
     *            Text to scan
     * @return Map with frequencies
     */
    public Map<IntArrayWrapper, Double> calcPatternFrequencies(final String text) {
        final Map<IntArrayWrapper, Double> patternFreq = new HashMap<IntArrayWrapper, Double>();

        for (int i = 0; i < text.length() - 4; i++) {
            final int[] initData = { 0, 1, 2, 3 };
            final IntArrayWrapper pat = new IntArrayWrapper(initData);

            int highest = 0;
            for (int j = 0; j < 4; j++) {
                boolean found = false;

                for (int k = 0; k < j; k++) {
                    if (text.charAt(i + j) == text.charAt(i + k)) {
                        pat.getData()[j] = k;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    highest++;
                    pat.getData()[j] = highest;
                }
            }

            Double num = patternFreq.get(pat);

            if (num == null) {
                num = 0.0;
            }

            patternFreq.put(pat, num + 1.0f / (text.length() - 3));
        }

        return patternFreq;
    }

    /**
     * Calculates the fitness of this combination of rows and columns by
     * reconstructing the text and comparing the pattern tetagram frequencies to
     * those of a large reference text.
     * 
     * @param col
     *            List of transposition grid columns that map to columns in the
     *            Polybius square
     * @param row
     *            List of transposition grid columns that map to columns in the
     *            Polybius square
     * @return Fitness of this arrangement
     */
    public int patternFitness(final List<List<Character>> col,
            final List<List<Character>> row) {

        final Map<IntArrayWrapper, Double> patFreq = calcPatternFrequencies(PolybiusSquare
                .unFraction(row, col));

        return patternDissimilarity(patternFreq, patFreq); // compare the
        // frequencies
    }

    /**
     * Calculates how dissimilar two patterns are. A lower value means a
     * <b>higher</b> similarity.
     * 
     * @param freqA
     *            Frequency table of first text
     * @param freqBFrequency
     *            table of second text
     * @return Dissimilarity between two patterns expressed as an integer
     */
    public int patternDissimilarity(final Map<IntArrayWrapper, Double> freqA,
            final Map<IntArrayWrapper, Double> freqB) {
        int score = 0;

        for (final IntArrayWrapper key : freqB.keySet()) {
            Double first = freqA.get(key);
            final Double second = freqB.get(key);

            if (first == null) {
                first = 0.0;
            }

            score += (first - second) * (first - second);

        }

        return score;
    }

    /**
     * Tries to find the optimal arrangement of columns and rows so that the
     * pattern frequencies of the resulting text are as close to the reference
     * text as possible.
     * 
     * @param col
     *            List of transposition grid columns that map to columns in the
     *            Polybius square
     * @param row
     *            List of transposition grid columns that map to rows in the
     *            Polybius square
     */
    public void findOptimalPatternDistribution(final List<List<Character>> col,
            final List<List<Character>> row) {
        for (int i = 0; i < col.size() - 1; i++) {
            for (int j = i; j < col.size(); j++) {
                final int oldScore = patternFitness(col, row);
                Collections.swap(col, i, j);

                final int newScore = patternFitness(col, row);

                if (newScore < oldScore) {
                    findOptimalPatternDistribution(col, row);
                } else {
                    Collections.swap(col, i, j); // swap back
                }
            }
        }

        for (int i = 0; i < row.size() - 1; i++) {
            for (int j = i; j < row.size(); j++) {
                final int oldScore = patternFitness(col, row);
                Collections.swap(row, i, j);

                final int newScore = patternFitness(col, row);

                if (newScore < oldScore) {
                    findOptimalPatternDistribution(col, row);
                } else {
                    Collections.swap(row, i, j); // swap back
                }
            }
        }

    }

    /**
     * Reads the log pattern tetagram frequencies from a file.
     * 
     * @param filename
     *            Filename
     * @throws IOException
     *             Error while reading from file
     */
    public void readPatternTetagrams(final String filename) throws IOException {
        final InputStream file = new FileInputStream(filename);
        final DataInputStream in = new DataInputStream(file);

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            final int[] tet = new int[4];
            for (int j = 0; j < 4; j++) {
                tet[j] = in.readInt();
            }

            patternFreq.put(new IntArrayWrapper(tet), in.readDouble());
        }

        LOG.info("Pattern tetagrams read.");

        in.close();
    }

    /**
     * Writes the pattern table to a file.
     * 
     * @param filename
     *            Name of output file
     * @param text
     *            Text to calculate log tetagram pattern frequencies from
     * @throws IOException
     *             Error while writing to a file
     */
    public void writePattern(final String filename, final String text)
            throws IOException {
        final Map<IntArrayWrapper, Double> patTet = calcPatternFrequencies(text);

        final OutputStream fstream = new FileOutputStream("pat.dat");
        final DataOutputStream out = new DataOutputStream(fstream);
        out.writeInt(patTet.size());

        /* Print the map */
        for (final Entry<IntArrayWrapper, Double> entry : patTet.entrySet()) {
            for (int i = 0; i < 4; i++) {
                System.out.print(entry.getKey().getData()[i]);
                out.writeInt(entry.getKey().getData()[i]);
            }

            System.out.print(" "
                    + Math.log(entry.getValue() / (text.length() - 3)) + "\n");

            out.writeDouble(Math.log(entry.getValue() / (text.length() - 3)));
        }

        out.close();
    }
}
