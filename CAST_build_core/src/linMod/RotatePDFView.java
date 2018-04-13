package linMod;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotatePDFView extends Rotate3DView {
	
//	static final private double kMaxHtFactor = 0.75;
	
	private String yDataKey;
	
	private NumValue sortedX[];
	protected Normal3DArtist normalArtist;
	
	boolean popNotSamp = true;
	private Color meanColor = null;
	
	public RotatePDFView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String yKey, String xKey, String dataKey, String sortedXParam) {
		super(theData, applet, yAxis, zAxis, xAxis, xKey, yKey, null);
		
		StringTokenizer theParams = new StringTokenizer(sortedXParam);
		sortedX = new NumValue[theParams.countTokens()];
		for (int i=0 ; i<sortedX.length ; i++)
			sortedX[i] = new NumValue(theParams.nextToken());
		
		normalArtist = new Normal3DArtist(this, yKey, xKey, theData, yAxis, xAxis, true);
		this.yDataKey = dataKey;
	}
	
	public void setPopNotSamp(boolean popNotSamp) {
		this.popNotSamp = popNotSamp;
	}
	
	public void setPopnMeanColor(Color meanColor) {
		this.meanColor = meanColor;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (popNotSamp)
			drawPDFs(g);
		else {
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yDataKey);
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			Point crossPos = null;
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				crossPos = getScreenPoint(ye.nextDouble(), 0.0, xe.nextDouble(), crossPos);
				if (crossPos != null)
					drawCross(g, crossPos);
			}
		}
	}
	
	private void drawMeanLine(Graphics g) {
		LinearModel model = (LinearModel)getVariable(yKey);
		double x0 = zAxis.getMinOnAxis();
		double x1 = zAxis.getMaxOnAxis();
		double y0 = model.evaluateMean(x0);
		double y1 = model.evaluateMean(x1);
		Point p0 = getScreenPoint(y0, 0.0, x0, null);
		Point p1 = getScreenPoint(y1, 0.0, x1, null);
		
		Color oldColor = g.getColor();
		g.setColor(meanColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		g.setColor(oldColor);
	}
	
	private boolean fromAbove() {
		return map.getTheta2() < 180;
	}
	
	private void drawPDFs(Graphics g) {
		if (meanColor != null && fromAbove())
			drawMeanLine(g);
			
		if (map.xAxisBehind())
			for (int i=0 ; i<sortedX.length ; i++)
				normalArtist.paintDistn(g, sortedX[i]);
		else
			for (int i=sortedX.length-1 ; i>=0 ; i--)
				normalArtist.paintDistn(g, sortedX[i]);
		
		if (meanColor != null && !fromAbove())
			drawMeanLine(g);
	}
}
	
