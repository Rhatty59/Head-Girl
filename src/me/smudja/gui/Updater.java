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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import me.rhett.reminder.Reminder;
import me.smudja.updater.Update;
import me.smudja.updater.UpdateManager;

/**
 * Deals with removing expired updates and adding new updates.
 * @author smithl
 *
 */
public enum Updater {
	
	INSTANCE;
	
	/**
	 * updates - stores all current updates
	 */
	private ArrayList<Update> updates;
	
	/**
	 * index of update we need to display
	 */
	private int updateToDisplay = -1;
	
	/**
	 * instance of UpdateManager
	 * 
	 * @see UpdateManager
	 */
	private UpdateManager updateManager = UpdateManager.getInstance();
	
	/**
	 * instance of Reminder
	 * 
	 * @see Reminder;
	 */
	private Reminder reminder = Reminder.getInstance();
	
	Updater() {
		updates = new ArrayList<Update>();
	}
	
	public static Updater getInstance() {
		return INSTANCE;
	}

	/**
	 * Updates array with new updates, removes old updates and returns next update node to be displayed
	 * 
	 * @return update node to be displayed
	 */
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
			 * The block of code below checks if there are any messages and if
			 * there are none, sets the messageToDisplay to -1 as it will be incremented
			 * by 1 when there is one in the else clause.
			 * 
			 * the second if statement cycles the messageToDisplay back to array record 0
			 * if it has reached the end of the array.
			 */

		if (updates.size() == 0) {
			updateToDisplay = -1;

			return getDateNode();
		} else {
			updateToDisplay += 1;
			if (updateToDisplay >= updates.size()) {
				updateToDisplay = 0;
			}
		}

		return updates.get(updateToDisplay).getNode();
	}

	/**
	 * Creates a node just displaying the current system date
	 * @return node containing current system date
	 */
	private Node getDateNode() {

		SimpleDateFormat format = new SimpleDateFormat("EEEE dd MMMM yyyy");

		VBox dateNode = new VBox();
		dateNode.setAlignment(Pos.CENTER);
		dateNode.setId("center");

		Text dateLbl = new Text(format.format(System.currentTimeMillis()));
		Text infoLbl = new Text(reminder.getInfoText());
		

		dateLbl.setTextAlignment(TextAlignment.CENTER);
		infoLbl.setTextAlignment(TextAlignment.CENTER);
		
		dateNode.getChildren().addAll(dateLbl, infoLbl);
		
		dateLbl.setId("center-text");
		dateLbl.applyCss();
		dateLbl.setFont(Font.font(HeadGirl.getFontSize()));	
		
		infoLbl.setId("center-text");
		infoLbl.applyCss();
		infoLbl.setFont(Font.font(HeadGirl.getFontSize()));	
		
		return dateNode;
	}
}
