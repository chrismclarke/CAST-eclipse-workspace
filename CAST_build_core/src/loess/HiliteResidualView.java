package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class HiliteResidualView extends ScatterView {
//	static public final String HILITE_RESID_PLOT = "hiliteResidPlot";
	
	static final public Color darkGreen = new Color(0x006600);
	static final private Color kDarkOrange = new Color(0xFF6600);
	
	private String lineKey, loessKey;
	private boolean showLoess = false;
	private boolean allowSelectPoint = true;
	
	public HiliteResidualView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey,
						String loessKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
		this.loessKey = loessKey;
		setRetainLastSelection(true);
	}
	
	public void setShowLoess(boolean showLoess) {
		if (this.showLoess != showLoess) {
			this.showLoess = showLoess;
			repaint();
		}
	}
	
	public void setAllowSelectPoint(boolean allowSelectPoint) {
		this.allowSelectPoint = allowSelectPoint;
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null)
			try {
				double x = getNumVariable().doubleValueAt(index);
				int horizPos = axis.numValToPosition(x);
				if (lineKey == null) {
					int zeroVertPos = yAxis.numValToPosition(0.0);
					Point zeroPoint = translateToScreen(0, zeroVertPos, null);
					
					g.setColor(Color.red);
					g.drawLine(thePoint.x, thePoint.y, thePoint.x, zeroPoint.y);
				}
				else {
					LinearModel model = (LinearModel)getVariable(lineKey);
					double yHat = model.evaluateMean(x);
					
					int vertPos = yAxis.numValToPosition(yHat);
					Point fittedPoint = translateToScreen(horizPos, vertPos, null);
					
					Color oldColor = g.getColor();
					g.setColor(darkGreen);
					g.drawLine(0, fittedPoint.y, fittedPoint.x, fittedPoint.y);
					g.drawLine(0, fittedPoint.y, 3, fittedPoint.y + 3);
					g.drawLine(0, fittedPoint.y, 3, fittedPoint.y - 3);
					g.setColor(Color.blue);
					g.drawLine(0, thePoint.y, thePoint.x, thePoint.y);
					g.drawLine(0, thePoint.y, 3, thePoint.y + 3);
					g.drawLine(0, thePoint.y, 3, thePoint.y - 3);
					
					g.setColor(Color.red);
					g.drawLine(fittedPoint.x, fittedPoint.y, fittedPoint.x, thePoint.y);
					
					g.setColor(oldColor);
				}
			} catch (AxisException e) {
			}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		g.setColor(Color.gray);
		if (lineKey == null)
			try {
				int zeroVertPos = yAxis.numValToPosition(0.0);
				Point fittedPoint = translateToScreen(0, zeroVertPos, null);
				g.drawLine(0, fittedPoint.y, getSize().width, fittedPoint.y);
			} catch (AxisException e) {
			}
		else {
			LinearModel model = (LinearModel)getVariable(lineKey);
			model.drawMean(g, this, axis, yAxis);
		}
		
		if (loessKey != null && showLoess) {
			g.setColor(kDarkOrange);
			
			LoessSmoothVariable loessVar = (LoessSmoothVariable)getVariable(loessKey);
			loessVar.drawCurve(g, this);
		}
		
		g.setColor(getForeground());
	}
	
	protected boolean canDrag() {
		return allowSelectPoint;
	}
}
	
