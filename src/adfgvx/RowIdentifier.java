package adfgvx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides functions that help identify which columns in the
 * transposition grid are columns/rows in the Polybius square.
 * 
 * @author Ben Ruijl
 * 
 */
public class RowIdentifier {

    /**
     * Finds the dissimilarity in a group of frequencies.
     * 
     * @param freqs
     *            Frequencies
     * @return The amount of dissimilarity
     */
    private static float groupDissimilarity(
            final List<Map<Character, Integer>> freqs) {
        float score = 0;

        for (int i = 0; i < freqs.size() - 1; i++) {
            for (int j = i; j < freqs.size(); j++) {
                score += Math.sqrt(dissimilarity(freqs.get(i), freqs.get(j)));
            }
        }

        return score;
    }

    /**
     * Finds the amount of dissimilarity between two frequencies.
     * 
     * @param freqA
     *            Frequency of a column
     * @param freqB
     *            Frequency of a column
     * @return The amount of dissimilarity
     */
    private static int dissimilarity(final Map<Character, Integer> freqA,
            final Map<Character, Integer> freqB) {
        int score = 0;

        for (final Character key : PolybiusSquare.keyName) {
            Integer first = freqA.get(key);
            Integer second = freqB.get(key);

            if (first == null) {
                first = 0;
            }

            if (second == null) {
                second = 0;
            }

            score += (first - second) * (first - second);

        }

        return score;
    }

    /**
     * Finds the optimal grouping of Polybius square rows and columns.
     * 
     * @param col
     *            Column that is a column in the square
     * @param row
     *            Column that is a row in the square
     */
    public static void findOptimalGrouping(
            final List<Map<Character, Integer>> col,
            final List<Map<Character, Integer>> row) {
        final List<Map<Character, Integer>> total = new ArrayList<Map<Character, Integer>>();
        total.addAll(col);
        total.addAll(row);

        // TODO: prevent checking everything twice (col,row=row,col)
        List<List<Map<Character, Integer>>> comb = Utils.combinations(total,
                total.size() / 2);

        float bestScore = Float.MAX_VALUE;
        List<Map<Character, Integer>> bestc = null;
        List<Map<Character, Integer>> bestr = null;
        for (List<Map<Character, Integer>> c : comb) {
            List<Map<Character, Integer>> r = Utils.complementary(total, c);

            final float score = (float) Math.sqrt(groupDissimilarity(c) + groupDissimilarity(r));

            if (score < bestScore) {
                bestScore = score;
                bestc = c;
                bestr = r;
            }
        }

        col.clear();
        col.addAll(bestc);
        row.clear();
        row.addAll(bestr);
    }
}
