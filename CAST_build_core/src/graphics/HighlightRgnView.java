package graphics;

import java.awt.*;

import dataView.*;
import axis.*;

import coreGraphics.*;


public class HighlightRgnView extends ScatterView {
//	static public final String HIGHLIGHT_RGN_PLOT = "highlightRgnPlot";
	
	private double rgnX[] = null;
	private double rgnY[] = null;
	private Color rgnColor;
	
	public HighlightRgnView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
	}
	
	public void setHighlightRgn(double[] rgnX, double[] rgnY, Color rgnColor) {
		this.rgnX = rgnX;
		this.rgnY = rgnY;
		this.rgnColor = rgnColor;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumValue yVal = (NumValue)(yVariable.valueAt(index));
		
		if (isBadValue(theVal) || isBadValue(yVal))
			return null;
//		try {
			int vertPos = yAxis.numValToRawPosition(yVal.toDouble());
			int horizPos = axis.numValToRawPosition(theVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
//		} catch (AxisException ex) {
//			return null;
//		}
	}
	
	public void paintView(Graphics g) {
		if (rgnX != null && rgnY != null) {
			int nPoints = rgnX.length;
			int xCoord[] = new int[nPoints];
			int yCoord[] = new int[nPoints];
			Point p = null;
			for (int i=0 ; i<nPoints ; i++) {
				int y = yAxis.numValToRawPosition(rgnY[i]);
				int x = axis.numValToRawPosition(rgnX[i]);
				p = translateToScreen(x, y, p);
				xCoord[i] = p.x;
				yCoord[i] = p.y;
			}
			g.setColor(rgnColor);
			g.fillPolygon(xCoord, yCoord, nPoints);
			g.setColor(getForeground());
		}
		super.paintView(g);
	}
}
	
