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

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import utility.LogLevel;
import utility.Reporter;

/**
 * @author smithl
 *
 */
public class PhotoUpdate extends Update {
	
	Image photo;

	/**
	 * 
	 */
	public PhotoUpdate(JSONObject jsonUpdate) {
		super(jsonUpdate);
		// we know this is a photo
		
		JSONArray photoSizes = (JSONArray) ((JSONObject) jsonUpdate.get("message")).get("photo");
		
		int sizeChoice = photoSizes.size() - 1;
		JSONObject photoChoice;
		
		do {
			if(sizeChoice < 0) {
				Reporter.report("Photo has no actionable file size, will not be displayed", LogLevel.MINOR);
			}
			photoChoice = (JSONObject) photoSizes.get(sizeChoice);
			sizeChoice--;
		} while(((long) photoChoice.get("file_size")) >= 20000000);
		
		String file_id = (String) photoChoice.get("file_id");

		String query;
		try {
			query = String.format("%s=%s",
						URLEncoder.encode("file_id", UpdateManager.INSTANCE.charset),
						URLEncoder.encode(file_id, UpdateManager.INSTANCE.charset)
					);
		} catch (UnsupportedEncodingException exc) {
			Reporter.report("Unable to encode query. Can't check for updates this run...", LogLevel.MAJOR);
			query = null;
		}
		
		try {
			// open connection to API and get string response
			URLConnection connection = new URL(UpdateManager.INSTANCE.getURL() + "getFile?" + query).openConnection();
			InputStream responseStream = connection.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, UpdateManager.INSTANCE.charset)); 
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
			String token = URLEncoder.encode(UpdateManager.INSTANCE.getToken(), UpdateManager.INSTANCE.charset);
			
			URL url = new URL("https://api.telegram.org/file/bot" + token + "/" + file_path);
	        
			BufferedImage photoBuff = ImageIO.read(url);
			
	        photo = SwingFXUtils.toFXImage(photoBuff, null);
		}
		catch (IOException ioExc) {
			Reporter.report("Unable to request photo from API (IO Exception). Can't display photo", LogLevel.MINOR);
		}
		
	}
	
	public Image getPhoto() {
		return photo;
	}

}
