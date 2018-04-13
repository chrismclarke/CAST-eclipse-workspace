package contin;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class RotateContinView extends Rotate3DView {
//	static public final String ROTATE_CONTIN = "rotateContin";
	
	static final public int JOINT = 0;
	static final public int X_MARGIN = 1;
	static final public int Y_MARGIN = 2;
	static final public int X_CONDIT = 3;
	static final public int Y_CONDIT = 4;
	
	static final private boolean HIGH = true;
	static final private boolean LOW = false;
	
	static final private int kHalfBarWidth = 6;
	static final private int kFinalFrame = 40;
	static final private int kFramesPerSec = 10;
	
	static final public Color[] outlineColor = {Color.blue, Color.red, new Color(0x006633), Color.black};
	static final public Color[] fillColor = {new Color(0xCCCCFF), new Color(0xFFFF99), new Color(0xFFCCCC), Color.white};
	static final public Color[] darkerFillColor = {new Color(0xADADEE), new Color(0xDDDD51), new Color(0xDDA2A2), new Color(0xEEEEEE)};
	static final public Color[] lighterFillColor = {new Color(0xE6E6FF), new Color(0xFFFFCC), new Color(0xFFE6E6), Color.white};
	static final private Color kGreyColor = new Color(0xAAAAAA);
	
	private String xKey, yKey;
	
	private int startType = JOINT;
	private int endType = JOINT;
	
	private boolean initialisedProbs = false;
	private double[][] conditProb;
	private double[] xMarginalProb;
	
	private String annotationString = "";
	private int xSliceIndex = -1;
	private int ySliceIndex = -1;
	
	public RotateContinView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																						D3Axis zAxis, String yKey, String xKey) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, null);
		
		this.xKey = xKey;
		this.yKey = yKey;
	}
	
	public void setSliceIndices(int xSliceIndex, int ySliceIndex) {
		this.xSliceIndex = xSliceIndex;
		this.ySliceIndex = ySliceIndex;
		repaint();
	}
	
	public void setAnnotation(String annotationString) {
		this.annotationString = annotationString;
		repaint();
	}
	
	public void animateChange(int newType) {
		if (getCurrentFrame() == kFinalFrame)
			startType = endType;
		endType = newType;
		if (endType == X_CONDIT || endType == Y_CONDIT)
			startType = JOINT;
		animateFrames(0, kFinalFrame, kFramesPerSec, null);
	}
	
	private double getFractOnYAxis(int y, int noOfYCats) {
		double yFract0 = (startType == X_MARGIN) ? 0.0 : xAxis.catValToPosition(y, noOfYCats);
		double yFract1 = (endType == X_MARGIN) ? 0.0 : xAxis.catValToPosition(y, noOfYCats);
		return yFract0 + getCurrentFrame() * (yFract1 - yFract0) / kFinalFrame;
	}
	
	private double getFractOnXAxis(int x, int noOfXCats) {
		double xFract0 = (startType == Y_MARGIN) ? 0.0 : zAxis.catValToPosition(x, noOfXCats);
		double xFract1 = (endType == Y_MARGIN) ? 0.0 : zAxis.catValToPosition(x, noOfXCats);
		return xFract0 + getCurrentFrame() * (xFract1 - xFract0) / kFinalFrame;
	}
	
	private double getProbForType(int y, int x, double[][] conditProb, double[] xMarginalProb,
																				int type, boolean highNotLow) {
		double prob = 0.0;
		switch (type) {
			case X_CONDIT:
				for (int i=0 ; i<conditProb.length ; i++)
					prob += xMarginalProb[i] * conditProb[i][y];
				return highNotLow ? (prob > 0) ?conditProb[x][y] * xMarginalProb[x] / prob
												: 1.0 / conditProb[x].length : 0.0;
			case Y_CONDIT:
				return highNotLow ? conditProb[x][y] : 0.0;
			case Y_MARGIN:
				for (int i=0 ; i<x ; i++)
					prob += xMarginalProb[i] * conditProb[i][y];
				break;
			case X_MARGIN:
				for (int j=0 ; j<y ; j++)
					prob += xMarginalProb[x] * conditProb[x][j];
				break;
			default:
		}
		if (highNotLow)
			prob += xMarginalProb[x] * conditProb[x][y];
		return prob;
	}
	
	private double getProb(int y, int x, double[][] conditProb,
										double[] xMarginalProb, boolean highNotLow, int destType) {
		int t0, t1;
		switch (destType) {
			case X_CONDIT:
				int ny = conditProb[0].length;
				t0 = kFinalFrame * y / ny;
				t1 = kFinalFrame * (y + 1) / ny;
				break;
			case Y_CONDIT:
				int nx = xMarginalProb.length;
				t0 = kFinalFrame * x / nx;
				t1 = kFinalFrame * (x + 1) / nx;
				break;
			default:
				t0 = 0;
				t1 = kFinalFrame;
		}
		
		double prob0 = getProbForType(y, x, conditProb, xMarginalProb, startType, highNotLow);
		double prob1 = getProbForType(y, x, conditProb, xMarginalProb, endType, highNotLow);
		
		int t = getCurrentFrame();
		double prob;
		if (t <= t0)
			prob = prob0;
		else if (t >= t1)
			prob = prob1;
		else
			prob = prob0 + (getCurrentFrame() - t0) * (prob1 - prob0) / (t1 - t0);
		
		
		return prob;
	}
	
	private Point getScreenPoint(int y, int x, double prob, int noOfYCats, int noOfXCats, Point thePoint) {
		double xFract = getFractOnYAxis(y, noOfYCats);
		
		double zFract = getFractOnXAxis(x, noOfXCats);
		
		double yFract = yAxis.numValToPosition(prob);
		
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	private void initialiseProbs() {
		if (initialisedProbs)
			return;
		
		int[] xCounts = null;
		
		CatVariableInterface xCore = (CatVariableInterface)getVariable(xKey);
		int nXCats = xCore.noOfCategories();
		if (xCore instanceof CatDistnVariable) {
			CatDistnVariable xVar = (CatDistnVariable)xCore;
			xMarginalProb = xVar.getProbs();
		}
		else {
			CatVariable xVar = (CatVariable)xCore;
			xCounts = xVar.getCounts();
			double xTotal = xVar.noOfValues();
			xMarginalProb = new double[nXCats];
			for (int i=0 ; i<nXCats ; i++)
				xMarginalProb[i] = xCounts[i] / xTotal;
		}
		
		CatVariableInterface yCore = (CatVariableInterface)getVariable(yKey);
		int nYCats = yCore.noOfCategories();
		if (yCore instanceof ContinResponseVariable) {
			ContinResponseVariable yVar = (ContinResponseVariable)yCore;
			conditProb = yVar.getConditionalProbs();
		}
		else {
			CatVariable yVar = (CatVariable)yCore;
			int[][] conditCounts = yVar.getCounts((CatVariable)xCore);
			conditProb = new double[nYCats][nXCats];
			for (int i=0 ; i<nXCats ; i++) {
				double xMargin = xCounts[i];
				for (int j=0 ; j<nYCats ; j++)
					conditProb[i][j] = conditCounts[j][i] / xMargin;
			}
		}
		initialisedProbs = true;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		initialiseProbs();
		
		int nXCats = xMarginalProb.length;
		int nYCats = conditProb[0].length;
		
		double probAngle = map.getTheta2();
		boolean probAxisPointsTowards = probAngle < 180;
		
		Point barBottom = new Point(0, 0);
		Point barTop = new Point(0, 0);
		
		int startX = 0;
		int endX = nXCats - 1;
		boolean xIncreasing = true;
		boolean xStacked = (startType == Y_MARGIN) && (getCurrentFrame() == 0) || (endType == Y_MARGIN) && (getCurrentFrame() == kFinalFrame);
		if (xStacked) {
			if (!probAxisPointsTowards) {
				startX = endX;
				endX = 0;
				xIncreasing = false;
			}
		}
		else if (!map.xAxisBehind()) {
			startX = endX;
			endX = 0;
			xIncreasing = false;
		}
		
		int startY = 0;
		int endY = nYCats - 1;
		boolean yIncreasing = true;
		boolean yStacked = (startType == X_MARGIN) && (getCurrentFrame() == 0) || (endType == X_MARGIN) && (getCurrentFrame() == kFinalFrame);
		if (yStacked) {
			if (!probAxisPointsTowards) {
				startY = endY;
				endY = 0;
				yIncreasing = false;
			}
		}
		else if (!map.zAxisBehind()) {
			startY = endY;
			endY = 0;
			yIncreasing = false;
		}
		
		int boxHt = (int)Math.round(2 * kHalfBarWidth * Math.abs(Math.sin(probAngle * Math.PI / 180.0)));
		
		for (int i=startX ; xIncreasing && (i<=endX) || !xIncreasing && (i>=endX) ; i=xIncreasing?i+1:i-1)
			for (int j=startY ; yIncreasing && (j<=endY) || !yIncreasing && (j>=endY) ; j=yIncreasing?j+1:j-1) {
				double lowProb = getProb(j, i, conditProb, xMarginalProb, LOW, endType);
				double highProb = getProb(j, i, conditProb, xMarginalProb, HIGH, endType);
				
				boolean drawBar = (lowProb != highProb) && (xSliceIndex == -1 || xSliceIndex == i)
																											 && (ySliceIndex == -1 || ySliceIndex == j);
				if (drawBar) {
					barBottom = getScreenPoint(j, i, lowProb, nYCats, nXCats, barBottom);
					barTop = getScreenPoint(j, i, highProb, nYCats, nXCats, barTop);
					
					g.setColor(fillColor[i % fillColor.length]);
					int ovalCenter = probAxisPointsTowards ? barBottom.y : barTop.y;
					g.fillOval(barTop.x - kHalfBarWidth, ovalCenter - boxHt / 2, 2 * kHalfBarWidth, boxHt);
					g.setColor(outlineColor[j % outlineColor.length]);
					g.drawOval(barTop.x - kHalfBarWidth, ovalCenter - boxHt / 2, 2 * kHalfBarWidth, boxHt);
					
					g.drawLine(barTop.x - kHalfBarWidth - 1, barTop.y, barTop.x - kHalfBarWidth - 1, barBottom.y);
					g.drawLine(barTop.x + kHalfBarWidth, barTop.y, barTop.x + kHalfBarWidth, barBottom.y);
					g.setColor(darkerFillColor[i % darkerFillColor.length]);
					g.fillRect(barTop.x - kHalfBarWidth, barTop.y + 1, 2 * kHalfBarWidth, barBottom.y - barTop.y - 1);
					g.setColor(fillColor[i % fillColor.length]);
					g.fillRect(barTop.x - kHalfBarWidth / 2, barTop.y + 1, kHalfBarWidth, barBottom.y - barTop.y - 1);
					
					g.setColor(lighterFillColor[i %lighterFillColor.length]);
					ovalCenter = probAxisPointsTowards ? barTop.y : barBottom.y;
					g.fillOval(barTop.x - kHalfBarWidth, ovalCenter - boxHt / 2, 2 * kHalfBarWidth, boxHt);
					g.setColor(outlineColor[j % outlineColor.length]);
					g.drawOval(barTop.x - kHalfBarWidth, ovalCenter - boxHt / 2, 2 * kHalfBarWidth, boxHt);
				}
			}
		if (annotationString != null) {
			g.setColor(kGreyColor);
			g.setFont(getApplet().getBigBoldFont());
			FontMetrics fm = g.getFontMetrics();
			int labelWidth = fm.stringWidth(annotationString);
			int ascent = fm.getAscent();
			g.drawString(annotationString, getSize().width - labelWidth - 4, ascent + 4);
		}
	}
	
//-----------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		initialisedProbs = false;
		repaint();
	}
}
