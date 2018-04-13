package samplingProg;

import java.awt.*;

import dataView.*;
import utils.*;

import sampling.*;


public class RandomDigitApplet extends XApplet implements RandDigActionInterface {
	static final private String RANDOM_SEED_PARAM = "random";
	
	static final private int kMillisecPerFrame = 50;
	
	private CoreRandomDigit theDigit;
	
	private XButton generateButton;
	
//	private XLabel newDigitDisplay;
	
	public void setupApplet() {
		DigitImages.loadDigits(this);
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			generateButton = new XButton(translate("Generate digit"), this);
		add(generateButton);
		
			long seed = Long.parseLong(getParameter(RANDOM_SEED_PARAM));
			theDigit = new RandomDigit(this, this, seed);
//			theDigit = new MiniRandomDigit(this, this, seed);
		add(theDigit);
		
//			newDigitDisplay = new XLabel("0 ", XLabel.LEFT, this);
//		add(newDigitDisplay);
	}
	
	public void noteNewDigit(CoreRandomDigit theDigit) {
//			newDigitDisplay.setText(theDigit.getDigit() + " ");
	}
	
	public void noteClearedDigit(CoreRandomDigit theDigit) {
//			newDigitDisplay.setText("");
	}
	
	private boolean localAction(Object target) {
		if (target == generateButton) {
			theDigit.animateNextDigit(kMillisecPerFrame);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}