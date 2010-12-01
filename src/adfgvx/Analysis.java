package adfgvx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class Analysis {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Analysis.class);

    private final Random random;
    private final Pattern pattern;
    private final Tetagram tetagram;

    private int correctAnalysis = 0;

    public Analysis(Pattern pattern, Tetagram tetagram) {
	this.pattern = pattern;
	this.tetagram = tetagram;

	random = new Random();
    }

    public void doAnalysis(String text) {
	PolybiusSquare square = PolybiusSquare.generateRandomSquare();
	LOG.debug(square);

	/* Generate an even keylength between 4 and 10. */
	int keyLength = random.nextInt(4) * 2 + 2;
	List<Integer> key = Grid.generateRandomKey(keyLength);
	LOG.info("Key: " + key);

	// Shrink ciphertext. It should be a multiple of the key length
	int timesKeyLength = 200;
	int start = random.nextInt(text.length() - keyLength
		* timesKeyLength);
	String cipherTextPiece = text.substring(start, start + keyLength
		* timesKeyLength);
	LOG.info("Plain text: " + cipherTextPiece);

	String fractionedText = square.encode(cipherTextPiece);
	Grid grid = new Grid(keyLength);
	grid.add(fractionedText);

	grid.transpose(key);

	String encryptedText = grid.encode();

	LOG.debug(encryptedText);

	LOG.info("--------- BEGINNING OF DECRYPTION");

	Grid cipherGrid = new Grid(key.size());
	cipherGrid.add(encryptedText);

	// calculate frequencies
	List<List<Character>> gridData = cipherGrid.getGrid();
	List<Map<Character, Integer>> freqs = new ArrayList<Map<Character, Integer>>();

	for (int i = 0; i < gridData.size(); i++) {
	    freqs.add(new HashMap<Character, Integer>());
	    Map<Character, Integer> freq = freqs.get(i);

	    for (Character c : PolybiusSquare.keyName) {
		freq.put(c, 0);
	    }

	    List<Character> col = gridData.get(i);

	    for (int j = 0; j < col.size(); j++) {
		Character curKey = col.get(j);
		freq.put(curKey, freq.get(curKey) + 1);
	    }

	    LOG.info(freq);
	}

	LOG.info("After sorting:");

	List<Map<Character, Integer>> col = new ArrayList<Map<Character, Integer>>();
	List<Map<Character, Integer>> row = new ArrayList<Map<Character, Integer>>();

	for (int i = 0; i < freqs.size(); i++) {
	    if (i < freqs.size() / 2) {
		col.add(freqs.get(i));
	    } else {
		row.add(freqs.get(i));
	    }
	}

	RowIdentifier.findOptimalDistribution(col, row);
	LOG.info(col);
	LOG.info(row);

	int invKey[] = new int[key.size()];
	for (int i = 0; i < key.size(); i++) {
	    invKey[key.get(i)] = i;
	}

	int correct = 0;
	for (int i = 0; i < key.size(); i++) {
	    if (col.contains(freqs.get(i)) && invKey[i] % 2 == 1) {
		correct++;
	    }

	    if (row.contains(freqs.get(i)) && invKey[i] % 2 == 0) {
		correct++;
	    }
	}

	int correctTrans = 0;
	for (int i = 0; i < key.size(); i++) {
	    if (col.contains(freqs.get(i)) && invKey[i] % 2 == 0) {
		correctTrans++;
	    }

	    if (row.contains(freqs.get(i)) && invKey[i] % 2 == 1) {
		correctTrans++;
	    }
	}

	LOG.info("Correct identification of rows and cols: "
		+ Math.max(correct, correctTrans) + "/" + key.size());

	// match pattern
	List<List<Character>> charCol = new ArrayList<List<Character>>();
	List<List<Character>> charRow = new ArrayList<List<Character>>();

	for (int i = 0; i < gridData.size(); i++) {
	    if (col.contains(freqs.get(i))) {
		charCol.add(gridData.get(i));
	    } else {
		charRow.add(gridData.get(i));
	    }
	}

	pattern.findOptimalPatternDistribution(charCol, charRow);

	// see if it is correct
	correct = 0;
	for (int i = 0; i < key.size(); i++) {
	    if (i % 2 == 0 && charCol.get(i / 2) == gridData.get(key.get(i))) {
		correct++;
	    }

	    if (i % 2 == 1 && charRow.get(i / 2) == gridData.get(key.get(i))) {
		correct++;
	    }
	}

	correctTrans = 0;
	for (int i = 0; i < key.size(); i++) {
	    if (i % 2 == 1 && charCol.get(i / 2) == gridData.get(key.get(i))) {
		correctTrans++;
	    }

	    if (i % 2 == 0 && charRow.get(i / 2) == gridData.get(key.get(i))) {
		correctTrans++;
	    }
	}

	LOG.info("Correct transposition grid after pattern check: "
		+ Math.max(correct, correctTrans) + "/" + key.size());

	if (Math.max(correct, correctTrans) == key.size()) {
	    // transposition grid is correct, now do mono sub solving
	    String monoSubText = PolybiusSquare.mapBigramToMonogram(charRow,
		    charCol);

	    float fitness = 0;
	    Map<Character, Character> bestAlphabet = new HashMap<Character, Character>();
	    for (int j = 0; j < 8; j++) {
		Map<Character, Character> newAlphabet = hillClimb(
			monoSubText, Encryption.randomAlphabet());

		float newFitness = (float) tetagram.fitness(monoSubText,
			newAlphabet);
		if (newFitness > fitness) {
		    fitness = newFitness;
		    bestAlphabet = newAlphabet;
		}
	    }

	    LOG.info("ANSWER: "
		    + Encryption.transscribeCipherText(monoSubText,
			    bestAlphabet));
	    correctAnalysis++;
	}

	LOG.info("--------- END OF DECRYPTION");
    }

    public Map<Character, Character> simmulatedAnnealing(String cipherText,
	    Map<Character, Character> alphabet, double T, double a) {

	Random r = new Random();

	final double absZero = 0.00001;
	double fitness = tetagram.fitness(cipherText, alphabet);

	@SuppressWarnings("unchecked")
	Entry<Character, Character>[] alphabetArray = alphabet.entrySet()
		.toArray(new Entry[0]);

	while (T > absZero) {
	    boolean done = false;
	    double oldFitness = fitness;

	    for (int i = 0; i < alphabet.size() - 1; i++) {
		if (!done) {
		    for (int j = i + 1; j < alphabet.size(); j++) {
			Character tmp = alphabetArray[i].getValue();
			alphabetArray[i].setValue(alphabetArray[j].getValue());
			alphabetArray[j].setValue(tmp);

			fitness = tetagram.fitness(cipherText, alphabet);
			// LOG.info(fitness - oldFitness);

			if (fitness > oldFitness
				|| Math.exp((fitness - oldFitness) / T) > r
					.nextDouble()) {
			    done = true;
			    break;
			} else {
			    // swap back
			    alphabetArray[j].setValue(alphabetArray[i]
				    .getValue());
			    alphabetArray[i].setValue(tmp);

			}
		    }
		}
	    }

	    T = T * a;
	}

	return alphabet;
    }

    public Map<Character, Character> hillClimb(String cipherText,
	    Map<Character, Character> alphabet) {

	@SuppressWarnings("unchecked")
	Entry<Character, Character>[] alphabetArray = alphabet.entrySet()
		.toArray(new Entry[0]);

	boolean goAgain = true;
	double fitness = tetagram.fitness(cipherText, alphabet);

	while (goAgain) {
	    goAgain = false;
	    double oldFitness = fitness;

	    for (int i = 0; i < alphabet.size() - 1; i++) {
		if (!goAgain) {
		    for (int j = i + 1; j < alphabet.size(); j++) {

			Character tmp = alphabetArray[i].getValue();
			alphabetArray[i].setValue(alphabetArray[j].getValue());
			alphabetArray[j].setValue(tmp);

			fitness = tetagram.fitness(cipherText, alphabet);

			if (fitness > oldFitness) {
			    goAgain = true;
			    break;
			} else {
			    // swap back
			    alphabetArray[j].setValue(alphabetArray[i]
				    .getValue());
			    alphabetArray[i].setValue(tmp);
			}
		    }
		}
	    }
	}

	return alphabet;
    }

    public Map<Character, Character> guessAlphabet(String text) {
	String letterFreqs = "ETAOINHSRDLMUWYCFGPBVKXJQZ0123456789";
	Map<Character, Integer> freq = new HashMap<Character, Integer>();

	for (int i = 0; i < text.length(); i++) {
	    if (freq.containsKey(text.charAt(i))) {
		freq.put(text.charAt(i), freq.get(text.charAt(i)) + 1);
	    } else {
		freq.put(text.charAt(i), 1);
	    }
	}

	ArrayList<Entry<Character, Integer>> list = new ArrayList<Entry<Character, Integer>>(
		freq.entrySet());
	Collections.sort(list, new Comparator<Entry<Character, Integer>>() {
	    @Override
	    public int compare(Entry<Character, Integer> o1,
		    Entry<Character, Integer> o2) {
		return o1.getValue().compareTo(o2.getValue());
	    }
	});

	Map<Character, Character> alphabet = new HashMap<Character, Character>();

	for (int i = 0; i < list.size(); i++) {
	    alphabet.put(list.get(i).getKey(), letterFreqs.charAt(i));
	}

	return alphabet;
    }

    public void doHillclimbTestRun(String text) {
	Random r = new Random();
	int textLength = 100;
	int correct = 0;

	for (int i = 0; i < 100; i++) {
	    int start = r.nextInt(text.length() - textLength);

	    String plainText = text.substring(start, start + textLength);
	    LOG.info("Plain text: " + plainText);

	    // encode
	    Map<Character, Character> encryptionAlphabet = Encryption
		    .randomAlphabet();
	    String encryptedText = Encryption.transscribeCipherText(plainText,
		    encryptionAlphabet);

	    float fitness = 0;
	    Map<Character, Character> bestAlphabet = new HashMap<Character, Character>();
	    for (int j = 0; j < 200; j++) {
		Map<Character, Character> newAlphabet = hillClimb(
			encryptedText, Encryption.randomAlphabet());

		float newFitness = (float) tetagram.fitness(encryptedText,
			newAlphabet);
		if (newFitness > fitness) {
		    fitness = newFitness;
		    bestAlphabet = newAlphabet;
		}
	    }

	    // Map<Character, Character> bestAlphabet = simmulatedAnnealing2(
	    // encryptedText, randomAlphabet(), 0.1, 0.6);
	    String answer = Encryption.transscribeCipherText(encryptedText,
		    bestAlphabet);
	    int count = 0;
	    for (Entry<Character, Character> entry : Utils.invert(
		    encryptionAlphabet).entrySet()) {
		if (entry.getValue().equals(bestAlphabet.get(entry.getKey()))) {
		    count++;
		}
	    }
	    LOG.info("ANSWER: " + answer + " " + count + " correct");

	    if (answer.equals(plainText)) {
		correct++;
	    }
	}

	LOG.info("Correct decryptions: " + correct);
    }

}
