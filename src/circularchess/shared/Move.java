package circularchess.shared;

import java.io.Serializable;

public class Move implements Serializable {
	private static final long serialVersionUID = 8720050413266102000L;
	// data available from parsing
	public int startRow;
	public int startCol;
	public int endRow;
	public int endCol;
	public Piece.Type promotion;
	// metadata
	public Piece captures;
	public int halfMoves;
	public String algNot;
	public boolean check, checkmate;

	public static Move parse(String move) {
		String[] proms = move.split("=");
		Piece.Type promotion = null;
		if (proms.length > 1) {
			char t = proms[1].toUpperCase().charAt(0);
			if (t == Piece.Type.QUEEN.type)
				promotion = Piece.Type.QUEEN;
			else if (t == Piece.Type.ROOK.type)
				promotion = Piece.Type.ROOK;
			else if (t == Piece.Type.KNIGHT.type)
				promotion = Piece.Type.KNIGHT;
			else if (t == Piece.Type.BISHOP.type)
				promotion = Piece.Type.BISHOP;
		}
		String[] parts = proms[0].split("-");
		Move m = new Move(Integer.parseInt(parts[0].substring(1)) - 1,
				parts[0].charAt(0) - 'a', Integer.parseInt(parts[1]
						.substring(1)) - 1, parts[1].charAt(0) - 'a', promotion);
		return m;
	}

	@SuppressWarnings("unused")
	private Move() {
	}

	public Move(int startRow, int startCol, int endRow, int endCol) {
		this(startRow, startCol, endRow, endCol, null);
	}

	public Move(int startRow, int startCol, int endRow, int endCol,
			Piece.Type promotion) {
		this.startRow = startRow;
		this.startCol = startCol;
		this.endRow = endRow;
		this.endCol = endCol;
		this.promotion = promotion;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Move)
			return equals((Move) obj);
		else
			return false;
	}

	public boolean equals(Move m) {
		boolean flag = startRow == m.startRow
				&& startCol == m.startCol
				&& endRow == m.endRow
				&& endCol == m.endCol
				&& ((promotion == null && m.promotion == null) || promotion
						.equals(m.promotion));
		return flag;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		if (promotion != null)
			hash = (int) promotion.type;
		hash = (hash << 2) + startCol;
		hash = (hash << 4) + startRow;
		hash = (hash << 2) + endCol;
		hash = (hash << 4) + endRow;
		return hash;
	}

	public String toString() {
		return algNot;
		/*
		String str = String.valueOf((char) (startCol + 'a'))
				+ Integer.toString(startRow + 1) + "-"
				+ String.valueOf((char) (endCol + 'a'))
				+ Integer.toString(endRow + 1);
		if (promotion != null)
			str += "=" + promotion.toString();
		return str;*/
	}
}
