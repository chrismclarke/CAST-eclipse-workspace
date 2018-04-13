package cat;

import java.awt.*;

import dataView.*;


public class CreateFreqTableView extends CoreCreateTableView {
//	static public final String FREQ_TABLE_TALLIES = "freqTableTallies";
	
	static private final int kTallyRightBorder = 25;
	
	static private final int kHeadingTopBottomBorder = 3;
	static private final int kInnerTopBottomBorder = 7;
	static private final int kLineGap = 10;
	
	static private final int kSpacerWidth = 7;
	
	private String kTallyHeading = "Tallies";
	private String kFreqHeading = "Freq";
	
	private int maxCount, countRight;
	private Rectangle tallyRect, freqRect;
	
	private int counts[];
	
	public CreateFreqTableView(DataSet theData, XApplet applet, String catRowKey) {
		super(theData, applet, catRowKey);
		
		kTallyHeading = applet.translate("Tallies");
		kFreqHeading = applet.translate("Freq");
	
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		int maxCounts[] = catRowVar.getCounts();
		maxCount = 0;
		for (int i=0 ; i<maxCounts.length ; i++)
			maxCount = Math.max(maxCount, maxCounts[i]);
		
		counts = new int[noOfRowCats];
	}
	
	public void clearCounts() {
		for (int i=0 ; i<noOfRowCats ; i++)
			counts[i] = 0;
		selectedRowCat = -1;
		repaint();
	}
	
	public void completeCounts() {
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		counts = catRowVar.getCounts();
		selectedRowCat = -1;
		repaint();
	}
	
	public void addCatValue(int i) {
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		int catIndex = catRowVar.getItemCategory(i);
		counts[catIndex] ++;
		selectedRowCat = catIndex;
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (!super.initialise(g))
			return false;
		
//		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		
		Font standardFont = g.getFont();
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		int tallyHeadingWidth = fm.stringWidth(kTallyHeading);
		int freqHeadingWidth = fm.stringWidth(kFreqHeading);
		
		g.setFont(standardFont);
		fm = g.getFontMetrics();
		
			int tallyTop = boldAscent + boldDescent + 2 * kHeadingTopBottomBorder;
			int tallyLeft = 2 * kRowCatNameLeftRightBorder + Math.max(maxRowCatWidth, rowVarNameWidth);
			int tallyHeight = 2 * kInnerTopBottomBorder + noOfRowCats * (ascent + descent)
																															+ (noOfRowCats - 1) * kLineGap;
			int tallyWidth = kTallyLeftBorder + kTallyRightBorder
																	+ Math.max(maxCount * kTallyWidth, tallyHeadingWidth);
		tallyRect = new Rectangle(tallyLeft, tallyTop, tallyWidth, tallyHeight);
		
			int freqLeft = tallyLeft + tallyWidth + kSpacerWidth;
			int maxCountWidth = fm.stringWidth(String.valueOf(maxCount));
			int freqWidth = 2 * kFreqLeftRightBorder + Math.max(maxCountWidth, freqHeadingWidth);
		freqRect = new Rectangle(freqLeft, tallyTop, freqWidth, tallyHeight);
		countRight = freqLeft + (freqWidth + maxCountWidth) / 2;
		
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		
		Font standardFont = g.getFont();
		g.setFont(boldFont);
		int headingBaseline = kHeadingTopBottomBorder + boldAscent;
		g.drawString(catRowVar.name, 0, headingBaseline);
		g.drawString(kTallyHeading, tallyRect.x + kTallyLeftBorder, headingBaseline);
		int freqHeadingWidth = g.getFontMetrics().stringWidth(kFreqHeading);
		g.drawString(kFreqHeading, freqRect.x + (freqRect.width - freqHeadingWidth) / 2, headingBaseline);
		
		g.setColor(Color.white);
		g.fillRect(tallyRect.x, tallyRect.y, tallyRect.width, tallyRect.height);
		g.fillRect(freqRect.x, freqRect.y, freqRect.width, freqRect.height);
		g.setColor(Color.black);
		g.drawLine(tallyRect.x, tallyRect.y, tallyRect.x, tallyRect.y + tallyRect.height - 1);
		g.drawLine(freqRect.x, freqRect.y, freqRect.x, freqRect.y + freqRect.height - 1);
		
		int catBaseline = tallyRect.y + kInnerTopBottomBorder + ascent;
		NumValue countVal = new NumValue(0, 0);
		for (int cat=0 ; cat<noOfRowCats ; cat++) {
			g.setColor(Color.blue);
			g.setFont(boldFont);
			catRowVar.getLabel(cat).drawRight(g, kRowCatNameLeftRightBorder, catBaseline);
			if (cat == selectedRowCat) {
				int arrowCentre = catBaseline - ascent / 2;
				int x = tallyRect.x - 2;
				drawRedArrow(g, x, arrowCentre);
			}
			
			drawTallies(g, counts[cat], catBaseline - ascent, tallyRect.x + kTallyLeftBorder,
																															cat == selectedRowCat);
			
			g.setColor((cat == selectedRowCat) ? Color.red : getForeground());
			g.setFont(standardFont);
			
			countVal.setValue(counts[cat]);
			countVal.drawLeft(g, countRight, catBaseline);
			
			catBaseline += (ascent + descent + kLineGap);
		}
	}
	
//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(freqRect.x + freqRect.width, freqRect.y + freqRect.height);
	}
	
}