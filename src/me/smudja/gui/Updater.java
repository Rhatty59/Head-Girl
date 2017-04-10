/**
 * 
 */
package me.smudja.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import me.smudja.updater.Update;
import me.smudja.updater.UpdateManager;

/**
 * Deals with removing expired updates and adding new updates.
 * Also, styles text.
 * @author smithl
 *
 */
public class Updater {
	
	/**
	 * updates - stores all current updates
	 */
	private ArrayList<Update> updates;
	
	public Updater() {
		updates = new ArrayList<Update>();
	}

	public synchronized String update() {
		
		long currentTime = System.currentTimeMillis();
		
		// remove expired updates
		Iterator<Update> iterator = updates.iterator();
		while (iterator.hasNext()) {
			Update item = iterator.next();
			if((currentTime - item.getRawDate()) > HeadGirl.getMessageLife()) {
				iterator.remove();
			}
		}
		
		// add any new updates
		updates.addAll(Arrays.asList(UpdateManager.INSTANCE.getUpdates()));
			
		
		// if we have too many to display just take the most recent ones
		if (updates.size() >= HeadGirl.getMaxUpdates()) {
		    
			ArrayList<Update> miniArray = new ArrayList<Update>();
		    
			for(int i = HeadGirl.getMaxUpdates(); i > 0; i--) {
				miniArray.add(updates.get(updates.size()-i));
			}

		    updates = miniArray;
		}


		// build text to be displayed
		String text;
		StringBuilder textBuilder = new StringBuilder();
		SimpleDateFormat format = new SimpleDateFormat("dd MMM YYYY HH:mm");
		for(Update update : updates) {
			textBuilder.append("[" + update.getFirstName().toUpperCase() + "] " + update.getText() + "\n");
			if(HeadGirl.getMaxUpdates() == 1) {
				textBuilder.append("\n");
			}
			textBuilder.append("SENT: " +format.format(update.getDate()).toUpperCase() + "\n");
			textBuilder.append("\n");
		}
		if(textBuilder.length() == 0) {
			text = "No New Messages";
		}
		else {
			text = textBuilder.toString().trim();
		}
		
		return text;
	}

}
