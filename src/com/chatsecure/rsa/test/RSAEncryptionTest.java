package com.chatsecure.rsa.test;

import com.chatsecure.rsa.RSAEncryption;

import java.math.BigInteger;

public class RSAEncryptionTest {
	public static void main(String args[]){
		RSAEncryption encryption = new RSAEncryption();
		System.out.println("public key is "+ encryption.getPublicKey());
		System.out.println("private key is "+ encryption.getPrivateKey());
		BigInteger toEncrypt = new BigInteger("checking rsa encryption".getBytes());
        BigInteger encryptedText = RSAEncryption.encrypt(encryption.getPublicKey(), toEncrypt);
		System.out.println("encrypted text is " + encryptedText);
        byte[] decryptedText = encryption.decrypt(encryptedText);
		System.out.println("Decrypted text is " + new String(decryptedText));
	}

}
