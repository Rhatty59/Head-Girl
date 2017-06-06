/**
 * 
 */
package me.smudja.updater;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import org.json.simple.JSONObject;

/**
 *  Represents an singular message 'update' from the bot API
 *  
 * @author smithl
 */
public class TextUpdate extends Update {
	
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
		super(jsonUpdate);
		
		text = unescapeJava((String) ((JSONObject)jsonUpdate.get("message")).get("text"));
	}
	
	/**
	 * 
	 * @return text
	 */
	public String getText() {
		return text;
	}

}
