package me.rhett.reminder;

public enum Reminder {
	
	INSTANCE;
	
	public static Reminder getInstance() {
		return INSTANCE;
	}

	private int currentMessage;
	
	Reminder() {
		currentMessage = 1;
	}
	
	public String getInfoText() {
		
		/*
		 * PUT YOUR CODE HERE
		 */
		
		
		return "MESSAGE TO BE SENT TO SCREEN";
	}

}
