/**
 * 
 */
package me.smudja.updater;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import me.smudja.gui.HeadGirl;

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

	@Override
	public Node getNode() {
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.setId("center");
		box.setMaxHeight(screenBounds.getHeight() - 50);
		
		Text msg = new Text("[" + getFirstName().toUpperCase() 
				+ "] " + getText() + "\n");
		
		
		msg.setWrappingWidth(screenBounds.getWidth());
		msg.setTextAlignment(TextAlignment.CENTER);
		
		box.getChildren().add(msg);
		
		msg.setId("center-text");
		msg.applyCss();
		msg.setFont(Font.font(HeadGirl.getFontSize()));
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM 'at' HH:mm");
		
		Text date = new Text("SENT: " + format.format(getTimeReceived()).toUpperCase());
		
		box.getChildren().add(date);
		
		date.setId("center-text");
		date.applyCss();
		date.setFont(Font.font(HeadGirl.getFontSize()));
		
		Group root = new Group();

		root.getChildren().add(box);

		root.applyCss();
		root.layout();
    
        Bounds boxBounds = box.getLayoutBounds();
        
        double font_size = HeadGirl.getFontSize();
        
        // rescale text size so that it fits on screen
        while(boxBounds.getHeight() > (screenBounds.getHeight()- 50)) {
        	font_size = font_size - 5;
        	msg.setFont(Font.font(font_size));
        	date.setFont(Font.font(font_size));
        	root.layout();
        	boxBounds = box.getLayoutBounds();
        }
		
		return box;
	}

}
