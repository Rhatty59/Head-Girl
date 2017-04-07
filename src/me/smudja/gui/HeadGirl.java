package me.smudja.gui;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class HeadGirl extends Application {
	
	public final static String VERSION = "1.0a";
	
	public final static int MAX_UPDATES = 9;

	public final static int TIMEOUT = 1;
	
	public final static int MESSAGE_LIFE = 30000;
	
	public final static int UPDATE_FREQUENCY = 10000;
	
	private Updater updater;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		updater = new Updater();
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setTitle("Head Girl v" + VERSION);
		
		FlowPane rootNode = new FlowPane(20, 20);
		rootNode.setAlignment(Pos.CENTER);
		
		primaryStage.setScene(new Scene(rootNode, 1200, 600));		
		
		TextArea display = new TextArea(updater.update());
		display.setEditable(false);
		display.setPrefSize(1200, 600);
		// google "using css in javafx" to find out how to edit the text in an easy way!
		
		rootNode.getChildren().add(display);
		
		primaryStage.show();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
		        @Override
		        public void run() {
		            Platform.runLater(new Runnable() {

						@Override
						public void run() {
							display.setText(updater.update());
						}
		            	
		            });
		        }
		    }, 0, HeadGirl.UPDATE_FREQUENCY);
	}
}
