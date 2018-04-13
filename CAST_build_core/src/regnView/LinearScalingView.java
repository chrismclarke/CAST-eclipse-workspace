package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class LinearScalingView extends DragLineView {
	static final private int kArrowSize = 5;
	static final private Color kLightGray = new Color(0xCCCCCC);
	
	private double min, max;
	
	public LinearScalingView(DataSet theData, XApplet applet, HorizAxis xAxis,
								VertAxis yAxis, String xKey, String lineKey, double min, double max) {
		super(theData, applet, xAxis, yAxis, xKey, null, lineKey);
		setDrawResiduals(false);
		this.min = min;
		this.max = max;
	}
	
	public void paintView(Graphics g) {
		Point p = null;
		g.setColor(kLightGray);
		try {
			int yPos = yAxis.numValToPosition(min);
			p = translateToScreen(0, yPos, p);
			g.fillRect(0, p.y + 1, getSize().width, getSize().height);
		}
		catch (AxisException e) {
		}
		try {
			int yPos = yAxis.numValToPosition(max);
			p = translateToScreen(0, yPos, p);
			g.fillRect(0, 0, getSize().width, p.y - 1);
		}
		catch (AxisException e) {
		}
		
		g.setColor(getForeground());
		drawBackground(g);
		
		int selectedIndex = getData().getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			g.setColor(Color.red);
			LinearModel model = (LinearModel)getData().getVariable(lineKey);
			
			NumVariable xVar = (NumVariable)getData().getVariable(xKey);
			NumValue x = (NumValue)xVar.valueAt(selectedIndex);
			
			p = getFittedPoint(x, model, p);
			g.drawLine(0, p.y, p.x, p.y);
			g.drawLine(0, p.y, kArrowSize, p.y + kArrowSize);
			g.drawLine(0, p.y, kArrowSize, p.y - kArrowSize);
			g.drawLine(p.x, p.y, p.x, getSize().height);
		}
	}

}
	
