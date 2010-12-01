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

public class Pattern {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Pattern.class);
    private Map<IntArrayWrapper, Float> patternFreq;
    
    public Pattern(final String filename) throws IOException {
	patternFreq = new HashMap<IntArrayWrapper, Float>();
	readPatternTetagrams(filename);
    }

    public Map<IntArrayWrapper, Float> calcPatternFreq(String text) {
	Map<IntArrayWrapper, Float> patternFreq = new HashMap<IntArrayWrapper, Float>();

	for (int i = 0; i < text.length() - 4; i++) {
	    int[] initData = { 0, 1, 2, 3 };
	    IntArrayWrapper pat = new IntArrayWrapper(initData);

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

	    Float num = patternFreq.get(pat);

	    if (num == null) {
		num = 0.0f;
	    }

	    patternFreq.put(pat, num + 1.0f / (text.length() - 3));
	}

	return patternFreq;
    }
    
    public int patternFitness(List<List<Character>> col,
	    List<List<Character>> row) {

	Map<IntArrayWrapper, Float> patFreq = calcPatternFreq(PolybiusSquare.mapBigramToMonogram(
		row, col));

	return patternSimilarity(patternFreq, patFreq); // compare the
	// frequencies
    }

    public int patternSimilarity(Map<IntArrayWrapper, Float> a,
	    Map<IntArrayWrapper, Float> b) {
	int score = 0;

	for (IntArrayWrapper key : b.keySet()) {
	    Float first = a.get(key);
	    Float second = b.get(key);

	    if (first == null) {
		first = 0.0f;
	    }

	    score += (first - second) * (first - second);

	}

	return score;
    }
    
    public void findOptimalPatternDistribution(List<List<Character>> col,
	    List<List<Character>> row) {
	for (int i = 0; i < col.size() - 1; i++) {
	    for (int j = i; j < col.size(); j++) {
		int oldScore = patternFitness(col, row);
		Collections.swap(col, i, j);

		int newScore = patternFitness(col, row);

		if (newScore < oldScore) {
		    findOptimalPatternDistribution(col, row);
		} else {
		    Collections.swap(col, i, j); // swap back
		}
	    }
	}

	for (int i = 0; i < row.size() - 1; i++) {
	    for (int j = i; j < row.size(); j++) {
		int oldScore = patternFitness(col, row);
		Collections.swap(row, i, j);

		int newScore = patternFitness(col, row);

		if (newScore < oldScore) {
		    findOptimalPatternDistribution(col, row);
		} else {
		    Collections.swap(row, i, j); // swap back
		}
	    }
	}

    }

    public void readPatternTetagrams(String filename) throws IOException {
	InputStream file = new FileInputStream(filename);
	DataInputStream in = new DataInputStream(file);

	int size = in.readInt();

	for (int i = 0; i < size; i++) {
	    int[] tet = new int[4];
	    for (int j = 0; j < 4; j++) {
		tet[j] = in.readInt();
	    }

	    Float freq = (float) in.readDouble();
	    patternFreq.put(new IntArrayWrapper(tet), freq);
	}

	LOG.info("Pattern tetagrams read.");

	in.close();
    }

    public void writePattern(String text) throws IOException {
	Map<IntArrayWrapper, Float> patTet = calcPatternFreq(text);

	OutputStream fstream = new FileOutputStream("pat.dat");
	DataOutputStream out = new DataOutputStream(fstream);
	out.writeInt(patTet.size());

	/* Print the map */
	for (Entry<IntArrayWrapper, Float> entry : patTet.entrySet()) {
	    for (int i = 0; i < 4; i++) {
		System.out.print(entry.getKey().getData()[i]);
		out.writeInt(entry.getKey().getData()[i]);
	    }

	    System.out.print(" "
		    + Math.log(entry.getValue() / (text.length() - 3))
		    + "\n");

	    out.writeDouble(Math.log(entry.getValue()
		    / (double) (text.length() - 3)));
	}

	out.close();
    }
}
