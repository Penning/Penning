package edu.umich.penning;

public class Event {

	public String userID;
	public String sessionID;
	
	public EventType event;
	public int cursorLocation;
	public char text;
	
	Event(EventType e) {
		event = e;
	}
}
