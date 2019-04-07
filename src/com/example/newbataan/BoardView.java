package com.example.newbataan;

import java.io.Serializable;
import java.util.List;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;



/**
 * Implementation of board view/controller. It draws board, handle user events
 * (touch), rotating screen and animation.
 */
public class BoardView extends View implements Serializable{

	public static final int BOARD_MARGIN = 10;
	public static final int BOARD_SIZE = 9;
	public static final int GRID_SIZE = 2;
	private static final int MSG_ANIMATE = 0;

	private final Handler animationHandler = new Handler(
			new AnimationMessageHandler());
	private MoveStageListener moveStageListener;

	/**
	 * Listener interface that send messages to Activity. Activity then handle
	 * this messages.
	 */
	public interface MoveStageListener {

		// Fires when user click's somewhere on board.
		void userClick(int x, int y);

		// When animation complete at same current move stage is complete.
		void animationComplete();
	}

	public void setMoveStageListener(MoveStageListener selectionListener) {
		this.moveStageListener = selectionListener;
	}

	/**
	 * Animation interface that control animation handler.
	 */
	public interface Animation {
		// This is called on onDraw method.
		void animate(Canvas canvas);

		// Say if animation should end.
		boolean isFinish();

		// Control which cells will be animated and hence should be
		// ignored when we draw grid.
		boolean skip(int x, int y);

		// How much frames per second we will use for our animation.
		int fps();
	}

	private Animation animation = new NullAnimation();

	// Here we store animation board state with all players and intermediate
	// states for cells.
	private State[][] positions;

	public void setPositions(State[][] positions) {
		this.positions = positions;
	}

	// Paint for board table line. It is here because onPaint is
	// using it several time per frame.
	private Paint boardLinePaint;

	// Width of board is also calculated dynamically when screen
	// size changes.
	private float boardWidth;

	// Maximum radius of ball - calculated dynamically also...
	private float maxRadius;

	// Can freely be here because it is calculated every time screen size
	// changes.
	private float cellSize;
	
	private Bitmap japanFlag;
	private Bitmap americanFlag;
	private Bitmap BgSnow;
	

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		requestFocus();
		boardLinePaint = new Paint();
	  
		
		japanFlag = getResBitmap(R.drawable.japflag);
	    americanFlag =getResBitmap(R.drawable.amflag);
	    BgSnow = getResBitmap(R.drawable.snow);
	}

	/*
	 * Classic onDraw. It paints table and ball states. When we need to animate
	 * stuff we call it to refresh canvas state (easy as in classic Java 2D
	 * graphics animation).
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float offsetBoardWidth = boardWidth - BOARD_MARGIN;
	      
	      
	      //rectF BgLoc = new RectF(0, 0, offsetBoardWidth + BOARD_MARGIN, offsetBoardWidth + BOARD_MARGIN); 
	      //canvas.drawBitmap(BgSnow, null ,BgLoc, null);
	      
	     /* boardLinePaint.setColor(Color.);  
	      boardLinePaint.setStyle(Style.FILL);
	      canvas.drawRect(0, 0, offsetBoardWidth + BOARD_MARGIN, offsetBoardWidth + BOARD_MARGIN, boardLinePaint);*/
	      	
	      for (int row = 0; row < BOARD_SIZE; row++) {
	          for (int col = 0; col < BOARD_SIZE; col++) {
	        	  if ((col == 0 && (row == 7 || row == 8))
	                      || (col == 1 && (row == 7 || row == 8))
	                      || (col == 7 && (row == 8 || row == 7))
	                      || (col == 8 && (row == 7 || row == 8))
	                      || (col == 0 && (row == 1 || row == 0))
	                      || (col == 1 && (row == 1))
	                      || (col == 7 && (row == 1))
	                      || (col == 8 && (row == 1 || row == 0))) {
	              } else {

	              float r = row * cellSize;
	              float c = col * cellSize;
	              //boardLinePaint.setColor(((row == 0 || row == 1) || (row == 2 && col == 2) || (row == 2 && col == 3) || (row == 2 && col == 4) || (row == 2 && col == 5) || (row == 2 && col == 6)) ? Color.GREEN : Color.DKGRAY);
	              //canvas.drawRect(c+ ((BOARD_SIZE*2)-5), r+ ((BOARD_SIZE*2)-5), c+cellSize + BOARD_SIZE , r+cellSize + BOARD_SIZE, boardLinePaint);
	              
	              RectF Loc = new RectF (c+ ((BOARD_SIZE*2)-5), r+ ((BOARD_SIZE*2)-5), c+cellSize + BOARD_SIZE , r+cellSize + BOARD_SIZE);
	              
	              if ((row == 0 || row == 1) || (row == 2 && col == 2) || (row == 2 && col == 3) || (row == 2 && col == 4) || (row == 2 && col == 5) || (row == 2 && col == 6)) { 	 
	            	canvas.drawBitmap(americanFlag, null, Loc, null); 
	              }
	              else{
	            	  canvas.drawBitmap(japanFlag, null, Loc, null);
	            
	              }
	              
	              }
	      }
	      }
		
	    setValuesFromDatas(canvas);

		animation.animate(canvas);
	}
	
	 private Bitmap getResBitmap(int bmpResId) {
	      Options opts = new Options();
	      opts.inDither = false;

	      Resources res = getResources();
	      Bitmap bmp = BitmapFactory.decodeResource(res, bmpResId, opts);

	      if (bmp == null && isInEditMode()) {
	          Drawable d = res.getDrawable(bmpResId);
	          int w = d.getIntrinsicWidth();
	          int h = d.getIntrinsicHeight();
	          bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
	          Canvas c = new Canvas(bmp);
	          d.setBounds(0, 0, w - 1, h - 1);
	          d.draw(c);
	      }

	      return bmp;
	  }

	/*
	 * Set values from board state structure and skip animated items.
	 */
	private void setValuesFromDatas(Canvas canvas) {
		for (int i = 1; i < 10; i++) {
			for (int j = 1; j < 10 ; j++) {
				// If this are currently animated squares, do not
				// draw them!
				if (!animation.skip(i-1, j-1))
					drawBall(i, j, positions[i-1][j-1], maxRadius, canvas,
							255);
				drawSolidSquare(canvas, i, j, positions[i-1][j-1]);
			}
		}
	}

	/*
	 * Method for drawing filled square (when user touch inappropriate section
	 * of table). It is stupid to create Paint object every time, but it is here
	 * for readability and encapsulation reasons.
	 */
	private void drawWhiteSquare(Canvas canvas, int i, int j, int alpha) {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		paint.setAlpha(alpha);
		drawCustomRect(i, j, canvas, paint, 0);
	}
	
	private void drawLGSquare(Canvas canvas, int i, int j, int alpha){
		Paint paint = new Paint();
		paint.setColor(Color.rgb(0, 255, 255));
		paint.setStyle(Style.FILL);
		paint.setAlpha(alpha);
		drawCustomRect(i, j, canvas, paint, 0);	
	}
	private void drawJSquare(Canvas canvas, int i, int j, int alpha){
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 51, 0));
		paint.setStyle(Style.FILL);
		paint.setAlpha(alpha);
		drawCustomRect(i, j, canvas, paint, 0);	
	}
	private void drawGSquare(Canvas canvas, int i, int j, int alpha){
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Style.FILL);
		paint.setAlpha(alpha);
		drawCustomRect(i, j, canvas, paint, 0);	
	}
	private void drawASquare(Canvas canvas, int i, int j, int alpha){
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 204, 0));
		paint.setStyle(Style.FILL);
		paint.setAlpha(alpha);
		drawCustomRect(i, j, canvas, paint, 0);	
	}


	private void drawCustomRect(int i, int j, Canvas canvas, Paint paint,
			float shrink) {
		canvas.drawRect(i * cellSize + GRID_SIZE + BOARD_MARGIN + shrink, j
				* cellSize + GRID_SIZE + BOARD_MARGIN + shrink, (i + 1)
				* cellSize - GRID_SIZE + BOARD_MARGIN - shrink, (j + 1)
				* cellSize + BOARD_MARGIN - GRID_SIZE - shrink, paint);
	}

	/*
	 * Draw fancy "disabled" and solid square. Same story here for Paint object
	 * as in drawWhiteSquare method.
	 */
	private void drawSolidSquare(Canvas canvas, int i, int j, State who) {
		if (who == State.BLOCK) {

			Paint paintBigger = new Paint();
			paintBigger.setColor(Color.rgb(184, 138, 0)); //light brown
			paintBigger.setStyle(Style.FILL);

			drawCustomRect(i - 1, j - 1, canvas, paintBigger, 0);

			Paint paintSmaller = new Paint();
			paintSmaller.setColor(Color.rgb(0, 107, 0)); //light green
			paintSmaller.setStyle(Style.FILL);

			float shrink = cellSize * 0.15f;

			drawCustomRect(i - 1, j - 1, canvas, paintSmaller, shrink);

			canvas.drawLine((i - 1) * cellSize + GRID_SIZE + BOARD_MARGIN,
					(j - 1) * cellSize + GRID_SIZE + BOARD_MARGIN, (i - 1)
							* cellSize + GRID_SIZE + BOARD_MARGIN + shrink,
					(j - 1) * cellSize + GRID_SIZE + BOARD_MARGIN + shrink,
					paintSmaller);

			canvas.drawLine(i * cellSize - GRID_SIZE + BOARD_MARGIN, (j - 1)
					* cellSize + GRID_SIZE + BOARD_MARGIN, i * cellSize
					- GRID_SIZE + BOARD_MARGIN - shrink, (j - 1) * cellSize
					+ GRID_SIZE + BOARD_MARGIN + shrink, paintSmaller);

			canvas.drawLine(i * cellSize - GRID_SIZE + BOARD_MARGIN, j
					* cellSize - GRID_SIZE + BOARD_MARGIN, i * cellSize
					- GRID_SIZE + BOARD_MARGIN - shrink, j * cellSize
					- GRID_SIZE + BOARD_MARGIN - shrink, paintSmaller);

			canvas.drawLine((i - 1) * cellSize + GRID_SIZE + BOARD_MARGIN, j
					* cellSize - GRID_SIZE + BOARD_MARGIN, (i - 1) * cellSize
					+ GRID_SIZE + BOARD_MARGIN + shrink, j * cellSize
					- GRID_SIZE + BOARD_MARGIN - shrink, paintSmaller);
		}

	}

	/*
	 * Draw custom balls. We can change balls alpha and radius in animation.
	 */
	private void drawBall(int i, int j, State who, float radius, Canvas canvas,
			int alpha) {

		// Calculate where we will put ball in our grid based on coordinates in
		// grid.
		float x = cellSize * (i - 1) + cellSize / 2 + BOARD_MARGIN;
		float y = cellSize * (j - 1) + cellSize / 2 + BOARD_MARGIN;
		// Skip empty every time.
		if (who != State.EMPTY && who != State.BLOCK) {
			Paint smallBall = new Paint();
			int color = Color.YELLOW;
			if (who == State.JAPANESE)
				color  = Color.YELLOW;
			else if(who == State.SELECTED)	
				color = Color.RED;
			else if (who == State.AMERICAN)
				color = Color.BLUE;
			smallBall.setColor(color);
			smallBall.setStyle(Style.FILL);
			smallBall.setAlpha(alpha);

			Paint bigBall = new Paint();
			bigBall.setColor(Color.GRAY);
			bigBall.setStyle(Style.FILL);
			bigBall.setAlpha(alpha);

			// Smaller ball is 15% smaller than bigger.
			canvas.drawCircle(x, y, radius * 1.15f, bigBall);

			canvas.drawCircle(x, y, radius, smallBall);
		}
	}


	
	
	/*
	 * Select ball action operation (ball become black).
	 */
	public void selectBall(int i, int j, State who, BataanMove[] piece) {
		animation = new PutBall();
		PutBall putBall = (PutBall) animation;
		putBall.alpha = 250;
		putBall.i = i;
		putBall.j = j;
		putBall.piece = piece;
		putBall.who = State.SELECTED;

		animationHandler.sendEmptyMessage(MSG_ANIMATE);
	}

	/*
	 * Create new ball operation (on empty square in grid).
	 */
	public void createBall(int i, int j, State who) {
		animation = new CreateBallAnimation();
		CreateBallAnimation createBallAnimation = (CreateBallAnimation) animation;
		createBallAnimation.radius = 250;
		createBallAnimation.i = i;
		createBallAnimation.j = j;
		createBallAnimation.who = who;

		animationHandler.sendEmptyMessage(MSG_ANIMATE);
	}

	/*
	 * Paint square in white block operation (along with alpha animation) when
	 * user perform illegal move.
	 */
	public void error(int i, int j) {
		animation = new FillSquareAnimation();
		FillSquareAnimation fillSquareAnimation = (FillSquareAnimation) animation;
		fillSquareAnimation.i = i;
		fillSquareAnimation.j = j;
		fillSquareAnimation.alpha = 255;

		animationHandler.sendEmptyMessage(MSG_ANIMATE);
	}

	/*
	 * Move ball from one place to another operation (with animation also).
	 */
	public void moveBall(int i1, int j1, int i2, int j2, State who) {
		animation = new MoveBallsAnimation();
		MoveBallsAnimation createBallAnimation = (MoveBallsAnimation) animation;
		createBallAnimation.radius = maxRadius;
		createBallAnimation.moveFrom[i1][j1] = true;
		createBallAnimation.moveTo[i2][j2] = true;
		createBallAnimation.whoFrom = who;
		createBallAnimation.whoTo = who;

		animationHandler.sendEmptyMessage(MSG_ANIMATE);
	}

	/*
	 * Change colors for all balls operation that have same coordinates as true
	 * values in "changeThem" matrix. Animation is same as for move operation.
	 */
	public void changeColors(boolean[][] changeThem, State whoFrom, State whoTo) {
		animation = new MoveBallsAnimation();
		MoveBallsAnimation createBallAnimation = (MoveBallsAnimation) animation;
		createBallAnimation.radius = maxRadius;
		createBallAnimation.moveFrom = changeThem;
		createBallAnimation.moveTo = changeThem;
		createBallAnimation.whoFrom = whoFrom;
		createBallAnimation.whoTo = whoTo;

		animationHandler.sendEmptyMessage(MSG_ANIMATE);
	}
	//source-JAPANESE
	public void highLightPieceSquare(BataanMove[] pieces, State owner){
        animation = new DrawHighligtedPieceAnimation();
        DrawHighligtedPieceAnimation highlightAnimation = (DrawHighligtedPieceAnimation) animation;
        highlightAnimation.coordinate = pieces;
        highlightAnimation.alpha = 250;
        highlightAnimation.owner = owner;

        animationHandler.sendEmptyMessage(MSG_ANIMATE);

    }
	//source-AMERICAN
	public void highLightPieceSquare1(BataanMove[] pieces, State owner){
        animation = new DrawHighligtedPieceAnimation1();
        DrawHighligtedPieceAnimation1 highlightAnimation = (DrawHighligtedPieceAnimation1) animation;
        highlightAnimation.coordinate = pieces;
        highlightAnimation.alpha = 250;
        highlightAnimation.owner = owner;
        animationHandler.sendEmptyMessage(MSG_ANIMATE);

    }


	 public void removePieces(BataanMove[] pieces, State who){
	        animation = new RemoveBallAnimation();
	        RemoveBallAnimation removeBallAnimation = (RemoveBallAnimation)animation;
	        removeBallAnimation.tobeRemove = pieces;
	        removeBallAnimation.radius = maxRadius;
	        removeBallAnimation.who = who;
	        animationHandler.sendEmptyMessage(MSG_ANIMATE);
	    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (animation.isFinish()) {
			int action = event.getAction();

			int i = (int) ((event.getX() - BOARD_MARGIN) / cellSize);
			int j = (int) ((event.getY() - BOARD_MARGIN) / cellSize);

			if (i >= 0 && i <= (BOARD_SIZE - 1) && j >= 0
					&& j <= (BOARD_SIZE - 1)) {

				// If user just click, then we will show painted square.
				if (action == MotionEvent.ACTION_DOWN) {
					moveStageListener.userClick(i, j);
					Log.d("Coordinates" + i + j, "Coordinates" );
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * Recalculate fields based on current screen size.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		boardWidth = w < h ? w : h;
		cellSize = (boardWidth - GRID_SIZE * BOARD_MARGIN) / BOARD_SIZE;

		maxRadius = cellSize * 0.68f / 2;
	}

	/*
	 * Set dimension of current view.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);
		int d = w == 0 ? h : h == 0 ? w : w < h ? w : h;
		setMeasuredDimension(d, d);
	}

	/**
	 * Inner animation handler. This handler call itself several times during
	 * animation and in every pass invalidates current view (calls onDraw method
	 * of View). It is controlled by Animation interface and hence concrete
	 * implementation of Animation interface. This implementation "tells" it
	 * when to stop.
	 */
	private class AnimationMessageHandler implements Callback {
		public boolean handleMessage(Message msg) {
			if (msg.what == MSG_ANIMATE) {
				BoardView.this.invalidate();
				if (!animationHandler.hasMessages(MSG_ANIMATE)) {
					if (animation.isFinish()) {
						animationHandler.removeMessages(MSG_ANIMATE);
						moveStageListener.animationComplete();
					} else {
						animationHandler.sendEmptyMessageDelayed(MSG_ANIMATE,
								animation.fps());
					}
				}
				return true;
			}
			return false;
		}
	}
	private class RemoveBallAnimation implements Animation {

        public BataanMove[] tobeRemove = null;
        public State who;
        public float radius;

        public void animate(Canvas canvas) {
            for(BataanMove c: tobeRemove)
                drawBall(c.source.x, c.source.y, who, radius, canvas, 255);
            radius -= 8;
            if (radius <= 0 )
                radius = 0;
        }

        public boolean isFinish() {
            return radius <= 0;
        }

        public boolean skip(int i, int j) {
            return false;
        }

        public int fps() {
            return 1000 / 500;
        }
    }


	/**
	 * This animation doesn't do anything - null animation.
	 */
	private class NullAnimation implements Animation {
		public void animate(Canvas canvas) {
			// do nothing
		}

		public boolean isFinish() {
			return true;
		}

		public boolean skip(int i, int j) {
			return false;
		}

		public int fps() {
			return 1000 / 1;
		}
	}

	/**
	 * Create ball animation (balls pops-up up in empty square).
	 */
	private class CreateBallAnimation implements Animation {

		public int i;
		public int j;
		public State who;
		public float radius;

		public void animate(Canvas canvas) {
			drawBall(i + 1, j + 1, who, radius, canvas, 255);
			radius += 8;
			if (radius >= BoardView.this.maxRadius)
				radius = BoardView.this.maxRadius;
		}

		public boolean isFinish() {
			return radius >= BoardView.this.maxRadius;
		}

		public boolean skip(int i, int j) {
			return (this.i == i && this.j == j);
		}

		public int fps() {
			return 1000 / 500;
		}
	}

	/**
	 * Move ball animation that moves current ball from one square to another
	 * altogether with pop-ing-up effect. :) It can be use for one ball or ball
	 * set (represented by coordinate matrix).
	 */
	private class MoveBallsAnimation implements Animation {
		public boolean[][] moveFrom = new boolean[BOARD_SIZE][BOARD_SIZE];
		public boolean[][] moveTo = new boolean[BOARD_SIZE][BOARD_SIZE];
		public State whoFrom;
		public State whoTo;
		public float radius;

		public boolean firstPahseFinish;
		public boolean secondPhaseFinish;

		public void animate(Canvas canvas) {
			if (!firstPahseFinish) {
				for (int i = 0; i < BOARD_SIZE; i++) {
					for (int j = 0; j < BOARD_SIZE; j++) {
						if (moveFrom[i][j])
							drawBall(i + 1, j + 1, whoFrom, radius, canvas, 255);
					}
				}

				radius -= 8;
				if (radius <= 0) {
					radius = 0;
					firstPahseFinish = true;
				}
			} else {

				for (int i = 0; i < BOARD_SIZE; i++) {
					for (int j = 0; j < BOARD_SIZE; j++) {
						if (moveTo[i][j])
							drawBall(i + 1, j + 1, whoTo, radius, canvas, 255);
					}
				}

				radius += 8;
				if (radius >= maxRadius) {
					radius = maxRadius;
					secondPhaseFinish = true;
				}
			}
		}

		public boolean isFinish() {
			return firstPahseFinish && secondPhaseFinish;
		}

		public boolean skip(int i, int j) {
			return moveFrom[i][j] || moveTo[i][j];
		}

		public int fps() {
			return 1000 / 500;
		}
	}

	/**
	 * Paint square with white gradually disappeared white inner square.
	 */
	private class FillSquareAnimation implements Animation {

		public int i;
		public int j;

		public int alpha;

		public void animate(Canvas canvas) {
			drawWhiteSquare(canvas, i, j, alpha);
			alpha -= 75;
			if (alpha <= 0)
				alpha = 0;
		}

		public boolean isFinish() {
			return alpha <= 0;
		}

		public boolean skip(int i, int j) {
			return false;
		}

		public int fps() {
			return 1000 / 500;
		}
	}
	
	/**
	 * And last but not the least animation that gradually change ball
     * color.		
	 */
	private class PutBall implements Animation {

		public int i;
		public int j;
		public State who;
		public int alpha;
		public BataanMove[] piece= null;

		public void animate(Canvas canvas) {
			drawLGSquare(canvas, i, j, alpha);
			drawBall(i + 1, j + 1, who, maxRadius, canvas, alpha);
			for(BataanMove c: piece){
				if(c.source.x == i && c.source.y == j){
					drawGSquare(canvas, c.dest.x, c.dest.y, alpha);
				}
			}
				
			//drawBall(i + 1, j + 1, who, maxRadius, canvas, alpha);
			alpha += 100;
			if (alpha >= 255)
				alpha = 255;
		}

		public boolean isFinish() {
			return alpha >= 255;
		}

		public boolean skip(int i, int j) {
			return (this.i == i && this.j == j);
		}

		public int fps() {
			return 1000 / 500;
		}
	}
	public class DrawHighligtedPieceAnimation implements Animation {

        public BataanMove[] coordinate = null;
        public int alpha;
        public State owner;

        public void animate(Canvas canvas) {
            for(BataanMove c: coordinate) {
                drawJSquare(canvas, c.source.x, c.source.y, alpha);
                drawBall(c.source.x+1, c.source.y+1, owner, maxRadius, canvas, 255);       
            }

            alpha += 8;
            if(alpha >= 255){
                alpha = 255;
            }
        }

        public boolean isFinish() {
            return alpha >= 255;
        }

        public boolean skip(int i, int j) {
            return false;
        }

        public int fps() {
            return 0;
        }
    }
	
	public class DrawHighligtedPieceAnimation1 implements Animation {

        public BataanMove[] coordinate = null;
        public int alpha;
        public State owner;

        public void animate(Canvas canvas) {
            for(BataanMove c: coordinate) {
                drawASquare(canvas, c.source.x, c.source.y, alpha);
                drawBall(c.source.x+1, c.source.y+1, owner, maxRadius, canvas, 255);       
            }

            alpha += 8;
            if(alpha >= 255){
                alpha = 255;
            }
        }

        public boolean isFinish() {
            return alpha >= 255;
        }

        public boolean skip(int i, int j) {
            return false;
        }

        public int fps() {
            return 0;
        }
    }


	
	
}