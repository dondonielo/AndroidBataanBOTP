package com.example.newbataan;



public enum State {

	// empty cell
	EMPTY(-1),
	// solid block that cannot be moved
	BLOCK(-2),
	// japanese player
	JAPANESE(-3),
	// american player
	AMERICAN(-4),
	// selected cell
	SELECTED(-5);

	private int stateValue;

	private State(int value) {
		stateValue = value;
	}

	public int getValue() {
		return stateValue;
	}

	public static State fromInt(int i) {
		for (State s : values()) {
			if (s.getValue() == i) {
				return s;
			}
		}
		return EMPTY;
	}
}