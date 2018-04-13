package valueList;

import java.awt.*;
import java.util.*;

import dataView.*;


public class ListHeading extends XPanel {
	static public final int kTopBottomBorder = 3;
	
	private ScrollValueContent content;
	private boolean initialised = false;
	private int ascent, descent;
	
	public ListHeading(ScrollValueContent content) {
		this.content = content;
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		initialised = true;
		return true;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		initialise(g);
		
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		
		int baseline = kTopBottomBorder + ascent;
		
		int extraGap = content.findExtraColumnnGap();
		int leftPos = ScrollValueContent.kLeftRightBorder;
		boolean firstCol = true;
		Enumeration e = content.getListColumns(g).elements();
		while (e.hasMoreElements()) {
			if (!firstCol)
				leftPos += (ScrollValueContent.kValueGap + extraGap);
			ListColumn yColumn = (ListColumn)e.nextElement();
			yColumn.drawHeading(g, baseline, leftPos);
			leftPos += yColumn.getColumnWidth();
			firstCol = false;
		}
	}
	
	public void resetVariables() {
		initialised = false;
		invalidate();
	}

	public Dimension getMinimumSize() {
		initialise(getGraphics());
		Dimension d = content.getMinimumSize();
		d.height = ascent + descent + 2 * kTopBottomBorder;
		return d;
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
