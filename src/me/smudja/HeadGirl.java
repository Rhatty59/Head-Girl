/**
 * 
 */
package me.smudja;

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
import java.text.DateFormat;

/**
 * @author smithl
 *
 */
public class HeadGirl {
	
	/**
	 * token - the telegram bot token
	 */
	private String token;
	
	/**
	 * url - the base url for executing commands
	 */
	private String url;

	/**
	 * constructor
	 */
	public HeadGirl() {
		try {
			token = new String(Files.readAllBytes(Paths.get("token"))).trim();
			url = "https://api.telegram.org/bot" + URLEncoder.encode(token) + "/";
			// TODO encode token with correct encoding
		}
		catch(IOException exc) {
			exc.printStackTrace();
		}
	}
	
	private void getUpdates() {
		String charset = java.nio.charset.StandardCharsets.UTF_8.name();
		
		int offset = 145981290;
		
		int limit = 1;
		
		int timeout = 2;
		
		String[] allowed_updates = {"message"};
		
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
			query = "";
		}
		
		try {
			URLConnection connection = new URL(url + "getUpdates?" + query).openConnection();
			InputStream responseStream = connection.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, charset)); 
			StringBuilder responseStrBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = streamReader.readLine()) != null)
			    responseStrBuilder.append(inputStr);
			JSONObject response = new JSONObject(responseStrBuilder.toString());
			if(!response.ok()) {
				System.out.println("ERROR: Could not check for updates");
			}
			else if(!response.updated()) {
				System.out.println("No updates yet...");
			}
			else {
				System.out.println("Update ID: " + response.getUpdateId());
				System.out.println("[" + response.getFirstName() + "] " + response.getMessage());
				System.out.println("Received: " + DateFormat.getInstance().format(response.getDate()));
			}
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
		} catch (JSONFormatException jsonExc) {
			jsonExc.printStackTrace();
		}
	}
			
			
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HeadGirl headGirl = new HeadGirl();
		headGirl.getUpdates();
	}

}
