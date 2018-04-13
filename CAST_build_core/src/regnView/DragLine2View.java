package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;


public class DragLine2View extends DragLineView {
	
	static final private Color lightBlue = new Color(0x0066FF);
	
	private boolean pixelSquare = false;
	
	public DragLine2View(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lineKey);
	}
	
	public void setPixelSquare(boolean pixelSquare) {
		this.pixelSquare = pixelSquare;
	}
	
	private Point getResidEnd(double x, double y, LinearModel model, Point thePoint) {
		double fit = model.evaluateMean(x);
		double resid = y - fit;
		boolean posSlope = model.getSlope().toDouble() > 0.0;
		
		int vertPos = yAxis.numValToRawPosition(fit);
		if (pixelSquare) {
			int xPos = axis.numValToRawPosition(x);
			int yPos = yAxis.numValToRawPosition(y);
			thePoint = translateToScreen(xPos, yPos, thePoint);
			int yPix = thePoint.y;
			
			thePoint = translateToScreen(xPos, vertPos, thePoint);
			int fitPix = thePoint.y;
			
			thePoint.x += (posSlope ? yPix - fitPix : fitPix - yPix);
			return thePoint;
		}
		else  {
			int horizPos = axis.numValToRawPosition(x + (posSlope ? -resid : resid));
			return translateToScreen(horizPos, vertPos, thePoint);
		}
	}
	
	protected void drawBackground(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point dataPoint = null;
		Point residEndPoint = null;
		int index = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue x = (NumValue)xe.nextValue();
			NumValue y = (NumValue)ye.nextValue();
			dataPoint = getScreenPoint(index, x, dataPoint);
			residEndPoint = getResidEnd(x.toDouble(), y.toDouble(), model, residEndPoint);
			
			if (dataPoint != null && residEndPoint != null) {
				int top = Math.min(dataPoint.y, residEndPoint.y);
				int left = Math.min(dataPoint.x, residEndPoint.x);
				int height = Math.abs(residEndPoint.y - dataPoint.y);
				int width = Math.abs(residEndPoint.x - dataPoint.x);
				g.setColor(lightBlue);
				g.fillRect(left, top, width, height);
				g.setColor(Color.blue);
				g.drawRect(left, top, width, height);
			}
			index++;
		}
		
		Point handles[] = model.getHandles(this, axis, yAxis);
		for (int i=0 ; i<handles.length ; i++)
			ModelGraphics.drawHandle(g, handles[i], selectedHandle == i);
		
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
	}
}
	
