package circularchess.client;

import java.util.HashMap;

import circularchess.shared.Game;
import circularchess.shared.Move;
import circularchess.shared.Piece;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class PromotedPopup extends PopupPanel {
	public PromotedPopup(HashMap<String, Image> images, final Game game, final int[] selected, final int[] target) {
		super(false, true);
		HorizontalPanel panel = new HorizontalPanel();
		
		
		Image q, r, n, b;
		if (game.whiteToMove) {
			q = images.get("Q");
			r = images.get("R");
			n = images.get("N");
			b = images.get("B");
		} else {
			q = images.get("q");
			r = images.get("r");
			n = images.get("n");
			b = images.get("b");
		}

		q.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.attemptMove(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.QUEEN));
				hide();
			}
		});
		r.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.attemptMove(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.ROOK));
				hide();
			}
		});
		n.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.attemptMove(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.KNIGHT));
				hide();
			}
		});
		b.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.attemptMove(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.BISHOP));
				hide();
			}
		});
		

		q.setVisible(true);
		r.setVisible(true);
		n.setVisible(true);
		b.setVisible(true);
		
		panel.add(q);
		panel.add(r);
		panel.add(n);
		panel.add(b);
		
		setWidget(panel);

	}
}
