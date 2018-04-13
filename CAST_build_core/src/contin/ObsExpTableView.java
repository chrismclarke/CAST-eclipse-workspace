package contin;

import java.awt.*;

import dataView.*;


public class ObsExpTableView extends DataView {
//	static public final String OBSEXPVIEW = "obsExpView";
	
	static final private int kVertBorder = 5;
	static final private int kXLabelBorder = 6;
	
	static final private int kRowGap = 10;
	static final private int kHalfMinColGap = 10;
	
	static final private int kYLabelTopGap = 6;
	static final private int kRightMarginGap = 6;
	static final private int kBottomMarginGap = 3;
	
	static final private Color kObservedColor = Color.blue;
	static final private Color kExpectedColor = Color.red;
	
	private String xKey, yKey;
	private NumValue maxExpected;
	
	private boolean initialised = false;
	
	private int ascent, descent;
	
	private int displayHeight, displayWidth;
//	private int nYCats, nXCats;
	private int maxExpectedWidth, maxObservedWidth;
	private int maxXCatWidth, minColumnWidth, topHeight;
	
	private double[][] expected = null;
	private int[][] observed = null;
	
	public ObsExpTableView(DataSet theData, XApplet applet, String yKey, String xKey, NumValue maxExpected) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.xKey = xKey;
		this.yKey = yKey;
		this.maxExpected = maxExpected;
	}
	
	public void setVariables(String yKey, String xKey) {
		this.xKey = xKey;
		this.yKey = yKey;
	}

//---------------------------------------------------------------------------------
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int nYCats = yVar.noOfCategories();
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int nXCats = xVar.noOfCategories();
		
		maxExpectedWidth = maxExpected.stringWidth(g);
		NumValue maxVal = new NumValue(maxExpected.toDouble(), 0);
		maxObservedWidth = maxVal.stringWidth(g);
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		topHeight = ascent + descent + kYLabelTopGap;
		
		maxXCatWidth = 0;
		for (int i=0 ; i<nXCats ; i++)
			maxXCatWidth = Math.max(maxXCatWidth, xVar.getLabel(i).stringWidth(g));
		
		int maxYCatWidth = 0;
		for (int j=0 ; j<nYCats ; j++)
			maxYCatWidth = Math.max(maxYCatWidth, yVar.getLabel(j).stringWidth(g));
		minColumnWidth = Math.max(maxYCatWidth, maxExpectedWidth) + 2 * kHalfMinColGap;
		
		displayHeight = nXCats * (2 * ascent + descent)
													+ (nXCats - 1) * kRowGap + 2 * kVertBorder;
		displayHeight += topHeight;
		
		if (hasMargins())
			displayHeight += ascent + descent + kBottomMarginGap;
		
		displayWidth = maxXCatWidth + kXLabelBorder + nYCats * minColumnWidth;
		if (hasMargins())
			displayWidth += kRightMarginGap + maxObservedWidth;
		
		expected = null;
		observed = null;
		
		initialised = true;
		return true;
	}
	
	protected boolean hasMargins() {
		return true;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(displayWidth, displayHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public int[][] getObservedArray() {
		if (observed == null) {
			CatVariable xVar = (CatVariable)getVariable(xKey);
			CatVariable yVar = (CatVariable)getVariable(yKey);
			observed = yVar.getCounts(xVar);		//		y is first index
		}
		return observed;
	}
	
	public double[][] getExpectedArray() {
		if (expected == null) {
			int[][] obs = getObservedArray();
			CatVariable yVar = (CatVariable)getVariable(yKey);
			int nYCats = yVar.noOfCategories();
			CatVariable xVar = (CatVariable)getVariable(xKey);
			int nXCats = xVar.noOfCategories();
			
			double[] xMargin = new double[nXCats];
			double[] yMargin = new double[nYCats];
			double total = 0.0;
			for (int i=0 ; i<nXCats ; i++) {
				for (int j=0 ; j<nYCats ; j++) {
					double dCount = obs[j][i];
					xMargin[i] += dCount;
					yMargin[j] += dCount;
				}
				total += xMargin[i];
			}
			
			for (int i=0 ; i<nXCats ; i++)
				xMargin[i] /= total;
			
			expected = new double[nYCats][];
			for (int j=0 ; j<nYCats ; j++) {
				expected[j] = new double[nXCats];
				for (int i=0 ; i<nXCats ; i++)
					expected[j][i] = xMargin[i] * yMargin[j];
			}
		}
		return expected;
	}
	
	public int getDF() {
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable yVar = (CatVariable)getVariable(yKey);
		return (xVar.noOfCategories() - 1) * (yVar.noOfCategories() - 1);
	}
	
	public double getChi2() {
		int[][] obs = getObservedArray();
		double[][] exp = getExpectedArray();
		double total = 0.0;
		for (int j=0 ; j<obs.length ; j++)
			for (int i=0 ; i<obs[0].length ; i++) {
				double diff = obs[j][i] - exp[j][i];
				double term = diff * diff;
				term /= exp[j][i];
				total += term;
			}
		return total;
	}

//---------------------------------------------------------------------------------
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CoreVariable yVar = (CoreVariable)getVariable(yKey);
		CoreVariable xVar = (CoreVariable)getVariable(xKey);
		
		CatVariableInterface yCatVar = (CatVariableInterface)yVar;
		int nYCats = yCatVar.noOfCategories();
		CatVariableInterface xCatVar = (CatVariableInterface)xVar;
		int nXCats = xCatVar.noOfCategories();
		
		int leftOffset = (getSize().width - displayWidth) / 2;
		int topOffset = (getSize().height - displayHeight) / 2;
		
		int actualColWidth = minColumnWidth;
		if (leftOffset < 0) {
			leftOffset = 0;
			int borderWidth = maxXCatWidth + kXLabelBorder;
			if (hasMargins())
				borderWidth += kRightMarginGap + maxObservedWidth;
			actualColWidth = (getSize().width - borderWidth) / nYCats;
		}
		
		int boxLeft = leftOffset + maxXCatWidth + kXLabelBorder;
		int boxTop = topOffset + topHeight;
		int boxWidth = nYCats * actualColWidth;
		int boxHeight = nXCats * (2 * ascent + descent) + (nXCats - 1) * kRowGap + 2 * kVertBorder;
		
		g.setColor(Color.white);
		g.fillRect(boxLeft, boxTop, boxWidth, boxHeight);
		
		g.setColor(getForeground());
		g.drawRect(boxLeft, boxTop, boxWidth - 1, boxHeight - 1);
		
		int[][] obs = getObservedArray();
		double[][] exp = getExpectedArray();
		
		int baseline = boxTop - kYLabelTopGap - descent;
		int colCenter = boxLeft + actualColWidth / 2;
		
		for (int j=0 ; j<nYCats ; j++) {
			yCatVar.getLabel(j).drawCentred(g, colCenter, baseline);
			colCenter += actualColWidth;
		}
		
		int obsBaseline = boxTop + kVertBorder + ascent;
		int expBaseline = obsBaseline + ascent + descent;
		int labelBaseline = (obsBaseline + expBaseline) / 2;
		int lineHt = 2 * ascent + descent + kRowGap;
		int colOneRight = boxLeft + maxObservedWidth + (actualColWidth - maxExpectedWidth) / 2;
		int marginRight = boxLeft + boxWidth + kRightMarginGap + maxObservedWidth;
		
		for (int i=0 ; i<nXCats ; i++) {
			g.setColor(getForeground());
			xCatVar.getLabel(i).drawLeft(g, boxLeft - kXLabelBorder, labelBaseline);
			
			int colRight = colOneRight;
			int total = 0;
			for (int j=0 ; j<nYCats ; j++) {
				NumValue oij = new NumValue(obs[j][i], 0);
				total += obs[j][i];
				NumValue eij = new NumValue(exp[j][i], maxExpected.decimals);
				
				g.setColor(kObservedColor);
				oij.drawLeft(g, colRight, obsBaseline);
				g.setColor(kExpectedColor);
				eij.drawAtPoint(g, colRight, expBaseline);
				colRight += actualColWidth;
			}
			if (hasMargins()) {
				g.setColor(kObservedColor);
				new NumValue(total, 0).drawLeft(g, marginRight, labelBaseline);
			}
			
			obsBaseline += lineHt;
			expBaseline += lineHt;
			labelBaseline += lineHt;
		}
		
		if (hasMargins()) {
			g.setColor(kObservedColor);
			baseline = boxTop + boxHeight + kBottomMarginGap + ascent;
			int colRight = colOneRight;
			for (int j=0 ; j<nYCats ; j++) {
				int total = 0;
				for (int i=0 ; i<nXCats ; i++)
					total += obs[j][i];
				new NumValue(total, 0).drawLeft(g, colRight, baseline);
				colRight += actualColWidth;
			}
			g.setColor(getForeground());
			new NumValue(((CatVariable)xVar).noOfValues(), 0).drawLeft(g, marginRight, baseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(xKey) || key.equals(yKey)) {
			expected = null;
			observed = null;
			repaint();
		}
	}
	
	public void changedCategoryLabels() {
		initialised = false;
		expected = null;
		observed = null;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
