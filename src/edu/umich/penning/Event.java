package edu.umich.penning;

public class Event {

	public String userID;
	public String sessionID;
	public long globalOrder;
	
	public EventType event;
	public int cursorLocation;
	public char text;
	public boolean confirmed = false;
	public int eventId;

	
	Event(EventType e) {
		event = e;
		userID = MainActivity.userId;
	}
	
	Event(EventProtocol.Event e, long orderId){
		//userID = e.getUserID();
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
		eventId = e.getEventID();
		if(e.getText().length() >= 1)
			text = e.getText().toCharArray()[0];
		else
			text = ' ';

		confirmed = true;
		globalOrder = orderId;

	}
}
