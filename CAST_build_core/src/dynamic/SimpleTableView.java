package dynamic;

import java.awt.*;

import dataView.*;


public class SimpleTableView extends DataView {
//	static final public String SIMPLE_TABLE = "simpleTable";
	
//	static final private int kTotalTableGap = 5;
	static final private int kKeyTableGap = 4;
	static final private int kColumnGap = 10;
	static final private int kRowGap = 2;
	static final private int kKeyRectSide = 20;
	static final private int kRectKeyGap = 3;
	
	private String kPercentString, kTotalString;
	
	private String valueName;
	
	private String yKey, keyKey;
	private Color[] keyColor;
	private int percentDecs;
	
	private Font boldFont;
	
	private int keyRectWidth, maxValueWidth, maxKeyWidth, maxPercentWidth;
	private int valueTitleWidth, keyTitleWidth, percentTitleWidth;
	private int boldAscent, boldDescent, ascent, descent, totalHeight;
	
	private boolean initialised = false;
	
	private boolean colorText = true;
	
	public SimpleTableView(DataSet theData, XApplet applet,
									String yKey, String keyKey, Color[] keyColor, int percentDecs) {
		super(theData, applet, new Insets(0,0,0,0));
		kPercentString = applet.translate("Percent");
		valueName = applet.translate("Count");
		kTotalString = applet.translate("Total");
		
		this.yKey = yKey;
		this.keyKey = keyKey;
		this.keyColor = keyColor;
		this.percentDecs = percentDecs;
		boldFont = new Font(applet.getFont().getName(), Font.BOLD, applet.getFont().getSize());
	}
	
	public void setKeyColors(Color[] keyColor) {
		this.keyColor = keyColor;
	}
	
	public void setColorText(boolean colorText) {
		this.colorText = colorText;
	}
	
	public void setValueName(String valueName) {
		this.valueName = valueName;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		double maxTotal = 0.0;
		if (yVar instanceof NumSeriesVariable) {
			NumSeriesVariable ySeriesVar = (NumSeriesVariable)getVariable(yKey);
			int seriesLength = ySeriesVar.seriesLength();
			double total[] = new double[seriesLength];
			ValueEnumeration ye = ySeriesVar.values();
			while (ye.hasMoreValues()) {
				NumSeriesValue y = (NumSeriesValue)ye.nextValue();
				for (int i=0 ; i<seriesLength ; i++)
					total[i] += y.toDouble(i);
			}
			for (int i=0 ; i<seriesLength ; i++)
				maxTotal = Math.max(maxTotal, total[i]);
		}
		else {
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues())
				maxTotal += ye.nextDouble();
		}
		CatVariable keyVar = (CatVariable)getVariable(keyKey);
		
		Font stdFont = g.getFont();
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		boldDescent = fm.getDescent();
		
		valueTitleWidth = fm.stringWidth(valueName);
		keyTitleWidth = fm.stringWidth(keyVar.name);
		percentTitleWidth = fm.stringWidth(kPercentString);
		
		g.setFont(stdFont);
		fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		NumValue totalValue = new NumValue(maxTotal, yVar.getMaxDecimals());
		maxValueWidth = totalValue.stringWidth(g);
		
		maxKeyWidth = keyVar.getMaxWidth(g);
		maxKeyWidth = Math.max(maxKeyWidth, fm.stringWidth(kTotalString));
		keyRectWidth = kKeyRectSide + kRectKeyGap;
		
		NumValue maxPercent = new NumValue(100, percentDecs);
		maxPercentWidth = maxPercent.stringWidth(g);
		
		int nCats = keyVar.noOfCategories();
		int rowHeight = Math.max(kKeyRectSide, ascent + descent);
		totalHeight = nCats * rowHeight + 2 * (boldAscent + boldDescent)
																															+ (nCats + 3) * kRowGap;
	}
	
	
	public void paintView(Graphics g) {
		initialise(g);
		CatVariable keyVar = (CatVariable)getVariable(keyKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Font stdFont = g.getFont();
		
		g.setFont(boldFont);
		int baseline = boldAscent;
		int keyLeft = keyRectWidth;
		g.drawString(keyVar.name, keyLeft, baseline);
		
		int tableLeft = keyLeft + Math.max(maxKeyWidth, keyTitleWidth) + kKeyTableGap;
		int valueRight = tableLeft + kColumnGap + Math.max(maxValueWidth, valueTitleWidth);
		g.drawString(valueName, valueRight - valueTitleWidth, baseline);
		int percentRight = valueRight + kColumnGap + Math.max(maxPercentWidth, percentTitleWidth);
		g.drawString(kPercentString, percentRight - percentTitleWidth, baseline);
		
		g.setFont(stdFont);
		int tableTop = baseline + boldDescent + kRowGap;
		int nCats = keyVar.noOfCategories();
		int innerWidth = percentRight + kColumnGap - tableLeft;
		int innerHeight = nCats * Math.max(ascent + descent, kKeyRectSide) + (nCats + 1) * kRowGap;
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, tableTop, innerWidth, innerHeight);
		g.setColor(getForeground());
		
		double total = 0.0;
		for (int i=0 ; i<nCats ; i++)
			total += yVar.doubleValueAt(i);
		NumValue percentVal = new NumValue(0.0, percentDecs);
		baseline = tableTop + kRowGap + Math.max(ascent, (ascent + kKeyRectSide) / 2);
		for (int i=0 ; i<nCats ; i++) {
			g.setColor(getForeground());
			int rectTop = baseline - (kKeyRectSide + ascent) / 2;
			g.drawRect(0, rectTop, kKeyRectSide, kKeyRectSide);
			g.setColor(keyColor[i]);
			g.fillRect(1, rectTop + 1, kKeyRectSide - 1, kKeyRectSide - 1);
			
			if (!colorText)
				g.setColor(getForeground());
			keyVar.valueAt(i).drawRight(g, keyLeft, baseline);
			yVar.valueAt(i).drawLeft(g, valueRight, baseline);
			percentVal.setValue(yVar.doubleValueAt(i) / total * 100.0);
			percentVal.drawLeft(g, percentRight, baseline);
			
			baseline += Math.max(ascent + descent, kKeyRectSide) + kRowGap;
		}
		
		g.setColor(getForeground());
		g.setFont(boldFont);
		baseline = tableTop + innerHeight + kRowGap + boldAscent;
		g.drawString(kTotalString, keyLeft, baseline);
		NumValue totalValue = new NumValue(total, yVar.getMaxDecimals());
		totalValue.drawLeft(g, valueRight, baseline);
		percentVal.setValue(100.0);
		percentVal.drawLeft(g, percentRight, baseline);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int width = keyRectWidth + Math.max(maxKeyWidth, keyTitleWidth) + Math.max(maxValueWidth, valueTitleWidth)
										+ Math.max(maxPercentWidth, percentTitleWidth) + kKeyTableGap + 3 * kColumnGap;
		
		return new Dimension(width, totalHeight);
	}
}