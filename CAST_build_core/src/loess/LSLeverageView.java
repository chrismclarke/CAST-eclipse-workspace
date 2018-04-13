package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class LSLeverageView extends ScatterView {
//	static public final String LS_LEVERAGE_PLOT = "lsLeveragePlot";
	
	protected String lineKey;
	private double offset;
	
	private boolean initialised = false;
	private double sx, sy, sxx, sxy;
	private int n = 0;
	
	public LSLeverageView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey,
						double offset) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
		this.offset = offset;
		setRetainLastSelection(true);
		setStickyDrag(true);
	}
	
	public void setOffset(double offset) {
		this.offset = offset;
		repaint();
	}
	
	protected boolean initialise (Graphics g) {
		if (!initialised) {
			ValueEnumeration xe = ((NumVariable)getVariable(xKey)).values();
			ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
			FlagEnumeration fe = getSelection().getEnumeration();
			sx = 0.0;
			sy = 0.0;
			sxx = 0.0;
			sxy = 0.0;
			n = 0;
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				double x = xe.nextDouble();
				double y = ye.nextDouble();
				boolean nextSel = fe.nextFlag();
				if (nextSel)
					y += offset;
				if (!Double.isNaN(x) && !Double.isNaN(y)) {
					sx += x;
					sy += y;
					sxx += x * x;
					sxy += x * y;
					n ++;
				}
			}
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		drawBackground(g);
		super.paintView(g);
		
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumValue yVal = (NumValue)(yVariable.valueAt(selectedIndex));
			NumValue xVal = (NumValue)(xVariable.valueAt(selectedIndex));
			Point selectedPoint = getScreenPoint(selectedIndex, xVal, null);
			
			if (selectedPoint != null) {
				g.setColor(Color.lightGray);
				drawCross(g, selectedPoint);
			}
			
								//		A dirty way to get moved y cross drawn
			double oldY = yVal.toDouble();
			yVal.setValue(oldY + offset);
			selectedPoint = getScreenPoint(selectedIndex, xVal, null);
			if (selectedPoint != null) {
				g.setColor(Color.red);
				drawBlob(g, selectedPoint);
			}
			
			yVal.setValue(oldY);
			g.setColor(getForeground());
		}
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
												//		We don't want to emphasise moved point
	}
		
	private void drawBackground(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		if (selectedIndex < 0) {
			g.setColor(Color.blue);
			model.drawMean(g, this, axis, yAxis);
		}
		else {
			g.setColor(Color.lightGray);
			model.drawMean(g, this, axis, yAxis);
			
			double x = ((NumVariable)getVariable(xKey)).doubleValueAt(selectedIndex);
//			double oldY = ((NumVariable)getVariable(yKey)).doubleValueAt(selectedIndex);
			double newSy = sy + offset;
			double newSxy = sxy + x * offset;
			
			g.setColor(Color.lightGray);
			model.drawMean(g, this, axis, yAxis);
			
			NumValue slope = model.getSlope();
			NumValue intercept = model.getIntercept();
			
			double oldSlopeVal = slope.toDouble();
			double oldInterceptVal = intercept.toDouble();
			
			slope.setValue((newSxy - sx * newSy / n) / (sxx - sx * sx / n));
			intercept.setValue((newSy - sx * slope.toDouble()) / n);
								//		A dirty way to get leverage line drawn
			
			g.setColor(Color.blue);
			model.drawMean(g, this, axis, yAxis);
			g.setColor(getForeground());
			
			slope.setValue(oldSlopeVal);
			intercept.setValue(oldInterceptVal);
		}
	}
}
	
