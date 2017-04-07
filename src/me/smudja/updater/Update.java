/**
 * 
 */
package me.smudja.updater;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import java.time.Instant;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author smithl
 *
 */
public class Update {
	
	private JSONParser parser;
	
	private JSONObject response;
	
	private JSONObject message;
	
	private boolean updated;
	
	private boolean ok;
	
	private long update_id;
	
	private String first_name;
	
	private long raw_date;
	
	private Date time_received;
	
	private String text;
	
	protected Update(String input) {	
		
		parser = new JSONParser();
		try {
			 response = (JSONObject) parser.parse(input);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		ok = (Boolean) response.get("ok");
		if(!ok) {
			return;
		}
		
		updated = (((JSONArray) response.get("result")).size() == 0 ? false : true);
		if(!updated) {
			return;
		}
		
		message = (JSONObject) ((JSONObject) ((JSONArray) response.get("result")).get(0)).get("message");
		
		update_id = (long) ((JSONObject) ((JSONArray) response.get("result")).get(0)).get("update_id");
		first_name = (String) ((JSONObject) message.get("from")).get("first_name");
		raw_date = ((long) message.get("date")) * 1000;
		time_received = Date.from(Instant.ofEpochMilli(raw_date));
		text = unescapeJava((String) message.get("text"));
	}
	
	public boolean updated() {
		return updated;
	}
	
	public boolean ok() {
		return ok;
	}
	
	public long getUpdateId() {
		return update_id;
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
