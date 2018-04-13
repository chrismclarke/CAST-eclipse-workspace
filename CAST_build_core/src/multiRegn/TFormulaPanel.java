package multiRegn;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;
import images.*;



public class TFormulaPanel extends Binary {
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final public Color kNumerColor = Color.black;
	static final public Color kDenomColor = new Color(0x006600);
	static final public Color kTColor = new Color(0xCC0000);
	
	static private FormulaPanel rightPanel(DataSet data,
								String numerKey, String denomKey, String tKey,
								NumValue maxNumer, NumValue maxDenom, NumValue maxT,
								boolean boxResult, FormulaContext context) {
		FormulaContext denomContext = context.getRecoloredContext(kDenomColor);
		FormulaContext numerContext = context.getRecoloredContext(kNumerColor);
		FormulaContext tContext = context.getRecoloredContext(kTColor);
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		
		XApplet applet = context.getApplet();
		
		OneValueView denomView = new OneValueView(data, denomKey, applet, maxDenom);
		denomView.setNameDraw(false);
		denomView.setHighlightSelection(false);
		denomView.unboxValue();
		SummaryValue denom = new SummaryValue(denomView, denomContext);
		
		OneValueView numerView = new OneValueView(data, numerKey, applet, maxNumer);
		numerView.setNameDraw(false);
		numerView.setHighlightSelection(false);
		numerView.unboxValue();
		SummaryValue numer = new SummaryValue(numerView, numerContext);
		
		OneValueView tView = new OneValueView(data, tKey, applet, maxT);
		tView.setNameDraw(false);
		if(!boxResult)
			tView.unboxValue();
		SummaryValue t = new SummaryValue(tView, tContext);
		
		return new Binary(Binary.EQUALS, new Ratio(numer, denom, blackContext),
								t, tContext);
	}
	
	static private Picture tPicture(String tGifFile, int ascent, int descent,
																									int width, FormulaContext context) {
		Image image = CoreImageReader.getImage(tGifFile);
		MediaTracker tracker = new MediaTracker(context.getApplet());
		tracker.addImage(image, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		
		return new Picture(image, width, ascent, descent, context);
	}
	
	public TFormulaPanel(DataSet data, String numerKey, String denomKey,
									String tKey, NumValue maxNumer, NumValue maxDenom, NumValue maxT,
									String tGifFile, int ascent, int descent, int width,
									boolean boxResult, FormulaContext context) {
		super(Binary.EQUALS, tPicture(tGifFile, ascent, descent, width, context),
						rightPanel(data, numerKey, denomKey, tKey, maxNumer, maxDenom,
						maxT, boxResult, context), context.getRecoloredContext(kTColor));
	}
	
	public TFormulaPanel(DataSet data, String numerKey, String denomKey,
									String tKey, NumValue maxNumer, NumValue maxDenom, NumValue maxT,
									String tGifFile, int ascent, int descent, int width, FormulaContext context) {
		this(data, numerKey, denomKey, tKey, maxNumer, maxDenom, maxT,
																		tGifFile, ascent, descent, width, true, context);
	}
}