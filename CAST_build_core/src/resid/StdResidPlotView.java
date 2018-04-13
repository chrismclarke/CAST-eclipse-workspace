package resid;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class StdResidPlotView extends ScatterView {
	static final private Color kBandColor[] = {null, new Color(0x6699FF), new Color(0x99CCFF),
																																				new Color(0xCBEAFF)};
	static final private Color kLineColor[] = {new Color(0x3366CC), new Color(0x3366CC),
																										new Color(0x6699FF), new Color(0x99CCFF)};
	
	public StdResidPlotView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis residAxis,
																															String xKey, String residKey) {
		super(theData, applet, xAxis, residAxis, xKey, residKey);
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null) {
			double x = getNumVariable().doubleValueAt(index);
			
			int vertPos = yAxis.numValToRawPosition(0.0);
			int horizPos = axis.numValToRawPosition(x);
			Point zeroPoint = translateToScreen(horizPos, vertPos, null);
			
			Color oldColor = g.getColor();
			g.setColor(Color.red);
			g.drawLine(zeroPoint.x, zeroPoint.y, zeroPoint.x, thePoint.y);
			g.setColor(oldColor);
		}
	}
	
	protected void drawBackground(Graphics g) {
		Point p0 = null;
		Point p1 = null;
		for (int i=3 ; i>0 ; i--)
			try {
				g.setColor(kBandColor[i]);
				int y0 = yAxis.numValToPosition(-i);
				int y1 = yAxis.numValToPosition(i);
				p0 = translateToScreen(0, y0, p0);
				p1 = translateToScreen(0, y1, p1);
				g.fillRect(0, p1.y, getSize().width, p0.y - p1.y);
			} catch (AxisException e) {
			}
		for (int i=3 ; i>=0 ; i--)
			try {
				g.setColor(kLineColor[i]);
				int y0 = yAxis.numValToPosition(-i);
				int y1 = yAxis.numValToPosition(i);
				p0 = translateToScreen(0, y0, p0);
				p1 = translateToScreen(0, y1, p1);
				g.drawLine(0, p0.y, getSize().width, p0.y);
				g.drawLine(0, p1.y, getSize().width, p1.y);
			} catch (AxisException e) {
			}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		g.setColor(getForeground());
		
		super.paintView(g);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(xKey) || key.equals(yKey))
			repaint();
		else
			super.doChangeVariable(g, key);
	}
	
}
	
