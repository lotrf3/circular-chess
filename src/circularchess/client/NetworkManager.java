package circularchess.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import circularchess.shared.Game;
import circularchess.shared.Move;

public class NetworkManager extends Timer {
	
	Game game;
	public NetworkManager(Game game){
		this.game = game;
		
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
		    	  game.move(move);
		      }
		    };
		service.getMove("",game.history.size(),callback);
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
		service.sendMove("", move,callback);
	}
}
