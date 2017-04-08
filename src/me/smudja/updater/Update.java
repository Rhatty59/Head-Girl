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
 */
public class Update {
	
	private boolean valid;
	
	private long update_id;
	
	private long user_id;
	
	private String first_name;
	
	private long raw_date;
	
	private Date time_received;
	
	private String text;
	
	protected Update(JSONObject jsonUpdate) {
		
		update_id = (long) jsonUpdate.get("update_id");
		user_id = (long) ((JSONObject)((JSONObject)jsonUpdate.get("message")).get("from")).get("id");
		first_name = (String) ((JSONObject)((JSONObject)jsonUpdate.get("message")).get("from")).get("first_name");
		raw_date = ((long) ((JSONObject)jsonUpdate.get("message")).get("date")) * 1000;
		time_received = Date.from(Instant.ofEpochMilli(raw_date));
		
		if(!((JSONObject)jsonUpdate.get("message")).containsKey("text")) {
			valid = false;
			text = "";
		}
		else {
			valid = true;
			text = unescapeJava((String) ((JSONObject)jsonUpdate.get("message")).get("text"));
		}	
	}
	
	public boolean valid() {
		return valid;
	}
	
	public long getUpdateId() {
		return update_id;
	}
	
	public long getUserId() {
		return user_id;
	}
	
	public String getFirstName() {
		return first_name;
	}
	
	public long getRawDate() {
		return raw_date;
	}
	
	public Date getDate() {
		return time_received;
	}
	
	public String getText() {
		return text;
	}

}
