package circularchess.server;

import circularchess.client.ChessService;
import circularchess.shared.FieldVerifier;
import circularchess.shared.Game;
import circularchess.shared.Move;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ChessServiceImpl extends RemoteServiceServlet implements
		ChessService {
	

	private Game game;

	@Override
	public void sendMove(String id, Move move) throws IllegalArgumentException {
		if(game == null)
			game = new Game();
		if(game.isLegal(move))
			game.move(move);
		else
			throw new IllegalArgumentException();
	}

	@Override
	public Move getMove(String id, int halfMove) {
		if(game == null)
			game = new Game();
		if(halfMove < game.history.size())
			return game.history.get(halfMove);
		else
			throw new IllegalArgumentException();
	}
}
