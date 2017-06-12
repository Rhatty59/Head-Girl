package me.smudja.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.smudja.utility.LogLevel;
import me.smudja.utility.Reporter;

public class HeadGirl extends Application {
	
	/**
	 * version
	 */
	public final static String VERSION = "1.0";
	
	/**
	 * the time to wait for a response from the API
	 * (seconds)
	 */
	private static int TIMEOUT;
	
	/**
	 * how long messages should persist before they expire
	 * (milliseconds)
	 */
	private static int MESSAGE_LIFE;
	
	/**
	 * how often to check for updates
	 * (milliseconds)
	 */
	private static int UPDATE_FREQUENCY;
	
	/**
	 * maximum number of updates to get at once
	 */
	private static int REQUEST_LIMIT;
	
	/**
	 * default font size for text display
	 */
	private static int FONT_SIZE;
	
	private Rectangle2D screenBounds;
	
	/**
	 * updater instance
	 */
	private Updater updater;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		updater = Updater.getInstance();
		Properties prop = new Properties();
		Path dir = Paths.get("").toAbsolutePath();
		// if no properties file, create it
		if(!Files.exists(dir.resolve("config/config.properties"))) {
			try {
				Files.createDirectory(dir.resolve("config"));
				Files.createFile(dir.resolve("config/config.properties"));
			} 
			catch (FileAlreadyExistsException faeExc) {}
			catch (IOException e) {
				Reporter.report("Unable to create properties file. Will try again on next init", LogLevel.MINOR);
			}
			try(FileOutputStream output = new FileOutputStream("config/config.properties")) {
				prop.setProperty("timeout", "1");
				prop.setProperty("message_life", "300000");
				prop.setProperty("update_frequency", "10000");
				prop.setProperty("request_limit", "10");
				prop.setProperty("font_size", "60");
				prop.store(output, "Configuration file for Head Girl");
			} catch (FileNotFoundException e) {
				Reporter.report("Unable to store default properties as file does not exist.", LogLevel.MINOR);
			} catch (IOException e) {
				Reporter.report("Unable to store default properties (IOException)", LogLevel.MINOR);
			}
		}

		try(FileInputStream input = new FileInputStream("config/config.properties")) {
			prop.load(input);
			TIMEOUT = Integer.parseInt(prop.getProperty("timeout", "1"));
			MESSAGE_LIFE = Integer.parseInt(prop.getProperty("message_life", "300000"));
			UPDATE_FREQUENCY = Integer.parseInt(prop.getProperty("update_frequency", "10000"));
			REQUEST_LIMIT = Integer.parseInt(prop.getProperty("request_limit", "10"));
			FONT_SIZE = Integer.parseInt(prop.getProperty("font_size", "60"));
		} catch (FileNotFoundException e) {
			Reporter.report("Unable to load properties as file doesn't exist, using defaults", LogLevel.MINOR);
			TIMEOUT = 1;
			MESSAGE_LIFE = 300000;
			UPDATE_FREQUENCY = 10000;
			REQUEST_LIMIT = 10;
			FONT_SIZE = 60;
		} catch (IOException e) {
			Reporter.report("Unable to load properties (IO Exception), using defaults", LogLevel.MINOR);
			TIMEOUT = 1;
			MESSAGE_LIFE = 300000;
			UPDATE_FREQUENCY = 10000;
			REQUEST_LIMIT = 10;
			FONT_SIZE = 60;
		}
		screenBounds = Screen.getPrimary().getVisualBounds();
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setTitle("Head Girl v" + VERSION);
		
		BorderPane rootNode = new BorderPane();
    	primaryStage.setScene( new Scene( rootNode ) );
    	primaryStage.setFullScreen(true);
    	primaryStage.setFullScreenExitHint("");
    	primaryStage.getScene().getStylesheets().add("headgirl.css");
    	
    	rootNode.setTop(top());
    	rootNode.setCenter(center(updater.update()));
		
		primaryStage.show();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
		        @Override
		        public void run() {
		            Platform.runLater(new Runnable() {

						@Override
						public void run() {
							Object[] update = updater.update();
							
							rootNode.setCenter(center(update));
						}
		            	
		            });
		        }
		    }, 0, HeadGirl.UPDATE_FREQUENCY);
	}

	/**
	 * 
	 * @return timeout
	 */
	public static int getTimeout() {
		return TIMEOUT;
	}
	
	/**
	 * 
	 * @return message_life
	 */
	public static int getMessageLife() {
		return MESSAGE_LIFE;
	}
	
	/**
	 * 
	 * @return update_frequency
	 */
	public static int getUpdateFrequency() {
		return UPDATE_FREQUENCY;
	}

	/**
	 * 
	 * @return request_limit
	 */
	public static int getRequestLimit() {
		return REQUEST_LIMIT;
	}
	
	private FlowPane top() {
		FlowPane topNode = new FlowPane();
        topNode.setAlignment(Pos.CENTER_RIGHT);
        
        Label timeLbl = new Label();
        timeLbl.setId("time");
        topNode.getChildren().add(timeLbl);
        
        DateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
        final Timeline timeline = new Timeline(
            new KeyFrame(
                Duration.millis( 500 ),
                event -> {
                    timeLbl.setText( timeFormat.format( System.currentTimeMillis() ) );
                }
            )
        );
        timeline.setCycleCount( Animation.INDEFINITE );
        timeline.play();
        
        return topNode;
	}
	
	private Node center(Object[] update) {
		FlowPane centerNode = new FlowPane();
		centerNode.setAlignment(Pos.CENTER);
		centerNode.setId("center");
		
		Text text = new Text((String) update[0]);
		ImageView image = new ImageView();
		image.setImage((Image) update[1]);
		if(!(image.getImage() == null)) {
			image.setPreserveRatio(true);
			if (image.getImage().getWidth() > screenBounds.getWidth()) {
				image.setFitWidth(screenBounds.getWidth());
			}
			if (image.getImage().getHeight() > (screenBounds.getHeight() - 150)) {
				image.setFitHeight(screenBounds.getHeight() - 150);
			}
		}
		
		text.setWrappingWidth(screenBounds.getWidth());
		text.setTextAlignment(TextAlignment.CENTER);
		
		centerNode.getChildren().addAll(image, text);
		
		text.setId("center-text");
		text.applyCss();
		text.setFont(Font.font(FONT_SIZE));
    
        Bounds textBounds = text.getLayoutBounds();
        
        double font_size = text.getFont().getSize();
        
        // rescale text size so that it fits on screen
        while(textBounds.getHeight() > (screenBounds.getHeight()- 50)) {
        	font_size--;
        	text.setFont(Font.font(font_size));
        	textBounds = text.getLayoutBounds();
        }
		
		return centerNode;
	}
}
