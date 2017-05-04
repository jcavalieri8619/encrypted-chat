package com.chatsecure.aes;

import java.math.BigInteger;

public class BinaryString {
	
	
	public static String getBinaryString(String text) {
		
		String binaryString = "";
		
		byte[] bytes = text.getBytes(); 
		StringBuilder binaryTxt = new StringBuilder();

		for (byte b : bytes)
		  {
		     int val = b;
		     for (int index = 0; index < 8; index++)
		     {
		    	binaryTxt.append((val & 128) == 0 ? 0 : 1);
		        val <<= 1;
		     }
		  }
		
		binaryString = binaryTxt.toString();
		System.out.println("Binary String for " + text + " is : " + binaryString);
		
		return binaryString;
		
	}
	
	public static String hexToBin(String s) {
		  return new BigInteger(s, 16).toString(2);
	}
	
}
