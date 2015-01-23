package circularchess.client;

import com.google.gwt.user.client.ui.PopupPanel;

public abstract class ShowPopupPanel extends PopupPanel {
	public ShowPopupPanel(boolean autoHide) {
		super(autoHide);
	}
	public ShowPopupPanel(boolean autoHide, boolean modal) {
		super(autoHide, modal);
	}
	
	public abstract void onShow();
}
