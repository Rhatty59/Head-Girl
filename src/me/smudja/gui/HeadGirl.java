package me.smudja.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class HeadGirl extends Application {
	
	public final static String VERSION = "1.0a";
	
	private static int MAX_UPDATES;

	private static int TIMEOUT;
	
	private static int MESSAGE_LIFE;
	
	private static int UPDATE_FREQUENCY;

	private static int REQUEST_LIMIT;
	
	private static int WINDOW_WIDTH;
	
	private static int WINDOW_HEIGHT;
	
	private static int FONT_SIZE;
	
	private Updater updater;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		updater = new Updater();
		Properties prop = new Properties();
		if(!Files.exists(Paths.get("config.properties"))) {
			try {
				Files.createFile(Paths.get("config.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try(FileOutputStream output = new FileOutputStream("config.properties")) {
				prop.setProperty("max_updates", "9");
				prop.setProperty("timeout", "1");
				prop.setProperty("message_life", "30000");
				prop.setProperty("update_frequency", "10000");
				prop.setProperty("request_limit", "10");
				prop.setProperty("window_width", "1200");
				prop.setProperty("window_height", "600");
				prop.setProperty("font_size", "60");
				prop.store(output, "Configuration file for Head Girl");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try(FileInputStream input = new FileInputStream("config.properties")) {
			prop.load(input);
			MAX_UPDATES = Integer.parseInt(prop.getProperty("max_updates"));
			TIMEOUT = Integer.parseInt(prop.getProperty("timeout"));
			MESSAGE_LIFE = Integer.parseInt(prop.getProperty("message_life"));
			UPDATE_FREQUENCY = Integer.parseInt(prop.getProperty("update_frequency"));
			REQUEST_LIMIT = Integer.parseInt(prop.getProperty("request_limit"));
			WINDOW_WIDTH = Integer.parseInt(prop.getProperty("window_width"));
			WINDOW_HEIGHT = Integer.parseInt(prop.getProperty("window_height"));
			FONT_SIZE = Integer.parseInt(prop.getProperty("font_size"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setTitle("Head Girl v" + VERSION);
		
		FlowPane rootNode = new FlowPane(20, 20);
		rootNode.setAlignment(Pos.CENTER);
		rootNode.setMaxSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		
		primaryStage.setScene(new Scene(rootNode, WINDOW_WIDTH, WINDOW_HEIGHT));
		primaryStage.setResizable(false);
		primaryStage.setFullScreen(true);
		
		Text text = new Text(updater.update());
		
		text.setWrappingWidth(WINDOW_WIDTH);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFont(Font.font(FONT_SIZE));
//		// google "using css in javafx" to find out how to edit the text in an easy way!
//		
        
		rootNode.getChildren().add(text);
        
        Bounds textBounds = text.getBoundsInLocal();
        
        double font_size = text.getFont().getSize();
        
        while(textBounds.getHeight() > WINDOW_HEIGHT) {
        	font_size--;
        	text.setFont(Font.font(font_size));
        	textBounds = text.getBoundsInLocal();
        }
		
		primaryStage.show();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
		        @Override
		        public void run() {
		            Platform.runLater(new Runnable() {

						@Override
						public void run() {
							double font_size = FONT_SIZE;
							
							text.setText(updater.update());
							text.setFont(Font.font(font_size));
							
							Bounds textBounds = text.getBoundsInLocal();
							while(textBounds.getHeight() > WINDOW_HEIGHT) {
								font_size--;
								text.setFont(Font.font(font_size));
								textBounds = text.getBoundsInLocal();
							}
						}
		            	
		            });
		        }
		    }, 0, HeadGirl.UPDATE_FREQUENCY);
	}

	public static int getMaxUpdates() {
		return MAX_UPDATES;
	}

	public static int getTimeout() {
		return TIMEOUT;
	}
	
	public static int getMessageLife() {
		return MESSAGE_LIFE;
	}
	
	public static int getUpdateFrequency() {
		return UPDATE_FREQUENCY;
	}

	public static int getRequestLimit() {
		return REQUEST_LIMIT;
	}
}
