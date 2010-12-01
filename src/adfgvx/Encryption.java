package adfgvx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Encryption {
    static public String plainAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    
    /**
     * 
     * @param cipherText
     * @param alphabet
     *            Alphabet from ciphertext to plaintext
     * @return
     */
    public static String transscribeCipherText(String cipherText,
	    Map<Character, Character> alphabet) {
	/* Transcribe ciphertext */
	char[] newTextArray = cipherText.toCharArray();
	for (int i = 0; i < newTextArray.length; i++) {
	    newTextArray[i] = alphabet.get(newTextArray[i]);
	}

	String newText = new String(newTextArray);

	return newText;
    }

    public static Map<Character, Character> randomAlphabet() {
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
}
