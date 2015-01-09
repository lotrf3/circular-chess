package circularchess.client;
import circularchess.shared.Game;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class OnlineTwoPlayerPopup extends PopupPanel implements ValueChangeHandler, ChangeHandler,ClickHandler {
	boolean flag = false;
	Game game;
	int color=0;
	final RadioButton rb0,rb1,rb2;
	final TextBox txt;
	final Button start;
	private StartListener callback;
	OnlineTwoPlayerPopup(Game game, StartListener callback){
		this.game = game;
		this.callback = callback;
		VerticalPanel panel = new VerticalPanel();
		Label label = new Label("Share this code with a friend to play, or enter their code here");
		panel.add(label);
		
		txt = new TextBox();
		txt.setText(generateCode());
		txt.addChangeHandler(this);
		panel.add(txt);
		
	    rb0 = new RadioButton("color", "Random");
	    rb0.setValue(true);
	    rb0.addValueChangeHandler(this);
		panel.add(rb0);
	    rb1 = new RadioButton("color", "White");
	    rb1.addValueChangeHandler(this);
		panel.add(rb1);
	    rb2 = new RadioButton("color", "Black");
	    rb2.addValueChangeHandler(this);
		panel.add(rb2);
		
	    start = new Button("Start");
	    start.addClickHandler(this);
	    panel.add(start);
		setWidget(panel);
	}
	private String generateCode(){
		int x = Math.abs(Random.nextInt());
		x = x - x % 3 + color;
		String str = Integer.toString(x,36);
		int sum = 0;
		for(int i=0; i<str.length(); i++){
			sum += Integer.parseInt(str.substring(i,i+1), 36);
		}
		String check = Integer.toString(sum % 36,36);
		RootPanel.get().add(new Label(check + "  "+ sum));
		return check + str;
	}
	private int parseCode(){
		String code = txt.getText();
		int checkSum = Integer.parseInt(code.substring(0,1),36);
		String str = code.substring(1);
		int sum = 0;
		for(int i=0; i<str.length(); i++){
			sum += Integer.parseInt(str.substring(i,i+1), 36);
		}
		sum = sum % 36;
		RootPanel.get().add(new Label(checkSum + "  "+ sum));
		if(sum != checkSum)
			return -1;
		else
			return Integer.parseInt(str, 36) % 3;
	}
	@Override
	public void onValueChange(ValueChangeEvent event) {
		if(rb0.getValue())
			color = 0;
		else if(rb1.getValue())
			color = 1;
		else if(rb2.getValue())
			color = 2;
		txt.setText(generateCode());
		flag = false;
	}
	@Override
	public void onChange(ChangeEvent event) {
		color = parseCode();
		if(color == 0)
			rb0.setValue(true);
		else if(color == 2)
			rb1.setValue(true);
		else if(color == 1)
			rb2.setValue(true);
		flag = true;
		start.setEnabled(color != -1);
	}
	@Override
	public void onClick(ClickEvent event) {
		String code = txt.getText();
		if (color == 0)
		{
			int x = Integer.parseInt(code.substring(1),36);
			if(x % 6 < 3)
				flag = !flag;
		}
		else if(color == 1)
			flag = !flag;
		callback.onStart(true,true,flag,!flag,code);
		hide();
	}

}
