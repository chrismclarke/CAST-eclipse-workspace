package contin;

import java.awt.*;

import dataView.*;
import axis.*;


public class AreaContinDragView extends DataView {
//	static public final String AREA_CONTIN_DRAG = "areaContinDrag";
	
	static final public boolean Y_MARGIN = true;
	static final public boolean X_MARGIN = false;
	
	static final private int kXLabelBorder = 3;
	static final private int kTableTopGap = 5;
	static final private int kTableRightGap = 5;
	
//	static final private int kProbDecimals = 3;
//	static final private NumValue kZero = new NumValue(0.0, kProbDecimals);
	
	static final private Color[] outlineColor = RotateContinView.outlineColor;
	static final private Color[] fillColor = RotateContinView.fillColor;
	
	private String xKey, yKey;
	private VertAxis yAxis;
	private HorizAxis xAxis;
	
	private boolean marginForY;
	private int nYCats, nXCats;
	private double[][] yConditXProb, xConditYProb;
	private double[] xMarginalProb, yMarginalProb;
	
	private boolean adjustedBorder = false;
	private int ascent, descent;
	
	public AreaContinDragView(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
						String xKey, boolean marginForY) {
		super(theData, applet, new Insets(8, 8, 8, 8));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.yAxis = yAxis;
		this.xAxis = xAxis;
		this.marginForY = marginForY;
	}
	
	private void findProbs() {
		ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
		nYCats = yVar.noOfCategories();
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		nXCats = xVar.noOfCategories();
		
		yConditXProb = yVar.getConditionalProbs();
		xMarginalProb = xVar.getProbs();
		
		if (yMarginalProb == null)
			yMarginalProb = new double[nYCats];
		else
			for (int j=0 ; j<nYCats ; j++)
				yMarginalProb[j] = 0.0;
		
		if (marginForY && xConditYProb == null)
			xConditYProb = new double[nYCats][];
		for (int j=0 ; j<nYCats ; j++) {
			for (int i=0 ; i<nXCats ; i++)
				yMarginalProb[j] += xMarginalProb[i] * yConditXProb[i][j];
			if (marginForY) {
				xConditYProb[j] = new double[nXCats];
				for (int i=0 ; i<nXCats ; i++)
					xConditYProb[j][i] = (yMarginalProb[j] >= 0.0)
										? xMarginalProb[i] * yConditXProb[i][j] / yMarginalProb[j]
										: 1.0 / nYCats;
			}
		}
	}
	
	
	public Insets getViewBorder() {
		if (adjustedBorder)
			return super.getViewBorder();
		else {
			Insets border = super.getViewBorder();
			Graphics g = getGraphics();
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
			int nYCats = yVar.noOfCategories();
			int maxYCatLength = 0;
			for (int i=0 ; i<nYCats ; i++)
				maxYCatLength = Math.max(maxYCatLength, yVar.getLabel(i).stringWidth(g));
			
			CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
			int nXCats = xVar.noOfCategories();
			int maxXCatLength = 0;
			for (int i=0 ; i<nXCats ; i++)
				maxXCatLength = Math.max(maxXCatLength, xVar.getLabel(i).stringWidth(g));
			
			int htForLabels = fm.getAscent() + fm.getDescent() + kTableTopGap;
			int widthForLabels = maxYCatLength + kTableRightGap;
			
			border.top += htForLabels;
			border.right += widthForLabels;
			adjustedBorder = true;
			return border;
		}
	}
	
	private Rectangle getPosOnAxes(int y, int x, Rectangle tempRect, Point tempTopLeft,
																						Point tempBottomRight) {
		Rectangle r = (tempRect == null) ? new Rectangle(0, 0, 0, 0) : tempRect;
		
		double yFractLow = 0.0;
		for (int j=0 ; j<y ; j++)
			yFractLow += marginForY ? yMarginalProb[j] : yConditXProb[x][j];
		double yFractHigh = yFractLow + (marginForY ? yMarginalProb[y] : yConditXProb[x][y]);
		
		double xFractLow = 0.0;
		for (int i=0 ; i<x ; i++)
			xFractLow += marginForY ? xConditYProb[y][i] : xMarginalProb[i];
		double xFractHigh = xFractLow + (marginForY ? xConditYProb[y][x] : xMarginalProb[x]);
		
		int yTop = yAxis.numValToRawPosition(yFractHigh);
		int yBottom = yAxis.numValToRawPosition(yFractLow);
		int xLeft = xAxis.numValToRawPosition(xFractLow);
		int xRight = xAxis.numValToRawPosition(xFractHigh);
		
		tempTopLeft = translateToScreen(xLeft, yTop, tempTopLeft);
		tempBottomRight = translateToScreen(xRight, yBottom, tempBottomRight);
		
		r.x = tempTopLeft.x;
		r.y = tempTopLeft.y;
		r.width = tempBottomRight.x - tempTopLeft.x;
		r.height = tempBottomRight.y - tempTopLeft.y;
		
		return r;
	}
	
	private int getXLabelCenter(int x, Point tempPoint) {
		double xFractLow = 0.0;
		for (int i=0 ; i<x ; i++)
			xFractLow += xMarginalProb[i];
		double xFractCenter = xFractLow + 0.5 * xMarginalProb[x];
		
		int xCenter = xAxis.numValToRawPosition(xFractCenter);
		tempPoint = translateToScreen(xCenter, 0, tempPoint);
		return tempPoint.x;
	}
	
	private int getYLabelBaseline(int y, Point tempPoint) {
		double yFractLow = 0.0;
		for (int i=0 ; i<y ; i++)
			yFractLow += yMarginalProb[i];
		double yFractCenter = yFractLow + 0.5 * yMarginalProb[y];
		
		int yCenter = yAxis.numValToRawPosition(yFractCenter);
		tempPoint = translateToScreen(0, yCenter, tempPoint);
		return tempPoint.y + (ascent - descent) / 2;
	}
	
	public void paintView(Graphics g) {
		findProbs();
		
		ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		
		Rectangle r = null;
		Point p1 = null;
		Point p2 = null;
		
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++) {
				r = getPosOnAxes(j, i, r, p1, p2);
				
				g.setColor(fillColor[i]);
				g.fillRect(r.x, r.y, r.width, r.height);
				
				g.setColor(outlineColor[j]);
				g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
				g.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
				
				if (r.width <= 6) {
					g.setColor(Color.black);
					g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
				}
			}
		
		int xLabelBaseline = getViewBorder().top - kTableTopGap - descent;
		for (int i=0 ; i<nXCats ; i++) {
			Value xLabel = xVar.getLabel(i);
			int width = xLabel.stringWidth(g);
			
			int xCenter = getXLabelCenter(i, p1);
			int xLeft = Math.max(0, Math.min(getSize().width - width, xCenter - width / 2));
			g.setColor(fillColor[i]);
			g.fillRect(xLeft - kXLabelBorder, xLabelBaseline - ascent - kXLabelBorder,
								width + 2 * kXLabelBorder, ascent + descent + 2 * kXLabelBorder);
			g.setColor(getForeground());
			xLabel.drawRight(g, xLeft, xLabelBaseline);
		}
		
		int yLabelLeft = translateToScreen(xAxis.numValToRawPosition(1.0), 0, p1).x
																							+ kTableRightGap;
		for (int j=0 ; j<nYCats ; j++) {
			Value yLabel = yVar.getLabel(j);
			
			int yBaseline = getYLabelBaseline(j, p1);
			g.setColor(outlineColor[j]);
			yLabel.drawRight(g, yLabelLeft, yBaseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
