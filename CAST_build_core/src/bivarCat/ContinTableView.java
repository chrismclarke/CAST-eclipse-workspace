package bivarCat;

import java.awt.*;

import dataView.*;


public class ContinTableView extends TwoWayView {
//	static final public String CONTINTABLE = "continTable";
	
	static final public int OUTER = 0;
	static final public int INNER = 1;
	
	static final private int kRowTextGap = 5;			//	between rows of cat names
	static final private int kRowKeyGap = 4;			//	between rows of keys
	static final private int kKeyCatHorizGap = 5;	//	between key and x-cat name
	static final private int kKeyCatVertGap = 4;		//	between key and y-cat name
	
	static final private int kCatHorizGap = 12;	//	between y-cat names at top
	static final private int kYNameCatGap = 3;	//	between y-varName & cat names
	
	static final private int kTableOuterBorder = 3;
	static final private int kTableInnerBorder = 3;
	
//	static final private int kVarNameKeyGap = 5;
//	static final private int kBetweenKeyGap = 3;
//	static final private int kKeyCatNameGap = 5;
	
	static final private int kKeyBoxSize = 20;
	static final private int kKeyBoxBorder = 4;
	
	static final private Color kBorderBlue = new Color(0x0000FF);
	static final private Color kBorderRed = new Color(0xFF0000);
	static final private Color kBorderGreen = new Color(0x006600);
	
	static final private Color kFillYellow = new Color(0xFFFF00);
	static final private Color kFillPaleGreen = new Color(0x33CC66);
	static final private Color kFillPaleBlue = new Color(0x0099FF);
	static final private Color kFillOrange = new Color(0xFF9900);
	
	static final private Color kMarginHilite = new Color(0xFFFF33);
	
	static final public int NO_HILITE = 0;
	static final public int X_HILITE = 1;			//	right
	static final public int Y_HILITE = 2;			//	bottom
	
	static public Color[] getColors(int borderType, int noOfCats) {
		Color result[] = new Color[noOfCats];
		if (borderType == OUTER) {
			switch (noOfCats) {
				case 2:
					result[0] = Color.black;
					result[1] = kBorderRed;
					break;
				case 3:
					result[0] = Color.black;
					result[1] = kBorderRed;
					result[2] = kBorderBlue;
					break;
				case 4:
					result[0] = Color.black;
					result[1] = kBorderRed;
					result[2] = kBorderBlue;
					result[3] = kBorderGreen;
					break;
				default:
					for (int i=0 ; i<noOfCats ; i++)
						result[i] = new Color((int)(((long)kBorderRed.getRGB() * i + (long)kBorderBlue.getRGB() * (noOfCats - i - 1))) / (noOfCats - 1));
			}
		}
		else {
			switch (noOfCats) {
				case 2:
					result[0] = kFillYellow;
					result[1] = kFillPaleBlue;
					break;
				case 3:
					result[0] = kFillYellow;
					result[1] = kFillPaleBlue;
					result[2] = kFillPaleGreen;
					break;
				case 4:
					result[0] = kFillYellow;
					result[1] = kFillPaleBlue;
					result[2] = kFillPaleGreen;
					result[3] = kFillOrange;
					break;
				default:
					for (int i=0 ; i<noOfCats ; i++)
						result[i] = new Color((int)(((long)kFillPaleBlue.getRGB() * i + (long)kFillOrange.getRGB() * (noOfCats - i - 1))) / (noOfCats - 1));
			}
		}
		return result;
	}
	
	private int propnDecimals;
	private boolean showKeys, showYMargin;
	
	private int xCatWidth, xVarNameWidth, yVarNameWidth, biggestCountWidth,
																					biggestPropnWidth, totalWidth;
	private int yCatWidth[];
	private int boldAscent, boldDescent, ascent, descent;
	private int rowHeight, columnWidth;
	private int tableLeft, tableTop, tableRight, tableBottom, tableWidth, tableHeight;
	
	private Color[] xColors, yColors;
	private int marginHIlite;
	
	private int totalCount;
	
	public ContinTableView(DataSet theData, XApplet applet, String xKey, String yKey, int propnDecimals,
									boolean showKeys, boolean showYMargin) {
		super(theData, applet, xKey, yKey);
		this.propnDecimals = propnDecimals;
		this.showKeys = showKeys;
		this.showYMargin = showYMargin;
	}
	
	public void setMarginHilite(int marginHIlite) {
		this.marginHIlite = marginHIlite;
		repaint();
	}
	
	protected boolean initialise(CatVariable xVariable, Variable yVariable, Graphics g) {
		if (super.initialise(xVariable, yVariable)) {
			int noOfXCats = xVariable.noOfCategories();
			int noOfYCats = ((CatVariable)yVariable).noOfCategories();
			
			int maxYCount = 0;
			totalCount = 0;
			for (int i=0 ; i<noOfYCats ; i++) {
				maxYCount = Math.max(maxYCount, yCounts[i]);
				totalCount += yCounts[i];
			}
			
			xColors = getColors(OUTER, noOfXCats);
			yColors = getColors(INNER, noOfYCats);
			
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
			
			rowHeight = ascent + descent + kRowTextGap;
			if (showKeys) {
				rowHeight = Math.max(rowHeight, kKeyBoxSize + kRowKeyGap);
				tableLeft = Math.max(xCatWidth + kKeyBoxSize + kKeyCatHorizGap, xVarNameWidth)
																								+ kTableOuterBorder;
			}
			else
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
			biggestPropnWidth = (new NumValue(1.0, propnDecimals)).stringWidth(g);
			columnWidth = Math.max(columnWidth, Math.max(biggestCountWidth, biggestPropnWidth)
																										+ kCatHorizGap);
			columnWidth = Math.max(columnWidth, (yVarNameWidth + noOfYCats - 1) / noOfYCats);
			tableWidth = noOfYCats * columnWidth  + 2 * kTableInnerBorder;
			
			tableTop = boldAscent + boldDescent + kYNameCatGap + ascent + descent + kTableOuterBorder;
			if (showKeys)
				tableTop += kKeyBoxSize + kKeyCatVertGap;
			
			totalWidth = fm.stringWidth(String.valueOf(totalCount));
			tableRight = kTableOuterBorder + Math.max(totalWidth, biggestPropnWidth);
			
			tableBottom = 0;
			if (showYMargin)
				tableBottom += ascent + descent + kTableOuterBorder;
			
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
		
		initialise(x, y, getGraphics());
		
		int leftBorder = (getSize().width - tableLeft - tableRight - tableWidth) / 2;
		int topBorder = (getSize().height - tableTop - tableBottom - tableHeight) / 2;
		g.translate(leftBorder, topBorder);		//		MUST be reset later
		
		g.drawRect(tableLeft, tableTop, tableWidth - 1, tableHeight - 1);
		g.setColor(Color.white);
		g.fillRect(tableLeft + 1, tableTop + 1, tableWidth - 2, tableHeight - 2);
		
		if (marginHIlite == X_HILITE) {
			g.setColor(kMarginHilite);
			g.fillRect(tableLeft + tableWidth, tableTop, tableRight, tableHeight - 1);
		}
		
		if (marginHIlite == Y_HILITE) {
			g.setColor(kMarginHilite);
			g.fillRect(tableLeft, tableTop + tableHeight, tableWidth - 1, tableBottom);
		}
		
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
		int valueWidth = (vertScale == COUNT) ? biggestCountWidth : biggestPropnWidth;
		int startValRight = tableLeft + kTableInnerBorder + (columnWidth + valueWidth) / 2;
		int totalColumnRight = tableLeft + tableWidth + kTableOuterBorder;
		if (vertScale == COUNT)
			totalColumnRight += totalWidth;
		else if (vertScale == PROPN_IN_X || vertScale == PERCENT_IN_X)
			totalColumnRight +=  biggestPropnWidth;
		
		for (int i=0 ; i<noOfXCats ; i++) {
			x.getLabel(i).drawRight(g, tableLeft - kTableOuterBorder - xCatWidth, baseline);
			
			int right = startValRight;
			for (int j=0 ; j<noOfYCats ; j++) {
				Value value = (vertScale == COUNT) ? new NumValue(jointCounts[i][j], 0)
									: (vertScale == PROPN_IN_X) ? new NumValue(((double)jointCounts[i][j]) / xCounts[i], propnDecimals)
									: (vertScale == PERCENT_IN_X) ? new NumValue(((double)jointCounts[i][j]) / xCounts[i] * 100, propnDecimals - 2)
									: (vertScale == PROPN_IN_Y) ? new NumValue(((double)jointCounts[i][j]) / yCounts[j], propnDecimals)
									: new NumValue(((double)jointCounts[i][j]) / yCounts[j] * 100, propnDecimals - 2);
				value.drawLeft(g, right, baseline);
				right += columnWidth;
			}
			if (vertScale == COUNT)
				new NumValue(xCounts[i], 0).drawLeft(g, totalColumnRight, baseline);
			else if (vertScale == PROPN_IN_X)
				new NumValue(1.0, propnDecimals).drawLeft(g, totalColumnRight, baseline);
			else if (vertScale == PERCENT_IN_X)
				new NumValue(100.0, propnDecimals - 2).drawLeft(g, totalColumnRight, baseline);
			
			baseline += rowHeight;
		}
		
		baseline = tableTop + tableHeight + kTableOuterBorder + ascent;
		if (showYMargin && vertScale != PROPN_IN_X && vertScale != PERCENT_IN_X) {
			int right = startValRight;
			for (int j=0 ; j<noOfYCats ; j++) {
				if (vertScale == COUNT)
					new NumValue(yCounts[j], 0).drawLeft(g, right, baseline);
				else if (vertScale == PROPN_IN_Y)
					new NumValue(1.0, propnDecimals).drawLeft(g, right, baseline);
				else
					new NumValue(100, propnDecimals - 2).drawLeft(g, right, baseline);
				
				right += columnWidth;
			}
			if (vertScale == COUNT) {
				Value value = new NumValue(totalCount, 0);
				value.drawLeft(g, totalColumnRight, baseline);
			}
		}
		
		if (showKeys) {
			int keyTop = tableTop + kTableInnerBorder + (rowHeight - kKeyBoxSize) / 2;
			for (int i=0 ; i<noOfXCats ; i++) {
				g.setColor(Color.darkGray);
				g.drawRect(0, keyTop -1, kKeyBoxSize + 1, kKeyBoxSize + 1);
				g.setColor(xColors[i]);
				g.fillRect(1, keyTop, kKeyBoxSize, kKeyBoxSize);
				g.setColor(Color.lightGray);
				g.fillRect(1 + kKeyBoxBorder, keyTop + kKeyBoxBorder,
										kKeyBoxSize - 2 * kKeyBoxBorder, kKeyBoxSize - 2 * kKeyBoxBorder);
				keyTop += rowHeight;
			}
			
			keyTop = tableTop - kTableOuterBorder - kKeyBoxSize - 2;
			int keyLeft = tableLeft + kTableInnerBorder + (columnWidth - kKeyBoxSize) / 2;
			for (int i=0 ; i<noOfYCats ; i++) {
				g.setColor(Color.darkGray);
				g.drawRect(keyLeft - 1, keyTop - 1, kKeyBoxSize + 1, kKeyBoxSize + 1);
				g.setColor(Color.lightGray);
				g.fillRect(keyLeft, keyTop, kKeyBoxSize, kKeyBoxSize);
				g.setColor(yColors[i]);
				g.fillRect(keyLeft + kKeyBoxBorder, keyTop + kKeyBoxBorder,
										kKeyBoxSize - 2 * kKeyBoxBorder, kKeyBoxSize - 2 * kKeyBoxBorder);
				keyLeft += columnWidth;
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
}