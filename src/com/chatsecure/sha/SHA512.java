package com.chatsecure.sha;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Contains logic to perform a SHA-512 hash on a byte[]
 * Using pseudocode from Wikipedia (https://en.wikipedia.org/wiki/SHA-2)
 */
public class SHA512 {
    public static byte[] hash(byte[] toHash) {
        // initialize hash values
        // (first 64 bits of the fractional parts of the square roots of the first 8 primes 2..19):
        long h0 = 0x6a09e667f3bcc908L;
        long h1 = 0xbb67ae8584caa73bL;
        long h2 = 0x3c6ef372fe94f82bL;
        long h3 = 0xa54ff53a5f1d36f1L;
        long h4 = 0x510e527fade682d1L;
        long h5 = 0x9b05688c2b3e6c1fL;
        long h6 = 0x1f83d9abfb41bd6bL;
        long h7 = 0x5be0cd19137e2179L;

        // Initialize array of round constants:
        // (first 64 bits of the fractional parts of the cube roots of the first 64 primes 2..311):
        long[] k = {0x428a2f98d728ae22L, 0x7137449123ef65cdL, 0xb5c0fbcfec4d3b2fL, 0xe9b5dba58189dbbcL, 0x3956c25bf348b538L,
                0x59f111f1b605d019L, 0x923f82a4af194f9bL, 0xab1c5ed5da6d8118L, 0xd807aa98a3030242L, 0x12835b0145706fbeL,
                0x243185be4ee4b28cL, 0x550c7dc3d5ffb4e2L, 0x72be5d74f27b896fL, 0x80deb1fe3b1696b1L, 0x9bdc06a725c71235L,
                0xc19bf174cf692694L, 0xe49b69c19ef14ad2L, 0xefbe4786384f25e3L, 0x0fc19dc68b8cd5b5L, 0x240ca1cc77ac9c65L,
                0x2de92c6f592b0275L, 0x4a7484aa6ea6e483L, 0x5cb0a9dcbd41fbd4L, 0x76f988da831153b5L, 0x983e5152ee66dfabL,
                0xa831c66d2db43210L, 0xb00327c898fb213fL, 0xbf597fc7beef0ee4L, 0xc6e00bf33da88fc2L, 0xd5a79147930aa725L,
                0x06ca6351e003826fL, 0x142929670a0e6e70L, 0x27b70a8546d22ffcL, 0x2e1b21385c26c926L, 0x4d2c6dfc5ac42aedL,
                0x53380d139d95b3dfL, 0x650a73548baf63deL, 0x766a0abb3c77b2a8L, 0x81c2c92e47edaee6L, 0x92722c851482353bL,
                0xa2bfe8a14cf10364L, 0xa81a664bbc423001L, 0xc24b8b70d0f89791L, 0xc76c51a30654be30L, 0xd192e819d6ef5218L,
                0xd69906245565a910L, 0xf40e35855771202aL, 0x106aa07032bbd1b8L, 0x19a4c116b8d2d0c8L, 0x1e376c085141ab53L,
                0x2748774cdf8eeb99L, 0x34b0bcb5e19b48a8L, 0x391c0cb3c5c95a63L, 0x4ed8aa4ae3418acbL, 0x5b9cca4f7763e373L,
                0x682e6ff3d6b2b8a3L, 0x748f82ee5defb2fcL, 0x78a5636f43172f60L, 0x84c87814a1f0ab72L, 0x8cc702081a6439ecL,
                0x90befffa23631e28L, 0xa4506cebde82bde9L, 0xbef9a3f7b2c67915L, 0xc67178f2e372532bL, 0xca273eceea26619cL,
                0xd186b8c721c0c207L, 0xeada7dd6cde0eb1eL, 0xf57d4f7fee6ed178L, 0x06f067aa72176fbaL, 0x0a637dc5a2c898a6L,
                0x113f9804bef90daeL, 0x1b710b35131c471bL, 0x28db77f523047d84L, 0x32caab7b40c72493L, 0x3c9ebe0a15c9bebcL,
                0x431d67c49c100d4cL, 0x4cc5d4becb3e42b6L, 0x597f299cfc657e2aL, 0x5fcb6fab3ad6faecL, 0x6c44198c4a475817L};
        /*
        Message Preprocessing:
        message needs to be multiple of 1024 bits long (i.e. multiple of 128 bytes long)
        first append new byte 0x80 to message
        next append enough 0 bytes such that length + 16 is divisible by 128
        next append L as 16 bytes
         */

        // length of original message
        int length = toHash.length;
        int KtoAppend = (int)Math.ceil((length + 17.0) / 128) * 128 - (length + 17); // append these many 0 bytes after appending 0x80 byte
        int messageLength = length + 17 + KtoAppend;
        byte[] message = new byte[(int)messageLength]; // new message that can be split into 64 byte chunks
        // convert length of original message to byte[]
        long bits = length * 8;
        byte[] lengthInBytes = new byte[16];
        for (int j = 0; j < 8; j++) {
            lengthInBytes[j] = (byte) ((bits >>> (8 * j)) & 0xFF);
        }
        int i;
        for (i = 0; i < length; i++) {
            message[i] = toHash[i];
        }
        message[i] = (byte) 0x80;
        i += KtoAppend + 1;
        for (int j = 15; j >= 0; j--) {
            message[i] = lengthInBytes[j];
            i++;
        }

        // break message into 1024 bit (128 byte) chunks
        byte[][] chunkedMessage = new byte[messageLength / 128][128];
        for (int j = 0; j < messageLength; j++) {
            chunkedMessage[j / 128][j % 128] = message[j];
        }

        // process each message chunk
        for (byte[] chunk : chunkedMessage) {
            long[] w = new long[80]; // 80-entry message schedule array w[0..79] of 64 bit (8 byte = 1 long) words

            // copy chunk into first 16 words of w
            int chunkIndex = 0;
            for (int j = 0; j < 16; j++) {
                ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
                for (int l = 0; l < 8; l++) {
                    buffer.put(chunk[chunkIndex]);
                    chunkIndex++;
                }
                w[j] = buffer.getLong(0);
            }

            // Extend the first 16 words into the remaining 64 words w[16..79] of the message schedule array
            for (int j = 16; j <= 79; j++) {
                long s0 = Long.rotateRight(w[j-15], 1) ^ Long.rotateRight(w[j-15], 8) ^ (w[j-15] >>> 7);
                long s1 = Long.rotateRight(w[j-2], 19) ^ Long.rotateRight(w[j-2], 61) ^ (w[j-2] >>> 6);
                w[j] = w[j-16] + s0 + w[j-7] + s1;
            }

            // Initialize working variables to current hash value
            long a = h0;
            long b = h1;
            long c = h2;
            long d = h3;
            long e = h4;
            long f = h5;
            long g = h6;
            long h = h7;

            // Compression function main loop
            for (int j = 0; j < 80; j++) {
                long S1 = Long.rotateRight(e, 14) ^ Long.rotateRight(e, 18) ^ Long.rotateRight(e, 41);
                long ch = (e & f) ^ ((~e) & g);
                long temp1 = h + S1 + ch + k[j] + w[j];
                long S0 = Long.rotateRight(a, 28) ^ Long.rotateRight(a, 34) ^ Long.rotateRight(a, 39);
                long maj = (a & b) ^ (a & c) ^ (b & c);
                long temp2 = S0 + maj;

                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;
            }

            // Add compressed chunk to current hash value
            h0 = h0 + a;
            h1 = h1 + b;
            h2 = h2 + c;
            h3 = h3 + d;
            h4 = h4 + e;
            h5 = h5 + f;
            h6 = h6 + g;
            h7 = h7 + h;
        }

        // Final hash value
        return ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN)
                .putLong(h0).putLong(h1).putLong(h2).putLong(h3)
                .putLong(h4).putLong(h5).putLong(h6).putLong(h7)
                .array();
    }
}
