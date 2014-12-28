/* package whatever; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class Game {
	public static void main(String args[]) throws IOException {
		Game g = new Game();
		BufferedReader br = new BufferedReader(new InputStreamReader(System. in ));
		boolean isEnded = false;
		while (!isEnded) {
			System.out.print(g.printBoard());
			isEnded = g.move(Move.parse(br.readLine()));
		}
	}
	Piece[][] board = {
		{
			new Piece(Piece.Type.KING, true), new Piece(Piece.Type.BISHOP, true), new Piece(Piece.Type.KNIGHT, true), new Piece(Piece.Type.ROOK, true)
		}, {
			new Piece(Piece.Type.PAWN, true), new Piece(Piece.Type.PAWN, true), new Piece(Piece.Type.PAWN, true), new Piece(Piece.Type.PAWN, true)
		}, {
			null, null, null, null
		}, {
			null, null, null, null
		}, {
			null, null, null, null
		}, {
			null, null, null, null
		}, {
			new Piece(Piece.Type.PAWN, false), new Piece(Piece.Type.PAWN, false), new Piece(Piece.Type.PAWN, false), new Piece(Piece.Type.PAWN, false)
		}, {
			new Piece(Piece.Type.KING, false), new Piece(Piece.Type.BISHOP, false), new Piece(Piece.Type.KNIGHT, false), new Piece(Piece.Type.ROOK, false)
		}, {
			new Piece(Piece.Type.QUEEN, false), new Piece(Piece.Type.BISHOP, false), new Piece(Piece.Type.KNIGHT, false), new Piece(Piece.Type.ROOK, false)
		}, {
			new Piece(Piece.Type.PAWN, false), new Piece(Piece.Type.PAWN, false), new Piece(Piece.Type.PAWN, false), new Piece(Piece.Type.PAWN, false)
		}, {
			null, null, null, null
		}, {
			null, null, null, null
		}, {
			null, null, null, null
		}, {
			null, null, null, null
		}, {
			new Piece(Piece.Type.PAWN, true), new Piece(Piece.Type.PAWN, true), new Piece(Piece.Type.PAWN, true), new Piece(Piece.Type.PAWN, true)
		}, {
			new Piece(Piece.Type.QUEEN, true), new Piece(Piece.Type.BISHOP, true), new Piece(Piece.Type.KNIGHT, true), new Piece(Piece.Type.ROOK, true)
		}
	};

	public String printBoard() {
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < 4; k++)
		sb.append((char)('a' + k));
		sb.append('\n');
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] != null) sb.append(board[i][j].toString());
				else sb.append('.');
			}
			sb.append(' ' + Integer.toString(i) + "\n");
		}
		return sb.toString();
	}

	public boolean move(Move move) {
		Piece dest = board[move.endRow][move.endCol];
		boolean res = dest == null ? false : dest.type.equals(Piece.Type.KING);
		board[move.endRow][move.endCol] = board[move.startRow][move.startCol];
		board[move.startRow][move.startCol] = null;
		return res;
	}

	public boolean isValid(Move m) {
		return validMoves(m.startRow, m.startCol).contains(m);
	}
	public Set < Move > validMoves(int row, int col) {
		Set < Move > validMoves = new HashSet < Move > ();
		Piece a = board[row][col];
		boolean[][] dirs = {
			{
				true, true, true
			}, {
				true, false, true
			}, {
				true, true, true
			}
		};
		for (int i = 0; i < 3; i++)
		for (int j = 0; j < 3; j++)
		if (dirs[i][j]) for (int k = 0; k < 16; k++) {
			int r = (row + (i - 1) * k) % 16;
			int c = col + (j - 1) * k;
			if (c < 0 || c >= 4) break;
			Piece b = board[r][c];
			if (b != null && b.white == a.white) break;
			validMoves.add(new Move(row, col, r, c));
			if (b != null) break;
		}
		return validMoves;
	}
}

class Piece {
	public enum Type {
		KING('k'),
		QUEEN('q'),
		ROOK('r'),
		KNIGHT('n'),
		BISHOP('b'),
		PAWN('p');
		char type;
		Type(char c) {
			type = c;
		}
		public String toString() {
			return String.valueOf(type);
		}
		public boolean equals(Type t) {
			return type == t.type;
		}
	}

	Type type;
	boolean white;
	public Piece(Type type, boolean white) {
		this.type = type;
		this.white = white;
	}
	public String toString() {
		String str = type.toString();
		if (white) str = str.toUpperCase();
		return str;
	}

}

class Move {
	int startRow, startCol, endRow, endCol;
	public static Move parse(String move) {
		String[] parts = move.split("-");
		return new Move(
		parts[0].charAt(0) - 'a',
		Integer.parseInt(parts[0].substring(1)),
		parts[1].charAt(0) - 'a',
		Integer.parseInt(parts[1].substring(1)));
	}

	public Move(int startRow, int startCol, int endRow, int endCol) {
		this.startRow = startRow;
		this.startCol = startCol;
		this.endRow = endRow;
		this.endCol = endCol;
	}

	public boolean equals(Move m) {
		return startRow == m.startRow && startCol == m.startCol && endRow == m.endRow && endCol == m.endCol;
	}
}
