package dynamic;

import java.awt.*;

import dataView.*;
import axis.*;


public class BarSizeView extends PieSizeView {
//	static public final String BAR_SIZE_VIEW = "barSizeView";
	
	static final private int kHalfBarWidth = 8;
	
	private HorizAxis catAxis;
	private VertAxis freqAxis;
	
	public BarSizeView(DataSet theData, XApplet applet, String yKey, Color[] catColors,
																							HorizAxis catAxis, VertAxis freqAxis) {
		super(theData, applet, yKey, catColors);
		this.catAxis = catAxis;
		this.freqAxis = freqAxis;
	}
	
	private double barScaling() {
		return showOnlyProportions ? 100.0 / getTotal() : 1.0;
	}
	
	
	public void paintView(Graphics g) {
		initialise(g);
		
		double scaling = barScaling();
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		Point barTop = null;
		for (int i=0 ; i<nVals ; i++) {
			double y = yVar.doubleValueAt(i) * scaling;
			int xPos = catAxis.catValToPosition(i);
			int yPos = freqAxis.numValToRawPosition(y);
			g.setColor(catColors[i]);
			barTop = translateToScreen(xPos, yPos, barTop);
			g.fillRect(barTop.x - kHalfBarWidth, barTop.y, 2 * kHalfBarWidth, getSize().height - barTop.y);
			g.setColor(getForeground());
			g.drawRect(barTop.x - kHalfBarWidth, barTop.y, 2 * kHalfBarWidth, getSize().height - barTop.y);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
