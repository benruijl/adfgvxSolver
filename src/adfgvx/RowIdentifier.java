package adfgvx;

import java.util.List;
import java.util.Map;

/**
 * This class provides functions that help indentify which columns in the
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
    private static int groupDissimilarity(List<Map<Character, Integer>> freqs) {
	int score = 0;

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
    private static int dissimilarity(Map<Character, Integer> freqA,
	    Map<Character, Integer> freqB) {
	int score = 0;

	for (Character key : PolybiusSquare.keyName) {
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
	    List<Map<Character, Integer>> col, List<Map<Character, Integer>> row) {
	// lowest score is best

	for (int i = 0; i < col.size() - 1; i++) {
	    for (int j = i; j < col.size(); j++) {
		int curScore = groupDissimilarity(col)
			+ groupDissimilarity(row);

		Map<Character, Integer> temp = col.get(i);
		col.set(i, row.get(j));
		row.set(j, temp);

		int score = groupDissimilarity(col) + groupDissimilarity(row);

		if (score < curScore) {
		    findOptimalGrouping(col, row);
		} else {
		    temp = col.get(i);
		    col.set(i, row.get(j));
		    row.set(j, temp);
		}
	    }
	}

    }
}
