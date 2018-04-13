package bivarCat;

import java.awt.*;

import dataView.*;


public class CatKey2View extends DataView {
	
	private boolean initialised = false;
	
	private String catKey;
	private int borderType;
	
	private int catNameWidth, varNameWidth, varNameHeight, rowHeight;
	private int ascent, descent;
	
	private Color[] changingColors = null;
	private boolean reversedCats;
	
	static final private int kVarNameKeyGap = 5;
	static final private int kBetweenKeyGap = 3;
	static final private int kKeyCatNameGap = 5;
	
	static final private int kKeyBorder = 5;
	static final private int kKeyBoxSize = 16;
	
	public CatKey2View(DataSet theData, XApplet applet, String catKey, int borderType) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
		this.borderType = borderType;
	}
	
	public void setColors(Color[] changingColors) {
		this.changingColors = changingColors;
	}
	
	public void changeColor(int index, Color newColor) {
		changingColors[index] = newColor;
		repaint();
	}
	
	public void setReversedCategories() {
		reversedCats = true;
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (!initialised) {
			int noOfCats = variable.noOfCategories();
			
			if (changingColors == null)
				changingColors = ContinTableView.getColors(borderType, noOfCats);
			
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
			FontMetrics fm = g.getFontMetrics();
			
			varNameWidth = fm.stringWidth(variable.name);
			varNameHeight = fm.getAscent() + fm.getDescent() + kVarNameKeyGap;
			
			g.setFont(oldFont);
			fm = g.getFontMetrics();
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
		
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(variable.name, kKeyBorder, kKeyBorder + fm.getAscent());
		g.setFont(oldFont);
		
		int noOfCats = variable.noOfCategories();
		int rowTop = kKeyBorder + varNameHeight;
		for (int i=reversedCats ? noOfCats-1 : 0
										; i>=0 && i<noOfCats
										; i=reversedCats ? i-1 : i+1) {
			int keyTop = rowTop + (rowHeight - kKeyBoxSize) / 2;
			g.setColor(Color.darkGray);
			g.drawRect(kKeyBorder - 1, keyTop - 1, kKeyBoxSize + 1, kKeyBoxSize + 1);
			g.setColor(changingColors[i]);
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
		
		return new Dimension(2 * kKeyBorder
						+ Math.max(varNameWidth, kKeyBoxSize + kKeyCatNameGap + catNameWidth),
								2 * kKeyBorder + varNameHeight + rowHeight * variable.noOfCategories());
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}