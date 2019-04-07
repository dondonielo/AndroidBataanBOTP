package com.example.newbataan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;





import android.util.Log;

public class GameFlow implements Serializable{
	public static State[][] positions;

	public static boolean ifPrevMoveIsJump;

	private BataanMove move = new BataanMove();

	public BataanMove getMove() {
		return move;
	}
	public void setMove(BataanMove move) {
        this.move = move;
    }

	public void deleteAll() {
		move.deleteAll();
	}

	public void deleteMove() {
		move.deleteMove();
	}

	public void piecedoMove(int cRow, int cCol, int nRow, int nCol,
			State player, State[][] pos) {

		if (player == State.JAPANESE
				|| (cCol - nCol != 2 && cCol - nCol != -2 && cRow - nRow != 2 && cRow
						- nRow != -2)) {
			pos[nRow][nCol] = pos[cRow][cCol];

		} else { // if the current player is an American, check if it can jump
					// in any direction.
			int jRow, jCol;
			if (((nRow - cRow == 2) && (cCol == nCol))) {
				jCol = cCol;
				jRow = nRow - 1;
			} else if (((nRow - cRow == -2) && (cCol == nCol))) {
				jCol = cCol;
				jRow = nRow + 1;
			} else if ((nCol - cCol == 2) && (cRow == nRow)) {
				jCol = nCol - 1;
				jRow = cRow;
			} else if ((nCol - cCol == -2) && (cRow == nRow)) {
				jCol = nCol + 1;
				jRow = cRow;
			} else {
				jCol = (nCol + cCol) / 2;
				jRow = (nRow + cRow) / 2;
			}
			pos[nRow][nCol] = State.AMERICAN;
			pos[jRow][jCol] = State.EMPTY;

		}
		// change
		pos[cRow][cCol] = State.EMPTY;
	}


	public static void setGame(State[][] player) {
		for (int r = 0; r <= 8; r++) {
			for (int c = 0; c <= 8; c++) {
				if (((r == 0 || r == 1 || r == 7 || r == 8) && c == 7)
						|| ((r == 0 || r == 1 || r == 8 || r == 7) && c == 8)
						|| ((r == 0 || r == 8) && c == 0)
						|| ((r == 0 || r == 1 || r == 8 || r == 7) && c == 1)) {
					player[r][c] = State.BLOCK;
				} else if (c == 0 || c == 1 || (c == 2 && (r == 3 || r == 5))) {
					player[r][c] = State.EMPTY;
				} else if (c == 2 && r == 2 || c == 2 && r == 4 || c == 2
						&& r == 6) {
					player[r][c] = State.AMERICAN;
				} else {
					player[r][c] = State.JAPANESE;
				}
			}
		}
	}

	public boolean pieceCanJump(State[][] pos, State player, int r1, int c1,
			int r2, int c2, int r3, int c3) {
		// only the American piece can do a jump.
		if (player == State.AMERICAN && pos[r1][c1] == State.AMERICAN) {
			// (r3,c3) is out of the board.
			if (r3 < 0 || r3 > 8 || c3 < 0 || c3 > 8
					|| pos[r3][c3] == State.BLOCK) {
				return false;
			} // (r3,c3) contains a piece.
			else if (pos[r3][c3] != State.EMPTY) {
				return false;
			} // there is no Japanese piece to jump on.
			else if (pos[r2][c2] != State.JAPANESE) {
				return false;
			} else {
				return true; // jump is valid.
			}

		} else {
			return false;
		}

	}

	public boolean pieceCanMove(State[][] pos, State player, int r1, int c1,
			int r2, int c2) {
		// (r2,c2) is out of the board.
		if (r2 < 0 || r2 > 8 || c2 < 0 || c2 > 8 || pos[r2][c2] == State.BLOCK) {
			return false;
		}
		// (r2,c2) already contains a piece.
		if (pos[r2][c2] != State.EMPTY) {
			return false;
		}
		// Japanese can only move downwards or sidewards.
		// if (player == Japanese) {
		if (pos[r1][c1] == player) {
			return true;
		} else {
			return false;
		}

	}

	public BataanMove[] getValidMoves(State[][] pos, State player) {
		// player is not an American or a Japanese.
		if (player != State.AMERICAN && player != State.JAPANESE) {
			return null;
		}

		// array list to store the valid moves.
		ArrayList<BataanMove> moves = new ArrayList<BataanMove>();

		/*
		 * After checking if there are available jumps and the array list is
		 * still empty, the second check is if there are valid normal move for
		 * the player,still every square of the board is checked for all the
		 * valid moves in the players piece.
		 */
		
		for (int row = 0; row <= 8; row++) {
			for (int col = 0; col <= 8; col++) {
				if (pos[row][col] == player) {
					if ((row % 2 == 0 && col % 2 == 0)
							|| (row % 2 != 0 && col % 2 != 0)) {

						if (pieceCanJump(pos, player, row, col, row + 1,
								col + 1, row + 2, col + 2)) {
							moves.add(new BataanMove(row, col, row + 2, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row - 1,
								col + 1, row - 2, col + 2)) {
							moves.add(new BataanMove(row, col, row - 2, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row + 1,
								col - 1, row + 2, col - 2)) {
							moves.add(new BataanMove(row, col, row + 2, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row - 1,
								col - 1, row - 2, col - 2)) {
							moves.add(new BataanMove(row, col, row - 2, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row, col - 1,
								row, col - 2)) {
							moves.add(new BataanMove(row, col, row, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row, col + 1,
								row, col + 2)) {
							moves.add(new BataanMove(row, col, row, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row - 1, col,
								row - 2, col)) {
							moves.add(new BataanMove(row, col, row - 2, col));
						}
						if (pieceCanJump(pos, player, row, col, row + 1, col,
								row + 2, col)) {
							moves.add(new BataanMove(row, col, row + 2, col));
						}

					} else {
						if (pieceCanJump(pos, player, row, col, row - 1, col,
								row - 2, col)) {
							moves.add(new BataanMove(row, col, row - 2, col));
						}
						if (pieceCanJump(pos, player, row, col, row, col - 1,
								row, col - 2)) {
							moves.add(new BataanMove(row, col, row, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row, col + 1,
								row, col + 2)) {
							moves.add(new BataanMove(row, col, row, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row + 1, col,
								row + 2, col)) {
							moves.add(new BataanMove(row, col, row + 2, col));
						}
					}
				}
			}
		}

		for (int row = 0; row <= 8; row++) {
			for (int col = 0; col <= 8; col++) {
				if (pos[row][col] == player) {
					if ((row % 2 == 0 && col % 2 == 0)
							|| (row % 2 != 0 && col % 2 != 0)) {

						if (pieceCanMove(pos, player, row, col, row + 1, col)) {
							moves.add(new BataanMove(row, col, row + 1, col));
						}
						if (pieceCanMove(pos, player, row, col, row - 1, col)) {
							moves.add(new BataanMove(row, col, row - 1, col));
						}
						if (pieceCanMove(pos, player, row, col, row + 1,
								col + 1)) {
							moves.add(new BataanMove(row, col, row + 1, col + 1));
						}
						if (pieceCanMove(pos, player, row, col, row + 1,
								col - 1)) {
							moves.add(new BataanMove(row, col, row + 1, col - 1));
						}
						if (pieceCanMove(pos, player, row, col, row, col + 1)) {
							moves.add(new BataanMove(row, col, row, col + 1));
						}
						if (pieceCanMove(pos, player, row, col, row, col - 1)) {
							moves.add(new BataanMove(row, col, row, col - 1));
						}
						if (pieceCanMove(pos, player, row, col, row - 1,
								col + 1)) {
							moves.add(new BataanMove(row, col, row - 1, col + 1));
						}
						if (pieceCanMove(pos, player, row, col, row - 1,
								col - 1)) {
							moves.add(new BataanMove(row, col, row - 1, col - 1));
						}

					} else if (((row == 2 || row == 6) && col == 1)) {
						if (row == 6) {
							if (pieceCanMove(pos, player, row, col, row - 1,
									col)) {
								moves.add(new BataanMove(row, col, row - 1, col));
							}
							if (pieceCanMove(pos, player, row, col, row,
									col - 1)) {
								moves.add(new BataanMove(row, col, row, col - 1));
							}
							if (pieceCanMove(pos, player, row, col, row,
									col + 1)) {
								moves.add(new BataanMove(row, col, row, col + 1));
							}
							if (pieceCanMove(pos, player, row, col, row + 1,
									col - 1)) {
								moves.add(new BataanMove(row, col, row + 1,
										col - 1));
							}
						} else {
							if (pieceCanMove(pos, player, row, col, row + 1,
									col)) {
								moves.add(new BataanMove(row, col, row + 1, col));
							}
							if (pieceCanMove(pos, player, row, col, row,
									col - 1)) {
								moves.add(new BataanMove(row, col, row, col - 1));
							}
							if (pieceCanMove(pos, player, row, col, row,
									col + 1)) {
								moves.add(new BataanMove(row, col, row, col + 1));
							}
							if (pieceCanMove(pos, player, row, col, row - 1,
									col - 1)) {
								moves.add(new BataanMove(row, col, row - 1,
										col - 1));
							}
						}
					} else {
						if (pieceCanMove(pos, player, row, col, row - 1, col)) {
							moves.add(new BataanMove(row, col, row - 1, col));
						}
						if (pieceCanMove(pos, player, row, col, row, col - 1)) {
							moves.add(new BataanMove(row, col, row, col - 1));
						}
						if (pieceCanMove(pos, player, row, col, row, col + 1)) {
							moves.add(new BataanMove(row, col, row, col + 1));
						}
						if (pieceCanMove(pos, player, row, col, row + 1, col)) {
							moves.add(new BataanMove(row, col, row + 1, col));
						}

					}

				}
			}
		}

		/*
		 * If still no legal moves are found return null else copy the legal
		 * moves into another array list and return it.
		 */
		if (moves.isEmpty()) {
			return null;
		} else {
			BataanMove[] movesArray = new BataanMove[moves.size()];
			for (int i = 0; i < moves.size(); i++) {
				movesArray[i] = moves.get(i);
			}
			return movesArray;
		}
	}

	public BataanMove[] getValidJumps(State[][] pos, State player) {
		if (player != State.AMERICAN) {
			return null;
		}

		ArrayList<BataanMove> moves = new ArrayList<BataanMove>();

		for (int row = 0; row <= 8; row++) {
			for (int col = 0; col <= 8; col++) {
				if (pos[row][col] == player) {
					if ((row % 2 == 0 && col % 2 == 0)
							|| (row % 2 != 0 && col % 2 != 0)) {

						if (pieceCanJump(pos, player, row, col, row + 1,
								col + 1, row + 2, col + 2)) {
							moves.add(new BataanMove(row, col, row + 2, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row - 1,
								col + 1, row - 2, col + 2)) {
							moves.add(new BataanMove(row, col, row - 2, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row + 1,
								col - 1, row + 2, col - 2)) {
							moves.add(new BataanMove(row, col, row + 2, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row - 1,
								col - 1, row - 2, col - 2)) {
							moves.add(new BataanMove(row, col, row - 2, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row, col - 1,
								row, col - 2)) {
							moves.add(new BataanMove(row, col, row, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row, col + 1,
								row, col + 2)) {
							moves.add(new BataanMove(row, col, row, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row - 1, col,
								row - 2, col)) {
							moves.add(new BataanMove(row, col, row - 2, col));
						}
						if (pieceCanJump(pos, player, row, col, row + 1, col,
								row + 2, col)) {
							moves.add(new BataanMove(row, col, row + 2, col));
						}

					} else {
						if (pieceCanJump(pos, player, row, col, row - 1, col,
								row - 2, col)) {
							moves.add(new BataanMove(row, col, row - 2, col));
						}
						if (pieceCanJump(pos, player, row, col, row, col - 1,
								row, col - 2)) {
							moves.add(new BataanMove(row, col, row, col - 2));
						}
						if (pieceCanJump(pos, player, row, col, row, col + 1,
								row, col + 2)) {
							moves.add(new BataanMove(row, col, row, col + 2));
						}
						if (pieceCanJump(pos, player, row, col, row + 1, col,
								row + 2, col)) {
							moves.add(new BataanMove(row, col, row + 2, col));
						}
					}
				}
			}
		}

		if (moves.isEmpty()) {
			return null;
		} else {
			BataanMove[] movesArray = new BataanMove[moves.size()];
			for (int i = 0; i < moves.size(); i++) {
				movesArray[i] = moves.get(i);
			}
			return movesArray;
		}
	}

	public BataanMove[] pieceMoreJumps(State[][] pos, State player, int row,
			int col) {
		if (player != State.AMERICAN) {
			return null;
		}

		ArrayList<BataanMove> moves = new ArrayList<BataanMove>();

		if (pos[row][col] == player) {
			if ((row % 2 == 0 && col % 2 == 0)
					|| (row % 2 != 0 && col % 2 != 0)) {

				if (pieceCanJump(pos, player, row, col, row + 1, col + 1,
						row + 2, col + 2)) {
					moves.add(new BataanMove(row, col, row + 2, col + 2));
				}
				if (pieceCanJump(pos, player, row, col, row - 1, col + 1,
						row - 2, col + 2)) {
					moves.add(new BataanMove(row, col, row - 2, col + 2));
				}
				if (pieceCanJump(pos, player, row, col, row + 1, col - 1,
						row + 2, col - 2)) {
					moves.add(new BataanMove(row, col, row + 2, col - 2));
				}
				if (pieceCanJump(pos, player, row, col, row - 1, col - 1,
						row - 2, col - 2)) {
					moves.add(new BataanMove(row, col, row - 2, col - 2));
				}
				if (pieceCanJump(pos, player, row, col, row, col - 1, row,
						col - 2)) {
					moves.add(new BataanMove(row, col, row, col - 2));
				}
				if (pieceCanJump(pos, player, row, col, row, col + 1, row,
						col + 2)) {
					moves.add(new BataanMove(row, col, row, col + 2));
				}
				if (pieceCanJump(pos, player, row, col, row - 1, col, row - 2,
						col)) {
					moves.add(new BataanMove(row, col, row - 2, col));
				}
				if (pieceCanJump(pos, player, row, col, row + 1, col, row + 2,
						col)) {
					moves.add(new BataanMove(row, col, row + 2, col));
				}

			} else {
				if (pieceCanJump(pos, player, row, col, row - 1, col, row - 2,
						col)) {
					moves.add(new BataanMove(row, col, row - 2, col));
				}
				if (pieceCanJump(pos, player, row, col, row, col - 1, row,
						col - 2)) {
					moves.add(new BataanMove(row, col, row, col - 2));
				}
				if (pieceCanJump(pos, player, row, col, row, col + 1, row,
						col + 2)) {
					moves.add(new BataanMove(row, col, row, col + 2));
				}
				if (pieceCanJump(pos, player, row, col, row + 1, col, row + 2,
						col)) {
					moves.add(new BataanMove(row, col, row + 2, col));
				}
			}
		}

		if (moves.isEmpty()) {
			return null;
		} else {
			BataanMove[] movesArray = new BataanMove[moves.size()];
			for (int i = 0; i < moves.size(); i++) {
				movesArray[i] = moves.get(i);
			}
			return movesArray;
		}
	}

	public static int countPiece(State[][] pos, State player) {
		int counter = 0;
		for (int i = 0; i < pos.length; i++) {
			for (int j = 0; j < pos.length; j++) {
				if (pos[i][j] == player)
					counter++;
			}
		}
		return counter;
	}

}