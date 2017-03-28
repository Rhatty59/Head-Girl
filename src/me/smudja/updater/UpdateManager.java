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
	 * url - the base url for executing commands
	 */
	private String url;
	
	String charset = java.nio.charset.StandardCharsets.UTF_8.name();
	
	int offset = 0;
	
	int limit = 1;
	
	int timeout = 1;
	
	String[] allowed_updates = {"message"};

	/**
	 * constructor
	 */
	private UpdateManager() {
		try {
			token = new String(Files.readAllBytes(Paths.get("token"))).trim();
			url = "https://api.telegram.org/bot" + URLEncoder.encode(token, charset) + "/";
		}
		catch(IOException exc) {
			exc.printStackTrace();
		}
	}
	
	public Update[] getUpdates() {
		ArrayList<Update> updatesList = new ArrayList<Update>();
		Update update;
		do {
			update = getUpdate();
			if(update.updated()) {
				updatesList.add(update);
			}
		} while(update.updated());
		Update[] updates = new Update[]{};
		return updatesList.toArray(updates);
	}
	
	private Update getUpdate() {
		String query;
		try {
			query = String.format("%s=%s&%s=%s&%s=%s&%s=%s",
						URLEncoder.encode("offset", charset),
						URLEncoder.encode(Integer.toString(offset), charset),
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
			while ((inputStr = streamReader.readLine()) != null)
			    responseStrBuilder.append(inputStr);
			Update response = new Update(responseStrBuilder.toString());
			if(response.ok() && response.updated()) {
				offset = response.getUpdateId() + 1;
			}
			return response;
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			return null;
		} catch (JSONFormatException jsonExc) {
			jsonExc.printStackTrace();
			return null;
		}
	}
}
