package test;

import java.awt.*;

import dataView.*;
import cat.*;
import qnUtils.*;


public class PieTestPropnView extends PieView {
//	static final public String PIE_TEST = "pieTest";
	
	static final private int kBorder = 15;
	static final private int kParamGap = kBorder + 1;
	
	private HypothesisTest test;
	private int bigRadius;
	
	public PieTestPropnView(DataSet theData, XApplet applet, String catKey, int dragType,
																																				HypothesisTest test) {
		super(theData, applet, catKey, dragType);
		this.test = test;
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (super.initialise(variable, g)) {
			bigRadius = radius;
			setRadius(bigRadius - kBorder);
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		g.setColor(Color.blue);
		int targetAngle = (int)Math.round(90 - test.getTestValue().toDouble() * 360);
		if (targetAngle < 0)
			targetAngle += 360;
		drawRadius(g, targetAngle, bigRadius);
		
		Dimension valueSize = test.getSize(g, HypothesisTest.PARAM_BOUNDARY_DRAW);
		FontMetrics fm = g.getFontMetrics();
		int textWidth = valueSize.width + fm.stringWidth("?");
		
		int textRadius = (bigRadius + radius) / 2;
		int targetEndX = cx + getXShift(targetAngle, textRadius);
		int targetEndY = cy - getYShift(targetAngle, textRadius);
		
		int baseline = targetEndY + test.getBaselineFromTop(g, HypothesisTest.PARAM_BOUNDARY_DRAW) / 2;
		int horizPos = (targetAngle <= 90 || targetAngle >= 270) ? targetEndX + kParamGap
																		: targetEndX - kParamGap - textWidth;
		test.paintBlue(g, horizPos, baseline, true, HypothesisTest.PARAM_BOUNDARY_DRAW, this);
		
		g.setFont(getApplet().getBigBoldFont());
		g.drawString("?", horizPos + valueSize.width, baseline);
		g.setFont(getFont());
		
		
		g.setColor(getForeground());
		
		super.paintView(g);
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		int dx = x - cx;
		int dy = y - cy;
		if (dx * dx + dy * dy <= radius * radius) {
			double proportion = 0;
			if (dx == 0)
				proportion = (dy > 0) ? 0.5 : 0.0;
			else {
				proportion = 0.25 + Math.atan((double)dy / dx) / (2.0 * Math.PI);
				if (dx <= 0.0)
					proportion += 0.5;
				if (proportion >= 1.0)
					proportion -= 1.0;
			}
			int previousCum = 0;
			double hitCount = proportion * totalCount;
			for (int i=0 ; i<cumCount.length ; i++) {
				int thisCum = cumCount[i];
				if (hitCount <= thisCum)
					return new CatPosInfo(i, thisCum - hitCount < hitCount - previousCum);
				previousCum = thisCum;
			}
		}
		
		return null;
	}
}