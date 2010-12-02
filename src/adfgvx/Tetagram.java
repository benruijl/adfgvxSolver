package adfgvx;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class delivers functions to solve a monoalphabetic substitution cipher.
 * It uses log frequencies of tetagrams to determine how fit a solution is. The
 * fitness function can be used in hill-climbing.
 * 
 * @author Ben Ruijl
 * 
 */
public class Tetagram {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Tetagram.class);
    /** Table of log tetagram frequencies. */
    private final Map<String, Double> tetagrams;

    /**
     * Creates an instance of Tetagram, which contains a reference frequency
     * table.
     * 
     * @param filename
     *            Filename of the reference frequency table
     * @throws IOException
     *             Read error
     */
    public Tetagram(final String filename) throws IOException {
        tetagrams = new HashMap<String, Double>();
        readTetagrams(filename);
    }

    /**
     * Reads a log frequency table from a reference text.
     * 
     * @param filename
     *            Filename of the source text
     * @throws IOException
     *             Read error
     */
    public void readTetagrams(final String filename) throws IOException {
        final InputStream file = new FileInputStream(filename);
        final DataInputStream in = new DataInputStream(file);

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            String tet = new String();
            for (int j = 0; j < 4; j++) {
                tet += in.readChar();
            }

            final Double freq = in.readDouble();
            tetagrams.put(tet, freq);
        }

        LOG.info("Tetagrams read.");

        in.close();
    }

    /**
     * Calculates the fitness of a certain substitution of text.
     * 
     * @param cipherText
     *            Cipher text
     * @param alphabet
     *            Alphabet map from cipher text to certain solution
     * @return Fitness expressed as a number
     */
    public double fitness(final String cipherText,
            final Map<Character, Character> alphabet) {
        final String newText = Encryption.transcribeCipherText(cipherText,
                alphabet);
        final Map<String, Integer> cipherTetagrams = new HashMap<String, Integer>();

        // long time = System.nanoTime();
        for (int i = 0; i < newText.length() - 4; i++) {
            final String tet = newText.substring(i, i + 4);

            final Integer count = cipherTetagrams.get(tet);
            if (count != null) {
                cipherTetagrams.put(tet, count + 1);
            } else {
                cipherTetagrams.put(tet, 1);
            }
        }
        // LOG.info(System.nanoTime() - time);

        double fitness = 0;
        final double sigmaSquared = 4.0;
        for (final Map.Entry<String, Integer> entry : cipherTetagrams
                .entrySet()) {
            double sourceLogFreq = Double.POSITIVE_INFINITY;

            final Double freq = tetagrams.get(entry.getKey());
            if (freq != null) {
                sourceLogFreq = freq;
            } else {
                // continue;
            }

            final Double logFreq = Math.log(entry.getValue()
                    / (double) (newText.length() - 3));

            /* TODO: check if the factor in front of the exp is required. */
            final double exponent = -(logFreq - sourceLogFreq)
                    * (logFreq - sourceLogFreq) / (2.0 * sigmaSquared);
            // LOG.info(exponent);
            fitness += 1.0 / Math.sqrt(2 * Math.PI * sigmaSquared)
                    * Utils.exp(exponent);

            // fitness += Math.round(logFreq) == Math.round(sourceLogFreq) ? 1 :
            // 0;
            // LOG.info(logFreq + " " + sourceLogFreq);
            // fitness -= Math.abs(sourceLogFreq - logFreq);
        }

        return fitness;
    }
}
