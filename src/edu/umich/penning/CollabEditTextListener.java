package edu.umich.penning;

import java.util.Stack;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author adoxner, pramodsum, Tim-Wood
 * Listener class for the main TextEdit which detects user input and acts accordingly
 *
 */
public class CollabEditTextListener implements TextWatcher {
	public Stack<Event> undoStack = new Stack<Event>();
	public Stack<Event> redoStack = new Stack<Event>();
	protected String fullText;
	private EditText owningContext;
	
	public CollabEditTextListener(EditText e){
		owningContext = e;
	}
	
	public void onTextChanged (CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if(text.length() > 0 && !MainActivity.undo_redo_action) {
			if(MainActivity.prev_undoRedo_action) {
				redoStack.clear();
			}
			
			if(lengthBefore < lengthAfter) {
				char c = text.toString().charAt(MainActivity.et.getSelectionEnd() - 1);
//				Toast.makeText(MainActivity.context, "Char inserted: " + c + " @ " + owningContext.getSelectionEnd(), Toast.LENGTH_LONG).show();
				insertChar(c);
				fullText = text.toString();
			}
			else if(lengthAfter < lengthBefore) {
				char c = fullText.charAt(MainActivity.et.getSelectionEnd());
//				Toast.makeText(MainActivity.context, "Char removed: " + c + " @ " + owningContext.getSelectionEnd(), Toast.LENGTH_LONG).show();
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
		undoStack.push(e);
	}
	
	public void removeChar(char c) {
		Event e = new Event(EventType.delete);
		e.text = c;
		e.cursorLocation = MainActivity.et.getSelectionEnd();
		System.out.println("Char removed: " + c + " @ " + e.cursorLocation + 1);
		undoStack.push(e);
	}
	
	public void undo() {
		if(undoStack.empty()) {
			//System.out.println("UNDOSTACK IS EMPTY! AHHHHHHHHHH");
			return;
		}
		
		//get last event from undoStack
		Event e = undoStack.pop();
		
		if(e.event == EventType.insert) {
			//re-insert last char
			MainActivity.et.setText(MainActivity.et.getText().delete(e.cursorLocation - 1, e.cursorLocation));
			MainActivity.et.setSelection(e.cursorLocation - 1);
		}
		else if(e.event == EventType.delete) {
			//remove last inserted char
			MainActivity.et.setText(MainActivity.et.getText().insert(e.cursorLocation, Character.toString(e.text)));
			MainActivity.et.setSelection(e.cursorLocation + 1);
			System.out.println("Char inserted: " + e.text + " @ " + e.cursorLocation);
		}
		
		redoStack.push(e);
		MainActivity.undo_redo_action = false;
		MainActivity.prev_undoRedo_action = true;
	}
	
	public void redo() {
		if(redoStack.empty()) {
			//System.out.println("REDOSTACK IS EMPTY! AHHHHHHHHHH");
			return;
		}
		
		//get last event from redoStack
		Event e = redoStack.pop();
		
		if(e.event == EventType.insert) {
			//re-insert last char
			MainActivity.et.setText(MainActivity.et.getText().insert(e.cursorLocation - 1, Character.toString(e.text)));
			MainActivity.et.setSelection(e.cursorLocation);
			System.out.println("Char inserted: " + e.text + " @ " + e.cursorLocation);
		}
		else if(e.event == EventType.delete) {
			//remove last char
			MainActivity.et.setText(MainActivity.et.getText().delete(e.cursorLocation, e.cursorLocation + 1));
			MainActivity.et.setSelection(e.cursorLocation);
			System.out.println("Char removed: " + e.text + " @ " + e.cursorLocation);
		}
	
		undoStack.push(e);
		MainActivity.undo_redo_action = false;
		MainActivity.prev_undoRedo_action = true;
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int before, int after) {
		// TODO Auto-generated method stub
		if(before > after && !MainActivity.undo_redo_action) { 
			System.out.println("GOING TO DELETE CHARACTER @ " + Integer.toString(MainActivity.et.getSelectionEnd()) + " FROM \"" + s + "\"");
			fullText = s.toString();
		}
	}
}
