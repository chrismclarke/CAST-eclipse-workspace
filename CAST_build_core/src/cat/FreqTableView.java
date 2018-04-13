package cat;

import java.awt.*;

import dataView.*;


public class FreqTableView extends CatKeyView {
//	static final public String FREQTABLE = "freqTable";
	
	static final public int SHORT_HEADINGS = 0;
	static final public int LONG_HEADINGS = 1;
	
	static final public int NO_RELFREQ = 0;
	static final public int PROPN = 1;
	static final public int PERCENT = 2;
	
	static final public int NO_COUNT = 0;
	static final public int COUNT = 1;
	
	static final private int kTotalTableGap = 5;
	static final private int kColumnGap = 20;
	static final private String kShortPercentString = "%";
	
	static final private Color kPalestGrey = new Color(0xEEEEEE);
	static final private Color kDarkBlue = new Color(0x000099);
	
	private String kShortFreqString, kLongFreqString;
	private String kLongPercentString;
	private String kShortPropnString, kLongPropnString;
	
	private int propnDecs;
	
	private int freqValWidth, freqTitleWidth, propnValWidth, propnTitleWidth;
	private int freqColumnWidth, propnColumnWidth, freqTableWidth, relFreqTableWidth;
	
	private int headingLength = SHORT_HEADINGS;
	private int relFreqType = PROPN;
	private boolean colourCategories = true;
	
	private String freqString;
	
	static String countWithCommas(int count) {
		if (count == 0)
			return "0";
		StringBuffer sb = new StringBuffer(20);
		int digitPos = 0;
		while (count != 0) {
			sb.insert(0, (char)('0' + count % 10));
			digitPos = (digitPos + 1) % 3;
			count /= 10;
			if (count > 0 && digitPos == 0)
				sb.insert(0, ',');
		}
		return sb.toString();
	}
	
	public FreqTableView(DataSet theData, XApplet applet, String catKey, int dragType, int propnDecs) {
		super(theData, applet, catKey, dragType);
		kShortFreqString = applet.translate("Count");
		kLongFreqString = applet.translate("Count");
		kLongPercentString = applet.translate("Percentage");
		kShortPropnString = applet.translate("Propn");
		kLongPropnString = applet.translate("Proportion");
		this.propnDecs = propnDecs;
	}
	
	public FreqTableView(DataSet theData, XApplet applet, String catKey, int dragType,
							int propnDecs, int headingLength, int relFreqType, String countName, boolean colourCategories) {
		this(theData, applet, catKey, dragType, propnDecs);
		
		this.headingLength = headingLength;
		this.relFreqType = relFreqType;
		freqString = countName;
		this.colourCategories = colourCategories;
	}
	
	public void setRelFreqDisplay(int relFreqType) {
		this.relFreqType = relFreqType;
		repaint();
	}
	
	public void setDecimals(int propnDecs) {
		this.propnDecs = propnDecs;
		initialised = false;
		repaint();
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (super.initialise(variable, g)) {
			int noOfCats = count.length;
			int maxCount = 0;
			for (int i=0 ; i<noOfCats ; i++)
				if (count[i] > maxCount)
					maxCount = count[i];
			
			FontMetrics fm = g.getFontMetrics();
			
			NumValue testVal = new NumValue(0.0, propnDecs);
			propnValWidth = testVal.stringWidth(g);
			propnTitleWidth = (headingLength == SHORT_HEADINGS) ?
						Math.max(fm.stringWidth(kShortPropnString), fm.stringWidth(kShortPercentString))
					:	Math.max(fm.stringWidth(kLongPropnString), fm.stringWidth(kLongPercentString));
			
			if (freqString == null)		//		otherwise a different name was specified
				freqString = (headingLength == SHORT_HEADINGS) ? kShortFreqString : kLongFreqString;
			freqTitleWidth = fm.stringWidth(freqString);
			freqValWidth = fm.stringWidth(countWithCommas(maxCount));
			
			freqColumnWidth = Math.max(freqValWidth, freqTitleWidth);
			propnColumnWidth = Math.max(propnValWidth, propnTitleWidth);
			
			freqTableWidth = freqColumnWidth + 2 * kTableHorizBorder;
			relFreqTableWidth = freqTableWidth + propnColumnWidth + kColumnGap;
			
			return true;
		}
		else
			return false;
	}
	
	protected Color getCatColor(int catIndex, boolean boldNotDim) {
		if (colourCategories)
			return super.getCatColor(catIndex, boldNotDim);
		else if (boldNotDim)
			return kDarkBlue;
		else
			return Color.gray;
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		boolean selectedCats[] = getSelectedCats();
		boolean noSelection = noSelectedCats(selectedCats);
		
		int tableWidth = (relFreqType != NO_RELFREQ) ? relFreqTableWidth : freqTableWidth;
		
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
		int propnColStart = freqColStart + freqColumnWidth + kColumnGap;
		int propnValRight = propnColStart + (propnColumnWidth + propnValWidth) / 2;
		int propnTitleLeft = propnColStart + (propnColumnWidth - propnTitleWidth) / 2;
		
		FontMetrics fm = g.getFontMetrics();
		
		g.drawString(freqString, freqTitleLeft, ascent);
		int totalBaseline = tableTopBorder + tableHeight + kTotalTableGap + ascent;
		String totalString = countWithCommas(totalCount);
		g.drawString(totalString, freqValRight - fm.stringWidth(totalString), totalBaseline);
		if (relFreqType != NO_RELFREQ) {
			String relFreqString = (headingLength == SHORT_HEADINGS) ? 
										((relFreqType == PROPN) ? kShortPropnString : kShortPercentString)
									:	((relFreqType == PROPN) ? kLongPropnString : kLongPercentString);
			g.drawString(relFreqString, propnTitleLeft, ascent);
			NumValue total = (relFreqType == PROPN) ? new NumValue(1.0, propnDecs)
																:	new NumValue(100.0, propnDecs - 2);
			total.drawLeft(g, propnValRight, totalBaseline);
		}
		
		int baseLine = tableTopBorder + kTableVertBorder + ascent;
		int rowTop = tableTopBorder;
		for (int i=0 ; i<count.length ; i++) {
			int rowBottom = baseLine + descent + (i == count.length - 1 ? kTableVertBorder : 0);
			if (!noSelection && !selectedCats[i]) {
				g.setColor(kPalestGrey);
				g.fillRect(tableLeftBorder, rowTop, tableWidth, rowBottom - rowTop);
			}
			g.setColor(getCatColor(i, true));
			variable.getLabel(i).drawRight(g, 0, baseLine);
			
			String countString = countWithCommas(count[i]);
			g.setColor(getCatColor(i, noSelection || selectedCats[i]));
			if (selectedCats[i]) {
				Font f = getFont();
				g.setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
			}
			g.drawString(countString, freqValRight - fm.stringWidth(countString), baseLine);
			
			if (relFreqType != NO_RELFREQ) {
				NumValue relFreq = (relFreqType == PROPN)
									? new NumValue(count[i] / (double)totalCount, propnDecs)
									: new NumValue(100.0 * count[i] / (double)totalCount, propnDecs - 2);
				relFreq.drawLeft(g, propnValRight, baseLine);
			}
			g.setFont(getFont());
			baseLine += (ascent + descent + leading);
			rowTop = rowBottom;
		}
		
		if (targetBefore != -1) {
			g.setColor(Color.red);
			int vert = tableTopBorder + kTableVertBorder - 3 + (ascent + descent + leading) * targetBefore;
			g.fillRect(0, vert, getSize().width, 3);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, getGraphics());
		
		return new Dimension(tableLeftBorder + relFreqTableWidth,
									tableTopBorder + tableHeight + kTotalTableGap + ascent + descent);
	}
}