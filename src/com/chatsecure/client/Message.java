package com.chatsecure.client;

import com.chatsecure.rsa.RSAPublicKey;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;


/**
 * Created by jcavalie on 3/13/17.
 */
public class Message implements Serializable{

    final private User user;
    final private String content;
    private MessageType type;
    private ArrayList<User> userList = null;


    private BigInteger RSAresult = null;
    private RSAPublicKey publicKey;





    public Message( MessageType type, User user, String content ){
        this.user = user;
        this.content = content;
        this.type=type;

    }

    @Override
    public String toString( ){
        String mod = publicKey == null ? null : publicKey.getMod().toString();
        String exp = publicKey == null ? null : publicKey.getExp().toString();
        return String.format( "Message{ user: %1$s\ncontent: %2$s  type: %3$s" +
                              "  status: %4$s \n" +
                              "pubkey_mod: %5$s \n" +
                              "pubkey_exp: %6$s \n" +
                              "RSAResult: %7$s \n}",
                              user, content, type, getStatus( ),
                              mod, exp, getRSAresult() );
    }


    public Message setUserList( final ArrayList<User> userList ){
        this.userList = userList;
        return this;
    }

    public Message setStatus( final Status status ){
        this.user.updateStatus( status );
        return this;
    }

    public Message setPublicKey(RSAPublicKey key) {
        this.publicKey = key;
        return this;
    }

    public BigInteger getRSAresult( ){
        return RSAresult;
    }

    public Message setRSAresult( final BigInteger RSAresult ){
        this.RSAresult = RSAresult;
        return this;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public User getUser( ){
        return user;
    }

    public String getContent( ){
        return content;
    }

    public MessageType getType( ){
        return type;
    }

    public Message setType(MessageType type ){
        this.type=type;
        return this;
    }


    public ArrayList<User> getUserList( ){
        return userList;
    }

    public Status getStatus( ){
        return this.user.getStatus( );
    }
}
