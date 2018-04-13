package inference;

import java.awt.*;

import dataView.*;
import axis.*;
import graphics3D.*;


public class CiPropnGraphView extends DataView {
	
	static final private int kArrowSize = 3;
	
	static final private Color kCiFillColor = new Color(0xCCCCFF);
	static final private Color kIdentityColor = new Color(0x666666);
	static final private Color kMarginColor = new Color(0xEEEEEE);
	static final private Color kPaleRed = new Color(0xFFCCCC);
	static final private Color kCiFillDark = new Color(0xB9A2C0);
	static final private Color kMarginDark = new Color(0xDCB1B1);
	
	private String xKey;
	private HorizAxis pAxis;
	private VertAxis ciAxis;
	
	int lowPPos, highPPos;
	private int lowPos[];
	private int highPos[];
	
	private boolean ciNotMarginOfError = true;
	
	private boolean initialised = false;
	
	public CiPropnGraphView(DataSet theData, XApplet applet, String xKey, HorizAxis pAxis, VertAxis ciAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.pAxis = pAxis;
		this.ciAxis = ciAxis;
	}
	
	public void reset() {
		initialised = false;
	}
	
	public void setCiNotMarginOfError(boolean ciNotMarginOfError) {
		this.ciNotMarginOfError = ciNotMarginOfError;
		repaint();
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		CatVariable x = (CatVariable)getVariable(xKey);
		int n = x.noOfValues();
		
		lowPPos = pAxis.numValToRawPosition(0.0);
		highPPos = pAxis.numValToRawPosition(1.0);
		
		lowPos = new int[highPPos - lowPPos + 1];
		highPos = new int[highPPos - lowPPos + 1];
		
		for (int i=lowPPos ; i<=highPPos ; i++)
			try {
				double p = pAxis.positionToNumVal(i);
				double plusMinus = 2.0 * Math.sqrt(p * (1 - p) / n);
				lowPos[i - lowPPos] = ciAxis.numValToRawPosition(p - plusMinus);
				highPos[i - lowPPos] = ciAxis.numValToRawPosition(p + plusMinus);
			} catch (AxisException e) {
			}
		
		initialised = true;
		return true;
	}
	
	private void paintBackground(Graphics g, int lowY, int highY,
																								Color marginColor, Color areaFillColor) {
		g.setColor(marginColor);
		int pPos0 = pAxis.numValToRawPosition(0.0);
		int ciPos0 = ciAxis.numValToRawPosition(0.0);
		int pPos1 = pAxis.numValToRawPosition(1.0);
		int ciPos1 = ciAxis.numValToRawPosition(1.0);
		Point pLow = translateToScreen(pPos0, ciPos0, null);
		Point pHigh = translateToScreen(pPos1, ciPos1, null);
		
		g.fillRect(0, 0, pLow.x, getSize().height);
		g.fillRect(pHigh.x, 0, getSize().width - pHigh.x, getSize().height);
		g.fillRect(0, 0, getSize().width, pHigh.y);
		g.fillRect(0, pLow.y, getSize().width, getSize().height - pLow.y);
		
		g.setColor(areaFillColor);
		for (int i=lowPPos ; i<=highPPos ; i++) {
			pLow = translateToScreen(i, lowPos[i - lowPPos], pLow);
			pHigh = translateToScreen(i, highPos[i - lowPPos], pHigh);
			g.drawLine(pLow.x, pLow.y, pLow.x, pHigh.y);
		}
		
		g.setColor(kIdentityColor);
		pLow = translateToScreen(pPos0, ciPos0, pLow);
		pHigh = translateToScreen(pPos1, ciPos1, pHigh);
		g.drawLine(pLow.x, pLow.y, pHigh.x, pHigh.y);
	}
	
	private void drawArrow(Graphics g, Point p) {
		g.drawLine(0, p.y, p.x, p.y);
		g.drawLine(0, p.y, kArrowSize, p.y + kArrowSize);
		g.drawLine(0, p.y, kArrowSize, p.y - kArrowSize);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		paintBackground(g, 0, getSize().height, kMarginColor, kCiFillColor);
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int n = xVar.noOfValues();
		int counts[] = xVar.getCounts();
		double p = counts[0] / (double)n;
		
		double plusMinus = ciNotMarginOfError ? 2.0 * Math.sqrt(p * (1 - p) / n)
																					: 1.0 / Math.sqrt(n);
		
		try {
			int pPos = pAxis.numValToPosition(p);
			int lowCIPos = ciAxis.numValToRawPosition(p - plusMinus);
			int highCIPos = ciAxis.numValToRawPosition(p + plusMinus);
			Point p0 = translateToScreen(pPos, lowCIPos, null);
			Point p1 = translateToScreen(pPos, highCIPos, null);
			
			g.setColor(kPaleRed);
			g.fillRect(0, p1.y, p0.x, p0.y - p1.y);
			
			g.setColor(Color.red);
			g.drawLine(p0.x, 0, p0.x, getSize().height);
			drawArrow(g, p0);
			drawArrow(g, p1);
			
			if (Rotate3DView.canClip) {
				g.setClip(0, p1.y + 1, p0.x - 1, p0.y - p1.y - 1);
				paintBackground(g, lowCIPos, highCIPos, kMarginDark, kCiFillDark);
				
				g.setColor(Color.red);
				drawArrow(g, p0);
				drawArrow(g, p1);
				g.setClip(0, 0, getSize().width, getSize().height);
			}
		} catch (AxisException e) {
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
