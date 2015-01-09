package circularchess.server;

import static com.googlecode.objectify.ObjectifyService.ofy;
import circularchess.client.ChessService;
import circularchess.shared.Game;
import circularchess.shared.Move;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ChessServiceImpl extends RemoteServiceServlet implements
		ChessService {
	static {
		ObjectifyService.register(Game.class);
	}

	@Override
	public void sendMove(String id, Move move) throws IllegalArgumentException {
		Game game = ofy().load().type(Game.class).id(id).now();
		if (game == null) {
			game = new Game();
			game.id = id;
		}
		if (game.isLegal(move))
			game.move(move);
		else
			throw new IllegalArgumentException();
		ofy().save().entity(game).now();
	}

	@Override
	public Move getMove(String id, int halfMove) {
		Game game = ofy().load().type(Game.class).id(id).now();
		if (game != null && halfMove < game.history.size())
			return game.history.get(halfMove);
		else
			return null;
	}
}
