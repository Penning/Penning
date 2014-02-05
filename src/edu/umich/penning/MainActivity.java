package edu.umich.penning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener.CollabrifyBroadcastListener;
import edu.umich.imlc.collabrify.client.CollabrifyListener.CollabrifyCreateSessionListener;
import edu.umich.imlc.collabrify.client.CollabrifyListener.CollabrifyJoinSessionListener;
import edu.umich.imlc.collabrify.client.CollabrifyListener.CollabrifyLeaveSessionListener;
import edu.umich.imlc.collabrify.client.CollabrifyListener.CollabrifyListSessionsListener;
import edu.umich.imlc.collabrify.client.CollabrifyListener.CollabrifySessionListener;
import edu.umich.imlc.collabrify.client.CollabrifyParticipant;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyUnrecoverableException;


public class MainActivity extends Activity implements
		CollabrifySessionListener, CollabrifyListSessionsListener,
		CollabrifyBroadcastListener, CollabrifyCreateSessionListener,
		CollabrifyJoinSessionListener, CollabrifyLeaveSessionListener {
	
	public static Context context;
	public static EditText et;
	private CollabEditTextListener listener;
	static boolean undo_redo_action = false;
	static boolean prev_undo = false;
	static boolean prev_redo = false;
	
	private static String TAG = "Penning";
	
	private static final String GMAIL = "user email";
	private static final String DISPLAY_NAME = "user display name";
	private static final String ACCOUNT_GMAIL = "441winter2014@umich.edu";
	private static final String ACCESS_TOKEN = "338692774BBE";
	
	private CollabrifyClient myClient;
	private TextView broadcastedText;
	private EditText broadcastText;
	private Button connectButton;
	private ArrayList<String> tags = new ArrayList<String>();
	private long sessionId;
	private String sessionName;
	private String password = "password";

	public static String userId;

	
	// redundant but for the sake of readability
	private CollabrifySessionListener sessionListener = this;
	private CollabrifyListSessionsListener listSessionsListener = this;
	private CollabrifyBroadcastListener broadcastListener = this;
	private CollabrifyCreateSessionListener createSessionListener = this;
	private CollabrifyJoinSessionListener joinSessionListener = this;
	private CollabrifyLeaveSessionListener leaveSessionListener = this;

	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (listener == null)
        	listener = new CollabEditTextListener(this);
        
        // Instantiate client object
        try{
          myClient = CollabrifyClient.newClient(this, GMAIL, DISPLAY_NAME,
              ACCOUNT_GMAIL, ACCESS_TOKEN, false);
        }
        catch( InterruptedException e ){
          Log.e(TAG, "error", e);
        }
        catch( ExecutionException e ){
          Log.e(TAG, "error", e);
        }
        
        setContentView(R.layout.activity_main);
        context = this;
        et = (EditText) findViewById(R.id.collabEditText1);
        et.addTextChangedListener(listener);
        

        // random userID
        userId = String.valueOf( String.valueOf( Double.valueOf(Math.random() * 1000000.0).intValue() ) );

        
        tags.add("sample");
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.undo_button:
            	Toast.makeText(this, "Undo Pressed" , Toast.LENGTH_SHORT).show();
            	undo_redo_action = true;
            	listener.undo();
                return true;
            case R.id.redo_button:
            	Toast.makeText(this, "Redo Pressed" , Toast.LENGTH_SHORT).show();
            	undo_redo_action = true;
            	listener.redo();
                return true;
            case R.id.create_session:
            	doCreateSession();
            	return true;
            case R.id.join_session:
            	getSessionId();
            	return true;
            case R.id.leave_session:
            	doLeaveSession();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onError(CollabrifyException e) {
		// print error
		Log.e(TAG, "error: ", e);
		
		if(e instanceof CollabrifyUnrecoverableException)
	    {
	      //the client has been reset and we are no longer in a session
	      onDisconnect();
	    }
	    Log.e(TAG, "error", e);
	}

	@Override
	public void onDisconnect() {
		System.out.println("Disconnected");
	}

	@Override
	public void onSessionJoined(long maxOrderId, long baseFileSize) {
		System.out.println("Session joined!");
		showToast("Joined session " + sessionId);
	}

	@Override
	public void onSessionCreated(CollabrifySession session) {
		
		sessionId = session.id();
	    sessionName = session.name();
		
		System.out.println("Session created: " + session.id());
		showSessionId();
	}

	@Override
	public void onBroadcastDone(byte[] event, long orderId, long srid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveSessionList(List<CollabrifySession> sessionList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBaseFileReceived(File baseFile) {
		Log.v(TAG, "base file recieved");
		
		FileInputStream baseInput;
		try {
			baseInput = new FileInputStream(baseFile);
			
			byte[] data = new byte[(int)baseFile.length()];
			baseInput.read(data, 0, (int)baseFile.length());
			
			final BaseProtocol.Base recievedBase = BaseProtocol.Base.parseFrom(data);
			
			// set text from base file
			runOnUiThread(new Runnable() {
			    public void run() {
			    	et.setText(recievedBase.getDocument());
			    }
			});
			
			
		} catch (FileNotFoundException e1) {
			Log.e(TAG, "Base File not found.");
			e1.printStackTrace();
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onBaseFileUploadComplete(long baseFileSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFurtherJoinsPrevented() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onParticipantJoined(CollabrifyParticipant p) {
		showToast("Now entering: " + p.getDisplayName());
	}

	@Override
	public void onParticipantLeft(CollabrifyParticipant p) {
		showToast(p.getDisplayName() + "has disconnected.");
	}

	@Override
	public void onReceiveEvent(long orderId, int submissionRegistrationId,
			String eventType, byte[] data, long elapsed) {
		EventProtocol.Event recievedEvent = null;
		try {
			recievedEvent = EventProtocol.Event.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		//System.out.println("Event UserID: " + recievedEvent.getUserID());
		//System.out.println("My UserID: " + userId);
//		if(recievedEvent.getUserID().equals(userId)) return;
		
		Event e1 = new Event(recievedEvent, orderId);
		e1.userID = userId;
		listener.onRemoteTextChange(e1);
	}
	
	public void BroadcastEvent(Event e)
	{
	  if(e == null)
	    return;
	  if( myClient != null && myClient.inSession() )
	  {
	    try
	    {
	      //showToast("Sending Event...");
	      EventProtocol.Event.Builder builtMessage = EventProtocol.Event.newBuilder();
	      builtMessage.setEventID((Double.valueOf(Math.random())).intValue());
	      if(e.userID == null)
	    	  builtMessage.setUserID(userId);
	      else
	    	  builtMessage.setUserID(e.userID);
	      
	      if(e.sessionID == null)
	    	  builtMessage.setSessionID(String.valueOf(sessionId));
	      else
	    	  builtMessage.setSessionID(e.sessionID);
	      
	      switch(e.event)
	      {
	      case insert: builtMessage.setType(EventProtocol.Event.EventType.INSERT); break;
	      case delete: builtMessage.setType(EventProtocol.Event.EventType.DELETE); break;
	      case undo: builtMessage.setType(EventProtocol.Event.EventType.UNDO); break;
	      case redo: builtMessage.setType(EventProtocol.Event.EventType.REDO); break;
	      case cursorLocationChanged: builtMessage.setType(EventProtocol.Event.EventType.CURSORLOCATIONCHANGED); break;
	      default: //error
	      }
	      builtMessage.setCursorLocation(e.cursorLocation)
	      	.setText(Character.toString(e.text));
	     
	      //TODO: resolve event types with second param
	      myClient.broadcast(builtMessage.build().toByteArray(), e.event.toString(), broadcastListener);
	    }
	    catch( CollabrifyException err )
	    {
	    onError(err);
	    }
	  }
	}

	@Override
	public void onSessionEnd(long id) {
		// TODO Auto-generated method stub
	}
	
	private void showToast(final String text)
	  {
	    runOnUiThread(new Runnable()
	    {

	      @Override
	      public void run()
	      {
	        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
	      }
	    });
	  }
	
	
	public void doCreateSession()
	  {
		
		if (myClient.inSession()){
			showToast("Already in session " + sessionId);
			return;
		}
		
	    try
	    {
	      Random rand = new Random();
	      sessionName = "Test " + rand.nextInt(Integer.MAX_VALUE);
	      
	      // make base file
	      File baseFile = new File(context.getFilesDir().getPath().toString() + "baseFile");
	      
	      // using base protocol
	      BaseProtocol.Base.Builder builtMessage = BaseProtocol.Base.newBuilder();
	      builtMessage.setUserID(String.valueOf(userId));
	      builtMessage.setSessionID("none");
	      builtMessage.setDocument(et.getText().toString());
	      
	      
	      // write to file
	      byte dataToWrite[] = builtMessage.build().toByteArray();
	      FileOutputStream out = new FileOutputStream(baseFile);
	      out.write(dataToWrite);
	      out.close();
	      
	      // make input stream
	      FileInputStream baseFileInputStream = new FileInputStream(baseFile);
	      
	      // create and send basefile
	      myClient.createSessionWithBase(sessionName, tags, password, 
	    		  0, baseFileInputStream, createSessionListener, sessionListener);
	      
	      //myClient.createSession(sessionName, tags, password, 0,
	      //    createSessionListener, sessionListener);
	    }
	    catch( CollabrifyException e )
	    {
	    	Log.w(TAG, "doCreateSession() error");
	    	onError(e);
	    }catch( Exception e){
	    	Log.w(TAG, "doCreateSession() error");
	    	e.printStackTrace();
	    }
	  }
	
	public void getSessionId(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter session ID:");

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	if(input.length() == 0 || input == null) return;
		    	
		        sessionId = Long.parseLong( input.getText().toString() );
		    	System.out.println(sessionId);
		    	doJoinSession();
		    }
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
	}
	
	public void showSessionId(){
		
		runOnUiThread(new Runnable(){ 
			public void run(){
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
	 
				// set title
				alertDialogBuilder.setTitle("Your session ID:");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage(String.valueOf(sessionId))
					.setCancelable(false)
					.setPositiveButton("OK", null);
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
			}
		});
			
	}
	
	public void doJoinSession()
	  {
	    if( myClient.inSession() )
	    {
	      return;
	    }
	    try
	    {
	      //myClient.requestSessionList(tags, listSessionsListener);
	    	myClient.joinSession(sessionId, password, (CollabrifyJoinSessionListener) createSessionListener, sessionListener);
	    }
	    catch( Exception e )
	    {
	      Log.e(TAG, "error", e);
		    
//		    if (!myClient.inSession()){
		    	showToast("Invalid session ID");
//		    }
	      
	    }
	  }

	public void doLeaveSession()
	  {
	    if( !myClient.inSession() )
	    {
	      return;
	    }
	    try
	    {
	      myClient.leaveSession(false, leaveSessionListener);
	      showToast("left.");
	    }
	    catch( CollabrifyException e )
	    {
	      onError(e);
	    }
	  }
}
