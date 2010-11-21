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
import java.util.HashMap;
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
import org.apache.log4j.Logger;

public class Main {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Main.class);
    private int correctAnalysis = 0;

    private Map<Integer[], Float> patternFreq;

    public Main(String cipherText, String pat) {
        patternFreq = new HashMap<Integer[], Float>();
        
        try {
            readPatternTetagrams(pat);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String enc = encrypt(cipherText);
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

    public String encrypt(String cipherFileName) {
        /* Read ciphertext. */
        String cipherText = readCipher(cipherFileName);
        
        // write pattern file
        try {
            writePattern(cipherText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Random r = new Random();

        for (int i = 0; i < 10000; i++) {

            PolybiusSquare square = PolybiusSquare.generateRandomSquare();
            LOG.debug(square);

            /* Generate an even keylength between 4 and 10. */
            int keyLength = r.nextInt(4) * 2 + 2;
            List<Integer> key = Grid.generateRandomKey(keyLength);
            LOG.info(key);

            // Shrink ciphertext. It should be a multiple of the key length
            int timesKeyLength = 40;
            int start = r.nextInt(cipherText.length() - keyLength
                    * timesKeyLength);
            String cipherTextPiece = cipherText.substring(start, start
                    + keyLength * timesKeyLength);
            LOG.debug(cipherTextPiece);

            String fractionedText = square.encode(cipherTextPiece);
            Grid grid = new Grid(keyLength);
            grid.add(fractionedText);

            doAnalysis(fractionedText, key);

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

            // print
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

        LOG.info("Correct (if transposed): " + Math.max(correct, correctTrans)
                + "/" + key.size());

        // match pattern
        List<List<Character>> charCol = new ArrayList<List<Character>>();
        List<List<Character>> charRow = new ArrayList<List<Character>>();
        findOptimalPatternDistribution(col, row);

        // see if it is correct
        correct = 0;
        for (int i = 0; i < key.size(); i++) {
            if (i % 2 == 0 && col.get(i / 2) == freqs.get(invKey[i])) {
                correct++;
            }

            if (i % 2 == 1 && row.get(i / 2) == freqs.get(invKey[i])) {
                correct++;
            }
        }

        correctTrans = 0;
        for (int i = 0; i < key.size(); i++) {
            if (i % 2 == 1 && col.get(i / 2) == freqs.get(invKey[i])) {
                correctTrans++;
            }

            if (i % 2 == 0 && row.get(i / 2) == freqs.get(invKey[i])) {
                correctTrans++;
            }
        }

        LOG.info("Correct after pattern check: "
                + Math.max(correct, correctTrans) + "/" + key.size());

        if (Math.max(correct, correctTrans) == key.size()) {
            correctAnalysis++;
        }
    }

    public void findOptimalPatternDistribution(
            List<List<Character>> col, List<List<Character>> row) {

    }

    public int patternSimilarity(Map<Integer[], Float> a,
            Map<Integer[], Float> b) {
        int score = 0;

        for (Integer[] key : b.keySet()) {
            Float first = a.get(key);
            Float second = b.get(key);

            if (first == null) {
                first = 0.0f;
            }

            score += (first - second) * (first - second);

        }

        return score;
    }

    public Map<Integer[], Float> calcPatternFreq(String text) {
        Map<Integer[], Float> patternFreq = new HashMap<Integer[], Float>();

        for (int i = 0; i < text.length() - 4; i++) {
            Integer pat[] = { 0, 1, 2, 3 };

            int highest = 0;
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < j; k++) {
                    if (text.charAt(i + j) == text.charAt(i + k)) {
                        pat[j] = k;
                        break;
                    }

                    pat[j] = highest;
                    highest++;
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
            Integer[] tet = new Integer[4];
            for (int j = 0; j < 4; j++) {
                tet[j] = in.readInt();
            }

            Float freq = (float) in.readDouble();
            patternFreq.put(tet, freq);
        }

        System.out.println("Tetagrams read.");

        in.close();
    }

    public void writePattern(String text) throws IOException {
        Map<Integer[], Float> patTet = calcPatternFreq(text);

        OutputStream fstream = new FileOutputStream("pat.dat");
        DataOutputStream out = new DataOutputStream(fstream);
        out.writeInt(patTet.size());

        /* Print the map */
        for (Entry<Integer[], Float> entry : patTet.entrySet()) {
            System.out.println(entry.getKey() + " " + (float) entry.getValue()
                    / (text.length() - 3));

            for (int i = 0; i < 4; i++) {
                out.writeInt(entry.getKey()[i]);
            }

            out.writeDouble(Math.log(entry.getValue()
                    / (double) (text.length() - 3)));
        }

        out.close();
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        LOG.info("Started");

        Options options = new Options();
        options.addOption("v", false, "Be verbose");
        options.addOption("c", true, "Cipher text");
        options.addOption("p", true, "Pattern");
        options.addOption("help", false, "Display help message");

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("adfgvSolver", options);

                return;
            }

            if (cmd.hasOption("c") && cmd.hasOption("p")) {
                new Main(cmd.getOptionValue("c"), cmd.getOptionValue("p"));
            } else {
                LOG.error("Please check input.");
            }

        } catch (ParseException e) {
            LOG.info("Could not parse arguments. Pass -help for help."
                    + e.getMessage());
        }

    }
}
