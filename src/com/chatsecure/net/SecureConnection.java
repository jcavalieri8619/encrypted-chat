package com.chatsecure.net;

import com.chatsecure.aes.CTR;
import com.chatsecure.client.Message;
import com.chatsecure.client.MessageType;
import com.chatsecure.client.Status;
import com.chatsecure.client.User;
import com.chatsecure.rsa.RSAEncryption;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jcavalie on 3/14/17.
 */

/**
 * singleton class
 */
public class SecureConnection
{

    private Thread coordinator = null;
    private String hostAddr;
    private CTR AEScrypto = null;
    private Integer portNum;
    private Socket userSocket = null;
    private OutputStream stream_to_P2Pcoord;
    private InputStream stream_from_P2Pcoord;
    private User userSelf;
    private static final SecureConnection theInstance = new SecureConnection( );

    private static AtomicBoolean initialized = new AtomicBoolean( false );


    //if you are P2Pcoordinator then you generate this key and distribute to all clients via RSA;
    //else this key is set in call doHandShake
    private static byte[] SHARED_KEY = new byte[ 16 ];


    public static SecureConnection getConnection( ){

        return theInstance;
    }

    private SecureConnection( ){
    }


    /**
     * version of init called by the userSelf who becomes P2P coordinator where host address is local host
     *
     * @param new_user             name of userSelf becoming P2P coordinator
     * @param port_num             port number
     * @param becomeP2Pcoordinator must be true here
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void initalize( User new_user, int port_num, boolean becomeP2Pcoordinator ) throws
            Exception{

        assert becomeP2Pcoordinator : "Initialized SecureConnection without host address and becomeP2P is false";

        initalize( InetAddress.getLocalHost( ).getHostAddress( ), new_user, port_num, true );


    }


    /**
     * @param host_addr            IP address of P2P coordinator as string
     * @param port_num             port number for SecureChat app
     * @param becomeP2Pcoordinator flag to indicate if you are the sole P2P coordinator for a chat room
     */
    public synchronized void initalize( String host_addr, User new_user, int port_num,
                                        boolean becomeP2Pcoordinator ) throws
            Exception{

        //if userSocket is not null then SecureConnection already initialized so just return
        if ( userSocket != null ){
            return;
        }


        userSelf = new_user;
        hostAddr = host_addr;
        portNum = port_num;

        ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
        ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );

        byte[] msg_bytes;


        if ( becomeP2Pcoordinator ){



            //generate random 128-bit key for AES128 that will be shared with all clients using RSA and
            SecureRandom random = new SecureRandom( );



            random.nextBytes( SHARED_KEY );
            SHARED_KEY[ 0 ] = (byte) Math.abs( SHARED_KEY[ 0 ] );
            if ( SHARED_KEY[ 0 ] == 0 ){
                SHARED_KEY[ 0 ] = 1;
            }

            //instantiate AES here for user assuming P2P role
            AEScrypto = new CTR( SHARED_KEY );

            Thread.currentThread( ).getId( );
            coordinator = new Thread( new P2Pcoordinator( ) );
            coordinator.setDaemon( true );
            coordinator.start( );


            userSocket = new Socket( hostAddr, portNum );
            //start coordinator then connect where hostAddr is localAddr so
            //coordinator will handle connection same way it would handle
            //connection from remote host.

            stream_to_P2Pcoord = userSocket.getOutputStream( );
            stream_from_P2Pcoord = userSocket.getInputStream( );


            Message m = new Message( MessageType.SELFCONNECTION,
                                     userSelf, null );

            to_byte_stream.writeObject( m );
            msg_bytes = byte_stream_in.toByteArray( );

            stream_to_P2Pcoord.write( msg_bytes, 0, msg_bytes.length );

            initialized.set( true );
            notifyAll( );

        } else{

            userSocket = new Socket( hostAddr, portNum );


            stream_to_P2Pcoord = userSocket.getOutputStream( );

            stream_from_P2Pcoord = userSocket.getInputStream( );

            try{
                doHandShake( );
            } catch ( Exception e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                            "Error Initialize:  doHandShake()", e );
                throw e;
            }
            initialized.set( true );
            notifyAll( );
        }


    }

    public boolean isConnected( ){
        return userSocket.isConnected( );
    }

    public synchronized SecureConnection waitForInitialization( ){


        while ( !initialized.get( ) ){

            try{
                wait( );
            } catch ( InterruptedException e ){
                e.printStackTrace( );
            }
            //busy wait if not yet initialized--will be quick
        }

        return this;
    }

    private Long byteArrayToLong( byte[] b ){
        final ByteBuffer bb = ByteBuffer.wrap( b );
        return bb.getLong( );
    }

    private byte[] LongToByteArray( Long i ){
        final ByteBuffer bb = ByteBuffer.allocate( Long.SIZE / Byte.SIZE );
        bb.putLong( i );
        return bb.array( );
    }

    private String byteToString( byte[] b ){
        return new String( b, Charset.forName( "US-ASCII" ) );
    }

    private void doHandShake( ) throws Exception{

        byte[] shared_secret;

        RSAEncryption RSAenc = new RSAEncryption( );


        try{

            //sending public key to P2P coordinator
            ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
            ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );

            byte[] msg_bytes;
            Message m = new Message( MessageType.HANDSHAKE,
                                     userSelf,
                                     null )
                    .setPublicKey( RSAenc.getPublicKey( ) );
            to_byte_stream.writeObject( m );
            msg_bytes = byte_stream_in.toByteArray( );


            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.INFO,
                                                                        ( "Handshake message: " + m.toString( ) ) );


            stream_to_P2Pcoord.write( msg_bytes, 0, msg_bytes.length );


            byte[] received_msg_bytes = new byte[ 8192 * 4 ];
            int num = stream_from_P2Pcoord.read( received_msg_bytes, 0, received_msg_bytes.length );
            if ( num == -1 ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                            ( "DOHANDSHAKE: READ RETURNED -1" ) );

                return;
            }
            byte[] in_buff = new byte[ num ];
            System.arraycopy( received_msg_bytes, 0, in_buff, 0, num );


            Message returnMsg;

            ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( in_buff );
            ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );

            returnMsg = (Message) from_byte_stream.readObject( );

            assert returnMsg.getType( ) ==
                   MessageType.HANDSHAKE : "doHandShake got back wrong message type from P2P coordinator";

            //here the com.chatsecure.client is using his private RSA key to decrypt this message--
            //that key should exist within RSA class
            shared_secret = RSAenc.decrypt( returnMsg.getRSAresult( ) );

            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.INFO,
                                                                        ( "RSA DECRYPTED SHARED KEY: " +
                                                                          Arrays.toString( shared_secret ) ) );



        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,

                                                                        "Error in doHandShake()", e );
            stream_from_P2Pcoord.close( );
            stream_to_P2Pcoord.close( );
            throw e;
        }

        SHARED_KEY = shared_secret;

        //instantiate AES here for regular users
        AEScrypto = new CTR( SHARED_KEY );

    }


    public void writeMessage( Message msg ) throws IOException{

        writeMessage_( msg, stream_to_P2Pcoord, AEScrypto );
    }

    private void writeMessage( Message msg, OutputStream oos, CTR AES ) throws IOException{

        writeMessage_( msg, oos, AES );
    }


    private void writeMessage_( Message msg, OutputStream oos, final CTR AES ) throws IOException{
        byte[] msg_bytes;
        byte[] encrypted_msg_bytes = null;

        try ( ByteArrayOutputStream byte_stream = new ByteArrayOutputStream( );
              ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream ) ){

            to_byte_stream.writeObject( msg );
            msg_bytes = byte_stream.toByteArray( );

            try{

                encrypted_msg_bytes = AES.encryptMessage( msg_bytes );
            } catch ( Exception e ){
                // TODO Auto-generated catch block
                e.printStackTrace( );
            }

            assert encrypted_msg_bytes != null : "CTR.encryptMessage returned null";


            oos.write( encrypted_msg_bytes );

        } catch ( IOException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Error in WriteMessage()", e );
            throw e;
        }
    }


    public Message readMessage( ) throws IOException, ClassNotFoundException{
        return readMessage_( stream_from_P2Pcoord, AEScrypto );
    }

    private Message readMessage( InputStream iis, CTR AES ) throws IOException, ClassNotFoundException{
        return readMessage_( iis, AES );
    }


    private Message readMessage_( InputStream iis, CTR AES ) throws IOException, ClassNotFoundException{

        byte[] decrypted_msg_bytes = null;

        byte[] received_msg_bytes = new byte[ 8192 ];


        int num = iis.read( received_msg_bytes, 0, 8192 );
        if ( num == -1 ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                        "READMESSAGE: iis.read returned -1. returning null" );
            return null;
        }
        byte[] encrypted_msg_bytes = new byte[ num ];
        System.arraycopy( received_msg_bytes, 0, encrypted_msg_bytes, 0, num );

        try{

            decrypted_msg_bytes = AES.decryptMessage( encrypted_msg_bytes );
        } catch ( Exception e1 ){

            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                        "AES decrypt error", e1 );
            return null;

        }

        assert decrypted_msg_bytes != null : "CTR.decrypt returned null";

        ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( decrypted_msg_bytes );
        ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );

        Message msg = (Message) from_byte_stream.readObject( );



        return msg;


    }


    public void closeConnection( ) throws IOException{

        writeMessage( new Message( MessageType.REMOVEUSER, userSelf, null ) );

//        if ( coordinator != null ){
//            //if coordinator closes connection then need to migrate coordinator
//            //or just kick everyone off
//
//        }
    }


    private class P2Pcoordinator implements Runnable
    {


        final ConcurrentMap<User, OutputStream> host_connections;
        final AtomicBoolean continue_coordinating;
        final ConcurrentMap<Integer, User> online_users;
        User controllerUser;
        final ArrayBlockingQueue<Message> outgoingMessages;
        CTR P2Ps_AES = null;


        Thread P2Plistener_thread;

        P2Pcoordinator( ) throws Exception{


            int queueCapacity = 100;
            outgoingMessages = new ArrayBlockingQueue<>( queueCapacity );

            P2Ps_AES = new CTR( SHARED_KEY );

            host_connections = new ConcurrentHashMap<>( );
            online_users = new ConcurrentHashMap<>( );

            controllerUser = userSelf;

            continue_coordinating = new AtomicBoolean( true );


            P2Plistener_thread = new Thread( new P2Plistener( ) );
            P2Plistener_thread.setDaemon( true );
            P2Plistener_thread.start( );

        }

        void internalCloseConnection( User U ){

            //if threadID is null then close all connections because
            //P2P coordinator is signing off (or something like that)
            if ( U == null || Objects.equals( U, controllerUser ) ){
                host_connections.forEach( ( user_, outStream ) -> {
                    try{
                        outStream.close( );
                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Socket close failed in P2Plistner" );


                    }


                } );
                host_connections.clear( );
                online_users.clear( );

            } else{
                try{


                    OutputStream usrStream = host_connections.remove( U );
                    online_users.remove( usrStream.hashCode( ) );
                    usrStream.close( );
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Socket close failed in internalClose" );

                } finally{
//                    while(true){
//                        try{
//                            outgoingMessages.put( new Message( MessageType.USER, U, "Signed Off" ) );
//                        } catch ( InterruptedException e ){
//                            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.WARNING,
//                                                                                        ( "interruption exception in internalClose" ) );
//                            continue;
//                        }
//                        break;
//                    }

                }

            }
        }


        @Override
        public void run( ){

            while ( continue_coordinating.get( ) ){

                Message msg;
                try{
                    msg = outgoingMessages.take( );
                } catch ( InterruptedException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.WARNING,
                                                                           "Interrupted exception" +
                                                                           " in P2P coordinator run" );
                    continue;

                }


                Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.INFO, ( "INSIDE BROADCAST--MSG: " + msg )
                );


                Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.INFO,
                                                                       ( "INSIDE BROADCAST--host_connections users: " +
                                                                         host_connections.keySet( ) )
                );



                for ( User u : host_connections.keySet( ) ){

                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.INFO,
                                                                           ( "BROADCASTING TO: " + u )
                    );

                    if ( msg.getType( ).equals( MessageType.REMOVEUSER ) &&
                         u.getName( ).equals( msg.getUser( ).getName( ) ) ){
                        internalCloseConnection( u );
                        continue;
                    }

                    try{
                        writeMessage( msg, host_connections.get( u ), P2Ps_AES );
                    } catch ( IOException e ){
                        Logger.getLogger( this.getClass( ).toString( ) ).log( Level.WARNING,
                                                                              "A broadcast message failed to send",
                                                                              e );

                        //don't kill entire program here because if socket write
                        //fails it may just be that users socket closed unexpectedly
                        //so just ignore fail and broadcast to everyone else

                    }


                }




            }
        }

        private class P2Plistener implements Runnable
        {

            final ServerSocket listening_sock;
            final ArrayList<Thread> threadList;

            final static int BACKLOG = 10;

            P2Plistener( ) throws IOException{

                try{
                    listening_sock = new ServerSocket( portNum, BACKLOG );
                    listening_sock.setReuseAddress( true );
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Server Socket failed in P2Plistner" );
                    throw e;
                }

                threadList = new ArrayList<>( );
            }

            @Override
            public void run( ){
                Thread t;
                while ( continue_coordinating.get( ) ){
                    try{

                        t = new Thread( new P2Phandler( listening_sock.accept( ) ) );
                        t.setDaemon( true );


                        threadList.add( t );
                        t.start( );


                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Server userSocket accept failed in P2Plistner" );

                    }


                }

            }
        }

        private class P2Phandler implements Runnable
        {
            final Socket connected_sock;
            boolean handshakeDone = false;
            User connecteduser = null;
            boolean continueHandling = true;

            P2Phandler( Socket connected_sock ){

                this.connected_sock = connected_sock;


            }


            @Override
            public void run( ){


                try ( OutputStream oos = connected_sock.getOutputStream( );
                      InputStream iis = connected_sock.getInputStream( ) ){


                    while ( connected_sock.isConnected( ) && continueHandling ){

                        if ( !handshakeDone ){

                            //handshake message is first message sent upon connection
                            //and is not encrypted yet because encryption depends
                            //on handshake--so just use readObject to extract Message
                            //containing users public key, then encrypt the shared
                            //secret AES128 bit key via RSAencrypt then send back to userSelf;
                            //now all further communication will be encrypted via AEScrypto


                            byte[] received_msg_bytes = new byte[ 8192 * 2 ];
                            int num = iis.read( received_msg_bytes, 0, received_msg_bytes.length );
                            if ( num == -1 ){
                                Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                                       "P2Phandler.run iis.read returned -1 error" );
                                return;
                            }
                            byte[] in_buff = new byte[ num ];
                            System.arraycopy( received_msg_bytes, 0, in_buff, 0, num );


                            ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( in_buff );
                            ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );

                            Message msg;
                            msg = (Message) from_byte_stream.readObject( );



                            assert msg.getType( ) == MessageType.HANDSHAKE ||
                                   msg.getType( ) == MessageType.SELFCONNECTION :
                                    "First sent message must be type HANDSHAKE or SELFCONNECTION";

                            connecteduser = msg.getUser( );

                            online_users.put( oos.hashCode( ), connecteduser );
                            host_connections.put( connecteduser, oos );


                            //if the userSelf connecting to P2P coordinator is the
                            //userSelf assuming role of p2P coordinator then no need
                            //for remaining handshake operations to continue
                            if ( msg.getType( ) == MessageType.SELFCONNECTION ){
                                handshakeDone = true;
                                continue;
                            }


                            BigInteger encryptedSharedSecret;

                            encryptedSharedSecret = RSAEncryption.encrypt( msg.getPublicKey( ),
                                                                           new BigInteger( SHARED_KEY ) );

                            ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
                            ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );

                            byte[] msg_bytes;
                            Message m = new Message( MessageType.HANDSHAKE,
                                                     new User( "P2Pcontroller", Status.CONTROLLER ),
                                                     null ).setRSAresult( encryptedSharedSecret );


                            to_byte_stream.writeObject( m );
                            msg_bytes = byte_stream_in.toByteArray( );


                            oos.write( msg_bytes, 0, msg_bytes.length );


                            outgoingMessages.put( msg.setType( MessageType.ADDUSER )
                                                          .setUserList( new ArrayList<>( online_users.values( ) ) )
                                                          .setPublicKey( null ) );


                            handshakeDone = true;

                        } else{

                            //decrypts message to take appropriate actions depending on
                            //message type then broadcasts message to all other users

                            Message msg;
                            msg = readMessage( iis, P2Ps_AES );


                            Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.INFO,
                                                                                   ( "P2Phandler.run for connected user: " +
                                                                                     connecteduser ) );


                            Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.INFO,
                                                                                   ( "P2Phandler.run read message: " +
                                                                                     msg ) );



                            if ( msg == null ){
                                Logger.getLogger( this.getClass( ).toString( ) ).log( Level.WARNING,
                                                                                      "P2Phandler--readMessage returned null" );
                                continue;
                            }

                            switch ( msg.getType( ) ){


                                case STATUS:
                                    online_users.put( oos.hashCode( ),
                                                      connecteduser.updateStatus( msg.getStatus( ) ) );
                                    break;

                                case REMOVEUSER:
                                    online_users.remove( oos.hashCode( ) );
                                    continueHandling = false;
                                    break;

                                case USER:

                                    break;

                            }


                            msg.setUserList( new ArrayList<>( online_users.values( ) ) );
                            outgoingMessages.put( msg );

                        }


                    }

                } catch ( IOException | ClassNotFoundException | InterruptedException e ){
                    Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                                                                          "Stream created failed in P2Phandler", e );

                }

                if ( continueHandling ){
                    //if continueHandling true then socket closed unexpectedly
                    //internalCloseConnection( Thread.currentThread( ).getId( ) );

                    try{
                        outgoingMessages.put( new Message( MessageType.REMOVEUSER, connecteduser, null ) );
                    } catch ( InterruptedException e ){
                        Logger.getLogger( this.getClass( ).toString( ) ).log( Level.WARNING,
                                                                              "Interrupted exception in handler",
                                                                              e );
                    }


                }


            }
        }


    }

}
