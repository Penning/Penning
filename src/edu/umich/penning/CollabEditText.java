/**
 * 
 */
package edu.umich.penning;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author adoxner
 *
 */
public class CollabEditText extends EditText {
	
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

}
