package com.chatsecure.client;

import java.io.Serializable;

/**
 * Created by jcavalie on 3/13/17.
 */
public class User implements Serializable{

    final private String name;
    private Status status;


    public User( final String name, final Status status ){
        this.name = name;
        this.status = status;
    }

    public User updateStatus( final Status status ){
        this.status = status;
        return this;
    }

    //
    @Override
    public String toString( ){
        return String.format( "User{ name: %1$s  status: %2$s}", name, status );
    }

    public String getName( ){
        return name;
    }

    public Status getStatus( ){
        return status;
    }
}
