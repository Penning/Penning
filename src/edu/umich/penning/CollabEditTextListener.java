/**
 * 
 */
package edu.umich.penning;

import java.util.Stack;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

/**
 * @author adoxner, pramodsum, Tim-Wood
 * Listener class for the main TextEdit which detects user input and acts accordingly
 *
 */
public class CollabEditTextListener implements TextWatcher {
	public Stack<Event> undoStack = new Stack<Event>();
	public Stack<Event> redoStack = new Stack<Event>();
	protected String fullText;
	
	/*
	 * Tracks cursor changes
	 */
//	@Override 
//	protected void onSelectionChanged(int selStart, int selEnd) {
//		// TODO send change to server
//		
//		if (selStart == selEnd){
//			// cursor moved, nothing selected
//			
//			System.out.println("Curser moved to " + selStart);
//			
//		}else{
//			// we don't handle this case
//			return;
//		}
//	}
	
	public void onTextChanged (CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if(text.length() > 0) {
			if(lengthBefore < lengthAfter) {
//				System.out.println("before: " + lengthBefore + "\nafter: " + lengthAfter);
				char c = text.toString().charAt(text.length() - 1);
				insertChar(c);
				fullText = text.toString();
			}
			else if(lengthAfter < lengthBefore) {
//				System.out.println("before: " + lengthBefore + "\nafter: " + lengthAfter);
				char c = fullText.charAt(fullText.length() - 1);
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
	}
	
	public void removeChar(char c) {
		Event e = new Event(EventType.delete);
		e.text = c;
		e.cursorLocation = MainActivity.et.getSelectionEnd();
		System.out.println("Char removed: " + c + " @ " + e.cursorLocation);
		undoStack.add(e);
	}
	
	public void undo() {
		//Toast.makeText(, "Undo Pressed" , Toast.LENGTH_SHORT);
		if(undoStack.empty()) return;
		
		//get last event from undoStack
		Event e = undoStack.pop();
		
		if(e.event == EventType.insert) {
			//remove last char
		}
		else if(e.event == EventType.delete) {
			//re-insert last char
		}
		
		redoStack.add(new Event(EventType.undo));
	}
	
	public void redo() {
		//Do redo stuff
		//Toast.makeText(app.getBaseContext(), "Redo Pressed" , Toast.LENGTH_SHORT);
		undoStack.add(new Event(EventType.redo));
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int before, int after) {
		// TODO Auto-generated method stub
		if(before > after) { 
			System.out.println("GOING TO DELETE CHARACTER FROM \"" + s + "\"");
			fullText = s.toString();
		}
	}
}
