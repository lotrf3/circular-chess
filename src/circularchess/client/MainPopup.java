package circularchess.client;

import circularchess.shared.Game;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainPopup extends PopupPanel {
	Game game;
	private StartListener callback;
	public MainPopup(final Game game, final StartListener callback) {
		super(false, true);
		this.game = game;
		this.callback = callback;
		
		VerticalPanel panel = new VerticalPanel();
		Button onePlayer = new Button("1-player", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
		        final OnePlayerPopup popup = new OnePlayerPopup(game, callback);
		        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
		          public void setPosition(int offsetWidth, int offsetHeight) {
		            int left = (Window.getClientWidth() - offsetWidth) / 3;
		            int top = (Window.getClientHeight() - offsetHeight) / 3;
		            popup.setPopupPosition(left, top);
		          }
		        });
		        hide();
			}
		});
		panel.add(onePlayer);
		Button twoLocalPlayer = new Button("Local 2-player", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callback.onStart(true,true,true,true,"");
				hide();
			}
		});
		panel.add(twoLocalPlayer);
		Button twoOnlinePlayer = new Button("Online 2-player", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
		        final OnlineTwoPlayerPopup popup = new OnlineTwoPlayerPopup(game,callback);
		        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
		          public void setPosition(int offsetWidth, int offsetHeight) {
		            int left = (Window.getClientWidth() - offsetWidth) / 3;
		            int top = (Window.getClientHeight() - offsetHeight) / 3;
		            popup.setPopupPosition(left, top);
		          }
		        });
		        hide();
			}
		});
		panel.add(twoOnlinePlayer);
		setWidget(panel);
	}
}
