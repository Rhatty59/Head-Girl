/**
 * 
 */
package me.smudja.updater;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.smudja.gui.HeadGirl;
import utility.LogLevel;
import utility.Reporter;

/**
 * @author smithl
 *
 */
public enum UpdateManager {
	
	INSTANCE;
	
	/**
	 * token - the telegram bot token
	 */
	private String token;
	
	/**
	 * authorised_users - stores a long[] of valid user ids, whose messages we permit to be displayed. Stored in external file "authorised_users"
	 */
	private long[] authorised_users;
	
	/**
	 * url - the base url for executing commands
	 */
	private String url;
	
	/**
	 * charset - the charset to use (currently telegram uses UTF8
	 */
	String charset = java.nio.charset.StandardCharsets.UTF_8.name();
	
	/**
	 * offset - the update to begin looking for updates from, defaults to 0 for all unconfirmed updates.
	 */
	long offset = 0;
	
	/**
	 * limit - the maximum number of updates to get per request
	 */
	int limit = HeadGirl.getRequestLimit();
	
	/**
	 * timeout - the time to wait for updates before giving up
	 */
	int timeout = HeadGirl.getTimeout();
	
	/**
	 * allowed_updates - the type of update we want to receive
	 */
	String[] allowed_updates = {"message"};

	/**
	 * constructor
	 */
	private UpdateManager() {
		try {
			Path dir = Paths.get("").toAbsolutePath();
			if (!Files.exists(dir.resolve("config/authorised_users"))) {
			    Files.createFile(dir.resolve("config/authorised_users"));
			}
			if (!Files.exists(dir.resolve("config/token"))) {
				Files.createFile(dir.resolve("config/token"));
				Reporter.report("No token file found. Please fill in token. Exiting...", LogLevel.INFO);
				System.exit(1);
			}
			token = new String(Files.readAllBytes(dir.resolve("config/token"))).trim();
			List<String> authUsersStr = Files.readAllLines(dir.resolve("config/authorised_users"));
			authorised_users = new long[authUsersStr.size()];
			for(String userStr : authUsersStr) {
				authorised_users[authUsersStr.indexOf(userStr)] = Long.parseLong(userStr.trim());
			}
			url = "https://api.telegram.org/bot" + URLEncoder.encode(token, charset) + "/";
		}
		catch(IOException exc) {
			Reporter.report("Error instantiating UpdateManager. Shutting down...", LogLevel.FATAL);
			System.exit(1);
		}
	}
	
	/**
	 * 
	 * @return an array containing all valid and non-expired updates
	 */
	public TextUpdate[] getUpdates() {
		ArrayList<TextUpdate> updatesList = new ArrayList<TextUpdate>();
		TextUpdate[] updateArr;
		
		// keep requesting updates from API until there are no more. High limit value will reduce the number of loops here (min 2 loops)
		do {
			updateArr = getUpdate();
			if(!(updateArr == null)) {
				for(TextUpdate update : updateArr) {
					updatesList.add(update);
				}
			}
		} while(!(updateArr == null));
		
		long currentTime = System.currentTimeMillis();
		
		// Remove expired updates
		Iterator<TextUpdate> iterator = updatesList.iterator();
		while (iterator.hasNext()) {
			TextUpdate item = iterator.next();
			if((currentTime - item.getRawDate()) > HeadGirl.getMessageLife()) {
				iterator.remove();
			}
		}
		
		// play ringtone if we have new updates
		if(!updatesList.isEmpty()) {
			ring();
		}
		TextUpdate[] updates = new TextUpdate[]{};
		return updatesList.toArray(updates);
	}
	
	/**
	 * 
	 * @return an array containing all updates from a single request to the API
	 */
	private TextUpdate[] getUpdate() {
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
			URLConnection connection = new URL(url + "getUpdates?" + query).openConnection();
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
			
			// if we have no new updates, return
			Boolean updated = (result.size() == 0 ? false : true);
			if(!updated) {
				return null;
			}
			
			// if we have updates set the offset such that these updates are confirmed so won't appear again
			if(ok && updated) {
				offset = ((long)((JSONObject)result.get(result.size() - 1)).get("update_id")) + 1;
			}
			
			ArrayList<TextUpdate> updatesList = new ArrayList<TextUpdate>();
			
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = result.iterator();
			
			// for each update, convert it into an Update instance and add to arraylist
			
			JSONObject update;
			JSONObject message;
			
			while(iterator.hasNext()) {
				update = iterator.next();
				message = (JSONObject) update.get("message");
				if (message.containsKey("text")) {
					updatesList.add(new TextUpdate(update));
				}
				else if (message.containsKey("photo")) {
				}
				else {
					Reporter.report("User sent invalid message (no text or photo field)", LogLevel.INFO);
				}
			}
			
			// remove messages from unauthorised users
			Iterator<TextUpdate> updateIterator = updatesList.iterator();
			while(updateIterator.hasNext()) {
				TextUpdate sel = updateIterator.next();
				if(!(sel.valid()) || !(LongStream.of(authorised_users).anyMatch(x -> x == sel.getUserId()))) {
					Reporter.report("Unauthorised user sent message to bot! User ID: " + sel.getUserId(), LogLevel.INFO);
					updateIterator.remove();
				}
			}
			
			TextUpdate[] updates = new TextUpdate[]{};
			
			return updatesList.toArray(updates);
		} catch (IOException ioExc) {
			Reporter.report("Unable to request update from API (IO Exception). Can't check for updates this run...", LogLevel.MINOR);
			return null;
		}
	}
	
	/**
	 * Plays the ringtone
	 */
	private synchronized void ring() {
		try {
	         // Open an audio input stream.
	         URL url = this.getClass().getClassLoader().getResource("ringtone.wav");
	         AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
	         // Get a sound clip resource.
	         Clip clip = AudioSystem.getClip();
	         // Open audio clip and load samples from the audio input stream.
	         clip.open(audioIn);
	         // plays 3 times (1 + 2 loops)
	         clip.loop(2);
	      } catch (Exception e) {
	    	  Reporter.report("Unable to play ringtone", LogLevel.MINOR);
	      }
	}
}
