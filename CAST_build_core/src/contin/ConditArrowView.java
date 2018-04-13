package contin;

import java.awt.*;

import dataView.*;


public class ConditArrowView extends DataView {
//	static public final String CONDITARROW = "conditArrow";
	
	static final private int kArrowWidth = 25;
	static final private int kLineWidth = 5;
	static final private int kValueVertGap = 5;
	static final private int kMinValueHorizGap = 10;
	static final private int kLeading = 5;
	
	protected String xKey, yKey;
	private int probDecimals;
	private NumValue zeroVal;
	
	private MarginArrowView linkedArrow;
	
	private boolean initialised = false;
	
	protected int ascent;
	private int zeroWidth;
	private int displayHeight, displayWidth;
	
	protected int selectedX = -1;
	protected int selectedY = -1;
	
	private int[] arrowX = new int[8];
	private int[] arrowY = new int[8];
	
	public ConditArrowView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals,
						MarginArrowView linkedArrow) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.probDecimals = probDecimals;
		this.linkedArrow = linkedArrow;
		zeroVal = new NumValue(0.0, probDecimals);
		
		setFont(applet.getStandardBoldFont());
		setForeground(Color.red);
	}
	
	public void setSelection(int selectedX, int selectedY) {
		this.selectedX = selectedX;
		this.selectedY = selectedY;
		repaint();
	}

//---------------------------------------------------------------------------------
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		zeroWidth = zeroVal.stringWidth(g);
		
		initialised = true;
		return true;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		displayWidth = 2 * kMinValueHorizGap + 2 * ascent + kLeading + (kArrowWidth + 1) / 2;
		displayHeight = (kArrowWidth + kLineWidth) / 2 + zeroWidth + 2 * kValueVertGap;
		return new Dimension(displayWidth, displayHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected double getNumerator(double[][] yConditXProb, double[] xMarginalProb) {
		return xMarginalProb[selectedX] * yConditXProb[selectedX][selectedY];
	}
	
	protected double getDenominator(double[][] yConditXProb, double[] xMarginalProb) {
		double val = 0.0;
		for (int i=0 ; i<xMarginalProb.length ; i++)
			val += xMarginalProb[i] * yConditXProb[i][selectedY];
		return val;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int vertOffset = linkedArrow.getSize().height / 2 - (kArrowWidth - 1) / 2;
		
		arrowY[7] = arrowY[1] = arrowY[0] = vertOffset + (kArrowWidth - kLineWidth) / 2;
		arrowY[2] = vertOffset;
		arrowY[3] = vertOffset + kArrowWidth / 2;
		arrowY[4] = vertOffset + kArrowWidth - 1;
		arrowY[6] = arrowY[5] = vertOffset + (kArrowWidth + kLineWidth) / 2 - 1;
		
		arrowX[7] = arrowX[6] = arrowX[0] = 0;
		arrowX[5] = arrowX[4] = arrowX[2] = arrowX[1] = getSize().width - (kArrowWidth + 1) / 2;
		arrowX[3] = getSize().width - 1;
		
		g.fillPolygon(arrowX, arrowY, 8);
		g.drawPolygon(arrowX, arrowY, 8);
		
		if (selectedX >= 0 || selectedY >= 0) {
			ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
			CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
			double[][] yConditXProb = yVar.getConditionalProbs();
			double[] xMarginalProb = xVar.getProbs();
			
			int horizStart = (getSize().width - (kArrowWidth + 1) / 2 - zeroWidth) / 2;
			int baseline = vertOffset + (kArrowWidth + kLineWidth) / 2 + kValueVertGap
																											+ ascent;
			
			g.setColor(Color.yellow);
			g.fillRect(horizStart - kMinValueHorizGap, baseline - ascent - kLeading,
											2 * kMinValueHorizGap + zeroWidth,
											2 * ascent + 3 * kLeading);
			g.setColor(getForeground());
			
			double numer = getNumerator(yConditXProb, xMarginalProb);
			double denom = getDenominator(yConditXProb, xMarginalProb);
			(new NumValue(numer, probDecimals)).drawRight(g, horizStart, baseline);
			g.drawLine(horizStart - 1, baseline + kLeading / 2, horizStart + zeroWidth,
																					baseline + kLeading / 2);
			baseline += ascent + kLeading;
			(new NumValue(denom, probDecimals)).drawRight(g, horizStart, baseline);
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
