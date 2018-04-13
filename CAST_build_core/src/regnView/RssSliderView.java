package regnView;

import java.awt.*;

import dataView.*;
import axis.*;

import regn.*;


public class RssSliderView extends DataView {
//	static final public String RSS_SLIDER_VIEW = "rssSlider";
	
	static final private Color kFillColor = Color.red;
	static final private Dimension kMinSize = new Dimension(20, 20);
	static final private int kRightMargin = 5;
	
	private String xKey, yKey, modelKey;
	private boolean straightNotInv;
	private NumCatAxis axis;
	private int ssqDecimals;
	
	public RssSliderView(DataSet theData, XApplet applet, String xKey, String yKey, String modelKey,
						boolean straightNotInv, NumCatAxis axis, int ssqDecimals) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.straightNotInv = straightNotInv;
		this.axis = axis;
		this.ssqDecimals = ssqDecimals;
	}
	
	public void paintView(Graphics g) {
		TwoWayModel model = (TwoWayModel)getVariable(modelKey);
		ValueEnumeration xe = ((NumVariable)getData().getVariable(xKey)).values();
		ValueEnumeration ye = ((NumVariable)getData().getVariable(yKey)).values();
		double ssq = 0.0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double x = xe.nextDouble();
			double y = ye.nextDouble();
			double resid = straightNotInv ? (y - model.predict(x, true))
										 :	(x - model.predict(y, false));
			ssq += resid * resid;
		}
		
		int zeroPos = axis.numValToRawPosition(0.0);
		int ht = getSize().height;
		Point p0 = translateToScreen(zeroPos, ht, null);
		
		g.setColor(kFillColor);
		if (Double.isInfinite(ssq)) {
			Point p1 = translateToScreen(zeroPos, 0, null);
			g.fillRect(p0.x, p0.y, (getSize().width - p0.x), (p1.y - p0.y));
		}
		else
			try {
				int ssqPos = axis.numValToPosition(ssq);
				Point p1 = translateToScreen(ssqPos, 0, null);
				g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
			} catch (AxisException e) {
				if (e.axisProblem == AxisException.TOO_HIGH_ERROR) {
					Point p1 = translateToScreen(zeroPos, 0, null);
					g.fillRect(p0.x, p0.y, (getSize().width - p0.x), (p1.y - p0.y));
				}
			}
		
		g.setColor(getForeground());
		
		int baseline = (ht + g.getFontMetrics().getAscent()) / 2;
		Value ssqVal;
		if (Double.isInfinite(ssq))
			ssqVal = new LabelValue("Infinite");
		else
			ssqVal = new NumValue(ssq, ssqDecimals);
		ssqVal.drawLeft(g, getSize().width - kRightMargin, baseline);
	}
	
	public Dimension getMinimumSize() {
		return kMinSize;
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
