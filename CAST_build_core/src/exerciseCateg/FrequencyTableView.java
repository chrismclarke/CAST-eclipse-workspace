package exerciseCateg;

import java.awt.*;

import dataView.*;


public class FrequencyTableView extends DataView {
//	static final public String FREQ_TABLE = "freqTable";
	
	static final private int kNameTableGap = 8;
	static final private int kHeadingTableGap = 5;
	static final private int kTableVertBorder = 4;
	static final private int kTableHorizBorder = 10;
//	static final private int kExtraLineSpacing = 4;
	static final private int kTotalTableGap = 5;
	static final private int kColumnGap = 10;
	
	static final private String kDefaultCumTitle = "Cumulative";
	
	static final private Color kHeadingColor = new Color(0x000099);
	
	private String catKey;
	
	private boolean selectedCats[];
	
	private boolean hasCumColumn = false;
	
	private String freqTitle;
	private String cumTitle = kDefaultCumTitle;
	
	private int ascent, descent, leading;
	private int catNameWidth, catLabelWidth;
	private int tableTopBorder, tableLeftBorder, tableHeight;
	private int freqTableWidth;
	
	private int freqValWidth, freqTitleWidth;
	private int freqColumnWidth;
	
	private int cumValWidth, cumTitleWidth;
	private int cumColumnWidth;
	
	private boolean initialised = false;
	
	public FrequencyTableView(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, null);
		freqTitle = applet.translate("Count");
		this.catKey = catKey;
	}
	
	public void selectCats(boolean[] selectedCats) {
		this.selectedCats = selectedCats;
	}
	
	public void clearSelection() {
		selectedCats = null;
	}
	
	public void setCountString(String s) {
		freqTitle = s;
	}
	
	public void setHasCumColumn(boolean hasCumColumn) {
		this.hasCumColumn = hasCumColumn;
		initialised = false;
	}
	
	protected void doInitialisation(Graphics g) {
		CatVariable yVar = (CatVariable)getVariable(catKey);
		int count[] = yVar.getCounts();
		int noOfCats = count.length;
		int maxCount = 0;
		catNameWidth = g.getFontMetrics().stringWidth(yVar.name);
		catLabelWidth = 0;
		for (int i=0 ; i<noOfCats ; i++) {
			if (count[i] > maxCount)
				maxCount = count[i];
			catLabelWidth = Math.max(catLabelWidth, yVar.getLabel(i).stringWidth(g));
		}
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		freqTitleWidth = fm.stringWidth(freqTitle);
		freqValWidth = new NumValue(maxCount, 0).stringWidthWithCommas(g);
		freqColumnWidth = Math.max(freqValWidth, freqTitleWidth);
		
		freqTableWidth = freqColumnWidth + 2 * kTableHorizBorder;
		
		if (hasCumColumn) {
			cumTitleWidth = fm.stringWidth(cumTitle);
			cumValWidth = freqValWidth;
			cumColumnWidth = Math.max(cumValWidth, cumTitleWidth);
			
			freqTableWidth += cumColumnWidth + kColumnGap;
		}
			
		tableTopBorder = ascent + descent + kHeadingTableGap;
		tableLeftBorder = Math.max(catLabelWidth, catNameWidth) + kNameTableGap;
		tableHeight = count.length * (ascent + descent) + (count.length - 1) * leading
																									+ 2 * kTableVertBorder;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(catKey)) {
			initialised = false;
			selectedCats = null;
			invalidate();
			repaint();
		}
	}
	
	
	public void paintView(Graphics g) {
		initialise(g);
		CatVariable yVar = (CatVariable)getVariable(catKey);
		int count[] = yVar.getCounts();
		
		int tableWidth = freqTableWidth;
		
		g.setColor(Color.white);
		g.fillRect(tableLeftBorder, tableTopBorder, tableWidth, tableHeight);
		g.setColor(Color.black);
		g.drawLine(tableLeftBorder, tableTopBorder - 1, tableLeftBorder + tableWidth - 1,
																								tableTopBorder - 1);
		g.drawLine(tableLeftBorder, tableTopBorder + tableHeight, tableLeftBorder + tableWidth - 1,
																						tableTopBorder + tableHeight);
		
		int freqColStart = tableLeftBorder + kTableHorizBorder;
		int freqValRight = freqColStart + (freqColumnWidth + freqValWidth) / 2;
		int freqTitleLeft = freqColStart + (freqColumnWidth - freqTitleWidth) / 2;
		
		int cumColStart = freqColStart + freqColumnWidth + kColumnGap;
		int cumValRight = hasCumColumn ? cumColStart + (cumColumnWidth + cumValWidth) / 2 : 0;
		
		FontMetrics fm = g.getFontMetrics();
		
		g.setColor(kHeadingColor);
		g.drawString(yVar.name, 0, ascent);
		g.drawString(freqTitle, freqTitleLeft, ascent);
		if (hasCumColumn) {
			int cumTitleLeft = cumColStart + (cumColumnWidth - cumTitleWidth) / 2;
			g.drawString(cumTitle, cumTitleLeft, ascent);
		}
		
		int totalBaseline = tableTopBorder + tableHeight + kTotalTableGap + ascent;
		int totalCount = 0;
		for (int i=0 ; i<count.length ; i++)
			totalCount += count[i];
		String totalString = new NumValue(totalCount, 0).getValueStringWithCommas();
		g.drawString(totalString, freqValRight - fm.stringWidth(totalString), totalBaseline);
		
		g.setColor(getForeground());
		
		int baseLine = tableTopBorder + kTableVertBorder + ascent;
		int rowTop = tableTopBorder;
		int catLabelLeft = Math.max((catNameWidth - catLabelWidth) / 2, 0);
		int cumCount = 0;
		for (int i=0 ; i<count.length ; i++) {
			int rowBottom = baseLine + descent + (i == count.length - 1 ? kTableVertBorder : 0);
			if (selectedCats != null && selectedCats[i]) {
				g.setColor(Color.yellow);
				g.fillRect(tableLeftBorder, rowTop, tableWidth, rowBottom - rowTop);
				g.setColor(getForeground());
			}
			yVar.getLabel(i).drawRight(g, catLabelLeft, baseLine);
			
			String countString = new NumValue(count[i], 0).getValueStringWithCommas();
			g.drawString(countString, freqValRight - fm.stringWidth(countString), baseLine);
			
			if (hasCumColumn) {
				cumCount += count[i];
				String cumString = new NumValue(cumCount, 0).getValueStringWithCommas();
				g.drawString(cumString, cumValRight - fm.stringWidth(cumString), baseLine);
			}
			
			baseLine += (ascent + descent + leading);
			rowTop = rowBottom;
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(tableLeftBorder + freqTableWidth,
									tableTopBorder + tableHeight + kTotalTableGap + ascent + descent);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}