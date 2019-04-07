package com.example.newbataan;


import android.app.Activity;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;




import com.example.newbataan.R;
import com.example.newbataan.BoardView.MoveStageListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;








public class MainNewBataan extends Activity {

    private static final java.lang.String VIEW_LOG_TAG = "delete";
    
    private State[][] positions = new State[BoardView.BOARD_SIZE][BoardView.BOARD_SIZE];

    private BoardView boardView;
    
    private BataanMove bMove;
    
    private GameFlow game = new GameFlow();

    private boolean isFinish = false;
    
	private NumberFormat numberFormat = new DecimalFormat("00");
	
	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		for (int i = 0; i < boardView.BOARD_SIZE; i++) {
			for (int j = 0; j < boardView.BOARD_SIZE; j++) {
				positions[i][j] = State
						.fromInt(bundle.getIntArray("pos" + i)[j]);
			}
		}
		refreshScore();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main_new_bataan);
        boardView = (BoardView) findViewById(R.id.boardView);
       
        GameFlow.setGame(positions);
        // refreshScore()
        boardView.setPositions(positions);
        boardView.setFocusable(true);
        boardView.setFocusableInTouchMode(true);
        
        boardView.setMoveStageListener(new CellSelected ());
        
       
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void refreshScore() {
    	 TextView JScore= (TextView) findViewById(R.id.JapScore);;
    	 TextView AScore= (TextView) findViewById(R.id.AmScore);;
		JScore.setText(numberFormat.format(game.countPiece(positions, State.JAPANESE)));
		AScore.setText(numberFormat.format(game.countPiece(positions, State.AMERICAN)));
	}
    private class CellSelected implements MoveStageListener {
        /*
		 * React on user click on the board. If user clicks on her/his
		 * ball then select that ball, of she/he select empty field then
		 * move ball, else display error by displaying error animation
		 * on that square.
		 */
    	private boolean isAmMove = false;
		
		private boolean isAmSelected = false;
		private boolean isJapSelected = false;

		private boolean isJapaneseChange = false;
		private boolean isAmericanChange = false;

		private boolean isAmVictory = false;
		private boolean isJapVictory = false;
		
		private boolean isAmTurn = false;


        public CellSelected(){
        	refreshScore();
            game.getMove().player = State.JAPANESE;
            BataanMove[] possibleMove= game.getValidMoves(positions, game.getMove().player);
            Log.d("possible??: (" + possibleMove.length +  "): ", "possible");
            highlightPieces(possibleMove, game.getMove().player,  true);
        }

        public void userClick(int x, int y) {
            Coordinate source = game.getMove().source;
            Coordinate destination = game.getMove().dest;
           

            BataanMove[] legalMoves = game.getValidMoves(positions, game.getMove().player);
            BataanMove[] legalJumps = game.getValidJumps(positions, game.getMove().player);
            
           

            //server turn and the server piece is click
            if(!isFinish && ((positions[x][y] == State.JAPANESE || positions[x][y] == State.AMERICAN) && legalMoves != null)){
            	
            	
                    for (int i = 0; i < legalMoves.length; i++) {
                        if (legalMoves[i].source.x == x && legalMoves[i].source.y == y) {
                        	
                        	
                        	if (source.isNotEmpty()) {
            					positions[source.x][source.y] = positions[x][y];
            				}         	
                        	
            				boardView.selectBall(x, y, State.SELECTED, legalMoves);
            				source.x = x;
            				source.y = y;
            				
            				if(positions[x][y]== State.JAPANESE){
            					showToast("Japanese, Select Destination");
            					break;
            				}
            				else {
            					showToast("American, Select Destination");
            					break;
            				}
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
                        	Log.e("Unique", "Coordinates" + x + y );                	   
                        	MakeMove(legalMoves[i], true);
                            BataanMove[] MoreJumps = game.pieceMoreJumps(positions, State.AMERICAN, destination.x, destination.y);
                            if ((source.x - destination.x == 2 
                            		|| source.x - destination.x == -2 
                            		|| source.y - destination.y == 2 
                            		|| source.y - destination.y == -2) == false && 
                            	(legalMoves[0].dest.y - 2 == legalMoves[0].source.y
                                    || legalMoves[0].dest.y + 2 == legalMoves[0].source.y 
                                    || legalMoves[0].dest.x - 2 == legalMoves[0].source.x
                                    || legalMoves[0].dest.x + 2 == legalMoves[0].source.x)) {
                                if (source.x == legalMoves[0].source.x && source.y == legalMoves[0].source.y) {
                                    positions[destination.x][destination.y] = State.EMPTY;
                                } else {
                                    positions[legalMoves[0].source.x][legalMoves[0].source.y] = State.EMPTY;
                                }
                                showToast("Failed to Capture Japanese Soldier, American soldier has been eliminated");
                                if(!isAmTurn) {
                            		isJapaneseChange = true;
                            	}else if(isAmTurn){
                            		isAmericanChange = true;
                   
                            	} else {
                            		boardView.error(x, y);
                            	}
                            }
                            else if (source.x - destination.x == 2 
                            		|| source.x - destination.x == -2 
                            		|| source.y - destination.y == 2 
                            		|| source.y - destination.y == -2) {   
                            if(MoreJumps != null) {
                            	showToast("Continue jumping American Soldier");
                                highlightPieces1(MoreJumps, game.getMove().player, true);
                                game.deleteMove();  	   
                            }
                            else {
                            	if(!isAmTurn) {
                            		isJapaneseChange = true;
                            	}else if(isAmTurn){
                            		isAmericanChange = true;
                   
                            	} else {
                            		boardView.error(x, y);
                            	}
                            }
                        }
                            else {

                            	if(!isAmTurn) {
                            		isJapaneseChange = true;
                            	}else if(isAmTurn){
                            		isAmericanChange = true;
                   
                            	} else {
                            		boardView.error(x, y);
                            	}
                            
                            }
                    }
                 } 
                    

            }
            else {
                boardView.error(x, y);
                if(!isAmTurn)
                    isJapaneseChange = false;
                else
                    isAmericanChange = false;
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
                    isAmTurn = false;
    
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
                    isAmTurn = true;

                    
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
        	
        	if (game.countPiece(positions, State.JAPANESE) < 17 ){
        		isAmVictory = true;
        		isFinish = true;
        		showToast("American Wins, 16 Japanese Soldiers remaining");	
        	}
        	
        	else if (game.countPiece(positions, State.AMERICAN) == 0){
        		isJapVictory = true;
        		isFinish = true;
        		showToast("Japanese Wins, No American Soldiers Remaining");	
        	}
        	else if(DefenseAreaOccupied()){
        		isJapVictory = true;
        		isFinish = true;
        		showToast("Japanese Wins, American Defense has occupied");
        	}
        	else if(AmValMove == null){
        		isJapVictory = false;
        		isAmVictory = false;
        		isFinish = true;
        		showToast("It's a Tie, American can't move and Japanese hasn't occupied the American Defense");
        		
        	}
        	
        	
        }
    } 
    
   
    //source-JAPANESE
    private void highlightPieces(BataanMove[] pieces, State owner, boolean b) {
        boardView.highLightPieceSquare(pieces, owner);
    }
  
    //source-AMERICAN
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
		 

		if (withAnimation)
	            boardView.moveBall(cRow, cCol, nRow, nCol, player);
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