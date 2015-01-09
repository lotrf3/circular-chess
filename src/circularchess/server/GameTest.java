package circularchess.server;

import java.util.*;

import circularchess.shared.Move;
import circularchess.shared.MoveListener;
import circularchess.shared.Piece;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

@Entity
public class GameTest {
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
	public Piece[][] board;
	public boolean whiteToMove = true;
	int halfMoves;
	int moves;
	public boolean whiteAuth;
	public boolean blackAuth;
	Result result;
	@Ignore
	MoveListener moveListener;
}
