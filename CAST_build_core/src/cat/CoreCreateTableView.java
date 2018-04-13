package cat;

import java.awt.*;

import dataView.*;


abstract public class CoreCreateTableView extends DataView {
	
	static protected final int kRowCatNameLeftRightBorder = 16;
	static protected final int kTallyLeftBorder = 10;
	static protected final int kTallyWidth = 5;
	static protected final int kFreqLeftRightBorder = 7;
	
	static protected final int kArrowWidth = 11;
	
	protected String catRowKey;
	protected int noOfRowCats;
	protected int selectedRowCat = -1;
	
	protected Font boldFont;
	
	private boolean initialised = false;
	protected int ascent, descent, boldAscent, boldDescent;
	protected int rowVarNameWidth, maxRowCatWidth;
	
	public CoreCreateTableView(DataSet theData, XApplet applet, String catRowKey) {
		super(theData, applet, new Insets(0,0,0,0));
		this.catRowKey = catRowKey;
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		noOfRowCats = catRowVar.noOfCategories();
		boldFont = applet.getStandardBoldFont();
	}
	
	abstract public void clearCounts();
	abstract public void completeCounts();
	abstract public void addCatValue(int i);
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
			
			Font standardFont = g.getFont();
			g.setFont(boldFont);
			FontMetrics fm = g.getFontMetrics();
			boldAscent = fm.getAscent();
			boldDescent = fm.getDescent();
			
			rowVarNameWidth = fm.stringWidth(catRowVar.name);
			maxRowCatWidth = 0;
			for (int i=0 ; i<noOfRowCats ; i++)
				maxRowCatWidth = Math.max(maxRowCatWidth, catRowVar.getLabel(i).stringWidth(g));
			
			g.setFont(standardFont);
			fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected int tallyHeight() {
		return ascent + descent;
	}
	
	protected void drawTallies(Graphics g, int count, int tallyTop, int tallyLeft,
																																				boolean selected) {
		int tallyHoriz = tallyLeft;
		int tallyHt = tallyHeight();
		g.setColor(getForeground());
		if (selected) {
			for (int i=0 ; i<count-1 ; i++) {
				if (i % 5 == 4)
					g.drawLine(tallyHoriz - 2, tallyTop, tallyHoriz - 5 * kTallyWidth + 2, tallyTop + tallyHt);
				else
					g.fillRect(tallyHoriz, tallyTop, 1, tallyHt);
				tallyHoriz += kTallyWidth;
			}
			g.setColor(Color.red);
			if (count % 5 == 0) {
				g.drawLine(tallyHoriz - 2, tallyTop, tallyHoriz - 5 * kTallyWidth + 2, tallyTop + tallyHt - 1);
				g.drawLine(tallyHoriz - 2, tallyTop + 1, tallyHoriz - 5 * kTallyWidth + 2, tallyTop + tallyHt);
			}
			else
				g.fillRect(tallyHoriz, tallyTop, 2, tallyHt);
		}
		else
			for (int i=0 ; i<count ; i++) {
				if (i % 5 == 4)
					g.drawLine(tallyHoriz - 2, tallyTop, tallyHoriz - 5 * kTallyWidth + 2, tallyTop + tallyHt);
				else
					g.fillRect(tallyHoriz, tallyTop, 1, tallyHt);
				tallyHoriz += kTallyWidth;
			}
	}
	
	protected void drawRedArrow(Graphics g, int pointHoriz, int pointVert) {
		g.setColor(Color.red);
		for (int i=0 ; i<6 ; i++)
			g.drawLine(pointHoriz-i, pointVert - i, pointHoriz-i, pointVert + i);
		for (int i=6 ; i<10 ; i++)
			g.drawLine(pointHoriz-i, pointVert - 2, pointHoriz-i, pointVert + 2);
		g.setColor(getForeground());
	}


//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}