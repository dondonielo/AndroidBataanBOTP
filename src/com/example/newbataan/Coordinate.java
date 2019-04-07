package com.example.newbataan;

import java.io.Serializable;

public class Coordinate implements Serializable{


	public int x = -1;
	public int y = -1;
	
	public Coordinate() {}
			
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void delete() {
		x = -1;
		y = -1;
	}
	
	public boolean isNotEmpty() {
		return x != -1 && y != -1;
	}
	
	public boolean isEmpty() {
		return x == -1 || y == -1;
	}
	

}