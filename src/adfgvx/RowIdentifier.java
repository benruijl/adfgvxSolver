package adfgvx;

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.Utils;

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
            final List<TObjectIntHashMap<Character>> freqs) {
        float score = 0;

        for (int i = 0; i < freqs.size() - 1; i++) {
            for (int j = i; j < freqs.size(); j++) {
                score += dissimilarity(freqs.get(i), freqs.get(j));
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
    private static int dissimilarity(final TObjectIntHashMap<Character> freqA,
            final TObjectIntHashMap<Character> freqB) {
        int score = 0;

        for (final Character key : PolybiusSquare.keyName) {
            int first = freqA.get(key);
            int second = freqB.get(key);

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
            final List<TObjectIntHashMap<Character>> col,
            final List<TObjectIntHashMap<Character>> row) {
        final List<TObjectIntHashMap<Character>> total = new ArrayList<TObjectIntHashMap<Character>>();
        total.addAll(col);
        total.addAll(row);

        // TODO: prevent checking everything twice (col,row=row,col)
        List<List<TObjectIntHashMap<Character>>> comb = Utils.combinations(total,
                total.size() / 2);

        float bestScore = Float.MAX_VALUE;
        List<TObjectIntHashMap<Character>> bestc = null;
        List<TObjectIntHashMap<Character>> bestr = null;
        for (List<TObjectIntHashMap<Character>> c : comb) {
            List<TObjectIntHashMap<Character>> r = Utils.complementary(total, c);

            final float score = (float) Math.sqrt(groupDissimilarity(c)
                    + groupDissimilarity(r));

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
