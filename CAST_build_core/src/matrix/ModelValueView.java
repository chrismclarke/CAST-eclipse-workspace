package matrix;

import java.awt.*;

import dataView.*;


public class ModelValueView extends DataView {
//	static public final String LINEAR_MODEL_VIEW = "linearModel";
	
	static final private String kEqualsString = "=";
	static final private int kEqualsGap = 12;
	
	static final private int kLeftRightBorder = 4;
	static final private int kTopBorder = 3;
	static final private int kBottomBorder = 3;
	static final private int kGenericHeadingTopBottom = 2;
	
	private ModelTerm yColumn, errorColumn;
	private ModelTerm xColumns[];
	private boolean showGenericHeading;
	
	private boolean initialised = false;
	
	protected int ascent, descent;
	private int yWidth, xWidth, errorWidth, equalsWidth;
	private int headingHt = 0;
	
	public ModelValueView(DataSet theData, XApplet applet, ModelTerm yColumn, ModelTerm[] xColumns,
										ModelTerm errorColumn, boolean showGenericHeading) {
		super(theData, applet, null);
		this.yColumn = yColumn;
		this.xColumns = xColumns;
		this.errorColumn = errorColumn;
		this.showGenericHeading = showGenericHeading;
	}

//--------------------------------------------------------------------------------
	
	final protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		else {
			doInitialisation(g);
			initialised = true;
			return true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		ascent = g.getFontMetrics().getAscent();
		descent = g.getFontMetrics().getDescent();
		
		yWidth = yColumn.maxLinearPartWidth(g, false);
		
		xWidth = 0;
		for (int i=0 ; i<xColumns.length ; i++) {
			boolean withPlus = i > 0;
			xWidth += xColumns[i].getGenericTermWidth(g, withPlus);
		}
		
		errorWidth = errorColumn.maxLinearPartWidth(g, true);
		
		equalsWidth = g.getFontMetrics().stringWidth(kEqualsString) + 2 * kEqualsGap;
		
		if (showGenericHeading) {
			headingHt = yColumn.getGenericTermHeight(g);
			for (int i=0 ; i<xColumns.length ; i++)
				headingHt = Math.max(headingHt, xColumns[i].getGenericTermHeight(g));
			headingHt = Math.max(headingHt, errorColumn.getGenericTermHeight(g));
			headingHt += 2 * kGenericHeadingTopBottom;
		}
	}

//--------------------------------------------------------------------------------
	
	private void drawHeading(Graphics g) {
		int horiz = kLeftRightBorder;
		int baseline = (headingHt + ascent - descent) / 2;
		
//		Color c = yColumn.getTermColor();
//		g.setColor(c == null ? getForeground() : c);
		yColumn.drawGenericTerm(horiz, baseline, false, g);
		horiz += yWidth;
		
		g.drawString(kEqualsString, horiz + kEqualsGap, baseline);
		horiz += equalsWidth;
		
		for (int i=0 ; i<xColumns.length ; i++) {
//			c = xColumns[i].getTermColor();
//			g.setColor(c == null ? getForeground() : c);
			
			boolean withPlus = i > 0;
			xColumns[i].drawGenericTerm(horiz, baseline, withPlus, g);
			horiz += xColumns[i].getGenericTermWidth(g, withPlus);
		}
		
//		c = errorColumn.getTermColor();
//		g.setColor(c == null ? getForeground() : c);
		errorColumn.drawGenericTerm(horiz, baseline, true, g);
	}
	
	private void drawValueFrame(Graphics g, boolean valSelected) {
		g.setColor(valSelected ? CoreMatrixView.kSelectedBackground : Color.white);
		g.fillRect(1, headingHt + 1, getSize().width - 2, getSize().height - headingHt - 2);
		
		g.setColor(getForeground());
		g.drawRect(0, headingHt, getSize().width - 1, getSize().height - headingHt - 1);
	}
	
	private void drawModelValue(Graphics g, Flags selection) {
		int horiz = kLeftRightBorder;
		int baseline = headingHt + kTopBorder + ascent;
		
		Color c = yColumn.getTermColor();
		g.setColor(c == null ? getForeground() : c);
		yColumn.drawLinearPartString(selection, horiz, baseline, false, g);
		horiz += yWidth;
		
		g.drawString(kEqualsString, horiz + kEqualsGap, baseline);
		horiz += equalsWidth;
		
		for (int i=0 ; i<xColumns.length ; i++) {
			c = xColumns[i].getTermColor();
			g.setColor(c == null ? getForeground() : c);
			
			boolean withPlus = i > 0;
			xColumns[i].drawLinearPartString(selection, horiz, baseline, withPlus, g);
			horiz += xColumns[i].getGenericTermWidth(g, withPlus);
		}
		
		c = errorColumn.getTermColor();
		g.setColor(c == null ? getForeground() : c);
		errorColumn.drawLinearPartString(selection, horiz, baseline, true, g);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		if (showGenericHeading)
			drawHeading(g);
		
		Flags selection = getSelection();
		int firstSelectedRow = getSelection().findFirstSetFlag();
					//	Assumes that if more than one row selected that all have same mean
		drawValueFrame(g, firstSelectedRow >= 0);
		
		if (firstSelectedRow >= 0)
			drawModelValue(g, selection);
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int width = 2 * kLeftRightBorder + yWidth + xWidth + errorWidth + equalsWidth;
		int height = headingHt + ascent + descent + kTopBorder + kBottomBorder;
		
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
		
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
