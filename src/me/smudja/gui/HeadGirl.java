package me.smudja.gui;

import java.text.DateFormat;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import me.smudja.updater.Update;
import me.smudja.updater.UpdateManager;

public class HeadGirl extends Application {
	
	public final static String VERSION = "1.0a";

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setTitle("Head Girl v" + VERSION);
		
		FlowPane rootNode = new FlowPane(20, 20);
		rootNode.setAlignment(Pos.CENTER);
		
		primaryStage.setScene(new Scene(rootNode, 1200, 600));		
		
		String text;
		StringBuilder textBuilder = new StringBuilder();
		for(Update update : UpdateManager.INSTANCE.getUpdates()) {
			textBuilder.append("Update ID: " + update.getUpdateId() + "\n");
			textBuilder.append("[" + update.getFirstName() + "] " + update.getMessage() + "\n");
			textBuilder.append("Received: " + DateFormat.getInstance().format(update.getDate()) + "\n");
			System.out.println();
		}
		if(textBuilder.length() == 0) {
			text = "No Updates To Display...";
		}
		else {
			text = textBuilder.toString();
		}
		
		TextArea display = new TextArea(text);
		display.setEditable(false);
		display.setPrefSize(1200, 600);
		
		rootNode.getChildren().add(display);
		
		primaryStage.show();
	}
}
