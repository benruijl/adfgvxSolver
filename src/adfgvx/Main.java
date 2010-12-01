package adfgvx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Main.class);
    private int correctAnalysis = 0;

    private String plainAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private Map<String, Double> tetagrams;
    private Map<IntArrayWrapper, Float> patternFreq;

    public Main(String cipherText, String pat, String tet) {
	patternFreq = new HashMap<IntArrayWrapper, Float>();
	tetagrams = new HashMap<String, Double>();

	// write pattern file
	/*
	 * try { writePattern(readCipher(cipherText)); } catch (IOException e) {
	 * e.printStackTrace(); }
	 */

	try {
	    readPatternTetagrams(pat);
	    readTetagrams(tet);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	// String enc = encrypt(cipherText);
	doHillclimbTestRun(cipherText);
    }

    public String readCipher(String filename) {
	String file;
	try {
	    file = new Scanner(new File(filename)).useDelimiter("\\Z").next();
	    file = file.replaceAll("[^A-Z]", "");
	    return file;
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

	return null;
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
    
    public void doHillclimbTestRun(String cipherFileName) {
	/* Read ciphertext. */
	String cipherText = readCipher(cipherFileName);
	Random r = new Random();
	int textLength = 100;
	int correct = 0;

	for (int i = 0; i < 100; i++) {
	    int start = r.nextInt(cipherText.length() - textLength);

	    String plainText = cipherText.substring(start, start
		    + textLength);
	    LOG.info("Plain text: " + plainText);

	    // encode
	    Map<Character, Character> encryptionAlphabet = randomAlphabet();
	    String encryptedText = transscribeCipherText(plainText, encryptionAlphabet);

	    float fitness = 0;
	    Map<Character, Character> bestAlphabet = new HashMap<Character, Character>();
	    for (int j = 0; j < 200; j++) {
		Map<Character, Character> newAlphabet = hillClimb(
			encryptedText, randomAlphabet());

		float newFitness = (float) fitness(encryptedText, newAlphabet);
		if (newFitness > fitness) {
		    fitness = newFitness;
		    bestAlphabet = newAlphabet;
		}
	    }

	    // Map<Character, Character> bestAlphabet = simmulatedAnnealing2(
	    // encryptedText, randomAlphabet(), 0.1, 0.6);
	    String answer = transscribeCipherText(encryptedText, bestAlphabet);
	    int count = 0;
	    for (Entry<Character, Character> entry : Utils.invert(encryptionAlphabet).entrySet()) {
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

    public String encrypt(String cipherFileName) {
	/* Read ciphertext. */
	String cipherText = readCipher(cipherFileName);

	Random r = new Random();

	for (int i = 0; i < 100; i++) {

	    PolybiusSquare square = PolybiusSquare.generateRandomSquare();
	    LOG.debug(square);

	    /* Generate an even keylength between 4 and 10. */
	    int keyLength = r.nextInt(4) * 2 + 2;
	    List<Integer> key = Grid.generateRandomKey(keyLength);
	    LOG.info("Key: " + key);

	    // Shrink ciphertext. It should be a multiple of the key length
	    int timesKeyLength = 200;
	    int start = r.nextInt(cipherText.length() - keyLength
		    * timesKeyLength);
	    String cipherTextPiece = cipherText.substring(start, start
		    + keyLength * timesKeyLength);
	    LOG.info("Plain text: " + cipherTextPiece);

	    String fractionedText = square.encode(cipherTextPiece);
	    Grid grid = new Grid(keyLength);
	    grid.add(fractionedText);

	    // doAnalysis(fractionedText, key);

	    grid.transpose(key);

	    String encryptedText = grid.encode();

	    LOG.debug(encryptedText);

	    doAnalysis(encryptedText, key);

	}

	LOG.info("Correct ones: " + correctAnalysis);

	return "";// encryptedText;
    }

    public int groupSimilarity(List<Map<Character, Integer>> freqs) {
	int score = 0;

	for (int i = 0; i < freqs.size() - 1; i++) {
	    for (int j = i; j < freqs.size(); j++) {
		score += similarity(freqs.get(i), freqs.get(j));
	    }
	}

	return score;
    }

    public int similarity(Map<Character, Integer> a, Map<Character, Integer> b) {
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

    public void findOptimalDistribution(List<Map<Character, Integer>> col,
	    List<Map<Character, Integer>> row) {
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

    public void doAnalysis(String cipher, List<Integer> key) {
	LOG.info("--------- BEGIN OF ENTRY");

	Grid grid = new Grid(key.size());
	grid.add(cipher);

	// calculate frequencies
	List<List<Character>> gridData = grid.getGrid();
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

	findOptimalDistribution(col, row);
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

	findOptimalPatternDistribution(charCol, charRow);

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
	    String encryptedText = mapBigramToMonogram(charRow, charCol);

	    float fitness = 0;
	    Map<Character, Character> bestAlphabet = new HashMap<Character, Character>();
	    for (int j = 0; j < 8; j++) {
		Map<Character, Character> newAlphabet = hillClimb(
			encryptedText, randomAlphabet());

		float newFitness = (float) fitness(encryptedText, newAlphabet);
		if (newFitness > fitness) {
		    fitness = newFitness;
		    bestAlphabet = newAlphabet;
		}
	    }

	    LOG.info("ANSWER: "
		    + transscribeCipherText(encryptedText, bestAlphabet));
	    correctAnalysis++;
	}

	LOG.info("--------- END OF ENTRY");
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

    public int patternFitness(List<List<Character>> col,
	    List<List<Character>> row) {

	Map<IntArrayWrapper, Float> patFreq = calcPatternFreq(mapBigramToMonogram(
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

	    patternFreq.put(pat, num + 1.0f / (float) (text.length() - 3));
	}

	return patternFreq;
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
		    + Math.log((float) entry.getValue() / (text.length() - 3))
		    + "\n");

	    out.writeDouble(Math.log(entry.getValue()
		    / (double) (text.length() - 3)));
	}

	out.close();
    }

    public String mapBigramToMonogram(List<List<Character>> row,
	    List<List<Character>> col) {
	StringBuffer buffer = new StringBuffer();

	/* Generate text from cols and rows */
	for (int j = 0; j < row.get(0).size(); j++) { // which row
	    for (int i = 0; i < row.size(); i++) { // which col
		int index = PolybiusSquare.keyNameToIndex(row.get(i).get(j))
			* 6 + PolybiusSquare.keyNameToIndex(col.get(i).get(j));
		buffer.append(plainAlphabet.charAt(index));
	    }
	}

	return buffer.toString();
    }

    public Map<Character, Character> simmulatedAnnealing(String cipherText,
	    Map<Character, Character> alphabet, double T, double a) {

	Random r = new Random();

	final double absZero = 0.00001;
	double fitness = fitness(cipherText, alphabet);
	
	@SuppressWarnings("unchecked")
	Entry<Character, Character>[] alphabetArray = alphabet
	.entrySet().toArray(new Entry[0]);

	while (T > absZero) {
	    boolean done = false;
	    double oldFitness = fitness;

	    for (int i = 0; i < alphabet.size() - 1; i++) {
		if (!done) {
		    for (int j = i + 1; j < alphabet.size(); j++) {
			Character tmp = alphabetArray[i].getValue();
			alphabetArray[i].setValue(alphabetArray[j].getValue());
			alphabetArray[j].setValue(tmp);

			fitness = fitness(cipherText, alphabet);
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
	double fitness = fitness(cipherText, alphabet);

	while (goAgain) {
	    goAgain = false;
	    double oldFitness = fitness;
	    
	    for (int i = 0; i < alphabet.size() - 1; i++) {
		if (!goAgain) {
		    for (int j = i + 1; j < alphabet.size(); j++) {

			Character tmp = alphabetArray[i].getValue();
			alphabetArray[i].setValue(alphabetArray[j].getValue());
			alphabetArray[j].setValue(tmp);

			fitness = fitness(cipherText, alphabet);

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

    /**
     * 
     * @param cipherText
     * @param alphabet
     *            Alphabet from ciphertext to plain
     * @return
     */
    public String transscribeCipherText(String cipherText,
	    Map<Character, Character> alphabet) {
	/* Transcribe ciphertext */
	char[] newTextArray = cipherText.toCharArray();
	for (int i = 0; i < newTextArray.length; i++) {
	    newTextArray[i] = alphabet.get(newTextArray[i]);
	}

	String newText = new String(newTextArray);

	return newText;
    }

    public void readTetagrams(String filename) throws IOException {
	InputStream file = new FileInputStream(filename);
	DataInputStream in = new DataInputStream(file);

	int size = in.readInt();

	for (int i = 0; i < size; i++) {
	    String tet = new String();
	    for (int j = 0; j < 4; j++) {
		tet += in.readChar();
	    }

	    Double freq = in.readDouble();
	    tetagrams.put(tet, freq);
	}

	LOG.info("Tetagrams read.");

	in.close();
    }
    
    public static double exp(double val) {
	    final long tmp = (long) (1512775 * val + (1072693248 - 60801));
	    return Double.longBitsToDouble(tmp << 32);
    }

    public double fitness(String cipherText, Map<Character, Character> alphabet) {
	String newText = transscribeCipherText(cipherText, alphabet);
	Map<String, Integer> cipherTetagrams = new HashMap<String, Integer>();

	//long time = System.nanoTime();
	for (int i = 0; i < newText.length() - 4; i++) {
	    String tet = newText.substring(i, i + 4);

	    Integer count = cipherTetagrams.get(tet);
	    if (count != null) {
		cipherTetagrams.put(tet, count + 1);
	    } else {
		cipherTetagrams.put(tet, 1);
	    }
	}
	//LOG.info(System.nanoTime() - time);
	
	double fitness = 0;
	double sigmaSquared = 4.0;
	for (Map.Entry<String, Integer> entry : cipherTetagrams.entrySet()) {
	    double sourceLogFreq = Double.POSITIVE_INFINITY;

	    Double freq = tetagrams.get(entry.getKey());
	    if (freq != null) {
		sourceLogFreq = freq;
	    } else {
		//continue;
	    }

	    Double logFreq = Math.log(entry.getValue()
		    / (double) (newText.length() - 3));

	    /* TODO: check if the factor in front of the exp is required. */
	    double exponent = -(logFreq - sourceLogFreq) * (logFreq - sourceLogFreq)
		    / (2.0 * sigmaSquared);
	    //LOG.info(exponent);
	    fitness += 1.0 / Math.sqrt(2 * Math.PI * sigmaSquared)
		    * exp(exponent);

	    // fitness += Math.round(logFreq) == Math.round(sourceLogFreq) ? 1 :
	    // 0;
	    // LOG.info(logFreq + " " + sourceLogFreq);
	    // fitness -= Math.abs(sourceLogFreq - logFreq);
	}

	return fitness;
    }

    public Map<Character, Character> randomAlphabet() {
	List<Character> alphabet = new ArrayList<Character>();

	for (int i = 0; i < plainAlphabet.length(); i++) {
	    alphabet.add(plainAlphabet.charAt(i));
	}

	Collections.shuffle(alphabet);

	Map<Character, Character> alphabetMap = new LinkedHashMap<Character, Character>();
	for (int i = 0; i < plainAlphabet.length(); i++) {
	    alphabetMap.put(plainAlphabet.charAt(i), alphabet.get(i));
	}

	return alphabetMap;
    }

    public static void main(String[] args) {
	BasicConfigurator.configure();
	LOG.setLevel(Level.INFO);
	LOG.info("Started");

	Options options = new Options();
	options.addOption("v", false, "Be verbose");
	options.addOption("c", true, "Cipher text");
	options.addOption("p", true, "Patterns");
	options.addOption("t", true, "Tetagrams");
	options.addOption("help", false, "Display help message");

	CommandLineParser parser = new GnuParser();
	try {
	    CommandLine cmd = parser.parse(options, args);

	    if (cmd.hasOption("help")) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("adfgvSolver", options);

		return;
	    }

	    if (cmd.hasOption("c") && cmd.hasOption("p") && cmd.hasOption("t")) {
		new Main(cmd.getOptionValue("c"), cmd.getOptionValue("p"),
			cmd.getOptionValue("t"));
	    } else {
		LOG.error("Please check input.");
	    }

	} catch (ParseException e) {
	    LOG.info("Could not parse arguments. Pass -help for help."
		    + e.getMessage());
	}

    }
}
