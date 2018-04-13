package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;

import distribution.*;


public class DataDensityView extends ContinuousProbView {
	static final private Color kDataColor = new Color(0xCC0000);
	static final private int kMaxWideBarCount = 12;
	
	private String dataKey;
	
	public DataDensityView(DataSet theData, XApplet applet, String distnKey, String dataKey,
												HorizAxis horizAxis, VertAxis densityAxis) {
		super(theData, applet, distnKey, horizAxis, densityAxis);
		this.dataKey = dataKey;
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		ContinDistnVariable distnVar = (ContinDistnVariable)getVariable(distnKey);
		double densityFactor = distnVar.getDensityFactor();
		
		g.setColor(kDataColor);
		NumVariable xVar = (NumVariable)getVariable(dataKey);
		boolean narrowBars = xVar.noOfValues() > kMaxWideBarCount;
		ValueEnumeration xe = xVar.values();
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			double density = getDensity(x, distnVar, densityFactor);
			try {
				int xPos = horizAxis.numValToPosition(x);
				int dPos = densityAxis.numValToPosition(density);
				Point pTop = translateToScreen(xPos, dPos, null);
				Point pBottom = translateToScreen(xPos, -1, null);
				if (narrowBars)
					g.drawLine(pTop.x, pTop.y, pTop.x, pBottom.y);
				else
					g.fillRect(pTop.x - 1, pTop.y, 3, pBottom.y - pTop.y);
			} catch (AxisException e) {
			}
		}
	}
}