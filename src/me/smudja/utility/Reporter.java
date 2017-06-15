/**
 * 
 */
package me.smudja.utility;

import java.text.DateFormat;

/**
 * Displays log information in an easy to read format
 * @author smithl
 *
 */
public class Reporter {
	
	private Reporter(){}
	
	public static void report(String message, LogLevel level) {
		System.out.println(DateFormat.getDateTimeInstance().format(System.currentTimeMillis()) + " [" + level.name() + "] " + message);
	}

}
