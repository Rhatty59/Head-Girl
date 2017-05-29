/**
 * 
 */
package utility;

import java.text.DateFormat;

/**
 * @author smithl
 *
 */
public class Reporter {
	
	private Reporter(){}
	
	public static void report(String message, LogLevel level) {
		System.out.println(DateFormat.getDateTimeInstance().format(System.currentTimeMillis()) + " [" + level.name() + "] " + message);
	}

}
