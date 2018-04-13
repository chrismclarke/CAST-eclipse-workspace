package multiRegn;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class DragResidSquareView extends DragParamResidView {
	
	static final private Color kResidColor = new Color(0x990000);
	static final private Color kPaleResid = getShadedColor(kResidColor);
	
	static final private Color kFillColor = new Color(0xFF3333);
	static final private Color kPaleFill = getShadedColor(kFillColor);
	
	public DragResidSquareView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String yKey, String xKey, String zKey, ColoredLinearEqnView equationView) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, yKey, xKey, zKey, equationView);
	}
	
	protected void drawResidual(Graphics g, int shadeHandling, boolean thisSideOfPlane,
												Point crossPos, Point fitPos) {
		boolean drawPale = (shadeHandling == USE_OPAQUE) && !thisSideOfPlane;
		g.setColor(drawPale ? kPaleFill : kFillColor);
		int yLow = Math.min(crossPos.y, fitPos.y);
		int ySide = Math.abs(crossPos.y - fitPos.y) + 1;
		g.fillRect(crossPos.x, yLow, ySide, ySide);
		
		g.setColor(drawPale ? kPaleResid : kResidColor);
		g.drawLine(fitPos.x, fitPos.y, crossPos.x, crossPos.y);
	}
}
	
