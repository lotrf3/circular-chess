package circularchess.client;

import java.util.HashMap;

import circularchess.shared.*;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

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
	private final HashMap<String, Image> images = new HashMap<String, Image>();
	private Canvas canvas;
	private Context2d ctx;
	Game game;
	private NetworkManager networkManager;
	private FlexTable moveText;
	private Audio illegalMoveAudio, moveAudio, gameOverAudio;
	private int boardOrientation = 4;
	public VerticalPanel whiteLost, blackLost;

	public void onModuleLoad() {
		Image img;
		for (Piece.Type type : Piece.Type.values()) {
			String key = type.toString();
			String lowerKey = key.toLowerCase();
			img = new Image("images/white-" + lowerKey + ".svg");
			images.put(key, img);
			img.setVisible(false);
			RootPanel.get().add(img);
			
			img = new Image("images/black-" + lowerKey + ".svg");
			images.put(lowerKey, img);
			img.setVisible(false);
			RootPanel.get().add(img);
		}
		canvas = Canvas.createIfSupported();
		canvas.setWidth(canvasWidth + "px");
		canvas.setCoordinateSpaceWidth(canvasWidth);

		canvas.setHeight(canvasHeight + "px");
		canvas.setCoordinateSpaceHeight(canvasHeight);

		ctx = canvas.getContext2d();
		ctx.translate(canvasWidth / 2.0, canvasHeight / 2.0);
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(canvas);

		moveText = new FlexTable();
		moveText.addStyleName("moveText");
		panel.add(moveText);
		
		whiteLost = new VerticalPanel();
		panel.add(whiteLost);
		blackLost = new VerticalPanel();
		panel.add(blackLost);
				
		RootPanel.get().add(panel);
		
		newGame();

	    illegalMoveAudio = Audio.createIfSupported();
	    illegalMoveAudio.setSrc("audio/illegal-move.mp3");
	    moveAudio = Audio.createIfSupported();
	    moveAudio.setSrc("audio/move.mp3");
	    gameOverAudio = Audio.createIfSupported();
	    gameOverAudio.setSrc("audio/game-over.mp3");		
		
		initHandlers();

		// setup timer
		final Timer timer = new Timer() {
			public void run() {
				redraw();
			}
		};
		timer.scheduleRepeating(refreshRate);

	}

	int mode = 0;
	int[] selected;
	int mouseX, mouseY;

	public void initHandlers() {
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
							&& ((game.whiteToMove
									&& (target[0] == 7 || target[0] == 8) && selected[0]
									- r == target[0])
							|| (!game.whiteToMove
									&& (target[0] == 0 || target[0] == 15) && selected[0]
									+ r == target[0]))) {
						
						final PromotedPopup popup = new PromotedPopup(images, game, selected,
								target);
						popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
							public void setPosition(int offsetWidth, int offsetHeight) {
								int left = (Window.getClientWidth() - offsetWidth) / 3;
								int top = (Window.getClientHeight() - offsetHeight) / 3;
								popup.setPopupPosition(left, top);
							}
						});
								
					} else {
						Move m = new Move(selected[0], selected[1], target[0],
								target[1]);
						game.attemptMove(m);
						selected[0] = selected[1] = -1;
					}
				}
			}
		});
	}

	public void log(String str) {
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
					Image img = images.get(p.toString());
					ImageElement imgElem = (ImageElement) img.getElement().cast();
					if (i == selected[0] && j == selected[1] && mode == 1) {
						ctx.drawImage(imgElem, mouseX - imgElem.getWidth() / 2.0
								- canvasWidth / 2.0, mouseY - imgElem.getHeight()
								/ 2.0 - canvasHeight / 2.0);
					} else {
						ctx.drawImage(
								imgElem,
								Math.cos((i + boardOrientation + 0.5) * CELL_ANGLE) * midRad
										- imgElem.getWidth() / 2.0,
								Math.sin((i + boardOrientation + 0.5) * CELL_ANGLE) * midRad
										- imgElem.getHeight() / 2.0);
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
		if(radius < INNER_RADIUS || radius > INNER_RADIUS + 4*RING_WIDTH)
			return coords;
		coords[1] = (int) ((radius - INNER_RADIUS)/RING_WIDTH);
		coords[0] = (((int) (theta/CELL_ANGLE)) - boardOrientation + 16) % 16;
		return coords;
	}

	@Override
	public void onMove(Move move) {
		if(online)
			networkManager.sendMove(move);
		if(game.whiteToMove){
			moveText.setText(game.moves, 2, move.toString());
		}
		else{
			moveText.setText(game.moves, 0, game.moves + ".");
			moveText.setText(game.moves, 1, move.toString());
		}
		if(move.captures != null){
			if(move.captures.white)
				whiteLost.add(createImage(move.captures.toString().charAt(0)));
			else
				blackLost.add(createImage(move.captures.toString().charAt(0)));
		}
		
		if(game.result != Game.Result.ONGOING){
			gameOverAudio.load();
			gameOverAudio.play();
			final ResultPopup popup = new ResultPopup(this);
			popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
				public void setPosition(int offsetWidth, int offsetHeight) {
					int left = (Window.getClientWidth() - offsetWidth) / 3;
					int top = (Window.getClientHeight() - offsetHeight) / 3;
					popup.setPopupPosition(left, top);
				}
			});
		}
		else{
			moveAudio.load();
			moveAudio.play();
		}
	}
	
	public void newGame(){
		game = new Game();
		selected = new int[] { -1, -1 };
		moveText.removeAllRows();
		game.setMoveListener(this);

		final MainPopup popup = new MainPopup(game, this);
		popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int left = (Window.getClientWidth() - offsetWidth) / 3;
				int top = (Window.getClientHeight() - offsetHeight) / 3;
				popup.setPopupPosition(left, top);
			}
		});
	}
	
	@Override
	public void onIllegalMove(Move move) {
		illegalMoveAudio.load();
		illegalMoveAudio.play();
	}

	@Override
	public void onStart(boolean whiteHuman, boolean blackHuman,
			boolean whiteAuth, boolean blackAuth, String id) {
		if(!whiteHuman || !whiteAuth)
			boardOrientation = 12;
		else
			boardOrientation = 4;
		game.whiteHuman = whiteHuman;
		game.blackHuman = blackHuman;
		game.whiteAuth = whiteAuth;
		game.blackAuth = blackAuth;
		online = !(whiteAuth && blackAuth);
		if (online) {
			networkManager = new NetworkManager(this, id);
			networkManager.scheduleRepeating(pollRate);
		}
		game.start();
	}

	public Image createImage(char c){
		switch(c){
		case 'K': return new Image("images/white-k.svg");
		case 'Q': return new Image("images/white-q.svg");
		case 'R': return new Image("images/white-r.svg");
		case 'B': return new Image("images/white-b.svg");
		case 'N': return new Image("images/white-n.svg");
		case 'P': return new Image("images/white-p.svg");
		case 'k': return new Image("images/black-k.svg");
		case 'q': return new Image("images/black-q.svg");
		case 'r': return new Image("images/black-r.svg");
		case 'b': return new Image("images/black-b.svg");
		case 'n': return new Image("images/black-n.svg");
		case 'p': return new Image("images/black-p.svg");
		}
		return null;
	}

}