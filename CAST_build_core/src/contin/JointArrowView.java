package contin;

import java.awt.*;

import dataView.*;


public class JointArrowView extends DataView {
//	static public final String JOINTARROW = "jointArrow";
	
	static final private int kArrowWidth = 25;
	static final private int kLineWidth = 5;
	static final private int kValueHorizGap = 5;
	static final private int kMinValueVertGap = 15;
	static final private int kSignWidth = 12;
	static final protected int kSignSize = 6;
	static final private int kLeading = 4;
	
	protected String xKey, yKey;
	private int probDecimals;
	private NumValue zeroVal;
	
	private CoreTableView linkedTable;
	
	private boolean initialised = false;
	
	protected int ascent;
	private int zeroWidth;
	private int displayHeight, displayWidth;
	
	protected int selectedX = -1;
	protected int selectedY = -1;
	
	private int[] arrowX = new int[8];
	private int[] arrowY = new int[8];
	
	public JointArrowView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.probDecimals = probDecimals;
		zeroVal = new NumValue(0.0, probDecimals);
		
		setFont(applet.getStandardBoldFont());
		setForeground(Color.red);
	}
	
	public void setLinkedTable(CoreTableView linkedTable) {
		this.linkedTable = linkedTable;
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
		
		displayHeight = 2 * kMinValueVertGap + noOfValues() * ascent
										+ (noOfValues() - 1) * kLeading + (kArrowWidth + 1) / 2;
		displayWidth = (kArrowWidth + kLineWidth) / 2 + kSignWidth + zeroWidth
																						+ 2 * kValueHorizGap;
		return new Dimension(displayWidth, displayHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected int noOfValues() {
		return 2;
	}
	
	protected void drawSign(Graphics g, int horizPos, int baseline) {
																//		Times
		g.drawLine(horizPos, baseline - 1, horizPos + kSignSize, baseline - 1 - kSignSize);
		g.drawLine(horizPos, baseline - 1 - kSignSize, horizPos + kSignSize, baseline - 1);
	}
	
	protected double[] getValues(ContinResponseVariable yVar, CatDistnVariable xVar) {
		double[][] yConditXProb = yVar.getConditionalProbs();
		double[] xMarginalProb = xVar.getProbs();
		
		double[] vals = new double[2];
		vals[0] = xMarginalProb[selectedX];
		vals[1] = yConditXProb[selectedX][selectedY];
		return vals;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int horizOffset = (linkedTable == null) ? 0
											: linkedTable.getTableCenter() - (kArrowWidth - 1) / 2;
		horizOffset = Math.min(horizOffset, getSize().width - displayWidth);
		
		arrowX[7] = arrowX[1] = arrowX[0] = horizOffset + (kArrowWidth - kLineWidth) / 2;
		arrowX[2] = horizOffset;
		arrowX[3] = horizOffset + kArrowWidth / 2;
		arrowX[4] = horizOffset + kArrowWidth - 1;
		arrowX[6] = arrowX[5] = horizOffset + (kArrowWidth + kLineWidth) / 2 - 1;
		
		arrowY[7] = arrowY[6] = arrowY[0] = 0;
		arrowY[5] = arrowY[4] = arrowY[2] = arrowY[1] = getSize().height - (kArrowWidth + 1) / 2;
		arrowY[3] = getSize().height - 1;
		
		g.fillPolygon(arrowX, arrowY, 8);
		g.drawPolygon(arrowX, arrowY, 8);
		
		if (selectedX >= 0 || selectedY >= 0) {
			ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
			CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
			
			int baseline = (getSize().height - (kArrowWidth + 1) / 2
								- (noOfValues() - 2) * ascent - (noOfValues() - 1) * kLeading) / 2;
			int horizStart = horizOffset + (kArrowWidth + kLineWidth) / 2 + kValueHorizGap;
			
			g.setColor(Color.yellow);
			g.fillRect(horizStart - kValueHorizGap, baseline - ascent - kLeading,
											2 * kValueHorizGap + kSignWidth + zeroWidth,
											noOfValues() * ascent + (noOfValues() + 1) * kLeading);
			g.setColor(getForeground());
			
			double[] vals = getValues(yVar, xVar);
			(new NumValue(vals[0], probDecimals)).drawRight(g, horizStart, baseline);
			for (int i=1 ; i<vals.length ; i++) {
				baseline += ascent + kLeading;
				drawSign(g, horizStart, baseline);
				(new NumValue(vals[i], probDecimals)).drawRight(g, horizStart + kSignWidth,
																											baseline);
			}
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
