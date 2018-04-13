package cat;

import java.awt.*;

import dataView.*;


public class CatKeyView extends CatDataView {
//	static final public String CATKEY = "catKey";
	
	protected int catNameWidth, catTitleWidth;
	protected int ascent, descent, leading;
	protected int tableTopBorder, tableLeftBorder, tableHeight;
	
	static final protected int kNameTableGap = 8;
	static final protected int kHeadingTableGap = 5;
	static final protected int kTableVertBorder = 4;
	static final protected int kTableHorizBorder = 10;
	static final protected int kExtraLineSpacing = 4;
	static final protected String kKeyString = "Key";
	
	public CatKeyView(DataSet theData, XApplet applet, String catKey, int dragType) {
		super(theData, applet, catKey, dragType);
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (super.initialise(variable, g)) {
			int noOfCats = count.length;
			g.setFont(getFont());
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			leading = fm.getLeading() + kExtraLineSpacing;
			
			catTitleWidth = fm.stringWidth(kKeyString);
			
			catNameWidth = 0;
			for (int i=0 ; i<noOfCats ; i++) {
				int nameWidth = variable.getLabel(i).stringWidth(g);
				if (nameWidth > catNameWidth)
					catNameWidth = nameWidth;
			}
			
			tableTopBorder = ascent + descent + kHeadingTableGap;
			tableLeftBorder = catNameWidth + kNameTableGap;
			tableHeight = count.length * (ascent + descent) + (count.length - 1) * leading
																										+ 2 * kTableVertBorder;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, g);
		
		boolean selectedCats[] = getSelectedCats();
		boolean noSelection = noSelectedCats(selectedCats);
		
		g.drawString(kKeyString, 0, ascent);
		
		int baseLine = tableTopBorder + kTableVertBorder + ascent;
		for (int i=0 ; i<count.length ; i++) {
			g.setColor(getCatColor(i, noSelection || selectedCats[i]));
			variable.getLabel(i).drawRight(g, 0, baseLine);
			baseLine += (ascent + descent + leading);
		}
		
		if (targetBefore != -1) {
			g.setColor(Color.red);
			int vert = tableTopBorder + kTableVertBorder - 3 + (ascent + descent + leading) * targetBefore;
			g.fillRect(0, vert, getSize().width, 3);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (y > tableTopBorder) {
			int dy = y - tableTopBorder;
			int lineHt = ascent + descent + leading;
			int catIndex = dy / lineHt;
			if (catIndex >= count.length)
				return null;
			return new CatPosInfo(catIndex, dy >= catIndex * lineHt + lineHt / 2);
		}
		
		return null;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, getGraphics());
		
		return new Dimension(tableLeftBorder,
									tableTopBorder + tableHeight);
	}
}