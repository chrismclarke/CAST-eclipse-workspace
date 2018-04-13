package stemLeaf;

import java.awt.*;

import dataView.*;
import valueList.*;


public class StemLeafValueView extends OneValueView {
//	static public final String STEM_LEAF_VALUE = "stemLeafValue";
	
	private int discardChars;
	
	public StemLeafValueView(DataSet theData, String variableKey, XApplet applet, int discardChars) {
		super(theData, variableKey, applet, null);
		this.discardChars = discardChars;
	}
	
	public void setDiscardChars(int discardChars) {
		this.discardChars = discardChars;
	}
	
	protected boolean highlightValue() {
		return false;
	}
	
	protected void drawValueText(Graphics g, String theValue, int left, int baseline,
																							int maxValueWidth) {
		int stemChars = Math.max(0, theValue.length() - discardChars - 1);
		int leafChars = theValue.length() - stemChars - discardChars;
		String stem = theValue.substring(0, stemChars);
		String leaf = theValue.substring(stemChars, stemChars + leafChars);
		String discard = theValue.substring(stemChars + leafChars);
		
		FontMetrics fm = g.getFontMetrics();
		int right = left + maxValueWidth;
		
		g.setColor(Color.gray);
		right -= fm.stringWidth(discard);
		g.drawString(discard, right, baseline);
		
		g.setColor(getForeground());
		right -= fm.stringWidth(leaf);
		g.drawString(leaf, right, baseline);
		
		g.setColor(Color.blue);
		right -= fm.stringWidth(stem);
		g.drawString(stem, right, baseline);
		
	}
}
