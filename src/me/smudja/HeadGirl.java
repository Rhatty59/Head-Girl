/**
 * 
 */
package me.smudja;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

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
		
		int offset = 145981276;
		
		int limit = 1;
		
		int timeout = 10;
		
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
			InputStream response = connection.getInputStream();
			try (Scanner scanner = new Scanner(response)) {
			    String responseBody = URLDecoder.decode(scanner.useDelimiter("\\A").next(), charset);
			    System.out.println(responseBody);
			}
		} catch (IOException exc) {
			exc.printStackTrace();
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
