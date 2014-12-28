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
			Move m = Move.parse(br.readLine());
			if(!g.isValid(m))
				System.out.println("Invalid move");
			else
				isEnded = g.move(m);
		}
		
	}
	boolean whiteToMove = true;
	Stack<Piece[][]> history;
	public Game(){
		history = new Stack<Piece[][]>();
		history.push(new Piece[][]{
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
		});
	}
	
	public Piece[][] board(){
		return history.peek();
	}

	public String printBoard() {
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < 4; k++)
		sb.append((char)('a' + k));
		sb.append('\n');
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				if (board()[i][j] != null) sb.append(board()[i][j].toString());
				else sb.append('.');
			}
			sb.append(' ' + Integer.toString(i) + "\n");
		}
		return sb.toString();
	}
	
	private Piece[][] cloneBoard(){
		return history.peek();
	}

	public boolean move(Move move) {
		Piece[][] oldBoard = history.peek();
		Piece[][] newBoard = new Piece[16][4];
		Piece dest = oldBoard[move.endRow][move.endCol];
		boolean res = dest == null ? false : dest.type.equals(Piece.Type.KING);
		for (int i = 0; i < 16; i++)
			newBoard[i] = Arrays.copyOf(oldBoard[i], oldBoard[i].length);
		newBoard[move.endRow][move.endCol] = newBoard[move.startRow][move.startCol];
		newBoard[move.startRow][move.startCol] = null;
		history.push(newBoard);
		whiteToMove = !whiteToMove;
		return res;
	}
	
	private Move bestMove(){
		HashMap<Double, Move> moves = new HashMap<Double, Move>();
		double score = alphabeta(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 5, moves);
		return moves.get(score);
	}
	
double alphaBeta( double alpha, double beta, int depthleft, Map<Double, Move> moves) {
	if(depthleft == 0) return quiesce( alpha, beta );
	for (Move m : allValidMoves()) {
		move(m);
		score = -alphaBeta( -beta, -alpha, depthleft - 1, null);
		if(moves != null)
			moves.put(score,m);
		history.pop();
		if( score >= beta )
			return beta;   //  fail hard beta-cutoff
		if( score > alpha )
			alpha = score; // alpha acts like max in MiniMax
	}
	return alpha;
}

double quiesce(double alpha, double beta) {
    double stand_pat = evaluate();
    if( stand_pat >= beta )
        return beta;
    if( alpha < stand_pat )
        alpha = stand_pat;
 
    for(Move m : allValidMoves())  {
    	if(board()[m.endRow][m.endCol] != null){
	        move(m);
	        score = -quiesce(-beta, -alpha);
	        history.pop();
	 
	        if( score >= beta )
	            return beta;
	        if( score > alpha )
	           alpha = score;
    	}
    }
    return alpha;
}
	
	private double evaluate(){
		double value = 0.0;
		Piece[][] b = history.peek();
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				if(b[i][j] != null)
					value += b[i][j].value();
		return value;
	}

	public boolean isValid(Move m) {
		return validMoves(m.startRow, m.startCol, new HashSet<Move>()).contains(m);
	}
	
	private Set<Move> allValidMoves(){
		Set<Move> moves = new HashSet<Move>();
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				validMoves(i,j,moves);
		return moves;
	}
	private Set<Move> validMoves(int row, int col, Set<Move> validMoves) {
		Piece a = board()[row][col];
		if(a != null && a.white == whiteToMove) {
			if(a.type.equals(Piece.Type.KNIGHT)) {
				for(int i=0; i<8; i++){
					int x = (((i+1)%4)/2+1) * ((i/8)*-2+1);
					int y = (((i-1)%4)/2+1) * (((i-2)/8)*-2+1);
					int r = (row + x) % 16;
					int c = col + y;
					if(c >= 0 && c < 4
						&& (board()[r][c] == null || board()[r][c].white != a.white)
						validMoves.add(new Move(row, col, r, c));
				}
			} else {
				int range;
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
						if(i != j && j != 0
							&& (!a.type.equals(Piece.Type.BISHOP) || (i+j)%2==0)
							&& (!a.type.equal(Piece.Type.ROOK) || (i+j)%2==1) {
							range = 16;
							if(a.type.equals(Piece.Type.KING))
								range = 1;
							for (int k = 0; k < 16; k++) {
								int r = (row + i * k) % 16;
								int c = col + j * k;
								if (c < 0 || c >= 4) break;
									Piece b = board()[r][c];
								if (b != null && b.white == a.white) break;
									validMoves.add(new Move(row, col, r, c));
								if (b != null) break;
							}
						}
			}
		}
		return validMoves;
	}
}

class Piece {
	public enum Type {
		KING('k', 1000),
		QUEEN('q', 9),
		ROOK('r', 5),
		KNIGHT('n', 3),
		BISHOP('b', 3),
		PAWN('p', 1);
		char type;
		public double value;
		Type(char c, double v) {
			type = c;
			value = v;
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
	public double value(){
		return white ? type.value() : -type.value();
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
