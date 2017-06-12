/**
 * 
 */
package me.smudja.updater;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import me.smudja.utility.LogLevel;
import me.smudja.utility.Reporter;

/**
 * actually makes requests to Telegram API.
 * also handles photo retrieval
 * 
 * @author smithl
 *
 */
enum APIManager {
	
	INSTANCE;
	
	/**
	 * token - the telegram bot token
	 */
	private String token;
	
	/**
	 * url - the base url for executing commands
	 */
	private String url;
	
	/** 
	 * bot_code - the bot code part of the url
	 */
	private String bot_code;
	
	/**
	 * charset - the charset to use (currently telegram uses UTF8
	 */
	private String charset = java.nio.charset.StandardCharsets.UTF_8.name();

	/**
	 * constructor
	 */
	private APIManager() {
		try {
			Path dir = Paths.get("").toAbsolutePath();
			if (!Files.exists(dir.resolve("config/token"))) {
				Files.createFile(dir.resolve("config/token"));
				Reporter.report("No token file found. Please fill in token. Exiting...", LogLevel.INFO);
				System.exit(1);
			}
			token = new String(Files.readAllBytes(dir.resolve("config/token"))).trim();
			url = "https://api.telegram.org/";
			bot_code = "bot" + token + "/";
		}
		catch(IOException exc) {
			Reporter.report("Error instantiating APIManager. Shutting down...", LogLevel.FATAL);
			System.exit(1);
		}
	}
	
	public static APIManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 *  request update from API
	 * @return an array containing all updates from a single request to the API
	 */
	public JSONArray getUpdate(long offset, int limit, int timeout, String[] allowed_updates) {
		String query;
		try {
			query = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
						URLEncoder.encode("offset", charset),
						URLEncoder.encode(Long.toString(offset), charset),
						URLEncoder.encode("limit", charset),
						URLEncoder.encode(Integer.toString(limit), charset),
						URLEncoder.encode("timeout", charset),
						URLEncoder.encode(Integer.toString(timeout), charset),
						URLEncoder.encode("allowed_updates", charset),
						URLEncoder.encode("[\"" + String.join("\", \"", allowed_updates) + "\"]", charset)
					);
		} catch (UnsupportedEncodingException exc) {
			Reporter.report("Unable to encode query. Can't check for updates this run...", LogLevel.MAJOR);
			return null;
		}
		
		try {
			// open connection to API and get string response
			URLConnection connection = new URL(url + bot_code + "getUpdates?" + query).openConnection();
			InputStream responseStream = connection.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, charset)); 
			StringBuilder responseStrBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = streamReader.readLine()) != null) {
				 responseStrBuilder.append(inputStr);
			}
			
			JSONParser parser = new JSONParser();
			
			// parse response into a JSONObject
			JSONObject response;
			try {
				 response = (JSONObject) parser.parse(responseStrBuilder.toString());
			} catch (ParseException e) {
				Reporter.report("Unable to parse reponse from API. Can't check for updates this run...", LogLevel.MINOR);
				return null;
			}
			
			Boolean ok = (Boolean) response.get("ok");
			if(!ok) {
				Reporter.report("Response from API not 'ok'. Can't check for updates this run...", LogLevel.MINOR);
				return null;
			}
			
			JSONArray result = (JSONArray) response.get("result");
			
			return result;
		} catch (IOException ioExc) {
			Reporter.report("Unable to request update from API (IO Exception). Can't check for updates this run...", LogLevel.MINOR);
			return null;
		}
	}
	
	/**
	 * request image file from API
	 * @param file_id - the ID of the file we want (given to us by getUpdates)
	 * @return image from API
	 */
	public Image getImageFile(String file_id) {

		String query;
		try {
			query = String.format("%s=%s",
						URLEncoder.encode("file_id", charset),
						URLEncoder.encode(file_id, charset)
					);
		} catch (UnsupportedEncodingException exc) {
			Reporter.report("Unable to encode query. Can't check for updates this run...", LogLevel.MAJOR);
			return null;
		}
		
		try {
			// open connection to API and get string response
			URLConnection connection = new URL(url + bot_code + "getFile?" + query).openConnection();
			InputStream responseStream = connection.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, charset)); 
			StringBuilder responseStrBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = streamReader.readLine()) != null) {
				 responseStrBuilder.append(inputStr);
			}
			
			JSONParser parser = new JSONParser();
			
			// parse response into a JSONObject
			JSONObject response;
			try {
				 response = (JSONObject) parser.parse(responseStrBuilder.toString());
			} catch (ParseException e) {
				Reporter.report("Unable to parse reponse from API. Can't display photo...", LogLevel.MINOR);
				response = null;
			}
			
			Boolean ok = (Boolean) response.get("ok");
			if(!ok) {
				Reporter.report("Response from API not 'ok'. Can't display photo...", LogLevel.MINOR);
			}
			
			JSONObject result = (JSONObject) response.get("result");
			
			String file_path = (String) result.get("file_path");
	        
			BufferedImage photoBuff = ImageIO.read(new URL(url + "file/" + bot_code + file_path));
			
	        Image photo = SwingFXUtils.toFXImage(photoBuff, null);
	        
	        return photo;
		}
		catch (IOException ioExc) {
			Reporter.report("Unable to request photo from API (IO Exception). Can't display photo", LogLevel.MINOR);
			return null;
		}
		
	}
}
