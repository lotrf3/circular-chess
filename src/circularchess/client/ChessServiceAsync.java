package circularchess.client;

import circularchess.shared.Move;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>ChessService</code>.
 */
public interface ChessServiceAsync {
	void sendMove(String id, Move move, AsyncCallback<Void> callback)
			throws IllegalArgumentException;

	void getMove(String id, int halfMove, AsyncCallback<Move> callback);
}
