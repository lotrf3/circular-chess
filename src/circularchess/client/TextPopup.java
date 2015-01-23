package circularchess.client;

import com.google.gwt.user.client.ui.TextArea;

public class TextPopup extends ShowPopupPanel {
	final TextArea ta;
	public TextPopup(String text, int lines) {
		super(true);
		ta = new TextArea();
		ta.setCharacterWidth(80);
		ta.setVisibleLines(lines);
		ta.setText(text);
		setWidget(ta);
		ta.setSelectionRange(0,text.length());
	}

	@Override
	public void onShow() {
		ta.setFocus(true);
		ta.setSelectionRange(0,ta.getText().length());
	}
}
