package circularchess.client;

import java.util.HashMap;

import circularchess.shared.*;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CircularChess implements EntryPoint, MoveListener, StartListener {
	private static final int pollRate = 1000;
	private static final int refreshRate = 25;
	private static final int canvasWidth = 600;
	private static final int canvasHeight = 600;
	private static final double RING_WIDTH = 50;
	private static final double INNER_RADIUS = 100;
	private static final double CELL_ANGLE = 2 * Math.PI / 16;
	private boolean online = false;
	private HashMap<String, Image> images;
	private Canvas canvas;
	private Context2d ctx;
	private Game game;
	private NetworkManager networkManager;

	public void onModuleLoad() {
		game = new Game();
		selected = new int[] { -1, -1 };
		images = new HashMap<String, Image>();
		for (Piece.Type type : Piece.Type.values()) {
			String key = type.toString();
			images.put(key, new Image("images/white-" + key + ".svg"));
			key = key.toLowerCase();
			images.put(key, new Image("images/black-" + key + ".svg"));
		}
		canvas = Canvas.createIfSupported();
		canvas.setWidth(canvasWidth + "px");
		canvas.setCoordinateSpaceWidth(canvasWidth);

		canvas.setHeight(canvasHeight + "px");
		canvas.setCoordinateSpaceHeight(canvasHeight);

		ctx = canvas.getContext2d();
		ctx.translate(canvasWidth / 2.0, canvasHeight / 2.0);
		RootPanel.get().add(canvas);

		initHandlers();

		// setup timer
		final Timer timer = new Timer() {
			public void run() {
				redraw();
			}
		};
		timer.scheduleRepeating(refreshRate);

		final MainPopup popup = new MainPopup(game, this);
		popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int left = (Window.getClientWidth() - offsetWidth) / 3;
				int top = (Window.getClientHeight() - offsetHeight) / 3;
				popup.setPopupPosition(left, top);
			}
		});
	}

	int mode = 0;
	int[] selected;
	int mouseX, mouseY;

	public void initHandlers() {
		game.setMoveListener(this);
		canvas.addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent event) {
				mouseX = event.getRelativeX(canvas.getElement());
				mouseY = event.getRelativeY(canvas.getElement());
				selected = getCoords(mouseX, mouseY);
				mode = 1;
			}
		});
		canvas.addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				mouseX = event.getRelativeX(canvas.getElement());
				mouseY = event.getRelativeY(canvas.getElement());
			}
		});
		canvas.addMouseUpHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent event) {
				mode = 0;
				mouseX = event.getRelativeX(canvas.getElement());
				mouseY = event.getRelativeY(canvas.getElement());
				Piece p = game.board[selected[0]][selected[1]];
				if (p != null) {
					int[] target = getCoords(mouseX, mouseY);
					int r = (selected[0] / 8) * 2 - 1;
					if (p.type == Piece.Type.PAWN
							&& (game.whiteToMove
									&& (target[0] == 7 || target[0] == 8) && selected[0]
									- r == target[0])
							|| (!game.whiteToMove
									&& (target[0] == 0 || target[0] == 15) && selected[0]
									+ r == target[0])) {
						RootPanel.get().add(
								new PromotedPicker(images, game, selected,
										target));
					} else {
						Move m = new Move(selected[0], selected[1], target[0],
								target[1]);
						if (game.isLegal(m)) {
							if (online) {
								if ((game.whiteToMove && game.whiteAuth)
										|| (!game.whiteToMove && game.blackAuth)) {
									game.move(m);
									networkManager.sendMove(m);
								} else
									log("Move your own pieces.");
							} else
								game.move(m);
						} else {
							log("Invalid Move");
							selected[0] = selected[1] = -1;
						}
					}
				}
			}
		});
	}

	private void log(String str) {
		RootPanel.get().add(new Label(str));
	}

	private void redraw() {
		ctx.clearRect(-canvasWidth / 2.0, -canvasHeight / 2.0, canvasWidth,
				canvasHeight);
		ctx.setStrokeStyle("#000000");
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				// draw board
				if ((i + j) % 2 == 0) {
					ctx.setFillStyle("#EEEEEE");
				} else {
					ctx.setFillStyle("#999999");
				}
				ctx.beginPath();
				double innerRad = INNER_RADIUS + j * RING_WIDTH;
				double outerRad = INNER_RADIUS + (j + 1) * RING_WIDTH;
				ctx.moveTo(Math.cos(i * CELL_ANGLE) * innerRad,
						Math.sin(i * CELL_ANGLE) * innerRad);
				ctx.lineTo(Math.cos(i * CELL_ANGLE) * outerRad,
						Math.sin(i * CELL_ANGLE) * outerRad);
				ctx.arcTo(
						Math.cos((i + 0.5) * CELL_ANGLE) * outerRad
								/ Math.cos(CELL_ANGLE / 2.0),
						Math.sin((i + 0.5) * CELL_ANGLE) * outerRad
								/ Math.cos(CELL_ANGLE / 2.0),
						Math.cos((i + 1.0) * CELL_ANGLE) * outerRad,
						Math.sin((i + 1.0) * CELL_ANGLE) * outerRad, outerRad);
				ctx.lineTo(Math.cos((i + 1.0) * CELL_ANGLE) * innerRad,
						Math.sin((i + 1.0) * CELL_ANGLE) * innerRad);
				ctx.arcTo(
						Math.cos((i + 0.5) * CELL_ANGLE) * innerRad
								/ Math.cos(CELL_ANGLE / 2),
						Math.sin((i + 0.5) * CELL_ANGLE) * innerRad
								/ Math.cos(CELL_ANGLE / 2),
						Math.cos(i * CELL_ANGLE) * innerRad,
						Math.sin(i * CELL_ANGLE) * innerRad, innerRad);
				ctx.closePath();
				ctx.fill();
				ctx.stroke();
			}
		}
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {

				double midRad = INNER_RADIUS + (j + 0.5) * RING_WIDTH;
				Piece p = game.board[i][j];
				if (p != null) {
					ImageElement img = (ImageElement) images.get(p.toString())
							.getElement().cast();
					if (i == selected[0] && j == selected[1] && mode == 1) {
						ctx.drawImage(img, mouseX - img.getWidth() / 2.0
								- canvasWidth / 2.0, mouseY - img.getHeight()
								/ 2.0 - canvasHeight / 2.0);
					} else {
						ctx.drawImage(
								img,
								Math.cos((i + 0.5) * CELL_ANGLE) * midRad
										- img.getWidth() / 2.0,
								Math.sin((i + 0.5) * CELL_ANGLE) * midRad
										- img.getHeight() / 2.0);
					}
				}
			}
		}
	}

	public int[] getCoords(double x, double y) {
		x -= canvasWidth / 2.0;
		y -= canvasHeight / 2.0;
		int[] coords = { -1, -1 };
		double theta = (Math.atan2(y, x) + 2 * Math.PI) % (2 * Math.PI);
		double radius = Math.sqrt(x * x + y * y);
		while ((coords[1] + 1) * RING_WIDTH + INNER_RADIUS < radius)
			coords[1]++;
		while ((coords[0] + 1) * CELL_ANGLE < theta)
			coords[0]++;
		return coords;
	}

	@Override
	public void onMove(Move move) {
		log(move.toString());
	}

	@Override
	public void onStart(boolean whiteHuman, boolean blackHuman,
			boolean whiteAuth, boolean blackAuth, String id) {
		game.whiteHuman = whiteHuman;
		game.blackHuman = blackHuman;
		game.whiteAuth = whiteAuth;
		game.blackAuth = blackAuth;
		online = !(whiteAuth && blackAuth);
		if (online) {
			networkManager = new NetworkManager(game, id);
			networkManager.scheduleRepeating(pollRate);
		}
		game.start();
	}

}