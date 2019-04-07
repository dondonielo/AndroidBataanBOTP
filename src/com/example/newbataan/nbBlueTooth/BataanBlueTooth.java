package com.example.newbataan.nbBlueTooth;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;


import com.example.newbataan.*;
import com.example.newbataan.BoardView.MoveStageListener;



import java.text.DecimalFormat;
import java.text.NumberFormat;

public class BataanBlueTooth extends Activity{
	public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_ENABLE_BT = 3;
    public final static String PLAY_MODE = "isServer";
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean server = false;
    private View mContentView;
    private View mLoadingView;

    private State yourPiece = State.EMPTY;
    
    private ViewGroup selectPiece;
    private ViewGroup boardFrame;
    
    private ArrayAdapter<String> mConversationArrayAdapter = null;
    private EditText txtMessage = null;
    private static final java.lang.String VIEW_LOG_TAG = "delete";
    private State[][] positions = new State[BoardView.BOARD_SIZE][BoardView.BOARD_SIZE];
    private BoardView boardView;
    private BataanMove bMove;
    private GameFlow game = new GameFlow();
    private boolean isFinish = false;
	private NumberFormat numberFormat = new DecimalFormat("00");
	
	/**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * Name of the device connecting to
     */
    private String mConnectingDeviceName = null;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;

    /**
     * Member object for the chat services
     */
    private BluetoothService mBluetoothService = null;
    private Handler mHandler = null;
    
    private Button btnDraw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	        // If the adapter is null, then Bluetooth is not supported
	    if (mBluetoothAdapter == null) {
	       Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	        finish();
	     }
	    
	    
	    server = getIntent().getExtras().getBoolean(PLAY_MODE);
        setContentView(R.layout.activity_bataan_blutooth);
        boardView = (BoardView) findViewById(R.id.boardViewBlueTooth);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/androidnation.ttf");
	    
	    btnDraw = (Button) findViewById(R.id.drawButton);
	    btnDraw.setTypeface(font);
	    
        gameSetup();
        boardView.setMoveStageListener(new CellSelected ());
 
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d("ONSTART-----", "ONSTART");
        // If BT is not on, request that it be enabled.
        // setupGame() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }else if(mBluetoothService == null){
            setUpBluetoothService();
            Log.d("SERVER-----" + server, "SERVER");
            if(!server) {
                connectDevice(getIntent(), true);
            }
        }
    }

    private void gameSetup(){
    	GameFlow.setGame(positions);
        // refreshScore()
        boardView.setPositions(positions);
    }
    @Override
    protected void onPause() {
        if (!isFinish) {
            sendMessage(new IdleMode());
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("ONSDESTROY-----", "ONDESTROY");
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
        //chatDialog.dismiss();
    }

    @Override
    public void onResume() {
        Log.d("ONRESUME-----", "ONRESUME");
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null && server) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                Log.d("mBluetoothService.start()", "before");
                mBluetoothService.start();
                Log.d("mBluetoothService.start()", "After");
            }
            if(!isFinish){
                Log.d("ResumeGame", " !finish ");
                sendMessage(new ResumeGame());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isFinish) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
            builder.setTitle("Quit Game");
            builder.setMessage("Are you sure you want to quit? ");
            // Add the buttons
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    isFinish = true;
                    finish();
                    System.exit(0);
                    BataanBlueTooth.super.onBackPressed();
                }
            });
            builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            builder.show();
        } else {
            finish();
            System.exit(0);
            BataanBlueTooth.super.onBackPressed();
        }
    }
    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(Object message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            //notifyMessage("Connection Notice", R.string.not_connected);
            Toast.makeText(BataanBlueTooth.this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothService.write(StreamUtils.toByteArray(message));
    }

    /**
     * Establish connection with other device
     */
    private void connectDevice(Intent data, boolean secure) {
        Log.d("CONNECT DEVICE-----", "CONNECT DEVICE");
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        //get the device name
        String name =  data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
        Log.d("Connecting", "name = "+name+" address = "+address);
        // Get the BluetoothDevice object
        setStatus(getString(R.string.connecting_title,name));
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device, secure);
        //server = false;
    }

    /**
     * Updates the status on the action bar.
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }


    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }


    private void setUpBluetoothService(){
        // Initialize the BluetoothService to perform bluetooth connection
        mBluetoothService = new BluetoothService(BataanBlueTooth.this, mHandler);
        Log.d("mmmm BluetoothService-----", "mmm BluetoothService");
    }


    private void showLoadingOrSelectPiece(boolean contentLoaded){

        Log.d("Loading", "showLoadingOrSelectPiece(boolean contentLoaded)");
        // Decide which view to hide and which to show.
        final View showView = contentLoaded ? mContentView : mLoadingView;
        final View hideView = contentLoaded ? mLoadingView : mContentView;

        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);
        showView.animate().alpha(1f).setDuration(mShortAnimationDuration).setListener(null);
        hideView.animate().alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hideView.setVisibility(View.GONE);
            }
        });

    }

    private void updateLoadingLabel(CharSequence msg){
        ((TextView)selectPiece.findViewById(R.id.select_piece_loading_label)).setText(msg);
    }

    private void removeSelectPieceLayout(){
        boardFrame.removeView(selectPiece);
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void refreshScore() {
    	 TextView bJScore= (TextView) findViewById(R.id.JapScoreB);;
    	 TextView bAScore= (TextView) findViewById(R.id.AmScoreB);;
		bJScore.setText(numberFormat.format(game.countPiece(positions, State.JAPANESE)));
		bAScore.setText(numberFormat.format(game.countPiece(positions, State.AMERICAN)));
	}
    private class CellSelected implements MoveStageListener {
        /*
		 * React on user click on the board. If user clicks on her/his
		 * ball then select that ball, of she/he select empty field then
		 * move ball, else display error by displaying error animation
		 * on that square.
		 */
    	private boolean isYourTurn = false;
		private boolean isJapaneseChange = false;
		private boolean isAmericanChange = false;
		private boolean isAmVictory = false;
		private boolean isJapVictory = false;

        public CellSelected(){
        	isYourTurn = server;

            if(server){
                yourPiece = State.JAPANESE;
                game.getMove().player = yourPiece;  
            }else{
                yourPiece = State.AMERICAN;
                game.getMove().player = yourPiece;
            }
            /**
             * The Handler that gets information back from the BluetoothChatService
             */
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case Constants.MESSAGE_STATE_CHANGE:
                            switch (msg.arg1) {
                                case BluetoothService.STATE_CONNECTED:
                                    refreshScore();
                                    gameSetup();
                                    boardView.invalidate();
                                    boardView.setFocusable(true);
                                    boardView.setFocusableInTouchMode(true);
                                    btnDraw.setText("DRAW");
                                    btnDraw.setOnClickListener(new View.OnClickListener() {									
										@Override
										public void onClick(View arg0) {
											isFinish = false;
											BataanBlueTooth.this.sendMessage(new DrawGame(DrawType.ASKDRAW));					
										}
									});
                                    ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                    BataanMove[] possibleMove= game.getValidMoves(positions, State.JAPANESE);
                                    highlightPieces(possibleMove, State.JAPANESE,  true);
                                    break;
                                case BluetoothService.STATE_CONNECTING:
                                    Log.e("=================", "(STATE_CONNECTING)");
                                    break;
                                case BluetoothService.STATE_LISTEN:
                                    Toast.makeText(BataanBlueTooth.this, "Waiting For Someone to Connect", Toast.LENGTH_SHORT).show();
                                    break;
                                case BluetoothService.STATE_NONE:
                                    setStatus(R.string.not_connected_title);
                                    break;
                            }
                            break;
                        case Constants.MESSAGE_DEVICE_NAME_CONNECTED_TO:
                            mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                            Toast.makeText(BataanBlueTooth.this, R.string.connected_title + mConnectedDeviceName, Toast.LENGTH_LONG).show();
                            break;
                        case Constants.MESSAGE_DEVICE_NAME_CONNECTING_TO:
                            mConnectingDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                            Toast.makeText(BataanBlueTooth.this, "CONNECTING to " + mConnectingDeviceName, Toast.LENGTH_LONG).show();
                            break;
                        case Constants.MESSAGE_TOAST:
                            Toast.makeText(BataanBlueTooth.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.MESSAGE_WRITE:
                            Log.e("=================", "(MESSAGE_WRITE)");
                            byte[] writeBuf = (byte[]) msg.obj;
                            Object yourMessage = StreamUtils.toObject(writeBuf);      
                            if(yourMessage instanceof BataanMove) {
                                BataanMove urMove = (BataanMove) yourMessage; 
                                BataanMove[] legalMoves = game.getValidMoves(positions, game.getMove().player);
                                MakeMove(urMove, true); 
                                game.setMove(urMove);     
                                if ((urMove.source.x - urMove.dest.x == 2 
                                		|| urMove.source.x  - urMove.dest.x == -2 
                                		|| urMove.source.y - urMove.dest.y == 2 
                                		|| urMove.source.y - urMove.dest.y == -2) == false && 
                                	(legalMoves[0].dest.y - 2 == legalMoves[0].source.y
                                        || legalMoves[0].dest.y + 2 == legalMoves[0].source.y 
                                        || legalMoves[0].dest.x - 2 == legalMoves[0].source.x
                                        || legalMoves[0].dest.x + 2 == legalMoves[0].source.x)) {
                                    if (urMove.source.x == legalMoves[0].source.x && urMove.source.y == legalMoves[0].source.y) {
                                        positions[urMove.dest.x][urMove.dest.y] = State.EMPTY ;
                                    } else {
                                        positions[legalMoves[0].source.x][legalMoves[0].source.y] = State.EMPTY;                  
                                    }
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                                    builder.setTitle("Failed to Capture a Japanese Soldier");
                                    builder.setMessage("An American Soldier was eliminated");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });
                                    builder.show();     
                                    isYourTurn = false;
                                    if ((!isYourTurn) || urMove.player == State.JAPANESE) {
                                        isJapaneseChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("AMERICAN TURN");
                                    } else if ((isYourTurn) || urMove.player== State.AMERICAN) {
                                        isAmericanChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                    } 
                                }
                                else if (urMove.source.x - urMove.dest.x == 2 
                                		|| urMove.source.x  - urMove.dest.x == -2 
                                		|| urMove.source.y - urMove.dest.y == 2 
                                		|| urMove.source.y - urMove.dest.y == -2) {  
                                BataanMove[] MoreJumps = game.pieceMoreJumps(positions, game.getMove().player, urMove.dest.x, urMove.dest.y);
                                if(MoreJumps != null) {
                                	((TextView)findViewById(R.id.statusGame)).setText("CONTINUE JUMPING, AMERICAN SOLDIER");
                                    highlightPieces1(MoreJumps, urMove.player, true);
                                    game.deleteMove();  
                                	if (urMove.player == State.JAPANESE) {
                                        isJapaneseChange = false;
                                    } else if (urMove.player== State.AMERICAN) {
                                        isAmericanChange = false;
                                    }                
                                }
                                else {
                                	isYourTurn = false;
                                	if (urMove.player == State.JAPANESE) {
                                        isJapaneseChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("AMERICAN TURN");
                                    } else if (urMove.player== State.AMERICAN) {
                                        isAmericanChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                    } 
                                }
                            }
                                else {
                                	isYourTurn = false;
                                	if (urMove.player == State.JAPANESE) {
                                        isJapaneseChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("AMERICAN TURN");
                                    } else if (urMove.player== State.AMERICAN) {
                                        isAmericanChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                    }         	
                                }
                            }else if (yourMessage instanceof DrawGame) {
                                switch (((DrawGame) yourMessage).type) {
                                case ASKDRAW:
                                    askDraw();
                                    break;
                                case DODRAW:
                                    drawGame(true);
                                    break;
                                case REFUSEDRAW:
                                    drawGame(false);
                                    break;
                                }
                            } else if(yourMessage instanceof String){
                                mConversationArrayAdapter.add("Me:  " + yourMessage);
                                txtMessage.setText("");

                            } else if(yourMessage instanceof IdleMode){
                                pauseGame();

                            }  else if(yourMessage instanceof ResumeGame){
                                resumeGame();
                            }
                            break;
                        case Constants.MESSAGE_READ:
                            byte[] readBuf = (byte[]) msg.obj;
                            Object message = StreamUtils.toObject(readBuf);                          
                            if(message instanceof BataanMove) {
                                BataanMove remoteMove = (BataanMove)message;
								BataanMove[] legalMoves = game.getValidMoves(positions, remoteMove.player);
                                MakeMove(remoteMove, true); 
                                game.setMove(remoteMove);                            
                                if ((remoteMove.source.x - remoteMove.dest.x == 2 
                                		|| remoteMove.source.x - remoteMove.dest.x == -2 
                                		|| remoteMove.source.y - remoteMove.dest.y == 2 
                                		||  remoteMove.source.y - remoteMove.dest.y == -2) == false && 
                                	(legalMoves[0].dest.y - 2 == legalMoves[0].source.y
                                        || legalMoves[0].dest.y + 2 == legalMoves[0].source.y 
                                        || legalMoves[0].dest.x - 2 == legalMoves[0].source.x
                                        || legalMoves[0].dest.x + 2 == legalMoves[0].source.x)) {
                                    if (remoteMove.source.x == legalMoves[0].source.x && remoteMove.source.y == legalMoves[0].source.y) {
                                        positions[remoteMove.dest.x][remoteMove.dest.y] = State.EMPTY;
                                    } else {
                                        positions[legalMoves[0].source.x][legalMoves[0].source.y] = State.EMPTY;
                                    }
                         
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                                    builder.setTitle("Failed to Capture a Japanese Soldier");
                                    builder.setMessage("An American Soldier was eliminated");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    });
                                    builder.show();
                                    
                                    isYourTurn = true;
                                    if ((!isYourTurn) || remoteMove.player == State.JAPANESE) {
                                        isJapaneseChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("AMERICAN TURN");
                                    } else if ((isYourTurn) || remoteMove.player == State.AMERICAN) {
                                        isAmericanChange = true;
                                        ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                    } 
                                
                                }
                                else if (remoteMove.source.x - remoteMove.dest.x == 2 
                                		|| remoteMove.source.x - remoteMove.dest.x == -2 
                                		|| remoteMove.source.y - remoteMove.dest.y == 2 
                                		||  remoteMove.source.y - remoteMove.dest.y == -2) {  
                                BataanMove[] MoreJumps = game.pieceMoreJumps(positions, State.AMERICAN, remoteMove.dest.x, remoteMove.dest.y);
                                if(MoreJumps != null) {
                                	((TextView)findViewById(R.id.statusGame)).setText("CONTINUE JUMPING, AMERICAN SOLDIER");
                                    highlightPieces1(MoreJumps, game.getMove().player, true);
                                    game.deleteMove(); 
                                    if (remoteMove.player == State.JAPANESE) {
                                        isJapaneseChange = false;
                                    } else if (remoteMove.player == State.AMERICAN) {
                                        isAmericanChange = false;
                                    } 
                                }
                                else {
                                	isYourTurn = true;
                                	  if (remoteMove.player == State.JAPANESE) {
                                          isJapaneseChange = true;
                                          ((TextView)findViewById(R.id.statusGame)).setText("AMERICAN TURN");
                                      } else if (remoteMove.player == State.AMERICAN) {
                                          isAmericanChange = true;
                                          ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                      } 
                                }
                            }
                                else {
                                	isYourTurn = true;
                                	  if (remoteMove.player == State.JAPANESE) {
                                          isJapaneseChange = true;
                                          ((TextView)findViewById(R.id.statusGame)).setText("AMERICAN TURN");
                                      } else if (remoteMove.player == State.AMERICAN) {
                                          isAmericanChange = true;
                                          ((TextView)findViewById(R.id.statusGame)).setText("JAPANESE TURN");
                                      } 
                                }
                            }else if (message instanceof DrawGame) {
                                switch (((DrawGame) message).type) {
                                case ASKDRAW:
                                    askForADraw();
                                    break;
                                case DODRAW:
                                    drawGame(true);
                                    break;
                                case REFUSEDRAW:
                                    drawGame(false);
                                    break;
                                }
                            } else if(message instanceof String){
                                mConversationArrayAdapter.add( mConnectedDeviceName+ ":  " + message);
                                Toast.makeText(BataanBlueTooth.this, "Message From " + mConnectingDeviceName, Toast.LENGTH_SHORT).show();
                            }else if(message instanceof IdleMode){
                                pauseGame();
                            }else if(message instanceof ResumeGame){
                                resumeGame();
                            }
                            break;
                    }
                }
            };
        }


        private void resumeGame(){
            boardView.setFocusable(true);
            boardView.setFocusableInTouchMode(true);

            //();

            //if(isYourTurn){}
            //resumeTimer();
        }

        private void pauseGame(){
           // showSelectPieceLayout();

            boardView.setFocusable(false);
            boardView.setFocusableInTouchMode(false);

            //showLoadingOrSelectPiece(false);
            // updateLoadingLabel(game.getOpposingColour(yourPiece).toString() +" is in IDLE Mode");
            //if(isYourTurn){}
            //pauseTimer();
        }
        private void askForADraw() {
            //pauseTimer();
            final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
            builder.setTitle("Asking For A Draw");
            builder.setMessage(mConnectedDeviceName + " is asking For a DRAW. ");
            // Add the buttons
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendMessage(new DrawGame(DrawType.DODRAW));
                }
            });
            builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendMessage(new DrawGame(DrawType.REFUSEDRAW));
                }
            });
            builder.show();
        }

        private void askDraw() {
            //serverSelectPiece();

            boardView.setFocusable(false);
            boardView.setFocusableInTouchMode(false);

            //showLoadingLabel(false);
            //updateLoadingLabel("Asking Draw to the " + game.getOpposingChip(yourPiece).toString());
            //((TextView)findViewById(R.id.statusGame)).setText("Asking Draw to " + yourPiece.toString());
            //showToast("Asking Draw to" + yourPiece.toString());
            //pauseTimer();
            final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
            builder.setTitle("ALERT!");
            builder.setMessage("You are asking for a DRAW from " + yourPiece.toString());
            // Add the buttons
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            builder.show();
        }

        private void drawGame(boolean doDraw) {
            if (doDraw) {
                isFinish = true;
                final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                builder.setTitle("Response");
                builder.setMessage("The Game is a DRAW");
                // Add the buttons
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isFinish = true;
                        finish();
                        System.exit(0);
                        BataanBlueTooth.super.onBackPressed();
                    }
                });
                builder.show();

            } else {
                //removeSelectPieceLayout();
                //showToast("Refuse to Draw");
            	final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                builder.setTitle("Response");
                builder.setMessage("You Refuse the Draw");
                // Add the buttons
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                builder.show();
            }
        }


        public void userClick(int x, int y) {
        	
        	 if(mBluetoothService.getState() != BluetoothService.STATE_CONNECTED){
                 //notifyMessage("Connection Notice", R.string.title_not_connected);
                 Toast.makeText(BataanBlueTooth.this, R.string.not_connected_title, Toast.LENGTH_SHORT).show();
                 return;
             }

             if(!isYourTurn){
                 //notifyMessage("", getString( R.string.not_you_turn, mConnectedDeviceName));
                 Toast.makeText(BataanBlueTooth.this, R.string.not_you_turn + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                 return;
             }
             
            Coordinate source = game.getMove().source;
            Coordinate destination = game.getMove().dest;
           

            BataanMove[] legalMoves = game.getValidMoves(positions, game.getMove().player);
            BataanMove[] legalJumps = game.getValidJumps(positions, game.getMove().player);
            
           
            //server turn and the server piece is click
            if(!isFinish && (((positions[x][y] == State.JAPANESE && yourPiece == State.JAPANESE) 
            		|| (positions[x][y] == State.AMERICAN && yourPiece == State.AMERICAN)) 
            		&& legalMoves != null)){
            	
            	
                    for (int i = 0; i < legalMoves.length; i++) {
                        if (legalMoves[i].source.x == x && legalMoves[i].source.y == y) {
                        	
                        	
                        	if (source.isNotEmpty()) {
            					positions[source.x][source.y] = positions[x][y];
            				}         	
                        	
            				boardView.selectBall(x, y, State.SELECTED, legalMoves);
            				source.x = x;
            				source.y = y;
            				
            				Log.d("PISLIT", "Coordniate" + x + y);
            				
            				if(yourPiece == State.JAPANESE || positions[x][y]== State.JAPANESE){
            					showToast("Japanese, Select Destination");
            					break;
            				}
            				else if (yourPiece == State.AMERICAN || positions[x][y]== State.AMERICAN) {
            					showToast("American, Select Destination");
            					break;
            				}
                        	}
                        else{
                            boardView.error(x, y);
                            //showToast("Reminder: Blue -- > American , Yellow --> Japanese");
                             	
                        }
                        
                    }
                    
               
            }
            

            //destination cell is click
            else if (!isFinish && source.isNotEmpty() && positions[x][y] == State.EMPTY && legalMoves!=null) {
                destination.x = x;
                destination.y = y;
//change
                //if(legalMoves!=null) {
                    // confirm legal move
                    for (int i = 0; i < legalMoves.length; i++) {
                    	if (legalMoves[i].source.x == source.x  && legalMoves[i].source.y == source.y
                                && legalMoves[i].dest.x == destination.x && legalMoves[i].dest.y == destination.y) { 
                        	//MakeMove(legalMoves[i], true);  	
                        	Log.e("TUGPA", "Coordinates" + " " + x + " " + y);
                        	sendMessage(game.getMove());
                        	//game.setMove(game.getMove());
                    }
                 } 
                    

            }
            else {
                boardView.error(x, y);
                if (yourPiece == State.JAPANESE) {
                    isJapaneseChange = false;
                } else if (yourPiece == State.AMERICAN) {
                    isAmericanChange = false;
                }
            }
        }

        @Override
        public void animationComplete() {

            if (isAmVictory) {
                isAmVictory = false;
                // stop all activity

            }
            if (isJapVictory) {
                isJapVictory = false;
                // stop all activity

            }

            if (isAmericanChange) {
            		
                    isAmericanChange = false;
                    //isYourTurn = false;
    
                    game.deleteAll();
                    BataanMove[] pieceMove = game.getValidMoves(positions, State.JAPANESE);
                  
       
                 if (pieceMove != null ) {
                	 	refreshScore();
                        game.getMove().player = State.JAPANESE;
                        highlightPieces(pieceMove, game.getMove().player, true);
                    } 
                
                checkBataanGame();

            }

            if (isJapaneseChange) {

                    isJapaneseChange = false;
                    //isYourTurn = true;

                    
                    game.deleteAll();
                    BataanMove[] pieceMove = game.getValidMoves(positions, State.AMERICAN);
               
                    
                    if (pieceMove != null) {
                    	refreshScore();
                        game.getMove().player = State.AMERICAN;
                        highlightPieces1(pieceMove, game.getMove().player, true);
                    }
                    
                checkBataanGame();
            }
            
       
        }
        private void checkBataanGame(){
        	BataanMove[] AmValMove = game.getValidMoves(positions, State.AMERICAN);
        	
        	if (game.countPiece(positions, State.JAPANESE) <= 16 ){
        		isAmVictory = true;
        		isFinish = true;
        		//showToast("American Wins, 16 Japanese Soldiers remaining");	
        		 final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                 builder.setTitle("American Wins!");
                 builder.setMessage("16 Japanese Soldiers remaining ");
                 // Add the buttons
                 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         isFinish = true;
                         finish();
                         System.exit(0);
                         BataanBlueTooth.super.onBackPressed();
                     }
                 });
                 builder.show();
        	}
        	
        	else if (game.countPiece(positions, State.AMERICAN) == 0){
        		isJapVictory = true;
        		isFinish = true;
        		//showToast("Japanese Wins, No American Soldiers Remaining");
        		final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                builder.setTitle("Japanese Wins!");
                builder.setMessage("No American Soldiers Remaining"); 
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isFinish = true;
                        finish();
                        System.exit(0);
                        BataanBlueTooth.super.onBackPressed();
                    }
                });
                builder.show();
        	}
        	else if(DefenseAreaOccupied()){
        		isJapVictory = true;
        		isFinish = true;
        		//showToast("Japanese Wins, American Defense has occupied");
        		final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                builder.setTitle("Japanese Wins!");
                builder.setMessage("American Field Defense has been occupied");
                // Add the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isFinish = true;
                        finish();
                        System.exit(0);
                        BataanBlueTooth.super.onBackPressed();
                    }
                });
                builder.show();
        		
        	}
        	else if(AmValMove == null){
        		isJapVictory = false;
        		isAmVictory = false;
        		isFinish = true;
        		//showToast("It's a Tie, American can't move and Japanese hasn't occupied the American Defense");
        		final AlertDialog.Builder builder = new AlertDialog.Builder(BataanBlueTooth.this);
                builder.setTitle("It is a Tie!");
                builder.setMessage("American cannot move and Japanese failed to occupy the American Defense");
                // Add the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isFinish = true;
                        finish();
                        System.exit(0);
                        BataanBlueTooth.super.onBackPressed();
                    }
                });
                builder.show();
        		
        	}
        	
        	
        }
    } 
    
   
    //source
    private void highlightPieces(BataanMove[] pieces, State owner, boolean b) {
        boardView.highLightPieceSquare(pieces, owner);
    }
    
    private void highlightPieces1(BataanMove[] pieces, State owner, boolean b) {
        boardView.highLightPieceSquare1(pieces, owner);
    }
    
    
    //changed
    public void MakeMove(BataanMove move, boolean withAnimation){
    	int cRow = move.source.x,
   		     cCol = move.source.y,
   		     nRow = move.dest.x, 
   		     nCol = move.dest.y; 
		 State player = move.player;
		 

		if (withAnimation){
			Log.e("COORDINATES", "COORDINATES" + cRow + cCol );
		
	            boardView.moveBall(cRow, cCol, nRow, nCol, player);
	            }
				game.piecedoMove(cRow, cCol, nRow, nCol, player, positions);
				
	 }
    
   
    
    public boolean DefenseAreaOccupied(){
        boolean IsOccupied = true;
        for (int i = 1; i < 8; i++) {
            if(positions[i][0] != State.JAPANESE){
                IsOccupied = false;
                break;
            }
        }
        if(IsOccupied){
                for (int j = 2; j < 7; j++) {
                    if(positions[j][1] != State.JAPANESE){
                        IsOccupied = false;
                        break;
                    }else if(positions[j][2] != State.JAPANESE){
                        IsOccupied = false;
                        break;
                    }
               }
        }

        return IsOccupied;
    }


}