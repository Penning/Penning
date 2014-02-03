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

	public void insertChar(char c) {
		Event e = new Event(EventType.insert);
		e.text = c;
		e.cursorLocation = MainActivity.et.getSelectionEnd();
		System.out.println("Char inserted: " + c + " @ " + e.cursorLocation);
		undoStack.add(e);
		myMainActivity.BroadcastEvent(e);
	}
	
	public void removeChar(char c) {
		Event e = new Event(EventType.delete);
		e.text = c;
		e.cursorLocation = MainActivity.et.getSelectionEnd();
		System.out.println("Char removed: " + c + " @ " + e.cursorLocation + 1);
		undoStack.add(e);
		myMainActivity.BroadcastEvent(e);
	}
	
	private void insert(char c, int cursorLocation) {
		MainActivity.et.setText(MainActivity.et.getText().insert(cursorLocation, Character.toString(c)));
		MainActivity.et.setSelection(cursorLocation + 1);
	}
	
	private void remove(int cursorLocation) {
		MainActivity.et.setText(MainActivity.et.getText().delete(cursorLocation, cursorLocation + 1));
		MainActivity.et.setSelection(cursorLocation);
	}
	
	public void undo() {
		if(undoStack.isEmpty()) return;
		
		//get last event from undoStack
		Event e = undoStack.lastElement();
		undoStack.removeElement(e);
		
		if(e.event == EventType.insert)
			remove(e.cursorLocation - 1);
		else if(e.event == EventType.delete)
			insert(e.text, e.cursorLocation);
		
		redoStack.add(e);
		MainActivity.undo_redo_action = false;
		MainActivity.prev_undo = true;
	}
	
	public void redo() {
		if(redoStack.isEmpty()) return;
		
		//get last event from redoStack
		Event e = redoStack.lastElement();
		redoStack.removeElement(e);
		
		if(e.event == EventType.insert)
			insert(e.text, e.cursorLocation - 1);
		else if(e.event == EventType.delete)
			remove(e.cursorLocation);
	
		undoStack.add(e);
		MainActivity.undo_redo_action = false;
		MainActivity.prev_redo = true;
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int before, int after) {
		if(before > after && !MainActivity.undo_redo_action) { 
			System.out.println("GOING TO DELETE CHARACTER @ " + Integer.toString(MainActivity.et.getSelectionEnd()) + " FROM \"" + s + "\"");
			fullText = s.toString();
		}
		
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
