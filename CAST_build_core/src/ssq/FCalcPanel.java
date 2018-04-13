package ssq;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;
import images.*;



public class FCalcPanel extends Binary {
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static private FormulaPanel rightPanel(DataSet data,
							String numerKey, String denomKey, String fKey, NumValue maxMsq, NumValue maxF,
							Color numerColor, Color denomColor, boolean boxNumDenom, boolean boxResult,
							FormulaContext context) {
		FormulaContext numerContext = context.getRecoloredContext(numerColor);
		FormulaContext denomContext = context.getRecoloredContext(denomColor);
		XApplet applet = context.getApplet();
		
		OneValueView denomView = new OneValueView(data, denomKey, applet, maxMsq);
		denomView.setNameDraw(false);
		denomView.setHighlightSelection(false);
		if (!boxNumDenom)
			denomView.unboxValue();
		SummaryValue denom = new SummaryValue(denomView, denomContext);
		
		OneValueView numerView = new OneValueView(data, numerKey, applet, maxMsq);
		numerView.setNameDraw(false);
		numerView.setHighlightSelection(false);
		if (!boxNumDenom)
			numerView.unboxValue();
		SummaryValue numer = new SummaryValue(numerView, numerContext);
		
		OneValueView fView = new OneValueView(data, fKey, applet, maxF);
		fView.setNameDraw(false);
		if (!boxResult)
			fView.unboxValue();
		SummaryValue fValue = new SummaryValue(fView, context);
		
		return new Binary(Binary.EQUALS, new Ratio(numer, denom, context), fValue, context);
	}
	
	static private Picture fPicture(FormulaContext context) {
		int ascent = AnovaImages.kSsqAscent;
		int descent = AnovaImages.kSsqDescent;
		int width = AnovaImages.kFWidth;
		Image image = AnovaImages.f;
		
		return new Picture(image, width, ascent, descent, context);
	}
	
	static private Picture fPicture(String fileName, int ascent, int descent,
																							int width, FormulaContext context) {
			Image image = CoreImageReader.getImage(fileName);
		MediaTracker tracker = new MediaTracker(context.getApplet());
		tracker.addImage(image, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		
		return new Picture(image, width, ascent, descent, context);
	}
	
	public FCalcPanel(DataSet data, String numerKey, String denomKey,
									String fKey, NumValue maxMsq, NumValue maxF, Color numerColor,
									Color denomColor, boolean boxNumDenom, boolean boxResult,
									String fGifFile, int fAscent, int fDescent, int fWidth, FormulaContext context) {
		super(Binary.EQUALS, fPicture(fGifFile, fAscent, fDescent, fWidth, context),
								rightPanel(data, numerKey, denomKey, fKey, maxMsq, maxF, numerColor,
																							denomColor, boxNumDenom, boxResult, context),
								context);
	}
	
	public FCalcPanel(DataSet data, String numerKey, String denomKey,
												String fKey, NumValue maxMsq, NumValue maxF, Color numerColor,
												Color denomColor, FormulaContext context) {
		super(Binary.EQUALS, fPicture(context),
								rightPanel(data, numerKey, denomKey, fKey, maxMsq, maxF, numerColor,
															denomColor, true, true, context), context);
	}
}