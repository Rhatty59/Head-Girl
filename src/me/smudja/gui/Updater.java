/**
 * 
 */
package me.smudja.gui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import me.smudja.updater.Update;
import me.smudja.updater.UpdateManager;

/**
 * @author smithl
 *
 */
public class Updater {
	
	private ArrayList<Update> updates;
	
	public Updater() {
		updates = new ArrayList<Update>();
	}

	public synchronized String update() {
		
		updates.addAll(Arrays.asList(UpdateManager.INSTANCE.getUpdates()));
			
		long currentTime = System.currentTimeMillis();
		
		Iterator<Update> iterator = updates.iterator();
		while (iterator.hasNext()) {
			Update item = iterator.next();
			if((currentTime - item.getRawDate()) > HeadGirl.getMessageLife()) {
				iterator.remove();
			}
		}
		
		if (updates.size() >= HeadGirl.getMaxUpdates()) {
		    
			ArrayList<Update> miniArray = new ArrayList<Update>();
		    
			for(int i = HeadGirl.getMaxUpdates(); i > 0; i--) {
				miniArray.add(updates.get(updates.size()-i));
			}

		    updates = miniArray;
		}


		
		String text;
		StringBuilder textBuilder = new StringBuilder();
		for(Update update : updates) {
			textBuilder.append("Update ID: " + update.getUpdateId() + "\n");
			textBuilder.append("[" + update.getFirstName() + "] " + update.getText() + "\n");
			textBuilder.append("Received: " + DateFormat.getDateTimeInstance().format(update.getDate()) + "\n");
			textBuilder.append("\n");
		}
		if(textBuilder.length() == 0) {
			text = "No Updates To Display...";
		}
		else {
			text = textBuilder.toString();
		}
		
		return text;
	}

}
