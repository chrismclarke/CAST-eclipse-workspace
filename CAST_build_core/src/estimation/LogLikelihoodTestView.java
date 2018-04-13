package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import formula.*;


public class LogLikelihoodTestView extends LogLikelihoodView {
	static final private Color kParamColor = Color.blue;
	static final private Color kMleColor = Color.red;
	static final private Color kPValueSigColor = Color.red;
	static final private Color kPValueNonsigColor = new Color(0x009900);
	static final private Color kSigBackgroundColor = new Color(0xFFEEEE);
	static final private int kArrowSize = 4;
	
	private double mle;
	private int likelihoodDecimals, pValueDecimals;
	private double sigLevel = 1.1;	//	default is for all p-values drawn in red
	
	public LogLikelihoodTestView(DataSet theData, XApplet applet, String distnKey,
													CoreLikelihoodFinder likelihoodFinder, HorizAxis paramAxis,
													VertAxis likelihoodAxis, double mle, int likelihoodDecimals,
													int pValueDecimals) {
		super(theData, applet, distnKey, likelihoodFinder, paramAxis, likelihoodAxis);
		setViewBorder(new Insets(0, 0, 0, 0));
		this.mle = mle;
		this.likelihoodDecimals = likelihoodDecimals;
		this.pValueDecimals = pValueDecimals;
	}
	
	public void setSigLevel(double sigLevel) {
		this.sigLevel = sigLevel;
	}
	
	protected void drawBackground(Graphics g) {
		try {
			double maxLikelihood = getLikelihood(mle);		//	actually the log-likelihood
			int mlePos = paramAxis.numValToPosition(mle);
//			int maxLikelihoodPos = likelihoodAxis.numValToPosition(maxLikelihood);
//			Point pMle = translateToScreen(mlePos, maxLikelihoodPos, null);
			
			if (sigLevel < 0.5) {
				double likelihoodCutoff = maxLikelihood - Chi2Table.chi2Quant(1 - sigLevel, 1);
				g.setColor(kSigBackgroundColor);
				int cutoffPos = likelihoodAxis.numValToPosition(likelihoodCutoff);
				Point pCutoff = translateToScreen(mlePos, cutoffPos, null);
				g.fillRect(0, 0, getSize().width, pCutoff.y);
			}
		} catch (AxisException e) {
		}
	}
	
	public void drawExtras(Graphics g, double minX, double maxX) {
		double selectedParam = getParam();
		double likelihood = getLikelihood(selectedParam);		//	actually the log-likelihood
		
		try {
			int paramPos = paramAxis.numValToPosition(selectedParam);
			int likelihoodPos = likelihoodAxis.numValToPosition(likelihood);
			Point pOnCurve = translateToScreen(paramPos, likelihoodPos, null);
			
			double maxLikelihood = getLikelihood(mle);		//	actually the log-likelihood
			int mlePos = paramAxis.numValToPosition(mle);
			int maxLikelihoodPos = likelihoodAxis.numValToPosition(maxLikelihood);
			Point pMle = translateToScreen(mlePos, maxLikelihoodPos, null);
			
			g.setColor(kParamColor);
			g.drawLine(pOnCurve.x, 0, pOnCurve.x, getSize().height);
			g.drawLine(0, pOnCurve.y, getSize().width, pOnCurve.y);
			
			g.setColor(kMleColor);
			g.drawLine(pMle.x, pMle.y, pMle.x, pOnCurve.y);
			g.drawLine(pMle.x - 1, pMle.y + 1, pMle.x - 1, pOnCurve.y - 1);
			g.drawLine(pMle.x + 1, pMle.y + 1, pMle.x + 1, pOnCurve.y - 1);
			
			g.drawLine(pMle.x - 1, pMle.y + 1, pMle.x - 1 - kArrowSize, pMle.y + 1 + kArrowSize);
			g.drawLine(pMle.x - 1, pOnCurve.y - 1, pMle.x - 1 - kArrowSize, pOnCurve.y - 1 - kArrowSize);
			
			g.drawLine(pMle.x + 1, pMle.y + 1, pMle.x + 1 + kArrowSize, pMle.y + 1 + kArrowSize);
			g.drawLine(pMle.x + 1, pOnCurve.y - 1, pMle.x + 1 + kArrowSize, pOnCurve.y - 1 - kArrowSize);
			
			NumValue diff = new NumValue(maxLikelihood - likelihood, likelihoodDecimals);
			g.setFont(getApplet().getBigBoldFont());
			int ascent = g.getFontMetrics().getAscent();
			int baseline = (pMle.y + pOnCurve.y + ascent) / 2;
			diff.drawRight(g, pMle.x + kArrowSize + 2, baseline);
			
			double chiSqr = 2 * (maxLikelihood - likelihood);
			NumValue pValue = new NumValue(1.0 - Chi2Table.cumulative(chiSqr, 1), pValueDecimals);
			g.setColor(pValue.toDouble() < sigLevel ? kPValueSigColor : kPValueNonsigColor);
			g.setFont(getApplet().getBigBigBoldFont());
			baseline = getSize().height - 10;
			String pValueString = getApplet().translate("p-value") + MText.expandText("  =  P(#chi##sup2# #ge# 2#times#")
																	+ diff.toString() + ")  =  " + pValue.toString();
			FontMetrics fm = g.getFontMetrics();
			int pValWidth = fm.stringWidth(pValueString);
			g.drawString(pValueString, (getSize().width - pValWidth) / 2, baseline);
		} catch (AxisException e) {
		}
	}
}