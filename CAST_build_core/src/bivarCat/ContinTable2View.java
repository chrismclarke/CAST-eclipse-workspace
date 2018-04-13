package bivarCat;

import java.awt.*;

import dataView.*;

import contin.ContinCatInfo;


public class ContinTable2View extends TwoWayView {
//	static final public String CONTINTABLE2 = "continTable2";
	
	static final private int kRowTextGap = 6;			//	between rows of cat names
	static final private int kCatHorizGap = 12;		//	between y-cat names at top
	static final private int kYNameCatGap = 5;		//	between y-varName & cat names
	
	static final private int kFractionGap = 2;		//	above and below horiz line of fraction
	
	static final private int kTableOuterHorizBorder = 10;
	static final private int kTableOuterVertBorder = 3;
	static final private int kTableInnerBorder = 3;
	
	static final private Color kXColor = new Color(0x660000);		//	dark red
	static final private Color kYColor = new Color(0x000066);		//	dark blue
	static final private Color kHiliteColor = Color.yellow;
	static final private Color kHiliteBackgroundColor = new Color(0xFFE64C);		//	darker yellow
	
	private int propnDecimals;
	private boolean showYMargin;		//	bottom margin
	private int selectedRow = -1;
	private int selectedCol = -1;
	
	private Font boldFont, smallFont;
	private int boldAscent, boldDescent, ascent, descent, smallAscent;
	
	private int xCatWidth, xVarNameWidth, yVarNameWidth;
	private int maxFractionWidth, maxCountWidth,
																					maxPropnWidth, totalWidth;
//	private int yCatWidth[];
	private int rowHeight, columnWidth;
	private int tableLeft, tableTop, tableRight, tableBottom, tableWidth, tableHeight;
	
	private int totalCount;
	
	public ContinTable2View(DataSet theData, XApplet applet, String xKey, String yKey,
																									int propnDecimals, boolean showYMargin) {
		super(theData, applet, xKey, yKey);
		this.propnDecimals = propnDecimals;
		this.showYMargin = showYMargin;
		
		boldFont = applet.getStandardBoldFont();
		smallFont = applet.getSmallFont();
	}
	
	private void findItemSizes(CatVariable xVariable, CatVariable yVariable, Graphics g) {
		int noOfXCats = xCounts.length;
		int noOfYCats = yCounts.length;
		
		int maxYCount = 0;
		totalCount = 0;
		for (int i=0 ; i<noOfYCats ; i++) {
			maxYCount = Math.max(maxYCount, yCounts[i]);
			totalCount += yCounts[i];
		}
		int maxXCount = 0;
		for (int i=0 ; i<noOfXCats ; i++)
			maxXCount = Math.max(maxXCount, xCounts[i]);
		
		Font oldFont = g.getFont();
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		boldDescent = fm.getDescent();
		xVarNameWidth = fm.stringWidth(xVariable.name);
		yVarNameWidth = fm.stringWidth(yVariable.name);
		
		g.setFont(smallFont);
		fm = g.getFontMetrics();
		smallAscent = fm.getAscent();
		maxFractionWidth = new NumValue(Math.max(maxXCount, maxYCount), 0).stringWidth(g);
		
		g.setFont(oldFont);
		fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		xCatWidth = 0;
		for (int i=0 ; i<noOfXCats ; i++)
			xCatWidth = Math.max(xCatWidth, xVariable.getLabel(i).stringWidth(g));
		
		NumValue maxCountVal = new NumValue(maxYCount, 0);
		maxCountWidth = maxCountVal.stringWidthWithCommas(g);
		maxPropnWidth = (new NumValue(1.0, propnDecimals)).stringWidth(g);
		
		rowHeight = Math.max(ascent + descent, 2 * (smallAscent + kFractionGap));
	}
	
	private void findColumnWidths(CatVariable xVariable, CatVariable yVariable, Graphics g) {
//		int noOfXCats = xCounts.length;
		int noOfYCats = yCounts.length;
		
		int previousWidth;
		int thisWidth = ((CatVariable)yVariable).getLabel(0).stringWidth(g);
		
		int maxHeadingWidth = 0;
		for (int i=0 ; i<noOfYCats ; i++) {
			previousWidth = thisWidth;
			if (i > 0)
				thisWidth = ((CatVariable)yVariable).getLabel(i).stringWidth(g);
			maxHeadingWidth = Math.max(maxHeadingWidth, (thisWidth + previousWidth) / 2);
		}
		
		int maxValueWidth = Math.max(maxCountWidth, Math.max(maxPropnWidth,
																				maxFractionWidth));
		
		columnWidth = Math.max(Math.max(maxHeadingWidth, maxValueWidth) + kCatHorizGap,
																		(yVarNameWidth + noOfYCats - 1) / noOfYCats);
		
		NumValue totalVal = new NumValue(totalCount, 0);
		totalWidth = totalVal.stringWidthWithCommas(g);
	}
	
	protected boolean initialise(CatVariable xVariable, Variable yVariable, Graphics g) {
		if (!super.initialise(xVariable, yVariable))
			return false;
		
		findItemSizes(xVariable, (CatVariable)yVariable, g);
		findColumnWidths(xVariable, (CatVariable)yVariable, g);
		
		int noOfXCats = xCounts.length;
		int noOfYCats = yCounts.length;
		
		tableLeft = Math.max(xCatWidth, xVarNameWidth) + kTableOuterHorizBorder;
		tableHeight = noOfXCats * (rowHeight + kRowTextGap)  + 2 * kTableInnerBorder;
		
		tableWidth = noOfYCats * columnWidth  + 2 * kTableInnerBorder;
		
		tableTop = boldAscent + boldDescent + kYNameCatGap + ascent + descent + kTableOuterVertBorder;
		
		tableRight = kTableOuterHorizBorder + Math.max(totalWidth, maxPropnWidth);
		
		tableBottom = 0;
		if (showYMargin)
			tableBottom += rowHeight + kTableOuterVertBorder;
		
		return true;
	}
	
	private void clearContents(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(tableLeft + 1, tableTop + 1, tableWidth - 2, tableHeight - 2);
		
		if (selectedRow >= 0) {
			int top = tableTop + selectedRow * (rowHeight + kRowTextGap)
																							+ kTableInnerBorder - kRowTextGap / 2;
			
			g.setColor(kHiliteBackgroundColor);
			g.fillRect(0, top, getSize().width, rowHeight + kRowTextGap);
			g.setColor(kHiliteColor);
			g.fillRect(tableLeft, top, tableWidth, rowHeight + kRowTextGap);
		}
		else if (selectedCol >= 0) {
			int left = tableLeft + kTableInnerBorder + selectedCol * columnWidth;
			int top = boldAscent + boldDescent + kYNameCatGap / 2;
			
			g.setColor(kHiliteBackgroundColor);
			g.fillRect(left, top, columnWidth, getSize().height - top);
			g.setColor(kHiliteColor);
			g.fillRect(left, tableTop, columnWidth, tableHeight);
		}
		
		g.setColor(getForeground());
		g.drawRect(tableLeft, tableTop, tableWidth - 1, tableHeight - 1);
	}
	
	private void drawHeading(Graphics g, CatVariable x, CatVariable y) {
		int noOfYCats = y.noOfCategories();
		Font oldFont = g.getFont();
		g.setFont(boldFont);
		g.setColor(kXColor);
		g.drawString(x.name, 0, tableTop - descent - kTableOuterVertBorder);

		g.setColor(kYColor);
		g.drawString(y.name, tableLeft + (tableWidth - yVarNameWidth) / 2, boldAscent);
		g.setFont(oldFont);
		
		int horizCenter = tableLeft + kTableInnerBorder + columnWidth / 2;
		for (int i=0 ; i<noOfYCats ; i++) {
			y.getLabel(i).drawCentred(g, horizCenter,
																			boldAscent + boldDescent + kYNameCatGap + ascent);
			horizCenter += columnWidth;
		}
	}
	
	private void drawFraction(Graphics g, int numer, int denom, int mainBaseline,
																												int valueCenter, NumValue countVal) {
		Font standardFont = g.getFont();
		int numerBaseline = mainBaseline - ascent / 2 - kFractionGap;
		int denomBaseline = numerBaseline + 2 * kFractionGap + smallAscent;
		g.setFont(smallFont);
		g.setColor(Color.red);
		countVal.setValue(numer);
		countVal.drawCentred(g, valueCenter, numerBaseline);
		countVal.setValue(denom);
		countVal.drawCentred(g, valueCenter, denomBaseline);
		int width = countVal.stringWidth(g);
		g.drawLine(valueCenter - width / 2 - 1, mainBaseline - ascent / 2,
																			valueCenter + width / 2 + 1, mainBaseline - ascent / 2);
		g.setColor(getForeground());
		g.setFont(standardFont);
	}
	
	private void drawRow(Graphics g, CatVariable x, int i, int noOfYCats, int baseline,
									int labelLeft, int valueRight, int valueCenter, int totalRight) {
		NumValue countVal = new NumValue(0, 0);
		NumValue propnVal = new NumValue(0, propnDecimals);
		NumValue percentVal = new NumValue(0, propnDecimals - 2);
		g.setColor(getForeground());
		
		for (int j=0 ; j<noOfYCats ; j++) {
			if (i == selectedRow || j == selectedCol) {
				int numer = jointCounts[i][j];
				int denom = (i == selectedRow) ?  xCounts[i] : yCounts[j];
				drawFraction(g, numer, denom, baseline, valueCenter, countVal);
			}
			else if (vertScale == COUNT) {
				countVal.setValue(jointCounts[i][j]);
				countVal.drawWithCommas(g, valueRight, baseline);
			}
			else if (vertScale == PROPN_IN_X) {
				propnVal.setValue(((double)jointCounts[i][j]) / xCounts[i]);
				propnVal.drawLeft(g, valueRight, baseline);
			}
			else if (vertScale == PROPN_IN_Y) {
				propnVal.setValue(((double)jointCounts[i][j]) / yCounts[j]);
				propnVal.drawLeft(g, valueRight, baseline);
			}
			else if (vertScale == PERCENT_IN_X) {
				percentVal.setValue(((double)jointCounts[i][j]) / xCounts[i] * 100);
				percentVal.drawLeft(g, valueRight, baseline);
			}
			else if (vertScale == PERCENT_IN_Y) {
				percentVal.setValue(((double)jointCounts[i][j]) / yCounts[j] * 100);
				percentVal.drawLeft(g, valueRight, baseline);
			}
			valueRight += columnWidth;
			valueCenter += columnWidth;
		}
		
		g.setColor(kXColor);
		x.getLabel(i).drawRight(g, labelLeft, baseline);
		
		if (i == selectedRow)
			drawFraction(g, xCounts[i], xCounts[i], baseline, totalRight - maxFractionWidth / 2 - 1, countVal);
		else if (vertScale == COUNT) {
			countVal.setValue(xCounts[i]);
			countVal.drawWithCommas(g, totalRight, baseline);
		}
		else if (vertScale == PROPN_IN_X) {
			propnVal.setValue(1.0);
			propnVal.drawLeft(g, totalRight, baseline);
		}
		else if (vertScale == PERCENT_IN_X) {
			percentVal.setValue(100);
			percentVal.drawLeft(g, totalRight, baseline);
		}
	}
	
	private void drawTotalRow(Graphics g, int noOfYCats, int baseline,
														int valueRight, int valueCenter, int totalRight) {
		g.setColor(kYColor);
		NumValue countVal = new NumValue(0, 0);
		for (int j=0 ; j<noOfYCats ; j++) {
			if (j == selectedCol)
				drawFraction(g, yCounts[j], yCounts[j], baseline, valueCenter, countVal);
			else if (vertScale == COUNT) {
				countVal.setValue(yCounts[j]);
				countVal.drawWithCommas(g, valueRight, baseline);
			}
			else if (vertScale == PROPN_IN_Y)
				new NumValue(1.0, propnDecimals).drawLeft(g, valueRight, baseline);
			else
				new NumValue(100, propnDecimals - 2).drawLeft(g, valueRight, baseline);
			valueRight += columnWidth;
			valueCenter += columnWidth;
		}
		if (vertScale == COUNT) {
			g.setColor(getForeground());
			countVal.setValue(totalCount);
			countVal.drawWithCommas(g, totalRight, baseline);
		}
	}
	
	public void paintView(Graphics g) {
		CatVariable x = (CatVariable)getVariable(xKey);
		CatVariable y = (CatVariable)getVariable(yKey);
		
		initialise(x, y, g);
		
		int noOfXCats = x.noOfCategories();
		int noOfYCats = y.noOfCategories();
		
		clearContents(g);
		
		drawHeading(g, x, y);
		
		int baseline = tableTop + kTableInnerBorder
																		+ (rowHeight + kRowTextGap + ascent - descent) / 2;
		int valueWidth = (vertScale == COUNT) ? maxCountWidth : maxPropnWidth;
		int startValRight = tableLeft + kTableInnerBorder + (columnWidth + valueWidth) / 2;
		int startValCenter = startValRight - valueWidth / 2;
		int totalColumnRight = tableLeft + tableWidth + kTableOuterHorizBorder;
		if (vertScale == COUNT)
			totalColumnRight += totalWidth;
		else if (vertScale == PROPN_IN_X || vertScale == PERCENT_IN_X)
			totalColumnRight +=  maxPropnWidth;
		int labelLeft = tableLeft - kTableOuterHorizBorder - xCatWidth;
		
		for (int i=0 ; i<noOfXCats ; i++) {
			drawRow(g, x, i, noOfYCats, baseline, labelLeft, startValRight,
																					startValCenter, totalColumnRight);
			baseline += (rowHeight + kRowTextGap);
		}
		
		if (showYMargin && vertScale != PROPN_IN_X && vertScale != PERCENT_IN_X) {
			baseline = tableTop + tableHeight + kTableOuterVertBorder
																									+ (rowHeight + ascent - descent) / 2;
			drawTotalRow(g, noOfYCats, baseline, startValRight, startValCenter, totalColumnRight);
		}
	}
	
	public void setDisplayType(int newMainGrouping, int newVertScale, boolean newStacked) {
		if (newMainGrouping != mainGrouping || vertScale != newVertScale) {
			vertScale = newVertScale;		//		ignore newStacked
			mainGrouping = newMainGrouping;
			selectedRow = selectedCol = -1;
			
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariable x = (CatVariable)getVariable(xKey);
		CatVariable y = (CatVariable)getVariable(yKey);
		
		initialise(x, y, getGraphics());
		
		return new Dimension(tableLeft + tableRight + tableWidth,
									tableTop + tableBottom + tableHeight);
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return vertScale != COUNT;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int row = -1;
		if (y >= tableTop && y < tableTop + tableHeight)
			row = (y - tableTop - kTableInnerBorder + kRowTextGap / 2) / (rowHeight + kRowTextGap);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		if (row >= xVar.noOfCategories())
			row = xVar.noOfCategories() - 1;
		
		int col = -1;
		if (x >= tableLeft && x < tableLeft + tableWidth)
			col = (x - tableLeft - kTableInnerBorder) / columnWidth;
		CatVariable yVar = (CatVariable)getVariable(yKey);
		if (col >= yVar.noOfCategories())
			col = yVar.noOfCategories() - 1;
		
		return new ContinCatInfo(row, col);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ContinCatInfo catInfo = (ContinCatInfo)startInfo;
		
		if (mainGrouping == YMAIN) {
			if (catInfo.yIndex != selectedCol) {
				selectedCol = catInfo.yIndex;
				repaint();
			}
		}
		else {
			if (catInfo.xIndex != selectedRow) {
				selectedRow = catInfo.xIndex;
				repaint();
			}
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedRow = selectedCol = -1;
		repaint();
	}
}