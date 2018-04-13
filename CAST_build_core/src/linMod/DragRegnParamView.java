package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;

import regnView.*;


public class DragRegnParamView extends DragLineView {
	
	private boolean gotSample = false;
	
	public DragRegnParamView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lineKey);
	}
	
	public void setGotSample(boolean gotSample) {
		this.gotSample = gotSample;
	}
	
	private Point getScreenPos(double x, double y) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = axis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, null);
	}
	
	protected void fillLS(Graphics g, double offsetSD, Color fillColor) {
		Color oldColor = g.getColor();
		g.setColor(fillColor);
		
		double lowX = axis.minOnAxis;
		double highX = axis.maxOnAxis;
		double xBorder = (highX - lowX) * 0.1;
		lowX -= xBorder;
		highX += xBorder;
		
		LinearModel distn = (LinearModel)getData().getVariable(lineKey);
		
		double yOffset = offsetSD * distn.evaluateSD().toDouble();
		
		double lowY1 = distn.evaluateMean(new NumValue(lowX)) + yOffset;
		double highY1 = distn.evaluateMean(new NumValue(highX)) + yOffset;
		
		Point startPos1 = getScreenPos(lowX, lowY1);
		Point endPos1 = getScreenPos(highX, highY1);
		
		double lowY2 = distn.evaluateMean(new NumValue(lowX)) - yOffset;
		double highY2 = distn.evaluateMean(new NumValue(highX)) - yOffset;
		
		Point startPos2 = getScreenPos(lowX, lowY2);
		Point endPos2 = getScreenPos(highX, highY2);
		
		int x[] = {startPos1.x, endPos1.x, endPos2.x, startPos2.x};
		int y[] = {startPos1.y, endPos1.y, endPos2.y, startPos2.y};
		
		g.fillPolygon(x, y, 4);
		g.drawLine(startPos1.x, startPos1.y, endPos1.x, endPos1.y);
		g.drawLine(startPos2.x, startPos2.y, endPos2.x, endPos2.y);
		g.setColor(oldColor);
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	protected void drawModelAndHandles(Graphics g) {
//		NumVariable xVariable = getNumVariable();
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		fillLS(g, 2.0, Color.lightGray);
		
		Point handles[] = model.getHandles(this, axis, yAxis);
		for (int i=0 ; i<handles.length ; i++)
			ModelGraphics.drawHandle(g, handles[i], selectedHandle == i);
		
		g.setColor(Color.blue);
		model.drawMean(g, this, axis, yAxis);
	}
	
	public void paintView(Graphics g) {
		drawModelAndHandles(g);
		
		if (gotSample)
			super.paintView(g);
	}
}
	
