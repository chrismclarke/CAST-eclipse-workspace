package histo;

import java.awt.*;

import dataView.*;


public class CatKey3View extends DataView {
//	static final public String CATKEY3 = "catKey3";
	
	private boolean initialised = false;
	
	private String catKey;
	
	private int catNameWidth, rowHeight;
	private int ascent, descent;
	
	static final private int kBetweenKeyGap = 3;
	static final private int kKeyCatNameGap = 5;
	
	static final private int kKeyBorder = 5;
	static final private int kKeyBoxSize = 16;
	
	public CatKey3View(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (!initialised) {
			int noOfCats = variable.noOfCategories();
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			catNameWidth = 0;
			for (int i=0 ; i<noOfCats ; i++) {
				int nameWidth = variable.getLabel(i).stringWidth(g);
				if (nameWidth > catNameWidth)
					catNameWidth = nameWidth;
			}
			
			rowHeight = Math.max((ascent + descent), kKeyBoxSize) + kBetweenKeyGap;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		int noOfCats = variable.noOfCategories();
		int rowTop = kKeyBorder;
		for (int i=0 ; i<noOfCats ; i++) {
			int keyTop = rowTop + (rowHeight - kKeyBoxSize) / 2;
			g.drawRect(kKeyBorder - 1, keyTop - 1, kKeyBoxSize + 1, kKeyBoxSize + 1);
			g.setColor((i==0) ? Histo2GroupView.kGroup1FillColor
																	: Histo2GroupView.kGroup0FillColor);
			g.fillRect(kKeyBorder, keyTop, kKeyBoxSize, kKeyBoxSize);
			
			int baseline = rowTop + (rowHeight + ascent - descent) / 2;
			g.setColor(getForeground());
			variable.getLabel(i).drawRight(g, kKeyBorder + kKeyBoxSize + kKeyCatNameGap, baseline);
			rowTop += rowHeight;
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, getGraphics());
		
		return new Dimension(2 * kKeyBorder + kKeyBoxSize + kKeyCatNameGap + catNameWidth,
									2 * kKeyBorder + rowHeight * variable.noOfCategories());
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}