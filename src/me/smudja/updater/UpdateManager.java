/**
 * 
 */
package me.smudja.updater;

import java.io.IOException;
import java.net.URL;
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
import me.smudja.gui.HeadGirl;
import me.smudja.utility.LogLevel;
import me.smudja.utility.Reporter;

/**
 * handles getting of updates using APIManager.
 * Parses response from APIManager and removes unauthorised updates
 * 
 * @author smithl
 * @see APIManager
 */
public enum UpdateManager {
	
	INSTANCE;
	
	/**
	 * authorised_users - stores a long[] of valid user ids, whose messages we permit to be displayed. Stored in external file "authorised_users"
	 */
	private long[] authorised_users;
	
	/**
	 * offset - the update to begin looking for updates from, defaults to 0 for all unconfirmed updates.
	 */
	private long offset = 0;
	
	/**
	 * allowed_updates - the type of update we want to receive
	 */
	private String[] allowed_updates = {"message"};
	
	/**
	 * APIManager
	 */
	private APIManager apiManager = APIManager.getInstance();

	/**
	 * constructor reads in config information
	 */
	private UpdateManager() {
		try {
			Path dir = Paths.get("").toAbsolutePath();
			if (!Files.exists(dir.resolve("config/authorised_users"))) {
			    Files.createFile(dir.resolve("config/authorised_users"));
			}
			List<String> authUsersStr = Files.readAllLines(dir.resolve("config/authorised_users"));
			authorised_users = new long[authUsersStr.size()];
			for(String userStr : authUsersStr) {
				authorised_users[authUsersStr.indexOf(userStr)] = Long.parseLong(userStr.trim());
			}
		}
		catch(IOException exc) {
			Reporter.report("Error instantiating UpdateManager. Shutting down...", LogLevel.FATAL);
			System.exit(1);
		}
	}
	
	public static UpdateManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 
	 * @return an array containing all valid and non-expired updates
	 */
	public Update[] getUpdates() {
		ArrayList<Update> updatesList = new ArrayList<Update>();
		Update[] updateArr;
		
		// keep requesting updates from API until there are no more. High limit value will reduce the number of loops here (min 2 loops)
		do {
			updateArr = getUpdate();
			if(!(updateArr == null)) {
				for(Update update : updateArr) {
					updatesList.add(update);
				}
			}
		} while(!(updateArr == null));
		
		long currentTime = System.currentTimeMillis();
		
		// Remove expired updates
		Iterator<Update> iterator = updatesList.iterator();
		while (iterator.hasNext()) {
			Update item = iterator.next();
			if((currentTime - item.getRawDate()) > HeadGirl.getMessageLife()) {
				iterator.remove();
			}
		}
		
		// play ringtone if we have new updates
		if(!updatesList.isEmpty()) {
			ring();
		}
		Update[] updates = new Update[]{};
		return updatesList.toArray(updates);
	}
	
	/**
	 * 
	 * @return an array containing all updates from a single request to the API
	 */
	private Update[] getUpdate() {	
		JSONArray result = apiManager.getUpdate(offset, HeadGirl.getRequestLimit(), HeadGirl.getTimeout(), allowed_updates);

		// if we have no new updates, return
		Boolean updated = (result.size() == 0 ? false : true);
		if(!updated) {
			return null;
		}

		offset = ((long)((JSONObject)result.get(result.size() - 1)).get("update_id")) + 1;

		ArrayList<Update> updatesList = new ArrayList<Update>();

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = result.iterator();

		// for each update, convert it into an Update instance and add to arraylist

		JSONObject update;
		JSONObject message;
		TextUpdate txtUpdate;
		PhotoUpdate photoUpdate;

		while(iterator.hasNext()) {
			update = iterator.next();
			message = (JSONObject) update.get("message");
			if (message.containsKey("text")) {
			// this is a text update
				txtUpdate = new TextUpdate(update);
				updatesList.add(txtUpdate);
				
				Reporter.report(
						"User sent text message: " + txtUpdate.getUserID()
						+ " [" + txtUpdate.getFirstName() + "] " + "- " + txtUpdate.getText()
						, LogLevel.INFO);
			}
			else if (message.containsKey("photo")) {
			// this is a photo update
				photoUpdate = new PhotoUpdate(update);
				updatesList.add(photoUpdate);
				
				Reporter.report(
						"User sent photo message: " + photoUpdate.getUserID()
						+ " [" + photoUpdate.getFirstName() + "]"
						, LogLevel.INFO);
			}
			else {
				Reporter.report("User sent invalid message (no text or photo field)", LogLevel.INFO);
			}
		}

		// remove messages from unauthorised users
		Iterator<Update> updateIterator = updatesList.iterator();
		while(updateIterator.hasNext()) {
			Update sel = updateIterator.next();
			if(!(LongStream.of(authorised_users).anyMatch(x -> x == sel.getUserID()))) {
				Reporter.report("Unauthorised user sent message to bot! User ID: " + sel.getUserID(), LogLevel.INFO);
				updateIterator.remove();
			}
		}

		Update[] updates = new Update[]{};

		return updatesList.toArray(updates);
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
