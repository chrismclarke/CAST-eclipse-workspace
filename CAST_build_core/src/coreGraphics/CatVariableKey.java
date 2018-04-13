package coreGraphics;

import java.awt.*;

import dataView.*;


public class CatVariableKey extends DataView {
//	static final public String CATKEY = "catKey";
	
	static final private int kHeadingTableGap = 5;
	static final private int kExtraLineSpacing = 4;
	static final private int kKeyRectWidth = 20;
	static final private int kRectCatGap = 3;
	
	private String catKey;
	
	private boolean initialised = false;
	
	private int catNameWidth, catTitleWidth;
	private int ascent, descent, rowAscent, rowDescent;
	private int tableTopBorder, tableHeight;
	
	public CatVariableKey(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
	}
	
	protected boolean initialise(CatVariableInterface variable, Graphics g) {
		if (!initialised) {
			int noOfCats = variable.noOfCategories();
			g.setFont(getFont());
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			rowAscent = Math.max(ascent, kKeyRectWidth * 2 / 3);
			rowDescent = Math.max(descent, kKeyRectWidth / 3);
			
			catTitleWidth = fm.stringWidth(((CoreVariable)variable).name);
			
			catNameWidth = 0;
			for (int i=0 ; i<noOfCats ; i++) {
				int nameWidth = variable.getLabel(i).stringWidth(g);
				if (nameWidth > catNameWidth)
					catNameWidth = nameWidth;
			}
			
			tableTopBorder = ascent + descent + kHeadingTableGap;
			tableHeight = noOfCats * (rowAscent + rowDescent) + (noOfCats - 1) * kExtraLineSpacing;
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		CatVariableInterface variable = (CatVariableInterface)getVariable(catKey);
		int noOfCats = variable.noOfCategories();
		initialise(variable, g);
		
		g.drawString(((CoreVariable)variable).name, 0, ascent);
		
		int baseline = tableTopBorder + rowAscent;
		for (int i=0 ; i<noOfCats ; i++) {
			g.setColor(Color.black);
			g.drawRect(0, baseline - kKeyRectWidth * 2 / 3, kKeyRectWidth, kKeyRectWidth);
			g.setColor(CatPieChartView.catColor[i]);
			g.fillRect(1, baseline - kKeyRectWidth * 2 / 3 + 1, kKeyRectWidth - 1, kKeyRectWidth - 1);
			
			variable.getLabel(i).drawRight(g, kKeyRectWidth + kRectCatGap, baseline);
			baseline += (rowAscent + rowDescent + kExtraLineSpacing);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariableInterface variable = (CatVariableInterface)getVariable(catKey);
		initialise(variable, getGraphics());
		
		return new Dimension(Math.max(catTitleWidth, catNameWidth + kKeyRectWidth + kRectCatGap),
									tableTopBorder + tableHeight + 2);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}