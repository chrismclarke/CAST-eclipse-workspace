package statistic2;

import java.awt.*;

import dataView.*;


public class CatLabelsView extends DataView {
	static private final int kTopBottomBorder = 5;
	static private final int kLeftRightBorder = 10;
	static private final int kMaxExtraSpacing = 10;
	
	private Value values[] = null;
	
	public CatLabelsView(DataSet theData, XApplet applet) {
		super(theData, applet, null);
	}
	
	protected void clearValues() {
		values = null;
	}
	
	protected Value[] generateValues() {
		CatVariable catVar = getCatVariable();
		int noOfCats = catVar.noOfCategories();
		Value result[] = new Value[noOfCats];
		for (int i=0 ; i<noOfCats ; i++)
			result[i] = catVar.getLabel(i);
		return result;
	}
	
	private Value[] getValues() {
		if (values == null)
			values = generateValues();
		return values;
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
//		int fontAscent = fm.getAscent();
		int lineHt = fm.getHeight();
		Value val[] = getValues();
		int itemHeight = val.length * lineHt + 2 * kTopBottomBorder;
		int itemWidth = 0;
		for (int i=0 ; i<val.length ; i++)
			itemWidth = Math.max(itemWidth, val[i].stringWidth(g));
		itemWidth += 2 * kLeftRightBorder;
		
		return new Dimension(itemWidth, itemHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public Dimension maximumSize() {
		Dimension maxDim = getMinimumSize();
		maxDim.height += getValues().length * kMaxExtraSpacing;
		return maxDim;
	}
	
	protected int getLabelWidth(Variable variable, Graphics g) {
		return g.getFontMetrics().stringWidth(variable.name);
	}
	
	protected int getMaxValueWidth(Variable variable, Graphics g) {
		return variable.getMaxWidth(g);
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int lineHt = fm.getHeight();
		Value val[] = getValues();
		
		int slackPerGroup = Math.min(kMaxExtraSpacing,
					(getSize().height - val.length * lineHt - 2 * kTopBottomBorder) / val.length);
		
		int valRight = getSize().width - kLeftRightBorder;
		int baseline = kTopBottomBorder + fm.getAscent() + slackPerGroup / 2;
		
		for (int i=val.length-1 ; i>=0 ; i--) {
			val[i].drawLeft(g, valRight, baseline);
			baseline += (lineHt + slackPerGroup);
		}
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
