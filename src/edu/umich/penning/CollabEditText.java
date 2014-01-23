/**
 * 
 */
package edu.umich.penning;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author adoxner, pramodsum, Tim-Wood
 *
 */
public class CollabEditText extends EditText {
	public Stack<Event> undoStack = new Stack<Event>();
	public Stack<Event> redoStack = new Stack<Event>();
	
	/*
	 * Tracks cursor changes
	 */
	@Override 
	protected void onSelectionChanged(int selStart, int selEnd) {
		// TODO send change to server
		
		if (selStart == selEnd){
			// cursor moved, nothing selected
			
			System.out.println("Curser moved to " + selStart);
			
		}else{
			// we don't handle this case
			return;
		}
	}
	
	

	/**
	 * @param context
	 */
	public CollabEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CollabEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CollabEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	protected void onTextChanged (CharSequence text, int start, int lengthBefore, int lengthAfter) {
		System.out.println("Key Pressed: " + text);
		
		if(text != "") {
			char c = text.toString().charAt(start - 1);
			System.out.println("C: " + c);
			insertChar(c, start, lengthBefore, lengthAfter);
		}
	}

	public void insertChar(char c, int start, int lengthBefore, int lengthAfter) {
		Event e = new Event(EventType.insert);
		e.text = c;
		e.cursorLocation = start;
		System.out.println("Char inserted: " + c);
		undoStack.add(e);
	}
	
	public void removeChar(KeyEvent keyEvent) {
		Event e = new Event(EventType.delete);
		e.cursorLocation = this.getSelectionStart();
		CharSequence enteredText = this.getText().toString();
		e.text = enteredText.subSequence(e.cursorLocation, 1).charAt(0);
		System.out.println("Char removed: " + e.text);
		undoStack.add(e);
	}
	
	public void undo() {
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
		undoStack.add(new Event(EventType.redo));
	}
}
