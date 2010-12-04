package adfgvx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides functions that are used in the encryption and decryption
 * fase of the cipher.
 * 
 * @author Ben Ruijl
 * 
 */
public class Encryption {
    /** The reference alphabet (English). */
    public static String plainAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    /**
     * Transcribes a cipher text to a new one using the <code>alphabet</code>
     * for the substitution map.
     * 
     * @param cipherText
     *            Cipher text
     * @param alphabet
     *            Alphabet from cipher text to plain text
     * @return Transcribed text
     */
    public static String transcribeCipherText(final String cipherText,
            final Map<Character, Character> alphabet) {
        /* Transcribe ciphertext */
        final char[] newTextArray = cipherText.toCharArray();
        for (int i = 0; i < newTextArray.length; i++) {
            newTextArray[i] = alphabet.get(newTextArray[i]);
        }

        final String newText = new String(newTextArray);

        return newText;
    }

    /**
     * Generates a random alphabet. It uses <code>plainAlphabet</code> to get
     * all the characters.
     * 
     * @return Random alphabet
     */
    public static Map<Character, Character> randomAlphabet() {
        final List<Character> alphabet = new ArrayList<Character>();

        for (int i = 0; i < plainAlphabet.length(); i++) {
            alphabet.add(plainAlphabet.charAt(i));
        }

        Collections.shuffle(alphabet);

        final Map<Character, Character> alphabetMap = new HashMap<Character, Character>();
        for (int i = 0; i < plainAlphabet.length(); i++) {
            alphabetMap.put(plainAlphabet.charAt(i), alphabet.get(i));
        }

        return alphabetMap;
    }
}
