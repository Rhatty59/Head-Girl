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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.smudja.gui.HeadGirl;

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
	
	private long[] authorised_users;
	
	/**
	 * url - the base url for executing commands
	 */
	private String url;
	
	String charset = java.nio.charset.StandardCharsets.UTF_8.name();
	
	long offset = 0;
	
	int limit = HeadGirl.REQUEST_LIMIT;
	
	int timeout = HeadGirl.TIMEOUT;
	
	String[] allowed_updates = {"message"};

	/**
	 * constructor
	 */
	private UpdateManager() {
		try {
			if (!Files.exists(Paths.get("authorised_users"))) {
			    Files.createFile(Paths.get("authorised_users"));
			}
			token = new String(Files.readAllBytes(Paths.get("token"))).trim();
			List<String> authUsersStr = Files.readAllLines(Paths.get("authorised_users"));
			authorised_users = new long[authUsersStr.size()];
			for(String userStr : authUsersStr) {
				authorised_users[authUsersStr.indexOf(userStr)] = Long.parseLong(userStr.trim());
			}
			url = "https://api.telegram.org/bot" + URLEncoder.encode(token, charset) + "/";
		}
		catch(IOException exc) {
			exc.printStackTrace();
		}
	}
	
	public Update[] getUpdates() {
		ArrayList<Update> updatesList = new ArrayList<Update>();
		Update[] updateArr;
		do {
			updateArr = getUpdate();
			if(!(updateArr == null)) {
				for(Update update : updateArr) {
					updatesList.add(update);
				}
			}
		} while(!(updateArr == null));
		Update[] updates = new Update[]{};
		return updatesList.toArray(updates);
	}
	
	private Update[] getUpdate() {
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
			exc.printStackTrace();
			return null;
		}
		
		try {
			URLConnection connection = new URL(url + "getUpdates?" + query).openConnection();
			InputStream responseStream = connection.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, charset)); 
			StringBuilder responseStrBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = streamReader.readLine()) != null) {
				 responseStrBuilder.append(inputStr);
			}
			
			JSONParser parser = new JSONParser();
			
			JSONObject response;
			try {
				 response = (JSONObject) parser.parse(responseStrBuilder.toString());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
			
			Boolean ok = (Boolean) response.get("ok");
			if(!ok) {
				return null;
			}
			
			JSONArray result = (JSONArray) response.get("result");
			
			Boolean updated = (result.size() == 0 ? false : true);
			if(!updated) {
				return null;
			}
			
			ArrayList<Update> updatesList = new ArrayList<Update>();
			
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = result.iterator();
			
			while(iterator.hasNext()) {
				updatesList.add(new Update(iterator.next()));
			}
			
			if(ok && updated) {
				offset = updatesList.get(updatesList.size() - 1).getUpdateId() + 1;
			}
			
			Iterator<Update> updateIterator = updatesList.iterator();
			while(updateIterator.hasNext()) {
				Update sel = updateIterator.next();
				if(!(sel.valid()) || !(LongStream.of(authorised_users).anyMatch(x -> x == sel.getUserId()))) {
					updateIterator.remove();
				}
			}
			
			Update[] updates = new Update[]{};
			
			return updatesList.toArray(updates);
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			return null;
		}
	}
}
