package contin;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import propnVenn.*;


public class AreaContinView extends DataView {
//	static public final String AREA_CONTIN = "areaContin";
	
	static final public boolean Y_MARGIN = true;
	static final public boolean X_MARGIN = false;
	
	static final private int kFinalFrame = 40;
	static final private int kFramesPerSec = 10;
	
	static final private int kXLabelBorder = 5;
	static final private int kTableTopGap = 14;
	static final private int kTableRightGap = 14;
	static final private int kArrowSize = 5;
	
	static final private int kProbDecimals = 3;
	static final private NumValue kZero = new NumValue(0.0, kProbDecimals);
	
	static final private Color[] outlineColor = RotateContinView.outlineColor;
	static final private Color[] fillColor = RotateContinView.fillColor;
	
	private String xKey, yKey;
	private VertAxis yAxis;
	private HorizAxis xAxis;
	
	private boolean marginForY = Y_MARGIN;
	private boolean initialised = false;
	private double[][] yConditXProb, xConditYProb;
	private double[] xMarginalProb, yMarginalProb;
	
	private boolean adjustedBorder = false;
	private int ascent, descent;
	
	private int selectedX = -1;
	private int selectedY = -1;
	
	private JointProbChoice theChoice;
	
	public AreaContinView(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
						String xKey) {
		super(theData, applet, new Insets(8, 8, 8, 8));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.yAxis = yAxis;
		this.xAxis = xAxis;
		
		setFrame(kFinalFrame);
	}
	
	public int getSelectedX() {
		return selectedX;
	}
	
	public int getSelectedY() {
		return selectedY;
	}
	
	public void setJointProbChoice(JointProbChoice theChoice) {
		this.theChoice = theChoice;
	}
	
	public void animateChange(boolean newMargin) {
		marginForY = newMargin;
//		selectedX = selectedY = -1;
		animateFrames(0, kFinalFrame, kFramesPerSec, null);
	}
	
	public double getXMarginProb(int x) {
		initialise();
		return (x >= 0) ? xMarginalProb[x] : 0.0;
	}
	
	public double getYMarginProb(int y) {
		initialise();
		return (y >= 0) ? yMarginalProb[y] : 0.0;
	}
	
	public double getXConditProb(int x, int y) {
		initialise();
		return (x >= 0 && y >= 0) ? xConditYProb[y][x] : 0.0;
	}
	
	public double getYConditProb(int y, int x) {
		initialise();
		return (x >= 0 && y >= 0) ? yConditXProb[x][y] : 0.0;
	}
	
	private boolean initialise() {
		if (initialised)
			return false;
		
		ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
		int nYCats = yVar.noOfCategories();
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		int nXCats = xVar.noOfCategories();
		
		yConditXProb = yVar.getConditionalProbs();
		xMarginalProb = xVar.getProbs();
		
		yMarginalProb = new double[nYCats];
		xConditYProb = new double[nYCats][];
		for (int j=0 ; j<nYCats ; j++) {
			for (int i=0 ; i<nXCats ; i++)
				yMarginalProb[j] += xMarginalProb[i] * yConditXProb[i][j];
			xConditYProb[j] = new double[nXCats];
			for (int i=0 ; i<nXCats ; i++)
				xConditYProb[j][i] = (yMarginalProb[j] >= 0.0)
										? xMarginalProb[i] * yConditXProb[i][j] / yMarginalProb[j]
										: 1.0 / nYCats;
		}
		
		initialised = true;
		return true;
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
			
			int htForLabels = fm.getAscent() + fm.getDescent() + 2 * kXLabelBorder + kTableTopGap;
			int widthForLabels = Math.max(maxYCatLength + maxXCatLength + fm.stringWidth("P( | )"),
											kZero.stringWidth(g) + fm.stringWidth(" = ")) + kTableRightGap;
			
			border.top += htForLabels;
			border.right += widthForLabels;
			adjustedBorder = true;
			return border;
		}
	}
	
	private Rectangle getPosOnAxes(int y, int x, int noOfYCats, int noOfXCats,
										Rectangle tempRect, Point tempTopLeft, Point tempBottomRight) {
		Rectangle r = (tempRect == null) ? new Rectangle(0, 0, 0, 0) : tempRect;
		
		double yFractLow0 = 0.0;
		double yFractLow1 = 0.0;
		for (int j=0 ; j<y ; j++) {
			yFractLow0 += yMarginalProb[j];
			yFractLow1 += yConditXProb[x][j];
		}
		double yFractHigh0 = yFractLow0 + yMarginalProb[y];
		double yFractHigh1 = yFractLow1 + yConditXProb[x][y];
		if (marginForY) {
			double temp = yFractLow0;
			yFractLow0 = yFractLow1;
			yFractLow1 = temp;
			temp = yFractHigh0;
			yFractHigh0 = yFractHigh1;
			yFractHigh1 = temp;
		}
		
		double xFractLow0 = 0.0;
		double xFractLow1 = 0.0;
		for (int i=0 ; i<x ; i++) {
			xFractLow1 += xMarginalProb[i];
			xFractLow0 += xConditYProb[y][i];
		}
		double xFractHigh1 = xFractLow1 + xMarginalProb[x];
		double xFractHigh0 = xFractLow0 + xConditYProb[y][x];
		if (marginForY) {
			double temp = xFractLow1;
			xFractLow1 = xFractLow0;
			xFractLow0 = temp;
			temp = xFractHigh1;
			xFractHigh1 = xFractHigh0;
			xFractHigh0 = temp;
		}
		
		double yLowFract = yFractLow0 + getCurrentFrame() * (yFractLow1 - yFractLow0) / kFinalFrame;
		double yHighFract = yFractHigh0 + getCurrentFrame() * (yFractHigh1 - yFractHigh0) / kFinalFrame;
		double xLowFract = xFractLow0 + getCurrentFrame() * (xFractLow1 - xFractLow0) / kFinalFrame;
		double xHighFract = xFractHigh0 + getCurrentFrame() * (xFractHigh1 - xFractHigh0) / kFinalFrame;
		
		int yTop = yAxis.numValToRawPosition(yHighFract);
		int yBottom = yAxis.numValToRawPosition(yLowFract);
		int xLeft = xAxis.numValToRawPosition(xLowFract);
		int xRight = xAxis.numValToRawPosition(xHighFract);
		
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
	
	private void drawHorizArrow(Graphics g, int x, int y, int width) {
		g.drawLine(x, y, x + width - 1, y);
		g.drawLine(x, y - 1, x + width - 1, y - 1);
		
		g.drawLine(x, y, x + kArrowSize, y + kArrowSize);
		g.drawLine(x, y - 1, x + kArrowSize, y - 1 - kArrowSize);
		g.drawLine(x + 1, y, x + 1 + kArrowSize, y + kArrowSize);
		g.drawLine(x + 1, y - 1, x + 1 + kArrowSize, y - 1 - kArrowSize);
		
		g.drawLine(x + width - 1, y, x + width - 1 - kArrowSize, y + kArrowSize);
		g.drawLine(x + width - 1, y - 1, x + width - 1 - kArrowSize, y - 1 - kArrowSize);
		g.drawLine(x + width - 2, y, x + width - 2 - kArrowSize, y + kArrowSize);
		g.drawLine(x + width - 2, y - 1, x + width - 2 - kArrowSize, y - 1 - kArrowSize);
	}
	
	private void drawVertArrow(Graphics g, int x, int y, int height) {
		int arrowSize = Math.max(Math.min(kArrowSize, height / 2 - 3), 1);
		boolean doubleWidth = (arrowSize >= 3);
		int x1 = doubleWidth ? x + 1 : x;
		
		g.drawLine(x, y, x, y + height - 1);
		g.drawLine(x1, y, x1, y + height - 1);
		
		g.drawLine(x, y, x - arrowSize, y + arrowSize);
		g.drawLine(x1, y, x1 + arrowSize, y + arrowSize);
		g.drawLine(x, y + height - 1, x - arrowSize, y + height - 1 - arrowSize);
		g.drawLine(x1, y + height - 1, x1 + arrowSize, y + height - 1 - arrowSize);
		
		if (doubleWidth) {
			g.drawLine(x, y + 1, x - arrowSize, y + arrowSize + 1);
			g.drawLine(x1, y + 1, x1 + arrowSize, y + arrowSize + 1);
			g.drawLine(x, y + height - 2, x - arrowSize, y + height - 2 - arrowSize);
			g.drawLine(x1, y + height - 2, x1 + arrowSize, y + height - 2 - arrowSize);
		}
	}
	
	private String getMarginStringStart(Value mainCat) {
		return "P(" + mainCat.toString() + ")";
	}
	
	private String getConditStringStart(Value mainCat, Value otherCat) {
		return "P(" + mainCat.toString() + " | " + otherCat.toString() + ")";
	}
	
	private String getProbString(double p) {
		NumValue prob = new NumValue(p, kProbDecimals);
		return " = " + prob.toString();
	}
	
/*
	private String getMarginStringValue(double[] marginProb) {
		NumValue prob = new NumValue(marginForY ? xConditYProb[selectedY][selectedX]
															: xMarginalProb[selectedX], kProbDecimals);
		return " = " + prob.toString();
	}
*/
	
	public void paintView(Graphics g) {
		initialise();
		
		ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
		int nYCats = yVar.noOfCategories();
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		int nXCats = xVar.noOfCategories();
		
		Rectangle r = null;
		Point p1 = null;
		Point p2 = null;
		
		int xSelectedPos = 0;
		int xSelectedWidth = 0;
		int ySelectedPos = 0;
		int ySelectedHeight = 0;
		
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++) {
				r = getPosOnAxes(j, i, nYCats, nXCats, r, p1, p2);
				
				g.setColor(fillColor[i]);
				g.fillRect(r.x, r.y, r.width, r.height);
				
				g.setColor(outlineColor[j]);
				g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
				g.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
				
				if (i == selectedX && j == selectedY) {
					xSelectedPos = r.x;
					xSelectedWidth = r.width;
					ySelectedPos = r.y;
					ySelectedHeight = r.height;
					g.setColor(Color.yellow);
					g.drawRect(r.x + 2, r.y + 2, r.width - 5, r.height - 5);
					g.drawRect(r.x + 3, r.y + 3, r.width - 7, r.height - 7);
					g.drawRect(r.x + 4, r.y + 4, r.width - 9, r.height - 9);
				}
			}
		
		int xLabelBaseline = getViewBorder().top - kTableTopGap - kXLabelBorder - descent;
		for (int i=0 ; i<nXCats ; i++) {
			Value xLabel = xVar.getLabel(i);
			int width = xLabel.stringWidth(g);
			
			if (i == selectedX) {
				int arrowBaseline = getViewBorder().top - kTableTopGap / 2;
				g.setColor(getForeground());
				drawHorizArrow(g, xSelectedPos, arrowBaseline, xSelectedWidth);
				
				String probString = marginForY
								? getConditStringStart(xLabel, yVar.getLabel(selectedY))
								: getMarginStringStart(xLabel);
				probString += getProbString(marginForY ? xConditYProb[selectedY][selectedX]
																	: xMarginalProb[selectedX]);
				
				Font oldFont = g.getFont();
				g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
				width = g.getFontMetrics().stringWidth(probString);
				int xLeft = Math.max(0, Math.min(getSize().width - width, xSelectedPos + (xSelectedWidth - width) / 2));
				g.setColor(fillColor[i]);
				g.fillRect(xLeft - kXLabelBorder, xLabelBaseline - ascent - kXLabelBorder,
									width + 2 * kXLabelBorder, ascent + descent + 2 * kXLabelBorder);
				g.setColor(Color.white);
				g.drawRect(xLeft - kXLabelBorder, xLabelBaseline - ascent - kXLabelBorder,
							width + 2 * kXLabelBorder - 1, ascent + descent + 2 * kXLabelBorder - 1);
				g.setColor(getForeground());
				g.drawString(probString, xLeft, xLabelBaseline);
				g.setFont(oldFont);
			}
			else {
				int xCenter = getXLabelCenter(i, p1);
				int xLeft = Math.max(0, Math.min(getSize().width - width, xCenter - width / 2));
				g.setColor(fillColor[i]);
				g.fillRect(xLeft - kXLabelBorder, xLabelBaseline - ascent - kXLabelBorder,
									width + 2 * kXLabelBorder, ascent + descent + 2 * kXLabelBorder);
				g.setColor(getForeground());
				xLabel.drawRight(g, xLeft, xLabelBaseline);
			}
		}
		
		int yLabelLeft = translateToScreen(xAxis.numValToRawPosition(1.0), 0, p1).x
																							+ kTableRightGap;
		for (int j=0 ; j<nYCats ; j++) {
			Value yLabel = yVar.getLabel(j);
			if (j == selectedY) {
				int arrowHoriz = getSize().width - getViewBorder().right + kTableRightGap / 2;
				g.setColor(getForeground());
				drawVertArrow(g, arrowHoriz, ySelectedPos, ySelectedHeight);
				
				String probString1 = !marginForY
								? getConditStringStart(yLabel, xVar.getLabel(selectedX))
								: getMarginStringStart(yLabel);
				String probString2 = getProbString(!marginForY ? yConditXProb[selectedX][selectedY]
																	: yMarginalProb[selectedY]);
				
				g.setColor(outlineColor[j]);
				Font oldFont = g.getFont();
				g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
				
				int yBaseline = ySelectedPos + ySelectedHeight / 2 - descent - 1;
				g.drawString(probString1, yLabelLeft, yBaseline);
				g.drawString(probString2, yLabelLeft, yBaseline + ascent + descent + 2);
				
				g.setFont(oldFont);
			}
			else {
				int yBaseline = getYLabelBaseline(j, p1);
				g.setColor(outlineColor[j]);
				yLabel.drawRight(g, yLabelLeft, yBaseline);
			}
		}
		if (getCurrentFrame() == kFinalFrame && theChoice != null)
			theChoice.endTransition();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}

	public void mousePressed(MouseEvent e) {
		if (getCurrentFrame() != kFinalFrame)			//		we don't want super.mousePressed()
			return;										//		to pause the animation
		super.mousePressed(e);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (getCurrentFrame() != kFinalFrame)
			return null;
		
		ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
		int nYCats = yVar.noOfCategories();
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		int nXCats = xVar.noOfCategories();
		
		try {
			Point hitPos = translateFromScreen(x, y, null);
			double yTarget = yAxis.positionToNumVal(hitPos.y);
			double xTarget = xAxis.positionToNumVal(hitPos.x);
			if (marginForY) {
				double yCum = 0.0;
				for (int j=0 ; j<nYCats ; j++) {
					yCum += yMarginalProb[j];
					if (yTarget <= yCum) {
						double xCum = 0.0;
						for (int i=0 ; i<nXCats ; i++) {
							xCum += xConditYProb[j][i];
							if (xTarget <= xCum)
								return new ContinCatInfo(i, j);
						}
					}
				}
			}
			else {
				double xCum = 0.0;
				for (int i=0 ; i<nXCats ; i++) {
					xCum += xMarginalProb[i];
					if (xTarget <= xCum) {
						double yCum = 0.0;
						for (int j=0 ; j<nYCats ; j++) {
							yCum += yConditXProb[i][j];
							if (yTarget <= yCum)
								return new ContinCatInfo(i, j);
						}
					}
				}
			}
		} catch (AxisException e) {
		}
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ContinCatInfo catInfo = (ContinCatInfo)startInfo;
		int newX = (catInfo == null) ? -1 : catInfo.xIndex;
		int newY = (catInfo == null) ? -1 : catInfo.yIndex;
		if (newX != selectedX || newY != selectedY) {
			selectedX = newX;
			selectedY = newY;
			repaint();
			if (theChoice != null)
				theChoice.repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
