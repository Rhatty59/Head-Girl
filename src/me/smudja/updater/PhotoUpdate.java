/**
 * 
 */
package me.smudja.updater;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import me.smudja.gui.HeadGirl;
import me.smudja.utility.LogLevel;
import me.smudja.utility.Reporter;

/**
 * @author smithl
 *
 */
public class PhotoUpdate extends Update {
	
	Image photo;

	/**
	 * 
	 */
	public PhotoUpdate(JSONObject jsonUpdate) {
		super(jsonUpdate);
		// we know this is a photo
		
		JSONArray photoSizes = (JSONArray) ((JSONObject) jsonUpdate.get("message")).get("photo");
		
		int sizeChoice = photoSizes.size() - 1;
		JSONObject photoChoice;
		
		do {
			if(sizeChoice < 0) {
				Reporter.report("Photo has no actionable file size, will not be displayed", LogLevel.MINOR);
			}
			photoChoice = (JSONObject) photoSizes.get(sizeChoice);
			sizeChoice--;
		} while(((long) photoChoice.get("file_size")) >= 20000000);
		
		String file_id = (String) photoChoice.get("file_id");

		photo = APIManager.INSTANCE.getImageFile(file_id);
	}
	
	public Image getPhoto() {
		return photo;
	}

	@Override
	public Node getNode() {
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.setId("center");
		box.setMaxHeight(screenBounds.getHeight() - 50);
		
		ImageView image = new ImageView();
		image.setImage(photo);
		if(!(image.getImage() == null)) {
			image.setPreserveRatio(true);
			if (image.getImage().getWidth() > screenBounds.getWidth()) {
				image.setFitWidth(screenBounds.getWidth());
			}
			if (image.getImage().getHeight() > (screenBounds.getHeight() - 150)) {
				image.setFitHeight(screenBounds.getHeight() - 150);
			}
		}
		
		Text info = new Text("From " + getFirstName());
		
		info.setWrappingWidth(screenBounds.getWidth());
		info.setTextAlignment(TextAlignment.CENTER);
		
		box.getChildren().addAll(image, info);
		
		info.setId("center-text");
		info.applyCss();
		info.setFont(Font.font(HeadGirl.getFontSize() - 10));
		
		box.layout();
		
		return box;
	}

}
