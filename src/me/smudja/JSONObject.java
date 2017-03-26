/**
 * 
 */
package me.smudja;

import java.time.Instant;
import java.util.Date;

/**
 * @author smithl
 *
 */
public class JSONObject {
	
	private boolean updated;
	
	private boolean ok;
	
	private int update_id;
	
	private String first_name;
	
	private Date date_received;
	
	private String message;
	
	public JSONObject(String input) throws JSONFormatException {		
		
		this.checkOk(input);
		this.checkUpdated(input);
		
		if(updated == false || ok == false) {
			return;
		}
		
		this.getUpdateId(input);
		this.getName(input);
		this.getDateReceived(input);
		this.getMessage(input);
	}

	private void checkOk(String input) throws JSONFormatException {
		int idxOk = input.indexOf("ok") + 4;
		if(idxOk == -1) {
			throw new JSONFormatException();
		}
		StringBuilder okBldr = new StringBuilder();
		while(input.charAt(idxOk) != ',') {
			okBldr.append(input.charAt(idxOk));
			idxOk++;
		}
		String strOk = okBldr.toString();
		if(strOk.compareTo("true") == 0) {
			ok = true;
		}
		else {
			ok = false;
		}
	}
	
	private void checkUpdated(String input) throws JSONFormatException {
		int idxResult = input.indexOf("result") + 9;
		if(idxResult == -1) {
			throw new JSONFormatException();
		}
		if(input.charAt(idxResult) == ']') {
			updated = false;
		}
		else {
			updated = true;
		}
	}
	
	private void getUpdateId(String input) throws JSONFormatException {
		int idxUpdateId = input.indexOf("update_id") + 11;
		if(idxUpdateId == -1) {
			throw new JSONFormatException();
		}
		StringBuilder updateIdBldr = new StringBuilder();
		while(input.charAt(idxUpdateId) != ',') {
			updateIdBldr.append(input.charAt(idxUpdateId));
			idxUpdateId++;
		}
		update_id = Integer.parseInt(updateIdBldr.toString());
	}
	
	private void getName(String input) throws JSONFormatException {
		int idxName = input.indexOf("first_name") + 13;
		if(idxName == -1) {
			throw new JSONFormatException();
		}
		StringBuilder nameBldr = new StringBuilder();
		while(input.charAt(idxName) != '"') {
			nameBldr.append(input.charAt(idxName));
			idxName++;
		}
		first_name = nameBldr.toString();
	}
	
	private void getDateReceived(String input) throws JSONFormatException {
		int idxDate = input.lastIndexOf("date") + 6;
		if(idxDate == -1) {
			throw new JSONFormatException();
		}
		StringBuilder dateBldr = new StringBuilder();
		while(input.charAt(idxDate) != ',') {
			dateBldr.append(input.charAt(idxDate));
			idxDate++;
		}
		date_received = Date.from(Instant.ofEpochSecond(Integer.valueOf(dateBldr.toString())));
	}
	
	private void getMessage(String input) throws JSONFormatException {
		int idxMsg = input.lastIndexOf("text") + 7;
		if(idxMsg == -1) {
			throw new JSONFormatException();
		}
		StringBuilder msgBldr = new StringBuilder();
		while(input.charAt(idxMsg) != '"') {
		// TODO breaks if user sends " (this appears as \" so maybe check for this as opposed to "
			msgBldr.append(input.charAt(idxMsg));
			idxMsg++;
		}
		message = msgBldr.toString();
	}
	
	public boolean updated() {
		return updated;
	}
	
	public boolean ok() {
		return ok;
	}
	
	public int getUpdateId() {
		return update_id;
	}
	
	public String getFirstName() {
		return first_name;
	}
	
	public Date getDate() {
		return date_received;
	}
	
	public String getMessage() {
		return message;
	}
	
//	{
//		"ok":true,
//		"result":[{
//		        	  "update_id":145981277,
//		        	  "message":{
//		        	  		"message_id":14,
//		        	  		"from":{
//		        	  			"id":105604450,
//		        	  			"first_name":"Smudja \ud83c\udd92\ud83c\udd93\ud83c\udd95",
//		        	  			"username":"Smudja"
//		        	  			},
//		        	  		"chat":{
//		        	  				"id":105604450,
//		        	  				"first_name":"Smudja \ud83c\udd92\ud83c\udd93\ud83c\udd95",
//		        	  				"username":"Smudja",
//		        	  				"type":"private"
//		        	  			},
//		        	  		"date":1490556866,
//		        	  		"text":"Hey hey"
//		        		}
//		          }]
//	}

}
