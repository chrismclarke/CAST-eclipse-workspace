package sampling;

import java.awt.*;

import dataView.*;


public class RandomDigit extends CoreRandomDigit {
	static final private double kLeadIn[] = {0.0, 0.05, 0.1, 0.2, 0.4, 0.7, 1.0, 1.5};
	static final private int kFramesPerDigit = 3;
	static final private int kBorder = 2;
	
	public RandomDigit(int maxDigit, XApplet applet, RandDigActionInterface digitAction, long seed) {
		super(maxDigit, applet, digitAction, seed);
	}
	
	public RandomDigit(XApplet applet, RandDigActionInterface digitAction, long seed) {
		super(applet, digitAction, seed);
	}
	
	public void corePaint(Graphics g) {
		int lowStep = currentFrame / kFramesPerDigit;
		int finalStep = finalFrame / kFramesPerDigit;
		double lowDigit = findMainDigit(lowStep, finalStep);
		double highDigit = findMainDigit(lowStep + 1, finalStep);
		if (highDigit < lowDigit)
			highDigit += (maxDigit + 1);
		double proportion = (currentFrame % kFramesPerDigit) / (double)kFramesPerDigit;
		double displayDigit = lowDigit * (1.0 - proportion) + highDigit * proportion;
		
		int lowInt = (int)Math.floor(displayDigit);
		int highInt = lowInt + 1;
		if (highInt > maxDigit)
			highInt = 0;
		
		proportion = displayDigit - lowInt;
		int offset = (int)(proportion * DigitImages.kDigitHeight);
		
		g.drawImage(DigitImages.digitImage[lowInt], kBorder, kBorder - offset, this);
		if (offset > 0)
			g.drawImage(DigitImages.digitImage[highInt], kBorder,
																				kBorder + DigitImages.kDigitHeight - offset, this);
		for (int i=0 ; i<kBorder ; i++)
			g.drawRect(i, i, DigitImages.kDigitWidth + 2 * (kBorder - i) - 1,
																	DigitImages.kDigitHeight + 2 * (kBorder - i) - 1);
	}
	
	private double findMainDigit(int step, int finalStep) {
		if (step > finalStep)
			step = finalStep;
		double digit;
		if (step < kLeadIn.length)
			digit = oldDigit + kLeadIn[step];
		else if (step > (finalStep - kLeadIn.length))
			digit = currentDigit - kLeadIn[finalStep - step];
		else
			digit = oldDigit + 2.0 + (step - kLeadIn.length);

		while (digit < 0.0)
			digit += (maxDigit + 1);
		while (digit >= maxDigit + 1)
			digit -= (maxDigit + 1);
			
		return digit;
	}
	
//---------------------------------------------------------------------
	
	protected void setupNewDigit() {
		oldDigit = currentDigit;
		currentDigit = generator.generateOne();
		
		int mainSteps = currentDigit - oldDigit - 3;
		if (mainSteps < 1)
			mainSteps += maxDigit + 1	;
		mainSteps += (maxDigit + 1) + 2 * kLeadIn.length;
		finalFrame = (mainSteps - 1) * kFramesPerDigit;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		return new Dimension(DigitImages.kDigitWidth + 2 * kBorder,
																				DigitImages.kDigitHeight + 2 * kBorder);
	}
}