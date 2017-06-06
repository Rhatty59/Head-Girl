/**
 * 
 */
package me.smudja.updater;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javafx.scene.image.Image;
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

}
