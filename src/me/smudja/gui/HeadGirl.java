package me.smudja.gui;

import java.text.DateFormat;

import javafx.application.Application;
import javafx.stage.Stage;
import me.smudja.updater.Update;
import me.smudja.updater.UpdateManager;

public class HeadGirl extends Application {
	
	public final static String VERSION = "1.0a";

	public static void main(String[] args) {
		Update[] updates = UpdateManager.INSTANCE.getUpdates();
		for(Update update : updates) {
			System.out.println("Update ID: " + update.getUpdateId());
			System.out.println("[" + update.getFirstName() + "] " + update.getMessage());
			System.out.println("Received: " + DateFormat.getInstance().format(update.getDate()));
		}
		if(updates.length == 0) {
			System.out.println("No Updates...");
		}
	}

	@Override
	public void init() {
		
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub

	}
}
