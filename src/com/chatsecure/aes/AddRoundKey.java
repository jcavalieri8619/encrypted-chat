package com.chatsecure.aes;

public class AddRoundKey {
	
	public static void add(int[][]inputArray, int[][]keyArray){
		
		for (int i = 0; i < inputArray.length; i++) {
            for (int j = 0; j < inputArray[0].length; j++) {
            	inputArray[j][i] ^= keyArray[j][i];
            }
        }
	}

}
