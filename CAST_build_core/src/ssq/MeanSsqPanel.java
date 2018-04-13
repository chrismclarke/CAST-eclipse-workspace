package ssq;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class MeanSsqPanel extends Binary {
	static final public int POOLED = -1;
	
	static private FormulaPanel oneRatioPanel(DataSet data, String ssqKey, String dfKey,
																		NumValue maxSsq, NumValue maxDF, FormulaContext context) {
		XApplet applet = context.getApplet();
		
		OneValueView ssqView = new OneValueView(data, ssqKey, applet, maxSsq);
		ssqView.setNameDraw(false);
		ssqView.setCenterValue(true);
		ssqView.setHighlightSelection(false);
		SummaryValue ssq = new SummaryValue(ssqView, context);
		
		OneValueView dfView = new OneValueView(data, dfKey, applet, maxDF);
		dfView.setNameDraw(false);
		dfView.setCenterValue(true);
		dfView.setHighlightSelection(false);
		SummaryValue df = new SummaryValue(dfView, context);
		
		return new Ratio(ssq, df, context);
	}
	
	static private FormulaPanel multiRatioPanel(DataSet data, String[] ssqKey, String[] dfKey,
															NumValue maxSsq, NumValue maxDF, FormulaContext[] context,
															FormulaContext mainContext) {
		XApplet applet = mainContext.getApplet();
		
		FormulaPanel numerPanel = null;
		for (int i=0 ; i<ssqKey.length ; i++) {
			OneValueView ssqView = new OneValueView(data, ssqKey[i], applet, maxSsq);
			ssqView.setNameDraw(false);
			ssqView.setCenterValue(true);
			ssqView.setHighlightSelection(false);
			SummaryValue ssq = new SummaryValue(ssqView, context[i]);
			
			if (i == 0)
				numerPanel = ssq;
			else
				numerPanel = new Binary(Binary.PLUS, numerPanel, ssq, mainContext);
		}
		
		FormulaPanel denomPanel = null;
		for (int i=0 ; i<dfKey.length ; i++) {
			OneValueView dfView = new OneValueView(data, dfKey[i], applet, maxDF);
			dfView.setNameDraw(false);
			dfView.setCenterValue(true);
			dfView.setHighlightSelection(false);
			SummaryValue df = new SummaryValue(dfView, context[i]);
			
			if (i == 0)
				denomPanel = df;
			else
				denomPanel = new Binary(Binary.PLUS, denomPanel, df, mainContext);
		}
		
		return new Ratio(numerPanel, denomPanel, mainContext);
	}
	
	static private FormulaPanel resultPanel(DataSet data, FormulaPanel leftPanel,
												String mssqKey, NumValue maxMssq, FormulaContext context) {
		XApplet applet = context.getApplet();
		
		OneValueView mssqView = new OneValueView(data, mssqKey, applet, maxMssq);
		mssqView.setNameDraw(false);
		SummaryValue mssq = new SummaryValue(mssqView, context);
		
		return new Binary(Binary.EQUALS, leftPanel, mssq, context);
	}
	
	static private Picture s2Picture(int groupIndex, FormulaContext context) {
		SsqImages.loadSsq(context.getApplet());
		
		int width = (groupIndex == POOLED) ? SsqImages.kPooledVarSquaredWidth
																				: SsqImages.kGroupVarSquaredWidth;
		Image image = (groupIndex == POOLED) ? SsqImages.sPooledSqr : SsqImages.sSqr[groupIndex];
		
		return new Picture(image, width, SsqImages.kAscent, SsqImages.kDescent, context);
	}
	
	public MeanSsqPanel(DataSet data, String ssqKey, String dfKey,
									String mssqKey, NumValue maxSsq, NumValue maxDF, NumValue maxMssq,
									int groupIndex, FormulaContext context) {
		super(Binary.EQUALS, context);
		FormulaPanel ratio = oneRatioPanel(data, ssqKey, dfKey, maxSsq, maxDF, context);
		FormulaPanel result = resultPanel(data, ratio, mssqKey, maxMssq, context);
		addSubFormulae(s2Picture(groupIndex, context), result);
	}
	
	public MeanSsqPanel(DataSet data, String[] ssqKey, String[] dfKey, String totalSsqKey,
									String totalDfKey, String mssqKey, NumValue maxSsq, NumValue maxDF,
									NumValue maxMssq, FormulaContext[] context, FormulaContext mainContext) {
		super(Binary.EQUALS, mainContext);
		FormulaPanel multiRatio = multiRatioPanel(data, ssqKey, dfKey, maxSsq, maxDF, context, mainContext);
		FormulaPanel ratio = oneRatioPanel(data, totalSsqKey, totalDfKey, maxSsq, maxDF, mainContext);
		FormulaPanel leftPanel = new Binary(Binary.EQUALS, multiRatio, ratio, mainContext);
		FormulaPanel result = resultPanel(data, leftPanel, mssqKey, maxMssq, mainContext);
		addSubFormulae(s2Picture(POOLED, mainContext), result);
	}
	
}