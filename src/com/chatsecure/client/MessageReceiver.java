package com.chatsecure.client;
import com.chatsecure.net.SecureConnection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jcavalie on 4/17/17.
 */
public class MessageReceiver implements Runnable
{

    private SecureConnection secureCon;
    private ChatController chatController;
    private String username;

    private static User userSelf;


    /**
     * constructor called by the userSelf who is assuming role is P2P coordinator
     * @param portNum
     * @param chatController
     * @param username
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public MessageReceiver( int portNum,
                            final ChatController chatController,
                            final String username ) throws Exception{


        this.chatController = chatController;
        this.username = username;

        userSelf = new User( this.username, Status.ONLINE );


        this.secureCon = SecureConnection.getConnection( );

        try{
            secureCon.initalize( userSelf, portNum, true );
        } catch ( Exception e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                        e );
            throw e;
        }


        chatController.addSelfUserToChat( userSelf );

    }


    public MessageReceiver( String hostName, int portNum,
                            final ChatController chatController,
                            final String username ) throws Exception{


        this.chatController = chatController;
        this.username = username;


        userSelf = new User( this.username, Status.ONLINE );


        this.secureCon = SecureConnection.getConnection( );
        try{
            secureCon.initalize( hostName, userSelf, portNum, false );
        } catch ( Exception e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                       e );
            throw e;
        }

        //chatController.addSelfUserToChat( userSelf );

    }

    public static User getUserSelf( ){
        return userSelf;
    }

    @Override
    public void run( ){


        Message incoming_msg;
        while ( secureCon.isConnected( ) ){

            try{

//                while ( secureCon.stream_from_P2Pcoord.available( ) == 0 ){
//                    try{
//                        Thread.sleep( 300 );
//                    } catch ( InterruptedException e ){
//                        e.printStackTrace( );
//                    }
//                    continue;
//                }


                incoming_msg = secureCon.waitForInitialization( ).readMessage( );

                if ( incoming_msg == null ){
                    Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                               "MessageRcvr--readMessage returned null" );
                    System.exit( 0 );
                }


            } catch ( IOException e ){
                Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                           "Error in MessageReceiver" +
                                                                           " reading new message",
                                                                           e );
                //returning will kill thread
                break;
            } catch ( ClassNotFoundException e ){
                Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                           "Error in MessageReceiver" +
                                                                           " reading new message",
                                                                           e );
                break;
            }


            chatController.updateChatRoomState( incoming_msg.getType( ), incoming_msg );



        }

    }
}
