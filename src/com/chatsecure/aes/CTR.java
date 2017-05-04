package com.chatsecure.aes;

import com.chatsecure.sha.SHA512;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class CTR {
    private int block_size = 16;
    private int hash_size = 64;
    private byte[] iv = new byte[ block_size ];
    private byte[] aes_key;

    public CTR( final byte[] aes_key ) throws Exception{
        setkey( aes_key );
    }

    //save the key that will be used to encrypt the message
    public boolean setkey( byte[] key_ ) throws Exception{
        if(key_.length == 16 || key_.length == 32){
			aes_key = new byte[key_.length];

            return ECB.setKey( key_ );
        } else{
            return false;
		}
	}

    public byte[] encryptMessage( byte[] msg_ ) throws Exception{
        //used to generate the random number for the IV, this is not cryptographically safe
	    Random random = new Random();
	    byte[] hash = new byte[hash_size];
	    byte[] encIV;
	    byte[] rand = new byte[8];
	    int alignedLength = msg_.length + block_size- (msg_.length % block_size);
	    byte[] encMsg = new byte[block_size+block_size+alignedLength + hash_size];
	    byte[] plainText = new byte[block_size+alignedLength + hash_size];
	    //generate random bytes for the IV
	    random.nextBytes(iv);
	    //generate random bytes for the length, since I am using 4 bytes of the 16 byte block for length
	    random.nextBytes(rand);
	    //this is to be able to convert from byte arrays to ints, for the length and IV
	    ByteBuffer bb = ByteBuffer.allocate(4);
	    //set the Initial IV to 0, I am only usin the last 4 bytes for the IV, the first 12 bytes are random
	    bb.putInt(0);
	    //copy the IV to the last 4 bytes of the IV block
	    System.arraycopy( bb.array(), 0, iv, 12, bb.array().length );	  
	    //copy the IV to the front of the encrypted message
	    System.arraycopy( iv, 0, encMsg, 0, iv.length );
	    //copy the random bytes at the start of the encryption section of the buffer
	    System.arraycopy( rand, 0, plainText, 0, rand.length );
	    //have to clear the byte buffer to start at the beginning of the buffer
	    bb.clear();
	    //save the length of the unencrypted message into the byte buffer to convert it to a byte array
	    bb.putInt(msg_.length);
	    //this copies the length after the first 8 bytes of random data, the last 4 bytes is for a message id (future implementation if someone wants to use the code)
	    System.arraycopy( bb.array(), 0, plainText, 8, bb.array().length );
	    //copy all original message after the length block size
	    System.arraycopy(msg_, 0, plainText, block_size, msg_.length);
	    //create hash	    
	    hash = SHA512.hash(msg_);
	    //add HASH to end of message
	    System.arraycopy(hash, 0, plainText, plainText.length-hash_size, hash_size);
	    
		//start encrypting the IV and xoring it with the buffer
	    byte[] cipherText = xorSection(plainText, 0);
	    //copy all the encrypted data to the encrypted message after the IV
	    System.arraycopy(cipherText, 0, encMsg, block_size, cipherText.length);
	    
		return encMsg;
	}

    public byte[] decryptMessage( byte[] encMsg_ ) throws Exception{
        byte [] newHash = new byte[hash_size];
	    byte [] origHash = new byte[hash_size];
	    int msgLength;
	    byte[] plainText = new byte[encMsg_.length-2*block_size];
	    ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE/8);
	    //get the initial IV
	    System.arraycopy( encMsg_, 0, iv, 0, block_size );
	    //decrypt the length and message id
	    //encrypt XOR
	    plainText = xorSection(encMsg_, block_size);
		
	    //this copies the length into the first part of the encryption
	    System.arraycopy( plainText, 8, bb.array(), 0, Integer.SIZE/8 );
	    msgLength = bb.getInt();  
	    //verify that the length is valid
	    if(msgLength > plainText.length-hash_size || msgLength < 0){
	    	System.out.println("Invalid Length Found");
	    	return null;
	    }
	    //this buffer is used to store the unencrypted message
	    byte[] msg = new byte[msgLength];
	    //start copying the unencrypted data into the return buffer
	    System.arraycopy(plainText, block_size, msg, 0,msgLength);
	    //grab the hash that is at the end of the buffer
	    System.arraycopy(plainText, plainText.length-(hash_size+block_size), origHash, 0,hash_size);
	    //Verify message
	    newHash = SHA512.hash(msg);
	    if(!Arrays.equals(newHash, origHash)){
	    	System.out.println("Invalid Hash");
	    	return null;
	    }
	   
	    return msg;
	}

    private byte[] xorSection( byte[] buff, int offset ) throws Exception{
        byte[] xorText = new byte[buff.length];
	    //start encrypting the IV and xoring it with the buffer
	    updateIV(); 
		//loop through the buffer and decrypt/encrypt every 16 byte block
	    for(int iter = 0; iter < buff.length-offset; iter+=16){
	    	byte[] tmpBuff = xorBuff(buff, offset + iter);	    
	    	System.arraycopy(tmpBuff, 0, xorText, iter, tmpBuff.length);
	    }
	    return xorText;
	}

    private void updateIV( ){
        ByteBuffer bb = ByteBuffer.allocate(4);
		int currIV;
		//copy the iv into the bytebuffer, to convert it to an int
		System.arraycopy( iv, 12, bb.array(), 0, bb.array().length );
		currIV = bb.getInt();
		//increment it to the next index
		currIV = currIV +1;
		//clear the byte buffer so we can save off the iv we just incremented
		bb.clear();		
		bb.putInt(currIV);
		//copy the new IV to the end of the global IV, remember the first 12 bytes is random so every message should have a unique IV
		System.arraycopy( bb.array(), 0, iv, 12, bb.array().length );
	}

    private byte[] xorBuff( byte[] buff, int offset ) throws Exception{
        byte[] xorBuff = new byte[block_size];
		byte[] cipherIV = new byte[iv.length];
		int i = 0;
		//encrypt the 16 byte block IV
		cipherIV = ECB.encrypt(iv);
		//loop through every byte of the encrypted IV to xor it with the buffer passed in
		for (byte b : cipherIV){
			//This is the same for encryption or decryption
			xorBuff[i] = (byte) (b ^ buff[offset+i]);
			i++;
		}
		return xorBuff;
	}

}
