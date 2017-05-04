package com.chatsecure.sha;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TestSHA {
    private static byte[] hash512(byte[] toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(toHash);
        } catch(NoSuchAlgorithmException e) {
            System.out.println("No such algorithm");
            return null;
        }
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length; i++){
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String toHash = "Hello, this is a test of the hashing algorithm";

        // Print out hash using built in method
        System.out.println(byteArrayToHexString(hash512(toHash.getBytes())));
        // Print out hash using new implementation (should be the same as above)
        System.out.println(byteArrayToHexString(SHA512.hash(toHash.getBytes())));

    }
}
