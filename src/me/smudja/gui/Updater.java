/**
 * 
 */
package me.smudja.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import me.smudja.updater.TextUpdate;
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
	private ArrayList<TextUpdate> updates;
	
	private int numberMessages = 0;
	public int messageToDisplay = -1;
	
	public Updater() {
		updates = new ArrayList<TextUpdate>();
	}

	public synchronized String update() {
		
		long currentTime = System.currentTimeMillis();
		
		// remove expired updates
		Iterator<TextUpdate> iterator = updates.iterator();
		while (iterator.hasNext()) {
			TextUpdate item = iterator.next();
			if((currentTime - item.getRawDate()) > HeadGirl.getMessageLife()) {
				iterator.remove();
			}
		}
		
		// add any new updates
		updates.addAll(Arrays.asList(UpdateManager.INSTANCE.getUpdates()));
		
		
		// if we have too many to display just take the most recent ones
		if (updates.size() >= HeadGirl.getMaxUpdates()) {
		    
			ArrayList<TextUpdate> miniArray = new ArrayList<TextUpdate>();
		    
			for(int i = HeadGirl.getMaxUpdates(); i > 0; i--) {
				miniArray.add(updates.get(updates.size()-i));
			}

		    updates = miniArray;
		}
		/*
		 * So in here, we take the array of messages [updates], with expired (30 minutes) deleted
		 * and if we have too many (getMaxUpdates) also deleted (fix this in future versions)
		 * and .get(messageToDisplay) and display each one every 30 seconds.
		 */
		
		numberMessages = updates.size();  // are there any messages in the array?
		
			String text;
			StringBuilder textBuilder = new StringBuilder();
			SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM 'at' HH:mm");
			
			/* 
			 * The block of code below checks if there are any messages and if
			 * there are none, sets the messageToDisplay to -1 as it will be incremented
			 * by 1 when there is one in the else clause.
			 * 
			 * the second if statement cycles the messageToDisplay back to array record 0
			 * if it has reached the end of the array.
			 */
			
			if (numberMessages == 0) {
				messageToDisplay = -1;
			} else {
				messageToDisplay += 1;
				if (messageToDisplay >= numberMessages) {
					messageToDisplay = 0;
				}
			}
			
			/*
			 * messageToDisplay now contains, well, the next messageToDisplay!
			 * Let's now build the text message using StringBuilder
			 */
			
				if (numberMessages > 0) {
					textBuilder.append("[" + updates.get(messageToDisplay).getFirstName().toUpperCase() 
						+ "] " + updates.get(messageToDisplay).getText() + "\n");
					textBuilder.append("SENT: " + format.format(updates.get(messageToDisplay).getDate())
						.toUpperCase() + "\n");
				}
		
			if(textBuilder.length() == 0) {
				text = "No New Messages";
			} else {
				text = textBuilder.toString().trim();
			}
			return text;
	}
}
