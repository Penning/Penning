package edu.umich.penning;

import java.util.Vector;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * 
 * @author adoxner, pramodsum, Tim-Wood
 * Listener class for the main TextEdit which detects user input and acts accordingly
 *
 */
public class CollabEditTextListener implements TextWatcher {
	public Vector<Event> undoStack = new Vector<Event>();
	public Vector<Event> redoStack = new Vector<Event>();
	protected String fullText;
	
	private Vector<Event> localEvents = new Vector<Event>();
	private Vector<Event> serverEvents = new Vector<Event>();
	
	public Event lastConfirmed = null;
	
	public boolean foreignEventHandle = false;
	
	private MainActivity myMainActivity;
	
	public CollabEditTextListener(MainActivity _myMainActivity){
		myMainActivity = _myMainActivity;
	}
	
	public void onTextChanged (CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if(foreignEventHandle)
		{
			foreignEventHandle = false;
			return;
			
		}
		if(text.length() > 0 && !MainActivity.undo_redo_action) {
			Event e = null;
			if(lengthBefore < lengthAfter && MainActivity.et.getSelectionEnd() > 0) {
				char c = text.toString().charAt(MainActivity.et.getSelectionEnd() - 1);
				insertChar(c);
				fullText = text.toString();
			}
			else if(lengthAfter < lengthBefore) {
				char c = fullText.charAt(MainActivity.et.getSelectionEnd());
				removeChar(c);
				fullText = text.toString();
			}
			fullText = text.toString();
		}
	}
	
	private void unwind(Event e) {
		if(e.event == EventType.insert)
			insert(e.text, e.cursorLocation);
		else if(e.event == EventType.delete)
			remove(e.cursorLocation);
	}
	
	private void unwind() {
		Vector<Event> local = new Vector<Event>();
		Vector<Event> remote = new Vector<Event>();
		
		if (localEvents == null || localEvents.isEmpty())
			return;
		System.out.println("SIZE: " + localEvents.size());
		System.out.println("lastConfirmed: " + lastConfirmed.text);
		System.out.println("Last localEvents: " + localEvents.lastElement().text);
		while(localEvents.lastElement().text != lastConfirmed.text) {
			Event e = localEvents.lastElement();
			local.add(e);
			localEvents.remove(e);
			unwind(e);
		}
//		if(localEvents.lastElement().text == lastConfirmed.text) {
//			Event e = localEvents.lastElement();
//			local.add(e);
//			localEvents.remove(e);
//			unwind(e);
//		}
		while(!serverEvents.isEmpty() && serverEvents.firstElement().globalOrder < lastConfirmed.globalOrder) {
			Event e = serverEvents.firstElement();
			remote.add(e);
			serverEvents.remove(0);
			unwind(e);
		}
		System.out.println("GOING TO REAPPLY");
//		return;
		reapply(local, remote);
	}
	
	private void reapply(Vector<Event> local, Vector<Event> remote) {
		Event e = null;
		while(!remote.isEmpty()) {
			e = remote.firstElement();
			if(e.event == EventType.insert)
				insert(e.text, e.cursorLocation);
			else if(e.event == EventType.delete)
				remove(e.cursorLocation);
			remote.remove(0);
		}
		while(!local.isEmpty()) {
			e = local.lastElement();
			System.out.print("REAPPLYING: " + e.text);
			if(e.event == EventType.insert)
				remove(e.cursorLocation);
			else if(e.event == EventType.delete)
				insert(e.text, e.cursorLocation);
			localEvents.add(e);
			local.remove(e);
		}
	}
	
	public void onRemoteTextChange(Event e) {
		if(e.userID == MainActivity.userId) {
			System.out.println("same userID: " + e.userID + " " + MainActivity.userId);
			lastConfirmed = e;
			unwind();
			return;
		}

		foreignEventHandle = true;
		if(e.event == EventType.insert)
			insert(e.text, e.cursorLocation - 1);
		else if(e.event == EventType.delete)
			remove(e.cursorLocation);
		serverEvents.add(e);
	}

	private void insertChar(char c) {
		Event e = new Event(EventType.insert);
		e.text = c;
		e.cursorLocation = MainActivity.et.getSelectionEnd();
		myMainActivity.BroadcastEvent(e);
		localEvents.add(e);
		System.out.println("Char inserted: " + c + " @ " + e.cursorLocation);
		e.event = EventType.delete;
		e.cursorLocation -= 1;
		undoStack.add(e);
	}
	
	private void removeChar(char c) {
		Event e = new Event(EventType.delete);
		e.text = c;
		e.cursorLocation = MainActivity.et.getSelectionEnd();
		myMainActivity.BroadcastEvent(e);
		localEvents.add(e);
		System.out.println("Char removed: " + c + " @ " + (e.cursorLocation + 1));
		e.event = EventType.insert;
		undoStack.add(e);
	}
	
	private void insert(final char c, final int cursorLocation) {
		myMainActivity.runOnUiThread(new Runnable(){
		    public void run(){
		    	System.out.println("Insert " + c + "@" + cursorLocation);
		    	MainActivity.et.setText(MainActivity.et.getText().insert(cursorLocation, Character.toString(c)));	
				MainActivity.et.setSelection(cursorLocation + 1);
		    }
		});
	}
	
	private void remove(final int cursorLocation) {
		myMainActivity.runOnUiThread(new Runnable(){
		    public void run(){
		    	System.out.println("Remove @" + cursorLocation);
				MainActivity.et.setText(MainActivity.et.getText().delete(cursorLocation, cursorLocation + 1));
				MainActivity.et.setSelection(cursorLocation);
		    }
		});
	}
	
	private Event performUndoRedo(Event e) {
		if(e.event == EventType.insert)
			insert(e.text, e.cursorLocation);
		else if(e.event == EventType.delete)
			remove(e.cursorLocation);
		
		myMainActivity.BroadcastEvent(e);

		if(e.event == EventType.insert)
			e.event = EventType.delete;
		else if(e.event == EventType.delete)
			e.event = EventType.insert;
		
		MainActivity.undo_redo_action = false;
		MainActivity.prev_undo = true;

		localEvents.add(e);
		return e;
	}
	
	public void undo() {
		if(undoStack.isEmpty()) return;
		
		//get last event from undoStack
		Event e = undoStack.lastElement();
		undoStack.removeElement(e);
		
		e = performUndoRedo(e);
		redoStack.add(e);	
	}
	
	public void redo() {
		if(redoStack.isEmpty()) return;
		
		//get last event from redoStack
		Event e = redoStack.lastElement();
		redoStack.removeElement(e);
		
		e = performUndoRedo(e);
		undoStack.add(e);
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int before, int after) {
		if(before > after) fullText = s.toString();
		
		if(s.length() > 0 && !MainActivity.undo_redo_action) {
			if(MainActivity.prev_undo || MainActivity.prev_redo) {
				redoStack.removeAllElements();
				MainActivity.prev_undo = false;
				MainActivity.prev_redo = false;
				
				if(before < after) {
					for(int i = 0; i < undoStack.size(); ++i) {
						if(undoStack.elementAt(i).cursorLocation > MainActivity.et.getSelectionEnd())
							undoStack.elementAt(i).cursorLocation += 1;
						if(redoStack.elementAt(i).cursorLocation > MainActivity.et.getSelectionEnd())
							redoStack.elementAt(i).cursorLocation += 1;
					}
				}
				else if(before < after) {
					for(int i = 0; i < undoStack.size(); ++i) {
						if(undoStack.elementAt(i).cursorLocation > MainActivity.et.getSelectionEnd() - 1)
							undoStack.elementAt(i).cursorLocation -= 1;
						if(redoStack.elementAt(i).cursorLocation > MainActivity.et.getSelectionEnd() - 1)
							redoStack.elementAt(i).cursorLocation -= 1;
					}
				}
			}
		}
	}

}