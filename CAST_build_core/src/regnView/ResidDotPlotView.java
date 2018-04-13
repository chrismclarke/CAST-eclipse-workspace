package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ResidDotPlotView extends DotPlotView {
//	static public final String RESID_DOTPLOT = "residDotPlot";
	
	public ResidDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis, 1.0);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		NumVariable variable = getNumVariable();
		Point zeroP = null;
		
		try {
			int horizPos = axis.numValToPosition(0.0);
			zeroP = translateToScreen(horizPos, 0, null);
		} catch (AxisException e) {
		}
		
		g.setColor(Color.lightGray);
		if (vertNotHoriz)
			g.drawLine(0, zeroP.y, getSize().width, zeroP.y);
		else
			g.drawLine(zeroP.x, 0, zeroP.x, getSize().height);
		
		g.setColor(Color.red);
		ValueEnumeration e = variable.values();
		int index = 0;
		Point p = null;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			p = getScreenPoint(index, nextVal, p);
			
			if (p != null) {
				if (vertNotHoriz)
					g.drawLine(p.x, p.y, p.x, zeroP.y);
				else
					g.drawLine(p.x, p.y, zeroP.x, p.y);
			}
			
			index++;
		}
	
	}
	
}
	
