package exper2;

import java.awt.*;

import dataView.*;


public class RepsTableView extends DataView {
	static final private int kNameTableGap = 8;
	static final private int kHeadingTableGap = 5;
	static final private int kTableVertBorder = 4;
	static final private int kTableHorizBorder = 10;
	static final private int kExtraLineSpacing = 4;
	static final private int kTotalTableGap = 5;
	
	static final private Color kHeadingColor = new Color(0x990000);
	
	private String kTreatmentsString, kReplicatesString;
	
	private String catKey;
	
	protected int catNameWidth, catTitleWidth;
	protected int ascent, descent, leading;
	protected int tableTopBorder, tableLeftBorder, tableHeight;
	
	private int freqValWidth, freqTitleWidth;
	private int freqColumnWidth, freqTableWidth;
	
	private boolean initialised = false;
	
	public RepsTableView(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, null);
		this.catKey = catKey;
	
		kTreatmentsString = applet.translate("Treatments");
		kReplicatesString = applet.translate("Replicates");
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			initialised = true;
			
			CatVariable xVar = (CatVariable)getVariable(catKey);
			int noOfCats = xVar.noOfCategories();
			g.setFont(getFont());
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			leading = fm.getLeading() + kExtraLineSpacing;
			
			catTitleWidth = fm.stringWidth(kTreatmentsString);
			
			catNameWidth = 0;
			for (int i=0 ; i<noOfCats ; i++) {
				int nameWidth = xVar.getLabel(i).stringWidth(g);
				if (nameWidth > catNameWidth)
					catNameWidth = nameWidth;
			}
			
			tableTopBorder = ascent + descent + kHeadingTableGap;
			tableLeftBorder = Math.max(catNameWidth, catTitleWidth) + kNameTableGap;
			tableHeight = noOfCats * (ascent + descent) + (noOfCats - 1) * leading
																										+ 2 * kTableVertBorder;
																										
			int totalCount = 0;
			int count[] = xVar.getCounts();
			for (int i=0 ; i<noOfCats ; i++)
				totalCount += count[i];
			
			freqTitleWidth = fm.stringWidth(kReplicatesString);
			freqValWidth = fm.stringWidth(String.valueOf(totalCount));
			
			freqColumnWidth = Math.max(freqValWidth, freqTitleWidth);
			
			freqTableWidth = freqColumnWidth + 2 * kTableHorizBorder;
			
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable xVar = (CatVariable)getVariable(catKey);
		int count[] = xVar.getCounts();
		int totalCount = 0;
		for (int i=0 ; i<count.length ; i++)
			totalCount += count[i];
		
		int tableWidth = freqTableWidth;
		
		g.setColor(Color.white);
		g.fillRect(tableLeftBorder, tableTopBorder, tableWidth, tableHeight);
		
		g.setColor(kHeadingColor);
		g.drawLine(tableLeftBorder, tableTopBorder - 1, tableLeftBorder + tableWidth - 1,
																								tableTopBorder - 1);
		g.drawLine(tableLeftBorder, tableTopBorder + tableHeight, tableLeftBorder + tableWidth - 1,
																						tableTopBorder + tableHeight);
		
		int freqColStart = tableLeftBorder + kTableHorizBorder;
		int freqValRight = freqColStart + (freqColumnWidth + freqValWidth) / 2;
		int freqTitleLeft = freqColStart + (freqColumnWidth - freqTitleWidth) / 2;
		
		FontMetrics fm = g.getFontMetrics();
		
		g.drawString(kTreatmentsString, 0, ascent);
		
		g.drawString(kReplicatesString, freqTitleLeft, ascent);
		int totalBaseline = tableTopBorder + tableHeight + kTotalTableGap + ascent;
		String totalString = String.valueOf(totalCount);
		g.drawString(totalString, freqValRight - fm.stringWidth(totalString), totalBaseline);
		
		g.setColor(getForeground());
		int baseLine = tableTopBorder + kTableVertBorder + ascent;
//		int rowTop = tableTopBorder;
		for (int i=0 ; i<count.length ; i++) {
//			int rowBottom = baseLine + descent + (i == count.length - 1 ? kTableVertBorder : 0);
			xVar.getLabel(i).drawRight(g, 0, baseLine);
			
			String countString = String.valueOf(count[i]);
			g.drawString(countString, freqValRight - fm.stringWidth(countString), baseLine);
			
			g.setFont(getFont());
			baseLine += (ascent + descent + leading);
//			rowTop = rowBottom;
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(tableLeftBorder + freqTableWidth,
									tableTopBorder + tableHeight + kTotalTableGap + ascent + descent);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (catKey.equals(key)) {
			initialised = false;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}