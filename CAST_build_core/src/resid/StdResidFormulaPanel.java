package resid;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;
import images.*;



public class StdResidFormulaPanel extends Binary {
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final public Color kSColor = new Color(0x006600);
	static final public Color kResColor = new Color(0xCC0000);
	
	static private Image stdResImage, delResImage;
	
	private Picture leftPicture;
	private OneValueView sView, stdResView;
	
	public StdResidFormulaPanel(DataSet data, String residKey,
								String leverageKey, String sKey, String stdResKey, NumValue maxResid,
								NumValue maxAntileverage, NumValue maxS, NumValue maxStdRes,
								String stdResGifFile, String delResGifFile, int ascent, int descent, int width,
								FormulaContext context) {
		super(Binary.EQUALS, context);
		
		FormulaPanel left = resPicture(stdResGifFile, delResGifFile, ascent, descent, width, context);
		FormulaPanel right = rightPanel(data, residKey, leverageKey, sKey, stdResKey,
																				maxResid, maxAntileverage, maxS, maxStdRes, context);
		addSubFormulae(left, right);
	}
	
	private FormulaPanel formulaPanel(DataSet data, String residKey, String leverageKey, String sKey,
														NumValue maxResid, NumValue maxAntileverage, NumValue maxS,
														FormulaContext context) {
		XApplet applet = context.getApplet();
		FormulaContext resContext = context.getRecoloredContext(kResColor);
		FormulaContext sContext = context.getRecoloredContext(kSColor);
		
		OneValueView resView = new OneValueView(data, residKey, applet, maxResid);
		resView.setNameDraw(false);
		resView.setHighlightSelection(false);
		SummaryValue res = new SummaryValue(resView, resContext);
		
		OneValueView antiLevView = new OneValueView(data, leverageKey, applet, maxAntileverage);
		antiLevView.setNameDraw(false);
		antiLevView.setHighlightSelection(false);
		SummaryValue antiLev = new SummaryValue(antiLevView, context);
		
		sView = new OneValueView(data, sKey, applet, maxS);
		sView.setNameDraw(false);
		SummaryValue s = new SummaryValue(sView, sContext);
		
		return new Binary(Binary.TIMES, res, new Ratio(antiLev, s, context), context);
	}
	
	private FormulaPanel rightPanel(DataSet data, String residKey, String leverageKey, String sKey,
								String stdResKey, NumValue maxResid, NumValue maxAntileverage, NumValue maxS,
								NumValue maxStdRes, FormulaContext context) {
		XApplet applet = context.getApplet();
		FormulaContext resContext = context.getRecoloredContext(kResColor);
		
		FormulaPanel formula = formulaPanel(data, residKey, leverageKey, sKey,
																							maxResid, maxAntileverage, maxS, context);
		
		stdResView = new OneValueView(data, stdResKey, applet, maxStdRes);
		stdResView.setNameDraw(false);
		stdResView.setHighlightSelection(false);
		SummaryValue stdRes = new SummaryValue(stdResView, resContext);
		
		return new Binary(Binary.EQUALS, formula, stdRes, context);
	}
	
	private Picture resPicture(String stdResGifFile, String delResGifFile, int ascent,
																							int descent, int width, FormulaContext context) {
		XApplet applet = context.getApplet();
		
			stdResImage = CoreImageReader.getImage(stdResGifFile);
			delResImage = CoreImageReader.getImage(delResGifFile);
		MediaTracker tracker = new MediaTracker(applet);
		tracker.addImage(stdResImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
		leftPicture = new Picture(stdResImage, width, ascent, descent, context);
		
		return leftPicture;
	}
	
	public void changeResidType(String stdResKey, String sKey, boolean useDeletedS) {
		stdResView.setVariableKey(stdResKey);
		sView.setVariableKey(sKey);
		sView.setHighlightSelection(useDeletedS);
		leftPicture.setImage(useDeletedS ? delResImage : stdResImage);
		leftPicture.repaint();
	}
}