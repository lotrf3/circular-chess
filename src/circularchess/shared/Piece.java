package circularchess.shared;

import java.io.Serializable;


public class Piece implements Serializable {

	public enum Type {
		KING('K', 1000), QUEEN('Q', 9), ROOK('R', 5), KNIGHT('N', 3), BISHOP(
				'B', 3), PAWN('P', 1);
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

	private Piece() {
	}

	public Piece(Type type, boolean white) {
		this.type = type;
		this.white = white;
	}

	public String toString() {
		String str = type.toString();
		if (!white)
			str = str.toLowerCase();
		return str;
	}

	public double value() {
		return white ? type.value : -type.value;
	}
}