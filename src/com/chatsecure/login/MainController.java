package com.chatsecure.login;


import com.chatsecure.client.MessageReceiver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sriramvaradharajan
 */

public class MainController
{

    @FXML
    private Label lblStatus;

    @FXML
    private TextField txtUserName;

    @FXML
    private TextField txtPassword;

    @FXML
    private CheckBox cboxp2pcoordinator;

    @FXML
    private TextField hostAddress;

    @FXML
    private Button btnSignIn;
    private MessageReceiver rcvr;
    private Thread rcvr_thread;

    static List<String> users = new ArrayList<String>( );

    static{
        users.add( "jpc" );
        users.add( "sv" );
        users.add( "cs" );
        users.add( "ua" );
        users.add( "pg" );
    }

    public void signin( ActionEvent event ){
        if ( users.contains( txtUserName.getText( ) ) && txtPassword.getText( ).equals( "password" ) ){
            lblStatus.setText( "Login success" );
            try{
                FXMLLoader loader = new FXMLLoader(
                        getClass( ).getResource( "/com/chatsecure/client/resources/views/chat_window.fxml" ) );
                Parent root = loader.load( );
                Scene scene = new Scene( root );
                Stage stage = (Stage) btnSignIn.getScene( ).getWindow( );
                stage.setScene( scene );

                try{
                    if ( cboxp2pcoordinator.isSelected( ) ){
                        rcvr_thread = new Thread(
                                rcvr = new MessageReceiver( 5320, loader.getController( ), txtUserName.getText( ) ) );
                        rcvr_thread.setName( "MSG_RCVR_4_P2P" );
                        rcvr_thread.start( );
                    } else{
                        rcvr_thread = new Thread(
                                rcvr = new MessageReceiver( hostAddress.getText( ), 5320, loader.getController( ),
                                                            txtUserName.getText( ) ) );
                        rcvr_thread.setName( "MSG_RCVR_4_USER" );
                        rcvr_thread.start( );

                    }
                } catch ( Exception e ){
                    e.printStackTrace( );
                }
                lblStatus.setText( "Login Succeeded" );
                stage.show( );
            } catch ( IOException e ){
                e.printStackTrace( );
            }

        } else{
            lblStatus.setText( "Login Failed" );
        }
    }

    public void checkp2p( MouseEvent event ){
        if ( cboxp2pcoordinator.isSelected( ) ){
            hostAddress.setVisible( false );
        } else{
            hostAddress.setVisible( true );
        }


    }
}


