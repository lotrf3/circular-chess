package circularchess.client;

import circularchess.shared.Move;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface ChessService extends RemoteService {
	void sendMove(String id, Move move) throws IllegalArgumentException;

	Move getMove(String id, int halfMove);
}
