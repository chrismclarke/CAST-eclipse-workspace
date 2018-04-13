package resid;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class DeleteChangeView extends ScatterView {
//	static public final String FIT_CHANGE_PLOT = "fitChangePlot";
	
	static final private Color kPink = new Color(0xFFBBBB);
	static final private Color kLightGray = new Color(0xDDDDDD);
	
	protected String lsKey, deletedLSKey;
	
	private boolean drawFitChange = true;
	
	public DeleteChangeView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
														String xKey, String yKey, String lsKey, String deletedLSKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lsKey = lsKey;
		this.deletedLSKey = deletedLSKey;
	}
	
	public void setDrawFitChange(boolean drawFitChange) {
		this.drawFitChange = drawFitChange;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		NumVariable variable = getNumVariable();
		Point thePoint = null;
		
		ValueEnumeration e = variable.values();
//		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				boolean selected = (index == getSelection().findSingleSetFlag());
				if (selected)
					g.setColor(kPink);
				drawCross(g, thePoint);
				if (selected)
					g.setColor(getForeground());
			}
			index++;
		}
	}
	
	private void drawBackground(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		
		LinearModel ls = (LinearModel)getVariable(lsKey);
		if (selectedIndex < 0) {
			g.setColor(Color.blue);
			ls.drawMean(g, this, axis, yAxis);
		}
		else {
			g.setColor(kLightGray);
			ls.drawMean(g, this, axis, yAxis);
			
			g.setColor(Color.blue);
			LinearModel deletedLS = (LinearModel)getVariable(deletedLSKey);
			if (deletedLS.setDeletedIndex(selectedIndex))
				deletedLS.updateLSParams(yKey);
			deletedLS.drawMean(g, this, axis, yAxis);
			
			if (drawFitChange) {
				g.setColor(Color.red);
				
				NumVariable xVar = (NumVariable)getVariable(xKey);
				NumValue x = (NumValue)xVar.valueAt(selectedIndex);
				double fullFit = ls.evaluateMean(x);
				double deletedFit = deletedLS.evaluateMean(x);
				
				int xPos = axis.numValToRawPosition(x.toDouble());
				int fullFitPos = yAxis.numValToRawPosition(fullFit);
				int deletedFitPos = yAxis.numValToRawPosition(deletedFit);
				Point p1 = translateToScreen(xPos, fullFitPos, null);
				Point p2 = translateToScreen(xPos, deletedFitPos, null);
				
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
			}
		}
		
		g.setColor(getForeground());
	}
	
}
	
