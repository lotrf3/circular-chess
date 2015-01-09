package circularchess.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import circularchess.shared.Game;
import circularchess.shared.Move;

public class NetworkManager extends Timer {
	
	Game game;
	String id;
	public NetworkManager(Game game, String id){
		this.game = game;
		this.id = id;
	}

	ChessServiceAsync service;
	
	@Override
	public void run() {
		if(service == null)
			service = GWT.create(ChessService.class);
		AsyncCallback<Move> callback = new AsyncCallback<Move>() {
		      public void onFailure(Throwable caught) {
		      }

		      public void onSuccess(Move move) {
		    	  if(move != null)
		    		  game.move(move);
		      }
		    };
		service.getMove(id,game.history.size(),callback);
	}
	
	public void sendMove(Move move){
		if(service == null)
			service = GWT.create(ChessService.class);

		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
		      public void onFailure(Throwable caught) {
		      }

		      public void onSuccess(Void move) {
		      }
		    };
		service.sendMove(id, move,callback);
	}
}
