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
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		boolean isEnded = false;
		Move m;
		while (!isEnded) {
			System.out.print(g.printBoard());
			if((g.whiteToMove && g.whiteHuman) || (!g.whiteToMove && g.blackHuman))
			{
				m = Move.parse(br.readLine());
				g.log(m.toString());
				if(!g.isValid(m)){
					System.out.println("Invalid move");
					continue;
				}
			}
			else
				m = g.bestMove();
			isEnded = g.move(m);
		}
	}
	
	boolean whiteHuman = true, blackHuman = false;
	Stack<Move> history;
	Piece[][] board;
	boolean whiteToMove = true;
	int halfMoves;
	int moves;
	public Game(){
		history = new Stack<Move>();
		board = new Piece[][]{
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
	}
	
	public String toFEN(){
		int nulls = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				if (board[i][j] != null){
					if(nulls > 0)
						sb.append(Integer.toString(nulls));
					sb.append(board[i][j].toString());
				}
				else
					nulls++;
			}
			nulls=0;
			if(i<15)
				sb.append("/");
		}
		if(whiteToMove)
			sb.append(" w");
		else
			sb.append(" b");
		sb.append(" - - ");
		sb.append(halfMoves);
		sb.append(" ");
		sb.append(moves);
		return sb.toString();
	}

	public String printBoard() {
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < 4; k++)
		sb.append((char)('a' + k));
		sb.append('\n');
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				if (board[i][j] != null) sb.append(board[i][j].toString());
				else sb.append('.');
			}
			sb.append(' ' + Integer.toString(i+1) + "\n");
		}
		return sb.toString();
	}
	public boolean move(Move move) {
		history.push(move);
		Piece dest = board[move.endRow][move.endCol];
		Piece src = board[move.startRow][move.startCol];
		move.captures = dest;
		move.halfMoves = halfMoves;
		boolean res = dest == null ? false : dest.type.equals(Piece.Type.KING);
		board[move.endRow][move.endCol] = board[move.startRow][move.startCol];
		if(move.promotion != null)
			board[move.endRow][move.endCol].type = move.promotion;
		board[move.startRow][move.startCol] = null;
		if(dest != null || src.type.equals(Piece.Type.PAWN))
			halfMoves = 0;
		else
			halfMoves++;
		res = res || halfMoves == 50;
		if(!whiteToMove)
			moves++;
		whiteToMove = !whiteToMove;
		return res;
	}
	
	public void unmove(){
		Move move = history.pop();
		board[move.startRow][move.startCol] = board[move.endRow][move.endCol];
		if(move.captures != null)
			board[move.endRow][move.endCol] = move.captures;
		else
			board[move.endRow][move.endCol] = null;
		if(move.promotion != null)
			board[move.startRow][move.startCol].type = Piece.Type.PAWN;
		halfMoves = move.halfMoves;
		if(whiteToMove)
			moves--;
		whiteToMove = !whiteToMove;
	}
	
	private Move bestMove(){
		Move best = null;
		double max = Double.NEGATIVE_INFINITY;
		for(Move m : allValidMoves()){
			move(m);
			double score = -negaMax(2);
			unmove();
			if(score > max){
				max = score;
				best = m;
			}
		}
		log(max + " " + best.toString());
		return best;
	}
	
	double negaMax(int depth){
		if(depth == 0)
			return evaluate();
		double max = Double.NEGATIVE_INFINITY;
		for(Move m : allValidMoves()){
			//log(depth + ":" + m.toString());
			move(m);
			double score = -negaMax(depth-1);
			//log("pre: " +printBoard()());
			unmove();
			//log("post:"+printBoard()());
			if(score > max)
				max = score;
		}
		return max;
	}
	
	
	
double alphaBeta( double alpha, double beta, int depthleft, Map<Double, Move> moves) {
	if(depthleft == 0) return quiesce( alpha, beta );
	for (Move m : allValidMoves()) {
		move(m);
		double score = -alphaBeta( -beta, -alpha, depthleft - 1, null);
		if(moves != null)
			moves.put(score,m);
		unmove();
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
    	if(board[m.endRow][m.endCol] != null){
	        move(m);
	        double score = -quiesce(-beta, -alpha);
	        unmove();
	 
	        if( score >= beta )
	            return beta;
	        if( score > alpha )
	           alpha = score;
    	}
    }
    return alpha;
}
	
	public double evaluate(){
		double value = 0.0;
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				if(board[i][j] != null)
					value += board[i][j].value();
		if(!whiteToMove)
			return -value;
		else
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
	
	private void validPawnMoves(int startRow, int startCol, int endRow, int endCol, Set<Move> moves){
		if((whiteToMove && (endRow == 7 || endRow == 8))
			|| (!whiteToMove && (endRow == 0 || endRow == 15))){
			moves.add(new Move(startRow, startCol, endRow, endCol, Piece.Type.QUEEN));
			moves.add(new Move(startRow, startCol, endRow, endCol, Piece.Type.ROOK));
			moves.add(new Move(startRow, startCol, endRow, endCol, Piece.Type.KNIGHT));
			moves.add(new Move(startRow, startCol, endRow, endCol, Piece.Type.BISHOP));
		}
		else
			moves.add(new Move(startRow, startCol, endRow, endCol));
	}
	
	private void log(String str){
		System.out.println(str);
	}
	private Set<Move> validMoves(int row, int col, Set<Move> validMoves) {
		Piece a = board[row][col];
		if(a != null && a.white == whiteToMove) {
			if(a.type.equals(Piece.Type.PAWN)){
				int r = (row/8) * 2 - 1;
				if(a.white)
					r*=-1;
				if(board[row + r][col] == null)
					validPawnMoves(row, col, row + r, col, validMoves);
				if(col > 0 && board[row+r][col-1] != null && board[row+r][col-1].white != a.white)
					validPawnMoves(row, col, row+r, col-1, validMoves);
				if(col < 3 && board[row+r][col+1] != null && board[row+r][col+1].white != a.white)
					validPawnMoves(row, col, row+r, col+1, validMoves);
				if(((a.white && (row == 1 || row == 14))
					|| (!a.white && (row == 6 || row == 9)))
					&&(board[row + 2*r][col] == null))
						validMoves.add(new Move(row, col, row + 2*r, col));
						
			}
			if(a.type.equals(Piece.Type.KNIGHT)) {
				for(int i=0; i<8; i++){
					int x = (((i+1)%4)/2+1) * ((i/4)*-2+1);
					int y = (((i+7)%4)/2+1) * ((((i+6)%8)/4)*-2+1);
					int r = (row + x + 16) % 16;
					int c = col + y;
					if(c >= 0 && c < 4
						&& (board[r][c] == null || board[r][c].white != a.white))
						validMoves.add(new Move(row, col, r, c));
				}
			} else {
				int range;
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
						if(i != j && j != 0
							&& (!a.type.equals(Piece.Type.BISHOP) || (i+j)%2==0)
							&& (!a.type.equals(Piece.Type.ROOK) || (i+j)%2==1)) {
							range = 16;
							if(a.type.equals(Piece.Type.KING))
								range = 1;
							for (int k = 0; k < 16; k++) {
								int r = (row + i * k) % 16;
								int c = col + j * k;
								if (c < 0 || c >= 4) break;
									Piece b = board[r][c];
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
		KING('K', 1000),
		QUEEN('Q', 9),
		ROOK('R', 5),
		KNIGHT('N', 3),
		BISHOP('B', 3),
		PAWN('P', 1);
		public char type;
		public double value;
		Type(char c, double v) {
			type = c;
			value = v;
		}
		public String toString() {
			return String.valueOf(type);
		}
		public boolean equals(Type t) {
			return t != null && type == t.type;
		}
	}

	public Type type;
	public boolean white;
	public Piece(Type type, boolean white) {
		this.type = type;
		this.white = white;
	}
	public String toString() {
		String str = type.toString();
		if (!white) str = str.toLowerCase();
		return str;
	}
	public double value(){
		return white ? type.value : -type.value;
	}
}

class Move {
	//data available from parsing
	int startRow, startCol, endRow, endCol;
	Piece.Type promotion;
	//metadata
	Piece captures;
	int halfMoves;
	public static Move parse(String move) {
		String[] proms = move.split("=");
		Piece.Type promotion = null;
		if(proms.length > 1){
			char t = proms[1].toUpperCase().charAt(0);
			if(t == Piece.Type.QUEEN.type)
				promotion = Piece.Type.QUEEN;
			else if(t == Piece.Type.ROOK.type)
				promotion = Piece.Type.ROOK;
			else if(t == Piece.Type.KNIGHT.type)
				promotion = Piece.Type.KNIGHT;
			else if(t == Piece.Type.BISHOP.type)
				promotion = Piece.Type.BISHOP;
		}
		String[] parts = proms[0].split("-");
		Move m = new Move(
		Integer.parseInt(parts[0].substring(1))-1,
		parts[0].charAt(0) - 'a',
		Integer.parseInt(parts[1].substring(1))-1,
		parts[1].charAt(0) - 'a',
		promotion);
		return m;
	}
	
	public Move(int startRow, int startCol, int endRow, int endCol){
		this(startRow, startCol, endRow, endCol, null);
	}

	public Move(int startRow, int startCol, int endRow, int endCol, Piece.Type promotion) {
		this.startRow = startRow;
		this.startCol = startCol;
		this.endRow = endRow;
		this.endCol = endCol;
		this.promotion = promotion;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Move)
			return equals((Move)obj);
		else
			return false;
	}

	public boolean equals(Move m) {
		boolean flag = startRow == m.startRow && startCol == m.startCol && endRow == m.endRow && endCol == m.endCol
			&& ((promotion == null && m.promotion == null) || promotion.equals(m.promotion));
		return flag;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		if(promotion != null)
			hash = (int)promotion.type;
		hash = (hash << 2) + startCol;
		hash = (hash << 4) + startRow;
		hash = (hash << 2) + endCol;
		hash = (hash << 4) + endRow;
		return hash;
	}
	
	public String toString(){
		String str = String.valueOf((char)(startCol + 'a')) + 
			Integer.toString(startRow + 1) + "-" +
			String.valueOf((char)(endCol + 'a'))+
			Integer.toString(endRow + 1);
		if(promotion != null)
			str += "=" + promotion.toString();
		return str;
	}
}

