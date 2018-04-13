package twoFactor;

import java.awt.*;

import dataView.*;
import axis.*;


abstract public class CoreEffectDiagramView extends DataView {
	protected String yKey, modelKey;
	protected String xKey[];
	protected HorizAxis horizAxis;
	protected VertAxis yAxis;
	protected int horizIndex;
	
	public CoreEffectDiagramView(DataSet theData, XApplet applet, String yKey, String[] xKey, String modelKey,
									HorizAxis horizAxis, VertAxis yAxis, int horizIndex) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.xKey = xKey;
		this.modelKey = modelKey;
		this.horizAxis = horizAxis;
		this.yAxis = yAxis;
		this.horizIndex = horizIndex;
	}
	
	protected Point getScreenPoint(double yVal, int catIndex, Point thePoint) {
		int vertPos = yAxis.numValToRawPosition(yVal);
		int horizPos = horizAxis.catValToPosition(catIndex);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void drawMeans(Graphics g, double[] mean, Color lineColor) {
		Point p0 = getScreenPoint(mean[0], 0, null);
		Point p1 = null;
		for (int i=1 ; i<mean.length ; i++) {
			p1 = getScreenPoint(mean[i], i, p1);
			g.setColor(lineColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			g.setColor(Color.black);
			drawBlob(g, p0);
			Point temp = p0;
			p0 = p1;
			p1 = temp;
		}
		drawBlob(g, p0);
	}
	
	protected void drawOtherLabels(Graphics g, double[] rightMean, Color[] labelColor,
																																			Value[] otherLabel) {
		labelColor = (Color[])labelColor.clone();
		Point p0 = null;
		int offset = g.getFontMetrics().getAscent() / 2;
		for (int i=0 ; i<rightMean.length ; i++) {
			p0 = getScreenPoint(rightMean[i], 0, p0);
			g.setColor(labelColor[i]);
			otherLabel[i].drawLeft(g, getSize().width - 3, p0.y + offset);
		}
	}
	
	protected void drawLineLabel(Graphics g, double rightMean, Color labelColor, String label) {
		FontMetrics fm = g.getFontMetrics();
		int offset = fm.getAscent() / 2;
		Point p0 = getScreenPoint(rightMean, 0, null);
		g.setColor(labelColor);
		int labelWidth = fm.stringWidth(label);
		g.drawString(label, getSize().width - 3 - labelWidth, p0.y + offset);
	}
	
	abstract public void paintView(Graphics g);

//-----------------------------------------------------------------------------------
		
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}