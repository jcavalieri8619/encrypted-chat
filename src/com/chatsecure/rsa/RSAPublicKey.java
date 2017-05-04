package com.chatsecure.rsa;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by prasant on 4/22/17.
 */
public class RSAPublicKey implements Serializable {
    private BigInteger mod;
    private BigInteger exp;

    public RSAPublicKey(BigInteger mod, BigInteger exp) {
        this.mod = mod;
        this.exp = exp;
    }

    public BigInteger getMod() {
        return mod;
    }

    public BigInteger getExp() {
        return exp;
    }
}
