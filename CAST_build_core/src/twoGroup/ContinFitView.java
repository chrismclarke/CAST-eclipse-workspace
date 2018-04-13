package twoGroup;

import java.awt.*;

import dataView.*;
import bivarCat.*;
import random.*;


public class ContinFitView extends TwoWayView {
	static final public int OUTER = 0;
	static final public int INNER = 1;
	
	static final private int kRowTextGap = 5;				//	between rows of cat names
	static final private int kObsFitGap = 8;		//	4	//	between observed and fitted counts
	static final private int kRowFitGap = 12;		//	10	//	between rows with observed & fitted
	
	static final private int kCatHorizGap = 12;	//	between y-cat names at top
	static final private int kYNameCatGap = 3;	//	between y-varName & cat names
	
	static final private int kTableOuterBorder = 3;
	static final private int kTableInnerBorder = 3;
	
//	static final private Color kFitColor = Color.red;
	static final private Color kFitBackgroundColor = new Color(0xDDDDDD);
	
	static final private LabelValue kBlankValue = new LabelValue("?");
	
	private int propnDecimals, fitDecimals;
	private String modelKey;
	private Color[] groupColor;
	
	private int xCatWidth, xVarNameWidth, yVarNameWidth, biggestCountWidth, fitDecimalsOffset,
																					biggestPropnWidth, totalWidth;
	private int yCatWidth[];
	private int boldAscent, boldDescent, ascent, descent;
	private int rowHeight, columnWidth;
	private int tableLeft, tableTop, tableRight, tableBottom, tableWidth, tableHeight;
	
	private int totalCount;
	
	private boolean layoutInitialised = false;
	
	public ContinFitView(DataSet theData, XApplet applet, String xKey, String yKey, String modelKey,
									int propnDecimals, int fitDecimals, Color[] groupColor) {
		super(theData, applet, xKey, yKey);
		this.propnDecimals = propnDecimals;
		this.modelKey = modelKey;
		this.fitDecimals = fitDecimals;
		this.groupColor = groupColor;
	}
	
	public ContinFitView(DataSet theData, XApplet applet, String xKey, String yKey, String modelKey,
									int propnDecimals, int fitDecimals) {
		this(theData, applet, xKey, yKey, modelKey, propnDecimals, fitDecimals, null);
	}
	
	private void initialiseLayout(CatVariable xVariable, CatVariable yVariable, Graphics g) {
		if (layoutInitialised)
			return;
		layoutInitialised = true;
		
		int noOfXCats = xVariable.noOfCategories();
		int noOfYCats = yVariable.noOfCategories();
		
		int maxYCount = 0;
		totalCount = 0;
		for (int i=0 ; i<noOfYCats ; i++) {
			maxYCount = Math.max(maxYCount, yCounts[i]);
			totalCount += yCounts[i];
		}
		
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
		FontMetrics fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		boldDescent = fm.getDescent();
		xVarNameWidth = fm.stringWidth(xVariable.name);
		yVarNameWidth = fm.stringWidth(yVariable.name);
		
		g.setFont(oldFont);
		fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		xCatWidth = 0;
		for (int i=0 ; i<noOfXCats ; i++) {
			int nameWidth = xVariable.getLabel(i).stringWidth(g);
			if (nameWidth > xCatWidth)
				xCatWidth = nameWidth;
		}
		
		rowHeight = (modelKey == null) ? ascent + descent + kRowTextGap
												: ascent * 2 + kObsFitGap + kRowFitGap;
		tableLeft = Math.max(xCatWidth, xVarNameWidth) + kTableOuterBorder;
		tableHeight = noOfXCats * rowHeight  + 2 * kTableInnerBorder;
		
		yCatWidth = new int[noOfYCats];
		columnWidth = 0;
		int previousSpace = 100;
		for (int i=0 ; i<noOfYCats ; i++) {
			yCatWidth[i] = ((CatVariable)yVariable).getLabel(i).stringWidth(g);
			int leftGap = (columnWidth - yCatWidth[i]) / 2;
			columnWidth += Math.max(Math.max(0, -leftGap),
																	kCatHorizGap - previousSpace - leftGap);
			previousSpace = (columnWidth - yCatWidth[i]) / 2;
		}
		biggestCountWidth = fm.stringWidth(String.valueOf(maxYCount));
		if (modelKey != null) {
			int biggestCountPlusDecimals = (new NumValue(maxYCount, fitDecimals)).stringWidth(g);
			fitDecimalsOffset = biggestCountPlusDecimals - biggestCountWidth;
			biggestCountWidth = biggestCountPlusDecimals;
		}
		biggestPropnWidth = (new NumValue(1.0, propnDecimals)).stringWidth(g);
		columnWidth = Math.max(columnWidth, Math.max(biggestCountWidth, biggestPropnWidth)
																									+ kCatHorizGap);
		columnWidth = Math.max(columnWidth, (yVarNameWidth + noOfYCats - 1) / noOfYCats);
		tableWidth = noOfYCats * columnWidth  + 2 * kTableInnerBorder;
		
		tableTop = boldAscent + boldDescent + kYNameCatGap + ascent + descent + kTableOuterBorder;
		
		totalWidth = fm.stringWidth(String.valueOf(totalCount));
		tableRight = kTableOuterBorder + Math.max(totalWidth, biggestPropnWidth);
		
		tableBottom = ascent + descent + kTableOuterBorder;
	}
	
	private boolean initialise(CatVariable xVariable, CatVariable yVariable, Graphics g) {
		if (super.initialise(xVariable, yVariable)) {
			initialiseLayout(xVariable, yVariable, g);
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		CatVariable x = (CatVariable)getVariable(xKey);
		int noOfXCats = x.noOfCategories();
		CatVariable y = (CatVariable)getVariable(yKey);
		int noOfYCats = y.noOfCategories();
		
		double probs[] = null;
		int[] sampleSizes = null;
		if (modelKey != null) {
			CatDistnVariable model = (CatDistnVariable)getVariable(modelKey);
			probs = model.getProbs();
			
			CatSampleVariable ySamp = (CatSampleVariable)y;
			RandomProductMulti generator = (RandomProductMulti)ySamp.getGenerator();
			sampleSizes = generator.getSampleSizes();
		}
		else
			sampleSizes = x.getCounts();
		
		initialise(x, y, g);
		
		int leftBorder = (getSize().width - tableLeft - tableRight - tableWidth) / 2;
		int topBorder = (getSize().height - tableTop - tableBottom - tableHeight) / 2;
		g.translate(leftBorder, topBorder);		//		MUST be reset later
		
		g.drawRect(tableLeft, tableTop, tableWidth - 1, tableHeight - 1);
		g.setColor(Color.white);
		g.fillRect(tableLeft + 1, tableTop + 1, tableWidth - 2, tableHeight - 2);
		g.setColor(getForeground());
		
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
		g.drawString(y.name, tableLeft + (tableWidth - yVarNameWidth) / 2, boldAscent);
		g.drawString(x.name, 0, tableTop - descent - kTableOuterBorder);
		g.setFont(oldFont);
		
		int horizCenter = tableLeft + kTableInnerBorder + columnWidth / 2;
		for (int i=0 ; i<noOfYCats ; i++) {
			y.getLabel(i).drawCentred(g, horizCenter,
															boldAscent + boldDescent + kYNameCatGap + ascent);
			horizCenter += columnWidth;
		}
		
		int baseline = tableTop + kTableInnerBorder + (rowHeight + ascent - descent) / 2;
		int obsOffset = 0;
		int fitOffset = 0;
		if (modelKey != null) {
			obsOffset = - (ascent + kObsFitGap) / 2;
			fitOffset = obsOffset + ascent + kObsFitGap;
		}
		int valueWidth = (vertScale == COUNT) ? biggestCountWidth : biggestPropnWidth;
		int startValRight = tableLeft + kTableInnerBorder + (columnWidth + valueWidth) / 2;
		if (vertScale == COUNT && modelKey != null)
			startValRight -= fitDecimalsOffset;
		int totalColumnRight = tableLeft + tableWidth + kTableOuterBorder;
		if (vertScale == COUNT)
			totalColumnRight += totalWidth;
		else if (vertScale == PROPN_IN_X)
			totalColumnRight +=  biggestPropnWidth;
		
		for (int i=0 ; i<noOfXCats ; i++) {
			if (modelKey != null) {
				g.setColor(kFitBackgroundColor);
				g.fillRect(tableLeft + 1, baseline + fitOffset - ascent - kObsFitGap / 2,
																			tableWidth - 2, ascent + kObsFitGap);
			}
			if (groupColor != null)
				g.setColor(groupColor[i]);
			else
				g.setColor(getForeground());
			x.getLabel(i).drawRight(g, tableLeft - kTableOuterBorder - xCatWidth, baseline);
			
			int right = startValRight;
			for (int j=0 ; j<noOfYCats ; j++) {
				Value value = null;
				if (xCounts[i] == 0)
					value =  kBlankValue;
				else if (vertScale == COUNT)
					value = new NumValue(jointCounts[i][j], 0);
				else if (vertScale == PROPN_IN_X)
					value = new NumValue(((double)jointCounts[i][j]) / xCounts[i], propnDecimals);
				value.drawLeft(g, right, baseline + obsOffset);
				if (modelKey != null && vertScale != PROPN_IN_Y) {
//					g.setColor(kFitColor);
					double p = probs[i * noOfYCats + j];
					if (vertScale == COUNT)
						(new NumValue(sampleSizes[i] * p, fitDecimals)).drawAtPoint(g, right, baseline + fitOffset);
					else
						(new NumValue(p, propnDecimals)).drawLeft(g, right, baseline + fitOffset);
//					g.setColor((groupColor == null) ? getForeground() : groupColor[i]);
				}
				right += columnWidth;
			}
			if (vertScale != PROPN_IN_Y) {
				Value value = (vertScale == COUNT) ? new NumValue(sampleSizes[i], 0)
																		: new NumValue(1.0, propnDecimals);
//				Value actualTotal = (xCounts[i] == 0) ? kBlankValue : value;
				value.drawLeft(g, totalColumnRight, baseline);
//				if (modelKey != null) {
//					g.setColor(kFitColor);
//					value.drawLeft(g, totalColumnRight, baseline + fitOffset);
//					g.setColor(getForeground());
//				}
			}
			
			baseline += rowHeight;
		}
		
		g.setColor(getForeground());
		baseline = tableTop + tableHeight + kTableOuterBorder + ascent;
		if (vertScale != PROPN_IN_X) {
			int right = startValRight;
			for (int j=0 ; j<noOfYCats ; j++) {
				Value value = (vertScale == COUNT) ? new NumValue(yCounts[j], 0)
																		: new NumValue(1.0, propnDecimals);
				value.drawLeft(g, right, baseline);
				right += columnWidth;
			}
			if (vertScale == COUNT) {
				Value value = new NumValue(totalCount, 0);
				value.drawLeft(g, totalColumnRight, baseline);
			}
		}
		g.translate(-leftBorder, -topBorder);		//		to reset origin
	}
	
	public void setDisplayType(int newMainGrouping, int newVertScale, boolean newStacked) {
		if (mainGrouping != newMainGrouping || vertScale != newVertScale) {
			mainGrouping = newMainGrouping;
			vertScale = newVertScale;		//		ignore newStacked
			
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariable x = (CatVariable)getVariable(xKey);
//		int noOfXCats = x.noOfCategories();
		CatVariable y = (CatVariable)getVariable(yKey);
//		int noOfYCats = y.noOfCategories();
		
		initialise(x, y, getGraphics());
		
		return new Dimension(tableLeft + tableRight + tableWidth,
									tableTop + tableBottom + tableHeight);
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (xKey.equals(key) || yKey.equals(key)) {
			CoreVariable y = getVariable(yKey);
			if (!(y instanceof CatSampleVariable)) 
				layoutInitialised = false;
			reinitialise();
			repaint();
		}
	}
}