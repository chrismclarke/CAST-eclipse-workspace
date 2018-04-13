package ssq;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;
import models.*;



public class RSquaredPanel extends Binary {
	
	static private FormulaPanel rightPanel(DataSet data, String explainedKey, String totalKey,
															String rSquaredKey, NumValue maxSsq, NumValue maxRSquared,
															FormulaContext context) {
		XApplet applet = context.getApplet();
		FormulaContext totalContext = context.getRecoloredContext(BasicComponentVariable.kTotalColor);
		FormulaContext explainedContext = context.getRecoloredContext(BasicComponentVariable.kExplainedColor);
		
		OneValueView totalView = new OneValueView(data, totalKey, applet, maxSsq);
		totalView.setNameDraw(false);
		totalView.setHighlightSelection(false);
		SummaryValue total = new SummaryValue(totalView, totalContext);
		
		OneValueView explainedView = new OneValueView(data, explainedKey, applet, maxSsq);
		explainedView.setNameDraw(false);
		explainedView.setHighlightSelection(false);
		SummaryValue explained = new SummaryValue(explainedView, explainedContext);
		
		OneValueView rSquaredView = new OneValueView(data, rSquaredKey, applet, maxRSquared);
		rSquaredView.setNameDraw(false);
		SummaryValue rSquared = new SummaryValue(rSquaredView, context);
		
		return new Binary(Binary.EQUALS, new Ratio(explained, total, context), rSquared, context);
	}
	
	static private Picture r2Picture(boolean explNotGroups, FormulaContext context) {
		int ascent = AnovaImages.kRSquaredAscent;
		int descent = AnovaImages.kRSquaredDescent;
		int width = explNotGroups ? AnovaImages.kRSquaredWidth : AnovaImages.kRSquared2Width;
		Image image = explNotGroups ? AnovaImages.rSquared : AnovaImages.rSquared2;
		
		return new Picture(image, width, ascent, descent, context);
	}
	
	public RSquaredPanel(DataSet data, String explainedKey, String totalKey,
															String rSquaredKey, NumValue maxSsq, NumValue maxRSquared,
															boolean explNotGroups, FormulaContext context) {
		super(Binary.EQUALS, r2Picture(explNotGroups, context),
						rightPanel(data, explainedKey, totalKey, rSquaredKey, maxSsq, maxRSquared, context),
						context);
	}
}