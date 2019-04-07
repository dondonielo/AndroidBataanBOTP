package com.example.newbataan;

import java.io.Serializable;



public class BataanMove implements Serializable{

	   // public static boolean ifPrevMoveIsJump;
	    public Coordinate source = new Coordinate();
	    public Coordinate dest = new Coordinate();
	    public State player = State.EMPTY;

	    public BataanMove(){
	        //source.x = -1;
	        //source.y = -1;
	        //dest.x = -1;
	       // dest.y = -1;
	    }

	    /*public int cRow = source.x,
	    	cCol = source.y,
	    	nRow = dest.x,
	    	nCol = dest.y;*/
	    
	    public BataanMove(int r1, int c1, int r2, int c2) {
	    	source.x = r1;
	        source.y = c1;
	        dest.x = r2;
	        dest.y = c2;
	        player = State.EMPTY;
	    }
	   

	    public void deleteAll() {
	        this.source.delete();
	        this.dest.delete();
	        this.player = State.EMPTY;
	    }

	    public void deleteMove() {
	        this.source.delete();
	        this.dest.delete();
	    }

	    public static boolean isNotEmpty() {
	        return false;
	    }
	}