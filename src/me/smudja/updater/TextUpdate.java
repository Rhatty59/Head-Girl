/**
 * 
 */
package me.smudja.updater;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import java.time.Instant;
import java.util.Date;

import org.json.simple.JSONObject;

/**
 * @author smithl
 * 
 * Represents an singular message 'update' from the bot API
 *
 */
public class TextUpdate {
	
	/**
	 * Stores whether or not the update is valid.
	 * true - if update is a message (has 'text' field)
	 * false - if update has no 'text' field (i.e. photo, location, etc.)
	 */
	private boolean valid;
	
	/**
	 * the id of the update
	 */
	private long update_id;
	
	/**
	 * the id of the user sending the update
	 */
	private long user_id;
	
	/**
	 * the user's first name. Note that all users have a first name.
	 */
	private String first_name;
	
	/**
	 * the raw date that the message was sent, measured in milliseconds since 1 Jan 1970
	 */
	private long raw_date;
	
	/** 
	 * the date that the message was sent, as a Date object
	 */
	private Date time_received;
	
	/**
	 * the text of the update. Most likely, the message content.
	 */
	private String text;
	
	/**
	 * Parses the JSONObject into this class.
	 * 
	 * @param jsonUpdate - a JSONObject storing the update.
	 */
	protected TextUpdate(JSONObject jsonUpdate) {
		
		update_id = (long) jsonUpdate.get("update_id");
		user_id = (long) ((JSONObject)((JSONObject)jsonUpdate.get("message")).get("from")).get("id");
		first_name = (String) ((JSONObject)((JSONObject)jsonUpdate.get("message")).get("from")).get("first_name");
		raw_date = ((long) ((JSONObject)jsonUpdate.get("message")).get("date")) * 1000;
		time_received = Date.from(Instant.ofEpochMilli(raw_date));
		
		// if the object contains no text then it is not valid.
		if(!((JSONObject)jsonUpdate.get("message")).containsKey("text")) {
			valid = false;
			text = "";
		}
		else {
			valid = true;
			text = unescapeJava((String) ((JSONObject)jsonUpdate.get("message")).get("text"));
		}	
	}
	
	/**
	 * 
	 * @return valid - whether the update is a valid one i.e. contains 'text'
	 */
	public boolean valid() {
		return valid;
	}
	
	/**
	 * 
	 * @return update_id
	 */
	public long getUpdateId() {
		return update_id;
	}
	
	/**
	 * 
	 * @return user_id
	 */
	public long getUserId() {
		return user_id;
	}
	
	/**
	 * 
	 * @return first_name
	 */
	public String getFirstName() {
		return first_name;
	}
	
	/**
	 * 
	 * @return raw_date
	 */
	public long getRawDate() {
		return raw_date;
	}
	
	/**
	 * 
	 * @return time_received
	 */
	public Date getDate() {
		return time_received;
	}
	
	/**
	 * 
	 * @return text
	 */
	public String getText() {
		return text;
	}

}
