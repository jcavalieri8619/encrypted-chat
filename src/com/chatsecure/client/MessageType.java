package com.chatsecure.client;

import java.io.Serializable;

/**
 * Created by jcavalie on 3/15/17.
 */
public enum MessageType implements Serializable{
    USER, REMOVEUSER, ADDUSER, STATUS, HANDSHAKE, MIGRATECONTROLLER, SELFCONNECTION
}
//