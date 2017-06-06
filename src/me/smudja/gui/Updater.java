/**
 * 
 */
package me.smudja.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import me.smudja.updater.PhotoUpdate;
import me.smudja.updater.TextUpdate;
import me.smudja.updater.Update;
import me.smudja.updater.UpdateManager;

/**
 * Deals with removing expired updates and adding new updates.
 * Also, styles text.
 * @author smithl
 *
 */
public enum Updater {
	
	INSTANCE;
	
	/**
	 * updates - stores all current updates
	 */
	private ArrayList<Update> updates;
	
	private int numberMessages = 0;
	public int messageToDisplay = -1;
	
	private UpdateManager updateManager = UpdateManager.getInstance();
	
	Updater() {
		updates = new ArrayList<Update>();
	}
	
	public static Updater getInstance() {
		return INSTANCE;
	}

	public synchronized Object[] update() {
		
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
		updates.addAll(Arrays.asList(updateManager.getUpdates()));
		
		/*
		 * So in here, we take the array of messages [updates], with expired (30 minutes) deleted
		 * and if we have too many (getMaxUpdates) also deleted (fix this in future versions)
		 * and .get(messageToDisplay) and display each one every 30 seconds.
		 */
		
		numberMessages = updates.size();  // are there any messages in the array?
		
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
				return new Object[]{"No New Messages", null};
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
			
			if (updates.get(messageToDisplay) instanceof TextUpdate) {
				TextUpdate txtUpdate = (TextUpdate) updates.get(messageToDisplay);
				textBuilder.append("[" + txtUpdate.getFirstName().toUpperCase() 
					+ "] " + txtUpdate.getText() + "\n");
				textBuilder.append("SENT: " + format.format(txtUpdate.getTimeReceived()).toUpperCase()
						+ "\n");
				return new Object[]{ textBuilder.toString().trim(), null};
			}
			else {
				PhotoUpdate photoUpdate = (PhotoUpdate) updates.get(messageToDisplay);
				textBuilder.append("SENT: " + format.format(photoUpdate.getTimeReceived()).toUpperCase()
						+ "\n");
				return new Object[]{ textBuilder.toString().trim(), photoUpdate.getPhoto()};
			}
	}
}
