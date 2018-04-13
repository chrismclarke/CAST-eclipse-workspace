package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;

import distribution.*;


public class BarChartSelected extends DiscreteProbView {
	private int x;
	
	public BarChartSelected(DataSet theData, XApplet applet, String distnKey, HorizAxis countAxis, int x) {
		super(theData, applet, distnKey, null, countAxis, DiscreteProbView.NO_DRAG);
		this.x = x;
		setForceZeroOneAxis(true);
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		g.setColor(Color.red);
		BinomialDistnVariable distn = (BinomialDistnVariable)getData().getVariable(distnKey);
		
		try {
			double probX = distn.getProbFactor() * distn.getScaledProb(x);
			int ht = (int)Math.round(getSize().height * probX);
			int xPos = countAxis.numValToPosition(x);
			Point barTop = translateToScreen(xPos, ht, null);
			g.drawLine(0, barTop.y, barTop.x, barTop.y);
		} catch (AxisException e) {
		}
	}
}