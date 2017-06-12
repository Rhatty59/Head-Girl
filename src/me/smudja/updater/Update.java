package me.smudja.updater;

import java.time.Instant;
import java.util.Date;

import org.json.simple.JSONObject;

import javafx.scene.Node;

/**
 * Superclass for TextUpdate and PhotoUpdate classes
 * 
 * @author smithl
 * @see TextUpdate, PhotoUpdate
 */
public abstract class Update {
	
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
	
	protected Update(JSONObject jsonUpdate) {
		update_id = (long) jsonUpdate.get("update_id");
		user_id = (long) ((JSONObject)((JSONObject)jsonUpdate.get("message")).get("from")).get("id");
		first_name = (String) ((JSONObject)((JSONObject)jsonUpdate.get("message")).get("from")).get("first_name");
		raw_date = ((long) ((JSONObject)jsonUpdate.get("message")).get("date")) * 1000;
		time_received = Date.from(Instant.ofEpochMilli(raw_date));
	}
	
	public abstract Node getNode();
	
	/**
	 * @return the update_id
	 */
	public long getUpdateID() {
		return update_id;
	}

	/**
	 * @return the user_id
	 */
	public long getUserID() {
		return user_id;
	}

	/**
	 * @return the first_name
	 */
	public String getFirstName() {
		return first_name;
	}

	/**
	 * @return the raw_date
	 */
	public long getRawDate() {
		return raw_date;
	}

	/**
	 * @return the time_received
	 */
	public Date getTimeReceived() {
		return time_received;
	}
	
	

}
