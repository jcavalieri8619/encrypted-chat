package com.chatsecure.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;


public class GUI extends Application
{

    private static Stage theStage;

    private Thread t;

    public static Stage getPrimaryStage( ){
        return theStage;
    }


    @Override
    public void start( Stage primaryStage ) throws Exception{

        FXMLLoader loader = new FXMLLoader( getClass( ).getResource( "resources/views/chat_window.fxml" ) );

        Parent root = loader.load( );

        ChatController controller = loader.getController( );

        BorderPane actualRoot = (BorderPane) root;


        //to access stage from other classes; set new to chat display after login screen
        theStage = primaryStage;


//
//        StackPane root = new StackPane( );
//        Rectangle rect = new Rectangle( );
//        Font f = Font.font( "KacstLetter", 18 );
//        Text text = new Text( "Hello JPC, how are you today? are you coming tomorrow" );
//        text.setFont( f );
//        text.setWrappingWidth( 200 );
//        root.getChildren( ).addAll( rect, text );
//        root.setAlignment( Pos.CENTER );
//        rect.setFill( Color.web( "#53d68e" ) );
//
//        Light.Distant light = new Light.Distant( );
//
//
//        light.setColor( Color.web( "#c6d7d6" ) );
//
//
//        Lighting lighting = new Lighting( );
//
//        lighting.setDiffuseConstant( 0.9 );
//
//        lighting.setLight( light );
//        lighting.setBumpInput( new Shadow( ) );
//        lighting.setSurfaceScale( 4.5 );
//        lighting.setSpecularConstant( 0.6 );
//        lighting.setSpecularExponent( 29.0 );
//        light.setAzimuth( 225.0 );
//        light.setElevation( 49.0 );
//        rect.setEffect( lighting );
//        rect.setHeight( text.getBoundsInLocal( ).getHeight( )+10 );
//        rect.setWidth( text.getBoundsInLocal( ).getWidth( ) +20);
//
//        rect.setArcHeight( 20 );
//        rect.setArcWidth( 30 );
//
//        text.setFill( Paint.valueOf( "white" ) );


        // primaryStage.initStyle( StageStyle.TRANSPARENT);


        //primaryStage.setFullScreen( true );


        Scene scene = new Scene( root );


        //TESTING out some random stuff

        // Use Java Collections to create the List.
//        Map<String, String> map = new HashMap<String, String>( );
//
//        map.put( "preKey1", "preVal1" );
//        map.put( "preKey2", "preVal2" );
//
//
//        // Now add observability by wrapping it with ObservableList.
//        ObservableMap<String, String> observableMap = FXCollections.observableMap( map );
//        observableMap.addListener( (MapChangeListener) change ->
//        {
//            System.out.println( "inside Map handler: " + change.wasAdded( ) + ",  " + change.getValueAdded( ) );
//        } );
//
//        // Changes to the observableMap WILL be reported.
//        observableMap.put( "key 1", "value 1" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//        observableMap.put( "key 3", "value 3" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//
//        observableMap.put( "key 3", "value 66" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//
//        // Changes to the underlying map will NOT be reported.
//        map.put( "key 2", "value 2" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//
//        List<String> list = new ArrayList<String>( );
//        list.add( "pre1" );
//        list.add( "pre2" );
//
//        // Now add observability by wrapping it with ObservableList.
//        ObservableList<String> observableList = FXCollections.observableList( list );
//        observableList.addListener( (ListChangeListener) change -> {
//            int k = 0;
//            while ( change.next( ) ){
//
//                System.out.println( "inside list handler " + ( k++ ) + ": " + change.getList( ) );
//            }
//        } );
//
//        // Changes to the observableList WILL be reported.
//        // This line will print out "Detected a change!"
//        observableList.add( "item one" );
//        observableList.add( "item TWO" );
//        observableList.add( "item THREEE" );
//
//        // Changes to the underlying list will NOT be reported
//        // Nothing will be printed as a result of the next line.
//        list.add( "item two" );
//
//        System.out.println( "Size: " + observableList.size( ) );


        //

        // ServerSocket serv = new ServerSocket( 5463 );
//        final Socket[] remoteSock = new Socket[ 1 ];
//
//        Thread t = new Thread( ( ) -> {
//            try{
//                remoteSock[ 0 ] = serv.accept( );
//            } catch ( IOException e ){
//                e.printStackTrace( );
//            }
//        } );
//         t.start();

//
//        Socket localSock = new Socket( "192.168.1.2", 5463 );
//        Socket remoteSock = serv.accept();
//        OutputStream localOutStream = localSock.getOutputStream();
//        InputStream localInStream = localSock.getInputStream();
//
//        OutputStream remoteOutStream = remoteSock.getOutputStream( );
//        InputStream remoteInStream = remoteSock.getInputStream( );
//
//        Message msg = new Message( MessageType.USER, new User( "JPC", Status.ONLINE ), "attack at dawn" );
//        ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
//        ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );
//
//        byte[] msg_bytes;
//        to_byte_stream.writeObject( msg );
//        msg_bytes = byte_stream_in.toByteArray( );
//
//        localOutStream.write( msg_bytes,0,msg_bytes.length );
//
//
//        byte[] in_buff = new byte[ 8192 ];
//        remoteInStream.read( in_buff, 0, 8192 );
//
//        Message new_msg;
//
//         ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( in_buff );
//              ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );
//
//            new_msg = (Message) from_byte_stream.readObject( );
//
//        System.out.println("TESTING OUTPUT: "+new_msg );
//


        primaryStage.setTitle( "SecureChat" );
        primaryStage.setScene( scene );
        primaryStage.show( );


        MessageReceiver rcvr;

        t = new Thread( ( rcvr = new MessageReceiver( 5323, controller, "JPC" ) ) );

        t.setName( "MSG-RCVR" );
        t.start();

        Thread t2 = new Thread( ( ) -> {
            try{
                controller.sendToP2Pcoordinator( new Message( MessageType.USER, MessageReceiver.getUserSelf( )
                        , "hello old boy" ) );
            } catch ( IOException e ){
                e.printStackTrace( );
            }
        } );

        t2.setName( "TEST-SENDER" );
        t2.start( );
    }


    public static void main( String[] args ){
        launch( args );
    }
}
