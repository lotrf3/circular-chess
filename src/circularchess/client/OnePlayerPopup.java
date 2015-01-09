package circularchess.client;

import circularchess.shared.Game;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OnePlayerPopup extends PopupPanel {
	Game game;
	private StartListener callback;
	public OnePlayerPopup(final Game game, final StartListener callback){
		super(false,true);
		this.game = game;
		this.callback = callback;
		VerticalPanel panel = new VerticalPanel();
		Button random = new Button("Random", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				boolean flag = true;
				if(Math.random() < 0.5)
					flag = !flag;
				callback.onStart(flag,!flag,true,true,"");
				hide();
			}
		});
		panel.add(random);
		Button white = new Button("White", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callback.onStart(true,false,true,true,"");
				hide();

			}
		});
		panel.add(white);
		Button black = new Button("Black", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callback.onStart(false,true,true,true,"");
				hide();
			}
		});
		panel.add(black);
		setWidget(panel);
		
	}
}
