package circularchess.shared;

public interface MoveListener {
	public void onMove(Move move);
	public void onIllegalMove(Move move);
}
