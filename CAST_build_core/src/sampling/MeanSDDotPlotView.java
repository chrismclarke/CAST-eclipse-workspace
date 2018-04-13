package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class MeanSDDotPlotView extends StackedDotPlotView {
//	static public final String MEAN_SD_DOTPLOT = "MeanSDDotPlot";
	
	static final private int kArrowHeight = 15;
	static final private int kArrowHead = 4;
	
	private boolean allowDrawMeanSd = true;
	
	private int meanPos, sdPos;
	private boolean canDrawMean, canDrawSD;
	
	public MeanSDDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																											String freqKey, boolean popNotSamp) {
		super(theData, applet, theAxis, freqKey, popNotSamp);
	}
	
	public MeanSDDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		this(theData, applet, theAxis, null, false);
	}
	
	public void setAllowDrawMeanSD(boolean allowDrawMeanSd) {
		this.allowDrawMeanSd = allowDrawMeanSd;
	}
	
	protected boolean initialise() {
		if (super.initialise()) {
			NumVariable y = getNumVariable();
			FreqVariable f = (freqKey == null) ? null : (FreqVariable)getVariable(freqKey);
			
			double mean = SummaryView.evaluateStatistic((Variable)y, f, null, popNotSamp, SummaryView.MEAN);
			canDrawMean = !Double.isNaN(mean);
			if (canDrawMean)
				meanPos = axis.numValToRawPosition(mean);
			
			double sd = SummaryView.evaluateStatistic((Variable)y, f, null, popNotSamp, SummaryView.SD);
			canDrawSD = !Double.isNaN(sd);
			if (canDrawMean && canDrawSD)
				sdPos = axis.numValToRawPosition(mean + sd);
			
			return true;
		}
		else
			return false;
	}
	
	protected void paintBackground(Graphics g) {
		int arrowBottom = Math.min(getMaxStackHeight(), getDisplayWidth() - kArrowHeight);
		Point p1 = null;
		Point p2 = null;
		
		if (allowDrawMeanSd && canDrawSD) {
			g.setColor(Color.red);
			p1 = translateToScreen(meanPos, arrowBottom + kArrowHeight / 2, p1);
			p2 = translateToScreen(sdPos, arrowBottom + kArrowHeight / 2, p2);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = translateToScreen(sdPos - kArrowHead, arrowBottom + kArrowHeight / 2 + kArrowHead, p1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = translateToScreen(sdPos - kArrowHead, arrowBottom + kArrowHeight / 2 - kArrowHead, p1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		
		if (allowDrawMeanSd && canDrawMean) {
			g.setColor(Color.blue);
			p1 = translateToScreen(meanPos, arrowBottom + kArrowHeight, p1);
			p2 = translateToScreen(meanPos, arrowBottom, p2);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = translateToScreen(meanPos - kArrowHead, arrowBottom + kArrowHead, p1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = translateToScreen(meanPos + kArrowHead, arrowBottom + kArrowHead, p1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}
	
	protected boolean canDrag() {
		return freqKey != null || !popNotSamp;
	}
}