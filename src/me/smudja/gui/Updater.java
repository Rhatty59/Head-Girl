/**
 * 
 */
package me.smudja.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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

	public synchronized Node update() {
		
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

			return getDateNode();
		} else {
			messageToDisplay += 1;
			if (messageToDisplay >= numberMessages) {
				messageToDisplay = 0;
			}
		}

		return updates.get(messageToDisplay).getNode();
	}

	private Node getDateNode() {

		SimpleDateFormat format = new SimpleDateFormat("EEEE dd MMMM yyyy");

		FlowPane dateNode = new FlowPane();
		dateNode.setAlignment(Pos.CENTER);
		dateNode.setId("center");

		Text text = new Text(format.format(System.currentTimeMillis()));

		text.setTextAlignment(TextAlignment.CENTER);
		
		dateNode.getChildren().addAll(text);
		
		text.setId("center-text");
		text.applyCss();
		text.setFont(Font.font(HeadGirl.getFontSize()));
		
		return dateNode;
	}
}
