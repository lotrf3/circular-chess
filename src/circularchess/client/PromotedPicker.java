package circularchess.client;

import java.util.HashMap;

import circularchess.shared.Game;
import circularchess.shared.Move;
import circularchess.shared.Piece;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

public class PromotedPicker extends Composite {
	public PromotedPicker(HashMap<String, Image> images, final Game game,
			final int[] selected, final int[] target) {
		// Place the check above the text box using a vertical panel.
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
				game.move(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.QUEEN));
			}
		});
		r.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.move(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.ROOK));
			}
		});
		n.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.move(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.KNIGHT));
			}
		});
		b.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				game.move(new Move(selected[0], selected[1], target[0],
						target[1], Piece.Type.BISHOP));
			}
		});

		panel.add(q);
		panel.add(r);
		panel.add(n);
		panel.add(b);

		// All composites must call initWidget() in their constructors.
		initWidget(panel);
	}

}
