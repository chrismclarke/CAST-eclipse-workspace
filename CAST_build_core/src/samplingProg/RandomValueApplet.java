package samplingProg;

import java.awt.*;

import dataView.*;
import utils.*;

import sampling.*;


public class RandomValueApplet extends XApplet implements RandomDigitProgInterface {
	static final private String RANDOM_SEED_PARAM = "random";
	static final private String DECIMAL_PARAM = "decimal";
	
	static final private int[] kMillisecPerFrame = {50, 47, 51, 49, 48};
	
	private RandomDigitsPanel theDigits;
	private XButton generateButton;
//	private XLabel newValueDisplay;
	
	private boolean valueIsDecimal;
	
	public void setupApplet() {
		DigitImages.loadDigits(this);
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			generateButton = new XButton(translate("Generate value"), this);
		add(generateButton);
		
			String decimalsString = getParameter(DECIMAL_PARAM);
			valueIsDecimal = decimalsString != null && decimalsString.equals("true");
			theDigits = new RandomDigitsPanel(this, this, valueIsDecimal ? RandomDigitsPanel.DECIMALS
															: RandomDigitsPanel.NO_DECIMALS, getParameter(RANDOM_SEED_PARAM));
//			theDigits = new RandomDigitsPanel(this, this, valueIsDecimal ? RandomDigitsPanel.DECIMALS
//															: RandomDigitsPanel.NO_DECIMALS, getParameter(RANDOM_SEED_PARAM),
//															RandomDigitsPanel.SMALL_DIGITS);
		add(theDigits);
		
//			newValueDisplay = new XLabel("000000", XLabel.LEFT, this);
//		add(newValueDisplay);
	}
	
	public void noteNewValue(RandomDigitsPanel valuePanel) {
//		if (valueIsDecimal)
//			newValueDisplay.setText(valuePanel.getDecimalValue() + "");
//		else
//			newValueDisplay.setText(valuePanel.getValue() + "");
	}
	
	public void noteClearedValue() {
//		newValueDisplay.setText("");
	}

	
	private boolean localAction(Object target) {
		if (target == generateButton) {
			theDigits.animateNextDigits(kMillisecPerFrame);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}