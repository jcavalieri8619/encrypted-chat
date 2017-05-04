package com.chatsecure.login;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author sriramvaradharajan
 *
 */
public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try{
			Parent root = FXMLLoader.load(getClass().getResource("/com/chatsecure/login/Login.fxml"));
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("/com/chatsecure/login/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
