package com.mazesolver.main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MazeSolver extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage mStage) {
		mStage = new Stage();
		Parent root =null ;
		try {
			root = FXMLLoader.load(getClass().getResource("/com/mazesolver/resources/fxml/MainStage.fxml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mStage.setScene(new Scene(root));
		mStage.setTitle("MazeSolver");
		mStage.setOnCloseRequest(e->{
			//Makes sure the running solve thread closes
			System.exit(0);
		});
		mStage.show();
		
	}
}
