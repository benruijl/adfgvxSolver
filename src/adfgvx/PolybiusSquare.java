package adfgvx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolybiusSquare {
    public static char[] keyName = { 'A', 'D', 'F', 'G', 'V', 'X' };

    private class Entry {
	private final int col;
	private final int row;

	public Entry(int col, int row) {
	    this.col = col;
	    this.row = row;
	}

	public int getCol() {
	    return col;
	}

	public int getRow() {
	    return row;
	}
    }

    private char[][] square = new char[6][6];
    private Map<Character, Entry> inverseSquare;

    static int keyNameToIndex(char key) {
	switch (key) {
	case 'A':
	    return 0;
	case 'D':
	    return 1;
	case 'F':
	    return 2;
	case 'G':
	    return 3;
	case 'V':
	    return 4;
	case 'X':
	    return 5;
	default:
	    return -1;
	}
    }

    public PolybiusSquare(List<Character> squareData) {
	inverseSquare = new HashMap<Character, Entry>();

	for (int i = 0; i < squareData.size(); i++) {
	    square[i / 6][i % 6] = squareData.get(i);
	    inverseSquare.put(squareData.get(i), new Entry(i / 6, i % 6));
	}

    }

    public static PolybiusSquare generateRandomSquare() {
	String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	List<Character> squareData = new ArrayList<Character>();

	for (int i = 0; i < alphabet.length(); i++) {
	    squareData.add(alphabet.charAt(i));
	}

	Collections.shuffle(squareData);

	return new PolybiusSquare(squareData);
    }

    public String encode(String input) {
	StringBuffer result = new StringBuffer();

	for (int i = 0; i < input.length(); i++) {
	    Entry entry = inverseSquare.get(input.charAt(i));
	    result.append(keyName[entry.getRow()]);
	    result.append(keyName[entry.getCol()]);
	}

	return result.toString();
    }

    @Override
    public String toString() {
	StringBuffer result = new StringBuffer("\n");

	for (int i = 0; i < 6; i++) {
	    for (int j = 0; j < 6; j++) {
		result.append(square[i][j] + " ");
	    }

	    result.append("\n");
	}

	return result.toString();
    }
}
