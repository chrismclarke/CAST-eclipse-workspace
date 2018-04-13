package dotPlot;

import java.awt.*;

import dataView.*;
import axis.*;

import coreGraphics.*;


public class StackedWithColorsView extends StackedDotPlotView {
	
	private int redIndex, blueIndex;
	
	public StackedWithColorsView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																																	int redIndex, int blueIndex) {
		super(theData, applet, theAxis, null, false);
		this.redIndex = redIndex;
		this.blueIndex = blueIndex;
	}
	
	protected void fiddleColor(Graphics g, int index) {
		if (index == redIndex)
			g.setColor(Color.red);
		else if (index == blueIndex)
			g.setColor(Color.blue);
		else
			g.setColor(Color.black);
	}
}