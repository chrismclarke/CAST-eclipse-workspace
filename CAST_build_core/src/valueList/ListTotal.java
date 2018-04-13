package valueList;

import java.awt.*;
import java.util.*;

import dataView.*;

public class ListTotal extends DataView {
	static public final int kTopBottomBorder = 3;
	
	private ScrollValueContent content;
	private boolean[] displayed;
	
	private boolean initialised = false;
	private int ascent, descent;
	
	public ListTotal(ScrollValueContent content, boolean[] displayed, DataSet theData, XApplet applet) {
		super(theData, applet, null);
		this.content = content;
		this.displayed = displayed;
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
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int baseline = kTopBottomBorder + ascent;
		
		int extraGap = content.findExtraColumnnGap();
		int leftPos = ScrollValueContent.kLeftRightBorder;
		boolean firstCol = true;
		Enumeration e = content.getListColumns(g).elements();
		int i = 0;
		while (e.hasMoreElements()) {
			if (!firstCol)
				leftPos += (ScrollValueContent.kValueGap + extraGap);
			ListColumn yColumn = (ListColumn)e.nextElement();
			if (displayed[i]) {
				g.setColor(Color.gray);
				g.drawLine(leftPos, 1, leftPos + yColumn.getColumnWidth(), 1);
				g.setColor(getForeground());
				yColumn.drawTotal(g, baseline, leftPos);
			}
			leftPos += yColumn.getColumnWidth();
			firstCol = false;
			i++;
		}
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


//-----------------------------------------------------------------------------------

	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
