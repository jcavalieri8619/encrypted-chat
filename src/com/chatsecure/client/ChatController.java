package com.chatsecure.client;


import com.chatsecure.net.SecureConnection;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Shadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatController
{

    @FXML
    private MenuItem menu_close_btn;

    @FXML
    private CheckMenuItem status_online_btn;

    @FXML
    private CheckMenuItem status_away_btn;

    @FXML
    private Menu status_menu;

    @FXML
    private Label user_count;
    @FXML
    private TextField msgTextArea;
    @FXML
    private BorderPane borderpane_root;
    @FXML
    private VBox vbox_center;
    @FXML
    private ListView<Message> chatDisplay;  //technically listview<message> but factory overrides
    @FXML
    private ScrollPane scrollpane_center;
    @FXML
    private HBox hbox_bottom;
    @FXML
    private AnchorPane anchorpane_bottom;
    @FXML
    private Button clearMsgBtn;
    @FXML
    private Button sendMsgBtn;
    @FXML
    private ListView<User> userDisplay; //technically listview<user> but factory overrides
    @FXML
    private VBox vbox_right;
    @FXML
    private Label onlineUsersLabel;

    private Circle myStatusSymbol = null;

    private final ObservableList<Message> chatMessages = FXCollections.observableArrayList( );

    private final ObservableList<User> onlineUsers = FXCollections.observableArrayList( );
    IntegerBinding numUsersProperty = Bindings.size( onlineUsers );

    public ChatController( ){

    }


    //
    @FXML
    public void initialize( ){


        VBox.setVgrow( vbox_center, Priority.ALWAYS );
        VBox.setVgrow( vbox_right, Priority.ALWAYS );
        HBox.setHgrow( hbox_bottom, Priority.ALWAYS );

        numUsersProperty.addListener( ( observableValue, old_number, new_number ) -> {
            user_count.setText( String.valueOf( new_number ) );
        } );


        status_away_btn.selectedProperty( ).addListener( ( observableValue, aBoolean, t1 ) -> {

            if ( t1 && !aBoolean ){
                status_online_btn.setSelected( false );
                myStatusSymbol.setFill( Paint.valueOf( "red" ) );
                //  MessageReceiver.getUserSelf( ).updateStatus( Status.AWAY );
                updateSelfStatusInChat( Status.AWAY );
            }
        } );

        status_online_btn.selectedProperty( ).addListener( ( observableValue, aBoolean, t1 ) -> {


            if ( t1 && !aBoolean ){

                status_away_btn.setSelected( false );
                myStatusSymbol.setFill( Paint.valueOf( "green" ) );
                // MessageReceiver.getUserSelf( ).updateStatus( Status.ONLINE );
                updateSelfStatusInChat( Status.ONLINE );
            }

        } );



        chatDisplay.setItems( chatMessages );

        chatDisplay.setCellFactory( list -> new MessageListCell( ) );

        userDisplay.setItems( onlineUsers );
        userDisplay.setCellFactory( list -> new UserListCell( ) );


        vbox_center.widthProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                          chatDisplay.setPrefWidth( (double) newValue ) );

        vbox_center.heightProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                           chatDisplay.setPrefHeight( (double) newValue ) );

        vbox_right.widthProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                         userDisplay.setPrefWidth( (double) newValue ) );

        vbox_right.heightProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                          userDisplay.setPrefHeight( (double) newValue ) );

        hbox_bottom.widthProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                          msgTextArea.setPrefWidth( (double) newValue ) );

        hbox_bottom.heightProperty( ).addListener( ( observableValue, number, t1 ) -> {
            msgTextArea.setPrefHeight( (double) t1 );
        } );


        msgTextArea.addEventFilter( KeyEvent.KEY_PRESSED, ke -> {
            if ( ke.getCode( ).equals( KeyCode.ENTER ) ){
                try{
                    sendButtonAction( );
                } catch ( IOException e ){
                    e.printStackTrace( );
                }
                ke.consume( );
            }
        } );


    }

    public void clearActionButton( ){
        msgTextArea.clear( );

    }

    public void sendButtonAction( ) throws IOException{

        String msg_content = msgTextArea.getText( );

        if ( !msg_content.isEmpty( ) ){
            Message currMsg = new Message( MessageType.USER, MessageReceiver.getUserSelf( ), msg_content );


            sendToP2Pcoordinator( currMsg );

            msgTextArea.clear( );
        }

    }


    public void sendToP2Pcoordinator( Message msg ) throws IOException{



        Thread writerThread = new Thread( ( ) -> {
            try{
                SecureConnection.getConnection( ).waitForInitialization( ).writeMessage( msg );
            } catch ( IOException e ){
                Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                           "SendtoP2Pcoordinator error" );
            }
        } );

        writerThread.setName( "WRITER_THREAD" );

        writerThread.start( );



    }

    public void updateChatRoomState( MessageType type, Message msg ){

        switch ( type ){
            case USER:
                addMessageToChat( msg );
                break;
            case REMOVEUSER:
                addMessageToChat( new Message( MessageType.USER, msg.getUser( ), "Signing Off" ).setUserList(
                        msg.getUserList( ) ) );
                removeUserFromChat( msg.getUser( ) );
                break;
            case ADDUSER:
                addNewUserToChat( msg );
                break;
            case STATUS:
                updateOnlineUsersStatusInChat( msg );
                break;
        }


    }


    private void addMessageToChat( Message msg ){
        Platform.runLater( ( ) -> {

            chatMessages.add( msg );
            chatDisplay.scrollTo( chatMessages.indexOf( msg ) );
            syncrhonizeOnlineUsersList( msg.getUserList( ) );

        } );
    }

    public void signOff( ){
        try{
            SecureConnection.getConnection( ).waitForInitialization( ).closeConnection( );
        } catch ( IOException e ){

        } finally{

            Platform.exit( );
        }
    }

    public void addSelfUserToChat( User user ){
        Platform.runLater( ( ) -> onlineUsers.add( user ) );
    }

    private void addNewUserToChat( Message msg ){
        Platform.runLater( ( ) -> syncrhonizeOnlineUsersList( msg.getUserList( ) ) );
    }

    private void removeUserFromChat( User user ){
        Platform.runLater( ( ) -> {
            if ( user.getName( ).equals( MessageReceiver.getUserSelf( ).getName( ) ) ){
                Platform.exit( );
            }

            onlineUsers.remove( user );
        } );



    }

    private void updateSelfStatusInChat( Status status ){


        try{

            onlineUsers.forEach( user -> {
                if ( user.getName( ).equals( MessageReceiver.getUserSelf( ).updateStatus( status ).getName( ) ) ){
                    user.updateStatus( status );

                }
            } );



            sendToP2Pcoordinator( new Message( MessageType.STATUS, MessageReceiver.getUserSelf( ),
                                               null ) );


        } catch ( IOException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                        "Error in status update handler",
                                                                        e );

            Platform.exit( );

        }


    }

    private void syncrhonizeOnlineUsersList( ArrayList<User> userList ){
//        onlineUsers.sort( Comparator.comparing( User::getName ) );
//        userList.sort( Comparator.comparing( User::getName ) );

        Platform.runLater( ( ) -> onlineUsers.setAll( userList ) );


    }

    private void updateOnlineUsersStatusInChat( Message msg ){
        Platform.runLater( ( ) -> {


            syncrhonizeOnlineUsersList( msg.getUserList( ) );
//            onlineUsers.forEach( user1 -> {
//                if ( user1.getName( ).equals( user.getName( ) ) ){
//                    user1.updateStatus( user.getStatus( ) );
//                }
//            } );

        } );

    }


    public void sendMethod( KeyEvent event ) throws IOException{
        if ( event.getCode( ) == KeyCode.ENTER ){
            sendButtonAction( );
        }
    }


    private class MessageListCell extends ListCell<Message>
    {

        @Override
        protected void updateItem( final Message item, final boolean empty ){
            super.updateItem( item, empty );

            if ( empty || item == null ){
                setText( null );
                setGraphic( null );
            } else{
                StackPane pane = new StackPane( );
                Rectangle rect = new Rectangle( );


                Font f = Font.font( "KacstLetter", 16 );
                Text textContent = new Text( );
                Text textName = new Text( );

                textContent.setFont( f );
                textName.setFont( f );



                pane.setAlignment( Pos.CENTER );
                textContent.setTextAlignment( TextAlignment.CENTER );

                textContent.setFill( Paint.valueOf( "white" ) );
                textName.setFill( Paint.valueOf( "white" ) );



                Light.Distant light = new Light.Distant( );


                light.setColor( Color.web( "#c6d7d6" ) );


                Lighting lighting = new Lighting( );

                lighting.setDiffuseConstant( 0.9 );

                lighting.setLight( light );
                lighting.setBumpInput( new Shadow( ) );
                lighting.setSurfaceScale( 4.5 );
                lighting.setSpecularConstant( 0.6 );
                lighting.setSpecularExponent( 29.0 );
                light.setAzimuth( 225.0 );
                light.setElevation( 49.0 );
                rect.setEffect( lighting );


                //Label label = new Label( item.getUser( ).getName( ) + ": " + item.getContent( ) );


                textContent.setWrappingWidth( 280 );


                if ( ( chatMessages.indexOf( item ) % 2 ) == 1 ){
                    //setNodeOrientation( NodeOrientation.LEFT_TO_RIGHT );

                    pane.setAlignment( Pos.CENTER_LEFT );

                    textContent.setText( item.getContent( ) );
                    textName.setText( item.getUser( ).getName( ) );
                    textContent.setTextAlignment( TextAlignment.RIGHT );
                    rect.setFill( Color.web( "#2c9ac6" ) );
                    rect.setHeight( textContent.getBoundsInLocal( ).getHeight( ) + 10 );
                    rect.setWidth( textContent.getBoundsInLocal( ).getWidth( ) +
                                   textName.getBoundsInLocal( ).getWidth( ) + 10 );
//                    rect.setNodeOrientation( NodeOrientation.LEFT_TO_RIGHT );
//                    text.setNodeOrientation( NodeOrientation.LEFT_TO_RIGHT );

                    textName.setX( 0.0 );
                    textContent.setX( textName.getBoundsInLocal( ).getWidth( ) + 20.0 );
                } else{
                    //setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
                    pane.setAlignment( Pos.CENTER_RIGHT );
                    StackPane.setMargin( rect, new Insets( 0, 14, 0, 0 ) );
                    StackPane.setMargin( textContent, new Insets( 0, 14, 0, 0 ) );
                    StackPane.setMargin( textName, new Insets( 0, 14, 0, 0 ) );

                    //String reversName = new StringBuilder( item.getUser().getName()+": " ).reverse().toString();
                    textContent.setText( item.getContent( ) );
                    textName.setText( item.getUser( ).getName( ) );
                    textContent.setTextAlignment( TextAlignment.LEFT );
                    rect.setFill( Color.web( "#53d68e" ) );
                    rect.setHeight( textContent.getBoundsInLocal( ).getHeight( ) + 10 );
                    rect.setWidth(
                            textContent.getBoundsInLocal( ).getWidth( ) + textName.getBoundsInLocal( ).getWidth( ) +
                            10 );
//                    rect.setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
//                    text.setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
                    textContent.setX( 0.0 );
                    textName.setX( textContent.getBoundsInLocal( ).getWidth( ) + 20.0 );

                }


                pane.getChildren( ).addAll( rect, textContent, textName );

                setGraphic( pane );
            }
        }


    }


    private class UserListCell extends ListCell<User>
    {
        @Override
        protected void updateItem( final User item, final boolean empty ){
            super.updateItem( item, empty );
            HBox box = new HBox( 10.0 );

            if ( empty || item == null ){
                setText( null );
                setGraphic( null );
            } else{

                Circle statusSymbol = null;
                if ( item.getName( ).equals( MessageReceiver.getUserSelf( ).getName( ) ) ){
                    if ( myStatusSymbol == null ){
                        statusSymbol = myStatusSymbol = new Circle( 10 );
                    } else{
                        statusSymbol = myStatusSymbol;
                    }
                } else{

                    statusSymbol = new Circle( 10 );
                }


                Label userName = new Label( item.getName( ) );


                userName.setPadding( new Insets( 5.0 ) );

                HBox.setHgrow( statusSymbol, Priority.NEVER );

                box.setAlignment( Pos.BASELINE_LEFT );


                if ( item.getStatus( ) == Status.ONLINE ){
                    statusSymbol.setFill( Paint.valueOf( "green" ) );
                } else if ( item.getStatus( ) == Status.AWAY ){
                    statusSymbol.setFill( Paint.valueOf( "red" ) );
                } else{
                    Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.WARNING,
                                                                               "User with CONTROLLER status " +
                                                                               "in list of online users" );
                }

                box.getChildren( ).addAll( statusSymbol, userName );

                setGraphic( box );
            }
        }


    }


}
