package stdError;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class BinomAndNormalView extends DataView {
	static final private Color kBinomialColor = new Color(0x990000);
	
	private BackgroundNormalArtist backgroundDrawer;
	
	private String binomKey;
	private HorizAxis errorAxis;
	
	private Color binomialColor = kBinomialColor;
	
	public BinomAndNormalView(DataSet theData, XApplet applet,
					String binomKey, String normalKey, HorizAxis errorAxis) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.binomKey = binomKey;
		this.errorAxis = errorAxis;
		backgroundDrawer = new BackgroundNormalArtist(normalKey, theData);
		backgroundDrawer.setMaxDensityFactor(1.1);		//	same as BinomialDistnVariable.getMaxScaledProb()
	}
	
	public void setBinomialColor(Color c) {
		binomialColor = c;
	}
	
	public void setNormalColor(Color c) {
		backgroundDrawer.setFillColor(c);
	}
	
	public void paintView(Graphics g) {
		backgroundDrawer.paintDistn(g, this, errorAxis);
			
		Point topLeft = null;
		BinomialDistnVariable y = (BinomialDistnVariable)getVariable(binomKey);
		int n = y.getCount();
		double p = y.getProb();
		
		double maxProb = getMaxProb();
		
		int barSpacing = 0;
		try {
			int x0Pos = errorAxis.numValToPosition(0.0);
			int x1Pos = errorAxis.numValToPosition(1.0 / n);
			barSpacing = x1Pos - x0Pos;
		} catch (AxisException e) {
		}
		
		int halfBarWidth = (barSpacing >= 20) ? 2
								: (barSpacing >= 10) ? 1
								: 0;
		
		g.setColor(binomialColor);
		for (int i=0 ; i<=n ; i++)
			try {
				double px = ((double)i) / n;
				int x = errorAxis.numValToPosition(px - p);
				double prob = y.getScaledProb(i);
				int ht = (int)Math.round(getSize().height * prob / maxProb);
				topLeft = translateToScreen(x, ht, topLeft);
				
				g.fillRect(topLeft.x - halfBarWidth, topLeft.y + 1, 2 * halfBarWidth + 1,
																						getSize().height - topLeft.y - 1);
			} catch (AxisException e) {
			}
	}
	
	public double getMaxProb() {
		BinomialDistnVariable y = (BinomialDistnVariable)getVariable(binomKey);
		return y.getMaxScaledProb();
	}
	
//--------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}