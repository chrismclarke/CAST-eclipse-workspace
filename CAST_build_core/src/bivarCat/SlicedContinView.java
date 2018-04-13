package bivarCat;

import java.awt.*;

import dataView.*;


public class SlicedContinView extends DataView {
//	static final public String SLICEDCONTIN = "slicedContin";
	
	private String xKey, yKey, zKey;
	private int propnDecimals;
	
	private boolean initialised = false;
	private int [][][] jointCounts;
	private int [][] xzCounts;
	private int [][] xyCounts;
	private int [] xCounts;
	
	private boolean showAllValues = true;
	private int zCategory = 1;
	
	static final private int kRowTextGap = 5;			//	between rows of cat names
	
	static final private int kCatHorizGap = 12;	//	between y-cat names at top
	static final private int kYNameCatGap = 3;	//	between y-varName & cat names
	
	static final private int kTableOuterBorder = 3;
	static final private int kTableInnerBorder = 3;
	static final private int kTotalPropnGap = 20;
	
	static final private Color kPropnColor = new Color(0x0000BB);
	
	private LabelValue kTotalLabel;
	
	private int xCatWidth, xVarNameWidth, yVarNameWidth, biggestCountWidth,
																	biggestPropnWidth, totalWidth, propnWidth;
	private int yCatWidth[];
	private int boldAscent, boldDescent, ascent, descent;
	private int rowHeight, columnWidth;
	private int tableLeft, tableTop, tableRight, tableWidth, tableHeight;
	
	public SlicedContinView(DataSet theData, XApplet applet, String xKey, String yKey, String zKey,
									int propnDecimals) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.propnDecimals = propnDecimals;
		this.xKey = xKey;
		this.yKey = yKey;
		this.zKey = zKey;
		kTotalLabel = new LabelValue(applet.translate("Total"));
	}
	
	public void setSlicing(boolean doSlicing) {
		showAllValues = !doSlicing;
		repaint();
	}
	
	private boolean initialise(CatVariable x, CatVariable y, CatVariable z, Graphics g) {
		if (!initialised) {
			jointCounts = x.getCounts(y, z);
			xzCounts = x.getCounts(z);
			xyCounts = x.getCounts(y);
			xCounts = x.getCounts();
			
			int noOfXCats = x.noOfCategories();
			int noOfYCats = y.noOfCategories();
			
			int maxCount = 0;
			for (int i=0 ; i<noOfXCats ; i++)
				maxCount += Math.max(maxCount, xCounts[i]);
			
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
			FontMetrics fm = g.getFontMetrics();
			boldAscent = fm.getAscent();
			boldDescent = fm.getDescent();
			xVarNameWidth = fm.stringWidth(x.name);
			yVarNameWidth = fm.stringWidth(y.name);
			
			int totalHeadingWidth = kTotalLabel.stringWidth(g);
			int propnHeadingWidth = fm.stringWidth("P()" + y.getLabel(0).toString());
			
			g.setFont(oldFont);
			fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			xCatWidth = 0;
			for (int i=0 ; i<noOfXCats ; i++) {
				int nameWidth = x.getLabel(i).stringWidth(g);
				if (nameWidth > xCatWidth)
					xCatWidth = nameWidth;
			}
			
			rowHeight = ascent + descent + kRowTextGap;
			tableLeft = Math.max(xCatWidth, xVarNameWidth) + kTableOuterBorder;
			tableHeight = noOfXCats * rowHeight  + 2 * kTableInnerBorder;
			
			yCatWidth = new int[noOfYCats];
			columnWidth = 0;
			int previousSpace = 100;
			for (int i=0 ; i<noOfYCats ; i++) {
				yCatWidth[i] = y.getLabel(i).stringWidth(g);
				int leftGap = (columnWidth - yCatWidth[i]) / 2;
				columnWidth += Math.max(Math.max(0, -leftGap),
																		kCatHorizGap - previousSpace - leftGap);
				previousSpace = (columnWidth - yCatWidth[i]) / 2;
			}
			biggestCountWidth = fm.stringWidth(String.valueOf(maxCount));
			biggestPropnWidth = (new NumValue(1.0, propnDecimals)).stringWidth(g);
			columnWidth = Math.max(columnWidth, biggestCountWidth + kCatHorizGap);
			columnWidth = Math.max(columnWidth, (yVarNameWidth + noOfYCats - 1) / noOfYCats);
			tableWidth = noOfYCats * columnWidth  + 2 * kTableInnerBorder;
			
			tableTop = boldAscent + boldDescent + kYNameCatGap + ascent + descent + kTableOuterBorder;
			
			totalWidth = Math.max(biggestCountWidth, totalHeadingWidth);
			propnWidth = Math.max(biggestPropnWidth, propnHeadingWidth);
			tableRight = kTableOuterBorder + totalWidth + kTotalPropnGap + propnWidth;
			
			initialised = false;
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
		CatVariable z = (CatVariable)getVariable(zKey);
//		int noOfZCats = z.noOfCategories();
		
		initialise(x, y, z, getGraphics());
		
		g.drawRect(tableLeft, tableTop, tableWidth - 1, tableHeight - 1);
		g.setColor(Color.white);
		g.fillRect(tableLeft + 1, tableTop + 1, tableWidth - 2, tableHeight - 2);
		g.setColor(getForeground());
		
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
		g.drawString(y.name, tableLeft + (tableWidth - yVarNameWidth) / 2, boldAscent);
		g.drawString(x.name, 0, tableTop - descent - kTableOuterBorder);
		
		int baseline = boldAscent + boldDescent + kYNameCatGap + ascent;
		int totalColumnRight = tableLeft + tableWidth + kTableOuterBorder + totalWidth;
		int propnColumnLeft = totalColumnRight + kTotalPropnGap;
		
		kTotalLabel.drawLeft(g, totalColumnRight, baseline);
		
		g.setColor(kPropnColor);
		g.drawString("P(" + y.getLabel(0).toString() + ")", propnColumnLeft, baseline);
		g.setColor(getForeground());
		
		g.setFont(oldFont);
		int horizCenter = tableLeft + kTableInnerBorder + columnWidth / 2;
		for (int i=0 ; i<noOfYCats ; i++) {
			y.getLabel(i).drawCentred(g, horizCenter, baseline);
			horizCenter += columnWidth;
		}
		
		baseline = tableTop + kTableInnerBorder + (rowHeight + ascent - descent) / 2;
		int startValRight = tableLeft + kTableInnerBorder + (columnWidth + biggestCountWidth) / 2;
		
		for (int i=0 ; i<noOfXCats ; i++) {
			x.getLabel(i).drawRight(g, tableLeft - kTableOuterBorder - xCatWidth, baseline);
			
			int right = startValRight;
			for (int j=0 ; j<noOfYCats ; j++) {
				int count = showAllValues ? xyCounts[i][j] : jointCounts[i][j][zCategory];
				Value value = new NumValue(count, 0);
				value.drawLeft(g, right, baseline);
				right += columnWidth;
			}
			
			int rowTotal = showAllValues ? xCounts[i] : xzCounts[i][zCategory];
			new NumValue(rowTotal, 0).drawLeft(g, totalColumnRight, baseline);
			int count1 = showAllValues ? xyCounts[i][0] : jointCounts[i][0][zCategory];
			
			g.setColor(kPropnColor);
			new NumValue(count1 / (double)rowTotal, propnDecimals).drawRight(g, propnColumnLeft, baseline);
			g.setColor(getForeground());
			
			baseline += rowHeight;
		}
	}
	
	public void setSlice(int zCategory) {
		if (this.zCategory != zCategory) {
			this.zCategory = zCategory;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariable x = (CatVariable)getVariable(xKey);
//		int noOfXCats = x.noOfCategories();
		CatVariable y = (CatVariable)getVariable(yKey);
//		int noOfYCats = y.noOfCategories();
		CatVariable z = (CatVariable)getVariable(zKey);
//		int noOfZCats = z.noOfCategories();
		
		initialise(x, y, z, getGraphics());
		
		return new Dimension(tableLeft + tableRight + tableWidth, tableTop + tableHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}