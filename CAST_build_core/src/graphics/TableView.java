package graphics;

import java.awt.*;

import dataView.*;


public class TableView extends DataView {
//	static final public String TABLE_VIEW = "tableView";
	
	static final protected int kNameTableGap = 8;
	static final protected int kHeadingTableGap = 5;
	static final protected int kTableVertBorder = 4;
	static final protected int kTableHorizBorder = 10;
	static final protected int kLineSpacing = 4;
	
//	static final private int kTotalTableGap = 5;
	static final private int kColumnGap = 20;
	static final private LabelValue kTotalLabel = new LabelValue("Total");
	
	private LabelValue kPercentLabel;
	
	private String yKey, labelKey;
	private int percentDecimals;
	private boolean hasPercentTotal;
	
	private Color valueColor = null;
	
	private Font boldFont;
	private int boldAscent, boldDescent, ascent, descent;
	
	private int labelTitleWidth, totalLabelWidth, valTitleWidth, percentTitleWidth;
	private int maxLabelWidth, maxValWidth, maxPercentWidth;
	private int tableTopBorder, tableBottomBorder, tableLeftBorder, tableHeight, tableWidth;
	
	private boolean initialised = false;
	
	public TableView(DataSet theData, XApplet applet, String yKey, String labelKey,
																				boolean hasPercentTotal, int percentDecimals) {
		super(theData, applet, null);
		kPercentLabel = new LabelValue(applet.translate("Percentage"));
		this.yKey = yKey;
		this.labelKey = labelKey;
		this.hasPercentTotal = hasPercentTotal;
		this.percentDecimals = percentDecimals;
	}
	
	public void setValueColor(Color valueColor) {
		this.valueColor = valueColor;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	private NumValue getTotal(NumVariable yVar) {
		ValueEnumeration ye = yVar.values();
		double sumY = 0.0;
		while (ye.hasMoreValues())
			sumY += ye.nextDouble();
		return new NumValue(sumY, yVar.getMaxDecimals());
	}
	
	protected void doInitialisation(Graphics g) {
		Font stdFont = g.getFont();
		boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		boldDescent = fm.getDescent();
		
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		labelTitleWidth = fm.stringWidth(labelVar.name);
		totalLabelWidth = kTotalLabel.stringWidth(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		valTitleWidth = fm.stringWidth(yVar.name);
		int totalValWidth = hasPercentTotal ? getTotal(yVar).stringWidth(g) : 0;
			
		percentTitleWidth = kPercentLabel.stringWidth(g);
		maxPercentWidth = new NumValue(100.0, percentDecimals).stringWidth(g);
				
		g.setFont(stdFont);
		fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		maxLabelWidth = labelVar.getMaxWidth(g);
		
		maxValWidth = 0;
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues())
			maxValWidth = Math.max(maxValWidth, ((NumValue)ye.nextValue()).stringWidthWithCommas(g));
		
		percentTitleWidth = kPercentLabel.stringWidth(g);
		
		tableTopBorder = boldAscent + boldDescent + kHeadingTableGap;
		tableHeight = nVals * (ascent + descent) + (nVals - 1) * kLineSpacing + 2 * kTableVertBorder;
		tableBottomBorder = hasPercentTotal ? tableTopBorder : 0;
		
		tableLeftBorder = Math.max(Math.max(labelTitleWidth, maxLabelWidth), totalLabelWidth) + kNameTableGap;
		tableWidth = 2 * kTableHorizBorder + Math.max(valTitleWidth, Math.max(maxValWidth, totalValWidth));
		if (hasPercentTotal)
			tableWidth += Math.max(percentTitleWidth, maxPercentWidth) + kColumnGap;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		
		g.setColor(Color.white);
		g.fillRect(tableLeftBorder, tableTopBorder, tableWidth, tableHeight);
		
		int valRight = tableLeftBorder + kTableHorizBorder + ((valTitleWidth > maxValWidth)
																							? (valTitleWidth + maxValWidth) / 2 : maxValWidth);
		int valTitleLeft = tableLeftBorder + kTableHorizBorder + ((valTitleWidth > maxValWidth)
																							? 0 : (maxValWidth - valTitleWidth) / 2);
		int percentRight = hasPercentTotal ? tableLeftBorder + kTableHorizBorder
																						+ Math.max(valTitleWidth, maxValWidth) + kColumnGap
																						+ Math.max(percentTitleWidth, maxPercentWidth) : 0;
		
		Font stdFont = g.getFont();
		g.setFont(boldFont);
		g.setColor(Color.black);
		int baseline = boldAscent;
		g.drawString(labelVar.name, 0, ascent);
		g.drawString(yVar.name, valTitleLeft, baseline);
		if (hasPercentTotal)
			kPercentLabel.drawLeft(g, percentRight, baseline);
		
		NumValue totalVal = hasPercentTotal ? getTotal(yVar) : null;
		NumValue tempPercent = hasPercentTotal ? new NumValue(0.0, percentDecimals) : null;
		Flags selection = getSelection();
		
		g.setFont(stdFont);
		if (valueColor != null)
			g.setColor(valueColor);
		baseline = tableTopBorder + kTableVertBorder + ascent;
		for (int i=0 ; i<nVals ; i++) {
			if (selection.valueAt(i)) {
				g.setColor(Color.yellow);
				g.fillRect(tableLeftBorder, baseline - ascent - kLineSpacing, tableWidth,
																															ascent + descent + 2 * kLineSpacing);
				g.setColor(getForeground());
			}
			labelVar.valueAt(i).drawRight(g, 0, baseline);
			NumValue y = (NumValue)yVar.valueAt(i);
			y.drawWithCommas(g, valRight, baseline);
			if (hasPercentTotal) {
				tempPercent.setValue(y.toDouble() / totalVal.toDouble() * 100.0);
				tempPercent.drawLeft(g, percentRight, baseline);
			}
			
			baseline += (ascent + descent + kLineSpacing);
		}
		
		g.setColor(Color.black);
		g.drawLine(tableLeftBorder, tableTopBorder - 1, tableLeftBorder + tableWidth - 1,
																								tableTopBorder - 1);
		g.drawLine(tableLeftBorder, tableTopBorder + tableHeight - 1, tableLeftBorder + tableWidth - 1,
																						tableTopBorder + tableHeight - 1);
		
		g.setFont(boldFont);
		g.setColor(getForeground());
		if (hasPercentTotal) {
			baseline = tableTopBorder + tableHeight + kHeadingTableGap + boldAscent;
			kTotalLabel.drawRight(g, 0, baseline);
			totalVal.drawWithCommas(g, valRight, baseline);
			tempPercent.setValue(100.0);
			tempPercent.drawLeft(g, percentRight, baseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(tableLeftBorder + tableWidth,
																			tableTopBorder + tableHeight + tableBottomBorder);
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (y > tableTopBorder) {
			int dy = y - tableTopBorder - (kTableVertBorder - kLineSpacing);
			int lineHt = ascent + descent + kLineSpacing;
			int catIndex = dy / lineHt;
			NumVariable yVar = (NumVariable)getVariable(yKey);
			int nVals = yVar.noOfValues();
			if (catIndex >= nVals)
				return null;
			return new IndexPosInfo(catIndex);
		}
		
		return null;
	}
		
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
}