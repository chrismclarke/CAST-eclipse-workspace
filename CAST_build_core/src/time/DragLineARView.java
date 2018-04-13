package time;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import regnView.*;


public class DragLineARView extends DragLineView {
	private String smoothKey;
	
	public DragLineARView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																	String xKey, String yKey, String lineKey, String smoothKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lineKey);
		this.smoothKey = smoothKey;
	}
	
	protected void drawBackground(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex > 0) {
			NumVariable xVariable = getNumVariable();
			LinearModel model = (LinearModel)getVariable(lineKey);
			NumValue x = (NumValue)xVariable.valueAt(selectedIndex);
			if (Double.isNaN(x.toDouble())) {
				NumVariable smoothVar = (NumVariable)getVariable(smoothKey);
				x = (NumValue)smoothVar.valueAt(selectedIndex - 1);
			}
			
			Point fittedPoint = getFittedPoint(x, model, null);
			
			g.setColor(TimeARView.kSelectBackgroundColor);
			g.fillRect(fittedPoint.x - 2, 0, 5, getSize().height);
			
			g.setColor(TimeARView.kPredictColor);
			g.drawLine(fittedPoint.x, fittedPoint.y, 0, fittedPoint.y);
			g.drawLine(0, fittedPoint.y, 5, fittedPoint.y - 5);
			g.drawLine(0, fittedPoint.y, 5, fittedPoint.y + 5);
			
			g.setColor(Color.red);
			Point actualPoint = getScreenPoint(selectedIndex, x, null);
			if (actualPoint != null && fittedPoint != null)
				g.drawLine(fittedPoint.x, fittedPoint.y, actualPoint.x, actualPoint.y);
		}
		super.drawBackground(g);
	}
}
	
