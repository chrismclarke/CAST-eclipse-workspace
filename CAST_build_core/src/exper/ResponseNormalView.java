package exper;

import java.awt.*;

import dataView.*;
import axis.*;

import models.*;


public class ResponseNormalView extends DataView {
//	static final public String RESPONSE_NORMAL_VIEW = "respNormView";
	
	static final private Color kNormalDistnColor = Color.red;
	static final private Color kxLineColor = Color.lightGray;
	
	static final private int kMaxDensity = 40;
	static final private int kMinHitDist = 5;
	
	private MultiHorizAxis horizAxis;
	private VertAxis vertAxis;
	private String modelKey;
	
	private NumVariable xVar[];
	
	private NumValue tempX[];
	private Point tempP = new Point(0, 0);
	
	public ResponseNormalView(DataSet theData, XApplet applet, MultiHorizAxis horizAxis, VertAxis vertAxis,
														String modelKey, String[] xKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.horizAxis = horizAxis;
		this.vertAxis = vertAxis;
		this.modelKey = modelKey;
		xVar = new NumVariable[xKey.length];
		for (int i=0 ; i<xKey.length ; i++)
			xVar[i] = (NumVariable)getVariable(xKey[i]);
		tempX = new NumValue[xKey.length];
	}
	
	public void paintView(Graphics g) {
		int xDisplayIndex = horizAxis.getAlternativeLabelIndex();
		
		g.setColor(kxLineColor);
		ValueEnumeration e = xVar[xDisplayIndex].values();
		while (e.hasMoreValues()) {
			double x = e.nextDouble();
			int horizPos = horizAxis.numValToRawPosition(x);
			tempP = translateToScreen(horizPos, 0, tempP);
			g.drawLine(tempP.x, 0, tempP.x, getSize().height);
		}
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) { 
			g.setColor(kNormalDistnColor);
			for (int i=0 ; i<xVar.length ; i++)
				tempX[i] = (NumValue)xVar[i].valueAt(selectedIndex);
				
			int horizPos = horizAxis.numValToRawPosition(tempX[xDisplayIndex].toDouble());
			
			MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
			double yMean = model.evaluateMean(tempX);
			double ySD = model.evaluateSD().toDouble();
			
			int lowVertPos = vertAxis.numValToRawPosition(yMean - 3.0 * ySD);
			int highVertPos = vertAxis.numValToRawPosition(yMean + 3.0 * ySD);
			
			int lowY = translateToScreen(horizPos, lowVertPos, tempP).y;
			tempP = translateToScreen(horizPos, highVertPos, tempP);
			drawDensity(tempP.x, lowY, tempP.y, g);
			g.setColor(getForeground());
		}
	}
	
	private void drawDensity(int xPos, int lowYPos, int highYPos, Graphics g) {
			g.drawLine(xPos, 0, xPos, getSize().height);
			
			int highY = Math.max(lowYPos, highYPos);
			int lowY = Math.min(lowYPos, highYPos);
			int distnHt = highY - lowY;
			if (distnHt == 0)
				g.drawLine(xPos, lowY, xPos + kMaxDensity, lowY);
			else {
				double mid = (lowY + highY) * 0.5;
				double sd = distnHt / 6.0;
				for (int pos=lowY ; pos<=highY ;  pos++) {
					double z = (pos - mid) / sd;
					double density = Math.exp(-0.5 * z * z);
					int width = (int)Math.round(density * kMaxDensity);
					g.drawLine(xPos, pos, xPos + width, pos);
				}
			}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point p = translateFromScreen(x, y, null);
		
		if (p != null) {
			int mouseX = p.x;
			
			int xDisplayIndex = horizAxis.getAlternativeLabelIndex();
			int noOfVals = xVar[xDisplayIndex].noOfValues();
			
			int minIndex = -1;
			int minDist = 0;
			boolean gotPoint = false;
			for (int i=0 ; i<noOfVals ; i++) {
				int xPos = horizAxis.numValToRawPosition(xVar[xDisplayIndex].doubleValueAt(i));
				int dist = Math.abs(xPos - mouseX);
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < kMinHitDist) {
					minIndex = i;
					minDist = dist;
				}
			}
			if (gotPoint && minDist < kMinHitDist)
				return new IndexPosInfo(minIndex);
		}
		return null;
	}
}
	
