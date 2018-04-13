package indicator;

import java.awt.*;

import dataView.*;
import models.*;


public class Type1SsqTableView extends Type3SsqTableView {
//	static final public String TYPE1_SSQ_VIEW = "type1Ssq";
	
	static final protected Color kTotalColor = new Color(0x660000);
	
	public Type1SsqTableView(DataSet theData, XApplet applet,
							String modelKey, String yKey, NumValue maxType3Ssq, NumValue maxDf,
							NumValue maxMeanSsq, NumValue maxF, String[] termName, int[] nxForTerm,
							int[][] hierarchy) {
		super(theData, applet, modelKey, yKey, maxType3Ssq, maxDf,
											maxMeanSsq, maxF, termName, nxForTerm, hierarchy);
	}
	
	public Type1SsqTableView(DataSet theData, XApplet applet,
							String modelKey, String yKey, NumValue maxType3Ssq, NumValue maxDf,
							NumValue maxMeanSsq, NumValue maxF) {
		this(theData, applet, modelKey, yKey, maxType3Ssq, maxDf, maxMeanSsq, maxF, null, null, null);
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		tableHeight += ascent + descent + kLineGap;
	}
	
	protected Value getSsqHeading() {
		LabelValue kType1SsqLabel = new LabelValue(getApplet().translate("Seq ssq"));
		return kType1SsqLabel;
	}
	
	protected int drawExtraTableRow(Graphics g, SSComponent residSsqComponent,
										SSComponent totalSsqComponent, int termBaseline, int paramNameLeft,
										int type3SsqRight, int dfRight, int meanSsqRight, int lineGap) {
		drawNontestRow(g, residSsqComponent.ssq, residSsqComponent.df, getApplet().translate("Residual"),
											termBaseline, paramNameLeft, type3SsqRight, dfRight, meanSsqRight,
											kResidColor);
		return termBaseline + (ascent + descent + lineGap) + kTopBottomBorder;
	}
	
	protected int drawRowUnderTable(Graphics g, SSComponent residSsqComponent,
										SSComponent totalSsqComponent, int termBaseline, int paramNameLeft,
										int type3SsqRight, int dfRight, int meanSsqRight, int lineGap) {
		drawNontestRow(g, totalSsqComponent.ssq, totalSsqComponent.df, getApplet().translate("Total"),
											termBaseline, paramNameLeft, type3SsqRight, dfRight, meanSsqRight,
											kTotalColor);
		return termBaseline + (ascent + descent + lineGap);
	}
	
	protected SSComponent nextExplainedSsq(MultipleRegnModel model, double[] modelConstraints,
										double[] seqConstraints, int startCoeffIndex, int nCoeffInTerm,
										SSComponent residSsqComponent) {
		SSComponent oldSsqComponent = model.getResidSsqComponent(yKey, seqConstraints);
		
		for (int j=0 ; j<nCoeffInTerm ; j++)			//	add variable to model
			seqConstraints[startCoeffIndex + j] = Double.NaN;
		
		SSComponent ssqComponent = model.getResidSsqComponent(yKey, seqConstraints);
		
		ssqComponent.ssq = oldSsqComponent.ssq - ssqComponent.ssq;
		ssqComponent.df = oldSsqComponent.df - ssqComponent.df;
			
		return ssqComponent;
	}
}