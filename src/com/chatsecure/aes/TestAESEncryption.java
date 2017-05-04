package com.chatsecure.aes;

/**
 * We will be AES in CBC mode. 
 * We will be using hexadecimal string to represent bytes. 
 * One hexadecimal string represents a nibble. So, it takes two hexadecimal to represent a byte. 
 * 
 * @author Udit Adhikari
 * @Date April 2nd 2017
 *
 */

public class TestAESEncryption {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		int numRounds = 10;

		String keyHexText = "AF0B50ED2CB5F98EC210BC582CBDE12F";
		int[][] keyArray = KeyGen.generate(keyHexText);
		int[][] subKey = new int[4][4];

		String initialVectorHexText = "2E19DFBA47BC59C259A0FF5A47DCF0C9";
		int[][] initialVector = initializeInitialVector(initialVectorHexText);

		String plainHexText = "CD152FA42B64E31F291D2A34DB6431A4";
		int[][] stateAES = parseHexStringToStateMatrix(plainHexText);

		AddRoundKey.add(stateAES, initialVector); // CBC mode

		subKey = KeyGen.getSubKey(0);

		for (int i = 1; i < numRounds; i++) {

			SubBytes.sub(stateAES);
			ShiftRows.shift(stateAES);
			MixColumns.mix(stateAES);

			subKey = KeyGen.getSubKey(i);
			AddRoundKey.add(stateAES, subKey);
		}

		SubBytes.sub(stateAES);
		ShiftRows.shift(stateAES);
		AddRoundKey.add(stateAES, subKey);

		// since we are performing CBC
		initialVector = stateAES;
		
		printAESState(stateAES);
	}

	private static int[][] initializeInitialVector(String initialVectorHexText) throws Exception {
		String subText = "";
		int[][] initialVector = new int[4][4];
		
		if(initialVectorHexText.length() != 32) {
			throw new Exception("Please have the length of initialization Vector be 16 bytes. Two hex strings represent one byte.");
		}
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				subText = initialVectorHexText.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2));
				initialVector[j][i] = Integer.parseInt(subText, 16);
			}
		}
		return initialVector;
	}

	private static int[][] parseHexStringToStateMatrix(String plainhexText) throws Exception {
		String subText = "";
		int[][] state = new int[4][4];
		
		if(plainhexText.length() != 32) {
			throw new Exception("Please have the length of plain hex text be 16 bytes. Two hex strings represent one byte.");
		}
		
		

		for (int i = 0; i < 4; i++) // Parses line into a matrix
		{
			for (int j = 0; j < 4; j++) {
				subText = plainhexText.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2));
				state[j][i] = Integer.parseInt(subText, 16);
			}
		}

		return state;
	}

	private static void printAESState(int[][] stateAES) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.println("i -> " + i + " j-> " + j + " state of AES  matrix is : " + stateAES[i][j]);
			}
		}

	}
}
