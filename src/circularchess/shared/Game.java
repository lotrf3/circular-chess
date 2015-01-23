package circularchess.shared;

import java.util.*;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
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
	public int moves;
	public boolean whiteAuth;
	public boolean blackAuth;
	private int[] whiteKing, blackKing;
	public Date date;
	public Result result;
	public Game(){
		date = new Date();
		whiteAuth = blackAuth = true;
		history = new Stack<Move>();
		whiteKing = new int[]{15,0};
		blackKing = new int[]{8,0};
		board = new Piece[][]{
			{
				new Piece(Piece.Type.QUEEN, true), new Piece(Piece.Type.BISHOP, true), new Piece(Piece.Type.KNIGHT, true), new Piece(Piece.Type.ROOK, true)
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
				new Piece(Piece.Type.QUEEN, false), new Piece(Piece.Type.BISHOP, false), new Piece(Piece.Type.KNIGHT, false), new Piece(Piece.Type.ROOK, false)
			}, {
				new Piece(Piece.Type.KING, false), new Piece(Piece.Type.BISHOP, false), new Piece(Piece.Type.KNIGHT, false), new Piece(Piece.Type.ROOK, false)
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
				new Piece(Piece.Type.KING, true), new Piece(Piece.Type.BISHOP, true), new Piece(Piece.Type.KNIGHT, true), new Piece(Piece.Type.ROOK, true)
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
				|| !whiteToMove && !blackHuman){
			Move m = bestMove();
			isPseudoLegal(m);//Adds algebraic notation
			move(m);
		}
	}
	
	public void start(){
		aiMove();
	}
	
	public void attemptMove(Move move){
		if(isLegal(move)){
			if (!((whiteToMove && whiteAuth)
				|| (!whiteToMove && blackAuth))){
				if(moveListener != null)
					moveListener.onIllegalMove(move);
			}
			else
				move(move, true);
		}
		else
			if(moveListener != null)
				moveListener.onIllegalMove(move);
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
		if(src.type == Piece.Type.KING){
			if(src.white){
				whiteKing[0] = move.endRow;
				whiteKing[1] = move.endCol;
			}
			else{
				blackKing[0] = move.endRow;
				blackKing[1] = move.endCol;
			}
		}
		if(whiteToMove)
			moves++;
		whiteToMove = !whiteToMove;
		String key = toFENNoMove().toString();
		Integer reps = repetitions.get(key);
		if(reps == null)
			repetitions.put(key, 1);
		else if(reps >= 2)
		{
			repetitions.put(key,3);
			result = Result.THREEFOLD_REPETITION;
		}else
			repetitions.put(key, reps + 1);
		if(dest != null || src.type.equals(Piece.Type.PAWN))
			halfMoves = 0;
		else
			halfMoves++;
		if(halfMoves == 50)
			result = Result.FIFTY_MOVE_RULE;
		if(permanent){
			calculateCheckmate(move);
			if(moveListener != null)
				moveListener.onMove(move);
			aiMove();
		}
	}
	
	public void unmove(){
		Move move = history.pop();
		String key = toFENNoMove().toString();
		int reps =  repetitions.get(key)-1;
		if(reps > 0)
			repetitions.put(key, reps);
		else
			repetitions.remove(key);
		
		board[move.startRow][move.startCol] = board[move.endRow][move.endCol];
		if(move.captures != null)
			board[move.endRow][move.endCol] = move.captures;
		else
			board[move.endRow][move.endCol] = null;
		if(move.promotion != null)
			board[move.startRow][move.startCol].type = Piece.Type.PAWN;
		halfMoves = move.halfMoves;
		result = Result.ONGOING;
		if(board[move.startRow][move.startCol].type == Piece.Type.KING){
			if(board[move.startRow][move.startCol].white)
			{
				whiteKing[0] = move.startRow;
				whiteKing[1] = move.startCol;
			}
			else{
				blackKing[0] = move.startRow;
				blackKing[1] = move.startCol;
			}
		}
		if(!whiteToMove)
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
		if(isPseudoLegal(m))
		{
			move(m, false);
			boolean isLegal = !canKingBeCaptured(!whiteToMove);
			unmove();
			return isLegal;
		}
		return false;
	}
	
	public boolean isUnderAttack(int row, int col, boolean attackerColor){
		Piece src;
		for(int i=-1; i<=1; i+=2){
			int d = (row/8) * 2 - 1;
			if(attackerColor)
				d*=-1;
			int r = (row - d + 16) % 16;
			int c = col + i;
			if(c >= 0 && c < 4){
				src = board[r][c];
				if(src != null
						&& src.type == Piece.Type.PAWN
						&& src.white == attackerColor)
					return true;
			}
		}
		for(int i=0; i<8; i++)
		{
			int x = (((i+1)%4)/2+1) * ((i/4)*-2+1);
			int y = (((i+7)%4)/2+1) * ((((i+6)%8)/4)*-2+1);
			int r = (row + x + 16) % 16;
			int c = col + y;
			if(c >= 0 && c < 4){
				src = board[r][c];
				if(src != null
						&& src.type == Piece.Type.KNIGHT
						&& src.white == attackerColor)
					return true;
			}
		}
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				if(!(i == 0 && j == 0)) {
					for (int k = 1; k <= 16; k++) {
						int r = (row + i * k + 16) % 16;
						int c = col + j * k;
						if (c < 0 || c >= 4) break;
						src = board[r][c];
						if(src != null){
							if(src.white == attackerColor)
							{
								if((src.type == Piece.Type.BISHOP
										|| src.type == Piece.Type.QUEEN
										|| (src.type == Piece.Type.KING && k == 1))
											&& (i+j+2)%2==0)
									return true;

								if((src.type == Piece.Type.ROOK
										|| src.type == Piece.Type.QUEEN
										|| (src.type == Piece.Type.KING && k == 1))
											&& (i+j+2)%2==1)
									return true;
							}
							break;
						}
					}
				}
		return false;
	}
	
	private Set<Move> allPseudoLegalMoves(){
		Set<Move> moves = new HashSet<Move>();
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				pseudoLegalMoves(i,j,moves, null);
		return moves;
	}
	
	
	private Set<Move> allLegalMoves(){
		Set<Move> moves = new HashSet<Move>();
		for(int i=0; i<16; i++)
			for(int j=0; j<4; j++)
				legalMoves(i,j,moves, null);
		return moves;
	}
	
	private void validPawnMoves(int startRow, int startCol, int endRow, int endCol, Set<Move> moves, Map<String, List<Move>> algNot){
		if((whiteToMove && (endRow == 7 || endRow == 8))
			|| (!whiteToMove && (endRow == 0 || endRow == 15))){
			addMove(new Move(startRow, startCol, endRow, endCol, Piece.Type.QUEEN),moves,algNot);
			addMove(new Move(startRow, startCol, endRow, endCol, Piece.Type.ROOK),moves,algNot);
			addMove(new Move(startRow, startCol, endRow, endCol, Piece.Type.KNIGHT),moves,algNot);
			addMove(new Move(startRow, startCol, endRow, endCol, Piece.Type.BISHOP),moves,algNot);
		}
		else
			addMove(new Move(startRow, startCol, endRow, endCol),moves,algNot);
	}
	
	private void log(String str){
		System.out.println(str);
	}
	
	private void calculateCheckmate(Move move){
		move.check = canKingBeCaptured(whiteToMove);
		String oldAlgNot = move.algNot;
		if(move.check)
			move.algNot+="+";
		Set<Move> moves = allPseudoLegalMoves();
		for(Move m : moves){
			move(m, false);
			if(!canKingBeCaptured(!whiteToMove)){
				unmove();
				return;
			}
			unmove();
		}
		if(move.check)
			if(!whiteToMove){
				move.algNot = oldAlgNot + "#";
				move.checkmate = true;
				result = Result.WHITE_CHECKMATE;
			}
			else{
				move.algNot = oldAlgNot + "#";
				move.checkmate = true;
				result = Result.BLACK_CHECKMATE;
			}
		else
			result = Result.STALEMATE;
	}
	
	private boolean canKingBeCaptured(boolean white){
		if(white)
			return isUnderAttack(whiteKing[0],whiteKing[1],false);
		else
			return isUnderAttack(blackKing[0],blackKing[1],true);
	}
	
	private Set<Move> legalMoves(int row, int col, Set<Move> moves, HashMap<String, List<Move>> algNot){
		pseudoLegalMoves(row, col, moves, algNot);
		Iterator<Move> it = moves.iterator();
		while(it.hasNext()){
			Move m = it.next();
			move(m, false);
			if(canKingBeCaptured(!whiteToMove))
				it.remove();
			unmove();
		}
		return moves;
	}
	
	
	private void addMove(Move m, Set<Move> validMoves, Map<String,List<Move>> algNot){
		validMoves.add(m);
		if(algNot != null){
			List<Move> reps = algNot.get(m.algNot);
			if(reps == null){
				reps = new ArrayList<Move>();
				algNot.put(m.algNot,reps);
			}
			reps.add(m);
		}
	}
	
	private String getAlgNotFile(int col){
		return String.valueOf((char)(col + 'a'));
	}
	private String getAlgNotRank(int row){
		 return String.valueOf(row + 1);
	}

	private boolean isPseudoLegal(Move m){
		StringBuilder algNot = new StringBuilder();
		boolean isLegal = false;
		Piece src = board[m.startRow][m.startCol];
		Piece dest = board[m.endRow][m.endCol];
		if(src.white != whiteToMove)
			return false;
		
		//nobody can self-capture
		if(dest != null && dest.white == src.white)
			return false;
		
		//nobody can null-move
		if(dest == src)
			return false;
		boolean fileReq = false,
				rankReq = false;
		if(src.type == Piece.Type.PAWN){
			//ensure promotion when required
			if(m.promotion == null){
				if(src.white && (m.endRow == 7 || m.endRow == 8)
							|| (!src.white&& (m.endRow == 0 || m.endRow == 15)))
				return false;
			} else //ensure no promotion when not required
				if(!(src.white && (m.endRow == 7 || m.endRow == 8)
							|| (!src.white&& (m.endRow == 0 || m.endRow == 15))))
					return false;
			
			
			int r = (m.startRow/8) * 2 - 1;
			if(src.white)
				r*=-1;
			if(dest != null){//if capturing
				if(Math.abs(m.startCol - m.endCol) == 1
					&& m.startRow + r == m.endRow){

					algNot.append(getAlgNotFile(m.startCol));
					algNot.append('x');
					algNot.append(getAlgNotFile(m.endCol)); 
					algNot.append(getAlgNotRank(m.endRow));
					isLegal = true;
				}
					
			}
			else //not capturing
			{
				if(m.startCol == m.endCol)//ensure same file
				{
					algNot.append(getAlgNotFile(m.endCol)); 
					algNot.append(getAlgNotRank(m.endRow));
					if(m.startRow + r == m.endRow)
						isLegal = true;
					else if(((src.white && ((m.startRow == 1 && board[2][m.startCol] == null) || (m.startRow == 14 && board[13][m.startCol] == null)))
								|| (!src.white && ((m.startRow == 6 && board[5][m.startCol] == null) || (m.startRow == 9 && board[10][m.startCol] == null))))
								&&(m.startRow + 2*r == m.endRow))
						isLegal = true;
				}
			}
			if(m.promotion != null){
				algNot.append('=');
				algNot.append(m.promotion.toString().toUpperCase());
			}
			
		} else if(src.type == Piece.Type.KNIGHT){
			for(int i=0; i<8; i++)
			{
				int x = (((i+1)%4)/2+1) * ((i/4)*-2+1);
				int y = (((i+7)%4)/2+1) * ((((i+6)%8)/4)*-2+1);
				int r = (m.endRow + x + 16) % 16;
				int c = m.endCol + y;
				if(c >= 0 && c < 4){
					if(src == board[r][c])
						isLegal = true;
					else if (board[r][c] != null
							&& board[r][c].type == Piece.Type.KNIGHT
							&& board[r][c].white == src.white)
					{
						if(c != m.startCol)
							fileReq = true;
						else if(r != m.startRow)
							rankReq = true; 
					}
						
						
				}
			}
			algNot.append(src.toString().toUpperCase());
			if(fileReq)
				algNot.append(getAlgNotFile(m.startCol));
			if(rankReq)
				algNot.append(getAlgNotRank(m.startRow));
			if(dest != null)
				algNot.append('x');
			algNot.append(getAlgNotFile(m.endCol));
			algNot.append(getAlgNotRank(m.endRow));
			
		} else if (src.type == Piece.Type.KING){
			int rowDiff = Math.abs(m.endRow-m.startRow);
			if(rowDiff == 15)
				rowDiff = 1;
			if(rowDiff <= 1 && Math.abs(m.endCol-m.startCol) <= 1){
				algNot.append(src.toString().toUpperCase());
				if(dest != null)
					algNot.append('x');
				algNot.append(getAlgNotFile(m.endCol)); 
				algNot.append(getAlgNotRank(m.endRow));
				isLegal = true;
			}
				
		} else {
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++)
					if(!(i == 0 && j == 0)
						&& (!src.type.equals(Piece.Type.BISHOP) || (i+j+2)%2==0)
						&& (!src.type.equals(Piece.Type.ROOK) || (i+j+2)%2==1)) {
						for (int k = 1; k <= 16; k++) {
							int r = (m.endRow + i * k + 16) % 16;
							int c = m.endCol + j * k;
							if (c < 0 || c >= 4) break;
							if(m.startRow == r && m.startCol == c){
								isLegal = true;
								break;
							}
							else if(board[r][c] != null) {
								if(board[r][c].type == src.type
										&& board[r][c].white == src.white){
									if(c != m.startCol)
										fileReq = true;
									else if(r != m.startRow)
										rankReq = true; 
								}
								break;
							}
						}
					}
			algNot.append(src.toString().toUpperCase());
			if(fileReq)
				algNot.append(getAlgNotFile(m.startCol));
			if(rankReq)
				algNot.append(getAlgNotRank(m.startRow));
			if(dest != null)
				algNot.append('x');
			algNot.append(getAlgNotFile(m.endCol));
			algNot.append(getAlgNotRank(m.endRow));
		}
		m.algNot = algNot.toString();
		return isLegal;
	}
	
	private Set<Move> pseudoLegalMoves(int row, int col, Set<Move> validMoves, Map<String, List<Move>> algNot) {
		Piece a = board[row][col];
		if(a != null && a.white == whiteToMove) {
			if(a.type.equals(Piece.Type.PAWN)){
				int r = (row/8) * 2 - 1;
				if(a.white)
					r*=-1;
				if(board[row + r][col] == null)
					validPawnMoves(row, col, row + r, col, validMoves,algNot);
				if(col > 0 && board[row+r][col-1] != null && board[row+r][col-1].white != a.white)
					validPawnMoves(row, col, row+r, col-1, validMoves,algNot);
				if(col < 3 && board[row+r][col+1] != null && board[row+r][col+1].white != a.white)
					validPawnMoves(row, col, row+r, col+1, validMoves,algNot);
				if(((a.white && ((row == 1 && board[2][col] == null) || (row == 14 && board[13][col] == null)))
					|| (!a.white && ((row == 6 && board[5][col] == null) || (row == 9 && board[10][col] == null))))
					&&(board[row + 2*r][col] == null))
						addMove(new Move(row, col, row + 2*r, col),validMoves,algNot);
						
			}
			else if(a.type.equals(Piece.Type.KNIGHT)) {
				for(int i=0; i<8; i++){
					int x = (((i+1)%4)/2+1) * ((i/4)*-2+1);
					int y = (((i+7)%4)/2+1) * ((((i+6)%8)/4)*-2+1);
					int r = (row + x + 16) % 16;
					int c = col + y;
					if(c >= 0 && c < 4
						&& (board[r][c] == null || board[r][c].white != a.white))
						addMove(new Move(row, col, r, c),validMoves,algNot);
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
									addMove(new Move(row, col, r, c),validMoves,algNot);
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
	
	
	
	public static Game parsePGN(String pgn){
		Game game = new Game();
		String[] lines = pgn.split("\\n");
		DateTimeFormat df = DateTimeFormat.getFormat("yyyy.MM.dd");
		int i=0;
		for(;i<lines.length; i++){
			RegExp regex = RegExp.compile("\\[(.*) \"(.*)\"\\]");
			MatchResult res = regex.exec(lines[i]);
			if(res != null){
				String key = res.getGroup(0);
				String value = res.getGroup(1);
				switch(key){
				case "Date":
					game.date = df.parse(value); break;
				case "White":
					game.whiteHuman = value == "Computer" ? false : true; break;
				case "Black":
					game.blackHuman = value == "Computer" ? false : true; break;
				case "Termination":
					for(Result r : Result.values())
						if(r.type == value){
							game.result = r;
							break;
						}
					break;
				}
			}
			else
			{
				String[] movesStr = lines[i].replaceAll("\\d+\\.", " ").split("\\s+");
				for(int j=0; j<movesStr.length; j++)
				{
					if(game.result == Result.ONGOING)
					{
						//FIXME: SUPER inefficient here 
						Set<Move> moves = game.allLegalMoves();
						for(Move m : moves)
						{
							game.isLegal(m);//adds algNot
							if(m.algNot == movesStr[j]){
								game.move(m);
								break;
							}
						}
					}
					else
						break;
				}
			}
		}
		return game;
	}
	
	private void toPGNTag(StringBuilder sb, String key, String value){
		sb.append('[')
			.append(key)
			.append(" \"")
			.append(value)
			.append("\"]\n");
	}
	
	public String toPGN(){
		StringBuilder sb = new StringBuilder();
		toPGNTag(sb,"Event","Live Chess");
		toPGNTag(sb,"Site","circular-chess.appspot.com");
		DateTimeFormat df = DateTimeFormat.getFormat("yyyy.MM.dd");
		toPGNTag(sb,"Round","1");
		toPGNTag(sb,"Date",df.format(date));
		
		toPGNTag(sb,"White",whiteHuman ? "Human" : "Computer");
		toPGNTag(sb,"Black",blackHuman ? "Human" : "Computer");
		toPGNTag(sb,"Result", result.result);
		toPGNTag(sb,"Termination",result.type);
		
		Iterator<Move> it = history.iterator();
		for(int i=1; it.hasNext(); i++){
			if(i % 10 == 1)
				sb.append('\n');
			sb.append(i)
				.append('.')
				.append(it.next().toString())
				.append(' ');
			if(it.hasNext())
				sb.append(it.next())
				.append(' ');
		}
		sb.append(result.result);
		return sb.toString();
	}
}
