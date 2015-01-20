package circularchess.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResultPopup extends PopupPanel {
	public ResultPopup(final CircularChess chess) {
		super(false, true);
		VerticalPanel panel = new VerticalPanel();
		Label label = new Label(chess.game.result.toString());
		panel.add(label);
		Button button = new Button("New Game", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				chess.newGame();
				hide();
			}
		});
		panel.add(button);
		setWidget(panel);

	}
}
