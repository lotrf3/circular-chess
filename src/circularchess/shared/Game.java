package circularchess.shared;

import java.util.*;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Serialize;

@Entity
public class Game {
	public enum Result {
		ONGOING("*", "Ongoing"),
		WHITE_CHECKMATE("1-0", "White checkmate"),
		BLACK_CHECKMATE("0-1", "Black checkmate"),
		WHITE_RESIGNS("0-1", "White resigns"),
		BLACK_RESIGNS("1-0", "Black resigns"),
		STALEMATE("1/2-1/2", "Stalemate"),
		INSUFFICIENT_MATERIAL("1/2-1/2", "Insufficient material"),
		THREEFOLD_REPETITION("1/2-1/2", "Threefold repetition"),
		FIFTY_MOVE_RULE("1/2-1/2", "Fifty move rule"),
		WHITE_TIME_FORFIET("0-1", "White ran out of time"),
		BLACK_TIME_FORFIET("1-0", "Black ran out of time"),
		DRAW_BY_AGREEMENT("1/2-1/2", "Draw by agreement");
		String result, type;
		Result(String result, String type){
			this.result = result;
			this.type = type;
		}
		
		public String toString(){
			return result + ". " + type;
		}
		
		public double value(){
			if(result == "1-0")
				return Double.POSITIVE_INFINITY;
			else if (result == "0-1")
				return Double.NEGATIVE_INFINITY;
			else if (result == "1/2-1/2")
				return 0;
			else
				return Double.NaN;
		}
		
		public double rating() throws Exception{

			if(result == "1-0")
				return 1;
			else if (result == "0-1")
				return 0;
			else if (result == "1/2-1/2")
				return 0.5;
			else
				throw new Exception();
		}
	}
	
	@Id
	public String id;
	HashMap<String, Integer> repetitions;
	public boolean whiteHuman = true;
	public boolean blackHuman = true;
	public Stack<Move> history;
	@Serialize
	public Piece[][] board;
	public boolean whiteToMove = true;
	int halfMoves;
	int moves;
	public boolean whiteAuth;
	public boolean blackAuth;
	Result result;
	public Game(){
		whiteAuth = blackAuth = true;
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
		repetitions = new HashMap<String, Integer>();
		repetitions.put(toFENNoMove().toString(), 1);
		result = Result.ONGOING;
	}
	
	private StringBuilder toFENNoMove(){
		int nulls = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				if (board[i][j] != null){
					if(nulls > 0)
						sb.append(Integer.toString(nulls));
					sb.append(board[i][j].toString());
					nulls=0;
				}
				else
					nulls++;
			}
			if(i<15){
				if(nulls > 0)
					sb.append(nulls);
				sb.append("/");
			}
			nulls=0;
		}
		if(whiteToMove)
			sb.append(" w");
		else
			sb.append(" b");
		return sb;
	}
	
	public String toFEN(){
		StringBuilder sb = toFENNoMove();
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
	
	public void move(Move move){
		move(move, true);
	}
	
	private void aiMove(){
		if((whiteToMove && !whiteHuman)
				|| !whiteToMove && !blackHuman)
			move(bestMove());
	}
	
	public void start(){
		aiMove();
	}
	
	
	private void move(Move move, boolean permanent) {
		history.push(move);
		Piece dest = board[move.endRow][move.endCol];
		Piece src = board[move.startRow][move.startCol];
		move.captures = dest;
		move.halfMoves = halfMoves;
		board[move.endRow][move.endCol] = board[move.startRow][move.startCol];
		if(move.promotion != null)
			board[move.endRow][move.endCol].type = move.promotion;
		board[move.startRow][move.startCol] = null;
		if(!whiteToMove)
			moves++;
		whiteToMove = !whiteToMove;
		String key = toFENNoMove().toString();
		Integer reps = repetitions.get(key);
		if(reps == null)
			repetitions.put(key, 1);
		else if(reps == 2)
			result = Result.THREEFOLD_REPETITION;
		else
			repetitions.put(key, reps + 1);
		if(dest != null || src.type.equals(Piece.Type.PAWN))
			halfMoves = 0;
		else
			halfMoves++;
		if(halfMoves == 50)
			result = Result.FIFTY_MOVE_RULE;
		if(permanent){
			if(moveListener != null)
				moveListener.onMove(move);
			aiMove();
		}
	}
	
	public void unmove(){
		Move move = history.pop();
		String key = toFENNoMove().toString();
		repetitions.put(key, repetitions.get(key)-1);
		
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
		for(Move m : allLegalMoves()){
			move(m, false);
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
		for(Move m : allPseudoLegalMoves()){
			//log(depth + ":" + m.toString());
			move(m, false);
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
		for (Move m : allLegalMoves()) {
			move(m, false);
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
	 
	    for(Move m : allLegalMoves())  {
	    	if(board[m.endRow][m.endCol] != null){
		        move(m, false);
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
		if(result != Result.ONGOING)
			return result.value();
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

	public boolean isLegal(Move m) {
		return legalMoves(m.startRow, m.startCol, new HashSet<Move>()).contains(m);
	}
	
	private Set<Move> allPseudoLegalMoves(){
		Set<Move> moves = new HashSet<Move>();
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				pseudoLegalMoves(i,j,moves);
		return moves;
	}
	
	
	private Set<Move> allLegalMoves(){
		Set<Move> moves = new HashSet<Move>();
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				legalMoves(i,j,moves);
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
	
	private void calculateCheckmate(){
		if(allLegalMoves().size() == 0)
			if(!whiteToMove)
				result = Result.WHITE_CHECKMATE;
			else
				result = Result.BLACK_CHECKMATE;
			
	}
	
	private boolean canKingBeCaptured(Set<Move> moves){
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++){
				pseudoLegalMoves(i,j,moves);
				for(Move m : moves){
					Piece p = board[m.endRow][m.endCol];
					if(p != null && p.type == Piece.Type.KING)
						return true;
				}
			}
		return false;

	}
	
	private Set<Move> legalMoves(int row, int col, Set<Move> moves){
		pseudoLegalMoves(row, col, moves);
		Iterator<Move> it = moves.iterator();
		Set<Move> temp = new HashSet<Move>();
		while(it.hasNext()){
			Move m = it.next();
			move(m, false);
			if(canKingBeCaptured(temp))
				it.remove();
			unmove();
			temp.clear();
		}
		return moves;
	}
	
	private Set<Move> pseudoLegalMoves(int row, int col, Set<Move> validMoves) {
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
			else if(a.type.equals(Piece.Type.KNIGHT)) {
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
						if(!(i == 0 && j == 0)
							&& (!a.type.equals(Piece.Type.BISHOP) || (i+j+2)%2==0)
							&& (!a.type.equals(Piece.Type.ROOK) || (i+j+2)%2==1)) {
							range = 16;
							if(a.type.equals(Piece.Type.KING))
								range = 1;
							for (int k = 1; k <= range; k++) {
								int r = (row + i * k + 16) % 16;
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
	@Ignore
	MoveListener moveListener;
	public void setMoveListener(MoveListener listener){
		moveListener = listener;
	}
	
	private void adjustRating(Player p, double result, double ro, int gp){
		double K = 20;
		if(p.gamesPlayed < 30)
			K = 50;
		else if(gp < 30)
			K = 10;
		p.rating += K * (result - 1/(1 + Math.pow(10, ((p.rating - ro)/400))));
	}
	
	public void adjustRatings(Player w, Player b) throws Exception{
		double rw = w.rating,
				rb = b.rating;
		adjustRating(w, result.rating(), rb, b.gamesPlayed);
		adjustRating(b, 1-result.rating(), rw, w.gamesPlayed);
		w.gamesPlayed++;
		b.gamesPlayed++;
	}
}
