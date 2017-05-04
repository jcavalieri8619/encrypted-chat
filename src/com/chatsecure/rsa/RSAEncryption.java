package com.chatsecure.rsa;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

/**
 * @author sriramvaradharajan
 *
 */

/*
 * This class implements the RSA algorithm steps. The steps are listed as below 
 * 
 * 1. Step 1: Select two large prime numbers. Say p and q. 
 * 2. Step 2: Calculate n = p.q
 * 3. Step 3: Calculate ø(n) = (p - 1).(q - 1)
 * 4. Step 4: Find e such that gcd(e, ø(n)) = 1 ; 1 < e < ø(n)
 * 5. Step 5: Calculate d such that e.d = 1 (mod ø(n))
 * once we have the combination of e ,d we can perform encryption and decryption of the message.
 * using the RSA algorithm 
 * 
 * encryption :ciphertext = (plaintext ^e)mod n
 * decryption : plaintext = (ciphertext ^d)mod n.
 * 
 */

public class RSAEncryption {

	private BigInteger p, q;
	private BigInteger n;
	private BigInteger phiN;
	private BigInteger e ,d;
	
	public RSAEncryption(){
		initialize();
	}
	
	public void initialize(){
		int SIZE = 1024;
		/* Step 1: Select two large prime numbers. Say p and q. */
		p = new BigInteger(SIZE, 15, new Random());
		q = new BigInteger(SIZE, 15, new Random());
		
		/* Step 2: Calculate n = p.q */
		n = p.multiply(q);
		
		/* Step 3: Calculate ø(n) = (p - 1).(q - 1) */
		phiN = p.subtract(BigInteger.valueOf(1));
		phiN = phiN.multiply( q.subtract( BigInteger.valueOf(1)));
		
		/* Step 4: Find e such that gcd(e, ø(n)) = 1 ; 1 < e < ø(n) */
		do
		{
			e = new BigInteger(2*SIZE, new Random());
		}
		while( (e.compareTo(phiN) != 1)||(e.gcd(phiN).compareTo(BigInteger.valueOf(1)) != 0));
		 
		/* Step 5: Calculate d such that e.d = 1 (mod ø(n)) */
		d = e.modInverse(phiN);
	}
	
	/*
	 * 
	 */
	public static BigInteger encrypt( RSAPublicKey publicKey, BigInteger plaintext )
	{

		return ( ( plaintext ) ).modPow( publicKey.getExp(), publicKey.getMod() );
	}

	public byte[] decrypt( BigInteger ciphertext )
	{
		return ( ( ( ciphertext ) ).modPow( d, n ).toByteArray( ) );
	}


	public BigInteger getPrivateKey( ){
		return d;
	}

	public RSAPublicKey getPublicKey( ){
		return new RSAPublicKey(n, e);
	}

}
