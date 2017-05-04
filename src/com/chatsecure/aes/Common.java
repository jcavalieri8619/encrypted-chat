package com.chatsecure.aes;

public class Common {

	public static int[] rotateLeft(int[] array, int num)
    {
        assert(array.length == 4);
        
        if (num % 4 == 0) {
            return array; //the fourth row just return the array as is. 
        }
        
        while (num > 0) { // one loop of this will left shift once
            int tempVal = array[0];
            for (int index = 0; index < array.length - 1; index++) {
            	array[index] = array[index + 1]; //left shifting
            }
            array[array.length - 1] = tempVal;
            --num;
        }
        return array;
    }
	
}
