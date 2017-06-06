package me.smudja.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
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
	 * width of window
	 * (pixels)
	 * needs to be set to screen resolution to ensure text fits correctly
	 */
	private static int WINDOW_WIDTH;
	
	/**
	 * height of window
	 * (pixels)
	 * needs to be set to screen resolution to ensure text fits correctly
	 */
	private static int WINDOW_HEIGHT;
	
	/**
	 * default font size for text display
	 */
	private static int FONT_SIZE;
	
	/**
	 * font color (HTML format)
	 */
	private static Paint FONT_COLOR;
	
	/**
	 * background color (HTML format)
	 */
	private static Paint BACKGROUND_COLOR;
	
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
				prop.setProperty("window_width", "1200");
				prop.setProperty("window_height", "600");
				prop.setProperty("font_size", "60");
				prop.setProperty("font_color", "000000");
				prop.setProperty("background_color", "FFFFFF");
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
			WINDOW_WIDTH = Integer.parseInt(prop.getProperty("window_width", "1200"));
			WINDOW_HEIGHT = Integer.parseInt(prop.getProperty("window_height", "600"));
			FONT_SIZE = Integer.parseInt(prop.getProperty("font_size", "60"));
			FONT_COLOR = Paint.valueOf(prop.getProperty("font_color", "000000"));
			BACKGROUND_COLOR = Paint.valueOf(prop.getProperty("background_color", "FFFFFF"));
		} catch (FileNotFoundException e) {
			Reporter.report("Unable to load properties as file doesn't exist, using defaults", LogLevel.MINOR);
			TIMEOUT = 1;
			MESSAGE_LIFE = 300000;
			UPDATE_FREQUENCY = 10000;
			REQUEST_LIMIT = 10;
			WINDOW_WIDTH = 1200;
			WINDOW_HEIGHT = 600;
			FONT_SIZE = 60;
			FONT_COLOR = Paint.valueOf("000000");
			BACKGROUND_COLOR = Paint.valueOf("FFFFFF");
		} catch (IOException e) {
			Reporter.report("Unable to load properties (IO Exception), using defaults", LogLevel.MINOR);
			TIMEOUT = 1;
			MESSAGE_LIFE = 300000;
			UPDATE_FREQUENCY = 10000;
			REQUEST_LIMIT = 10;
			WINDOW_WIDTH = 1200;
			WINDOW_HEIGHT = 600;
			FONT_SIZE = 60;
			FONT_COLOR = Paint.valueOf("000000");
			BACKGROUND_COLOR = Paint.valueOf("FFFFFF");
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setTitle("Head Girl v" + VERSION);
		
		FlowPane rootNode = new FlowPane();
		rootNode.setAlignment(Pos.CENTER);
		rootNode.setMaxSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		rootNode.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, null, null)));
		
		primaryStage.setScene(new Scene(rootNode, WINDOW_WIDTH, WINDOW_HEIGHT));
		primaryStage.setResizable(true);
		primaryStage.setFullScreen(true);
		
		Object[] first_update = updater.update();
		Text text = new Text((String) first_update[0]);
		ImageView image = new ImageView();
		image.setImage((Image) first_update[1]);
		if(!(image.getImage() == null)) {
			image.setPreserveRatio(true);
			if (image.getImage().getWidth() > WINDOW_WIDTH) {
				image.setFitWidth(WINDOW_WIDTH);
			}
			if (image.getImage().getHeight() > WINDOW_HEIGHT - 50) {
				image.setFitHeight(WINDOW_HEIGHT - 50);
			}
		}
		
		text.setWrappingWidth(WINDOW_WIDTH);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFont(Font.font(FONT_SIZE));		
        text.setFill(FONT_COLOR);
		
		rootNode.getChildren().addAll(image, text);
    
        Bounds textBounds = text.getBoundsInLocal();
        
        double font_size = text.getFont().getSize();
        
        // rescale text size so that it fits on screen
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
							
							Object[] update = updater.update();
							
							text.setText((String) update[0]);
							text.setFont(Font.font(font_size));
							
							image.setImage((Image) update[1]);
							if(!(image.getImage() == null)) {
								image.setPreserveRatio(true);
								if (image.getImage().getWidth() > WINDOW_WIDTH) {
									image.setFitWidth(WINDOW_WIDTH);
								}
								if (image.getImage().getHeight() > WINDOW_HEIGHT - 50) {
									image.setFitHeight(WINDOW_HEIGHT - 50);
								}
							}
							
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
}
