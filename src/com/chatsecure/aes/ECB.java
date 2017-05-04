/**
 * 
 */
package com.chatsecure.aes;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.chatsecure.aes.*;
/**
 * @author cstevens
 *
 */
public class ECB {
	private static int[][] keyArray;
	public static boolean setKey(byte[] key_) throws Exception{
		if(key_.length != 16)
			return false;
		String str = new String(key_);
        
		keyArray = KeyGen.generate(byteArrayToHex(key_));

		return true;
	}
	
	public static byte[] encrypt(byte[] inputArray) throws Exception{
		int numRounds = 10;
		int[][] subKey = new int[4][4];
		byte[] output = new byte[16];
		subKey = KeyGen.getSubKey(0);
		int[][] stateAES = parseByteArrayToStateMatrix(byteArrayToHex(inputArray));
		AddRoundKey.add(stateAES, subKey);
		for (int i = 1; i < numRounds; i++) {

			SubBytes.sub(stateAES);
			ShiftRows.shift(stateAES);
			MixColumns.mix(stateAES);
			subKey = KeyGen.getSubKey(i);
			AddRoundKey.add(stateAES, subKey);
		}    

		SubBytes.sub(stateAES);
		ShiftRows.shift(stateAES);
		subKey = KeyGen.getSubKey(numRounds);
		AddRoundKey.add(stateAES, subKey);
		output = hexStringToByteArray(MatrixToString(stateAES));
		return output;
	}
	private static int[][] parseByteArrayToStateMatrix(String plainhexText) throws Exception {
		String subText = "";
		int[][] state = new int[4][4];
		
		if(plainhexText.length() != 32) {
			//System.out.println(Integer.toString(plainhexText.length()));
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

	public static String byteArrayToHex(byte[] a) {
		   StringBuilder sb = new StringBuilder(a.length * 2);
		   for(byte b: a)
		      sb.append(String.format("%02x", b));
		   return sb.toString();
		}
	public static String MatrixToString(int[][] m) //takes in a matrix and converts it into a line of 32 hex characters.
    {
        String t = "";
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                String h = Integer.toHexString(m[j][i]).toUpperCase();
                if (h.length() == 1) {
                    t += '0' + h;
                } else {
                    t += h;
                }
            }
        }
        return t;
    }
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
