package distribution;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DiscreteCdfView extends DiscreteProbView {
	static final private int kArrowHead = 4;
	
	protected NumCatAxis probAxis = null;
	
	public DiscreteCdfView(DataSet theData, XApplet applet, String distnKey,
																										NumCatAxis countAxis, int dragType) {
		this(theData, applet, distnKey, null, countAxis, dragType);
	}
	
	public DiscreteCdfView(DataSet theData, XApplet applet, String distnKey,
																	NumCatAxis pAxis, NumCatAxis countAxis, int dragType) {
																									//		pAxis only used for binomial distn
		super(theData, applet, distnKey, null, pAxis, countAxis, dragType);
	}
	
	public void setProbAxis(NumCatAxis probAxis) {
		this.probAxis = probAxis;
	}
	
	public void paintView(Graphics g) {
		drawTitleString(g);
		
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		double probFactor = y.getProbFactor();
		
		int height = getHeight();
		Point p0 = null;
		Point p1 = null;
		
		int lastHorizPos = 0;
		int lastCumPos = 0;
		if (probAxis != null)
			try {
				lastCumPos = probAxis.numValToPosition(0.0);
			} catch (AxisException e) {
			}
		p0 = translateToScreen(lastHorizPos, lastCumPos, p0);
		p0.y ++;
		
		double cumProb = 0;
		int maxY = getMaxCount(y);
		
		for (int i=0 ; i<=maxY ; i++)
			try {
				int xPos = (pAxis != null) ? pAxis.numValToPosition(((double)i) / maxY) : countAxis.numValToPosition(i);
				double prob = y.getScaledProb(i) * probFactor;
				
				p1 = translateToScreen(xPos, lastCumPos, p1);
				p1.y ++;
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				Point temp = p1; p1 = p0; p0 = temp;
				
				cumProb += prob;
				int cumPos = (probAxis == null) ? (int)Math.round(height * cumProb)
																								: probAxis.numValToPosition(cumProb);
				p1 = translateToScreen(xPos, cumPos, p1);
				p1.y ++;
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				if ((int)Math.floor(y.getMaxSelection()) == i) {
					g.setColor(Color.red);
					g.drawLine(p0.x, p0.y, p0.x, getSize().height);
					g.drawLine(0, p1.y, p1.x, p1.y);
					g.drawLine(0, p1.y, kArrowHead, p1.y - kArrowHead);
					g.drawLine(0, p1.y, kArrowHead, p1.y + kArrowHead);
					g.setColor(getForeground());
				}
				
				temp = p1; p1 = p0; p0 = temp;
				lastHorizPos = xPos;
				lastCumPos = cumPos;
			} catch (AxisException e) {
			}
		p1 = translateToScreen(getSize().width, lastCumPos, p1);
		p1.y ++;
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
	}
	
	protected int getMaxCount(DiscreteDistnVariable y) {
		int maxY = (int)Math.round(Math.floor(countAxis.maxOnAxis));
		if (y instanceof BinomialDistnVariable)
			maxY = ((BinomialDistnVariable)y).getCount();
		else if (y instanceof BetaBinomDistnVariable)
			maxY = ((BetaBinomDistnVariable)y).getN();
		return maxY;
	}
}