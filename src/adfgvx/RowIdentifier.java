package adfgvx;

import java.util.List;
import java.util.Map;

public class RowIdentifier {
    private static int groupSimilarity(List<Map<Character, Integer>> freqs) {
	int score = 0;

	for (int i = 0; i < freqs.size() - 1; i++) {
	    for (int j = i; j < freqs.size(); j++) {
		score += similarity(freqs.get(i), freqs.get(j));
	    }
	}

	return score;
    }

    private static int similarity(Map<Character, Integer> a,
	    Map<Character, Integer> b) {
	int score = 0;

	for (Character key : PolybiusSquare.keyName) {
	    Integer first = a.get(key);
	    Integer second = b.get(key);

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

    public static void findOptimalDistribution(
	    List<Map<Character, Integer>> col, List<Map<Character, Integer>> row) {
	// lowest score is best

	for (int i = 0; i < col.size() - 1; i++) {
	    for (int j = i; j < col.size(); j++) {
		int curScore = groupSimilarity(col) + groupSimilarity(row);

		Map<Character, Integer> temp = col.get(i);
		col.set(i, row.get(j));
		row.set(j, temp);

		int score = groupSimilarity(col) + groupSimilarity(row);

		if (score < curScore) {
		    findOptimalDistribution(col, row);
		} else {
		    temp = col.get(i);
		    col.set(i, row.get(j));
		    row.set(j, temp);
		}
	    }
	}

    }
}
