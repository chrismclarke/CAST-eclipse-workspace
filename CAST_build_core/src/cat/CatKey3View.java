package cat;

import java.awt.*;

import dataView.*;


public class CatKey3View extends DataView {
//	static final public String CATKEY = "catKey";
	
	static final private int kHeadingTableGap = 3;
	static final private int kTableBorder = 2;
	static final private int kExtraLineSpacing = 2;
	static final private int kKeyRectWidth = 20;
	static final private int kRectCatGap = 3;
	static final private int kLeftRightBorder = 4;
	
	static final private Color kDarkGreen = new Color(0x009900);
	static final private Color kPurple = new Color(0x9900CC);
	static final public Color kCatColour[] = {Color.blue, Color.red, kDarkGreen, Color.cyan, Color.darkGray, kPurple, Color.yellow, Color.magenta};
	
	private String catKey;
	
	protected int catNameWidth, catTitleWidth;
	protected int ascent, descent, leading, headingAscent;
	protected int tableTopBorder, tableHeight;
	
	private Color catColour[] = kCatColour;
	
	private boolean initialised = false;
	
	private boolean reverseOrder = false;
	private boolean fillNotCross = true;
	
	private Color keyFillColor = null;
	
	private Font headingFont;
	private boolean showHeading = true;
	
	public CatKey3View(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
	}
	
	public void setReverseOrder() {
		reverseOrder = true;
	}
	
	public void setFillNotCross(boolean fillNotCross) {
		this.fillNotCross = fillNotCross;
	}
	
	public void setKeyFillColor(Color keyFillColor) {
		this.keyFillColor = keyFillColor;
	}
	
	public void setShowHeading(boolean showHeading) {
		this.showHeading = showHeading;
	}
	
	public void setCatColour(Color[] catColour) {
		this.catColour = catColour;
	}
	
	protected boolean initialise(CatVariable variable, Graphics g) {
		if (initialised)
			return false;
		initialised = true;
		
		int noOfCats = variable.noOfCategories();
		FontMetrics fm = g.getFontMetrics();
		ascent = Math.max(fm.getAscent(), kKeyRectWidth * 2 / 3);
		descent = Math.max(fm.getDescent(), kKeyRectWidth / 3);
		leading = fm.getLeading() + kExtraLineSpacing;
		
		catNameWidth = 0;
		for (int i=0 ; i<noOfCats ; i++) {
			int nameWidth = variable.getLabel(i).stringWidth(g);
			if (nameWidth > catNameWidth)
				catNameWidth = nameWidth;
		}
		
		Font standardFont = g.getFont();
		headingFont = new Font(standardFont.getName(), Font.BOLD, standardFont.getSize());
		g.setFont(headingFont);
		fm = g.getFontMetrics();
		headingAscent = fm.getAscent();
		catTitleWidth = fm.stringWidth(variable.name);
		g.setFont(standardFont);
		
		tableTopBorder = showHeading ? headingAscent + descent + kHeadingTableGap : 0;
		tableHeight = noOfCats * (ascent + descent) + (noOfCats - 1) * leading
																									+ 2 * kTableBorder;
		return true;
	}
	
	private int translateIndex(int index, int noOfCats) {
		return reverseOrder ? (noOfCats - index - 1) : index;
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		int noOfCats = variable.noOfCategories();
		initialise(variable, g);
		
		if (keyFillColor != null) {
			g.setColor(keyFillColor);
			g.fillRect(0, tableTopBorder, getSize().width, getSize().height - tableTopBorder);
			g.setColor(getForeground());
		}
		
		if (showHeading) {
			Font standardFont = g.getFont();
			g.setFont(headingFont);
			g.drawString(variable.name, kLeftRightBorder, headingAscent);
			g.setFont(standardFont);
		}
		
		if (!fillNotCross)
			setCrossSize(LARGE_CROSS);
		
		int baseLine = tableTopBorder + kTableBorder + ascent;
		Point p = new Point(kLeftRightBorder + kKeyRectWidth / 2, 0);
		
		for (int i=0 ; i<noOfCats ; i++) {
			int index = translateIndex(i, noOfCats);
			if (fillNotCross) {
				g.setColor(getForeground());
				g.drawRect(kLeftRightBorder, baseLine - ascent, kKeyRectWidth - 1, ascent + descent);
				g.setColor(catColour[index]);
				g.fillRect(kLeftRightBorder + 1, baseLine - ascent + 1, kKeyRectWidth - 2, ascent + descent - 1);
			}
			else {
				g.setColor(getCrossColor(index));
				p.y = baseLine - getCrossSize() - 2;
				drawMark(g, p, index);
			}
			variable.getLabel(index).drawRight(g, kLeftRightBorder + kKeyRectWidth + kRectCatGap,
																														baseLine);
			baseLine += (ascent + descent + leading);
		}
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(catKey)) {
			initialised = false;
			invalidate();
			repaint();
		}
	}
	
	public Dimension getMinimumSize() {
		CatVariable variable = (CatVariable)getVariable(catKey);
		initialise(variable, getGraphics());
		
		return new Dimension(2 * kLeftRightBorder + Math.max(catNameWidth + kKeyRectWidth + kRectCatGap, catTitleWidth),
									tableTopBorder + tableHeight);
	}
}