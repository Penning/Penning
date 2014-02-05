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
	
	Event(EventProtocol.Event e){
		userID = e.getUserID();
		sessionID = e.getSessionID();
		switch(e.getType()){
		case INSERT:
			event = EventType.insert;
			break;
		case DELETE:
			event = EventType.delete;
		case UNDO:
			break;
		case REDO:
			break;
		case CURSORLOCATIONCHANGED:
			event = EventType.cursorLocationChanged;
			break;
		default:
			break;
		}
		cursorLocation = e.getCursorLocation();
		if(e.getText().length() >= 1)
			text = e.getText().toCharArray()[0];
		else
			text = ' ';
	}
}
